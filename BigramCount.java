package org.bigramorg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.io.IOException;
import java.util.regex.Pattern;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.util.StringUtils;
import opennlp.tools.tokenize.SimpleTokenizer;
//import OpenNlpTest;
import org.apache.log4j.Logger;
import opennlp.tools.tokenize.*;
public class BigramCount extends Configured implements Tool {

  private static final Logger LOG = Logger.getLogger(BigramCount.class);

  public static void main(String[] args) throws Exception {
    int res = ToolRunner.run(new BigramCount(), args);
    System.exit(res);
  }

  public int run(String[] args) throws Exception {
    Job job = Job.getInstance(getConf(), "bigramcount");
    for (int i = 0; i < args.length; i += 1) {
      if ("-skip".equals(args[i])) {
        job.getConfiguration().setBoolean("bigramcount.skip.patterns", true);
	job.getConfiguration().setBoolean("bigramcount.case.sensitive",false);
        i++;
	job.addCacheFile(new Path(args[i]).toUri());
	LOG.info("added file to distributed cache accessible to namenodes: " + args[i]);
        i++;
        job.addCacheFile(new Path(args[i]).toUri());
      
      }
    }
    //job.addCacheFile(new Path("/user/cloudera/wordcount/en-token.bin").toUri());
    job.setJarByClass(this.getClass());
    job.addArchiveToClassPath(new Path("/home/cloudera/opennlp/apache-opennlp-1.6.0/lib/opennlp-tools-1.6.0.jar"));
    job.addArchiveToClassPath(new Path("/home/cloudera/opennlp/apache-opennlp-1.6.0/lib/opennlp-uima-1.6.0.jar"));
    // Use TextInputFormat, the default unless job.setInputFormatClass is used
    FileInputFormat.addInputPath(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));
    job.setMapperClass(Map.class);
    job.setCombinerClass(Reduce.class);
    job.setReducerClass(Reduce.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(IntWritable.class);
    return job.waitForCompletion(true) ? 0 : 1;
  }

  public static class Map extends Mapper<LongWritable, Text, Text, IntWritable> {
    private final static IntWritable one = new IntWritable(1);
    private Text word = new Text();
    //private SimpleTokenizer tokenizer = OpenNlpTest.getSimpleTokenizer();
    private boolean caseSensitive = false;
    private long numRecords = 0;
    private String input;
    private TokenizerME tokenizers;
    private Set<String> patternsToSkip = new HashSet<String>();
    private ArrayList<String> tempData = new ArrayList<String>();
	private static final Pattern word_b = Pattern.compile("\\s*\\b\\s*");

    protected void setup(Context context)
        throws IOException,
        InterruptedException {
      if (context.getInputSplit() instanceof FileSplit) {
        this.input = ((FileSplit) context.getInputSplit()).getPath().toString();
      } else {
        this.input = context.getInputSplit().toString();
      }
      Configuration config = context.getConfiguration();
      URI[] local_path = context.getCacheFiles(); 
      LOG.info("size of cached file:" + local_path[0]);
      tokenizers = OpenNlpTest.getTokenizer(local_path[1]);
      this.caseSensitive = config.getBoolean("bigramcount.case.sensitive", false);
      if (config.getBoolean("bigramcount.skip.patterns", false)) {
        	URI[] localPaths = context.getCacheFiles();
        	parseSkipFile(localPaths[0]);
      }
    }

    private void parseSkipFile(URI patternsURI) {
      LOG.info("Added file to the distributed cache: " + patternsURI);
      try {
        BufferedReader finput = new BufferedReader(new FileReader(new File(patternsURI.getPath()).getName()));
        String pattern;
        while ((pattern = finput.readLine()) != null) {
          patternsToSkip.add(pattern);
        }
      } catch (IOException ioe) {
        System.err.println("Caught exception while parsing the cached file '"
            + patternsURI + "' : " + StringUtils.stringifyException(ioe));
      }
    }

    public void map(LongWritable offset, Text lines, Context context)
        throws IOException, InterruptedException {
      String line = lines.toString();
      if (!caseSensitive) {
        line = line.toLowerCase();
      }
      Text currentWord = new Text();
	   
      for (String word : tokenizers.tokenize(line) ){//OpenNlpTest.getSimpleTokenizer(line)) {
        if (word.isEmpty() || patternsToSkip.contains(word)|| word.length() <= 2) {
            continue;
        }
		tempData.add(word);
            	currentWord = new Text(word);
            	context.write(currentWord,one);
        }           
	  for( int i = 0 ; i < tempData.size() - 1;i++){
			currentWord = new Text(tempData.get(i)+" "+ tempData.get(i+1));
			context.write(currentWord,one);
	  }
	  tempData.clear();
    }
  }

  public static class Reduce extends Reducer<Text, IntWritable, Text, IntWritable> {
    @Override
    public void reduce(Text word, Iterable<IntWritable> counts, Context context)
        throws IOException, InterruptedException {
      int sum = 0;
      for (IntWritable count : counts) {
        	sum += count.get();
      }
      context.write(word, new IntWritable(sum));
    }
  }
}


package org.bigramorg;

import opennlp.tools.cmdline.PerformanceMonitor;
import opennlp.tools.cmdline.postag.POSModelLoader;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSSample;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.WhitespaceTokenizer;
//import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.util.Span;
import opennlp.tools.tokenize.*;
import opennlp.tools.tokenize.SimpleTokenizer;
import java.io.File;
import java.io.*;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;

public class OpenNlpTest {

	private static TokenizerME tokenizers;
	public static TokenizerME getTokenizer(URI model_path){
		TokenizerModel model = null;
		InputStream modelIn = null;
		try{
			modelIn = new FileInputStream(new File(model_path.getPath()).getName());
			model = new TokenizerModel(modelIn);
		
		} catch(IOException e){
			e.printStackTrace();
		}finally{
			if(modelIn != null){
				try{
					modelIn.close();
				}catch(IOException e1){
				}
			}
		}
		tokenizers = new TokenizerME(model);
		return tokenizers;
	}

	public static String[] getWhitespaceTokenizer(String line){
		return WhitespaceTokenizer.INSTANCE.tokenize(line);
	}

	public static String[] getSimpleTokenizer(String line){
		return SimpleTokenizer.INSTANCE.tokenize(line);
	}

	public static void getPosTag(String pathName) throws IOException {
    		POSModel model = new POSModelLoader().load(new File(pathName));
    		PerformanceMonitor perfMon = new PerformanceMonitor(System.err, "sent");
    		POSTaggerME tagger = new POSTaggerME(model);

    		String input = "Can anyone help me dig through OpenNLP's horrible documentation?";
    		ObjectStream<String> lineStream =  new PlainTextByLineStream(new StringReader(input));

    		perfMon.start();
    		String line;
    		while ((line = lineStream.read()) != null) {

        		String[] whitespaceTokenizerLine = WhitespaceTokenizer.INSTANCE.tokenize(line);
        		String[] tags = tagger.tag(whitespaceTokenizerLine);

        		POSSample sample = new POSSample(whitespaceTokenizerLine, tags);
        		System.out.println(sample.toString());

    	    		perfMon.incrementCounter();
    		}
    		perfMon.stopAndPrintFinalResult();
	}
}

# OpenNLPwithHadoopTokenizationPOStagFeature
Goal of this project is to generate the bigrams, unigrams, count them and pos tag feature generation for bigrams and unigrams.

Before we can generate the bigrams, we need to tokenize them. There are different kind of tokenizing techniques available in natural language
processing. For instance, in python NLTK, it has TwitterTokenizer, MoseTokenizer, WhiteSpaceTokenizer etc. In OpenNLP of Java, it has
three kinds of Tokenizer WhiteSpace, Simple, TokenizerME. Each will produce different kind of tokenizing result depending on your input data
set. I am not going into details of these algorithms about what they do and how they do.

POS tagger generates the parts of speech tag for each tokenized word or bigrams. its a entropy based markov chain algorithm. it can be used
as a feature in text analytics.

For this programe better not to use the POS tag function unless u try to change output type of mapper object. One more thing, POS tag is 
document context dependant that means similar word can have different pos tag depending on the context and that thing need to be considered. Therefore i will write a different Hadoop application to generate postag for different document.


Tokenization functions are readilly usable.

The challenge i faced when i tried to run the program is, how i can incorporate the external jars from OpenNlp library. it can be done
two ways:

1. u can install the OpenNLP library to each machine using shell or python script.
2. u can push the external jar to distributed cache make it available to all the machines.

I chose the second step. my external library path is /home/cloudera/opennlp/apache-opennlp-1.6.0/lib/*. So i add this path to Hadoop HDFS system. and made this available to all machine by adding the path into distributed cache by using following java line.

Job.addArchivetoClassPath(new Path());

Following steps related to project compilation, jar archive file generation and submit the job into hadoop clusters. three steps opeartion:

javac -cp /usr/lib/hadoop/*:/usr/lib/hadoop-mapreduce/*:/home/cloudera/opennlp/apache-opennlp-1.6.0/lib/* BigramCount.java /
OpenNlpTest.java -d build -Xlint

creating the jar files
jar -cvf bigramcount.jar org.bigramorg.BigramCount -C build/ .

submitting the job into Hadoop Cluster
hadoop jar bigramcount.jar org.bigramorg.BigramCount /user/cloudera/wordcount/input /user/cloudera/wordcount/output /
-skip /user/cloudera/wordcount/stop_words.text /user/cloudera/wordcount/en-token.bin


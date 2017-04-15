# OpenNLPwithHadoopTokenizationPOStagFeature
Goal of this project is to generate the bigrams, unigrams, count them and pos tag feature generation for bigrams and unigrams.

Before we can generate the bigrams, we need to tokenize them. There are different kind of tokenizing techniques available in natural language
processing. For instance, in python NLTK, it has TwitterTokenizer, MoseTokenizer, WhiteSpaceTokenizer etc. In OpenNLP of Java, it has
three kinds of Tokenizer WhiteSpace, Simple, TokenizerME. Each will produce different kind of tokenizing result depending on your input data
set. I am not going into details of these algorithms about what they do and how they do.

POS tagger generates the parts of speech tag for each tokenized word or bigrams. its a entropy based markov chain algorithm. it can be used
as a feature in text analytics.

compiling the java files along with external jar
javac -cp /usr/lib/hadoop/*:/usr/lib/hadoop-mapreduce/*:/home/cloudera/opennlp/apache-opennlp-1.6.0/lib/* BigramCount.java /
OpenNlpTest.java -d build -Xlint

creating the jar files
jar -cvf bigramcount.jar org.bigramorg.BigramCount -C build/ .

submitting the job into Hadoop Cluster
hadoop jar bigramcount.jar org.bigramorg.BigramCount /user/cloudera/wordcount/input /user/cloudera/wordcount/output /
-skip /user/cloudera/wordcount/stop_words.text /user/cloudera/wordcount/en-token.bin


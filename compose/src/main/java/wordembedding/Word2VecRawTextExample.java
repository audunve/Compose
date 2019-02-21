package wordembedding;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.sentenceiterator.BasicLineIterator;
import org.deeplearning4j.text.sentenceiterator.LineSentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentencePreProcessor;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.deeplearning4j.ui.standalone.ClassPathResource;

/**
 * Created by agibsonccc on 10/9/14.
 *
 * Neural net that processes text into wordvectors. See below url for an in-depth explanation.
 * https://deeplearning4j.org/word2vec.html
 */
@SuppressWarnings("deprecation")
public class Word2VecRawTextExample {
	
	public static void main(String[] args) throws IOException {
	
	String filePath = new ClassPathResource("raw_sentences.txt").getFile().getAbsolutePath();

    // Strip white space before and after for each line
    
    SentenceIterator iter = new LineSentenceIterator(new File("./files/skybrary_original.txt"));
    iter.setPreProcessor(new SentencePreProcessor() {
        public String preProcess(String sentence) {
            return sentence.toLowerCase();
        }
    });
    
    TokenizerFactory t = new DefaultTokenizerFactory();
    t.setTokenPreProcessor(new CommonPreprocessor());
    
    Word2Vec vec = new Word2Vec.Builder()
            .minWordFrequency(5)
            .layerSize(100)
            .seed(42)
            .windowSize(5)
            .iterate(iter)
            .tokenizerFactory(t)
            .build();

    System.err.println("Fitting Word2Vec model....");
    vec.fit();
    
 // Write word vectors
    WordVectorSerializer.writeWordVectors(vec, "./files/skybrary_Word2Vec_model.txt");

    System.err.println("Closest Words:");
    Collection<String> lst = vec.wordsNearest("day", 10);
    System.out.println(lst);

    
    double cosSim = vec.similarity("aircraft", "airport");
    System.out.println(cosSim);
    
    Collection<String> lst3 = vec.wordsNearest("airport", 10);
    System.out.println(lst3);
    
	}
	
}

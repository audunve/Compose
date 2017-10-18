package compose.wordnet;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import compose.misc.StringUtils;
import edu.smu.tspell.wordnet.WordNetDatabase;
import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Pointer;
import net.didion.jwnl.data.PointerType;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.Word;
import net.didion.jwnl.dictionary.Dictionary;

import org.apache.lucene.analysis.Analyzer;

/**
 * Methods using the lexicon WordNet. The library used is JWNL.
 *
 * @author Audun
 *
 */
public class WordNetOperations
{
	final static POS pos = POS.NOUN;
	
	/**
	 * Retrieves synsets from WordNet associated with an input word
	 * @param inputWord The input word for which synsets will be retrieved 
	 * @return Synsets associated with an input word
	 * @throws FileNotFoundException
	 * @throws JWNLException
	 */
	public static Synset[] getSynsets (String inputWord) throws FileNotFoundException, JWNLException {
		
		JWNL.initialize(new FileInputStream("/Users/audunvennesland/git/Compose/compose/file_property.xml"));
	    Dictionary dictionary = Dictionary.getInstance();
	    
	    String token = StringUtils.stringTokenize(inputWord, true);

	    IndexWord indexWord = dictionary.lookupIndexWord(pos, token);

		Synset[] synsets = indexWord.getSenses();
	    

		return synsets;
		
	}
	
	/**
	 * A method that checks if an input word is present in WordNet
	 * @param inputWord The input word for which presens in WordNet is checked
	 * @return a boolean stating whether or not the input word resides in WordNet
	 * @throws FileNotFoundException
	 * @throws JWNLException
	 */
	public static boolean containedInWordNet(String inputWord) throws FileNotFoundException, JWNLException {
		
		Synset[] synsets = getSynsets(inputWord);
		
		if (synsets.length > 0)
		{
			return true;
		}
		else
		{
			return false;
		}		
		
	}
	
/*
	public static String getDomain(String inputWord) throws JWNLException, FileNotFoundException {
		
		
		String domain = null;
		
		JWNL.initialize(new FileInputStream("/Users/audunvennesland/git/Compose/compose/file_property.xml"));
	    final Dictionary dictionary = Dictionary.getInstance();
		
		IndexWord token = dictionary.lookupIndexWord(pos, inputWord); 
		
		//get the synsets
		Synset[] synsets = getSynsets(inputWord);
		
		long offsetAttribute = 0;
		
		//get the offset attributes for each synset
		for (Synset synset : synsets) {
			offsetAttribute = synset.getOffset();
		}
		
		return domain;
		
	}*/
	
	/*public static void readWDDomains(File file) {

		FileInputStream fis = null;
		BufferedInputStream bis = null;
		DataInputStream dis = null;

		try {
			fis = new FileInputStream(file);

			// Here BufferedInputStream is added for fast reading.
			bis = new BufferedInputStream(fis);
			dis = new DataInputStream(bis);

			// dis.available() returns 0 if the file does not have more lines.
			while (dis.available() != 0) {

				// this statement reads the line from the file and print it to
				// the console.
				System.out.println(dis.readLine());
			}

			// dispose all the resources after using them.
			fis.close();
			bis.close();
			dis.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}*/
		
    public static void main(final String[] args) throws FileNotFoundException, JWNLException, OWLOntologyCreationException
    {
    	
    	String inputWord = "test";
    	
    	Synset[] synsets = getSynsets(inputWord);
    	
    	System.out.println("Printing synsets to " + inputWord);
    	for (Synset synset : synsets) {
    		System.out.println(synset.toString());
    	}
    	
    	//get domain
    	System.out.println("The (first) offset attribute associated with " + inputWord + " is: ");
    	for (Synset synset : synsets) {
    		System.out.println(synset.getOffset());
    	}
    	
    	File wdDomains = new File("./files/WDDomains/wn-domains-3.2-20070223");
    	//readWDDomains(wdDomains);
    	
    	File ontoFile1 = new File("./files/OAEI-16-conference/ontologies/Biblio_2015.rdf");
    	OWLOntologyManager manager = OWLManager.createOWLOntologyManager();		
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile1);
		
		JWNL.initialize(new FileInputStream("/Users/audunvennesland/git/Compose/compose/file_property.xml"));
	    Dictionary dictionary = Dictionary.getInstance();
		
		Set<OWLClass> classes = onto.getClassesInSignature();
		for (OWLClass cls : classes) {
			String input = StringUtils.stringTokenize(cls.getIRI().getFragment(), true);
			//System.out.println("Trying " + input);
			 IndexWord indexWord = dictionary.lookupIndexWord(pos, input);
			 if (indexWord != null) {
			// System.out.println("Indexword is " + indexWord.toString());
			 } else {
				 //System.out.println("No Indexword for " + input);
			 }
			 
			 Synset[] syns = getSynsets(indexWord.toString());
			 for (int i = 0; i < syns.length; i++) {
				 //System.out.println((syns[i].toString()));
				 //System.out.println("The domain is " + getDomain(syns[i].toString()));
			 }
		}
		

}
}
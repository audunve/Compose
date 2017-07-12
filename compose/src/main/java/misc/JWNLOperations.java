package misc;

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

import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Pointer;
import net.didion.jwnl.data.PointerType;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.Word;
import net.didion.jwnl.dictionary.Dictionary;
import no.ntnu.idi.compose.preprocessing.Preprocessor;

import org.apache.lucene.analysis.Analyzer;

/**
 * Demonstration of the core features of Java WordNet library by John Didion
 *
 * @author Roland Kluge
 *
 */
public class JWNLOperations
{
	final static POS pos = POS.NOUN;
	
	public static Synset[] getSynsets (String inputWord) throws FileNotFoundException, JWNLException {
		
		JWNL.initialize(new FileInputStream("/Users/audunvennesland/git/Compose/compose/file_property.xml"));
	    Dictionary dictionary = Dictionary.getInstance();
	    
	    String token = Preprocessor.stringTokenize(inputWord, true);

	    IndexWord indexWord = dictionary.lookupIndexWord(pos, token);

		Synset[] synsets = indexWord.getSenses();
	    

		return synsets;
		
	}
	
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
		
	}
	
	public static void readWDDomains(File file) {

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
	}
		
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
			String input = Preprocessor.stringTokenize(cls.getIRI().getFragment(), true);
			System.out.println("Trying " + input);
			 IndexWord indexWord = dictionary.lookupIndexWord(pos, input);
			 if (indexWord != null) {
			 System.out.println("Indexword is " + indexWord.toString());
			 } else {
				 System.out.println("No Indexword for " + input);
			 }
			 
			 Synset[] syns = getSynsets(indexWord.toString());
			 for (int i = 0; i < syns.length; i++) {
				 System.out.println((syns[i].toString()));
				 System.out.println("The domain is " + getDomain(syns[i].toString()));
			 }
		}
		

}
}

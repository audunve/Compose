package no.ntnu.idi.compose.WordNet;

import java.io.File;
import java.util.Iterator;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import edu.smu.tspell.wordnet.*;


public class WordNetLexicon
{
		
	//TO-DO: Currently there is no preprocessing of the input word (stemming, tokenization, etc.) and this should be implemented!
	public static boolean containedInWordNet(String inputWord) {
		
		System.setProperty("wordnet.database.dir", "/Users/audunvennesland/Documents/PhD/Development/WordNet/WordNet-3.0/dict");
		WordNetDatabase database = WordNetDatabase.getFileInstance();
		Synset[] synsets = database.getSynsets(inputWord);
		
		if (synsets.length > 0)
		{
			return true;
		}
		else
		{
			return false;
		}		
		
	}

	public static void main(String[] args) throws OWLOntologyCreationException
	{

			
			File ontoFile = new File("/Users/audunvennesland/Documents/PhD/Ontologies/OAEI/OAEI2015/Biblio/Biblio_2015.rdf");
			
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();		
			OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);
			Iterator<OWLClass> itr = onto.getClassesInSignature().iterator();
			
			String thisClass;
			
			int numClasses = onto.getClassesInSignature().size();
			
			int counter = 0;
			
			while(itr.hasNext()) {
				thisClass = itr.next().getIRI().getFragment();
				if (containedInWordNet(thisClass) == true) {
					counter++;	
					System.out.println(thisClass + " is listed in WordNet");
				} else {
				System.out.println(thisClass + " is not listed in WordNet");
				}
			}
			
			double wordNetCoverage = (double)counter / (double)numClasses;
			
			System.out.println("Number of classes in BIBLIO: " + numClasses);
			System.out.println("Number of classes in BIBLIO listed in WordNet: " + counter);
			
			System.out.println("The WordNet Coverage of BIBLIO is: " + wordNetCoverage);
			

			
		
	}

}

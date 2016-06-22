package no.ntnu.idi.compose.Matchers;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

//import org.ivml.alimo.ISub;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDocumentFormat;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import fr.inrialpes.exmo.ontowrap.Ontology;

/**
 * @author audunvennesland
 * @version 1.0
 * @created 21-apr-2016 10:34:51
 * Using the iSub algorithm to match strings
 */
public class BasicStringMatcher implements Matcher {

	/**
	 * 
	 * @param onto1
	 * @param onto2
	 */
	@SuppressWarnings("rawtypes")
	public Map matchOntologies(File ontoFile1, File ontoFile2) throws OWLOntologyCreationException{

		Map mapping = new HashMap();
		Map classSet = new HashMap();
		//ISub isub = new ISub();

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto1 = manager.loadOntologyFromOntologyDocument(ontoFile1);
		OWLOntology onto2 = manager.loadOntologyFromOntologyDocument(ontoFile2);

		Iterator<OWLClass> itr_onto1 = onto1.getClassesInSignature().iterator();
		Iterator<OWLClass> itr_onto2 = onto2.getClassesInSignature().iterator();
		int length = onto2.getClassesInSignature().size();
		double[] score = null;

		String thisClass;
		String thatClass;

		while (itr_onto1.hasNext()) {
			thisClass = itr_onto1.next().getIRI().getFragment();	
			for (int i = 0; i < length; i++) {

				thatClass = itr_onto2.next().getIRI().getFragment();
				//score[i] = isub.score(thisClass, thatClass);
				classSet.put(thisClass, thatClass);
				//System.out.println(score[i]);
				mapping.put(classSet, score[i]);
			}

		}

		manager.removeOntology(onto1);
		manager.removeOntology(onto2);


		return mapping;

	}

	/**
	 * 
	 * @param onto1
	 * @param onto2
	 * @param matcherComposition
	 */
	public Map matchOntologies(File onto1, File onto2, Map matcherComposition){
		return matcherComposition;

	}

	public static void main(String[] args) throws OWLOntologyCreationException {
		
		BasicStringMatcher matcher = new BasicStringMatcher();

		//import the owl files
		File file1 = new File("/Users/audunvennesland/Documents/PhD/Ontologies/Cultural Heritage/BIBO/BIBO.owl");
		//File file2 = new File("/Users/audunvennesland/Documents/PhD/Ontologies/Cultural Heritage/Bibtex Ontology/BibTex.owl");
		File file2 = new File("/Users/audunvennesland/Documents/PhD/Ontologies/OAEI/OAEI2015/Biblio/Biblio_2015.rdf");
		
		Map newMap = matcher.matchOntologies(file1, file2);
		
		System.out.println(newMap.toString());
	}

}
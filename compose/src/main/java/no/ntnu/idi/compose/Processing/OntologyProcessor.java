package no.ntnu.idi.compose.Processing;


import java.io.File;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Map;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import fr.inrialpes.exmo.ontowrap.LoadedOntology;
import fr.inrialpes.exmo.ontowrap.OntowrapException;
import no.ntnu.idi.compose.Loading.OWLLoader;

/**
 * @author audunvennesland
 * @version 1.0
 * @created 21-apr-2016 10:34:52
 */
public class OntologyProcessor {

	public OntologyProcessor(){

	}

	static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

	/**
	 * 
	 * @param inputOntology
	 */
	public double computeSparsityProfile(File onto){
		
		
		return 0;
	}

	/**
	 * Average Depth (AD): The average depth (D) of the classes in an ontology as a
	 * mean of the depth of over all classes. AD = D(Ci) / |C|
	 * 
	 * @param onto1
	 * @param onto2
	 */
	public double computeAverageDepth(File onto){
		return 0;
	}

	/**
	 * Average Population (AP): Indicates the average distribution of instances across
	 * all classes. AP = |I| / |C|.
	 * 
	 * @param onto1
	 * @param onto2
	 * @throws OWLOntologyCreationException 
	 */
	public static double computeAveragePopulation(File onto1, File onto2) throws OWLOntologyCreationException{

		int classes = OWLLoader.getNumClasses(onto1, onto2);
		int individuals = OWLLoader.getNumIndividuals(onto1, onto2);
		
		double averagePopulation = (double)individuals / (double)classes;
		return averagePopulation;
	}

	/**
	 * Class Richness (CR): The ratio of the number of classes for which instances
	 * (Ci) exist (|Ci|) divided by the total number of classes in the ontology
	 * 
	 * @param onto1
	 * @param onto2
	 * @throws OWLOntologyCreationException 
	 */
	public static double computeClassRichness(File inputFile) throws OWLOntologyCreationException{
		
		OWLOntology localOnt = manager.loadOntologyFromOntologyDocument(inputFile);
		//get all classes and put them in a data structure
		Iterator<OWLClass> itr = localOnt.getClassesInSignature().iterator();
		
		//counter to keep track of num classes with individuals
		int counter = 0;
		
		//
		double classRichness = 0;
		
		while(itr.hasNext()) {
			if (OWLLoader.containsIndividuals(itr.next()) == true) {
				counter++;
			}
		}
		
		classRichness = (double)counter / (double)localOnt.getClassesInSignature().size();
		
		return classRichness;
	}

	/**
	 * Inheritance Richness is the average number of subclasses (Ci) per class (C). IR
	 * = HC (C, Ci) / C
	 * 
	 * @param onto1
	 * @param onto2
	 */
	public double computeInheritanceRichness(LoadedOntology onto1, LoadedOntology onto2){
		return 0;
	}

	/**
	 * Null label and comment (N): The percentage of terms with no comment nor label
	 * (NCi). N = |NCi| / |T|.
	 * 
	 * @param onto1
	 * @param onto2
	 */
	public double computeNullLabelOrComment(LoadedOntology onto1, LoadedOntology onto2){
		return 0;
	}

	/**
	 * Relationship Richness (RR): The percentage of object properties (P) that are
	 * different from subClassOf relations (SC) -> RR = |P| / |SC| + |P|
	 * 
	 * @param onto1
	 * @param onto2
	 */
	public double computeRelationshipRichness(File onto1, File onto2){
		
		int numSubClasses;
		int numObjectProperties;
		
		
		return 0;
	}

	/**
	 * WordNet Coverage (WC): The percentage of terms with label or local name present
	 * in WordNet.
	 * 
	 * @param onto1
	 * @param onto2
	 */
	public double computeWordNetCoverage(LoadedOntology onto1, LoadedOntology onto2){
		return 0;
	}

	/**
	 * Using an ontology concept id as key and possible search keywords as value for
	 * the Map keywords in the input parameter. Further the URI to the web service (e.
	 * g. DBPedia Spotlight) and a confidence value stating the confidence for the KB
	 * annotation.
	 * 
	 * The return value is a map where the key is ontology concept id and the return
	 * value is the instances as value in the map.
	 * 
	 * @param keywords
	 * @param URI
	 * @param confidence
	 */
	public Map findEnrichment(Map keywords, String URI, double confidence){
		// FIXME Auto-generated method stub
		return null;
	}

	public String getOntologyFormat(File ontologyFile) {
		// FIXME Auto-generated method stub
		return null;
	}

	public Map FindEnrichment(Map keywords, String URI, double confidence) {
		// FIXME Auto-generated method stub
		return null;
	}
	
	public static void main(String[] args) throws OWLOntologyCreationException, URISyntaxException, OntowrapException {
		
		File onto1 = new File("/Users/audunvennesland/Documents/PhD/Ontologies/Cultural Heritage/BIBO/BIBO.owl");
		File onto2 = new File("/Users/audunvennesland/Documents/PhD/Ontologies/Cultural Heritage/Bibtex Ontology/BibTex.owl");

		System.out.println("The Average Population (AP) is: " + computeAveragePopulation(onto1, onto2));
		
		System.out.println("The Class Richness (CR) is: " + computeClassRichness(onto1));
	}

}
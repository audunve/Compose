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
	 * This should be computed from the Average Population and Class Richness
	 * @param inputOntology
	 */
	public double computeSparsityProfile(File ontoFile){
		
		
		return 0;
	}

	/**
	 * Average Depth (AD): The average depth (D) of the classes in an ontology as a
	 * mean of the depth of over all classes. AD = D(Ci) / |C|
	 * ? Would a strategy be to measure the edges from each class to the root (Thing) and divide this from num classes in the ontology ?
	 * 
	 * @param onto1
	 * @param onto2
	 */
	public double computeAverageDepth(File ontoFile1, File ontoFile2){
		
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
	public static double computeAveragePopulation(File ontoFile) throws OWLOntologyCreationException {

		int classesOnto = OWLLoader.getNumClasses(ontoFile);
		
		int individualsOnto = OWLLoader.getNumIndividuals(ontoFile);
		
		double averagePopulation = ((double)individualsOnto / ((double)classesOnto));
		
		return averagePopulation;
	}
	
	/**
	 * Average Population (AP): Indicates the average distribution of instances across
	 * all classes. AP = |I| / |C|.
	 * 
	 * @param onto1
	 * @param onto2
	 * @throws OWLOntologyCreationException 
	 */
	public static double computeAveragePopulation(File ontoFile1, File ontoFile2) throws OWLOntologyCreationException {

		int classesOnto1 = OWLLoader.getNumClasses(ontoFile1);
		int classesOnto2 = OWLLoader.getNumClasses(ontoFile2);
		int individualsOnto1 = OWLLoader.getNumIndividuals(ontoFile1);
		int individualsOnto2 = OWLLoader.getNumIndividuals(ontoFile2);
		
		double averagePopulation = ((double)individualsOnto1 + (double)individualsOnto2) / ((double)classesOnto1 + (double)classesOnto2);
		
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
	public static double computeClassRichness(File ontoFile) throws OWLOntologyCreationException{
		
		int numClassesContainingIndividuals = OWLLoader.containsIndividuals(ontoFile);
		int numClassesInTotal = OWLLoader.getNumClasses(ontoFile);
		
		double classRichness = (double)numClassesContainingIndividuals / (double)numClassesInTotal;

		return classRichness;
	}

	/**
	 * Inheritance Richness (IR): This metric can distinguish a horizontal ontology from a vertical ontology or an ontology with different levels of specialization. 
	 * Inheritance Richness is the average number of subclasses (Ci) per class (C). IR = HC (C, Ci) / C
	 * 
	 * @param onto1
	 * @param onto2
	 * @throws OWLOntologyCreationException 
	 */
	public static double computeInheritanceRichness(File ontoFile1, File ontoFile2) throws OWLOntologyCreationException{
		
		int numSubClassesOnto1 = OWLLoader.getNumSubClasses(ontoFile1);
		int numSubClassesOnto2 = OWLLoader.getNumSubClasses(ontoFile2);
		int numClassesOnto1 = OWLLoader.getNumClasses(ontoFile1);
		int numClassesOnto2 = OWLLoader.getNumClasses(ontoFile2);	

		return (double)(numSubClassesOnto1 + numSubClassesOnto2) / (double)(numClassesOnto1 + numClassesOnto2);
	}
	
	public static double computeInheritanceRichness(File ontoFile1) throws OWLOntologyCreationException{
		
		int numSubClassesOnto1 = OWLLoader.getNumSubClasses(ontoFile1);
		int numClassesOnto1 = OWLLoader.getNumClasses(ontoFile1);

		return (double)(numSubClassesOnto1) / (double)(numClassesOnto1);
	}

	/**
	 * Null label and comment (N): The percentage of concepts with no comment nor label
	 * (NCi), so basically the concepts with no comment nor label divided by all concepts: N = |NCi| / |T|.
	 * For now this metric only focuses on the classes, but the same principles apply for object properties and data properties as well
	 * 
	 * @param onto1
	 * @param onto2
	 * @throws OWLOntologyCreationException 
	 */
	public static double computeNullLabelOrComment(File ontoFile1, File ontoFile2) throws OWLOntologyCreationException{
		
		int numClassesWithoutComments = OWLLoader.getNumClassesWithoutComments(ontoFile1) + OWLLoader.getNumClassesWithoutComments(ontoFile2);
		int numClassesWithoutLabels = OWLLoader.getNumClassesWithoutLabels(ontoFile1) + OWLLoader.getNumClassesWithoutLabels(ontoFile2);
		int numClasses = OWLLoader.getNumClasses(ontoFile1) + OWLLoader.getNumClasses(ontoFile2);
		
		return (((double)numClassesWithoutComments / (double)numClasses) + ((double)numClassesWithoutLabels / (double)numClasses)) / 2;
	}
	
	public static double computeNullLabelOrComment(File ontoFile1) throws OWLOntologyCreationException{
		
		int numClassesWithoutComments = OWLLoader.getNumClassesWithoutComments(ontoFile1);
		int numClassesWithoutLabels = OWLLoader.getNumClassesWithoutLabels(ontoFile1);
		int numClasses = OWLLoader.getNumClasses(ontoFile1);
		
		return (((double)numClassesWithoutComments / (double)numClasses) + ((double)numClassesWithoutLabels / (double)numClasses)) / 2;
	}

	/**
	 * Relationship Richness (RR): This metric reflects the diversity of relations and placement of relations in the ontology. The percentage of object properties (P) that are
	 * different from subClassOf relations (SC) -> RR = |P| / (|SC| + |P|). If an ontology has an RR close to zero, that would indicate that most of the relationships are is-a relations.
	 * 
	 * @param onto1
	 * @param onto2
	 * @throws OWLOntologyCreationException 
	 */
	public static double computeRelationshipRichness(File ontoFile1, File ontoFile2) throws OWLOntologyCreationException{
		
		int numSubClasses = OWLLoader.getNumSubClasses(ontoFile1) + OWLLoader.getNumSubClasses(ontoFile2);
		int numObjectProperties = OWLLoader.getNumObjectProperties(ontoFile1) + OWLLoader.getNumObjectProperties(ontoFile2);
		
		return  (double)numObjectProperties / ((double)numSubClasses + (double)numObjectProperties);
	}
	
	public static double computeRelationshipRichness(File ontoFile1) throws OWLOntologyCreationException{
		
		int numSubClasses = OWLLoader.getNumSubClasses(ontoFile1);
		int numObjectProperties = OWLLoader.getNumObjectProperties(ontoFile1);
		
		return  (double)numObjectProperties / ((double)numSubClasses + (double)numObjectProperties);
	}

	/**
	 * WordNet Coverage (WC): The percentage of terms with label or local name present
	 * in WordNet.
	 * 
	 * @param onto1
	 * @param onto2
	 * @throws OWLOntologyCreationException 
	 */
	public static double computeWordNetCoverage(File ontoFile1, File ontoFile2) throws OWLOntologyCreationException{
		
		double WC = (OWLLoader.getWordNetCoverage(ontoFile1) + OWLLoader.getWordNetCoverage(ontoFile2)) / 2;
		
		return WC;
	}
	
	public static double computeWordNetCoverage(File ontoFile1) throws OWLOntologyCreationException{
		
		double WC = OWLLoader.getWordNetCoverage(ontoFile1);
		
		return WC;
	}
	
	//Number of concepts containing abbrevations in their labels
	public static double computeAbbreviationAnalysis(File ontoFile) {
		double abbreviationMetric = 0;
		
		
		
		return abbreviationMetric;
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
		// TO-DO: Implement functionality for finding enrichments
		return null;
	}

	
	public static void main(String[] args) throws OWLOntologyCreationException, URISyntaxException, OntowrapException {
		
		File onto1 = new File("/Users/audunvennesland/Documents/PhD/Ontologies/OAEI/Conference-2016/conference/Conference.owl");
		File onto2 = new File("/Users/audunvennesland/Documents/PhD/Ontologies/OAEI/Conference-2016/conference/ekaw.owl");
		

		System.out.println("The Inheritance Richness (IR) of " + onto1.getName() + " is: " + computeInheritanceRichness(onto1));
		System.out.println("The Inheritance Richness (IR) of " + onto2.getName() + " is: " + computeInheritanceRichness(onto2));
		System.out.println("The Inheritance Richness (IR) of " + onto1.getName() + " and " + onto2.getName() + " is: " + computeInheritanceRichness(onto1, onto2));
		
		System.out.println("The NullLabelOrComment (N) of " + onto1.getName() + " and " + onto2.getName() + " is: " + computeNullLabelOrComment(onto1, onto2) + " (" + computeNullLabelOrComment(onto1, onto2)*100 + " percent)");
		System.out.println("The NullLabelOrComment (N) of " + onto1.getName() + " is: " + computeNullLabelOrComment(onto1));
		System.out.println("The NullLabelOrComment (N) of " + onto2.getName() + " is: " + computeNullLabelOrComment(onto2));
		
		System.out.println("The Relationship Richness (RR) of " + onto1.getName() + " and " + onto2.getName() + " is: " + computeRelationshipRichness(onto1, onto2));
		System.out.println("The Relationship Richness (RR) of " + onto1.getName() + " is: " + computeRelationshipRichness(onto1));
		System.out.println("The Relationship Richness (RR) of " + onto2.getName() + " is: " + computeRelationshipRichness(onto2));
		
		System.out.println("The WordNet Coverage (WC) of " + onto1.getName() + " is: " + computeWordNetCoverage(onto1));
		System.out.println("The WordNet Coverage (WC) of " + onto2.getName() + " is: " + computeWordNetCoverage(onto2));
		System.out.println("The WordNet Coverage (WC) of " + onto1.getName() + " and " + onto2.getName() + " is: " + computeWordNetCoverage(onto1, onto2) + " (" + computeWordNetCoverage(onto1, onto2)*100 + " percent)");
		
		System.out.println("The Num Compounds (NC) of " + onto1.getName() + " is: " + OWLLoader.getNumClassCompounds(onto1));
		System.out.println("The Num Compounds (NC) of " + onto2.getName() + " is: " + OWLLoader.getNumClassCompounds(onto2));
		System.out.println("The Num Compounds (NC) of " + onto1.getName() + " and " + onto2.getName() + " is: " + ((OWLLoader.getNumClassCompounds(onto1) + OWLLoader.getNumClassCompounds(onto2))) / 2);
		
		
	}

}
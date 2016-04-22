package no.ntnu.idi.compose.Processing;

import fr.inrialpes.exmo.ontowrap.LoadedOntology;

/**
 * @author audunvennesland
 * @version 1.0
 * @created 21-apr-2016 10:34:52
 */
public interface Processor {

	/**
	 * 
	 * @param ontologyFile
	 */
	public int getNumClasses(LoadedOntology ontologyFile);

	/**
	 * 
	 * @param ontologyFile
	 */
	public int getNumObjectProperties(LoadedOntology ontologyFile);

	/**
	 * 
	 * @param ontologyFile
	 */
	public int getNumIndividuals(LoadedOntology ontologyFile);

	/**
	 * 
	 * @param inputOntology
	 */
	public double computeSparsityProfile(LoadedOntology inputOntology);

	/**
	 * Relationship Richness (RR): The percentage of object properties (P) that are
	 * different from subClassOf relations (SC) -> RR = |P| / |SC| + |P|
	 * 
	 * @param onto1
	 * @param onto2
	 */
	public double computeRelationshipRichness(LoadedOntology onto1, LoadedOntology onto2);

	/**
	 * Inheritance Richness is the average number of subclasses (Ci) per class (C). IR
	 * = HC (C, Ci) / C
	 * 
	 * @param onto1
	 * @param onto2
	 */
	public double computeInheritanceRichness(LoadedOntology onto1, LoadedOntology onto2);

	/**
	 * Class Richness (CR): The ratio of the number of classes for which instances
	 * (Ci) exist
	 * 
	 * @param onto1
	 * @param onto2
	 */
	public double computeClassRichness(LoadedOntology onto1, LoadedOntology onto2);

	/**
	 * Average Population (AP): Indicates the average distribution of instances across
	 * all classes. AP = |I| / |C|.
	 * 
	 * @param onto1
	 * @param onto2
	 */
	public double computeAveragePopulation(LoadedOntology onto1, LoadedOntology onto2);

	/**
	 * Average Depth (AD): The average depth (D) of the classes in an ontology as a
	 * mean of the depth of over all classes. AD = D(Ci) / |C|
	 * 
	 * @param onto1
	 * @param onto2
	 */
	public double computeAverageDepth(LoadedOntology onto1, LoadedOntology onto2);

	/**
	 * WordNet Coverage (WC): The percentage of terms with label or local name present
	 * in WordNet.
	 * 
	 * @param onto1
	 * @param onto2
	 */
	public double computeWordNetCoverage(LoadedOntology onto1, LoadedOntology onto2);

	/**
	 * Null label and comment (N): The percentage of terms with no comment nor label
	 * (NCi). N = |NCi| / |T|.
	 * 
	 * @param onto1
	 * @param onto2
	 */
	public double computeNullLabelOrComment(LoadedOntology onto1, LoadedOntology onto2);

}
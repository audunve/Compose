package no.ntnu.idi.compose.Processing;




import java.io.File;
import java.util.Map;

import org.semanticweb.owl.util.OWLManager;

import fr.inrialpes.exmo.ontowrap.LoadedOntology;
import no.ntnu.idi.compose.Enrichment.EnrichmentCollector;
import no.ntnu.idi.compose.Loading.Loader;

/**
 * @author audunvennesland
 * @version 1.0
 * @created 21-apr-2016 10:34:52
 */
public class OntologyProcessor implements EnrichmentCollector, Processor, Loader {

	private OWLManager manager;
	private File file;
	private LoadedOntology onto;

	public OntologyProcessor(){

	}

	public void finalize() throws Throwable {

	}

	/**
	 * 
	 * @param ontologyFile
	 */
	public String getOntologyFormat(LoadedOntology ontologyFile){
		return "";
	}

	/**
	 * 
	 * @param ontologyFile
	 */
	public int getNumClasses(LoadedOntology ontologyFile){
		return 0;
	}

	/**
	 * 
	 * @param ontologyFile
	 */
	public int getNumIndividuals(LoadedOntology ontologyFile){
		return 0;
	}

	/**
	 * 
	 * @param ontologyFile
	 */
	public int getNumObjectProperties(LoadedOntology ontologyFile){
		return 0;
	}

	/**
	 * 
	 * @param inputOntology
	 */
	public double computeSparsityProfile(LoadedOntology inputOntology){
		return 0;
	}

	/**
	 * Average Depth (AD): The average depth (D) of the classes in an ontology as a
	 * mean of the depth of over all classes. AD = D(Ci) / |C|
	 * 
	 * @param onto1
	 * @param onto2
	 */
	public double computeAverageDepth(LoadedOntology onto1, LoadedOntology onto2){
		return 0;
	}

	/**
	 * Average Population (AP): Indicates the average distribution of instances across
	 * all classes. AP = |I| / |C|.
	 * 
	 * @param onto1
	 * @param onto2
	 */
	public double computeAveragePopulation(LoadedOntology onto1, LoadedOntology onto2){
		return 0;
	}

	/**
	 * Class Richness (CR): The ratio of the number of classes for which instances
	 * (Ci) exist
	 * 
	 * @param onto1
	 * @param onto2
	 */
	public double computeClassRichness(LoadedOntology onto1, LoadedOntology onto2){
		return 0;
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
	public double computeRelationshipRichness(LoadedOntology onto1, LoadedOntology onto2){
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
		return null;
	}

	public void loadOntologyFromURI(){

	}

	/**
	 * 
	 * @param ontologyFile
	 */
	public void loadOntologyFromFile(File ontologyFile){

	}

	public String getOntologyFormat(File ontologyFile) {
		// FIXME Auto-generated method stub
		return null;
	}

	public Map FindEnrichment(Map keywords, String URI, double confidence) {
		// FIXME Auto-generated method stub
		return null;
	}

}
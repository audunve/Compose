package compose.matchers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import compose.graph.GraphOperations;
import compose.misc.ISub;
import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.ontowrap.OntowrapException;

/**
 * This matcher represents ontologies as graphs (directed acyclic graphs) in the Neo4J graph database and performs structural matching using parent-node and child-node
 * similarity.
 * @author audunvennesland
 * 2. feb. 2017
 */
@SuppressWarnings("deprecation")
public class NeighborhoodMatcher extends ObjectAlignment implements AlignmentProcess {

	final static double THRESHOLD = 0.6;

	/**
	 * This label represents the graph/ontology to process
	 */
	static Label labelOnto1;
	/**
	 * This label represents the graph/ontology to process
	 */
	static Label labelOnto2;

	static GraphDatabaseService db;

	ISub iSubMatcher = new ISub();

	final static String key = "classname";
	
	SuperclassMatcher superclassMatcher = new SuperclassMatcher();
	SubclassMatcher subclassMatcher = new SubclassMatcher();



	/**
	 * Constructor that receives the labels (ontology file names) and database name to use from interacting matcher ui (currently TestMatcher.java)
	 * @param ontology1Name the name of the first ontology to match
	 * @param ontology2Name the name of the second ontology to match
	 * @param database the database to query
	 */
	public NeighborhoodMatcher(String ontology1Name, String ontology2Name,GraphDatabaseService database) {
		labelOnto1 = DynamicLabel.label(ontology1Name);
		labelOnto2 = DynamicLabel.label(ontology2Name);
		db = database;
	}


	/**
	 * The align() method is imported from the Alignment API and is modified to use the methods declared in this class
	 */
	public void align( Alignment alignment, Properties param ) throws AlignmentException {
		try {

			// Match classes
			for ( Object cl2: ontology2().getClasses() ){
				for ( Object cl1: ontology1().getClasses() ){

					// add mapping into alignment object 
					addAlignCell(cl1,cl2, "=", superclassMatcher.matchSuperClasses(cl1,cl2));  
				}

			}

		} catch (Exception e) { e.printStackTrace(); }
	}


	/**
	 * A method that combines superclass- and subclass similarity to find the similarity score between the two input ontology objectss
	 * @param o1 an input ontology object (i.e. an OWL entity)
	 * @param o2 an input ontology object (i.e. an OWL entity)
	 * @return a similarity measure from matching the two input ontology objects
	 * @throws OWLOntologyCreationException
	 * @throws OntowrapException
	 * @throws IOException
	 */
	public double matchNeighborhood(Object o1, Object o2) throws OWLOntologyCreationException, OntowrapException, IOException {

		//System.out.println("Matching " + o1.toString() + " and " + o2.toString());

		double structProx = superclassMatcher.matchSuperClasses(o1, o2);

		//System.out.println("The structProx of " + o1.toString() + " and " + o2.toString() + " is " + structProx);

		double subClassMatch = subclassMatcher.matchSubClasses(o1, o2);

		//System.out.println("The subclass match of " + o1.toString() + " and " + o2.toString() + " is " + subClassMatch);

		double neighborhoodMatch = (structProx + subClassMatch) / 2;

		//System.out.println("The neighborhood match of " + o1.toString() + " and " + o2.toString() + " is " + neighborhoodMatch + "\n");


		if ((structProx + subClassMatch) / 2 > THRESHOLD) {
			return neighborhoodMatch;
		} else {
			return 0;
		}
	}
	

	
	

}

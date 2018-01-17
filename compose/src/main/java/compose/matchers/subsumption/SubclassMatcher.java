package compose.matchers;

import java.io.IOException;
import java.util.ArrayList;
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
public class SubclassMatcher extends ObjectAlignment implements AlignmentProcess {

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



	/**
	 * Constructor that receives the labels (ontology file names) and database name to use from interacting matcher ui (currently TestMatcher.java)
	 * @param ontology1Name the name of the first ontology to match
	 * @param ontology2Name the name of the second ontology to match
	 * @param database the database to query
	 */
	public SubclassMatcher(String ontology1Name, String ontology2Name,GraphDatabaseService database) {
		labelOnto1 = DynamicLabel.label(ontology1Name);
		labelOnto2 = DynamicLabel.label(ontology2Name);
		db = database;
	}
	
	/**
	 * 
	 */
	public SubclassMatcher() {
		// FIXME Auto-generated constructor stub
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
					addAlignCell(cl1,cl2, "=", matchSubClasses(cl1,cl2));  
				}

			}

		} catch (Exception e) { e.printStackTrace(); }
	}

	

	

	/**
	 * This method computes a similarity between two input objects (i.e. OWL entities) based on how similar their subclasses are.
	 * The similarity between subclasses is based on the ISub algorithm (Stolios, et al., 2005)
	 * @param o1 an input ontology object (i.e. an OWL entity)
	 * @param o2 an input ontology object (i.e. an OWL entity)
	 * @return a similarity measure based on the similarity of subclasses of the two input ontology objects
	 * @throws OWLOntologyCreationException
	 * @throws OntowrapException
	 * @throws IOException
	 */
	public double matchSubClasses(Object o1, Object o2) throws OWLOntologyCreationException, OntowrapException, IOException {

		String s1 = ontology1().getEntityName(o1);
		String s2 = ontology2().getEntityName(o2);
		
		//test
		System.out.println("The concepts to be matcher are: " + s1 + " and " + s2);

		//get the s1 node from ontology 1
		Node s1Node = GraphOperations.getNode(s1, labelOnto1);
		System.out.println("S1 Node retrieved: " + s1Node.toString());

		//get the s2 node from ontology 2
		Node s2Node = GraphOperations.getNode(s2, labelOnto2);
		System.out.println("S2 Node retrieved: " + s2Node.toString());
		
		//get the parent nodes of a class from ontology 1
		ArrayList onto1SubClasses = GraphOperations.getClosestChildNodesAsList(s1Node, labelOnto1);
		for (int i = 0; i < onto1SubClasses.size(); i++) {
		}

		//get the parent nodes of a class from ontology 2
		ArrayList onto2SubClasses = GraphOperations.getClosestChildNodesAsList(s2Node,labelOnto2);
		for (int i = 0; i < onto2SubClasses.size(); i++) {
		}

		double iSubSimScore = 0;
		ISub iSubMatcher = new ISub();
		double distance = 0;
		//match the subclasses of the two nodes
		//matching the parentnodes
		for (int i = 0; i < onto1SubClasses.size(); i++) {
			for (int j = 0; j < onto2SubClasses.size(); j++) {
				iSubSimScore = iSubMatcher.score(onto1SubClasses.get(i).toString(), onto2SubClasses.get(j).toString());
				//if any of the subclasses match above the threshold, use the iSub score as the similarity between the two input classes
				if (iSubSimScore >= THRESHOLD) {
					distance = iSubSimScore;

				}	
			}
		}

		return distance;

	}
	

	
	

}

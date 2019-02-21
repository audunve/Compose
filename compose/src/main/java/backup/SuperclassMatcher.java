package backup;

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

import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.ontowrap.OntowrapException;
import graph.Graph;
import graph.Graph;
import utilities.ISub;

/**
 * This matcher represents ontologies as graphs (directed acyclic graphs) in the Neo4J graph database and performs structural matching using parent-node and child-node
 * similarity.
 * @author audunvennesland
 * 2. feb. 2017
 */
@SuppressWarnings("deprecation")
public class SuperclassMatcher extends ObjectAlignment implements AlignmentProcess {

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
	public SuperclassMatcher(String ontology1Name, String ontology2Name,GraphDatabaseService database) {
		labelOnto1 = DynamicLabel.label(ontology1Name);
		labelOnto2 = DynamicLabel.label(ontology2Name);
		db = database;
	}


	/**
	 * 
	 */
	public SuperclassMatcher() {
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
					addAlignCell(cl1,cl2, "=", matchSuperClasses(cl1,cl2));  
				}

			}

		} catch (Exception e) { e.printStackTrace(); }
	}

	

	

	/**
	 * This method computes the structural proximity of two input classes. 
	 * (1) First it finds the input classes in the corresponding graphs, and measures their distance to root (owl:Thing), 
	 * (2) then it retrieves the list of parent nodes to these two input classes,
	 * (3) then it matches the parent nodes of the corresponding input classes,
	 * (4) if the similarity of parent nodes is above the threshold, the distance to root for these parent nodes is counted,
	 * (5) finally, the structural proximity is computed as:
	 * (2 * avgAncestorDistanceToRoot) / (distanceC1ToRoot + distanceC2ToRoot)
	 * @param o1 an ontology object (OWL entity)
	 * @param o2 an ontology object (OWL entity)
	 * @return measure of similarity between the two input objects (ontology entities)
	 * @throws OWLOntologyCreationException
	 * @throws OntowrapException
	 * @throws IOException
	 */
	public double matchSuperClasses(Object o1, Object o2) throws OWLOntologyCreationException, OntowrapException, IOException {

		String s1 = ontology1().getEntityName(o1);
		String s2 = ontology2().getEntityName(o2);

		//get the s1 node from ontology 1
		Node s1Node = Graph.getNode(s1, labelOnto1);

		//get the s2 node from ontology 2
		Node s2Node = Graph.getNode(s2, labelOnto2);

		//get the parent nodes of a class from ontology 1
		ArrayList<Object> onto1Parents = Graph.getAllParentNodes(s1Node, labelOnto1);
	
		//get the parent nodes of a class from ontology 2
		ArrayList<Object> onto2Parents = Graph.getAllParentNodes(s2Node,labelOnto2);

		//find distance from s1 node to owl:Thing
		int distanceC1ToRoot = Graph.findDistanceToRoot(s1Node);

		//find distance from s2 to owl:Thing
		int distanceC2ToRoot = Graph.findDistanceToRoot(s2Node);

		double iSubSimScore = 0;
		ISub iSubMatcher = new ISub();

		//map to keep the pair of ancestors matching above the threshold
		Map<Object,Object> matchingMap = new HashMap<Object,Object>();

		//matching the parent nodes
		for (int i = 0; i < onto1Parents.size(); i++) {
			for (int j = 0; j < onto2Parents.size(); j++) {
				iSubSimScore = iSubMatcher.score(onto1Parents.get(i).toString(), onto2Parents.get(j).toString());

				if (iSubSimScore >= THRESHOLD) {

					matchingMap.put(onto1Parents.get(i) , onto2Parents.get(j));
				}	
			}
		}

		double structProx = 0;
		double currentStructProx = 0;
		double avgAncestorDistanceToRoot = 0;


		//loop through the matchingMap containing key-value pairs of ancestors from O1 and O2 being similar over the given threshold
		for (Entry<Object, Object> entry : matchingMap.entrySet()) {
			Node anc1 = Graph.getNode(entry.getKey().toString(), labelOnto1);
			Node anc2 = Graph.getNode(entry.getValue().toString(), labelOnto2);

			avgAncestorDistanceToRoot = (Graph.findDistanceToRoot(anc1) + Graph.findDistanceToRoot(anc2)) / 2;

			currentStructProx = (2 * avgAncestorDistanceToRoot) / (distanceC1ToRoot + distanceC2ToRoot);

			if (currentStructProx > structProx) {

				structProx = currentStructProx;
			}

		}

		return structProx;
	}
	

	
	

}

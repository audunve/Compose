package subsumptionmatching;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import backup.GraphOperations_delete;
import backup.ParentMatcher.RelTypes;
import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.ontowrap.OntowrapException;
import utilities.ISub;

/**
 * This matcher represents ontologies as graphs (directed acyclic graphs) in the Neo4J graph database and performs structural matching using parent-node and child-node
 * similarity.
 * @author audunvennesland
 * 2. feb. 2017
 */
@SuppressWarnings("deprecation")
public class OppositeSubclassMatcher extends ObjectAlignment implements AlignmentProcess {

	//final static double THRESHOLD = 0.6;
	final String isA = "&lt;";
	final String hasA = "&gt;";
	double weight;

	/**
	 * This label represents the graph/ontology to process
	 */
	static Label labelOnto1;
	/**
	 * This label represents the graph/ontology to process
	 */
	static Label labelOnto2;

	static GraphDatabaseService db;


	String key = "classname";



	/**
	 * Constructor that receives the labels (ontology file names) and database name to use from interacting matcher ui (currently TestMatcher.java)
	 * @param ontology1Name the name of the first ontology to match
	 * @param ontology2Name the name of the second ontology to match
	 * @param database the database to query
	 */
	public OppositeSubclassMatcher(String ontology1Name, String ontology2Name,GraphDatabaseService database, double weight) {
		labelOnto1 = DynamicLabel.label(ontology1Name);
		labelOnto2 = DynamicLabel.label(ontology2Name);
		db = database;
		this.weight = weight;
	}


	/**
	 * The align() method is imported from the Alignment API and is modified to use the methods declared in this class
	 */
	public void align( Alignment alignment, Properties param ) throws AlignmentException {
		
		System.out.println("\nStarting Opposite Subclass Matcher...");
		long startTime = System.currentTimeMillis();
		
		try {

			// Match classes
			for ( Object cl2: ontology2().getClasses() ){
				for ( Object cl1: ontology1().getClasses() ){

					//get map from matchSubClasses2Class where the relation is the key and the value is the score
					Map<String, Double> matchingMap = matchCommonSubclasses(cl1, cl2);

					// add mapping into alignment object for each entry in the matching map
					for (Map.Entry<String, Double> entry : matchingMap.entrySet()) {
						addAlignCell(cl1,cl2, entry.getKey(), weight*entry.getValue());  
					}

				}

			}

		} catch (Exception e) { e.printStackTrace(); }
		
		long endTime = System.currentTimeMillis();
		System.out.println("Opposite Subclass Matcher completed in " + (endTime - startTime) / 1000 + " seconds.");
	}





	/**
	 * This method computes a subsumption relation if two concepts are terminologically equal. Then both are subsumed by the parent of the other concept. 
	 * @param o1 an input ontology object (i.e. an OWL entity)
	 * @param o2 an input ontology object (i.e. an OWL entity)
	 * @return a similarity measure based on the similarity of subclasses of the two input ontology objects
	 * @throws OWLOntologyCreationException
	 * @throws OntowrapException
	 * @throws IOException
	 */
	private Map<String, Double> matchCommonSubclasses(Object o1, Object o2) throws OWLOntologyCreationException, OntowrapException, IOException {

		String s1 = ontology1().getEntityName(o1);
		String s2 = ontology2().getEntityName(o2);

		//test
		//System.out.println("The concepts to be matched are: " + s1 + " and " + s2);

		//System.out.println("Labels are " + labelOnto1.toString() + " and " + labelOnto2.toString());

		//get the s1 node from ontology 1
		Node s1Node = getNode(s1, labelOnto1);
		//System.out.println("S1 Node retrieved: " + s1Node.toString());

		//get the s2 node from ontology 2
		Node s2Node = getNode(s2, labelOnto2);
		//System.out.println("S2 Node retrieved: " + s2Node.toString());

		//get the subclasses of s1
		ArrayList onto1SubClasses = getClosestChildNodesAsList(s1Node, labelOnto1);

		//get the subclasses of s2
		ArrayList onto2SubClasses = getClosestChildNodesAsList(s2Node, labelOnto2);

		double distance = 0;

		//map to keep the relation and matching score
		Map<String,Double> matchingMap = new HashMap<String,Double>();


		//iterate through all subclasses and s2 and compare with s1
		if (onto2SubClasses.size() > 0) {
			for (int i = 0; i < onto2SubClasses.size(); i++) {
				if (s1.toLowerCase().equals(onto2SubClasses.get(i).toString().toLowerCase())) {
					distance = 1;
					matchingMap.put(isA, distance);

				} else {
					distance = 0;
					matchingMap.put(isA, distance);
				}
			}
			//just to create a relation for all concept combinations when calculating the Harmony value
		} else {
			matchingMap.put(isA, 0.0);
		}

		//iterate through all subclasses of s1 and compare with s2
		if (onto1SubClasses.size() > 0) {
			for (int i = 0; i < onto1SubClasses.size(); i++) {
				if(s2.toLowerCase().equals(onto1SubClasses.get(i).toString().toLowerCase())) {
					distance = 1;
					matchingMap.put(hasA, distance);
				} else {
					distance = 0;
					matchingMap.put(hasA, distance);
				}
			}
			//just to create a relation for all concept combinations when calculating the Harmony value
		} else {
			matchingMap.put(hasA, 0.0);
		}


		return matchingMap;
	}




	private static void registerShutdownHook(final GraphDatabaseService db)
	{
		Runtime.getRuntime().addShutdownHook( new Thread()
		{
			@Override
			public void run()
			{
				db.shutdown();
				System.out.println("Shutdownhook executed!");
			}
		} );
	}

	public static enum RelTypes implements RelationshipType
	{
		isA
	}

	/**
	 * Returns the graph node given a label, a property name and property value
	 * @param propertyValue
	 * @return
	 */

	public Node getNode(String value, Label label) {
		Node testNode = null;

		try ( Transaction tx = db.beginTx() ) {
			testNode = db.findNode(label, key, value);
			tx.success();

		}


		return testNode;	

	}

	public String getNodeName(Node n) {

		String value = null;

		try ( Transaction tx = db.beginTx() ) {
			value = n.getProperty(key).toString();
			tx.success();

		}

		return value;	
	}

	public long getNodeID(Node n) {

		long id = 0;

		try ( Transaction tx = db.beginTx() ) {
			id = n.getId();
			tx.success();	

		}


		return id;	
	}

	public Traverser getChildNodesTraverser(Node classNode) {

		TraversalDescription td = null;
		try ( Transaction tx = db.beginTx() ) {

			td = db.traversalDescription().breadthFirst().relationships(RelTypes.isA, Direction.INCOMING).evaluator(Evaluators.excludeStartPosition());

			tx.success();

		}


		return td.traverse(classNode);
	}

	public ArrayList<Object> getClosestChildNodesAsList(Node classNode, Label label) {

		ArrayList<Object> childNodeList= new ArrayList<Object>();
		Traverser childNodesTraverser = null;

		try ( Transaction tx = db.beginTx() ) {

			childNodesTraverser = getChildNodesTraverser(classNode);

			for (Path childNodePath: childNodesTraverser) {
				if(childNodePath.length() == 1 && childNodePath.endNode().hasLabel(label)) {
					childNodeList.add(childNodePath.endNode().getProperty("classname"));
				}
			}

			tx.success();

		}

		return childNodeList;
	}


	public Traverser getParentNodeTraverser (Node classNode) {

		TraversalDescription td = null;

		try ( Transaction tx = db.beginTx() ) {

			td = db.traversalDescription()
					.breadthFirst()
					.relationships(RelTypes.isA, Direction.OUTGOING)
					.evaluator(Evaluators.excludeStartPosition());

			tx.success();

		}

		return td.traverse(classNode);
	}

	public ArrayList<Object> getClosestParentNode(Node classNode, Label label) {

		ArrayList<Object> parentNodeList= new ArrayList<Object>();
		Traverser parentNodeTraverser = null;

		try ( Transaction tx = db.beginTx() ) {

			parentNodeTraverser = getParentNodeTraverser(classNode);

			for (Path parentNodePath: parentNodeTraverser) {
				if(parentNodePath.length() == 1 && parentNodePath.endNode().hasLabel(label)) {
					parentNodeList.add(parentNodePath.endNode().getProperty("classname"));
				}
			}

			tx.success();

		}

		return parentNodeList;
	}

	public ArrayList<Object> getAllParentNodes(Node classNode, Label label) {

		ArrayList<Object> parentNodeList= new ArrayList<Object>();
		Traverser parentNodeTraverser = null;

		try ( Transaction tx = db.beginTx() ) {

			parentNodeTraverser = getParentNodeTraverser(classNode);

			for (Path parentNodePath: parentNodeTraverser) {
				if (parentNodePath.endNode().hasLabel(label)){
					parentNodeList.add(parentNodePath.endNode().getProperty("classname"));

				}

			}

			tx.success();

		}


		return parentNodeList;
	}

	/**
	 * We use a Map as a work-around to counting the edges between a given node and the root (owl:Thing). This is possible since a Map only allows
	 * unique keys and a numbered Neo4J path consists of a set of path items <edge-count, node (property)> where all nodes for each edge-count
	 * is listed (e.g. for the node "AcademicArticle" the upwards path is <1, Article>, <2, Document>, <3, owl:Thing>). 
	 * @param db
	 * @param classNode
	 * @return
	 */
	public int findDistanceToRoot(Node classNode) {

		Traverser parentNodeTraverser = null;
		Map<Object, Object> parentNodeMap = new HashMap<>();

		try ( Transaction tx = db.beginTx() ) {

			parentNodeTraverser = getParentNodeTraverser(classNode);

			for (Path parentNodePath : parentNodeTraverser) {
				parentNodeMap.put(parentNodePath.length(), parentNodePath.endNode().getProperty("classname"));

			}

			tx.success();

		}


		int distanceToRoot = parentNodeMap.size();

		return distanceToRoot;
	}


}

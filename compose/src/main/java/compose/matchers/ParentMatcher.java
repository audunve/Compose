package compose.matchers;

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

import compose.graph.GraphOperations;
import compose.misc.ISub;
import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.ontowrap.OntowrapException;

@SuppressWarnings("deprecation")
public class ParentMatcher extends ObjectAlignment implements AlignmentProcess {

	final double THRESHOLD = 0.8;
	final String isA = "&lt;";
	final String hasA = "&gt;";

	Label labelOnto1;
	Label labelOnto2;

	GraphDatabaseService db;

	ISub iSubMatcher = new ISub();

	String key = "classname";


	//constructor that receives the labels (ontology file names) and the database from TestMatcher.java
	public ParentMatcher(String ontology1Name, String ontology2Name, GraphDatabaseService database) {
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

					//get map from matchSubClasses2Class where the relation is the key and the value is the score
					Map<String, Double> matchingMap = matchSubClasses2Class(cl1, cl2);

					// add mapping into alignment object for each entry in the matching map
					for (Map.Entry<String, Double> entry : matchingMap.entrySet()) {
						addAlignCell(cl1,cl2, entry.getKey(), entry.getValue());  
					}


				}

			}

		} catch (Exception e) { e.printStackTrace(); }
	}


	/**
	 * Matches the parents of o1 with o2, if there is a similarity above a certain threshold (determined by ISub), then
	 * o1 is subsumed by o2, and vice versa. 
	 * @param o1
	 * @param o2
	 * @return
	 * @throws OWLOntologyCreationException
	 * @throws OntowrapException
	 * @throws IOException
	 */
	private Map<String,Double> matchSubClasses2Class(Object o1, Object o2) throws OWLOntologyCreationException, OntowrapException, IOException {

		String s1 = ontology1().getEntityName(o1);
		System.out.println("s1 is " + s1);
		String s2 = ontology2().getEntityName(o2);
		System.out.println("s2 is " + s2);
		
		System.out.println("Labels are " + labelOnto1.toString() + " and " + labelOnto2.toString());
		
		//Whenever I use the GraphOperations class for using Neo4J methods I get a nullpointerexception in TestMatcher...
		//GraphOperations op = new GraphOperations();

		//get the s1 node from ontology 1
		//Node s1Node = GraphOperations.getNode(s1, labelOnto1);
		Node s1Node = getNode(s1, labelOnto1);
		System.out.println("s1Node is " + s1Node.getId());

		//get the s2 node from ontology 2
		//Node s2Node = GraphOperations.getNode(s2, labelOnto2);
		Node s2Node = getNode(s2, labelOnto2);
		System.out.println("s2Node is " + s1Node.getId());

		//get the parent nodes of a class from ontology 1
		//ArrayList onto1Parents = GraphOperations.getClosestParentNode(s1Node, labelOnto1);
		ArrayList onto1Parents = getClosestParentNode(s1Node, labelOnto1);
		for (int i = 0; i < onto1Parents.size(); i++) {
		}

		//get the parent nodes of a class from ontology 2
		//ArrayList onto2Parents = GraphOperations.getClosestParentNode(s2Node,labelOnto2);
		ArrayList onto2Parents = getClosestParentNode(s2Node,labelOnto2);
		for (int i = 0; i < onto2Parents.size(); i++) {
		}

		//double score = 0;
		double iSubSimScore = 0;
		ISub iSubMatcher = new ISub();

		//map to keep the relation and matching score
		Map<String,Double> matchingMap = new HashMap<String,Double>();

		System.out.println("\n");
		System.out.println("------- Matching task: " + s1 + " and " + s2 + " -------");

		//match o1 with the parent nodes of o2
		for (int i = 0; i < onto2Parents.size(); i++) {
			iSubSimScore = iSubMatcher.score(s1, onto2Parents.get(i).toString());
			System.out.println("Matching " + s1 + " with " + s2 + "´s parent " + onto2Parents.get(i) + " with a score of " + iSubSimScore);
			if (iSubSimScore >= THRESHOLD) {
				matchingMap.put(isA, iSubSimScore);
				System.out.println("Conclusion: " + s1 + " " + hasA + " " + s2);
			}
		}
		//match parent nodes of o1 with o2
		for (int i = 0; i < onto1Parents.size(); i++) {
			iSubSimScore = iSubMatcher.score(s2, onto1Parents.get(i).toString());
			System.out.println("Matching " + s2 + " with " + s1 + "´s parent " +  onto1Parents.get(i) + " with a score of " + iSubSimScore);
			if (iSubSimScore >= THRESHOLD) {
				matchingMap.put(isA, iSubSimScore);
				System.out.println("Conclusion: " + s1 + " " + isA + " " + s2);
			}
		}

		System.out.println("------- End Matching task: " + s1 + " and " + s2 + " -------");
		System.out.println("\n");
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

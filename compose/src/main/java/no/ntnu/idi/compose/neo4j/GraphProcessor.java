package no.ntnu.idi.compose.neo4j;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;

import no.ntnu.idi.compose.algorithms.ISub;

public class GraphProcessor {

	ISub iSubMatcher = new ISub();

	//should initialize with a graph database
	public GraphProcessor(){}


	/**
	 * Registers a shutdown hook for the Neo4j instance so that it shuts down nicely when the VM exits
	 * @param graphDb
	 */
	private static void registerShutdownHook(final GraphDatabaseService graphDb)
	{
		Runtime.getRuntime().addShutdownHook( new Thread()
		{
			@Override
			public void run()
			{
				graphDb.shutdown();
			}
		} );
	}

	private static enum RelTypes implements RelationshipType
	{
		isA
	}

	/**
	 * Returns the graph node given a label, a property name and property value
	 * @param propertyValue
	 * @return
	 */

	public Node getNode(GraphDatabaseService db, Label label, String key, String value) {
		Node testNode = null;
		try ( Transaction tx = db.beginTx() ) {
		testNode = db.findNode(label, key, value);
		tx.success();
		}
		return testNode;	
	}
/*	
	public String getNodeName(Node n) {

		String nodeName = null;
		
		try ( Transaction tx = db.beginTx() ) {
		nodeName = db.findNode(label, key, value);
		tx.success();
		}
		return testNode;	
	}*/

	public Traverser getChildNodesTraverser(GraphDatabaseService db, Node classNode) {

		TraversalDescription td = null;
		try ( Transaction tx = db.beginTx() ) {

		td = db.traversalDescription().breadthFirst().relationships(RelTypes.isA, Direction.INCOMING).evaluator(Evaluators.excludeStartPosition());

		tx.success();
		}
		
		return td.traverse(classNode);
	}
	
	public ArrayList getClosestChildNodesAsList(GraphDatabaseService db, Node classNode) {
		
		ArrayList<Object> childNodeList= new ArrayList<Object>();
		Traverser childNodesTraverser = null;
		
		try ( Transaction tx = db.beginTx() ) {

		childNodesTraverser = getChildNodesTraverser(db, classNode);

		for (Path childNodePath: childNodesTraverser) {
			if(childNodePath.length() == 1) {
				childNodeList.add(childNodePath.endNode().getProperty("classname"));
			}
		}

		tx.success();
		}
		
		return childNodeList;
	}


	public Traverser getParentNodeTraverser (GraphDatabaseService db, Node classNode) {
		
		TraversalDescription td = null;

		try ( Transaction tx = db.beginTx() ) {
		td = db.traversalDescription().breadthFirst().relationships(RelTypes.isA, Direction.OUTGOING).evaluator(Evaluators.excludeStartPosition());

			tx.success();
		}
		return td.traverse(classNode);
	}
	
public ArrayList<Object> getClosestParentNode(GraphDatabaseService db, Node classNode) {
		
	ArrayList<Object> parentNodeList= new ArrayList<Object>();
	Traverser parentNodeTraverser = null;
	
	try ( Transaction tx = db.beginTx() ) {

		parentNodeTraverser = getParentNodeTraverser(db, classNode);

	for (Path parentNodePath: parentNodeTraverser) {
		if(parentNodePath.length() == 1) {
			parentNodeList.add(parentNodePath.endNode().getProperty("classname"));
		}
	}

	tx.success();
	}
	
	return parentNodeList;
}

public ArrayList<Object> getAllParentNodes(GraphDatabaseService db, Node classNode) {
	
	ArrayList<Object> parentNodeList= new ArrayList<Object>();
	Traverser parentNodeTraverser = null;
	
	try ( Transaction tx = db.beginTx() ) {

		parentNodeTraverser = getParentNodeTraverser(db, classNode);

	for (Path parentNodePath: parentNodeTraverser) {

			parentNodeList.add(parentNodePath.endNode().getProperty("classname"));

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
	public int findDistanceToRoot(GraphDatabaseService db, Node classNode) {

		Traverser parentNodeTraverser = null;
		Map<Object, Object> parentNodeMap = new HashMap<>();

		try ( Transaction tx = db.beginTx() ) {
			
			parentNodeTraverser = getParentNodeTraverser(db, classNode);
			
			for (Path parentNodePath : parentNodeTraverser) {
				parentNodeMap.put(parentNodePath.length(), parentNodePath.endNode().getProperty("classname"));
				
			}
			
			tx.success();
		}

		int distanceToRoot = parentNodeMap.size();

		return distanceToRoot;
	}

	/**
	 * StructProx = ((2 * avgLCSDistanceToRoot) / distanceC1ToRoot + distanceC2ToRoot)
	 * @return
	 */
	public double computeStructProx(GraphDatabaseService db, Label label, Node classNode1, Node classNode2) {
		
		int class1DistanceToRoot = findDistanceToRoot(db, classNode1);
		int class2DistanceToRoot = findDistanceToRoot(db, classNode2);
		
		//find the list of ancestors of node 1
		ArrayList<Object> parentNodes1 = getAllParentNodes(db, classNode1);
		//find the list of ancestors of node 2
		ArrayList<Object> parentNodes2 = getAllParentNodes(db, classNode2);
		
		//match ancestors and if the similarity is above a threshold find their structProx
		//map to keep the pair of ancestors matching above the threshold
		Map<String,String> matchingMap = new HashMap<String,String>();
		
		double iSubSimScore = 0;
		double threshold = 0.8;

		for (Object i : parentNodes1) {
			for (Object j : parentNodes2) {
				iSubSimScore = iSubMatcher.score(i.toString(), j.toString());
				//if the similarity between the ancestors is equal to or above the defined threshold these two ancestors are kept
				if (iSubSimScore >= threshold) {
					System.out.println("Putting " + i.toString() + " and " + j.toString() + " in the matching map");
					matchingMap.put(i.toString(), j.toString());
				}				
			}
		}

		//iterate through the map of ancestors and compute the average distance to the root for the pair of ancestors and compute the structProx value
		double structProx = 0;
		double currentStructProx = 0;
		double avgAncestorDistanceToRoot = 0;
		
		//loop through the matchingMap containing key-value pairs of ancestors from O1 and O2 being similar over the given threshold
		for (Entry<String, String> entry : matchingMap.entrySet()) {
			
//			avgAncestorDistanceToRoot = (path1.shortestDistance(root,entry.getKey().toString()) + path2.shortestDistance(root,entry.getValue().toString())) / 2;
			Node ancestorX = getNode(db, label, "classname", entry.getKey());
			Node ancestorY = getNode(db, label, "classname", entry.getValue());
			
			avgAncestorDistanceToRoot = (findDistanceToRoot(db, ancestorX) + findDistanceToRoot(db, ancestorY)) / 2;
			
			currentStructProx = (2 * avgAncestorDistanceToRoot) / (class1DistanceToRoot + class2DistanceToRoot);

			if (currentStructProx > structProx) {
				structProx = currentStructProx;
			}

		}

		return structProx;
	}
	
	public static void main(String[] args) {
		
		GraphProcessor gp = new GraphProcessor();

		File dbFile = new File("/Users/audunvennesland/Documents/PhD/Development/Neo4J/Test5");
		GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(dbFile);
		registerShutdownHook(db);
		
		String ontology = "BIBO.owl";
		@SuppressWarnings("deprecation")
		Label label = DynamicLabel.label(ontology);
		String key = "classname";
		String class1 = "AcademicArticle";
		
		//get the node
		Node documentNode = gp.getNode(db, label, key, class1);

		
		//get the arraylist of childnodes
		ArrayList<Object> list = gp.getClosestChildNodesAsList(db, documentNode);
		
		System.out.println("Printing the closest child nodes");
		if (list.size() == 0) {
			System.out.println(documentNode + " has no sub-classes");
		} else {
		for (int i = 0; i < list.size(); i++) {
				System.out.println(list.get(i));
			}
		}
		
		//get the arraylist of parentnodes (should only be one)
		ArrayList<Object> parentList = gp.getClosestParentNode(db, documentNode);
		
		System.out.println("Printing the closest parent nodes");
		for (int i = 0; i < parentList.size(); i++) {
			System.out.println(parentList.get(i));
		}
		
		//find the distance to root (owl:Thing) for a given node
		int distance = gp.findDistanceToRoot(db, documentNode);
		System.out.println("The distance from " + documentNode.toString() + " to owl:Thing is " + distance);
		
		//computing structProx between 
		
		
	}

	
}

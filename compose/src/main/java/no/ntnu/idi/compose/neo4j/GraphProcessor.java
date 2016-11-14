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
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import no.ntnu.idi.compose.Loading.GraphLoader;
import no.ntnu.idi.compose.algorithms.ISub;

public class GraphProcessor {
	
	GraphDatabaseService db;
	Label label;
	String key;

	ISub iSubMatcher = new ISub();

	//should initialize with a graph database
	public GraphProcessor(GraphDatabaseService db, Label label, String key){
		this.db = db;
		this.label = label;
		this.key = key;
	}


	/**
	 * Registers a shutdown hook for the Neo4j instance so that it shuts down nicely when the VM exits
	 * @param graphDb
	 */
	private static void registerShutdownHook(final GraphDatabaseService db)
	{
		Runtime.getRuntime().addShutdownHook( new Thread()
		{
			@Override
			public void run()
			{
				db.shutdown();
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

	public Node getNode(String value) {
		Node testNode = null;
		
		try ( Transaction tx = db.beginTx() ) {
		testNode = db.findNode(label, key, value);
		tx.success();
		}
		//registerShutdownHook(db);
		return testNode;	
	}
	
	public String getNodeName(Node n) {

		String value = null;
		
		try ( Transaction tx = db.beginTx() ) {
		value = n.getProperty(key).toString();
		tx.success();
		}
		//registerShutdownHook(db);
		return value;	
	}
	
	public long getNodeID(Node n) {

		long id = 0;
		
		try ( Transaction tx = db.beginTx() ) {
			id = n.getId();
		tx.success();		
		}
		//registerShutdownHook(db);
		return id;	
	}

	public Traverser getChildNodesTraverser(Node classNode) {

		TraversalDescription td = null;
		try ( Transaction tx = db.beginTx() ) {

		td = db.traversalDescription().breadthFirst().relationships(RelTypes.isA, Direction.INCOMING).evaluator(Evaluators.excludeStartPosition());

		tx.success();
		}
		
		//registerShutdownHook(db);
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
		//registerShutdownHook(db);
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
		//registerShutdownHook(db);
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
	//registerShutdownHook(db);
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
			System.out.println("Adding " + parentNodePath.endNode().getId() + " to the array list...");
			}

	}

	tx.success();
	}
	//registerShutdownHook(db);
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

		//registerShutdownHook(db);
		
		int distanceToRoot = parentNodeMap.size();

		return distanceToRoot;
	}


	
	public static void main(String[] args) throws OWLOntologyCreationException {
		
		File dbFile = new File("/Users/audunvennesland/Documents/PhD/Development/Neo4J/CONFERENCE2EKAW");
		GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(dbFile);
		registerShutdownHook(db);
		
		//get the two ontology files
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		File f1 = new File("/Users/audunvennesland/Documents/PhD/Development/Experiments/Conference2Ekaw/conference/Conference.owl");		
		File f2 = new File("/Users/audunvennesland/Documents/PhD/Development/Experiments/Conference2Ekaw/conference/ekaw.owl");
		
		String key = "classname";
		
		//create the labels
		Label labelOnto1 = DynamicLabel.label( f1.toPath().getFileName().toString());
		Label labelOnto2 = DynamicLabel.label( f2.toPath().getFileName().toString());
		
		System.out.println("...Trying to load the ontologies...");
		OWLOntology onto1 = manager.loadOntologyFromOntologyDocument(f1);
		OWLOntology onto2 = manager.loadOntologyFromOntologyDocument(f2);

		System.out.println("...Trying to create a graph for each of the two ontologies...");
		//create a graph for each ontology file
		GraphLoader gl = new GraphLoader(db);
		gl.createOntologyGraph(onto1, labelOnto1);
		gl.createOntologyGraph(onto2, labelOnto2);
		
		/*//testing some operations on ontology graph 1
		GraphProcessor gp1 = new GraphProcessor(db, labelOnto1, key);
		GraphProcessor gp2 = new GraphProcessor(db, labelOnto2, key);
		
		String hydrogenCarClass = "HydrogenCar";
		String hydrogenClass ="Hydrogen";
		
		System.out.println("This will match " + hydrogenCarClass + " with " + hydrogenClass + " ...");
		
		//get the tanker node from ontology 1
		Node hydrogenCarNode = gp1.getNode(hydrogenCarClass);
		System.out.println("The node for class HydrogenCar has ID " + gp1.getNodeID(hydrogenCarNode));
		
		//get the hydrogen node from ontology 2
		Node hydrogenNode = gp2.getNode(hydrogenClass);
		System.out.println("The node for class Hydrogen has ID " + gp2.getNodeID(hydrogenNode));

		
		//get the parent nodes of a class from ontology 1
		System.out.println("The parents of the class HydrogenCar are: ");
		ArrayList onto1Parents = gp1.getAllParentNodes(hydrogenCarNode, labelOnto1);
		for (int i = 0; i < onto1Parents.size(); i++) {
			System.out.println(onto1Parents.get(i));
		}
		
		//get the parent nodes of a class from ontology 2
		System.out.println("The parents of the class Hydrogen are: ");
		ArrayList onto2Parents = gp2.getAllParentNodes(hydrogenNode,labelOnto2);
		for (int i = 0; i < onto2Parents.size(); i++) {
			System.out.println(onto2Parents.get(i));
		}
		
		
		//find distance from hydrogen node to owl:Thing
		int distanceHydrogen = gp1.findDistanceToRoot(hydrogenNode);
		System.out.println("The distance from Hydrogen to owl:Thing is " + distanceHydrogen);
		
		//find distance from HydrogenCar to owl:Thing
		int distanceHydrogenCar = gp2.findDistanceToRoot(hydrogenCarNode);
		System.out.println("The distance from HydrogenCar to owl:Thing is " + distanceHydrogenCar);
		
		double iSubSimScore = 0;
		ISub iSubMatcher = new ISub();
		
		//matching the parent nodes
		for (int i = 0; i < onto1Parents.size(); i++) {
			for (int j = 0; j < onto2Parents.size(); j++) {
				iSubSimScore = iSubMatcher.score(onto1Parents.get(i).toString(), onto2Parents.get(j).toString());
				System.out.println("The score between " + onto1Parents.get(i).toString() + " and " + onto2Parents.get(j).toString() + " is " + iSubSimScore);
			}
		}*/
		
		//find the average depth of an ontology by counting the edges between each class to the root and average these across all classes of an ontology
		
		
		
	}

	
}

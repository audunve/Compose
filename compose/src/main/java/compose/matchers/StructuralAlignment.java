package compose.matchers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.PathExpanders;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;

import compose.misc.ISub;
import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.ontowrap.OntowrapException;

@SuppressWarnings("deprecation")
public class StructuralAlignment extends ObjectAlignment implements AlignmentProcess {

	final double THRESHOLD = 0.7;

	static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	static OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
	
	File f1 = new File("./files/ontologies/Test/TestTransportWithInstances1.owl");		
	File f2 = new File("./files/ontologies/Test/TestTransportWithInstances2.owl");
	
	ISub iSubMatcher = new ISub();
	
	//assumes that the ontology graphs in the database is created
	//create the graph database
	//File dbFile = new File("/Users/audunvennesland/Documents/PhD/Development/Neo4J/CONFERENCE2EKAW");
	//GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(dbFile);
	File dbFile = new File("/Users/audunvennesland/Documents/PhD/Development/Neo4J/ntnu-lyon");
	GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(dbFile);
	
	String key = "classname";
			
	//create the labels
	Label labelOnto1 = DynamicLabel.label( f1.toPath().getFileName().toString());
	Label labelOnto2 = DynamicLabel.label( f2.toPath().getFileName().toString());

	

	/**
	 * The align() method is imported from the Alignment API and is modified to use the wordNetMatch method declared in this class
	 */
	public void align( Alignment alignment, Properties param ) throws AlignmentException {
		try {

			//for matching properties use the ontologyX.getProperties() method instead...
			// Match classes
			for ( Object cl2: ontology2().getClasses() ){
				for ( Object cl1: ontology1().getClasses() ){

					// add mapping into alignment object 

					addAlignCell(cl1,cl2, "=", computeStructProx(cl1,cl2));  
				}

			}

		} catch (Exception e) { e.printStackTrace(); }
	}
	
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

	public Node getNode(String value, Label label) {
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
	
	/**
	 * This method finds the shortest path between two nodes used as parameters. The path is the full path consisting of nodes and relationships between the classNode..
	 * ...and the parentNode.
	 * @param parentNode
	 * @param classNode
	 * @param label
	 * @param rel
	 * @return Iterable<Path> paths
	 */
	public Iterable<Path> findShortestPathBetweenNodes(Node parentNode, Node classNode, Label label, RelationshipType rel) {

		PathFinder<Path> finder = GraphAlgoFactory.shortestPath(
				PathExpanders.forType(rel), 15);
		Iterable<Path> paths = finder.findAllPaths( classNode, parentNode );
		return paths;

	}
	
	public double computePath(Object o1, Object o2) throws OWLOntologyCreationException, OntowrapException, IOException {
		
		double score = 0;
		//get parents of o1 and o2
		
		//match parents
		
		//find distance between o1/o2 and parents that match
		
		return score;
	}


	public double computeStructProx(Object o1, Object o2) throws OWLOntologyCreationException, OntowrapException, IOException {
		
		registerShutdownHook(db);		
		
		String s1 = ontology1().getEntityName(o1);
		String s2 = ontology2().getEntityName(o2);
		
		//get the s1 node from ontology 1
		Node s1Node = getNode(s1, labelOnto1);
		
		//get the s2 node from ontology 2
		Node s2Node = getNode(s2, labelOnto2);
	
		//get the parent nodes of a class from ontology 1
		ArrayList onto1Parents = getAllParentNodes(s1Node, labelOnto1);
		for (int i = 0; i < onto1Parents.size(); i++) {
		}
		
		//get the parent nodes of a class from ontology 2
		ArrayList onto2Parents = getAllParentNodes(s2Node,labelOnto2);
		for (int i = 0; i < onto2Parents.size(); i++) {
		}
		
		//find distance from s1 node to owl:Thing
		int distanceC1ToRoot = findDistanceToRoot(s1Node);
		
		//find distance from s2 to owl:Thing
		int distanceC2ToRoot = findDistanceToRoot(s2Node);
		
		double iSubSimScore = 0;
		ISub iSubMatcher = new ISub();
		
		//map to keep the pair of ancestors matching above the threshold
		Map<Object,Object> matchingMap = new HashMap<Object,Object>();
		
		//matching the parentnodes
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
			Node anc1 = getNode(entry.getKey().toString(), labelOnto1);
			Node anc2 = getNode(entry.getValue().toString(), labelOnto2);
			
			avgAncestorDistanceToRoot = (findDistanceToRoot(anc1) + findDistanceToRoot(anc2)) / 2;
			
			currentStructProx = (2 * avgAncestorDistanceToRoot) / (distanceC1ToRoot + distanceC2ToRoot);

			if (currentStructProx > structProx) {
				
				structProx = currentStructProx;
			}

		}
		
		return structProx;
	}
	
	

}
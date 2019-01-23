package graph;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import utilities.ISub;
import utilities.OntologyOperations;
import utilities.StringUtilities;

public class Graph {
	
	static GraphDatabaseService db;
	
	final static double THRESHOLD = 0.6;
	
	Label label;
	
	/**
	 * This label represents the graph/ontology to process
	 */
	static Label labelOnto1;
	/**
	 * This label represents the graph/ontology to process
	 */
	static Label labelOnto2;

	ISub iSubMatcher = new ISub();

	final static String key = "classname";

	public Graph(GraphDatabaseService db) {
		
		this.db = db;
		
	}

	/**
	 * This method creates a Neo4J graph from an input ontology
	 * @param OWLOntology onto
	 * @param Label label
	 * @param GraphDatabaseService db
	 * @throws OWLOntologyCreationException
	 */
	public static void createOntologyGraph(OWLOntology onto, Label label) throws OWLOntologyCreationException {

		Map<String, String> superClassMap = OntologyOperations.getClassesAndSuperClasses(onto);
		Set<String> classes = superClassMap.keySet();
		Iterator<String> itr = classes.iterator();

		try ( Transaction tx = db.beginTx() )
		{
			//creating a node for owl:Thing
			Node thingNode = db.createNode(label);
			thingNode.setProperty("classname", "owl:Thing");
			
			//create nodes from the ontology, that is, create nodes and give them properties (classname) according to their ontology names
			while (itr.hasNext()) {
				Node classNode = db.createNode(label);
				classNode.setProperty("classname", itr.next().toString());
			}

			//create isA relationships between classes and their superclasses
			ResourceIterable<Node> testNode = db.getAllNodes();
			ResourceIterator<Node> iter = testNode.iterator();
			
			//iterate through the nodes of the graph database
			while(iter.hasNext()) {
				Node n = iter.next();
				if (n.hasProperty("classname")) {
					String thisClassName = n.getProperty("classname").toString();
					String superClass = null;
					//check if thisClassName equals any of the keys in superClassMap
					for (Map.Entry<String, String> entry : superClassMap.entrySet()) {
						//if this graph node matches a key in the map...
						if (thisClassName.equals(entry.getKey())) {
							//get the superclass that belongs to the key in the map
							superClass = superClassMap.get(entry.getKey());
							//find the "superclass-node" that matches the map value belonging to this key class
							Node superClassNode = db.findNode(label, "classname", (Object) superClassMap.get(thisClassName));
							//create an isA relationship from this graph node to its superclass
							//if a class does not have any defined super-classes, create an isA relationship to owl:thing
							if (superClassNode != null) {
								n.createRelationshipTo(superClassNode, RelTypes.isA);				
							} else {
								n.createRelationshipTo(thingNode, RelTypes.isA);			    		
							}
						}
					}
				}
			}
			
			//TO-DO:create the individuals
			
			//TO-DO:create the object property relations
			
			//TO-DO:create the datatype properties

			tx.success();
		}

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
	
	/**
	 * Returns a graph node given a label, a property name and property value
	 * @param value
	 * @param label a label represents the graph/ontology to process
	 * @return the node searched for
	 */
	public static Node getNode(String value, Label label) {
		Node testNode = null;

		try ( Transaction tx = db.beginTx() ) {
			testNode = db.findNode(label, key, value);
			tx.success();
		}
		return testNode;	

	}
	
	/**
	 * Returns the ID of a node given the Node instance as parameter
	 * @param n a Node instance
	 * @return the ID of a node as a long
	 */
	public long getNodeID(Node n) {

		long id = 0;

		try ( Transaction tx = db.beginTx() ) {
			id = n.getId();
			tx.success();	

		}

		return id;	
	}

	/**
	 * Returns a Traverser that traverses the children of a node given a Node instance as parameter
	 * @param classNode a Node instance
	 * @return a traverser
	 */
	public static Traverser getChildNodesTraverser(Node classNode) {

		TraversalDescription td = null;
		try ( Transaction tx = db.beginTx() ) {

			td = db.traversalDescription().breadthFirst().relationships(RelTypes.isA, Direction.INCOMING).evaluator(Evaluators.excludeStartPosition());
			tx.success();

		}

		return td.traverse(classNode);
	}

	/**
	 * Returns an ArrayList of all child nodes of a node
	 * @param classNode a Node instance
	 * @param label representing the graph/ontology to process
	 * @return
	 */
	public static ArrayList<Object> getClosestChildNodesAsList(Node classNode, Label label) {

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

	/**
	 * Returns a Traverser that traverses the parents of a node given a Node instance as parameter
	 * @param classNode a Node instance
	 * @return a traverser
	 */
	public static Traverser getParentNodeTraverser (Node classNode) {

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

	//TO-DO: Why is this an ArrayList and not a Node being returned?
	/**
	 * Returns an ArrayList holding the parent node of the node provided as parameter
	 * @param classNode a node for which the closest parent is to be returned
	 * @param label a label representing the graph (ontology) to process
	 * @return the closest parent node
	 */
	public static ArrayList<Object> getClosestParentNode(Node classNode, Label label) {

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

	/**
	 * Returns an ArrayList holding all parent nodes to the Node provided as parameter
	 * @param classNode the Node for which all parent nodes are to be retrieved
	 * @param label representing the graph/ontology to process
	 * @return all parent nodes to node provided as parameter
	 */
	public static ArrayList<Object> getAllParentNodes(Node classNode, Label label) {

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
	 * This method finds the shortest path between two nodes used as parameters. The path is the full path consisting of nodes and relationships between the classNode..
	 * ...and the parentNode.
	 * @param parentNode
	 * @param classNode
	 * @param label
	 * @param rel
	 * @return Iterable<Path> paths
	 */
	public static Iterable<Path> findShortestPathBetweenNodes(Node parentNode, Node classNode, Label label, RelationshipType rel) {

		PathFinder<Path> finder = GraphAlgoFactory.shortestPath(
				PathExpanders.forType(rel), 15);
		Iterable<Path> paths = finder.findAllPaths( classNode, parentNode );
		return paths;

	}

	/**
	 * Returns the distance from the Node provided as parameter and the root node (i.e. owl:Thing)
	 * We use a Map as a work-around to counting the edges between a given node and the root (owl:Thing). This is possible since a Map only allows
	 * unique keys and a numbered Neo4J path consists of a set of path items <edge-count, node (property)> where all nodes for each edge-count
	 * is listed (e.g. for the node "AcademicArticle" the upwards path is <1, Article>, <2, Document>, <3, owl:Thing>). 
	 * @param classNode
	 * @return
	 */
	public static int findDistanceToRoot(Node classNode) {

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

	/**
	 * This method finds the shortest path between two nodes used as parameters. The path is the full path consisting of nodes and relationships between the classNode..
	 * ...and the rootNode.
	 * @param rootNode
	 * @param classNode
	 * @param label
	 * @param rel
	 * @return Iterable<Path> paths
	 */
	public Iterable<Path> findShortestPathToRoot(Node rootNode, Node classNode, Label label, RelationshipType rel) {

		PathFinder<Path> finder = GraphAlgoFactory.shortestPath(
				PathExpanders.forType(rel), 15);
		Iterable<Path> paths = finder.findAllPaths( classNode, rootNode );
		return paths;
	}
	

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
	
	public static void main(String[] args) throws OWLOntologyCreationException {
		
		//create the database
		File dbFile = new File("/Users/audunvennesland/Documents/PhD/Development/Neo4J/PathMatcher");
		GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(dbFile);
		registerShutdownHook(db);
		
		//get the ontology
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		File f1 = new File("./files/PathMatcher/PathMatcher-1.owl");		
		System.out.println("...Loading ontology " + StringUtilities.stripPath(f1.toString()));
		OWLOntology o1 = manager.loadOntologyFromOntologyDocument(f1);

		String ontologyName = manager.getOntologyDocumentIRI(o1).getFragment();
		System.out.println("The name of the ontology is " + ontologyName);

		Label label = DynamicLabel.label( ontologyName );
		
		Graph loader = new Graph(db);
		
		System.out.println("Trying to create a graph...");

		loader.createOntologyGraph(o1, label);
		
		System.out.println("Graph created successfully!");
		
	}
}
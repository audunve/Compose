package no.ntnu.idi.compose.matchers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
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
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;

import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import fr.inrialpes.exmo.ontowrap.OntowrapException;
import no.ntnu.idi.compose.algorithms.ISub;
import no.ntnu.idi.compose.preprocessing.Preprocessor;

/**
 * This matcher represents ontologies as graphs (directed acyclic graphs) in the Neo4J graph database and performs structural matching using parent-node and child-node
 * similarity.
 * @author audunvennesland
 * 2. feb. 2017
 */
@SuppressWarnings("deprecation")
public class ClassEq_Structural_Matcher extends ObjectAlignment implements AlignmentProcess {

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
	public ClassEq_Structural_Matcher(String ontology1Name, String ontology2Name,GraphDatabaseService database) {
		labelOnto1 = DynamicLabel.label(ontology1Name);
		labelOnto2 = DynamicLabel.label(ontology2Name);
		db = database;
	}


	/**
	 * The align() method is imported from the Alignment API and is modified to use the methods declared in this class
	 */
	public void align( Alignment alignment, Properties param ) throws AlignmentException {
		try {

			//for matching properties use the ontologyX.getProperties() method instead...
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
	 * A shutdown hook that takes down the database "gracefully"
	 * @param db
	 */
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

	private static enum RelTypes implements RelationshipType
	{
		isA
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
	 * Returns the name associated with a node given the Node instance as parameter
	 * @param n a Node instance
	 * @return a Node name
	 */
	/*public static String getNodeName(Node n) {
		
		//test
		System.out.println("Getting node name for " + n);

		String value = null;
		String nodeName = null;

		try ( Transaction tx = db.beginTx() ) {
			value = n.getProperty(key).toString();
			
			tx.success();

		}

		return value;	
	}*/

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
		Node s1Node = getNode(s1, labelOnto1);

		//get the s2 node from ontology 2
		Node s2Node = getNode(s2, labelOnto2);

		//get the parent nodes of a class from ontology 1
		
		//test
		//System.out.println("Getting parent nodes of " + getNodeName(s1Node));
		ArrayList onto1Parents = getAllParentNodes(s1Node, labelOnto1);
		for (int i = 0; i < onto1Parents.size(); i++) {
		}
		//test
		//System.out.println("The size of the parent node list of S1 " + getNodeName(s1Node) + " is " + onto1Parents.size());
		//get the parent nodes of a class from ontology 2
		//test
		//System.out.println("Getting parent nodes of " + getNodeName(s2Node));
		ArrayList onto2Parents = getAllParentNodes(s2Node,labelOnto2);
		for (int i = 0; i < onto2Parents.size(); i++) {
		}
		//test
		//System.out.println("The size of the parent node list of S2 " + getNodeName(s2Node) + " is " + onto2Parents.size());

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
		Node s1Node = getNode(s1, labelOnto1);
		System.out.println("S1 Node retrieved: " + s1Node.toString());

		//get the s2 node from ontology 2
		Node s2Node = getNode(s2, labelOnto2);
		System.out.println("S2 Node retrieved: " + s2Node.toString());
		
		//get the parent nodes of a class from ontology 1
		ArrayList onto1SubClasses = getClosestChildNodesAsList(s1Node, labelOnto1);
		for (int i = 0; i < onto1SubClasses.size(); i++) {
		}

		//get the parent nodes of a class from ontology 2
		ArrayList onto2SubClasses = getClosestChildNodesAsList(s2Node,labelOnto2);
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

		double structProx = matchSuperClasses(o1, o2);

		//System.out.println("The structProx of " + o1.toString() + " and " + o2.toString() + " is " + structProx);

		double subClassMatch = matchSubClasses(o1, o2);

		//System.out.println("The subclass match of " + o1.toString() + " and " + o2.toString() + " is " + subClassMatch);

		double neighborhoodMatch = (structProx + subClassMatch) / 2;

		//System.out.println("The neighborhood match of " + o1.toString() + " and " + o2.toString() + " is " + neighborhoodMatch + "\n");


		if ((structProx + subClassMatch) / 2 > THRESHOLD) {
			return neighborhoodMatch;
		} else {
			return 0;
		}

	}
	
	public static void main(String[] args) {
		
		File dbFile = new File("/Users/audunvennesland/Documents/PhD/Development/Neo4J/ntnu-lyon");
		GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(dbFile);
		registerShutdownHook(db);
		
		//find a node Node getNode(String value, Label label) / testNode = db.findNode(label, key, value);
		String value = "Line";
		Label lab = DynamicLabel.label("OTN.owl");
		String key = "classname";
		//Node testNode = getNode(value,lab);
		Node testNode = null;
		String nodeName = null;
		
		try ( Transaction tx = db.beginTx() ) {
			
			testNode = db.findNode(lab,key,value);
			nodeName = testNode.getProperty(key).toString();
		}
		
		//public static ArrayList<Object> getAllParentNodes(Node classNode, Label label) {
		ArrayList<Object> parentList = getAllParentNodes(testNode, lab);
		//System.out.println("Size of parentlist to " + nodeName + " is " + parentList.size());

		
		
		
	}
	
	

}

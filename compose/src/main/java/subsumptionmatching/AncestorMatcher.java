package subsumptionmatching;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

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
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.ontowrap.OntowrapException;
import utilities.ISub;

@SuppressWarnings("deprecation")
public class AncestorMatcher extends ObjectAlignment implements AlignmentProcess {

	
	private static final Logger logger = LoggerFactory.getLogger(AncestorMatcher.class);

	final double THRESHOLD = 0.6;
	final String isA = "&lt;";
	final String hasA = "&gt;";

	Label labelOnto1;
	Label labelOnto2;
	
	double weight;

	GraphDatabaseService db;

	ISub iSubMatcher = new ISub();

	String key = "classname";


	//constructor that receives the labels (ontology file names) from TestMatcher.java
	public AncestorMatcher(String ontology1Name, String ontology2Name, GraphDatabaseService database, double weight) {
		labelOnto1 = DynamicLabel.label(ontology1Name);
		labelOnto2 = DynamicLabel.label(ontology2Name);
		db = database;
		this.weight = weight;
	}


	/**
	 * The align() method is imported from the Alignment API and is modified to use the methods declared in this class
	 */
	public void align( Alignment alignment, Properties param ) throws AlignmentException {
		
		System.out.println("\nStarting Ancestor Matcher...");
		long startTime = System.currentTimeMillis();
		
		try {

			// Match classes
			for ( Object cl2: ontology2().getClasses() ){
				for ( Object cl1: ontology1().getClasses() ){

					//System.out.println("Matching " + cl1.toString() + " and " + cl2.toString());
					Map<String, Double> matchingMap = computePath(cl1, cl2);
					

					// add mapping into alignment object for each entry in the matching map
					for (Map.Entry<String, Double> entry : matchingMap.entrySet()) {
						addAlignCell(cl1,cl2, entry.getKey(), weight*entry.getValue());  
					}


				}

			}

		} catch (Exception e) { e.printStackTrace(); }
		
		long endTime = System.currentTimeMillis();
		System.out.println("Ancestor Matcher completed in " + (endTime - startTime) / 1000 + " seconds.");
	}

	
public Map<String, Double> computePath(Object o1, Object o2) throws OWLOntologyCreationException, OntowrapException, IOException {
		
		logger.debug("Hello from AncestorMatcher - computePath()");
		
		//map to keep the relation and matching score
		Map<String,Double> matchingMap = new HashMap<String,Double>();
		

		double score = 0;
		String s1 = ontology1().getEntityName(o1);
		String s2 = ontology2().getEntityName(o2);
		
		//get the s1 node from ontology 1
		Node s1Node = getNode(s1, labelOnto1);

		//get the s2 node from ontology 2
		Node s2Node = getNode(s2, labelOnto2);

		//get parents of o1 and o2
		ArrayList<Object> parentNodesO1 = getAllParentNodes(s1Node,labelOnto1);
		ArrayList<Object> parentNodesO2 = getAllParentNodes(s2Node,labelOnto2);
		
		//System.out.println("Number of parents to s1Node: " + parentNodesO1.size());
		//System.out.println("Number of parents to s2Node: " + parentNodesO2.size());
		
		//holds similar parents (key = O1 parent, value = O2 parent)
		//Map<String, String> parentMap = new HashMap<String, String>();
		ArrayList<String> parents = new ArrayList<String>();
		
		double sim = 0;
		

		//match parents
		//if there are several parents that match:
		//- select the pair of parents with the highest similarity
		//- if the similarity is equal among two or more pairs, 
		//select the pair with the average shortest distance to the objects being matched (o1 and o2) as these are considered more "discriminating"
		for (int i = 0; i < parentNodesO1.size(); i++) {
			for (int j = 0; j < parentNodesO2.size(); j++) {
				
				double thisSim = iSubMatcher.score(parentNodesO1.get(i).toString(), parentNodesO2.get(j).toString());
				
				//if two parents are equal, put them in the parent map, but only if their similarity is greater than any previous similarity computed
				if (thisSim >= THRESHOLD && thisSim > sim) {
					//parentMap.put(parentNodesO1.get(i).toString(), parentNodesO2.get(j).toString());
					parents.add(parentNodesO1.get(i).toString());
					parents.add(parentNodesO2.get(j).toString());
					sim = thisSim;
				}
			}
		}
		
		//System.out.println("Similar parents: ");
		if (parents.size() > 0) {
		for (String s : parents) {
			//System.err.println(s);
			
			//find distance between o1/o2 and parents that match (the most)
			Node parentO1Node = getNode(parents.get(0), labelOnto1);
			Node parentO2Node = getNode(parents.get(1), labelOnto2);
			
			Iterable<Path> o1DistanceToSimParent = findShortestPathBetweenNodes(s1Node, parentO1Node, labelOnto1, RelTypes.isA);
			Iterable<Path> o2DistanceToSimParent = findShortestPathBetweenNodes(s2Node, parentO2Node, labelOnto2, RelTypes.isA);
			
			Path pathO1 = o1DistanceToSimParent.iterator().next();
			Path pathO2 = o2DistanceToSimParent.iterator().next();
			
			int o1Distance = pathO1.length();
			//System.out.println("Distance from " + s1 + " to " + parents.get(0) + ": " + o1Distance);
			int o2Distance = pathO2.length();
			//System.out.println("Distance from " + s2 + " to " + parents.get(1) + ": " + o2Distance);
			
			//constrain so that there has to be some terminological similarity between the concepts
			double conceptSimScore = iSubMatcher.score(s1, s2);
			
			//constrain so that the respective distance between o1 and common parent and o2 and common parent is not more than 1
			int respectiveDistance = 0;
			if (o1Distance > o2Distance) {
			respectiveDistance = o1Distance - o2Distance;
			} else {
				respectiveDistance = o2Distance - o1Distance;
			}

			
//			if (o1Distance > o2Distance && respectiveDistance == 1 && conceptSimScore > 0.4 ) {
			if (o1Distance > o2Distance && conceptSimScore > 0.4 ) {
				score = 1;
				matchingMap.put(isA, score);
			} else {
				score = 0;
				matchingMap.put(isA, score);
			}
			
			return matchingMap;
			
		}} else {
			//System.out.println("There are no similar parents for this pair!");
			
			matchingMap.put(isA, 0.0);
		
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
				if (parentNodePath.endNode().hasLabel(label) && !parentNodePath.endNode().getProperty("classname").equals("owl:Thing")){
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
	
	/**
	 * This method finds the shortest path between two nodes used as parameters. The path is the full path consisting of nodes and relationships between the classNode..
	 * ...and the parentNode.
	 * @param parentNode
	 * @param classNode
	 * @param label
	 * @param rel
	 * @return Iterable<Path> paths
	 */
	public  Iterable<Path> findShortestPathBetweenNodes(Node parentNode, Node classNode, Label label, RelationshipType rel) {

		Iterable<Path> paths = null;
		try ( Transaction tx = db.beginTx() ) {
		PathFinder<Path> finder = GraphAlgoFactory.shortestPath(
				PathExpanders.forType(rel), 15);
		paths = finder.findAllPaths( classNode, parentNode );
		
		tx.success();

		}
		
		return paths;

	}



}

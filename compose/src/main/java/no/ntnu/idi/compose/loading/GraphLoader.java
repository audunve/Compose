package no.ntnu.idi.compose.loading;

import java.io.File;
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
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 * @author audunvennesland
 * Date:02.02.2017
 * @version 1.0
 */
public class GraphLoader {
	
	GraphDatabaseService db;

	/**
	 * Constructor to initialise the graph database
	 * @param db a GraphDatabaseService
	 */
	public GraphLoader(GraphDatabaseService db) {
		
		this.db = db;
		
	}

	/**
	 * This method creates a Neo4J graph from an input ontology
	 * @param onto the OWL ontology for which a graph is created
	 * @param label a label is used for creating the name of the graph (the name of the graph is the same as the ontology name)
	 * @throws OWLOntologyCreationException An exception which describes an error during the creation of an ontology. If an ontology cannot be created then subclasses of this class will describe the reasons.
	 */
	public void createOntologyGraph(OWLOntology onto, Label label) throws OWLOntologyCreationException {

		Map<String, String> superClassMap = OWLLoader.getClassesAndSuperClasses(onto);
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

	/**
	 * Registers a shutdown hook for the Neo4j instance so that it shuts down nicely when the VM exits
	 * @param graphDb a GraphDatabaseService
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

/**
 * 
 * @author audunvennesland
 * 2. feb. 2017
 */
	private static enum RelTypes implements RelationshipType
	{
		isA
	}
	
	/**
	 * Test method
	 * @param args
	 * @throws OWLOntologyCreationException An exception which describes an error during the creation of an ontology. If an ontology cannot be created then subclasses of this class will describe the reasons.
	 */
	public static void main(String[] args) throws OWLOntologyCreationException {
		
		//create the database
		File dbFile = new File("/Users/audunvennesland/Documents/PhD/Development/Neo4J/ntnu-lyon-paper");
		GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(dbFile);
		registerShutdownHook(db);
		
		//get the ontology
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		//File f1 = new File("./files/ntnu-lyon-paper/ontologies/doremus.owl");		
		File f1 = new File("./files/ntnu-lyon-paper/ontologies/otn.owl");		
		System.out.println("...Trying to load the ontology...");
		OWLOntology o1 = manager.loadOntologyFromOntologyDocument(f1);

		String ontologyName = manager.getOntologyDocumentIRI(o1).getFragment();
		System.out.println("The name of the ontology is " + ontologyName);

		Label label = DynamicLabel.label( ontologyName );
		
		GraphLoader loader = new GraphLoader(db);
		
		System.out.println("Trying to create a graph...");

		loader.createOntologyGraph(o1, label);
		
		System.out.println("Graph created successfully!");
		
	}
}

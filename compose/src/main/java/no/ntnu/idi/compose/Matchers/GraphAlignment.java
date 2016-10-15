package no.ntnu.idi.compose.Matchers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;

import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.ontowrap.OntowrapException;
import no.ntnu.idi.compose.Loading.GraphLoader;
import no.ntnu.idi.compose.algorithms.ISub;
import no.ntnu.idi.compose.neo4j.GraphProcessor;

@SuppressWarnings("deprecation")
public class GraphAlignment extends ObjectAlignment implements AlignmentProcess {

	final double THRESHOLD = 0.8;

	static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	static OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
	ISub iSubMatcher = new ISub();

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
					System.out.println("Adding " + cl1 + " and " + cl2 + " to the alignment");
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


	public double computeStructProx(Object o1, Object o2) throws OWLOntologyCreationException, OntowrapException, IOException {
		
		//create the graph database
		File dbFile = new File("/Users/audunvennesland/Documents/PhD/Development/Neo4J/MatchingDB");
		GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(dbFile);
		//registerShutdownHook(db);
		
		//get the two ontology files
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		File f1 = new File("/Users/audunvennesland/Documents/PhD/Ontologies/TestOntologiesTransport/TestTransport1.owl");		
		File f2 = new File("/Users/audunvennesland/Documents/PhD/Ontologies/TestOntologiesTransport/TestTransport2.owl");
		
		String key = "classname";
		
		//create the labels
		Label labelOnto1 = DynamicLabel.label( f1.toPath().getFileName().toString());
		Label labelOnto2 = DynamicLabel.label( f2.toPath().getFileName().toString());
		
		System.out.println("...Trying to load the ontologies...");
		OWLOntology onto1 = manager.loadOntologyFromOntologyDocument(f1);
		OWLOntology onto2 = manager.loadOntologyFromOntologyDocument(f2);

		System.out.println("...Trying to create a graph for each of the two ontologies...");
		//create a graph for each ontology file
		//GraphLoader gl = new GraphLoader(db);
		//gl.createOntologyGraph(onto1, labelOnto1);
		//gl.createOntologyGraph(onto2, labelOnto2);
		
		//testing some operations on ontology graph 1
		GraphProcessor gp1 = new GraphProcessor(db, labelOnto1, key);
		GraphProcessor gp2 = new GraphProcessor(db, labelOnto2, key);
		
		String s1 = ontology1().getEntityName(o1);
		String s2 = ontology2().getEntityName(o2);
		
		
		System.out.println("This will match " + s1 + " with " + s2 + " ...");
		
		//get the s1 node from ontology 1
		Node s1Node = gp1.getNode(s1);
		
		//get the s2 node from ontology 2
		Node s2Node = gp2.getNode(s2);
	
		//get the parent nodes of a class from ontology 1
		ArrayList onto1Parents = gp1.getAllParentNodes(s1Node, labelOnto1);
		for (int i = 0; i < onto1Parents.size(); i++) {
			System.out.println(onto1Parents.get(i));
		}
		
		//get the parent nodes of a class from ontology 2
		ArrayList onto2Parents = gp2.getAllParentNodes(s2Node,labelOnto2);
		for (int i = 0; i < onto2Parents.size(); i++) {
			System.out.println(onto2Parents.get(i));
		}
		
		//find distance from s1 node to owl:Thing
		int distanceC1ToRoot = gp1.findDistanceToRoot(s1Node);
		
		//find distance from s2 to owl:Thing
		int distanceC2ToRoot = gp2.findDistanceToRoot(s2Node);
		
		double iSubSimScore = 0;
		ISub iSubMatcher = new ISub();
		
		//map to keep the pair of ancestors matching above the threshold
		Map<Object,Object> matchingMap = new HashMap<Object,Object>();
		
		//matching the parentnodes
		for (int i = 0; i < onto1Parents.size(); i++) {
			for (int j = 0; j < onto2Parents.size(); j++) {
				iSubSimScore = iSubMatcher.score(onto1Parents.get(i).toString(), onto2Parents.get(j).toString());
				System.out.println("The score between " + onto1Parents.get(i).toString() + " and " + onto2Parents.get(j).toString() + " is " + iSubSimScore);
				if (iSubSimScore >= THRESHOLD) {
					System.out.println("Putting " + onto1Parents.get(i) + " and " + onto2Parents.get(j) + " in the matching map");
					matchingMap.put(onto1Parents.get(i) , onto2Parents.get(j));
				}	
			}
		}
		
		double structProx = 0;
		double currentStructProx = 0;
		double avgAncestorDistanceToRoot = 0;
		
		System.out.println("The size of the matching map is " + matchingMap.size());
		
		//loop through the matchingMap containing key-value pairs of ancestors from O1 and O2 being similar over the given threshold
		for (Entry<Object, Object> entry : matchingMap.entrySet()) {
			Node anc1 = gp1.getNode(entry.getKey().toString());
			Node anc2 = gp2.getNode(entry.getValue().toString());
			
			avgAncestorDistanceToRoot = (gp1.findDistanceToRoot(anc1) + gp2.findDistanceToRoot(anc2)) / 2;
			System.out.println("The avgAncestorDistanceToRoot is " + avgAncestorDistanceToRoot);
			
			currentStructProx = (2 * avgAncestorDistanceToRoot) / (distanceC1ToRoot + distanceC2ToRoot);

			if (currentStructProx > structProx) {
				
				structProx = currentStructProx;
			}

		}
		System.out.println("The structProx is " + structProx);
		return structProx;
	}
	
	

}

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
import org.neo4j.graphdb.Transaction;
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

public class GraphAlignment extends ObjectAlignment implements AlignmentProcess {


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
					addAlignCell(cl1,cl2, "=", computeStructProx(cl1,cl2));  
				}

			}

		} catch (Exception e) { e.printStackTrace(); }
	}


	public double computeStructProx(Object o1, Object o2) throws OWLOntologyCreationException, OntowrapException, IOException {

		final String PROPERTYNAME = "classname";

		//create the graph database
		File dbFile = new File("/Users/audunvennesland/Documents/PhD/Development/Neo4J/MatchingDB");
		GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(dbFile);
		
		File ontoFile1 = new File("/Users/audunvennesland/Documents/PhD/Ontologies/TestOntologiesTransport/TestTransport1.owl");
		File ontoFile2 = new File("/Users/audunvennesland/Documents/PhD/Ontologies/TestOntologiesTransport/TestTransport2.owl");

		OWLOntology onto1 = manager.loadOntologyFromOntologyDocument(ontoFile1);
		OWLOntology onto2 = manager.loadOntologyFromOntologyDocument(ontoFile2);

		@SuppressWarnings("deprecation")
		Label labelOnto1 = DynamicLabel.label( ontoFile1.toPath().getFileName().toString() );
		@SuppressWarnings("deprecation")
		Label labelOnto2 = DynamicLabel.label( ontoFile2.toPath().getFileName().toString()  );

		//create the ontology graphs
		GraphLoader gLoader = new GraphLoader();
		
		System.out.println("Creating graphs " + labelOnto1.toString() + " and " + labelOnto2.toString());

		gLoader.createOntologyGraph(onto1, labelOnto1, db);
		gLoader.createOntologyGraph(onto2, labelOnto2, db);

		String s1 = ontology1().getEntityName(o1);
		String s2 = ontology2().getEntityName(o2);

		GraphProcessor gp = new GraphProcessor();

		
		//get the nodes to be matched from the graphs
		Node n1 = gp.getNode(db, labelOnto1, PROPERTYNAME, s1);
		Node n2 = gp.getNode(db, labelOnto2, PROPERTYNAME, s2);
		
		//System.out.println("Retrieving " + n1.getProperty(PROPERTYNAME) + " and " + n2.getProperty(PROPERTYNAME) + " and preparing them for the matching operation");

		//find the distance of these nodes to the root (owl:Thing)
		int distanceC1ToRoot = gp.findDistanceToRoot(db, n1);
		int distanceC2ToRoot = gp.findDistanceToRoot(db, n2);
		
		//System.out.println("The distance from " + n1.getProperty(PROPERTYNAME) + " to owl:Thing is " + distanceC1ToRoot + "\n" + " and the distance from " + 
		//n2.getProperty(PROPERTYNAME) + " to owl:Thing is " + distanceC2ToRoot);

		//find the list of ancestors of node 1
		ArrayList<Object> parentNodes1 = gp.getAllParentNodes(db, n1);
		//find the list of ancestors of node 2
		ArrayList<Object> parentNodes2 = gp.getAllParentNodes(db, n2);
		
		
		System.out.println("The parent nodes of object 1 are ");
		for (int i = 0; i < parentNodes1.size(); i++) {
			System.out.println(parentNodes1.get(i));
		}
		
		System.out.println("The parent nodes of object 2 are ");
		for (int i = 0; i < parentNodes2.size(); i++) {
			System.out.println(parentNodes2.get(i));
		}

		//match ancestors and if the similarity is above a threshold find their structProx
		//map to keep the pair of ancestors matching above the threshold
		Map<String,String> matchingMap = new HashMap<String,String>();

		double iSubSimScore = 0;
		double threshold = 0.8;

		//the matching map contains ancestors from node 1 as key and ancestors from node 2 as values
		
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

		//compute the structProx
		double structProx = 0;
		double currentStructProx = 0;
		double avgAncestorDistanceToRoot = 0;

		//loop through the matchingMap containing key-value pairs of ancestors from O1 and O2 being similar over the given threshold
		for (Entry<String, String> entry : matchingMap.entrySet()) {
			
			Node anc1 = gp.getNode(db, labelOnto1, PROPERTYNAME, entry.getKey());
			Node anc2 = gp.getNode(db, labelOnto2, PROPERTYNAME, entry.getValue());

			avgAncestorDistanceToRoot = ((double)gp.findDistanceToRoot(db, anc1) + (double)gp.findDistanceToRoot(db, anc2)) / 2;

			currentStructProx = (2 * avgAncestorDistanceToRoot) / ((double)distanceC1ToRoot + (double)distanceC2ToRoot);

			if (currentStructProx > structProx) {
				structProx = currentStructProx;
			}

		}

		return structProx;
	}



}

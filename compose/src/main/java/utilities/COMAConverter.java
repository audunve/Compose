package utilities;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import graph.Graph;

public class COMAConverter {

	public static void main(String[] args) throws OWLOntologyCreationException, AlignmentException {

		File ontoFile1 = new File("./files/ESWC_ATMONTO_AIRM/ATMOntoCoreMerged.owl");
		File ontoFile2 = new File("./files/ESWC_ATMONTO_AIRM/airm-mono.owl");

		//create a new instance of the neo4j database in each run
		String ontologyParameter1 = null;
		String ontologyParameter2 = null;	
		Graph creator = null;
		OWLOntologyManager manager = null;
		OWLOntology o1 = null;
		OWLOntology o2 = null;
		Label labelO1 = null;
		Label labelO2 = null;
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		String dbName = String.valueOf(timestamp.getTime());
		File dbFile = new File("/Users/audunvennesland/Documents/phd/development/Neo4J_new/" + dbName);	
		System.out.println("Creating a new database");
		GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(dbFile);
		System.out.println("Database created");
		//registerShutdownHook(db);

		ontologyParameter1 = StringUtilities.stripPath(ontoFile1.toString());
		ontologyParameter2 = StringUtilities.stripPath(ontoFile2.toString());

		//create new graphs
		manager = OWLManager.createOWLOntologyManager();
		o1 = manager.loadOntologyFromOntologyDocument(ontoFile1);
		o2 = manager.loadOntologyFromOntologyDocument(ontoFile2);

		labelO1 = DynamicLabel.label( ontologyParameter1 );
		labelO2 = DynamicLabel.label( ontologyParameter2 );

		System.out.println("Creating ontology graphs");
		creator = new Graph(db);

		creator.createOntologyGraph(o1, labelO1);
		creator.createOntologyGraph(o2, labelO2);

		File inputAlignmentFile = new File("./files/ESWC_ATMONTO_AIRM/v2-19102018/Equivalence/AML-ATMONTO-AIRM-05.rdf");
		AlignmentParser parser = new AlignmentParser();
		BasicAlignment inputAlignment = (BasicAlignment) parser.parse(inputAlignmentFile.toURI().toString());

		Map<String, Set<String>> super1 = OntologyOperations.getSuperclasses(o1);
		System.out.println("Printing superclasses in ATMONTO");

		Map<String, Set<String>> super2 = OntologyOperations.getSuperclasses(o2);

		try ( Transaction tx = db.beginTx() ) {

			for (Cell c : inputAlignment) {

				StringBuffer path1 = new StringBuffer();
				StringBuffer path2 = new StringBuffer();
				String fullPath1 = null;
				String fullPath2 = null;	
				ArrayList<Object> c1Parents = new ArrayList<Object>();
				ArrayList<Object> c2Parents = new ArrayList<Object>();

				Node s1Node = Graph.getNode(c.getObject1AsURI().getFragment(), labelO1);
				Node s2Node = Graph.getNode(c.getObject2AsURI().getFragment(), labelO2);
				
				if (!s1Node.toString().equals("owl:Thing")) {
					c1Parents = Graph.getAllParentNodes(s1Node, labelO1);
				}

				if (!s2Node.toString().equals("owl:Thing")) {
					c2Parents = Graph.getAllParentNodes(s2Node, labelO2);
				}
				
				Collections.reverse(c1Parents);
				Collections.reverse(c2Parents);

				if (c1Parents != null) {
					for (Object s : c1Parents) {
						if (!s.toString().equals("owl:Thing")) {
							path1.append(s.toString() + "/");
						}
					}
					fullPath1 = path1.toString() + s1Node.getProperty("classname");
				} else {
					fullPath1 = c.getObject1AsURI().getFragment();
				}

				if (c2Parents != null) {
					for (Object s : c2Parents) {
						if (!s.toString().equals("owl:Thing")) {
							path2.append(s.toString() + "/");
						}
					}
					fullPath2 = path2.toString() + s2Node.getProperty("classname");
				} else {
					fullPath2 = c.getObject2AsURI().getFragment();
				}

				System.out.println(fullPath1 + " :: " + fullPath2 +  " :: " + c.getStrength());

			}

			tx.success();

		}

	}


}

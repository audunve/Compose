package ui;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.Properties;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
//Alignment API classes
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import backup.EditMatcher_remove;
import backup.InstanceMatcher_remove;
import backup.ParentMatcher;
import backup.SuperclassMatcher;
import backup.TrigramMatcher;
import backup.WNRiWordNetDistance;
import equivalencematching.DefinitionsEquivalenceMatcher;
import equivalencematching.StringEquivalenceMatcher;
import equivalencematching.PropertyEquivalenceMatcher;
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import graph.Graph;
import meronymmatching.WNMeronymMatcher;
import subsumptionmatching.AncestorSubsumptionMatcher;
import subsumptionmatching.CompoundSubsumptionMatcher;
import subsumptionmatching.DefinitionsSubsumptionMatcher;
import subsumptionmatching.LexicalSubsumptionMatcher;
import utilities.StringUtilities;


public class TestMatcher {
	
	public static void main(String[] args) throws AlignmentException, IOException, URISyntaxException, OWLOntologyCreationException {
		
		/*** 1. SELECT THE MATCHER TO BE RUN ***/
		final String MATCHER = "DEFINITIONS_EQ";

		/*** 2. SELECT THE TWO ONTOLOGIES TO BE MATCHED ***/
//		File ontoFile1 = new File("./files/_PHD_EVALUATION/BIBFRAME-SCHEMAORG/ONTOLOGIES/bibframe.rdf");
//		File ontoFile2 = new File("./files/_PHD_EVALUATION/BIBFRAME-SCHEMAORG/ONTOLOGIES/schema-org.owl");
		File ontoFile1 = new File("./files/_PHD_EVALUATION/ATMONTO-AIRM/ONTOLOGIES/ATMOntoCoreMerged.owl");
		File ontoFile2 = new File("./files/_PHD_EVALUATION/ATMONTO-AIRM/ONTOLOGIES/airm-mono.owl");
		
		/*** 3. SELECT THE NEO4J DATABASE FILE (FOR THE STRUCTURAL MATCHERS ONLY) ***/
		final File dbFile = new File("/Users/audunvennesland/Documents/phd/development/Neo4J_new/test");
		
		/*** INITIAL VALUES, NO NEED TO TOUCH THESE ***/
		double threshold;
		String alignmentFileName = null;
		File outputAlignment = null;
		String ontologyParameter1 = null;
		String ontologyParameter2 = null;
		PrintWriter writer = null;
		AlignmentVisitor renderer = null;
		Properties params = new Properties();
		AlignmentProcess a = null;
		
		/*** USED FOR INCLUDING THE ONTOLOGY FILE NAMES IN THE COMPUTED ALIGNMENT FILE ***/
		String onto1 = StringUtilities.stripPath(ontoFile1.toString());
		String onto2 = StringUtilities.stripPath(ontoFile2.toString());

		switch(MATCHER) {

		case "STRING":
			a = new StringEquivalenceMatcher();
			//a = new EditMatcher();
			//a = new TrigramMatcher();
			threshold = 0.8;

			a.init(ontoFile1.toURI(), ontoFile2.toURI());
			params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	
			
			System.err.println("The a alignment contains " + a.nbCells() + " correspondences");

			alignmentFileName = "./files/ESWC_ATMONTO_AIRM/ISUB/" + onto1 + "-" + onto2 + "-ISub08.rdf";

			outputAlignment = new File(alignmentFileName);

			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(outputAlignment)), true); 
			renderer = new RDFRendererVisitor(writer);

			BasicAlignment StringAlignment = (BasicAlignment)(a.clone());

			StringAlignment.cut(threshold);

			StringAlignment.render(renderer);
			
			System.err.println("The StringAlignment contains " + StringAlignment.nbCells() + " correspondences");
			writer.flush();
			writer.close();

			System.out.println("Matching completed!");
			break;
		
		case "DEFINITIONS_EQ":
			a = new DefinitionsEquivalenceMatcher();
			threshold = 0.2;

			a.init(ontoFile1.toURI(), ontoFile2.toURI());
			params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	
			
			System.err.println("The a alignment contains " + a.nbCells() + " correspondences");

			alignmentFileName = "./files/_PHD_EVALUATION/MATCHERTESTING/" + onto1 + "-" + onto2 + "-DEFINITIONS-EQ-"+threshold+".rdf";

			outputAlignment = new File(alignmentFileName);

			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(outputAlignment)), true); 
			renderer = new RDFRendererVisitor(writer);

			BasicAlignment DefinitionsEquivalenceAlignment = (BasicAlignment)(a.clone());
			
			

			DefinitionsEquivalenceAlignment.cut(threshold);

			DefinitionsEquivalenceAlignment.render(renderer);
			
			System.err.println("The DefinitionsEquivalenceAlignment contains " + DefinitionsEquivalenceAlignment.nbCells() + " correspondences");
			writer.flush();
			writer.close();

			System.out.println("Matching completed!");
			break;
			
		case "PROPERTYMATCHER":
			a = new PropertyEquivalenceMatcher();
			threshold = 0.2;

			a.init(ontoFile1.toURI(), ontoFile2.toURI());
			params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	
			
			System.err.println("The a alignment contains " + a.nbCells() + " correspondences");

			alignmentFileName = "./files/_PHD_EVALUATION/MATCHERTESTING/" + onto1 + "-" + onto2 + "-PROPERTYMATCHER-EQ-"+threshold+".rdf";

			outputAlignment = new File(alignmentFileName);

			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(outputAlignment)), true); 
			renderer = new RDFRendererVisitor(writer);

			BasicAlignment PropertyMatcherAlignment = (BasicAlignment)(a.clone());
			
			

			PropertyMatcherAlignment.cut(threshold);

			PropertyMatcherAlignment.render(renderer);
			
			System.err.println("The PropertyMatcherAlignment contains " + PropertyMatcherAlignment.nbCells() + " correspondences");
			writer.flush();
			writer.close();

			System.out.println("Matching completed!");
			break;
			
		case "INSTANCE":
			
			a = new InstanceMatcher_remove();
			threshold = 0.1;

			a.init(ontoFile1.toURI(), ontoFile2.toURI());
			params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	

			//alignmentFileName = "./files/UoA/test/" + onto1 + "-" + onto2 + "/Instances.rdf";

			outputAlignment = new File(alignmentFileName);

			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(outputAlignment)), true); 
			renderer = new RDFRendererVisitor(writer);

			BasicAlignment InstanceAlignment = (BasicAlignment)(a.clone());

			InstanceAlignment.cut(threshold);

			InstanceAlignment.render(renderer);
			writer.flush();
			writer.close();

			System.out.println("Matching completed!");
			break;
			
			

		case "WORDNET":
			
			a = new WNRiWordNetDistance();
			threshold = 0.6;

			a.init(ontoFile1.toURI(), ontoFile2.toURI());
			params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	

			alignmentFileName = "./files/COMPOSE-ClassEq_WordNet.rdf";

			outputAlignment = new File(alignmentFileName);

			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(outputAlignment)), true); 
			renderer = new RDFRendererVisitor(writer);

			BasicAlignment wordNetAlignment = (BasicAlignment)(a.clone());

			wordNetAlignment.cut(threshold);

			wordNetAlignment.render(renderer);
			writer.flush();
			writer.close();

			System.out.println("Matching completed!");
			break;

		case "GRAPHALIGNMENT":

			GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(dbFile);
			registerShutdownHook(db);

			ontologyParameter1 = StringUtilities.stripPath(ontoFile1.toString());
			ontologyParameter2 = StringUtilities.stripPath(ontoFile2.toString());

			System.out.println("Passing " + ontologyParameter1 + " and " + ontologyParameter2 + " to the structural matcher");
			a = new SuperclassMatcher(ontologyParameter1,ontologyParameter2, db);
			threshold = 0.6;

			a.init(ontoFile1.toURI(), ontoFile2.toURI());
			params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	
			
			//alignmentFileName = "./files/OAEI2011/" + onto1 + "-" + onto2 + "/COMPOSE-ClassEq_Graph.rdf";		

			outputAlignment = new File(alignmentFileName);

			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(outputAlignment)), true); 
			renderer = new RDFRendererVisitor(writer);

			BasicAlignment structuralAlignment = (BasicAlignment)(a.clone());

			structuralAlignment.cut(threshold);

			structuralAlignment.render(renderer);
			
			writer.flush();
			writer.close();

			System.out.println("Matching completed!");
			break;
			
		/*case "SUBSUMPTION_SUBCLASS":

			db = new GraphDatabaseFactory().newEmbeddedDatabase(dbFile);
			registerShutdownHook(db);

			ontologyParameter1 = StringUtils.stripPath(ontoFile1.toString());
			ontologyParameter2 = StringUtils.stripPath(ontoFile2.toString());
			System.out.println("Passing " + ontologyParameter1 + " and " + ontologyParameter2 + " to Subsumption_SubClass_Alignment.java");
			
			//create new graphs
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			OWLOntology o1 = manager.loadOntologyFromOntologyDocument(ontoFile1);
			OWLOntology o2 = manager.loadOntologyFromOntologyDocument(ontoFile2);
			Label labelO1 = DynamicLabel.label( ontologyParameter1 );
			Label labelO2 = DynamicLabel.label( ontologyParameter2 );
			
			GraphCreator creator = new GraphCreator(db);
			creator.createOntologyGraph(o1, labelO1);
			creator.createOntologyGraph(o2, labelO2);
			
			//perform the matching
			a = new ParentMatcher(ontologyParameter1,ontologyParameter2, db);
			threshold = 0.6;
			a.init(ontoFile1.toURI(), ontoFile2.toURI());
			params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	
		
			alignmentFileName = "./files/PathMatcher/TestPathMatcher.rdf";	

			outputAlignment = new File(alignmentFileName);

			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(outputAlignment)), true); 
			renderer = new RDFRendererVisitor(writer);

			BasicAlignment subsumptionSubClassAlignment = (BasicAlignment)(a.clone());

			subsumptionSubClassAlignment.cut(threshold);

			subsumptionSubClassAlignment.render(renderer);
			writer.flush();
			writer.close();

			System.out.println("Matching completed!");
			break;
			*/
	
			
		case "SUBSUMPTION_PATH":

			db = new GraphDatabaseFactory().newEmbeddedDatabase(dbFile);
			registerShutdownHook(db);

			ontologyParameter1 = StringUtilities.stripPath(ontoFile1.toString());
			ontologyParameter2 = StringUtilities.stripPath(ontoFile2.toString());
			System.out.println("Passing " + ontologyParameter1 + " and " + ontologyParameter2 + " to Subsumption_SubClass_Alignment.java");
			
			//create new graphs
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			OWLOntology o1 = manager.loadOntologyFromOntologyDocument(ontoFile1);
			OWLOntology o2 = manager.loadOntologyFromOntologyDocument(ontoFile2);
			Label labelO1 = DynamicLabel.label( ontologyParameter1 );
			Label labelO2 = DynamicLabel.label( ontologyParameter2 );
			
			Graph creator = new Graph(db);
			creator.createOntologyGraph(o1, labelO1);
			creator.createOntologyGraph(o2, labelO2);
			
			//perform the matching
			a = new AncestorSubsumptionMatcher(ontologyParameter1,ontologyParameter2, db);
			threshold = 0.6;
			a.init(ontoFile1.toURI(), ontoFile2.toURI());
			params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	
		
			alignmentFileName = "./files/Path/PathMatcher-biblio2bibo.rdf";	

			outputAlignment = new File(alignmentFileName);

			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(outputAlignment)), true); 
			renderer = new RDFRendererVisitor(writer);

			BasicAlignment subsumptionPathAlignment = (BasicAlignment)(a.clone());

			subsumptionPathAlignment.cut(threshold);

			subsumptionPathAlignment.render(renderer);
			writer.flush();
			writer.close();

			System.out.println("Matching completed!");
			break;

		case "SUBSUMPTION_COMPOUND":
			
			a = new CompoundSubsumptionMatcher();
			threshold = 0.6;

			a.init(ontoFile1.toURI(), ontoFile2.toURI());
			params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	

			alignmentFileName = "./files/ESWC_ATMONTO_AIRM/subsumption/COMPOUND_SYN-Subsumption.rdf";		

			outputAlignment = new File(alignmentFileName);

			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(outputAlignment)), true); 
			renderer = new RDFRendererVisitor(writer);

			BasicAlignment subsumptionCompoundAlignment = (BasicAlignment)(a.clone());

			subsumptionCompoundAlignment.cut(threshold);

			subsumptionCompoundAlignment.render(renderer);
			writer.flush();
			writer.close();

			System.out.println("Matching completed!");
			break;
			
		case "MERONYM_MATCHER":
			
			a = new WNMeronymMatcher();
			threshold = 0.6;

			a.init(ontoFile1.toURI(), ontoFile2.toURI());
			params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	

			alignmentFileName = "./files/ESWC_ATMONTO_AIRM/meronymy/MERONYM_SIMPLE-Meronymy.rdf";		

			outputAlignment = new File(alignmentFileName);

			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(outputAlignment)), true); 
			renderer = new RDFRendererVisitor(writer);

			BasicAlignment wnMeronymAlignment = (BasicAlignment)(a.clone());

			wnMeronymAlignment.cut(threshold);

			wnMeronymAlignment.render(renderer);
			writer.flush();
			writer.close();

			System.out.println("Matching completed!");
			break;

		case "DEFINITIONS_SUB":
			a = new DefinitionsSubsumptionMatcher();
			threshold = 0.2;

			a.init(ontoFile1.toURI(), ontoFile2.toURI());
			params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	
			
			System.err.println("The a alignment contains " + a.nbCells() + " correspondences");

			alignmentFileName = "./files/_PHD_EVALUATION/MATCHERTESTING/" + onto1 + "-" + onto2 + "-DEFINITIONS-SUB-"+threshold+".rdf";

			outputAlignment = new File(alignmentFileName);

			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(outputAlignment)), true); 
			renderer = new RDFRendererVisitor(writer);

			BasicAlignment DefinitionsSubsumptionAlignment = (BasicAlignment)(a.clone());

			DefinitionsSubsumptionAlignment.cut(threshold);
			DefinitionsSubsumptionAlignment.render(renderer);
			
			System.err.println("The DefinitionsEquivalenceAlignment contains " + DefinitionsSubsumptionAlignment.nbCells() + " correspondences");
			writer.flush();
			writer.close();

			System.out.println("Matching completed!");
			break;

		case "SUBSUMPTION_WORDNET":
			
			a = new LexicalSubsumptionMatcher();
			threshold = 0.6;

			a.init(ontoFile1.toURI(), ontoFile2.toURI());
			params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	

			//alignmentFileName = "./files/OAEI-16-conference/alignments/" + onto1 + "-" + onto2 + "/Subsumption_WordNet.rdf";	

			outputAlignment = new File(alignmentFileName);

			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(outputAlignment)), true); 
			renderer = new RDFRendererVisitor(writer);

			BasicAlignment subsumptionWordNetAlignment = (BasicAlignment)(a.clone());

			subsumptionWordNetAlignment.cut(threshold);

			subsumptionWordNetAlignment.render(renderer);
			writer.flush();
			writer.close();

			System.out.println("Matching completed!");
			break;
			
		}

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


}
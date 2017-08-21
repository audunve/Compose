package no.ntnu.idi.compose.ui;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
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
import org.semanticweb.owl.align.Evaluator;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import compose.graph.GraphCreator;
import compose.matchers.CompoundMatcher;
import compose.matchers.ISubMatcher;
import compose.matchers.InstanceMatcher;
import compose.matchers.PropEq_String_Matcher;
import compose.matchers.PropEq_WordNet_Matcher;
import compose.matchers.Subsumption_WordNet_Matcher;
import compose.matchers.WordNetMatcher;
import compose.matchers.SubclassMatcher;
import compose.matchers.AncestorMatcher;
import compose.matchers.ParentMatcher;
import compose.matchers.Subsumption_Structural_Matcher;
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.eval.PRecEvaluator;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import compose.misc.StringUtils;


public class TestMatcher {
	
	static Logger logger = LoggerFactory.getLogger(AncestorMatcher.class);

	public static void main(String[] args) throws AlignmentException, IOException, URISyntaxException, OWLOntologyCreationException {
		
		logger.info("Hello from TestMatcher");

		/*** 1. SELECT THE MATCHER TO BE RUN ***/
		final String MATCHER = "SUBSUMPTION_PATH";

		/*** 2. SELECT THE TWO ONTOLOGIES TO BE MATCHED ***/
		File ontoFile1 = new File("./files/OAEI-16-conference/ontologies/Biblio_2015.rdf");
		File ontoFile2 = new File("./files/OAEI-16-conference/ontologies/BIBO.owl");
//		File ontoFile1 = new File("./files/PathMatcher/PathMatcher1.owl");
//		File ontoFile2 = new File("./files/PathMatcher/PathMatcher2.owl");
		
		/*** 3. SELECT THE NEO4J DATABASE FILE (FOR THE STRUCTURAL MATCHERS ONLY) ***/
		final File dbFile = new File("/Users/audunvennesland/Documents/PhD/Development/Neo4J/biblio-bibo2");
		

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
		String onto1 = StringUtils.stripPath(ontoFile1.toString());
		String onto2 = StringUtils.stripPath(ontoFile2.toString());

		switch(MATCHER) {

		case "STRING":
			a = new ISubMatcher();
			threshold = 0.1;

			a.init(ontoFile1.toURI(), ontoFile2.toURI());
			params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	

			alignmentFileName = "./files/Path/String.rdf";

			outputAlignment = new File(alignmentFileName);

			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(outputAlignment)), true); 
			renderer = new RDFRendererVisitor(writer);

			BasicAlignment StringAlignment = (BasicAlignment)(a.clone());

			StringAlignment.cut(threshold);

			StringAlignment.render(renderer);
			writer.flush();
			writer.close();

			System.out.println("Matching completed!");
			break;
			
			
		case "INSTANCE":
			
			a = new InstanceMatcher();
			threshold = 0.1;

			a.init(ontoFile1.toURI(), ontoFile2.toURI());
			params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	

			alignmentFileName = "./files/UoA/test/" + onto1 + "-" + onto2 + "/Instances.rdf";

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
			
			a = new WordNetMatcher();
			threshold = 0.6;

			a.init(ontoFile1.toURI(), ontoFile2.toURI());
			params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	

			alignmentFileName = "./files/OAEI-16-conference/alignments/" + onto1 + "-" + onto2 + "/COMPOSE-ClassEq_WordNet.rdf";

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

			ontologyParameter1 = StringUtils.stripPath(ontoFile1.toString());
			ontologyParameter2 = StringUtils.stripPath(ontoFile2.toString());

			System.out.println("Passing " + ontologyParameter1 + " and " + ontologyParameter2 + " to the structural matcher");
			a = new SubclassMatcher(ontologyParameter1,ontologyParameter2, db);
			threshold = 0.6;

			a.init(ontoFile1.toURI(), ontoFile2.toURI());
			params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	
			
			alignmentFileName = "./files/OAEI2011/" + onto1 + "-" + onto2 + "/COMPOSE-ClassEq_Graph.rdf";		

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
			
		case "SUBSUMPTION_SUBCLASS":

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
			
	
			
		case "SUBSUMPTION_PATH":

			db = new GraphDatabaseFactory().newEmbeddedDatabase(dbFile);
			registerShutdownHook(db);

			ontologyParameter1 = StringUtils.stripPath(ontoFile1.toString());
			ontologyParameter2 = StringUtils.stripPath(ontoFile2.toString());
			System.out.println("Passing " + ontologyParameter1 + " and " + ontologyParameter2 + " to Subsumption_Path_Alignment.java");
			

			//create new graphs
			manager = OWLManager.createOWLOntologyManager();
			o1 = manager.loadOntologyFromOntologyDocument(ontoFile1);
			o2 = manager.loadOntologyFromOntologyDocument(ontoFile2);
			labelO1 = DynamicLabel.label( ontologyParameter1 );
			labelO2 = DynamicLabel.label( ontologyParameter2 );
			
			creator = new GraphCreator(db);
			creator.createOntologyGraph(o1, labelO1);
			creator.createOntologyGraph(o2, labelO2);
			
			//perform the matching
			a = new AncestorMatcher(ontologyParameter1,ontologyParameter2, db);
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
			
			a = new CompoundMatcher();
			threshold = 0.6;

			a.init(ontoFile1.toURI(), ontoFile2.toURI());
			params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	

			alignmentFileName = "./files/OAEI-16-conference/alignments/" + onto1 + "-" + onto2 + "/COMPOSE-Subsumption_String.rdf";		

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

		

		case "SUBSUMPTION_WORDNET":
			
			a = new Subsumption_WordNet_Matcher();
			threshold = 0.6;

			a.init(ontoFile1.toURI(), ontoFile2.toURI());
			params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	

			alignmentFileName = "./files/OAEI-16-conference/alignments/" + onto1 + "-" + onto2 + "/Subsumption_WordNet.rdf";	

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
			

		case "PROPERTY_WORDNET":
			
			a = new PropEq_WordNet_Matcher();
			threshold = 0.6;

			a.init(ontoFile1.toURI(), ontoFile2.toURI());
			params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	

			alignmentFileName = "./files/ntnu-lyon-paper/alignments/km4c-otn/" + onto1 + "-" + onto2 + "/PropEq_WordNet2.rdf";

			outputAlignment = new File(alignmentFileName);

			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(outputAlignment)), true); 
			renderer = new RDFRendererVisitor(writer);

			BasicAlignment PropertyWordNet2Alignment = (BasicAlignment)(a.clone());

			PropertyWordNet2Alignment.cut(threshold);

			PropertyWordNet2Alignment.render(renderer);
			writer.flush();
			writer.close();

			System.out.println("Matching completed!");
			break;
			
		case "PROPERTY_STRING":
			
			a = new PropEq_String_Matcher();
			threshold = 0.8;

			a.init(ontoFile1.toURI(), ontoFile2.toURI());
			params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	

			alignmentFileName = "./files/ntnu-lyon-paper/alignments/km4c-otn/" + onto1 + "-" + onto2 + "/PropEq_String.rdf";

			outputAlignment = new File(alignmentFileName);

			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(outputAlignment)), true); 
			renderer = new RDFRendererVisitor(writer);

			BasicAlignment StringPropertyAlignment = (BasicAlignment)(a.clone());

			StringPropertyAlignment.cut(threshold);

			StringPropertyAlignment.render(renderer);
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
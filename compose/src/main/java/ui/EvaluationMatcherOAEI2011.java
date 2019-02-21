package ui;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
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

import backup.ParentMatcher;
import equivalencematching.StringEquivalenceMatcher;
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.eval.PRecEvaluator;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import graph.Graph;
import subsumptionmatching.AncestorMatcher;
import subsumptionmatching.CompoundMatcher;
import subsumptionmatching.OppositeSubclassMatcher;
import subsumptionmatching.LexicalSubsumptionMatcher_oldest;
import utilities.StringUtilities;


public class EvaluationMatcherOAEI2011 {

	public static void main(String[] args) throws AlignmentException, IOException, URISyntaxException, OWLOntologyCreationException {

		final String MATCHER = "COMPOUND_MATCHER";
		final Map<String, Double> ontologyProfilingWeights = new HashMap<String,Double>();
		double threshold;
		File outputAlignment = null;
		final File ontologyDir = new File("./files/OAEI2009/allontologies");
		final File sourceFile = new File("./files/OAEI2009/101/101.rdf");

		File[] filesInDir = null;
		final String prefix = "file:";

		Properties params = new Properties();

		AlignmentProcess a = null;

		AlignmentParser parser = new AlignmentParser(0);
		
		PRecEvaluator eval = null;
		
		
		BasicAlignment evaluatedAlignment = null;
		Alignment referenceAlignment = null;

		Properties p = new Properties();
		String refFile = null;
		String alignmentFileName = null;
		PrintWriter writer = null;
		AlignmentVisitor renderer = null;
		
		String ontologyParameter1 = null;
		String ontologyParameter2 = null;
		
		Graph creator = null;
		OWLOntologyManager manager = null;
		OWLOntology o1 = null;
		OWLOntology o2 = null;
		Label labelO1 = null;
		Label labelO2 = null;


		switch(MATCHER) { 

		case "STRING_EQUIVALENCE_MATCHER":

			filesInDir = ontologyDir.listFiles();
			
			//get the appropriate weights from the ontologyProfilingWeights map
			double weight = ontologyProfilingWeights.get("CNE");

			for (int i = 0; i < filesInDir.length; i++) {
						a = new StringEquivalenceMatcher(weight);
						System.out.println("Matching " + sourceFile + " and " + filesInDir[i] );
						a.init( new URI(prefix.concat(sourceFile.toString().substring(2))), new URI(prefix.concat(filesInDir[i].toString().substring(2))));
						params = new Properties();
						params.setProperty("", "");
						a.align((Alignment)null, params);	

						alignmentFileName = "./files/OAEI2009/alignments/" + StringUtilities.stripOntologyName(sourceFile.toString()) + 
								"-" + StringUtilities.stripOntologyName(filesInDir[i].toString()) + "-Compound"+weight+".rdf";

						outputAlignment = new File(alignmentFileName);

						writer = new PrintWriter(
								new BufferedWriter(
										new FileWriter(outputAlignment)), true); 
						 renderer = new RDFRendererVisitor(writer);

						 evaluatedAlignment = (BasicAlignment)(a.clone());
						
						System.out.println("Before normalising evaluatedAlignment contains " + evaluatedAlignment.nbCells());
						evaluatedAlignment.normalise();
						System.out.println("After normalising evaluatedAlignment contains " + evaluatedAlignment.nbCells());

						evaluatedAlignment.cut(threshold);
						
						evaluatedAlignment.render(renderer);
						writer.flush();
						writer.close();
						
						//perform evaluation
						refFile = "file:files/OAEI2009/referencealignments/" + 101 + "-" + StringUtilities.stripOntologyName(filesInDir[i].toString()) + ".rdf";
						referenceAlignment = parser.parse(new URI(refFile));
						
						eval = new PRecEvaluator(referenceAlignment, evaluatedAlignment);

						eval.eval(p);

						System.out.println("------------------------------");
						System.out.println("Evaluator scores for " + StringUtilities.stripPath(filesInDir[i].toString()));
						System.out.println("------------------------------");
						System.out.println("F-measure: " + eval.getResults().getProperty("fmeasure").toString());
						System.out.println("Precision: " + eval.getResults().getProperty("precision").toString());
						System.out.println("Recall: " + eval.getResults().getProperty("recall").toString());

						System.out.println("True positives (TP): " + eval.getResults().getProperty("true positive").toString());

						int fp = eval.getFound() - eval.getCorrect();
						System.out.println("False positives (FP): " + fp);
						int fn = eval.getExpected() - eval.getCorrect();
						System.out.println("False negatives (FN): " + fn);
						System.out.println("\n");

			}
			System.out.println("Matching completed!");
			break;
		
		case "COMPOUND_MATCHER":

			filesInDir = ontologyDir.listFiles();

			for (int i = 0; i < filesInDir.length; i++) {
						a = new CompoundMatcher();
						threshold = 0.9;
						System.out.println("Matching " + sourceFile + " and " + filesInDir[i] );
						a.init( new URI(prefix.concat(sourceFile.toString().substring(2))), new URI(prefix.concat(filesInDir[i].toString().substring(2))));
						params = new Properties();
						params.setProperty("", "");
						a.align((Alignment)null, params);	

						alignmentFileName = "./files/OAEI2009/alignments/" + StringUtilities.stripOntologyName(sourceFile.toString()) + 
								"-" + StringUtilities.stripOntologyName(filesInDir[i].toString()) + "-Compound"+threshold+".rdf";

						outputAlignment = new File(alignmentFileName);

						writer = new PrintWriter(
								new BufferedWriter(
										new FileWriter(outputAlignment)), true); 
						 renderer = new RDFRendererVisitor(writer);

						 evaluatedAlignment = (BasicAlignment)(a.clone());
						
						System.out.println("Before normalising evaluatedAlignment contains " + evaluatedAlignment.nbCells());
						evaluatedAlignment.normalise();
						System.out.println("After normalising evaluatedAlignment contains " + evaluatedAlignment.nbCells());

						evaluatedAlignment.cut(threshold);
						
						evaluatedAlignment.render(renderer);
						writer.flush();
						writer.close();
						
						//perform evaluation
						refFile = "file:files/OAEI2009/referencealignments/" + 101 + "-" + StringUtilities.stripOntologyName(filesInDir[i].toString()) + ".rdf";
						referenceAlignment = parser.parse(new URI(refFile));
						
						eval = new PRecEvaluator(referenceAlignment, evaluatedAlignment);

						eval.eval(p);

						System.out.println("------------------------------");
						System.out.println("Evaluator scores for " + StringUtilities.stripPath(filesInDir[i].toString()));
						System.out.println("------------------------------");
						System.out.println("F-measure: " + eval.getResults().getProperty("fmeasure").toString());
						System.out.println("Precision: " + eval.getResults().getProperty("precision").toString());
						System.out.println("Recall: " + eval.getResults().getProperty("recall").toString());

						System.out.println("True positives (TP): " + eval.getResults().getProperty("true positive").toString());

						int fp = eval.getFound() - eval.getCorrect();
						System.out.println("False positives (FP): " + fp);
						int fn = eval.getExpected() - eval.getCorrect();
						System.out.println("False negatives (FN): " + fn);
						System.out.println("\n");

			}
			System.out.println("Matching completed!");
			break;
			
		case "ANCESTOR_MATCHER":

			filesInDir = ontologyDir.listFiles();

			for (int i = 0; i < filesInDir.length; i++) {
			
				//create a new instance of the neo4j database in each run
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			String dbName = String.valueOf(timestamp.getTime());

			File dbFile = new File("/Users/audunvennesland/Documents/PhD/Development/Neo4J/" + dbName);
				
			GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(dbFile);
			registerShutdownHook(db);
			
			String onto1 = StringUtilities.stripPath(sourceFile.toString());
			String onto2 = StringUtilities.stripPath(filesInDir[i].toString());

			ontologyParameter1 = StringUtilities.stripPath(sourceFile.toString());
			ontologyParameter2 = StringUtilities.stripPath(filesInDir[i].toString());
			System.out.println("Passing " + ontologyParameter1 + " and " + ontologyParameter2 + " to Subsumption_Path_Alignment.java");
			

			//create new graphs
			manager = OWLManager.createOWLOntologyManager();
			o1 = manager.loadOntologyFromOntologyDocument(sourceFile);
			o2 = manager.loadOntologyFromOntologyDocument(filesInDir[i]);
			labelO1 = DynamicLabel.label( ontologyParameter1 );
			labelO2 = DynamicLabel.label( ontologyParameter2 );
			
			creator = new Graph(db);
			creator.createOntologyGraph(o1, labelO1);
			creator.createOntologyGraph(o2, labelO2);
			
			//perform the matching
			a = new AncestorMatcher(ontologyParameter1,ontologyParameter2, db);
			threshold = 0.9;
			a.init(sourceFile.toURI(), filesInDir[i].toURI());
			params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	
		
			alignmentFileName = "./files/OAEI2009/alignments/" + StringUtilities.stripOntologyName(sourceFile.toString()) + 
					"-" + StringUtilities.stripOntologyName(filesInDir[i].toString()) + "-Ancestor"+threshold+".rdf";

			outputAlignment = new File(alignmentFileName);

			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(outputAlignment)), true); 
			renderer = new RDFRendererVisitor(writer);

			evaluatedAlignment = (BasicAlignment)(a.clone());
			
			System.out.println("Before normalising subsumptionPathAlignment contains " + evaluatedAlignment.nbCells());
			evaluatedAlignment.normalise();
			System.out.println("After normalising subsumptionPathAlignment contains " + evaluatedAlignment.nbCells());

			evaluatedAlignment.cut(threshold);

			evaluatedAlignment.render(renderer);
			writer.flush();
			writer.close();
			
			//perform evaluation
			refFile = "file:files/OAEI2009/referencealignments/" + 101 + "-" + StringUtilities.stripOntologyName(filesInDir[i].toString()) + ".rdf";
			referenceAlignment = parser.parse(new URI(refFile));
			
			eval = new PRecEvaluator(referenceAlignment, evaluatedAlignment);

			eval.eval(p);

			System.out.println("------------------------------");
			System.out.println("Evaluator scores for " + StringUtilities.stripPath(filesInDir[i].toString()));
			System.out.println("------------------------------");
			System.out.println("F-measure: " + eval.getResults().getProperty("fmeasure").toString());
			System.out.println("Precision: " + eval.getResults().getProperty("precision").toString());
			System.out.println("Recall: " + eval.getResults().getProperty("recall").toString());

			System.out.println("True positives (TP): " + eval.getResults().getProperty("true positive").toString());

			int fp = eval.getFound() - eval.getCorrect();
			System.out.println("False positives (FP): " + fp);
			int fn = eval.getExpected() - eval.getCorrect();
			System.out.println("False negatives (FN): " + fn);
			System.out.println("\n");
			
			}
			System.out.println("Matching completed!");
			break;
			
		case "PARENT_MATCHER":

			filesInDir = ontologyDir.listFiles();

			for (int i = 0; i < filesInDir.length; i++) {
			
				//create a new instance of the neo4j database in each run
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			String dbName = String.valueOf(timestamp.getTime());
			File dbFile = new File("/Users/audunvennesland/Documents/PhD/Development/Neo4J/" + dbName);				
			GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(dbFile);
			registerShutdownHook(db);
			
			String onto1 = StringUtilities.stripPath(sourceFile.toString());
			String onto2 = StringUtilities.stripPath(filesInDir[i].toString());

			ontologyParameter1 = StringUtilities.stripPath(sourceFile.toString());
			ontologyParameter2 = StringUtilities.stripPath(filesInDir[i].toString());
			System.out.println("Passing " + ontologyParameter1 + " and " + ontologyParameter2 + " to Subsumption_Path_Alignment.java");
			

			//create new graphs
			manager = OWLManager.createOWLOntologyManager();
			o1 = manager.loadOntologyFromOntologyDocument(sourceFile);
			o2 = manager.loadOntologyFromOntologyDocument(filesInDir[i]);
			labelO1 = DynamicLabel.label( ontologyParameter1 );
			labelO2 = DynamicLabel.label( ontologyParameter2 );
			
			creator = new Graph(db);
			creator.createOntologyGraph(o1, labelO1);
			creator.createOntologyGraph(o2, labelO2);
			
			//perform the matching
			a = new ParentMatcher(ontologyParameter1,ontologyParameter2, db);
			threshold = 0.9;
			a.init(sourceFile.toURI(), filesInDir[i].toURI());
			params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	
		
			alignmentFileName = "./files/OAEI2009/alignments/" + StringUtilities.stripOntologyName(sourceFile.toString()) + 
					"-" + StringUtilities.stripOntologyName(filesInDir[i].toString()) + "-Parent"+threshold+".rdf";

			outputAlignment = new File(alignmentFileName);

			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(outputAlignment)), true); 
			renderer = new RDFRendererVisitor(writer);

			evaluatedAlignment = (BasicAlignment)(a.clone());
			
			System.out.println("Before normalising subsumptionPathAlignment contains " + evaluatedAlignment.nbCells());
			evaluatedAlignment.normalise();
			System.out.println("After normalising subsumptionPathAlignment contains " + evaluatedAlignment.nbCells());

			evaluatedAlignment.cut(threshold);

			evaluatedAlignment.render(renderer);
			writer.flush();
			writer.close();
			
			//perform evaluation
			refFile = "file:files/OAEI2009/referencealignments/" + 101 + "-" + StringUtilities.stripOntologyName(filesInDir[i].toString()) + ".rdf";
			referenceAlignment = parser.parse(new URI(refFile));
			
			eval = new PRecEvaluator(referenceAlignment, evaluatedAlignment);

			eval.eval(p);

			System.out.println("------------------------------");
			System.out.println("Evaluator scores for " + StringUtilities.stripPath(filesInDir[i].toString()));
			System.out.println("------------------------------");
			System.out.println("F-measure: " + eval.getResults().getProperty("fmeasure").toString());
			System.out.println("Precision: " + eval.getResults().getProperty("precision").toString());
			System.out.println("Recall: " + eval.getResults().getProperty("recall").toString());

			System.out.println("True positives (TP): " + eval.getResults().getProperty("true positive").toString());

			int fp = eval.getFound() - eval.getCorrect();
			System.out.println("False positives (FP): " + fp);
			int fn = eval.getExpected() - eval.getCorrect();
			System.out.println("False negatives (FN): " + fn);
			System.out.println("\n");
			
			}
			System.out.println("Matching completed!");
			break;
			
		case "COMMON_OPPOSITE_SUBCLASS_MATCHER":

			filesInDir = ontologyDir.listFiles();

			for (int i = 0; i < filesInDir.length; i++) {
			
				//create a new instance of the neo4j database in each run
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			String dbName = String.valueOf(timestamp.getTime());

			File dbFile = new File("/Users/audunvennesland/Documents/PhD/Development/Neo4J/" + dbName);
				
			GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(dbFile);
			registerShutdownHook(db);
			
			String onto1 = StringUtilities.stripPath(sourceFile.toString());
			String onto2 = StringUtilities.stripPath(filesInDir[i].toString());

			ontologyParameter1 = StringUtilities.stripPath(sourceFile.toString());
			ontologyParameter2 = StringUtilities.stripPath(filesInDir[i].toString());
			System.out.println("Passing " + ontologyParameter1 + " and " + ontologyParameter2 + " to Subsumption_Path_Alignment.java");
			

			//create new graphs
			manager = OWLManager.createOWLOntologyManager();
			o1 = manager.loadOntologyFromOntologyDocument(sourceFile);
			o2 = manager.loadOntologyFromOntologyDocument(filesInDir[i]);
			labelO1 = DynamicLabel.label( ontologyParameter1 );
			labelO2 = DynamicLabel.label( ontologyParameter2 );
			
			creator = new Graph(db);
			creator.createOntologyGraph(o1, labelO1);
			creator.createOntologyGraph(o2, labelO2);
			
			//perform the matching
			a = new OppositeSubclassMatcher(ontologyParameter1,ontologyParameter2, db);
			threshold = 0.9;
			a.init(sourceFile.toURI(), filesInDir[i].toURI());
			params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	
		
			alignmentFileName = "./files/OAEI2009/alignments/" + StringUtilities.stripOntologyName(sourceFile.toString()) + 
					"-" + StringUtilities.stripOntologyName(filesInDir[i].toString()) + "-CommonOppositeSubclass"+threshold+".rdf";

			outputAlignment = new File(alignmentFileName);

			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(outputAlignment)), true); 
			renderer = new RDFRendererVisitor(writer);

			evaluatedAlignment = (BasicAlignment)(a.clone());
			
			System.out.println("Before normalising subsumptionPathAlignment contains " + evaluatedAlignment.nbCells());
			evaluatedAlignment.normalise();
			System.out.println("After normalising subsumptionPathAlignment contains " + evaluatedAlignment.nbCells());

			evaluatedAlignment.cut(threshold);

			evaluatedAlignment.render(renderer);
			writer.flush();
			writer.close();
			
			//perform evaluation
			refFile = "file:files/OAEI2009/referencealignments/" + 101 + "-" + StringUtilities.stripOntologyName(filesInDir[i].toString()) + ".rdf";
			referenceAlignment = parser.parse(new URI(refFile));
			
			eval = new PRecEvaluator(referenceAlignment, evaluatedAlignment);

			eval.eval(p);

			System.out.println("------------------------------");
			System.out.println("Evaluator scores for " + StringUtilities.stripPath(filesInDir[i].toString()));
			System.out.println("------------------------------");
			System.out.println("F-measure: " + eval.getResults().getProperty("fmeasure").toString());
			System.out.println("Precision: " + eval.getResults().getProperty("precision").toString());
			System.out.println("Recall: " + eval.getResults().getProperty("recall").toString());

			System.out.println("True positives (TP): " + eval.getResults().getProperty("true positive").toString());

			int fp = eval.getFound() - eval.getCorrect();
			System.out.println("False positives (FP): " + fp);
			int fn = eval.getExpected() - eval.getCorrect();
			System.out.println("False negatives (FN): " + fn);
			System.out.println("\n");
			
			}
			System.out.println("Matching completed!");
			break;


			case "WORDNET_MATCHER":

			filesInDir = ontologyDir.listFiles();

			for (int i = 0; i < filesInDir.length; i++) {
				a = new LexicalSubsumptionMatcher_oldest();
						threshold = 0.9;
						System.out.println("Matching " + sourceFile + " and " + filesInDir[i] );
						a.init( new URI(prefix.concat(sourceFile.toString().substring(2))), new URI(prefix.concat(filesInDir[i].toString().substring(2))));
						params = new Properties();
						params.setProperty("", "");
						a.align((Alignment)null, params);	

						alignmentFileName = "./files/OAEI2009/alignments/" + StringUtilities.stripOntologyName(sourceFile.toString()) + 
								"-" + StringUtilities.stripOntologyName(filesInDir[i].toString()) + "-WordNet-"+threshold+".rdf";

						outputAlignment = new File(alignmentFileName);

						writer = new PrintWriter(
								new BufferedWriter(
										new FileWriter(outputAlignment)), true); 
						 renderer = new RDFRendererVisitor(writer);

						evaluatedAlignment = (BasicAlignment)(a.clone());
						
						System.out.println("Before normalising evaluatedAlignment contains " + evaluatedAlignment.nbCells());
						evaluatedAlignment.normalise();
						System.out.println("After normalising evaluatedAlignment contains " + evaluatedAlignment.nbCells());

						evaluatedAlignment.cut(threshold);
						
						evaluatedAlignment.render(renderer);
						writer.flush();
						writer.close();
						
						//perform evaluation
						refFile = "file:files/OAEI2009/referencealignments/" + 101 + "-" + StringUtilities.stripOntologyName(filesInDir[i].toString()) + ".rdf";
						referenceAlignment = parser.parse(new URI(refFile));
						
						eval = new PRecEvaluator(referenceAlignment, evaluatedAlignment);

						eval.eval(p);

						System.out.println("------------------------------");
						System.out.println("Evaluator scores for " + StringUtilities.stripPath(filesInDir[i].toString()));
						System.out.println("------------------------------");
						System.out.println("F-measure: " + eval.getResults().getProperty("fmeasure").toString());
						System.out.println("Precision: " + eval.getResults().getProperty("precision").toString());
						System.out.println("Recall: " + eval.getResults().getProperty("recall").toString());

						System.out.println("True positives (TP): " + eval.getResults().getProperty("true positive").toString());

						int fp = eval.getFound() - eval.getCorrect();
						System.out.println("False positives (FP): " + fp);
						int fn = eval.getExpected() - eval.getCorrect();
						System.out.println("False negatives (FN): " + fn);
						System.out.println("\n");

			}
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
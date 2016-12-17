package no.ntnu.idi.compose.ui;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
//Alignment API classes
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Evaluator;

import fr.inrialpes.exmo.align.cli.GroupEval;
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.eval.PRecEvaluator;
import no.ntnu.idi.compose.loading.OWLLoader;
import no.ntnu.idi.compose.matchers.ClassEq_Structural_Matcher;
import no.ntnu.idi.compose.matchers.ClassEq_String_Matcher;
import no.ntnu.idi.compose.matchers.Subsumption_String_Matcher;
import no.ntnu.idi.compose.matchers.Subsumption_Structural_Matcher;
import no.ntnu.idi.compose.matchers.Subsumption_WordNet_Matcher;
import no.ntnu.idi.compose.matchers.ClassEq_WordNet_Matcher;
import no.ntnu.idi.compose.preprocessing.Preprocessor;
import no.ntnu.idi.compose.profiling.OntologyProcessor;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.parser.AlignmentParser;


public class ESWC17Matcher {

	public static void main(String[] args) throws AlignmentException, IOException, URISyntaxException {

		final String MATCHER = "SUBSUMPTION_HYPONYMY";
		double threshold;
		File outputAlignment = null;
		final File ontologyDir = new File("./files/experiment_eswc17/ontologies");

		File[] filesInDir = null;
		final String prefix = "file:";

		Properties params = new Properties();

		AlignmentProcess a = null;

		AlignmentParser inputParser = new AlignmentParser(0);

		switch(MATCHER) { 

		
		case "ISUB":
			a = new ClassEq_String_Matcher();
			threshold = 0.8;
			
			filesInDir = ontologyDir.listFiles();

			//TO-DO: Fix so that the correspondences from all ontology files are collected into one alignment file
			for (int i = 0; i < filesInDir.length; i++) {
				for (int j = i+1; j < filesInDir.length; j++) {
					if (filesInDir[i].isFile() && filesInDir[j].isFile() && i!=j) {
						System.out.println("Matching " + filesInDir[i] + " and " + filesInDir[j] );
						a.init( new URI(prefix.concat(filesInDir[i].toString().substring(2))), new URI(prefix.concat(filesInDir[j].toString().substring(2))));
						params = new Properties();
						params.setProperty("", "");
						a.align((Alignment)null, params);	

						String alignmentFileName = "./files/experiment_eswc17/alignments/" + Preprocessor.stripOntologyName(filesInDir[i].toString()) + 
								"-" + Preprocessor.stripOntologyName(filesInDir[j].toString()) + "/ClassEq_String.rdf";

						outputAlignment = new File(alignmentFileName);

						PrintWriter writer = new PrintWriter(
								new BufferedWriter(
										new FileWriter(outputAlignment)), true); 
						AlignmentVisitor renderer = new RDFRendererVisitor(writer);

						BasicAlignment a2 = (BasicAlignment)(a.clone());

						a2.cut(threshold);
						
						a2.render(renderer);
						writer.flush();
						writer.close();
					}
				}
			}
			System.out.println("Matching completed!");
			break;


		case "WORDNET":
			a = new ClassEq_WordNet_Matcher();
			threshold = 0.8;
			filesInDir = ontologyDir.listFiles();

			for (int i = 0; i < filesInDir.length; i++) {
				for (int j = i+1; j < filesInDir.length; j++) {
					if (filesInDir[i].isFile() && filesInDir[j].isFile() && i!=j) {
						System.out.println("Matching " + filesInDir[i] + " and " + filesInDir[j] );
						a.init( new URI(prefix.concat(filesInDir[i].toString().substring(2))), new URI(prefix.concat(filesInDir[j].toString().substring(2))));
						params = new Properties();
						params.setProperty("", "");
						a.align((Alignment)null, params);	

						String alignmentFileName = "./files/experiment_eswc17/alignments/" + Preprocessor.stripOntologyName(filesInDir[i].toString()) + 
								"-" + Preprocessor.stripOntologyName(filesInDir[j].toString()) + "/ClassEq_WordNet.rdf";

						outputAlignment = new File(alignmentFileName);

						PrintWriter writer = new PrintWriter(
								new BufferedWriter(
										new FileWriter(outputAlignment)), true); 
						AlignmentVisitor renderer = new RDFRendererVisitor(writer);

						BasicAlignment a2 = (BasicAlignment)(a.clone());

						a2.cut(threshold);

						a2.render(renderer);
						writer.flush();
						writer.close();
					}
				}
			}

			System.out.println("Matching completed!");
			break;


		case "GRAPHALIGNMENT":

			File dbFile = new File("/Users/audunvennesland/Documents/PhD/Development/Neo4J/Experiment_eswc17");
			GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(dbFile);
			registerShutdownHook(db);
			filesInDir = ontologyDir.listFiles();

			for (int i = 0; i < filesInDir.length; i++) {
				for (int j = i+1; j < filesInDir.length; j++) {
					if (filesInDir[i].isFile() && filesInDir[j].isFile() && i!=j) {

						String ontologyParameter1 = Preprocessor.stripPath(filesInDir[i].toString());
						String ontologyParameter2 = Preprocessor.stripPath(filesInDir[j].toString());
						System.out.println("Passing " + ontologyParameter1 + " and " + ontologyParameter2 + " to ClassEq_Structural_Matcher.java");
						a = new ClassEq_Structural_Matcher(ontologyParameter1,ontologyParameter2, db);
						threshold = 0.8;

						System.out.println("Matching " + filesInDir[i] + " and " + filesInDir[j] );
						a.init( new URI(prefix.concat(filesInDir[i].toString().substring(2))), new URI(prefix.concat(filesInDir[j].toString().substring(2))));
						params = new Properties();
						params.setProperty("", "");
						a.align((Alignment)null, params);	

						String alignmentFileName = "./files/experiment_eswc17/alignments/" + Preprocessor.stripOntologyName(filesInDir[i].toString()) + 
								"-" + Preprocessor.stripOntologyName(filesInDir[j].toString()) + "/ClassEq_Graph-Sub.rdf";				
						outputAlignment = new File(alignmentFileName);

						PrintWriter writer = new PrintWriter(
								new BufferedWriter(
										new FileWriter(outputAlignment)), true); 
						AlignmentVisitor renderer = new RDFRendererVisitor(writer);

						BasicAlignment a2 = (BasicAlignment)(a.clone());

						a2.cut(threshold);

						a2.render(renderer);
						writer.flush();
						writer.close();
					}
				}
			}
			System.out.println("Matching completed!");
			break;
			
		/*case "ANNOTATIONS":
			a = new AnnotationsAlignment();

			filesInDir = ontologyDir.listFiles();

			for (int i = 0; i < filesInDir.length; i++) {
				for (int j = i+1; j < filesInDir.length; j++) {
					if (filesInDir[i].isFile() && filesInDir[j].isFile() && i!=j) {
						System.out.println("Matching " + filesInDir[i] + " and " + filesInDir[j] );
						a.init( new URI(prefix.concat(filesInDir[i].toString().substring(2))), new URI(prefix.concat(filesInDir[j].toString().substring(2))));
						params = new Properties();
						params.setProperty("", "");
						a.align((Alignment)null, params);	
						threshold = 0.8;

						String alignmentFileName = "./files/experiment_eswc17/alignments/" + Preprocessor.stripOntologyName(filesInDir[i].toString()) + 
								"-" + Preprocessor.stripOntologyName(filesInDir[j].toString()) + "/Annotations.rdf";				
						outputAlignment = new File(alignmentFileName);

						PrintWriter writer = new PrintWriter(
								new BufferedWriter(
										new FileWriter(outputAlignment)), true); 
						AlignmentVisitor renderer = new RDFRendererVisitor(writer);

						BasicAlignment a2 = (BasicAlignment)(a.clone());

						a2.cut(threshold);

						a2.render(renderer);
						writer.flush();
						writer.close();
					}
				}
			}

			System.out.println("Matching completed!");
			break;*/
			
		case "SUBSUMPTION_COMPOUND":
			a = new Subsumption_String_Matcher();
			threshold = 0.8;
			filesInDir = ontologyDir.listFiles();

			for (int i = 0; i < filesInDir.length; i++) {
				for (int j = i+1; j < filesInDir.length; j++) {
					if (filesInDir[i].isFile() && filesInDir[j].isFile() && i!=j) {
						System.out.println("Matching " + filesInDir[i] + " and " + filesInDir[j] );
						a.init( new URI(prefix.concat(filesInDir[i].toString().substring(2))), new URI(prefix.concat(filesInDir[j].toString().substring(2))));
						params = new Properties();
						params.setProperty("", "");
						a.align((Alignment)null, params);	

						String alignmentFileName = "./files/experiment_eswc17/alignments/" + Preprocessor.stripOntologyName(filesInDir[i].toString()) + 
								"-" + Preprocessor.stripOntologyName(filesInDir[j].toString()) + "/Subsumption_String_Matcher.rdf";				
						outputAlignment = new File(alignmentFileName);

						PrintWriter writer = new PrintWriter(
								new BufferedWriter(
										new FileWriter(outputAlignment)), true); 
						AlignmentVisitor renderer = new RDFRendererVisitor(writer);

						BasicAlignment a2 = (BasicAlignment)(a.clone());

						a2.cut(threshold);

						a2.render(renderer);
						writer.flush();
						writer.close();
					}
				}
			}
			System.out.println("Matching completed!");
			break;

		case "SUBSUMPTION_SUBCLASS":

			dbFile = new File("/Users/audunvennesland/Documents/PhD/Development/Neo4J/Experiment_eswc17");
			db = new GraphDatabaseFactory().newEmbeddedDatabase(dbFile);
			registerShutdownHook(db);
			filesInDir = ontologyDir.listFiles();

			for (int i = 0; i < filesInDir.length; i++) {
				for (int j = i+1; j < filesInDir.length; j++) {
					if (filesInDir[i].isFile() && filesInDir[j].isFile() && i!=j) {
						String ontologyParameter1 = Preprocessor.stripPath(filesInDir[i].toString());
						String ontologyParameter2 = Preprocessor.stripPath(filesInDir[j].toString());
						System.out.println("Passing " + ontologyParameter1 + " and " + ontologyParameter2 + " to Subsumption_SubClass_Alignment.java");
						a = new Subsumption_Structural_Matcher(ontologyParameter1,ontologyParameter2, db);
						threshold = 0.8;
						System.out.println("Matching " + filesInDir[i] + " and " + filesInDir[j] );
						a.init( new URI(prefix.concat(filesInDir[i].toString().substring(2))), new URI(prefix.concat(filesInDir[j].toString().substring(2))));
						params = new Properties();
						params.setProperty("", "");
						a.align((Alignment)null, params);	

						String alignmentFileName = "./files/experiment_eswc17/alignments/" + Preprocessor.stripOntologyName(filesInDir[i].toString()) + 
								"-" + Preprocessor.stripOntologyName(filesInDir[j].toString()) + "/Subsumption_SubClass.rdf";				
						outputAlignment = new File(alignmentFileName);

						PrintWriter writer = new PrintWriter(
								new BufferedWriter(
										new FileWriter(outputAlignment)), true); 
						AlignmentVisitor renderer = new RDFRendererVisitor(writer);

						BasicAlignment a2 = (BasicAlignment)(a.clone());

						a2.cut(threshold);

						a2.render(renderer);
						writer.flush();
						writer.close();
					}
				}
			}
			System.out.println("Matching completed!");
			break;
			
			
		case "SUBSUMPTION_HYPONYMY":
			a = new Subsumption_WordNet_Matcher();
			threshold = 0.9;
			filesInDir = ontologyDir.listFiles();

			for (int i = 0; i < filesInDir.length; i++) {
				for (int j = i+1; j < filesInDir.length; j++) {
					if (filesInDir[i].isFile() && filesInDir[j].isFile() && i!=j) {
						System.out.println("Matching " + filesInDir[i] + " and " + filesInDir[j] );
						a.init( new URI(prefix.concat(filesInDir[i].toString().substring(2))), new URI(prefix.concat(filesInDir[j].toString().substring(2))));
						params = new Properties();
						params.setProperty("", "");
						a.align((Alignment)null, params);	

						String alignmentFileName = "./files/experiment_eswc17/alignments/" + Preprocessor.stripOntologyName(filesInDir[i].toString()) + 
								"-" + Preprocessor.stripOntologyName(filesInDir[j].toString()) + "/Subsumption_WordNet_Matcher.rdf";

						outputAlignment = new File(alignmentFileName);

						PrintWriter writer = new PrintWriter(
								new BufferedWriter(
										new FileWriter(outputAlignment)), true); 
						AlignmentVisitor renderer = new RDFRendererVisitor(writer);

						BasicAlignment a2 = (BasicAlignment)(a.clone());

						a2.cut(threshold);

						a2.render(renderer);
						writer.flush();
						writer.close();
					}
				}
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


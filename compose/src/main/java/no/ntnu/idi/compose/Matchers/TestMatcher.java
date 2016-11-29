package no.ntnu.idi.compose.Matchers;


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
import no.ntnu.idi.compose.Loading.OWLLoader;
import no.ntnu.idi.compose.Matchers.CompoundAlignment;
import no.ntnu.idi.compose.Processing.OntologyProcessor;
import no.ntnu.idi.compose.misc.StringProcessor;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.parser.AlignmentParser;


public class TestMatcher {

	public static void main(String[] args) throws AlignmentException, IOException, URISyntaxException {


		//input parameters for each matching operation
		final String MATCHER = "ANNOTATIONS";
		final double THRESHOLD = 0.1;
		File outputAlignment = null;
		final File ontologyDir = new File("./files/OAEI-16-conference/ontologies");
		File[] filesInDir = null;
		final String prefix = "file:";


		//Parameters defining the (string) matching method to be applied
		Properties params = new Properties();

		AlignmentProcess a = null;

		AlignmentParser inputParser = new AlignmentParser(0);

		switch(MATCHER) {

	
		case "ISUB":
			a = new ISubAlignment();

			filesInDir = ontologyDir.listFiles();

			for (int i = 0; i < filesInDir.length; i++) {
				for (int j = i+1; j < filesInDir.length; j++) {
				if (filesInDir[i].isFile() && filesInDir[j].isFile() && i!=j) {
					System.out.println("Matching " + filesInDir[i] + " and " + filesInDir[j] );
					a.init( new URI(prefix.concat(filesInDir[i].toString().substring(2))), new URI(prefix.concat(filesInDir[j].toString().substring(2))));
					params = new Properties();
					params.setProperty("", "");
					a.align((Alignment)null, params);	

					//put output alignment in appropriate folder
					String alignmentFileName = "./files/OAEI-16-conference/alignments/" + StringProcessor.stripOntologyName(filesInDir[i].toString()) + 
							"-" + StringProcessor.stripOntologyName(filesInDir[j].toString()) + "/ISub.rdf";
					
					outputAlignment = new File(alignmentFileName);
					
					PrintWriter writer = new PrintWriter(
							new BufferedWriter(
									new FileWriter(outputAlignment)), true); 
					AlignmentVisitor renderer = new RDFRendererVisitor(writer);

					//to manipulate the alignments we are using the BasicAlignment, not the Alignment
					//clone the computed alignment from Alignment to BasicAlignment
					BasicAlignment a2 = (BasicAlignment)(a.clone());

					//implement the similarity threshold
					a2.cut(THRESHOLD);

					a2.render(renderer);
					writer.flush();
					writer.close();


				}
				}
			}
			
			System.out.println("Matching completed!");
			break;
			
		
		case "EDIT":
			a = new EditDistNameAlignment();

			filesInDir = ontologyDir.listFiles();

			for (int i = 0; i < filesInDir.length; i++) {
				for (int j = i+1; j < filesInDir.length; j++) {
				if (filesInDir[i].isFile() && filesInDir[j].isFile() && i!=j) {
					System.out.println("Matching " + filesInDir[i] + " and " + filesInDir[j] );
					a.init( new URI(prefix.concat(filesInDir[i].toString().substring(2))), new URI(prefix.concat(filesInDir[j].toString().substring(2))));
					params = new Properties();
					params.setProperty("", "");
					a.align((Alignment)null, params);	

					//put output alignment in appropriate folder
					String alignmentFileName = "./files/OAEI-16-conference/alignments/" + StringProcessor.stripOntologyName(filesInDir[i].toString()) + 
							"-" + StringProcessor.stripOntologyName(filesInDir[j].toString()) + "/Edit.rdf";
					
					outputAlignment = new File(alignmentFileName);
					
					PrintWriter writer = new PrintWriter(
							new BufferedWriter(
									new FileWriter(outputAlignment)), true); 
					AlignmentVisitor renderer = new RDFRendererVisitor(writer);

					//to manipulate the alignments we are using the BasicAlignment, not the Alignment
					//clone the computed alignment from Alignment to BasicAlignment
					BasicAlignment a2 = (BasicAlignment)(a.clone());

					//implement the similarity threshold
					a2.cut(THRESHOLD);

					a2.render(renderer);
					writer.flush();
					writer.close();


				}
				}
			}
			
			System.out.println("Matching completed!");
			break;
			
		
		case "WORDNET":
			a = new WS4JAlignment();

			filesInDir = ontologyDir.listFiles();

			for (int i = 0; i < filesInDir.length; i++) {
				for (int j = i+1; j < filesInDir.length; j++) {
				if (filesInDir[i].isFile() && filesInDir[j].isFile() && i!=j) {
					System.out.println("Matching " + filesInDir[i] + " and " + filesInDir[j] );
					a.init( new URI(prefix.concat(filesInDir[i].toString().substring(2))), new URI(prefix.concat(filesInDir[j].toString().substring(2))));
					params = new Properties();
					params.setProperty("", "");
					a.align((Alignment)null, params);	

					//put output alignment in appropriate folder
					String alignmentFileName = "./files/OAEI-16-conference/alignments/" + StringProcessor.stripOntologyName(filesInDir[i].toString()) + 
							"-" + StringProcessor.stripOntologyName(filesInDir[j].toString()) + "/WordNet.rdf";
					
					outputAlignment = new File(alignmentFileName);
					
					PrintWriter writer = new PrintWriter(
							new BufferedWriter(
									new FileWriter(outputAlignment)), true); 
					AlignmentVisitor renderer = new RDFRendererVisitor(writer);

					//to manipulate the alignments we are using the BasicAlignment, not the Alignment
					//clone the computed alignment from Alignment to BasicAlignment
					BasicAlignment a2 = (BasicAlignment)(a.clone());

					//implement the similarity threshold
					a2.cut(THRESHOLD);

					a2.render(renderer);
					writer.flush();
					writer.close();


				}
				}
			}
			
			System.out.println("Matching completed!");
			break;

		
		case "COMPOUND":
			a = new CompoundAlignment();

			filesInDir = ontologyDir.listFiles();

			for (int i = 0; i < filesInDir.length; i++) {
				for (int j = i+1; j < filesInDir.length; j++) {
				if (filesInDir[i].isFile() && filesInDir[j].isFile() && i!=j) {
					System.out.println("Matching " + filesInDir[i] + " and " + filesInDir[j] );
					a.init( new URI(prefix.concat(filesInDir[i].toString().substring(2))), new URI(prefix.concat(filesInDir[j].toString().substring(2))));
					params = new Properties();
					params.setProperty("", "");
					a.align((Alignment)null, params);	

					//put output alignment in appropriate folder
					String alignmentFileName = "./files/OAEI-16-conference/alignments/" + StringProcessor.stripOntologyName(filesInDir[i].toString()) + 
							"-" + StringProcessor.stripOntologyName(filesInDir[j].toString()) + "/Compound.rdf";				
					outputAlignment = new File(alignmentFileName);
					
					PrintWriter writer = new PrintWriter(
							new BufferedWriter(
									new FileWriter(outputAlignment)), true); 
					AlignmentVisitor renderer = new RDFRendererVisitor(writer);

					//to manipulate the alignments we are using the BasicAlignment, not the Alignment
					//clone the computed alignment from Alignment to BasicAlignment
					BasicAlignment a2 = (BasicAlignment)(a.clone());

					//implement the similarity threshold
					a2.cut(THRESHOLD);

					a2.render(renderer);
					writer.flush();
					writer.close();


				}
				}
			}
			
			System.out.println("Matching completed!");
			break;
			
			
		case "GRAPHALIGNMENT":

			File dbFile = new File("/Users/audunvennesland/Documents/PhD/Development/Neo4J/OAEI-Conference-16");
			GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(dbFile);
			registerShutdownHook(db);
			filesInDir = ontologyDir.listFiles();
			
			for (int i = 0; i < filesInDir.length; i++) {
				for (int j = i+1; j < filesInDir.length; j++) {
				if (filesInDir[i].isFile() && filesInDir[j].isFile() && i!=j) {
					
					//need to provide the ontology file names as parameters to GraphAlignment.java
					String ontologyParameter1 = StringProcessor.stripPath(filesInDir[i].toString());
					String ontologyParameter2 = StringProcessor.stripPath(filesInDir[j].toString());
					System.out.println("Passing " + ontologyParameter1 + " and " + ontologyParameter2 + " to GraphAlignment.java");
					a = new GraphAlignment(ontologyParameter1,ontologyParameter2, db);
					
					System.out.println("Matching " + filesInDir[i] + " and " + filesInDir[j] );
					a.init( new URI(prefix.concat(filesInDir[i].toString().substring(2))), new URI(prefix.concat(filesInDir[j].toString().substring(2))));
					params = new Properties();
					params.setProperty("", "");
					a.align((Alignment)null, params);	

					//put output alignment in appropriate folder
					String alignmentFileName = "./files/OAEI-16-conference/alignments/" + StringProcessor.stripOntologyName(filesInDir[i].toString()) + 
							"-" + StringProcessor.stripOntologyName(filesInDir[j].toString()) + "/Graph-Super.rdf";				
					outputAlignment = new File(alignmentFileName);
					
					PrintWriter writer = new PrintWriter(
							new BufferedWriter(
									new FileWriter(outputAlignment)), true); 
					AlignmentVisitor renderer = new RDFRendererVisitor(writer);

					//to manipulate the alignments we are using the BasicAlignment, not the Alignment
					//clone the computed alignment from Alignment to BasicAlignment
					BasicAlignment a2 = (BasicAlignment)(a.clone());

					//implement the similarity threshold
					a2.cut(THRESHOLD);

					a2.render(renderer);
					writer.flush();
					writer.close();
				}
				}
			}
			
			System.out.println("Matching completed!");
			break;

			
			
		case "ANNOTATIONS":
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

					//put output alignment in appropriate folder
					String alignmentFileName = "./files/OAEI-16-conference/alignments/" + StringProcessor.stripOntologyName(filesInDir[i].toString()) + 
							"-" + StringProcessor.stripOntologyName(filesInDir[j].toString()) + "/Annotations.rdf";				
					outputAlignment = new File(alignmentFileName);
					
					PrintWriter writer = new PrintWriter(
							new BufferedWriter(
									new FileWriter(outputAlignment)), true); 
					AlignmentVisitor renderer = new RDFRendererVisitor(writer);

					//to manipulate the alignments we are using the BasicAlignment, not the Alignment
					//clone the computed alignment from Alignment to BasicAlignment
					BasicAlignment a2 = (BasicAlignment)(a.clone());

					//implement the similarity threshold
					a2.cut(THRESHOLD);

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


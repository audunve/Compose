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

import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.eval.PRecEvaluator;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import no.ntnu.idi.compose.matchers.ClassEq_Structural_Matcher;
import no.ntnu.idi.compose.matchers.ClassEq_String_Matcher;
import no.ntnu.idi.compose.matchers.PropEq_WordNet_Matcher;
import no.ntnu.idi.compose.matchers.PropEq_String_Matcher;
import no.ntnu.idi.compose.matchers.Subsumption_String_Matcher;
import no.ntnu.idi.compose.matchers.Subsumption_Structural_Matcher;
import no.ntnu.idi.compose.matchers.Subsumption_WordNet_Matcher;
import no.ntnu.idi.compose.matchers.ClassEq_WordNet_Matcher;
import no.ntnu.idi.compose.preprocessing.Preprocessor;


public class TestMatcher {

	public static void main(String[] args) throws AlignmentException, IOException, URISyntaxException {

		double threshold;
		//final String MATCHER = "STRING";
		//final String MATCHER = "WORDNET";
		//final String MATCHER = "GRAPHALIGNMENT";
		//final String MATCHER = "SUBSUMPTION_COMPOUND";
		//final String MATCHER = "SUBSUMPTION_WORDNET";
		final String MATCHER = "SUBSUMPTION_SUBCLASS";
		
		
		String alignmentFileName = null;

		String onto1 = "301";
		String onto2 = "304";

		File ontoFile1 = new File("./files/OAEI2011/301-304/301.rdf");
		File ontoFile2 = new File("./files/OAEI2011/301-304/304.rdf");
		
		File outputAlignment = null;
		String ontologyParameter1 = null;
		String ontologyParameter2 = null;

		PrintWriter writer = null;
		AlignmentVisitor renderer = null;

		//Parameters defining the (string) matching method to be applied
		Properties params = new Properties();

		AlignmentProcess a = null;

		switch(MATCHER) {
		
		

		case "STRING":
			a = new ClassEq_String_Matcher();
			threshold = 0.6;

			a.init(ontoFile1.toURI(), ontoFile2.toURI());
			params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	

			alignmentFileName = "./files/OAEI2011/" + onto1 + "-" + onto2 + "/COMPOSE-ClassEq_String.rdf";

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
			
			

		case "WORDNET":
			a = new ClassEq_WordNet_Matcher();
			threshold = 0.6;

			a.init(ontoFile1.toURI(), ontoFile2.toURI());
			params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	

			alignmentFileName = "./files/OAEI2011/" + onto1 + "-" + onto2 + "/COMPOSE-ClassEq_WordNet.rdf";

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

			//File dbFile = new File("/Users/audunvennesland/Documents/PhD/Development/Neo4J/ntnu-lyon");
			File dbFile = new File("/Users/audunvennesland/Documents/PhD/Development/Neo4J/ER2017/303-304");
			//File dbFile = new File("/Users/audunvennesland/Documents/PhD/Development/Neo4J/ntnu-lyon-paper");
			GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(dbFile);
			registerShutdownHook(db);

			ontologyParameter1 = Preprocessor.stripPath(ontoFile1.toString());
			ontologyParameter2 = Preprocessor.stripPath(ontoFile2.toString());

			System.out.println("Passing " + ontologyParameter1 + " and " + ontologyParameter2 + " to the structural matcher");
			a = new ClassEq_Structural_Matcher(ontologyParameter1,ontologyParameter2, db);
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

			dbFile = new File("/Users/audunvennesland/Documents/PhD/Development/Neo4J/ER2017/301-304");
			db = new GraphDatabaseFactory().newEmbeddedDatabase(dbFile);
			registerShutdownHook(db);

			ontologyParameter1 = Preprocessor.stripPath(ontoFile1.toString());
			ontologyParameter2 = Preprocessor.stripPath(ontoFile2.toString());
			System.out.println("Passing " + ontologyParameter1 + " and " + ontologyParameter2 + " to Subsumption_SubClass_Alignment.java");
			a = new Subsumption_Structural_Matcher(ontologyParameter1,ontologyParameter2, db);
			threshold = 0.6;
			a.init(ontoFile1.toURI(), ontoFile2.toURI());
			params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	
		
			alignmentFileName = "./files/OAEI2011/" + onto1 + "-" + onto2 + "/COMPOSE-Subsumption_SubClass.rdf";	

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

		case "SUBSUMPTION_COMPOUND":
			a = new Subsumption_String_Matcher();
			threshold = 0.6;

			a.init(ontoFile1.toURI(), ontoFile2.toURI());
			params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	

			alignmentFileName = "./files/OAEI2011/" + onto1 + "-" + onto2 + "/COMPOSE-Subsumption_String.rdf";		

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

			alignmentFileName = "./files/OAEI2011/" + onto1 + "-" + onto2 + "/COMPOSE-Subsumption_WordNet.rdf";	

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
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

		//Threshold for similarity score for which correspondences should be considered
		double threshold;
		final String MATCHER = "PROPERTY_STRING";
		String alignmentFileName = null;
		String onto1 = "cmt";
		String onto2 = "confOf";
		File ontoFile1 = new File("./files/experiment_eswc17/ontologies/cmt.owl");
		File ontoFile2 = new File("./files/experiment_eswc17/ontologies/confOf.owl");
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
			threshold = 0.9;

			a.init(ontoFile1.toURI(), ontoFile2.toURI());
			params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	

			alignmentFileName = "./files/experiment_eswc17/alignments/" + onto1 + "-" + onto2 + "/ClassEq_String.rdf";

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
			threshold = 0.9;

			a.init(ontoFile1.toURI(), ontoFile2.toURI());
			params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	

			alignmentFileName = "./files/experiment_eswc17/alignments/" + onto1 + "-" + onto2 + "/ClassEq_WordNet1.rdf";

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

			File dbFile = new File("/Users/audunvennesland/Documents/PhD/Development/Neo4J/Experiment_eswc17");
			GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(dbFile);
			registerShutdownHook(db);


			ontologyParameter1 = Preprocessor.stripPath(ontoFile1.toString());
			ontologyParameter2 = Preprocessor.stripPath(ontoFile2.toString());

			System.out.println("Passing " + ontologyParameter1 + " and " + ontologyParameter2 + " to the structural matcher");
			a = new ClassEq_Structural_Matcher(ontologyParameter1,ontologyParameter2, db);
			threshold = 0.4;

			a.init(ontoFile1.toURI(), ontoFile2.toURI());
			params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	

			alignmentFileName = "./files/experiment_eswc17/alignments/" + onto1 + "-" + onto2 + "/Test.rdf";			

			outputAlignment = new File(alignmentFileName);

			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(outputAlignment)), true); 
			renderer = new RDFRendererVisitor(writer);

			BasicAlignment structuralAlignment = (BasicAlignment)(a.clone());

			structuralAlignment.cut(threshold);

			structuralAlignment.render(renderer);
			
			AlignmentParser aparser = new AlignmentParser(0);
			Alignment referenceAlignment = aparser.parse(new URI("file:files/referenceAlignments/OAEI_Biblio2BIBO_ReferenceAlignment_Class_EquivalenceOnly.rdf"));
			Properties p = new Properties();
			
			//ISub
			Evaluator iSubEvaluator = new PRecEvaluator(referenceAlignment, structuralAlignment);
			iSubEvaluator.eval(p);
			System.out.println("------------------------------");
			System.out.println("Evaluation scores for isub:");
			System.out.println("------------------------------");
			System.out.println("F-measure: " + iSubEvaluator.getResults().getProperty("fmeasure").toString());
			System.out.println("Precision: " + iSubEvaluator.getResults().getProperty("precision").toString());
			System.out.println("Recall: " + iSubEvaluator.getResults().getProperty("recall").toString());
			
			writer.flush();
			writer.close();

			System.out.println("Matching completed!");
			break;

		case "SUBSUMPTION_COMPOUND":
			a = new Subsumption_String_Matcher();
			threshold = 0.9;

			a.init(ontoFile1.toURI(), ontoFile2.toURI());
			params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	

			alignmentFileName = "./files/experiment_eswc17/alignments/" + onto1 + "-" + onto2 + "/Subsumption_String_Matcher.rdf";			

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

		case "SUBSUMPTION_SUBCLASS":

			dbFile = new File("/Users/audunvennesland/Documents/PhD/Development/Neo4J/Experiment_eswc17");
			db = new GraphDatabaseFactory().newEmbeddedDatabase(dbFile);
			registerShutdownHook(db);

			ontologyParameter1 = Preprocessor.stripPath(ontoFile1.toString());
			ontologyParameter2 = Preprocessor.stripPath(ontoFile2.toString());
			System.out.println("Passing " + ontologyParameter1 + " and " + ontologyParameter2 + " to Subsumption_SubClass_Alignment.java");
			a = new Subsumption_Structural_Matcher(ontologyParameter1,ontologyParameter2, db);
			threshold = 0.9;
			a.init(ontoFile1.toURI(), ontoFile2.toURI());
			params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	

			alignmentFileName = "./files/experiment_eswc17/alignments/" + onto1 + "-" + onto2 + "/Subsumption_SubClass.rdf";				

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

		case "SUBSUMPTION_WORDNET":
			a = new Subsumption_WordNet_Matcher();
			threshold = 0.9;

			a.init(ontoFile1.toURI(), ontoFile2.toURI());
			params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	

			alignmentFileName = "./files/experiment_eswc17/alignments/" + onto1 + "-" + onto2 + "/Subsumption_WordNet_Matcher.rdf";

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

			alignmentFileName = "./files/experiment_eswc17/alignments/" + onto1 + "-" + onto2 + "/PropEq_WordNet2.rdf";

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

			alignmentFileName = "./files/experiment_eswc17/alignments/" + onto1 + "-" + onto2 + "/PropEq_String.rdf";

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

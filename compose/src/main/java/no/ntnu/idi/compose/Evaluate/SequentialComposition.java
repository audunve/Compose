package no.ntnu.idi.compose.Evaluate;



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
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Evaluator;

import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.eval.PRecEvaluator;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import no.ntnu.idi.compose.Matchers.GraphAlignment;
import no.ntnu.idi.compose.Matchers.ISubAlignment;
import no.ntnu.idi.compose.Matchers.WS4JAlignment;
import no.ntnu.idi.compose.misc.StringProcessor;


/*

Matcher order
- Random order of the matchers
- Order decided upon the Ontology Profiling step

Matcher configuration
- Complete match: All ontology concepts for the two input ontologies are matched by each matcher m, but the correspondences 
  identified by the previous matcher is given some weight.
- Partial match: All correspondences in A1 (from m1) are transferred m2 for refinement, whereby m2 only processes these 
  correspondences.
- Partial match: Only the correspondences in A1 (from m1) above a certain threshold are transferred to m2, whereby m2 only 
  processes these correspondences.

 */

public class SequentialComposition {

	static double threshold;
	static File outputAlignment = null;


	static Properties params = new Properties();

	AlignmentParser inputParser = new AlignmentParser(0);

	SequentialComposition(){};

	public static Alignment produceStringMatcherAlignment (URI onto1, URI onto2, String dataset) throws AlignmentException, URISyntaxException, IOException {

		AlignmentProcess matcher = null;
		matcher = new ISubAlignment();
		threshold = 0.9;
		Alignment finalAlignment = null;

		matcher.init( onto1, onto2);
		params = new Properties();
		params.setProperty("", "");
		matcher.align((Alignment)null, params);	

		String alignmentFileName = "./files/experiment_eswc17/alignments/" + dataset + "/StringMatcherAlignment.rdf";
		outputAlignment = new File(alignmentFileName);

		PrintWriter writer = new PrintWriter(
				new BufferedWriter(
						new FileWriter(outputAlignment)), true); 
		AlignmentVisitor renderer = new RDFRendererVisitor(writer);

		finalAlignment = (BasicAlignment)(matcher.clone());
		finalAlignment.cut(threshold);		
		finalAlignment.render(renderer);
		writer.flush();
		writer.close();

		System.out.println("String-matching completed!");
		return finalAlignment;
	}

	public static Alignment produceWordNetMatcherAlignment (URI onto1, URI onto2, String dataset) throws AlignmentException, URISyntaxException, IOException {

		AlignmentProcess matcher = null;
		matcher = new WS4JAlignment();
		threshold = 0.8;

		Alignment finalAlignment = null;

		matcher.init(onto1, onto2);
		params = new Properties();
		params.setProperty("", "");
		matcher.align((Alignment)null, params);	

		String alignmentFileName = "./files/experiment_eswc17/alignments/" + dataset + "/WordNetMatcherAlignment.rdf";

		outputAlignment = new File(alignmentFileName);

		PrintWriter writer = new PrintWriter(
				new BufferedWriter(
						new FileWriter(outputAlignment)), true); 
		AlignmentVisitor renderer = new RDFRendererVisitor(writer);

		finalAlignment = (BasicAlignment)(matcher.clone());
		finalAlignment.cut(threshold);		
		finalAlignment.render(renderer);
		writer.flush();
		writer.close();

		System.out.println("WordNet matching completed!");
		return finalAlignment;
	}

	public static Alignment produceStructuralMatcherAlignment (URI onto1, URI onto2, String dataset) throws AlignmentException, URISyntaxException, IOException {

		File dbFile = new File("/Users/audunvennesland/Documents/PhD/Development/Neo4J/Experiment_eswc17");
		GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(dbFile);
		registerShutdownHook(db);

		AlignmentProcess matcher = null;

		Alignment finalAlignment = null;

		String ontologyParameter1 = StringProcessor.stripPath(onto1.toString());
		String ontologyParameter2 = StringProcessor.stripPath(onto2.toString());

		System.out.println("Passing " + ontologyParameter1 + " and " + ontologyParameter2 + " to GraphAlignment.java");
		matcher = new GraphAlignment(ontologyParameter1,ontologyParameter2, db);
		threshold = 0.8;

		matcher.init(onto1, onto2);
		params = new Properties();
		params.setProperty("", "");
		matcher.align((Alignment)null, params);	

		String alignmentFileName = "./files/experiment_eswc17/alignments/" + dataset + "/StructuralMatcherAlignment.rdf";

		outputAlignment = new File(alignmentFileName);

		PrintWriter writer = new PrintWriter(
				new BufferedWriter(
						new FileWriter(outputAlignment)), true); 
		AlignmentVisitor renderer = new RDFRendererVisitor(writer);

		finalAlignment = (BasicAlignment)(matcher.clone());
		finalAlignment.cut(threshold);		
		finalAlignment.render(renderer);
		writer.flush();
		writer.close();

		System.out.println("Structural matching completed!");
		return finalAlignment;
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

	public static void main(String[] args) throws AlignmentException, IOException {

		final String scenario = "sequential_complete";

		switch(scenario) {

		case "sequential_complete":

			break;

		case "sequential_partial_1":

			break;

		case "sequential_partial_2":

			break;





		}

	}

	/*AlignmentParser parser = null;
		parser = new AlignmentParser(0);

		final String permutation = "string_wn_structure_merged";

		switch(permutation) {

		case "string_wn_structure_merged":
			//importing the alignments
			Alignment string_alignment = parser.parse("file:files/experiment_eswc17/alignments/biblio-bibo/ISub.rdf");
			System.out.println("The string alignment contains " + string_alignment.nbCells() + " cells.");
			Alignment wordNet_alignment = parser.parse("file:files/experiment_eswc17/alignments/biblio-bibo/WordNet.rdf");
			System.out.println("The wordnet alignment contains " + wordNet_alignment.nbCells() + " cells.");
			Alignment structure_alignment = parser.parse("file:files/experiment_eswc17/alignments/biblio-bibo/WordNet.rdf");
			System.out.println("The structural alignment contains " + structure_alignment.nbCells() + " cells.");

			//using edit distance as baseline
			//Alignment baseline_alignment = parser.parse("file:files/experiment_eswc17/alignments/biblio-bibo/Edit.rdf");

			double threshold = 0.8;

			//create a clone of string
			BasicAlignment string_wordnet = (BasicAlignment)(string_alignment.clone());
			//ingesting the string and WordNet alignments
			string_wordnet.ingest(wordNet_alignment);
			//create a clone of string and wordnet
			BasicAlignment string_wordnet_structure = (BasicAlignment)(string_wordnet.clone());
			//ingesting the string, WordNet, structure alignments
			string_wordnet_structure.ingest(structure_alignment);

			//store the wordnet_string_structure
			File outputFile = new File("./files/experiment_eswc17/alignments/biblio-bibo/string-wordnet-structure.rdf");
			PrintWriter writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(outputFile)), true); 
			AlignmentVisitor renderer = new RDFRendererVisitor(writer);

			//clone the computed alignment from Alignment to BasicAlignment
			BasicAlignment finalAlignment = (BasicAlignment)(string_wordnet_structure.clone());

			System.out.println("The final alignment contains " + finalAlignment.nbCells() + " cells (before cut)");

			//implement a similarity threshold
			finalAlignment.cut(threshold);

			System.out.println("The final alignment contains " + finalAlignment.nbCells() + " cells (after cut)");

			finalAlignment.render(renderer);
			writer.flush();
			writer.close();

			AlignmentParser aparser = new AlignmentParser(0);
			Alignment referenceAlignment = aparser.parse(new File("./files/referenceAlignments/OAEI_Biblio2BIBO_ReferenceAlignment_Class_EquivalenceOnly.rdf").toURI());

			Properties p = new Properties();

			Evaluator evaluator = new PRecEvaluator(referenceAlignment, finalAlignment);
			evaluator.eval(p);
			System.out.println("------------------------------");
			System.out.println("Evaluation scores:");
			System.out.println("------------------------------");
			System.out.println("F-measure: " + evaluator.getResults().getProperty("fmeasure").toString());
			System.out.println("Precision: " + evaluator.getResults().getProperty("precision").toString());
			System.out.println("Recall: " + evaluator.getResults().getProperty("recall").toString());

		}


	}*/


}

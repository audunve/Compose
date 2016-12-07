package no.ntnu.idi.compose.Evaluate;



import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Evaluator;

import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.eval.PRecEvaluator;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.parser.AlignmentParser;


/*

- Complete match: All ontology concepts for the two input ontologies are matched by each matcher, and the alignment 
  from each matcher is merged with the alignments from the other matchers.
- The similarity thresholds for each alignment A are configured based on the results from the Ontology Profiling
- If a correspondence is homogenous, that is if m1 has Human-Person, while m2 has Human-Individual, and 
  m1 should be given weight based on the Ontology Profiling, then the correspondence Human-Person is maintained
  while Human-Individual is discarded.

  */

public class ParallelComposition {

	ParallelComposition(){};

	public static void main(String[] args) throws AlignmentException, IOException {

		AlignmentParser parser = null;
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


	}

	
}

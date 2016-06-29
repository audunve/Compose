package no.ntnu.idi.compose.Matchers.SemanticMatchers;



import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Properties;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Evaluator;

import fr.inrialpes.exmo.align.impl.eval.PRecEvaluator;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.parser.AlignmentParser;

public class TestWordNetMatcher {

	public static void main(String[] args) throws AlignmentException, IOException {

		WordNetMatcher matcher = new WordNetMatcher();

		//Threshold for similarity score for which correspondences should be considered
		double threshold = 0.2;
		

		File ontoFile1 = new File("/Users/audunvennesland/Documents/PhD/Development/Experiments/OEAIBIBLIO2BIBO/BIBO.owl");
		File ontoFile2 = new File("/Users/audunvennesland/Documents/PhD/Development/Experiments/OEAIBIBLIO2BIBO/Biblio_2015.rdf");

		URI onto1 = ontoFile1.toURI();
		URI onto2 = ontoFile2.toURI();

		Properties params = new Properties();

		long time = System.currentTimeMillis();
		matcher.init(onto1, onto2);
		matcher.align(null, params);
		time = System.currentTimeMillis() - time;
		System.out.println("The matching operation took " + time + " millisecond(s)");

		//Storing the alignment as RDF
		PrintWriter writer = new PrintWriter(
				new BufferedWriter(
						new FileWriter("/Users/audunvennesland/Documents/PhD/Development/Experiments/OEAIBIBLIO2BIBO/new_alignment.rdf")), true); 
		AlignmentVisitor renderer = new RDFRendererVisitor(writer);
		matcher.cut(threshold);
		matcher.render(renderer);
		writer.flush();
		writer.close();

		//Evaluate the alignment against a reference alignment
		AlignmentParser aparser = new AlignmentParser(0);
		Alignment referenceAlignment = aparser.parse(new File("/Users/audunvennesland/Documents/PhD/Development/Experiments/OEAIBIBLIO2BIBO/OAEI_Biblio2BIBO_ReferenceAlignment.rdf").toURI());
		Properties p = new Properties();

		Evaluator evaluator = new PRecEvaluator(referenceAlignment, matcher);
		evaluator.eval(p);
		System.out.println("------------------------------");
		System.out.println("Evaluation scores:");
		System.out.println("------------------------------");
		System.out.println("F-measure: " + evaluator.getResults().getProperty("fmeasure").toString());
		System.out.println("Precision: " + evaluator.getResults().getProperty("precision").toString());
		System.out.println("Recall: " + evaluator.getResults().getProperty("recall").toString());


	}

}
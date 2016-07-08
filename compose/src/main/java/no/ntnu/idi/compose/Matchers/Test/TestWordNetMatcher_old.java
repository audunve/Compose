package no.ntnu.idi.compose.Matchers.Test;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Properties;

//Alignment API classes
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Evaluator;

import fr.inrialpes.exmo.align.impl.eval.PRecEvaluator;
import fr.inrialpes.exmo.align.impl.method.ClassStructAlignment;
import fr.inrialpes.exmo.align.impl.method.EditDistNameAlignment;
import fr.inrialpes.exmo.align.impl.method.NameAndPropertyAlignment;
import fr.inrialpes.exmo.align.impl.method.StringDistAlignment;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.ling.JWNLAlignment;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.dictionary.Dictionary;


public class TestWordNetMatcher_old {

	public static void main(String[] args) throws AlignmentException, IOException, JWNLException {

		//Treshold for similarity score for which correspondences should be considered
		final double THRESHOLD = 0.8;
		
		JWNL.initialize(new FileInputStream("/Users/audunvennesland/git/Compose/compose/file_property.xml"));
        final Dictionary dictionary = Dictionary.getInstance();

		File ontoFile1 = new File("/Users/audunvennesland/Documents/PhD/Ontologies/OAEI/OAEI2015/Biblio/Biblio_2015.rdf");
		File ontoFile2 = new File("/Users/audunvennesland/Documents/PhD/BIBO.owl");

		URI onto1 = ontoFile1.toURI();
		URI onto2 = ontoFile2.toURI();
		
		//Parameters defining the (string) matching method to be applied
		Properties params = new Properties();

		AlignmentProcess a = new JWNLAlignment();
		params.setProperty( "wndict", "/Users/audunvennesland/Documents/PhD/Development/WordNet/WordNet-3.0/dict");
		params.setProperty("wnFunction", "basicSynonymySimilarity");
		a.init(onto1, onto2);
		a.align( (Alignment)null, params );

		//Storing the alignment as RDF
		PrintWriter writer = new PrintWriter(
				new BufferedWriter(
						new FileWriter("/Users/audunvennesland/Documents/PhD/Development/Experiments/OEAIBIBLIO2BIBO/new_alignment.rdf")), true); 
		AlignmentVisitor renderer = new RDFRendererVisitor(writer);

		//Defines the threshold for correspondences to be included
		a.cut(THRESHOLD);
		a.render(renderer);
		writer.flush();
		writer.close();

		//Evaluate the alignment against a reference alignment
		AlignmentParser aparser = new AlignmentParser(0);
		Alignment referenceAlignment = aparser.parse(new File("/Users/audunvennesland/Documents/PhD/Development/Experiments/OEAIBIBLIO2BIBO/OAEI_Biblio2BIBO_ReferenceAlignment.rdf").toURI());
		Properties p = new Properties();

		Evaluator evaluator = new PRecEvaluator(referenceAlignment, a);
		evaluator.eval(p);
		System.out.println("------------------------------");
		System.out.println("Evaluation scores:");
		System.out.println("------------------------------");
		System.out.println("F-measure: " + evaluator.getResults().getProperty("fmeasure").toString());
		System.out.println("Precision: " + evaluator.getResults().getProperty("precision").toString());
		System.out.println("Recall: " + evaluator.getResults().getProperty("recall").toString());


	}
}
package no.ntnu.idi.compose.Matchers;


import java.io.File;
//Java standard classes
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Properties;

//Alignment API classes
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.Evaluator;

import fr.inrialpes.exmo.align.impl.eval.PRecEvaluator;
import fr.inrialpes.exmo.align.impl.method.NameAndPropertyAlignment;
import fr.inrialpes.exmo.align.impl.method.StringDistAlignment;
import fr.inrialpes.exmo.align.parser.AlignmentParser;



public class TestMatcher {

	public static void main( String[] args ) throws AlignmentException, UnsupportedEncodingException {


		File ontoFile1 = new File("/Users/audunvennesland/Documents/PhD/Ontologies/OAEI/OAEI2015/Biblio/Biblio_2015.rdf");
		File ontoFile2 = new File("/Users/audunvennesland/Documents/PhD/BIBO.owl");

		URI onto1 = ontoFile1.toURI();
		URI onto2 = ontoFile2.toURI();

		Properties params = new Properties();

		// Aligning
		AlignmentProcess a1 = new StringDistAlignment();

		//Audun: Changed the parameters so that the smoa distance is used to compute alignments
		params.setProperty("stringFunction", "smoaDistance");
		a1.init ( onto1, onto2 );
		a1.align( (Alignment)null, params );

		//Audun: Changed the parameters so that the ngram distance is used to compute alignments
		AlignmentProcess a2 = new StringDistAlignment();
		a2.init(onto1, onto2);
		params = new Properties();
		params.setProperty("stringFunction", "ngramDistance");
		a2.align((Alignment)null, params);

		//Audun: Changed the parameters so that the Jaro Winkler is used to compute alignments
		AlignmentProcess a3 = new StringDistAlignment();
		a3.init(onto1, onto2);
		params = new Properties();
		params.setProperty("stringFunction", "jaroWinklerMeasure");
		a3.align((Alignment)null, params);

		//Audun: Name and Property Alignment
		AlignmentProcess a4 = new NameAndPropertyAlignment();
		a4.init(onto1, onto2);
		params = new Properties();
		params.setProperty("default", "default");
		a4.align((Alignment)null, params);

		/*	    // Outputing computed alignments
	    PrintWriter writer = new PrintWriter (
				  new BufferedWriter(
		                   new OutputStreamWriter( System.out, "UTF-8" )), true);
	    AlignmentVisitor renderer = new RDFRendererVisitor(writer);
	    a4.render(renderer);
	    writer.flush();
	    writer.close();*/

		//Evaluate the alignment against a reference alignment
		AlignmentParser aparser = new AlignmentParser(0);
		Alignment referenceAlignment = aparser.parse(new File("/Users/audunvennesland/Documents/PhD/Development/Experiments/OEAIBIBLIO2BIBO/OAEI_Biblio2BIBO_ReferenceAlignment.rdf").toURI());
		Properties p = new Properties();

		Evaluator evaluator = new PRecEvaluator(referenceAlignment, a4);
		evaluator.eval(p);

		System.out.println("F-measure: " + evaluator.getResults().getProperty("fmeasure").toString());
		System.out.println("Precision: " + evaluator.getResults().getProperty("precision").toString());
		System.out.println("Recall: " + evaluator.getResults().getProperty("recall").toString());


	}
}


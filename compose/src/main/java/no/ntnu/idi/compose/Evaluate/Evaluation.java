package no.ntnu.idi.compose.Evaluate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Evaluator;

import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.eval.PRecEvaluator;
import fr.inrialpes.exmo.align.parser.AlignmentParser;

/**
 * @author audunvennesland
 * 9. apr. 2017 
 */
public class Evaluation {
	
	public static void main(String[] args) throws AlignmentException, URISyntaxException {
		
		AlignmentParser aparser = new AlignmentParser(0);
		
		Alignment referenceAlignment = aparser.parse(new URI("file:files/experiment_eswc17/alignments/biblio-bibo/referencealignment/refAlign_subsumption.rdf"));
		Alignment evaluatedAlignment = aparser.parse(new URI("file:files/experiment_eswc17/alignments/biblio-bibo/web-intelligence-17-weightedSequentialCombination/structure-lexical-string.rdf"));
		Properties p = new Properties();
		
		PRecEvaluator eval = new PRecEvaluator(referenceAlignment, evaluatedAlignment);
		
		eval.eval(p);
		System.out.println("------------------------------");
		System.out.println("Evaluation scores:");
		System.out.println("------------------------------");
		System.out.println("F-measure: " + eval.getResults().getProperty("fmeasure").toString());
		System.out.println("Precision: " + eval.getResults().getProperty("precision").toString());
		System.out.println("Recall: " + eval.getResults().getProperty("recall").toString());
		System.out.println("------------------------------");
		System.out.println("True positives (TP): " + eval.getResults().getProperty("true positive").toString());

		
		int fp = eval.getFound() - eval.getCorrect();
		System.out.println("False positives (FP): " + fp);
		int fn = eval.getExpected() - eval.getCorrect();
		System.out.println("False negatives (FN): " + fn);
	}
	

}
                                                                                        
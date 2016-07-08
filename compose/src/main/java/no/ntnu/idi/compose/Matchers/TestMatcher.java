package no.ntnu.idi.compose.Matchers;


import java.io.BufferedWriter;
import java.io.File;
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
import fr.inrialpes.exmo.align.impl.method.CompoundAlignment;
import fr.inrialpes.exmo.align.impl.method.EditDistNameAlignment;
import fr.inrialpes.exmo.align.impl.method.NameAndPropertyAlignment;
import fr.inrialpes.exmo.align.impl.method.StringDistAlignment;
import fr.inrialpes.exmo.align.impl.method.WordNetAlignment;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.parser.AlignmentParser;



public class TestMatcher {

	public static void main(String[] args) throws AlignmentException, IOException {

		//Threshold for similarity score for which correspondences should be considered
		final double THRESHOLD = 0.2;
		final String MATCHER = "WORDNET";

		
		//File ontoFile1 = new File("/Users/audunvennesland/Documents/PhD/Ontologies/Music/MusicOntology/MusicOntology.owl");
		//File ontoFile2 = new File("/Users/audunvennesland/Documents/PhD/Ontologies/Music/MusicBrainz/MusicBrainz.owl");
		File ontoFile1 = new File("/Users/audunvennesland/Documents/PhD/Ontologies/OAEI/Conference-2016/conference/Conference.owl");
		File ontoFile2 = new File("/Users/audunvennesland/Documents/PhD/Ontologies/OAEI/Conference-2016/conference/Ekaw.owl");
		//File ontoFile1 = new File("/Users/audunvennesland/Documents/PhD/Ontologies/OAEI/OAEI2015/Biblio/Biblio_2015.rdf");
		//File ontoFile2 = new File("/Users/audunvennesland/Documents/PhD/BIBO.owl");

		URI onto1 = ontoFile1.toURI();
		URI onto2 = ontoFile2.toURI();
		//Parameters defining the (string) matching method to be applied
		Properties params = new Properties();

		AlignmentProcess a = null;

		switch(MATCHER) {

		//Smoa distance
		case "SMOA": 
			a = new StringDistAlignment();
			params.setProperty("stringFunction", "smoaDistance");
			a.init ( onto1, onto2 );
			a.align( (Alignment)null, params );
			break;

			//N-gram distance
		case "NGRAM":
			a = new StringDistAlignment();
			params.setProperty("stringFunction", "ngramDistance");
			a.init ( onto1, onto2 );
			a.align( (Alignment)null, params );
			break;

			//Jaro-Winkler 
		case "JARO-WINKLER":
			a = new StringDistAlignment();
			a.init(onto1, onto2);
			params = new Properties();
			params.setProperty("stringFunction", "jaroWinklerMeasure");
			a.align((Alignment)null, params);
			break;

			//Edit (Levenshtein) distance
		case "EDIT":
			a = new EditDistNameAlignment();
			a.init(onto1, onto2);
			params = new Properties();
			a.align((Alignment)null, params);
			break;

			//Name and property matcher
		case "NAMEPROP":
			a = new NameAndPropertyAlignment();
			a.init(onto1, onto2);
			params = new Properties();
			a.align((Alignment)null, params);

			//Name and property matcher
		case "CLASSSTRUCT":
			a = new ClassStructAlignment();
			a.init(onto1, onto2);
			params = new Properties();
			a.align((Alignment)null, params);

			//Hamming distance matcher
		case "HAMMING":
			a = new StringDistAlignment();
			a.init(onto1, onto2);
			params = new Properties();
			params.setProperty("stringFunction", "hammingDistance");
			a.align((Alignment)null, params);
			
		case "WORDNET":
			a = new WordNetAlignment();
			a.init(onto1, onto2);
			params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	
			
			//Substring distance matcher
		case "SUBSTRING":
			a = new StringDistAlignment();
			a.init(onto1, onto2);
			params = new Properties();
			params.setProperty("stringFunction", "subStringDistance");
			a.align((Alignment)null, params);
				
		/*case "COMPOUND":
			a = new CompoundAlignment();
			a.init(onto1, onto2);
			params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	*/
			


		}

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
		//Alignment referenceAlignment = aparser.parse(new File("/Users/audunvennesland/Documents/PhD/Development/Experiments/OEAIBIBLIO2BIBO/OAEI_Biblio2BIBO_ReferenceAlignment.rdf").toURI());
		Alignment referenceAlignment = aparser.parse(new File("/Users/audunvennesland/Documents/PhD/Ontologies/OAEI/Conference-2016/reference-alignment/conference-ekaw.rdf").toURI());
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


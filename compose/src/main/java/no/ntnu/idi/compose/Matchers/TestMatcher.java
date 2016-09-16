package no.ntnu.idi.compose.Matchers;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

//Alignment API classes
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Evaluator;

import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.eval.PRecEvaluator;
import fr.inrialpes.exmo.align.impl.method.NameAndPropertyAlignment;
import no.ntnu.idi.compose.Matchers.CompoundAlignment;
import no.ntnu.idi.compose.Matchers.EditDistNameAlignment;
import no.ntnu.idi.compose.Matchers.StringDistAlignment;
import no.ntnu.idi.compose.Matchers.WordNetAlignment;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.parser.AlignmentParser;


public class TestMatcher {

	public static void main(String[] args) throws AlignmentException, IOException, URISyntaxException {

		//Threshold for similarity score for which correspondences should be considered
		final double THRESHOLD = 0.4;
		final String MATCHER = "STRUCTURALALIGNMENT";


		//Parameters defining the (string) matching method to be applied
		Properties params = new Properties();

		AlignmentProcess a = null;

		AlignmentParser inputParser = new AlignmentParser(0);

		//An optional input alignment
		//Alignment inputAlignment = inputParser.parse(new File("/Users/audunvennesland/Documents/PhD/Development/Experiments/OAEIBIBLIO2BIBO/output_alignment_biblio-bibo_edit.rdf").toURI());

		switch(MATCHER) {

		//Smoa distance
		case "SMOA": 
			a = new StringDistAlignment();
			params.setProperty("stringFunction", "smoaDistance");
	    	a.init( new URI("file:examples/rdf/Biblio_2015.rdf"), new URI("file:examples/rdf/BIBO.owl"));
			a.align((Alignment)null, params );
			break;

/*			//Smoa distance with input alignment
		case "SMOA_WITH_ALIGNMENT": 
			a = new StringDistAlignment();
			params.setProperty("stringFunction", "smoaDistance");
			a.init ( onto1, onto2 );
			a.align(inputAlignment, params );
			break;*/


			//N-gram distance
		case "NGRAM":
			a = new StringDistAlignment();
			params.setProperty("stringFunction", "ngramDistance");
	    	a.init( new URI("file:examples/rdf/Biblio_2015.rdf"), new URI("file:examples/rdf/BIBO.owl"));
			a.align( (Alignment)null, params );
			break;

/*			//N-gram distance with input alignment
		case "NGRAM_WITH_ALIGNMENT":
			a = new StringDistAlignment();
			params.setProperty("stringFunction", "ngramDistance");
			a.init ( onto1, onto2 );
			a.align( inputAlignment, params );
			break;*/

			//Jaro-Winkler 
		case "JARO-WINKLER":
			a = new StringDistAlignment();
	    	a.init( new URI("file:examples/rdf/Biblio_2015.rdf"), new URI("file:examples/rdf/BIBO.owl"));
			params = new Properties();
			params.setProperty("stringFunction", "jaroWinklerMeasure");
			a.align((Alignment)null, params);
			break;

			//Edit (Levenshtein) distance
		case "EDIT":
			a = new EditDistNameAlignment();
	    	a.init( new URI("file:examples/rdf/Biblio_2015.rdf"), new URI("file:examples/rdf/BIBO.owl"));
			params = new Properties();
			a.align((Alignment)null, params);
			break;

			//Hamming distance matcher
		case "HAMMING":
			a = new StringDistAlignment();
	    	a.init( new URI("file:examples/rdf/Biblio_2015.rdf"), new URI("file:examples/rdf/BIBO.owl"));
			params = new Properties();
			params.setProperty("stringFunction", "hammingDistance");
			a.align((Alignment)null, params);
			break;

			//Substring distance matcher
		case "SUBSTRING":
			a = new StringDistAlignment();
	    	a.init( new URI("file:examples/rdf/Biblio_2015.rdf"), new URI("file:examples/rdf/BIBO.owl"));
			params = new Properties();
			params.setProperty("stringFunction", "subStringDistance");
			a.align((Alignment)null, params);
			break;
			
		case "ISUB":
			a = new ISubAlignment();
	    	a.init( new URI("file:examples/rdf/Biblio_2015.rdf"), new URI("file:examples/rdf/BIBO.owl"));
			params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	
			break;

		case "WORDNET":
			a = new WordNetAlignment();
	    	a.init( new URI("file:examples/rdf/Biblio_2015.rdf"), new URI("file:examples/rdf/BIBO.owl"));
			params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	
			break;
			
		case "WUPALMER":
			a = new WS4JAlignment();
	    	a.init( new URI("file:examples/rdf/Biblio_2015.rdf"), new URI("file:examples/rdf/BIBO.owl"));
			params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	
			break;

		case "COMPOUND":
			a = new CompoundAlignment();
	    	a.init( new URI("file:examples/rdf/Biblio_2015.rdf"), new URI("file:examples/rdf/BIBO.owl"));
			params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	
			break;
			
		case "NAME_AND_PROPERTY":
			a = new NameAndPropertyAlignment();
	    	a.init( new URI("file:examples/rdf/Biblio_2015.rdf"), new URI("file:examples/rdf/BIBO.owl"));
			params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	
			break;
			
		case "STRUCTURALALIGNMENT":
			a = new StructuralAlignment();
	    	a.init( new URI("file:examples/rdf/TestTransport1.owl"), new URI("file:examples/rdf/TestTransport2.owl"));
			params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	
			break;

		}

		//Storing the alignment as RDF
		File outputFile = new File("./examples/rdf/output.rdf");
		PrintWriter writer = new PrintWriter(
				new BufferedWriter(
						new FileWriter(outputFile)), true); 
		AlignmentVisitor renderer = new RDFRendererVisitor(writer);

		//to manipulate the alignments we using the BasicAlignment, not the Alignment
		//clone the computed alignment from Alignment to BasicAlignment
		BasicAlignment a2 = (BasicAlignment)(a.clone());

/*		//merge the computed alignment with the input alignment
		a2.ingest(inputAlignment);*/

		//implement a similarity threshold
		a2.cut(THRESHOLD);

		//implement the join() method that joins an already computed alignment with this new alignment
		//a2.join(inputAlignment);
		//onto1.getURI() != align.getOntology1URI()


		a2.render(renderer);
		writer.flush();
		writer.close();

		//Evaluate the alignment against a reference alignment
		AlignmentParser aparser = new AlignmentParser(0);

		Alignment referenceAlignment = aparser.parse(new URI("file:examples/rdf/ReferenceAlignmentBIBLIO_BIBO.rdf"));
		//Alignment referenceAlignment = aparser.parse(new File("/Users/audunvennesland/Documents/PhD/Ontologies/OAEI/Conference-2016/reference-alignment/conference-ekaw.rdf").toURI());
		Properties p = new Properties();

		Evaluator evaluator = new PRecEvaluator(referenceAlignment, a2);
		evaluator.eval(p);
		System.out.println("------------------------------");
		System.out.println("Evaluation scores:");
		System.out.println("------------------------------");
		System.out.println("F-measure: " + evaluator.getResults().getProperty("fmeasure").toString());
		System.out.println("Precision: " + evaluator.getResults().getProperty("precision").toString());
		System.out.println("Recall: " + evaluator.getResults().getProperty("recall").toString());


	}
}


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
import no.ntnu.idi.compose.Matchers.CompoundAlignment;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.parser.AlignmentParser;


public class TestMatcher {

	public static void main(String[] args) throws AlignmentException, IOException, URISyntaxException {

		//Threshold for similarity score for which correspondences should be considered
		final double THRESHOLD = 0.8;
		final String MATCHER = "ISUB";


		//Parameters defining the (string) matching method to be applied
		Properties params = new Properties();

		AlignmentProcess a = null;

		AlignmentParser inputParser = new AlignmentParser(0);

		//An optional input alignment
		//Alignment inputAlignment = inputParser.parse(new File("/Users/audunvennesland/Documents/PhD/Development/Experiments/OAEIBIBLIO2BIBO/output_alignment_biblio-bibo_edit.rdf").toURI());

		switch(MATCHER) {
	
		case "ISUB":
			a = new ISubAlignment();
			//a.init( new URI("file:files/ontologies/Biblio_2015.rdf"), new URI("file:files/ontologies/BIBO.owl"));
	    	a.init( new URI("file:files/ontologies/Conference.owl"), new URI("file:files/ontologies/ekaw.owl"));
			params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	
			break;
			
		case "EDIT":
			a = new EditDistNameAlignment();
			//a.init( new URI("file:files/ontologies/Biblio_2015.rdf"), new URI("file:files/ontologies/BIBO.owl"));
	    	a.init( new URI("file:files/ontologies/Conference.owl"), new URI("file:files/ontologies/ekaw.owl"));
			params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	
			break;
			
		case "WORDNET":
			a = new WS4JAlignment();
			//a.init( new URI("file:files/ontologies/Biblio_2015.rdf"), new URI("file:files/ontologies/BIBO.owl"));
	    	a.init( new URI("file:files/ontologies/Conference.owl"), new URI("file:files/ontologies/ekaw.owl"));
			params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	
			break;

		case "COMPOUND":
			a = new CompoundAlignment();
			//a.init( new URI("file:files/ontologies/Biblio_2015.rdf"), new URI("file:files/ontologies/BIBO.owl"));
	    	a.init( new URI("file:files/ontologies/Conference.owl"), new URI("file:files/ontologies/ekaw.owl"));
			params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	
			break;
			
			
		case "GRAPHALIGNMENT":
			a = new GraphAlignment();
			//a.init( new URI("file:files/ontologies/Biblio_2015.rdf"), new URI("file:files/ontologies/BIBO.owl"));
	    	a.init( new URI("file:files/ontologies/Conference.owl"), new URI("file:files/ontologies/ekaw.owl"));
			params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	
			break;
			
		case "INSTANCEALIGNMENT":
			a = new InstanceAlignment();
			//a.init( new URI("file:files/ontologies/Biblio_2015.rdf"), new URI("file:files/ontologies/BIBO.owl"));
	    	//a.init( new URI("file:files/ontologies/Conference.owl"), new URI("file:files/ontologies/ekaw.owl"));
	    	a.init( new URI("file:files/ontologies/Test/TestTransportWithInstances1.owl"), new URI("file:files/ontologies/Test/TestTransportWithInstances2.owl"));
			params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	
			break;
		}

		//Storing the alignment as RDF
		File outputFile = new File("./files/alignments/output_Conference2Ekaw_EditDistAlignment.rdf");
		//File outputFile = new File("./files/alignments/output_Conference2Ekaw_Semantic-Compound_ClassSubsumption.rdf");
		PrintWriter writer = new PrintWriter(
				new BufferedWriter(
						new FileWriter(outputFile)), true); 
		AlignmentVisitor renderer = new RDFRendererVisitor(writer);

		//to manipulate the alignments we using the BasicAlignment, not the Alignment
		//clone the computed alignment from Alignment to BasicAlignment
		BasicAlignment a2 = (BasicAlignment)(a.clone());

		//implement a similarity threshold
		a2.cut(THRESHOLD);

		a2.render(renderer);
		writer.flush();
		writer.close();

		//Evaluate the alignment against a reference alignment
		AlignmentParser aparser = new AlignmentParser(0);

		//Alignment referenceAlignment = aparser.parse(new URI("file:files/referenceAlignments/OAEI_Biblio2BIBO_ReferenceAlignment_Class_SubsumptionOnly.rdf"));
		Alignment referenceAlignment = aparser.parse(new URI("file:files/referenceAlignments/conference-ekaw_ReferenceAlignment_Class_EquivalenceOnly.rdf"));
		Properties p = new Properties();

		Evaluator evaluator = new PRecEvaluator(referenceAlignment, a2);
		evaluator.eval(p);
		System.out.println("------------------------------");
		System.out.println("Evaluation scores:");
		System.out.println("------------------------------");
		System.out.println("Precision: " + evaluator.getResults().getProperty("precision").toString());
		System.out.println("Recall: " + evaluator.getResults().getProperty("recall").toString());
		System.out.println("F-measure: " + evaluator.getResults().getProperty("fmeasure").toString());


	}
}


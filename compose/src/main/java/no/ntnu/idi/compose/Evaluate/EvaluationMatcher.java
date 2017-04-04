package no.ntnu.idi.compose.Evaluate;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
//Alignment API classes
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Evaluator;

import fr.inrialpes.exmo.align.cli.GroupEval;
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.eval.PRecEvaluator;
import fr.inrialpes.exmo.align.impl.method.EditDistNameAlignment;
import fr.inrialpes.exmo.align.impl.method.StringDistAlignment;
import no.ntnu.idi.compose.loading.OWLLoader;
import no.ntnu.idi.compose.matchers.ClassEq_String_Matcher;
import no.ntnu.idi.compose.matchers.Subsumption_String_Matcher;
import no.ntnu.idi.compose.profiling.OntologyProcessor;
import no.ntnu.idi.compose.preprocessing.Preprocessor;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.parser.AlignmentParser;


public class EvaluationMatcher {

	public static void main(String[] args) throws AlignmentException, IOException, URISyntaxException {


		//input parameters for each matching operation
		final String MATCHER = "EDIT";
		final double THRESHOLD = 0.8;
		File outputAlignment = null;
		final File ontologyDir = new File("./files/experiment_eswc17/ontologies");
		//only for testing the graph-based matchers:
		//final File ontologyDir = new File("./files/ontologies/Test");
		File[] filesInDir = null;
		final String prefix = "file:";
		//Evaluate the alignment against a reference alignment
		AlignmentParser aparser = new AlignmentParser(0);
		String aggMethod = "min";
		String[] listAlgo = null;

		Alignment referenceAlignment = aparser.parse(new URI("file:files/referenceAlignments/OAEI_Biblio2BIBO_ReferenceAlignment_Class_EquivalenceOnly.rdf"));

		Properties p = new Properties();


		//Parameters defining the (string) matching method to be applied
		Properties params = new Properties();



		//AlignmentParser inputParser = new AlignmentParser(0);


			AlignmentProcess smoaAlignment = new StringDistAlignment();
			params.setProperty("stringFunction", "smoaDistance");
			smoaAlignment.init( new URI("file:files/ontologies/Biblio_2015.rdf"), new URI("file:files/ontologies/BIBO.owl"));
			smoaAlignment.align((Alignment)null, params );
			Alignment basicSmoaAlignment = (Alignment) smoaAlignment.clone();
			basicSmoaAlignment.cut(THRESHOLD);

			
			//Storing the alignment as RDF
			File smoaOutputFile = new File("./files/evaluation/Smoa.rdf");
			PrintWriter smoaAlignmentWriter = new PrintWriter(
					new BufferedWriter(
							new FileWriter(smoaOutputFile)), true); 
			AlignmentVisitor smoaRenderer = new RDFRendererVisitor(smoaAlignmentWriter);
			basicSmoaAlignment.render(smoaRenderer);


			AlignmentProcess iSubAlignment = new ClassEq_String_Matcher();
			iSubAlignment.init( new URI("file:files/ontologies/Biblio_2015.rdf"), new URI("file:files/ontologies/BIBO.owl"));
			params = new Properties();
			params.setProperty("", "");
			iSubAlignment.align((Alignment)null, params);	
			BasicAlignment basicISubAlignment = (BasicAlignment)(iSubAlignment.clone());			
			basicISubAlignment.cut(THRESHOLD);
			
			//Storing the alignment as RDF
			File iSubOutputFile = new File("./files/evaluation/ISub.rdf");
			PrintWriter iSubAlignmentWriter = new PrintWriter(
					new BufferedWriter(
							new FileWriter(iSubOutputFile)), true); 
			AlignmentVisitor iSubRenderer = new RDFRendererVisitor(iSubAlignmentWriter);
			basicISubAlignment.render(iSubRenderer);
			
			//ingest
			BasicAlignment ingestISubAndSmoaAlignment = (BasicAlignment)(basicISubAlignment.clone());
			ingestISubAndSmoaAlignment.ingest(basicSmoaAlignment);
			File ingestISubAndSmoaOutputFile = new File("./files/evaluation/ingestISubAndSmoa.rdf");
			PrintWriter ingestISubAndSmoaAlignmentWriter = new PrintWriter(
					new BufferedWriter(
							new FileWriter(ingestISubAndSmoaOutputFile)), true); 
			AlignmentVisitor ingestISubAndSmoaRenderer = new RDFRendererVisitor(ingestISubAndSmoaAlignmentWriter);
			ingestISubAndSmoaAlignment.render(ingestISubAndSmoaRenderer);
			

			//meet
			BasicAlignment meetISubAndSmoaAlignment = (BasicAlignment)(ingestISubAndSmoaAlignment.clone());
			meetISubAndSmoaAlignment.meet(basicSmoaAlignment);
			File meetISubAndSmoaOutputFile = new File("./files/evaluation/MeetISubAndSmoa.rdf");
			PrintWriter meetISubAndSmoaAlignmentWriter = new PrintWriter(
					new BufferedWriter(
							new FileWriter(meetISubAndSmoaOutputFile)), true); 
			AlignmentVisitor meetISubAndSmoaRenderer = new RDFRendererVisitor(meetISubAndSmoaAlignmentWriter);
			meetISubAndSmoaAlignment.render(meetISubAndSmoaRenderer);
			
			//join
			BasicAlignment joinISubAndSmoaAlignment = (BasicAlignment)(basicISubAlignment.clone());
			joinISubAndSmoaAlignment.meet(basicSmoaAlignment);
			File joinISubAndSmoaOutputFile = new File("./files/evaluation/joinISubAndSmoa.rdf");
			PrintWriter joinISubAndSmoaAlignmentWriter = new PrintWriter(
					new BufferedWriter(
							new FileWriter(joinISubAndSmoaOutputFile)), true); 
			AlignmentVisitor joinISubAndSmoaRenderer = new RDFRendererVisitor(joinISubAndSmoaAlignmentWriter);
			joinISubAndSmoaAlignment.render(joinISubAndSmoaRenderer);
			
			//Join
			Evaluator joinISubAndSmoaEvaluator = new PRecEvaluator(referenceAlignment, joinISubAndSmoaAlignment);
			joinISubAndSmoaEvaluator.eval(p);
			System.out.println("------------------------------");
			System.out.println("Evaluation scores for the join():");
			System.out.println("------------------------------");
			System.out.println("F-measure: " + joinISubAndSmoaEvaluator.getResults().getProperty("fmeasure").toString());
			System.out.println("Precision: " + joinISubAndSmoaEvaluator.getResults().getProperty("precision").toString());
			System.out.println("Recall: " + joinISubAndSmoaEvaluator.getResults().getProperty("recall").toString());
			
			//Meet
			Evaluator meetISubAndSmoaEvaluator = new PRecEvaluator(referenceAlignment, meetISubAndSmoaAlignment);
			meetISubAndSmoaEvaluator.eval(p);
			System.out.println("------------------------------");
			System.out.println("Evaluation scores for the meet():");
			System.out.println("------------------------------");
			System.out.println("F-measure: " + meetISubAndSmoaEvaluator.getResults().getProperty("fmeasure").toString());
			System.out.println("Precision: " + meetISubAndSmoaEvaluator.getResults().getProperty("precision").toString());
			System.out.println("Recall: " + meetISubAndSmoaEvaluator.getResults().getProperty("recall").toString());
			
			//Smoa
			Evaluator smoaEvaluator = new PRecEvaluator(referenceAlignment, basicSmoaAlignment);
			smoaEvaluator.eval(p);
			System.out.println("------------------------------");
			System.out.println("Evaluation scores for smoa:");
			System.out.println("------------------------------");
			System.out.println("F-measure: " + smoaEvaluator.getResults().getProperty("fmeasure").toString());
			System.out.println("Precision: " + smoaEvaluator.getResults().getProperty("precision").toString());
			System.out.println("Recall: " + smoaEvaluator.getResults().getProperty("recall").toString());
			
			//ISub
			Evaluator iSubEvaluator = new PRecEvaluator(referenceAlignment, basicISubAlignment);
			iSubEvaluator.eval(p);
			System.out.println("------------------------------");
			System.out.println("Evaluation scores for isub:");
			System.out.println("------------------------------");
			System.out.println("F-measure: " + iSubEvaluator.getResults().getProperty("fmeasure").toString());
			System.out.println("Precision: " + iSubEvaluator.getResults().getProperty("precision").toString());
			System.out.println("Recall: " + iSubEvaluator.getResults().getProperty("recall").toString());
			
			
			//Ingest
			Evaluator combinedISubAndSmoaEvaluator = new PRecEvaluator(referenceAlignment, ingestISubAndSmoaAlignment);
			combinedISubAndSmoaEvaluator.eval(p);
			System.out.println("------------------------------");
			System.out.println("Evaluation scores for the ingest():");
			System.out.println("------------------------------");
			System.out.println("F-measure: " + iSubEvaluator.getResults().getProperty("fmeasure").toString());
			System.out.println("Precision: " + iSubEvaluator.getResults().getProperty("precision").toString());
			System.out.println("Recall: " + iSubEvaluator.getResults().getProperty("recall").toString());
			
			smoaAlignmentWriter.flush();
			smoaAlignmentWriter.close();
			iSubAlignmentWriter.flush();
			iSubAlignmentWriter.close();
/*			meetISubAndSmoaAlignmentWriter.flush();
			meetISubAndSmoaAlignmentWriter.close();
			joinISubAndSmoaAlignmentWriter.flush();
			joinISubAndSmoaAlignmentWriter.close();*/
			ingestISubAndSmoaAlignmentWriter.flush();
			ingestISubAndSmoaAlignmentWriter.close();
	}

}
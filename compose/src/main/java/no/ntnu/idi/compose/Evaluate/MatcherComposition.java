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
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Evaluator;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.eval.PRecEvaluator;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import fr.inrialpes.exmo.ontowrap.OntowrapException;
import no.ntnu.idi.compose.Matchers.GraphAlignment;
import no.ntnu.idi.compose.Matchers.ISubAlignment;
import no.ntnu.idi.compose.Matchers.WS4JAlignment;
import no.ntnu.idi.compose.Matchers.WordNetAlignment;
import no.ntnu.idi.compose.Preprocess.Preprocessor;

public class MatcherComposition {


	public static void main(String[] args) throws AlignmentException, IOException, URISyntaxException, OntowrapException, OWLOntologyCreationException {

		final String composition = "Sequential_PartialMatch_ClassEq";
		String experiment = "cmt-confOf";
		double threshold = 0;

		//the ontologies being matched (this is needed for the graph-based matcher)
		String onto1 = experiment.substring(0, experiment.lastIndexOf("-"));
		String onto2 = experiment.substring(experiment.lastIndexOf("-")+1, experiment.length());
		
		File ontoFile1 = new File("./files/experiment_eswc17/ontologies/" + onto1 + ".owl");
		File ontoFile2 = new File("./files/experiment_eswc17/ontologies/" + onto2 + ".owl");
		File dbFile = new File("/Users/audunvennesland/Documents/PhD/Development/Neo4J/Experiment_eswc17");
		GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(dbFile);

		String ontologyParameter1 = Preprocessor.stripPath(ontoFile1.toString());
		String ontologyParameter2 = Preprocessor.stripPath(ontoFile2.toString());

		//reference alignment for the evaluation
		AlignmentParser aparser = new AlignmentParser(0);
		Properties p = new Properties();
		
		Evaluator evaluator = null;
		

		//declare the matchers
		ISubAlignment stringMatcher = new ISubAlignment();
		WS4JAlignment wordNetMatcher = new WS4JAlignment();
		//the graph-based matcher needs parameters (preprocessed names of the ontology files and a pointer to the Neo4J database)
		GraphAlignment structuralMatcher = new GraphAlignment(ontologyParameter1,ontologyParameter2, db);

		ParallelComposition par = new ParallelComposition();
		HybridComposition hyb = new HybridComposition();

		switch(composition) {

		//here we only need to process the alignment files, so no matcher involved
		case "Sequential_CompleteMatch_ClassEq":

			threshold = 0.9;
			
			//import the alignment files
			File a1 = new File("./files/experiment_eswc17/alignments/" + experiment + "/ClassEq_String.rdf");
			File a2 = new File("./files/experiment_eswc17/alignments/" + experiment + "/ClassEq_WordNet.rdf");
			File a3 = new File("./files/experiment_eswc17/alignments/" + experiment + "/ClassEq_Structural.rdf");
			
			Alignment seqCompAlignment_classEq = SequentialComposition.completeMatch(a3, a2, a1);
			Alignment referenceAlignment_classEq = aparser.parse(new URI("file:files/experiment_eswc17/alignments/" + experiment + "/referencealignment/refalign_classEq.rdf"));
			
			//store the alignment
			File outputAlignmentFile = new File("./files/experiment_eswc17/alignments/" + experiment + "/Sequential_CompleteMatch_wordnet-string-structural_classEq.rdf");

			PrintWriter writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(outputAlignmentFile)), true); 
			AlignmentVisitor renderer = new RDFRendererVisitor(writer);
			
			seqCompAlignment_classEq.cut(threshold);

			seqCompAlignment_classEq.render(renderer);

			
			System.out.println("Matching completed!");

			evaluator = new PRecEvaluator(referenceAlignment_classEq, seqCompAlignment_classEq);
			p = new Properties();
			evaluator.eval(p);
			System.out.println("------------------------------");
			System.out.println("F-measure: " + evaluator.getResults().getProperty("fmeasure").toString());
			System.out.println("Precision: " + evaluator.getResults().getProperty("precision").toString());
			System.out.println("Recall: " + evaluator.getResults().getProperty("recall").toString());
			System.out.println("------------------------------");

			
			writer.flush();
			writer.close();
			
			break;
			
		case "Sequential_CompleteMatch_PropEq":
			
			threshold = 0.6;
			
			//import the alignment files
			a1 = new File("./files/experiment_eswc17/alignments/" + experiment + "/PropEq_ObjectProperty.rdf");
			a2 = new File("./files/experiment_eswc17/alignments/" + experiment + "/PropEq_WordNet.rdf");
			a3 = new File("./files/experiment_eswc17/alignments/" + experiment + "/PropString-recall.rdf");
			
			Alignment seqCompAlignment_propEq = SequentialComposition.completeMatch(a3, a2, a1);
			Alignment referenceAlignment_propEq = aparser.parse(new URI("file:files/experiment_eswc17/alignments/" + experiment + "/referencealignment/refalign_propEq.rdf"));
			
			//store the alignment
			outputAlignmentFile = new File("./files/experiment_eswc17/alignments/" + experiment + "/Sequential_CompleteMatch_wordnet-propstring-string-propEq.rdf");

			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(outputAlignmentFile)), true); 
			renderer = new RDFRendererVisitor(writer);
			
			seqCompAlignment_propEq.cut(threshold);

			seqCompAlignment_propEq.render(renderer);

			System.out.println("Matching completed!");

			evaluator = new PRecEvaluator(referenceAlignment_propEq, seqCompAlignment_propEq);
			p = new Properties();
			
			evaluator.eval(p);
			System.out.println("------------------------------");
			System.out.println("F-measure: " + evaluator.getResults().getProperty("fmeasure").toString());
			System.out.println("Precision: " + evaluator.getResults().getProperty("precision").toString());
			System.out.println("Recall: " + evaluator.getResults().getProperty("recall").toString());
			System.out.println("------------------------------");

			writer.flush();
			writer.close();
			
			break;
			
		case "Sequential_CompleteMatch_Subsumption":
			
			threshold = 0.9;
			
			//import the alignment files
			a1 = new File("./files/experiment_eswc17/alignments/" + experiment + "/Subsumption_Compound.rdf");
			a2 = new File("./files/experiment_eswc17/alignments/" + experiment + "/Subsumption_WordNet.rdf");
			a3 = new File("./files/experiment_eswc17/alignments/" + experiment + "/Subsumption_SubClass.rdf");
			
			Alignment seqCompAlignment_subsumption = SequentialComposition.completeMatch(a3, a2, a1);
			Alignment referenceAlignment_subsumption = aparser.parse(new URI("file:files/experiment_eswc17/alignments/" + experiment + "/referencealignment/refalign_subsumption.rdf"));
			
			//store the alignment
			outputAlignmentFile = new File("./files/experiment_eswc17/alignments/" + experiment + "/Sequential_CompleteMatch-wordnet-compound-structural-subsumption.rdf");

			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(outputAlignmentFile)), true); 
			renderer = new RDFRendererVisitor(writer);
			
			seqCompAlignment_subsumption.cut(threshold);

			seqCompAlignment_subsumption.render(renderer);

			System.out.println("Matching completed!");

			evaluator = new PRecEvaluator(referenceAlignment_subsumption, seqCompAlignment_subsumption);
			p = new Properties();
			
			evaluator.eval(p);
			System.out.println("------------------------------");
			System.out.println("F-measure: " + evaluator.getResults().getProperty("fmeasure").toString());
			System.out.println("Precision: " + evaluator.getResults().getProperty("precision").toString());
			System.out.println("Recall: " + evaluator.getResults().getProperty("recall").toString());
			System.out.println("------------------------------");

			writer.flush();
			writer.close();
			
			break;

		//here the matchers are involved
		//so e.g. the initial alignment a1 from m1 (string)	is matched by m2 (wordnet), and this wordnet alignment is then matched by the structure-based matcher. 
		case "Sequential_PartialMatch_ClassEq":
			
			//import initial alignment
			File inputFile = new File("./files/experiment_eswc17/alignments/" + experiment + "/ClassEq_Structural.rdf");
			//this alignment file must be parsed...
			AlignmentParser parser = new AlignmentParser();
			BasicAlignment structAlignment = (BasicAlignment)parser.parse(inputFile.toURI().toString());
			//BasicAlignment structureAlignment = (BasicAlignment) GraphAlignment.matchAlignment(wnAlignment);
			
			BasicAlignment wordNetAlignment = (BasicAlignment) WordNetAlignment.matchAlignment(structAlignment);
			BasicAlignment stringAlignment = (BasicAlignment) WordNetAlignment.matchAlignment(wordNetAlignment);
			
			
			
			//store the alignment
			outputAlignmentFile = new File("./files/experiment_eswc17/alignments/" + experiment + "/Sequential_PartialMatch-wordnet-structure-string.rdf");

			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(outputAlignmentFile)), true); 
			renderer = new RDFRendererVisitor(writer);
			
			stringAlignment.cut(threshold);

			stringAlignment.render(renderer);

			System.out.println("Matching completed!");

			//evaluate
			Alignment referenceAlignment_partial_classEq = aparser.parse(new URI("file:files/experiment_eswc17/alignments/" + experiment + "/referencealignment/refalign_classEq.rdf"));
			
			p = new Properties();
			
			evaluator = new PRecEvaluator(referenceAlignment_partial_classEq, structAlignment);
			evaluator.eval(p);
			System.out.println("The f-measure for the input alignment is " + evaluator.getResults().getProperty("fmeasure").toString());

			evaluator = new PRecEvaluator(referenceAlignment_partial_classEq, stringAlignment);
			evaluator.eval(p);
			System.out.println("The f-measure for the wordnet alignment is " + evaluator.getResults().getProperty("fmeasure").toString());
			
			evaluator = new PRecEvaluator(referenceAlignment_partial_classEq, wordNetAlignment);
			evaluator.eval(p);
			System.out.println("The f-measure for the string alignment is " + evaluator.getResults().getProperty("fmeasure").toString());

			evaluator = new PRecEvaluator(referenceAlignment_partial_classEq, stringAlignment);

			evaluator.eval(p);
			System.out.println("------------------------------");
			System.out.println("F-measure: " + evaluator.getResults().getProperty("fmeasure").toString());
			System.out.println("Precision: " + evaluator.getResults().getProperty("precision").toString());
			System.out.println("Recall: " + evaluator.getResults().getProperty("recall").toString());
			System.out.println("------------------------------");
			
			writer.flush();
			writer.close();

			break;

		case "Hybrid_CompleteMatch":

			break;

		case "Hybrid_PartialMatch":

			break;

		case "Parallel_CompleteMatch":

			break;



		}




	}
}

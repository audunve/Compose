package no.ntnu.idi.compose.composition;

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
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Evaluator;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.eval.PRecEvaluator;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import fr.inrialpes.exmo.ontowrap.OntowrapException;
import no.ntnu.idi.compose.matchers.PropEq_String_DataProperty_Matcher;
import no.ntnu.idi.compose.matchers.ClassEq_Structural_Matcher;
import no.ntnu.idi.compose.matchers.ClassEq_String_Matcher;
import no.ntnu.idi.compose.matchers.PropEq_WordNet_Matcher;
import no.ntnu.idi.compose.matchers.ClassEq_WordNet_Matcher;
import no.ntnu.idi.compose.preprocessing.Preprocessor;

/**
 * Runs the different matcher compositions (
 * @author audunvennesland
 * 2. feb. 2017
 */
public class MatcherComposition {


	public static void main(String[] args) throws AlignmentException, IOException, URISyntaxException, OntowrapException, OWLOntologyCreationException {

		//final String composition = "Parallel_SimpleVote_Subsumption";
		final String composition = "Parallel_SimpleVote_ClassEq";		
		String experiment = "303-304";
		double threshold = 0;
		File inputFile  = null;
		AlignmentParser parser = null;
		BasicAlignment structAlignment = null;
		BasicAlignment stringAlignment = null;
		BasicAlignment wordNetAlignment = null;
		
		String onto1 = "303";
		String onto2 = "304";

		File ontoFile1 = new File("./files/OAEI2011/" + experiment + "/" + onto1 + "..rdf");
		File ontoFile2 = new File("./files/OAEI2011/" + experiment + "/" + onto2 + "..rdf");

		File dbFile = new File("/Users/audunvennesland/Documents/PhD/Development/Neo4J/ER2017/"+experiment);
		GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(dbFile);

		String ontologyParameter1 = Preprocessor.stripPath(ontoFile1.toString());
		String ontologyParameter2 = Preprocessor.stripPath(ontoFile2.toString());

		//reference alignment for the evaluation
		AlignmentParser aparser = new AlignmentParser(0);
		Properties p = new Properties();

		Evaluator evaluator = null;


		//declare the matchers
		ClassEq_String_Matcher stringMatcher = new ClassEq_String_Matcher();
		ClassEq_WordNet_Matcher wordNetMatcher = new ClassEq_WordNet_Matcher();
		//the graph-based matcher needs parameters (preprocessed names of the ontology files and a pointer to the Neo4J database)
		ClassEq_Structural_Matcher structuralMatcher = new ClassEq_Structural_Matcher(ontologyParameter1,ontologyParameter2, db);


		ParallelComposition par = new ParallelComposition();
		HybridComposition hyb = new HybridComposition();

		switch(composition) {

		//here we only need to process the alignment files, so no matcher involved
		//this is the weighted complete match composition 
		case "Sequential_CompleteMatch_ClassEq":

			threshold = 0.9;

			//import the alignment files
			File a1 = new File("./files/experiment_eswc17/alignments/" + experiment + "/ClassEq_String.rdf");
			File a2 = new File("./files/experiment_eswc17/alignments/" + experiment + "/ClassEq_WordNet.rdf");
			File a3 = new File("./files/experiment_eswc17/alignments/" + experiment + "/ClassEq_Structural.rdf");

			Alignment seqCompAlignment_classEq = SequentialComposition.weightedSequentialComposition(a1, a3, a2);
			Alignment referenceAlignment_classEq = aparser.parse(new URI("file:files/experiment_eswc17/alignments/" + experiment + "/referencealignment/refalign_classEq.rdf"));

			//store the alignment
			File outputAlignmentFile = new File("./files/experiment_eswc17/alignments/" + experiment + "/NonWeightedSequential_CompleteMatch_ClassEq.rdf");

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

		case "Sequential_NonWeightedCompleteMatch_ClassEq":

			threshold = 0.9;

			//import the alignment files
			a1 = new File("./files/experiment_eswc17/alignments/" + experiment + "/ClassEq_String.rdf");
			a2 = new File("./files/experiment_eswc17/alignments/" + experiment + "/ClassEq_WordNet.rdf");
			a3 = new File("./files/experiment_eswc17/alignments/" + experiment + "/ClassEq_Structural.rdf");

			Alignment weightedSeqCompAlignment_classEq = SequentialComposition.nonWeightedCompleteMatch(a1, a3, a2);
			Alignment weightedReferenceAlignment_classEq = aparser.parse(new URI("file:files/experiment_eswc17/alignments/" + experiment + "/referencealignment/refalign_classEq.rdf"));

			//store the alignment
			outputAlignmentFile = new File("./files/experiment_eswc17/alignments/" + experiment + "/Sequential_CompleteMatch_ClassEq.rdf");

			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(outputAlignmentFile)), true); 
			AlignmentVisitor nonWeightedSeqRenderer = new RDFRendererVisitor(writer);

			weightedSeqCompAlignment_classEq.cut(threshold);

			weightedSeqCompAlignment_classEq.render(nonWeightedSeqRenderer);


			System.out.println("Matching completed!");

			evaluator = new PRecEvaluator(weightedReferenceAlignment_classEq, weightedSeqCompAlignment_classEq);
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

		case "Sequential_NonWeightedCompleteMatch_PropEq":

			threshold = 0.6;

			//import the alignment files
			a1 = new File("./files/experiment_eswc17/alignments/" + experiment + "/PropEq_ObjectProperty.rdf");
			a2 = new File("./files/experiment_eswc17/alignments/" + experiment + "/PropEq_WordNet.rdf");
			a3 = new File("./files/experiment_eswc17/alignments/" + experiment + "/PropString-recall.rdf");

			Alignment weightedSeqCompAlignment_propEq = SequentialComposition.nonWeightedCompleteMatch(a2, a3, a1);
			Alignment weightedReferenceAlignment_propEq = aparser.parse(new URI("file:files/experiment_eswc17/alignments/" + experiment + "/referencealignment/refalign_propEq.rdf"));

			//store the alignment
			outputAlignmentFile = new File("./files/experiment_eswc17/alignments/" + experiment + "/Sequential_CompleteMatch_ClassEq.rdf");

			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(outputAlignmentFile)), true); 
			AlignmentVisitor nonWeightedSeqRendererPropEq = new RDFRendererVisitor(writer);

			weightedSeqCompAlignment_propEq.cut(threshold);

			weightedSeqCompAlignment_propEq.render(nonWeightedSeqRendererPropEq);


			System.out.println("Matching completed!");

			evaluator = new PRecEvaluator(weightedReferenceAlignment_propEq, weightedSeqCompAlignment_propEq);
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

			Alignment seqCompAlignment_propEq = SequentialComposition.weightedSequentialComposition(a3, a2, a1);
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
			a1 = new File("./files/experiment_eswc17/alignments/" + experiment + "/Subsumption_String_Matcher.rdf");
			a2 = new File("./files/experiment_eswc17/alignments/" + experiment + "/Subsumption_WordNet_Matcher.rdf");
			a3 = new File("./files/experiment_eswc17/alignments/" + experiment + "/Subsumption_SubClass.rdf");

			Alignment seqCompAlignment_subsumption = SequentialComposition.weightedSequentialComposition(a3, a2, a1);
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
			inputFile = new File("./files/experiment_eswc17/alignments/" + experiment + "/ClassEq_Structural.rdf");
			//this alignment file must be parsed...
			parser = new AlignmentParser();
			structAlignment = (BasicAlignment)parser.parse(inputFile.toURI().toString());
			//BasicAlignment structureAlignment = (BasicAlignment) ClassEq_Structural_Matcher.matchAlignment(wnAlignment);

			wordNetAlignment = (BasicAlignment) ClassEq_WordNet_Matcher.matchAlignment(structAlignment);
			stringAlignment = (BasicAlignment) ClassEq_WordNet_Matcher.matchAlignment(wordNetAlignment);



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


		case "Parallel_SimpleVote_ClassEq":
			//import the alignment files
			a1 = new File("./files/OAEI2011/"+experiment+"/COMPOSE-ClassEq_String.rdf");
			a2 = new File("./files/OAEI2011/"+experiment+"/COMPOSE-ClassEq_Graph.rdf");
			a3 = new File("./files/OAEI2011/"+experiment+"/COMPOSE-ClassEq_WordNet.rdf");

			//set the threshold
			threshold = 0.6;

			Alignment Parallel_SimpleVote_ClassEqAlignment = ParallelComposition.simpleVote(a1, a2, a3);
			Alignment referenceAlignment_Parallel_SimpleVote_ClassEq = aparser.parse(new URI("file:files/OAEI2011/" + experiment + "/refalign.rdf"));

			//store the alignment
			File Parallel_SimpleVote_ClassEq_AlignmentFile = new File("./files/OAEI2011/" + experiment + "/Parallel_SimpleVote_ClassEq.rdf");

			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(Parallel_SimpleVote_ClassEq_AlignmentFile)), true); 
			AlignmentVisitor Parallel_SimpleVote_ClassEq_renderer = new RDFRendererVisitor(writer);

			Parallel_SimpleVote_ClassEqAlignment.cut(threshold);

			Parallel_SimpleVote_ClassEqAlignment.render(Parallel_SimpleVote_ClassEq_renderer);

			System.out.println("Matching completed!");

			evaluator = new PRecEvaluator(referenceAlignment_Parallel_SimpleVote_ClassEq, Parallel_SimpleVote_ClassEqAlignment);
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
			
		case "Parallel_SimpleVote_Subsumption":
			//import the alignment files
			a1 = new File("./files/OAEI2011/"+ experiment +"/COMPOSE-Subsumption_String.rdf");
			a2 = new File("./files/OAEI2011/"+ experiment +"/COMPOSE-Subsumption_SubClass.rdf");
			a3 = new File("./files/OAEI2011/"+ experiment +"/COMPOSE-Subsumption_WordNet.rdf");

			//set the threshold
			threshold = 0.6;

			Alignment Parallel_SimpleVote_SubsumptionAlignment = ParallelComposition.simpleVote(a1, a2, a3);
			Alignment referenceAlignment_Parallel_SimpleVote_Subsumption = aparser.parse(new URI("file:files/OAEI2011/" + experiment + "/refalign.rdf"));

			//store the alignment
			File Parallel_SimpleVote_Subsumption_AlignmentFile = new File("./files/OAEI2011/" + experiment + "/Parallel_SimpleVote_Subsumption.rdf");

			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(Parallel_SimpleVote_Subsumption_AlignmentFile)), true); 
			AlignmentVisitor Parallel_SimpleVote_Subsumption_renderer = new RDFRendererVisitor(writer);

			Parallel_SimpleVote_SubsumptionAlignment.cut(threshold);

			Parallel_SimpleVote_SubsumptionAlignment.render(Parallel_SimpleVote_Subsumption_renderer);


			System.out.println("Matching completed!");

			evaluator = new PRecEvaluator(referenceAlignment_Parallel_SimpleVote_Subsumption, Parallel_SimpleVote_SubsumptionAlignment);
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

		case "Parallel_SimpleVote_PropEq":
			//import the alignment files
			a1 = new File("./files/experiment_eswc17/alignments/" + experiment + "/PropEq_String.rdf");
			a2 = new File("./files/experiment_eswc17/alignments/" + experiment + "/PropEq_WordNet2.rdf");
			a3 = new File("./files/experiment_eswc17/alignments/" + experiment + "/PropString-recall.rdf");

			//set the threshold
			threshold = 0.6;

			Alignment Parallel_SimpleVote_PropEqAlignment = ParallelComposition.simpleVote(a3, a1, a2);
			Alignment referenceAlignment_Parallel_SimpleVote_PropEq = aparser.parse(new URI("file:files/experiment_eswc17/alignments/" + experiment + "/referencealignment/refalign_propEq.rdf"));

			//store the alignment
			File Parallel_SimpleVote_PropEq_AlignmentFile = new File("./files/experiment_eswc17/alignments/" + experiment + "/Parallel_SimpleVote_PropEq.rdf");

			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(Parallel_SimpleVote_PropEq_AlignmentFile)), true); 
			AlignmentVisitor Parallel_SimpleVote_PropEq_renderer = new RDFRendererVisitor(writer);

			Parallel_SimpleVote_PropEqAlignment.cut(threshold);

			Parallel_SimpleVote_PropEqAlignment.render(Parallel_SimpleVote_PropEq_renderer);


			System.out.println("Matching completed!");

			evaluator = new PRecEvaluator(referenceAlignment_Parallel_SimpleVote_PropEq, Parallel_SimpleVote_PropEqAlignment);
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

		


		case "Parallel_CompleteMatch_WithPriority_ClassEq":
			//import the alignment files
			a1 = new File("./files/experiment_eswc17/alignments/" + experiment + "/ClassEq_String.rdf");
			a2 = new File("./files/experiment_eswc17/alignments/" + experiment + "/ClassEq_WordNet2.rdf");
			a3 = new File("./files/experiment_eswc17/alignments/" + experiment + "/ClassEq_Structural.rdf");

			//set the threshold
			threshold = 0.9;

			Alignment Parallel_CompleteMatch_WithPriority_ClassEqAlignment = ParallelComposition.completeMatchWithPriority(a3, a2, a3);
			Alignment referenceAlignment_Parallel_CompleteMatch_WithPriority_ClassEq = aparser.parse(new URI("file:files/experiment_eswc17/alignments/" + experiment + "/referencealignment/refalign_classEq.rdf"));

			//store the alignment
			File Parallel_CompleteMatch_WithPriority_ClassEq_AlignmentFile = new File("./files/experiment_eswc17/alignments/" + experiment + "/Parallel_CompleteMatch_WithPriority_ClassEq.rdf");

			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(Parallel_CompleteMatch_WithPriority_ClassEq_AlignmentFile)), true); 
			AlignmentVisitor Parallel_CompleteMatch_WithPriority_ClassEq_renderer = new RDFRendererVisitor(writer);

			Parallel_CompleteMatch_WithPriority_ClassEqAlignment.cut(threshold);

			Parallel_CompleteMatch_WithPriority_ClassEqAlignment.render(Parallel_CompleteMatch_WithPriority_ClassEq_renderer);


			System.out.println("Matching completed!");

			evaluator = new PRecEvaluator(referenceAlignment_Parallel_CompleteMatch_WithPriority_ClassEq, Parallel_CompleteMatch_WithPriority_ClassEqAlignment);
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

		case "Parallel_CompleteMatch_WithPriority_PropEq":
			//import the alignment files
			a1 = new File("./files/experiment_eswc17/alignments/" + experiment + "/PropEq_String.rdf");
			a2 = new File("./files/experiment_eswc17/alignments/" + experiment + "/PropEq_WordNet2.rdf");
			a3 = new File("./files/experiment_eswc17/alignments/" + experiment + "/PropString-recall.rdf");

			//set the threshold
			threshold = 0.6;

			Alignment Parallel_CompleteMatch_WithPriority_PropEqAlignment = ParallelComposition.completeMatchWithPriority(a3, a1, a2);
			Alignment referenceAlignment_Parallel_CompleteMatch_WithPriority_PropEq = aparser.parse(new URI("file:files/experiment_eswc17/alignments/" + experiment + "/referencealignment/refalign_propEq.rdf"));

			//store the alignment
			File Parallel_CompleteMatch_WithPriority_PropEq_AlignmentFile = new File("./files/experiment_eswc17/alignments/" + experiment + "/Parallel_CompleteMatch_WithPriority_PropEq.rdf");

			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(Parallel_CompleteMatch_WithPriority_PropEq_AlignmentFile)), true); 
			AlignmentVisitor Parallel_CompleteMatch_WithPriority_PropEq_renderer = new RDFRendererVisitor(writer);

			Parallel_CompleteMatch_WithPriority_PropEqAlignment.cut(threshold);

			Parallel_CompleteMatch_WithPriority_PropEqAlignment.render(Parallel_CompleteMatch_WithPriority_PropEq_renderer);


			System.out.println("Matching completed!");

			evaluator = new PRecEvaluator(referenceAlignment_Parallel_CompleteMatch_WithPriority_PropEq, Parallel_CompleteMatch_WithPriority_PropEqAlignment);
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

		case "Parallel_CompleteMatch_WithPriority_Subsumption":
			//import the alignment files
			a1 = new File("./files/experiment_eswc17/alignments/" + experiment + "/Subsumption_String_Matcher.rdf");
			a2 = new File("./files/experiment_eswc17/alignments/" + experiment + "/Subsumption_WordNet_Matcher.rdf");
			a3 = new File("./files/experiment_eswc17/alignments/" + experiment + "/Subsumption_SubClass.rdf");

			//set the threshold
			threshold = 0.9;

			Alignment Parallel_CompleteMatch_WithPriority_SubsumptionAlignment = ParallelComposition.completeMatchWithPriority(a3, a1, a2);
			Alignment referenceAlignment_Parallel_CompleteMatch_WithPriority_Subsumption = aparser.parse(new URI("file:files/experiment_eswc17/alignments/" + experiment + "/referencealignment/refalign_subsumption.rdf"));

			//store the alignment
			File Parallel_CompleteMatch_WithPriority_Subsumption_AlignmentFile = new File("./files/experiment_eswc17/alignments/" + experiment + "/Parallel_CompleteMatch_WithPriority_Subsumption.rdf");

			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(Parallel_CompleteMatch_WithPriority_Subsumption_AlignmentFile)), true); 
			AlignmentVisitor Parallel_CompleteMatch_WithPriority_Subsumption_renderer = new RDFRendererVisitor(writer);

			Parallel_CompleteMatch_WithPriority_SubsumptionAlignment.cut(threshold);

			Parallel_CompleteMatch_WithPriority_SubsumptionAlignment.render(Parallel_CompleteMatch_WithPriority_Subsumption_renderer);


			System.out.println("Matching completed!");

			evaluator = new PRecEvaluator(referenceAlignment_Parallel_CompleteMatch_WithPriority_Subsumption, Parallel_CompleteMatch_WithPriority_SubsumptionAlignment);
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

		case "Hybrid_Merge_ClassEq":

			//import the alignment files
			a1 = new File("./files/experiment_eswc17/alignments/" + experiment + "/ClassEq_String.rdf");
			a2 = new File("./files/experiment_eswc17/alignments/" + experiment + "/ClassEq_WordNet2.rdf");
			a3 = new File("./files/experiment_eswc17/alignments/" + experiment + "/ClassEq_Structural.rdf");

			//set the threshold
			threshold = 0.9;

			Alignment Hybrid_Merge_ClassEq = HybridComposition.merge(a1, a2, a3);
			Alignment referenceAlignment_Hybrid_Merge_ClassEq = aparser.parse(new URI("file:files/experiment_eswc17/alignments/" + experiment + "/referencealignment/refalign_classEq.rdf"));

			//store the alignment
			File Hybrid_Merge_ClassEq_AlignmentFile = new File("./files/experiment_eswc17/alignments/" + experiment + "/Hybrid_Merge_ClassEq.rdf");

			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(Hybrid_Merge_ClassEq_AlignmentFile)), true); 
			AlignmentVisitor Hybrid_Merge_ClassEq_renderer = new RDFRendererVisitor(writer);

			Hybrid_Merge_ClassEq.cut(threshold);

			Hybrid_Merge_ClassEq.render(Hybrid_Merge_ClassEq_renderer);


			System.out.println("Matching completed!");

			evaluator = new PRecEvaluator(referenceAlignment_Hybrid_Merge_ClassEq, Hybrid_Merge_ClassEq);
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

		case "Hybrid_Merge_PropEq":

			//import the alignment files
			a1 = new File("./files/experiment_eswc17/alignments/" + experiment + "/PropEq_String.rdf");
			a2 = new File("./files/experiment_eswc17/alignments/" + experiment + "/PropEq_WordNet2.rdf");
			a3 = new File("./files/experiment_eswc17/alignments/" + experiment + "/PropString-recall.rdf");

			//set the threshold
			threshold = 0.9;

			Alignment Hybrid_Merge_PropEq = HybridComposition.merge(a2, a1, a3);
			Alignment referenceAlignment_Hybrid_Merge_PropEq = aparser.parse(new URI("file:files/experiment_eswc17/alignments/" + experiment + "/referencealignment/refalign_PropEq.rdf"));

			//store the alignment
			File Hybrid_Merge_PropEq_AlignmentFile = new File("./files/experiment_eswc17/alignments/" + experiment + "/Hybrid_Merge_PropEq.rdf");

			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(Hybrid_Merge_PropEq_AlignmentFile)), true); 
			AlignmentVisitor Hybrid_Merge_PropEq_renderer = new RDFRendererVisitor(writer);

			Hybrid_Merge_PropEq.cut(threshold);

			Hybrid_Merge_PropEq.render(Hybrid_Merge_PropEq_renderer);


			System.out.println("Matching completed!");

			evaluator = new PRecEvaluator(referenceAlignment_Hybrid_Merge_PropEq, Hybrid_Merge_PropEq);
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

		case "Hybrid_Merge_Subsumption":

			//import the alignment files
			a1 = new File("./files/experiment_eswc17/alignments/" + experiment + "/Subsumption_String_Matcher.rdf");
			a2 = new File("./files/experiment_eswc17/alignments/" + experiment + "/Subsumption_WordNet_Matcher.rdf");
			a3 = new File("./files/experiment_eswc17/alignments/" + experiment + "/Subsumption_SubClass.rdf");

			//set the threshold
			threshold = 0.9;

			Alignment Hybrid_Merge_Subsumption = HybridComposition.merge(a1, a2, a3);
			Alignment referenceAlignment_Hybrid_Merge_Subsumption = aparser.parse(new URI("file:files/experiment_eswc17/alignments/" + experiment + "/referencealignment/refalign_Subsumption.rdf"));

			//store the alignment
			File Hybrid_Merge_Subsumption_AlignmentFile = new File("./files/experiment_eswc17/alignments/" + experiment + "/Hybrid_Merge_Subsumption.rdf");

			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(Hybrid_Merge_Subsumption_AlignmentFile)), true); 
			AlignmentVisitor Hybrid_Merge_Subsumption_renderer = new RDFRendererVisitor(writer);

			Hybrid_Merge_Subsumption.cut(threshold);

			Hybrid_Merge_Subsumption.render(Hybrid_Merge_Subsumption_renderer);


			System.out.println("Matching completed!");

			evaluator = new PRecEvaluator(referenceAlignment_Hybrid_Merge_Subsumption, Hybrid_Merge_Subsumption);
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




		}




	}
}

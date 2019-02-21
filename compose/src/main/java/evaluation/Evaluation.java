package evaluation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.TreeMap;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

//import compliancevalidator.graph.GraphCreator;
import equivalencematching.DefinitionsEquivalenceMatcher;
import equivalencematching.LexicalMatcherWordNet;
import equivalencematching.PropertyMatcher;
import equivalencematching.RangeMatcher;
import equivalencematching.StringEquivalenceMatcher;
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.eval.PRecEvaluator;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import graph.GraphCreator;
import subsumptionmatching.ClosestParentMatcher;
import subsumptionmatching.CompoundMatcher;
import subsumptionmatching.DefinitionsSubsumptionMatcher;
import subsumptionmatching.LexicalSubsumptionMatcher;
import subsumptionmatching.OppositeSubclassMatcher;
import utilities.StringUtilities;
import utilities.VectorExtractor;

public class Evaluation {

	final static String prefix = "file:";
	final static String wiki_vectorFile = "./files/_PHD_EVALUATION/EMBEDDINGS/Wikipedia_vectors_lemmatized.txt";
	final static String atm_vectorFile = "./files/_PHD_EVALUATION/EMBEDDINGS/Skybrary_vectors_lemmatized.txt";

	public static void main(String[] args) throws AlignmentException, URISyntaxException, IOException, OWLOntologyCreationException {

		// create a scanner so we can read the command-line input
		Scanner scanner = new Scanner(System.in);

		System.out.println("Starting the Evaluation Application");

		System.out.print("\nEnter path to ontology 1: ");
		String onto1Path = scanner.next();

		System.out.print("Enter path to ontology 2: ");
		String onto2Path = scanner.next();

		System.out.print("Enter path to the reference alignment: ");
		String refalignPath = scanner.next();

		System.out.print("Enter matcher weight (between 0.0 and 1.0): ");
		String weightStr = scanner.next();
		double weight = Double.parseDouble(weightStr);

		System.out.print("Enter path to folder where alignment file will be stored: ");
		String alignmentFolder = scanner.next();

		System.out.print("Enter full path to the folder where the evaluation results (Excel) should be stored: ");
		String evalPath = scanner.next();

		final File onto1 = new File(onto1Path);
		final File onto2 = new File(onto2Path);

		System.out.print("\nSelect Equivalence (1), Subsumption (2): ");

		String relationType = scanner.next();

		String matcher = null;

		if (relationType.equals("1")) {

			System.out.println("\nEQ1: String Equivalence Matcher");
			System.out.println("EQ2: Definitions Equivalence Matcher");
			System.out.println("EQ3: Property Matcher");
			System.out.println("EQ4: Range Matcher");
			System.out.println("EQ5: Lexical Equivalence Matcher");
			System.out.println("ALL-EQ: All Equivalence Matchers");
			System.out.print("\nSelect matcher configuration: ");

			matcher = scanner.next();


		} else if (relationType.equals("2")) {

			System.out.println("\nSUB1: Closest Parent Subsumption Matcher");
			System.out.println("SUB2: Compound Subsumption Matcher");
			System.out.println("SUB3: Definitions Subsumption Matcher");
			System.out.println("SUB4: Lexical Subsumption Matcher");
			System.out.println("SUB5: Opposite Subclass Subsumption Matcher");
			System.out.println("ALL-SUB: All Subsumption Matchers");
			System.out.print("\nSelect matcher configuration: ");

			matcher = scanner.next();
		}

		System.out.print("\nType confidence threshold (or type 'ALL' for all thresholds):");
		String conf = scanner.next();
		
		System.out.print("Choose vectors from Wikipedia (WIKI) or Skybrary (ATM): ");
		String vectorFile = scanner.next();

		switch(matcher) {

		case "EQ1":
			runStringEquivalenceMatcher(onto1, onto2, conf, weight, refalignPath, alignmentFolder, evalPath);
			break;

		case "EQ2":

			if (vectorFile.equals("WIKI")) {
				runDefinitionsEquivalenceMatcher(onto1, onto2, conf, weight, wiki_vectorFile, refalignPath, alignmentFolder, evalPath);
			} else if (vectorFile.equals("ATM")) {
				runDefinitionsEquivalenceMatcher(onto1, onto2, conf, weight, atm_vectorFile, refalignPath, alignmentFolder, evalPath);
			} else {
				System.out.println("You didn´t select 'WIKI' nor 'ATM'");
			}


			break;

		case "EQ3":
			runPropertyEquivalenceMatcher(onto1, onto2, conf, weight, refalignPath, alignmentFolder, evalPath);
			break;

		case "EQ4":
			runRangeEquivalenceMatcher(onto1, onto2, conf, weight, refalignPath, alignmentFolder, evalPath);
			break;

		case "EQ5":
			runLexicalEquivalenceMatcherWordNet(onto1, onto2, conf, weight, refalignPath, alignmentFolder, evalPath);
			break;

		case "ALL-EQ":
			runStringEquivalenceMatcher(onto1, onto2, conf, weight, refalignPath, alignmentFolder, evalPath);
			if (vectorFile.equals("WIKI")) {
				runDefinitionsEquivalenceMatcher(onto1, onto2, conf, weight, wiki_vectorFile, refalignPath, alignmentFolder, evalPath);
			} else if (vectorFile.equals("ATM")) {
				runDefinitionsEquivalenceMatcher(onto1, onto2, conf, weight, atm_vectorFile, refalignPath, alignmentFolder, evalPath);
			} else {
				System.out.println("You didn´t select 'WIKI' nor 'ATM'");
			}
			runPropertyEquivalenceMatcher(onto1, onto2, conf, weight, refalignPath, alignmentFolder, evalPath);
			runRangeEquivalenceMatcher(onto1, onto2, conf, weight, refalignPath, alignmentFolder, evalPath);
			runLexicalEquivalenceMatcherWordNet(onto1, onto2, conf, weight, refalignPath, alignmentFolder, evalPath);

		case "SUB1":
			runClosestParentSubsumptionMatcher(onto1, onto2, conf, weight, refalignPath, alignmentFolder, evalPath);
			break;

		case "SUB2":
			runCompoundSubsumptionMatcher(onto1, onto2, conf, weight, refalignPath, alignmentFolder, evalPath);
			break;

		case "SUB3":
			runDefinitionsSubsumptionMatcher(onto1, onto2, conf, weight, refalignPath, alignmentFolder, evalPath);
			break;			

		case "SUB4":
			runLexicalSubsumptionMatcher(onto1, onto2, conf, weight, refalignPath, alignmentFolder, evalPath);
			break;	

		case "SUB5":
			runOppositeSubclassSubsumptionMatcher(onto1, onto2, conf, weight, refalignPath, alignmentFolder, evalPath);
			break;	

		case "ALL-SUB":
			runClosestParentSubsumptionMatcher(onto1, onto2, conf, weight, refalignPath, alignmentFolder, evalPath);
			runCompoundSubsumptionMatcher(onto1, onto2, conf, weight, refalignPath, alignmentFolder, evalPath);
			runDefinitionsSubsumptionMatcher(onto1, onto2, conf, weight, refalignPath, alignmentFolder, evalPath);
			runLexicalSubsumptionMatcher(onto1, onto2, conf, weight, refalignPath, alignmentFolder, evalPath);
			runOppositeSubclassSubsumptionMatcher(onto1, onto2, conf, weight, refalignPath, alignmentFolder, evalPath);
			break;	

		}

	}

	private static void runStringEquivalenceMatcher(File onto1, File onto2, String conf, double weight, String refalignPath, String alignmentFolder, String evaluationFilePath) throws AlignmentException, URISyntaxException, IOException {
		System.out.println("Starting String Equivalence Matcher");
		long startTime = System.currentTimeMillis();

		PrintWriter writer = null;
		AlignmentVisitor renderer = null;	
		BasicAlignment evaluatedAlignment = null;	
		Properties params = new Properties();
		String alignmentFileName = null;
		File outputAlignment = null;
		Properties p = new Properties();
		AlignmentParser aparser = new AlignmentParser(0);
		Alignment referenceAlignment = aparser.parse(new URI(StringUtilities.convertToFileURL(refalignPath)));

		AlignmentProcess a = new StringEquivalenceMatcher(weight);

		System.out.println("Matching " + onto1 + " and " + onto2 );
		a.init( new URI(prefix.concat(onto1.toString().substring(2))), new URI(prefix.concat(onto2.toString().substring(2))));
		params = new Properties();
		params.setProperty("", "");
		a.align((Alignment)null, params);	

		//create an output file for all confidence thresholds [0.1, 0.2...0.9]
		if (conf.equals("ALL")) {

			double[] confidences = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
			double precision = 0;
			double recall = 0;
			double fMeasure = 0;
			PRecEvaluator eval = null;
			Map<String, EvaluationScore> evaluationMap = new TreeMap<String, EvaluationScore>();


			for (int i = 0; i < confidences.length; i++) {
				EvaluationScore evalScore = new EvaluationScore();
				alignmentFileName = alignmentFolder + "/" + StringUtilities.stripOntologyName(onto1.toString()) + 
						"-" + StringUtilities.stripOntologyName(onto2.toString()) + "-StringEquivalenceMatcher"+confidences[i]+".rdf";

				outputAlignment = new File(alignmentFileName);


				writer = new PrintWriter(
						new BufferedWriter(
								new FileWriter(outputAlignment)), true); 
				renderer = new RDFRendererVisitor(writer);

				evaluatedAlignment = (BasicAlignment)(a.clone());
				evaluatedAlignment.normalise();
				evaluatedAlignment.cut(confidences[i]);

				//perform the evaluation here...				
				eval = new PRecEvaluator(referenceAlignment, evaluatedAlignment);

				eval.eval(p);

				precision = Double.valueOf(eval.getResults().getProperty("precision").toString());
				recall = Double.valueOf(eval.getResults().getProperty("recall").toString());
				fMeasure = Double.valueOf(eval.getResults().getProperty("fmeasure").toString());

				evalScore.setPrecision(precision);
				evalScore.setRecall(recall);
				evalScore.setfMeasure(fMeasure);
				evaluationMap.put(String.valueOf(confidences[i]), evalScore);

				evaluatedAlignment.render(renderer);
				writer.flush();
				writer.close();

			}

			Evaluator.evaluateSingleMatcherThresholds(evaluationMap, evaluationFilePath+"/StringEquivalenceMatcher");

			//or if only a single confidence is requested...
		} else {

			alignmentFileName = alignmentFolder + "/" + StringUtilities.stripOntologyName(onto1.toString()) + 
					"-" + StringUtilities.stripOntologyName(onto2.toString()) + "-StringEquivalenceMatcher"+conf+".rdf";

			outputAlignment = new File(alignmentFileName);

			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(outputAlignment)), true); 
			renderer = new RDFRendererVisitor(writer);

			evaluatedAlignment = (BasicAlignment)(a.clone());

			evaluatedAlignment.normalise();

			evaluatedAlignment.cut(Double.parseDouble(conf));

			evaluatedAlignment.render(renderer);
			writer.flush();
			writer.close();

		}

		long estimatedTime = ((System.currentTimeMillis() - startTime)/1000)/60;
		System.out.println("\nString Equivalence Matcher completed in " + estimatedTime + " minutes!");
	}

	private static void runLexicalEquivalenceMatcherWordNet(File onto1, File onto2, String conf, double weight, String refalignPath, String alignmentFolder, String evaluationFilePath) throws AlignmentException, URISyntaxException, IOException {
		System.out.println("Starting Lexical Equivalence Matcher");
		long startTime = System.currentTimeMillis();

		PrintWriter writer = null;
		AlignmentVisitor renderer = null;	
		BasicAlignment evaluatedAlignment = null;	
		Properties params = new Properties();
		String alignmentFileName = null;
		File outputAlignment = null;
		Properties p = new Properties();
		AlignmentParser aparser = new AlignmentParser(0);
		Alignment referenceAlignment = aparser.parse(new URI(StringUtilities.convertToFileURL(refalignPath)));

		AlignmentProcess a = new LexicalMatcherWordNet(weight);

		System.out.println("Matching " + onto1 + " and " + onto2 );
		a.init( new URI(prefix.concat(onto1.toString().substring(2))), new URI(prefix.concat(onto2.toString().substring(2))));
		params = new Properties();
		params.setProperty("", "");
		a.align((Alignment)null, params);	

		//create an output file for all confidence thresholds [0.1, 0.2...0.9]
		if (conf.equals("ALL")) {

			double[] confidences = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
			double precision = 0;
			double recall = 0; 
			double fMeasure = 0;
			PRecEvaluator eval = null;
			Map<String, EvaluationScore> evaluationMap = new TreeMap<String, EvaluationScore>();


			for (int i = 0; i < confidences.length; i++) {
				EvaluationScore evalScore = new EvaluationScore();
				alignmentFileName = alignmentFolder + "/" + StringUtilities.stripOntologyName(onto1.toString()) + 
						"-" + StringUtilities.stripOntologyName(onto2.toString()) + "-LexicalEquivalenceMatcher"+confidences[i]+".rdf";

				outputAlignment = new File(alignmentFileName);

				writer = new PrintWriter(
						new BufferedWriter(
								new FileWriter(outputAlignment)), true); 
				renderer = new RDFRendererVisitor(writer);

				evaluatedAlignment = (BasicAlignment)(a.clone());
				evaluatedAlignment.normalise();
				evaluatedAlignment.cut(confidences[i]);

				//perform the evaluation here...
				eval = new PRecEvaluator(referenceAlignment, evaluatedAlignment);
				eval.eval(p);

				precision = Double.valueOf(eval.getResults().getProperty("precision").toString());
				recall = Double.valueOf(eval.getResults().getProperty("recall").toString());
				fMeasure = Double.valueOf(eval.getResults().getProperty("fmeasure").toString());

				evalScore.setPrecision(precision);
				evalScore.setRecall(recall);
				evalScore.setfMeasure(fMeasure);
				evaluationMap.put(String.valueOf(confidences[i]), evalScore);			

				evaluatedAlignment.render(renderer);
				writer.flush();
				writer.close();

			}
			Evaluator.evaluateSingleMatcherThresholds(evaluationMap, evaluationFilePath+"/LexicalEquivalenceMatcher");

			//or if only a single confidence is requested...
		} else {

			alignmentFileName = alignmentFolder + "/" + StringUtilities.stripOntologyName(onto1.toString()) + 
					"-" + StringUtilities.stripOntologyName(onto2.toString()) + "-LexicalEquivalenceMatcher"+conf+".rdf";

			outputAlignment = new File(alignmentFileName);

			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(outputAlignment)), true); 
			renderer = new RDFRendererVisitor(writer);

			evaluatedAlignment = (BasicAlignment)(a.clone());

			evaluatedAlignment.normalise();

			evaluatedAlignment.cut(Double.parseDouble(conf));

			evaluatedAlignment.render(renderer);
			writer.flush();
			writer.close();

		}


		long estimatedTime = ((System.currentTimeMillis() - startTime)/1000)/60;
		System.out.println("\nLexical Equivalence Matcher (WordNet) completed in " + estimatedTime + " minutes!");
	}



	private static void runRangeEquivalenceMatcher(File ontoFile1, File ontoFile2, String conf, double weight, String refalignPath, String alignmentFolder, String evaluationFilePath) throws AlignmentException, URISyntaxException, IOException, OWLOntologyCreationException {
		System.out.println("Starting Range Equivalence Matcher");
		long startTime = System.currentTimeMillis();

		PrintWriter writer = null;
		AlignmentVisitor renderer = null;	
		BasicAlignment evaluatedAlignment = null;	
		Properties params = new Properties();
		String alignmentFileName = null;
		File outputAlignment = null;
		Properties p = new Properties();
		AlignmentParser aparser = new AlignmentParser(0);
		Alignment referenceAlignment = aparser.parse(new URI(StringUtilities.convertToFileURL(refalignPath)));

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

		OWLOntology onto1 = manager.loadOntologyFromOntologyDocument(ontoFile1);
		OWLOntology onto2 = manager.loadOntologyFromOntologyDocument(ontoFile2);

		AlignmentProcess a = new RangeMatcher(onto1, onto2, weight);

		System.out.println("Matching " + onto1 + " and " + onto2 );
		a.init( new URI(prefix.concat(ontoFile1.toString().substring(2))), new URI(prefix.concat(ontoFile2.toString().substring(2))));
		params = new Properties();
		params.setProperty("", "");
		a.align((Alignment)null, params);	

		//create an output file for all confidence thresholds [0.1, 0.2...0.9]
		if (conf.equals("ALL")) {

			double[] confidences = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
			double precision = 0;
			double recall = 0; 
			double fMeasure = 0;
			PRecEvaluator eval = null;
			Map<String, EvaluationScore> evaluationMap = new TreeMap<String, EvaluationScore>();


			for (int i = 0; i < confidences.length; i++) {
				EvaluationScore evalScore = new EvaluationScore();
				alignmentFileName = alignmentFolder + "/" + StringUtilities.stripOntologyName(ontoFile1.toString()) + 
						"-" + StringUtilities.stripOntologyName(ontoFile2.toString()) + "-RangeEquivalenceMatcher"+confidences[i]+".rdf";

				outputAlignment = new File(alignmentFileName);

				writer = new PrintWriter(
						new BufferedWriter(
								new FileWriter(outputAlignment)), true); 
				renderer = new RDFRendererVisitor(writer);

				evaluatedAlignment = (BasicAlignment)(a.clone());
				evaluatedAlignment.normalise();
				evaluatedAlignment.cut(confidences[i]);

				//perform the evaluation here...
				eval = new PRecEvaluator(referenceAlignment, evaluatedAlignment);
				eval.eval(p);

				precision = Double.valueOf(eval.getResults().getProperty("precision").toString());
				recall = Double.valueOf(eval.getResults().getProperty("recall").toString());
				fMeasure = Double.valueOf(eval.getResults().getProperty("fmeasure").toString());

				evalScore.setPrecision(precision);
				evalScore.setRecall(recall);
				evalScore.setfMeasure(fMeasure);
				evaluationMap.put(String.valueOf(confidences[i]), evalScore);			

				evaluatedAlignment.render(renderer);
				writer.flush();
				writer.close();

			}
			Evaluator.evaluateSingleMatcherThresholds(evaluationMap, evaluationFilePath+"/RangeEquivalenceMatcher");

			//or if only a single confidence is requested...
		} else {

			alignmentFileName = alignmentFolder + "/" + StringUtilities.stripOntologyName(onto1.toString()) + 
					"-" + StringUtilities.stripOntologyName(onto2.toString()) + "-RangeEquivalenceMatcher"+conf+".rdf";

			outputAlignment = new File(alignmentFileName);

			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(outputAlignment)), true); 
			renderer = new RDFRendererVisitor(writer);

			evaluatedAlignment = (BasicAlignment)(a.clone());

			evaluatedAlignment.normalise();

			evaluatedAlignment.cut(Double.parseDouble(conf));

			evaluatedAlignment.render(renderer);
			writer.flush();
			writer.close();

		}

		long estimatedTime = ((System.currentTimeMillis() - startTime)/1000)/60;
		System.out.println("\nRange Equivalence Matcher completed in " + estimatedTime + " minutes!");
	}

	private static void runDefinitionsEquivalenceMatcher(File ontoFile1, File ontoFile2, String conf, double weight, String vectorFile, String refalignPath, String alignmentFolder, String evaluationFilePath) throws AlignmentException, URISyntaxException, IOException, OWLOntologyCreationException {
		System.out.println("Starting Definitions Equivalence Matcher");
		long startTime = System.currentTimeMillis();

		PrintWriter writer = null;
		AlignmentVisitor renderer = null;	
		BasicAlignment evaluatedAlignment = null;	
		Properties params = new Properties();
		String alignmentFileName = null;
		File outputAlignment = null;
		Properties p = new Properties();
		AlignmentParser aparser = new AlignmentParser(0);
		Alignment referenceAlignment = aparser.parse(new URI(StringUtilities.convertToFileURL(refalignPath)));

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto1 = manager.loadOntologyFromOntologyDocument(ontoFile1);
		OWLOntology onto2 = manager.loadOntologyFromOntologyDocument(ontoFile2);

		Map<String, ArrayList<Double>> wordAndVecMap = new HashMap<String, ArrayList<Double>>();
		

		if (vectorFile.equals("WIKI")) {
			wordAndVecMap = VectorExtractor.createVectorMap(new File(wiki_vectorFile));
		} else {
			wordAndVecMap = VectorExtractor.createVectorMap(new File(atm_vectorFile));
		}

		AlignmentProcess a = new DefinitionsEquivalenceMatcher(onto1, onto2, wordAndVecMap, weight);

		System.out.println("Matching " + onto1 + " and " + onto2 );

		a.init( new URI(prefix.concat(ontoFile1.toString().substring(2))), new URI(prefix.concat(ontoFile2.toString().substring(2))));
		params = new Properties();
		params.setProperty("", "");
		a.align((Alignment)null, params);	

		//create an output file for all confidence thresholds [0.1, 0.2...0.9]
		if (conf.equals("ALL")) {

			double[] confidences = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
			double precision = 0;
			double recall = 0; 
			double fMeasure = 0;
			PRecEvaluator eval = null;
			Map<String, EvaluationScore> evaluationMap = new TreeMap<String, EvaluationScore>();


			for (int i = 0; i < confidences.length; i++) {
				EvaluationScore evalScore = new EvaluationScore();
				alignmentFileName = alignmentFolder + "/" + StringUtilities.stripOntologyName(ontoFile1.toString()) + 
						"-" + StringUtilities.stripOntologyName(ontoFile2.toString()) + "-DefinitionsEquivalenceMatcher"+confidences[i]+".rdf";

				outputAlignment = new File(alignmentFileName);

				writer = new PrintWriter(
						new BufferedWriter(
								new FileWriter(outputAlignment)), true); 
				renderer = new RDFRendererVisitor(writer);

				evaluatedAlignment = (BasicAlignment)(a.clone());
				evaluatedAlignment.normalise();
				evaluatedAlignment.cut(confidences[i]);

				//perform the evaluation here...
				eval = new PRecEvaluator(referenceAlignment, evaluatedAlignment);
				eval.eval(p);

				precision = Double.valueOf(eval.getResults().getProperty("precision").toString());
				recall = Double.valueOf(eval.getResults().getProperty("recall").toString());
				fMeasure = Double.valueOf(eval.getResults().getProperty("fmeasure").toString());

				evalScore.setPrecision(precision);
				evalScore.setRecall(recall);
				evalScore.setfMeasure(fMeasure);
				evaluationMap.put(String.valueOf(confidences[i]), evalScore);			

				evaluatedAlignment.render(renderer);
				writer.flush();
				writer.close();

			}
			Evaluator.evaluateSingleMatcherThresholds(evaluationMap, evaluationFilePath+"/DefinitionsEquivalenceMatcher");

			//or if only a single confidence is requested...
		} else {

			alignmentFileName = alignmentFolder + "/" + StringUtilities.stripOntologyName(onto1.toString()) + 
					"-" + StringUtilities.stripOntologyName(onto2.toString()) + "-DefinitionsEquivalenceMatcher"+conf+".rdf";

			outputAlignment = new File(alignmentFileName);

			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(outputAlignment)), true); 
			renderer = new RDFRendererVisitor(writer);

			evaluatedAlignment = (BasicAlignment)(a.clone());

			evaluatedAlignment.normalise();

			evaluatedAlignment.cut(Double.parseDouble(conf));

			evaluatedAlignment.render(renderer);
			writer.flush();
			writer.close();

		}

		long estimatedTime = ((System.currentTimeMillis() - startTime)/1000)/60;
		System.out.println("\nDefinitions Equivalence Matcher completed in " + estimatedTime + " minutes!");
	}

	private static void runDefinitionsSubsumptionMatcher(File onto1, File onto2, String conf, double weight, String refalignPath, String alignmentFolder, String evaluationFilePath) throws AlignmentException, URISyntaxException, IOException {
		System.out.println("Starting Definitions Subsumption Matcher");
		long startTime = System.currentTimeMillis();

		PrintWriter writer = null;
		AlignmentVisitor renderer = null;	
		BasicAlignment evaluatedAlignment = null;	
		Properties params = new Properties();
		String alignmentFileName = null;
		File outputAlignment = null;
		Properties p = new Properties();
		AlignmentParser aparser = new AlignmentParser(0);
		Alignment referenceAlignment = aparser.parse(new URI(StringUtilities.convertToFileURL(refalignPath)));

		AlignmentProcess a = new DefinitionsSubsumptionMatcher(weight);

		System.out.println("Matching " + onto1 + " and " + onto2 );
		a.init( new URI(prefix.concat(onto1.toString().substring(2))), new URI(prefix.concat(onto2.toString().substring(2))));
		params = new Properties();
		params.setProperty("", "");
		a.align((Alignment)null, params);	

		//create an output file for all confidence thresholds [0.1, 0.2...0.9]
		if (conf.equals("ALL")) {

			double[] confidences = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
			double precision = 0;
			double recall = 0; 
			double fMeasure = 0;
			PRecEvaluator eval = null;
			Map<String, EvaluationScore> evaluationMap = new TreeMap<String, EvaluationScore>();


			for (int i = 0; i < confidences.length; i++) {
				EvaluationScore evalScore = new EvaluationScore();
				alignmentFileName = alignmentFolder + "/" + StringUtilities.stripOntologyName(onto1.toString()) + 
						"-" + StringUtilities.stripOntologyName(onto2.toString()) + "-DefinitionsSubsumptionMatcher"+confidences[i]+".rdf";

				outputAlignment = new File(alignmentFileName);

				writer = new PrintWriter(
						new BufferedWriter(
								new FileWriter(outputAlignment)), true); 
				renderer = new RDFRendererVisitor(writer);

				evaluatedAlignment = (BasicAlignment)(a.clone());
				evaluatedAlignment.normalise();
				evaluatedAlignment.cut(confidences[i]);

				//perform the evaluation here...
				eval = new PRecEvaluator(referenceAlignment, evaluatedAlignment);
				eval.eval(p);

				precision = Double.valueOf(eval.getResults().getProperty("precision").toString());
				recall = Double.valueOf(eval.getResults().getProperty("recall").toString());
				fMeasure = Double.valueOf(eval.getResults().getProperty("fmeasure").toString());

				evalScore.setPrecision(precision);
				evalScore.setRecall(recall);
				evalScore.setfMeasure(fMeasure);
				evaluationMap.put(String.valueOf(confidences[i]), evalScore);			

				evaluatedAlignment.render(renderer);
				writer.flush();
				writer.close();

			}
			Evaluator.evaluateSingleMatcherThresholds(evaluationMap, evaluationFilePath+"/DefinitionsSubsumptionMatcher");

			//or if only a single confidence is requested...
		} else {

			alignmentFileName = alignmentFolder + "/" + StringUtilities.stripOntologyName(onto1.toString()) + 
					"-" + StringUtilities.stripOntologyName(onto2.toString()) + "-DefinitionsSubsumptionMatcher"+conf+".rdf";

			outputAlignment = new File(alignmentFileName);

			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(outputAlignment)), true); 
			renderer = new RDFRendererVisitor(writer);

			evaluatedAlignment = (BasicAlignment)(a.clone());

			evaluatedAlignment.normalise();

			evaluatedAlignment.cut(Double.parseDouble(conf));

			evaluatedAlignment.render(renderer);
			writer.flush();
			writer.close();

		}

		long estimatedTime = ((System.currentTimeMillis() - startTime)/1000)/60;
		System.out.println("\nDefinitions Subsumption Matcher completed in " + estimatedTime + " minutes!");
	}

	private static void runPropertyEquivalenceMatcher(File ontoFile1, File ontoFile2, String conf, double weight, String refalignPath, String alignmentFolder, String evaluationFilePath) throws AlignmentException, URISyntaxException, IOException, OWLOntologyCreationException {
		System.out.println("Starting Property Equivalence Matcher");
		long startTime = System.currentTimeMillis();

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

		OWLOntology onto1 = manager.loadOntologyFromOntologyDocument(ontoFile1);
		OWLOntology onto2 = manager.loadOntologyFromOntologyDocument(ontoFile2);

		PrintWriter writer = null;
		AlignmentVisitor renderer = null;	
		BasicAlignment evaluatedAlignment = null;	
		Properties params = new Properties();
		String alignmentFileName = null;
		File outputAlignment = null;
		Properties p = new Properties();
		AlignmentParser aparser = new AlignmentParser(0);
		Alignment referenceAlignment = aparser.parse(new URI(StringUtilities.convertToFileURL(refalignPath)));

		AlignmentProcess a = new PropertyMatcher(onto1, onto2, weight);

		System.out.println("Matching " + onto1 + " and " + onto2 );
		a.init( new URI(prefix.concat(ontoFile1.toString().substring(2))), new URI(prefix.concat(ontoFile2.toString().substring(2))));
		params = new Properties();
		params.setProperty("", "");
		a.align((Alignment)null, params);	

		//create an output file for all confidence thresholds [0.1, 0.2...0.9]
		if (conf.equals("ALL")) {

			double[] confidences = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
			double precision = 0;
			double recall = 0; 
			double fMeasure = 0;
			PRecEvaluator eval = null;
			Map<String, EvaluationScore> evaluationMap = new TreeMap<String, EvaluationScore>();


			for (int i = 0; i < confidences.length; i++) {
				EvaluationScore evalScore = new EvaluationScore();
				alignmentFileName = alignmentFolder + "/" + StringUtilities.stripOntologyName(ontoFile1.toString()) + 
						"-" + StringUtilities.stripOntologyName(ontoFile2.toString()) + "-PropertyEquivalenceMatcher"+confidences[i]+".rdf";

				outputAlignment = new File(alignmentFileName);

				writer = new PrintWriter(
						new BufferedWriter(
								new FileWriter(outputAlignment)), true); 
				renderer = new RDFRendererVisitor(writer);

				evaluatedAlignment = (BasicAlignment)(a.clone());
				evaluatedAlignment.normalise();
				evaluatedAlignment.cut(confidences[i]);

				//perform the evaluation here...
				eval = new PRecEvaluator(referenceAlignment, evaluatedAlignment);
				eval.eval(p);

				precision = Double.valueOf(eval.getResults().getProperty("precision").toString());
				recall = Double.valueOf(eval.getResults().getProperty("recall").toString());
				fMeasure = Double.valueOf(eval.getResults().getProperty("fmeasure").toString());

				evalScore.setPrecision(precision);
				evalScore.setRecall(recall);
				evalScore.setfMeasure(fMeasure);
				evaluationMap.put(String.valueOf(confidences[i]), evalScore);			

				evaluatedAlignment.render(renderer);
				writer.flush();
				writer.close();

			}
			Evaluator.evaluateSingleMatcherThresholds(evaluationMap, evaluationFilePath+"/PropertyEquivalenceMatcher");

			//or if only a single confidence is requested...
		} else {

			alignmentFileName = alignmentFolder + "/" + StringUtilities.stripOntologyName(onto1.toString()) + 
					"-" + StringUtilities.stripOntologyName(onto2.toString()) + "-PropertyEquivalenceMatcher"+conf+".rdf";

			outputAlignment = new File(alignmentFileName);

			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(outputAlignment)), true); 
			renderer = new RDFRendererVisitor(writer);

			evaluatedAlignment = (BasicAlignment)(a.clone());

			evaluatedAlignment.normalise();

			evaluatedAlignment.cut(Double.parseDouble(conf));

			evaluatedAlignment.render(renderer);
			writer.flush();
			writer.close();

		}

		long estimatedTime = ((System.currentTimeMillis() - startTime)/1000)/60;
		System.out.println("\nProperty Equivalence Matcher completed in " + estimatedTime + " minutes!");
	}


	private static void runCompoundSubsumptionMatcher(File onto1, File onto2, String conf, double weight, String refalignPath, String alignmentFolder, String evaluationFilePath) throws AlignmentException, URISyntaxException, IOException {
		System.out.println("Starting Compound Subsumption Matcher");
		long startTime = System.currentTimeMillis();

		PrintWriter writer = null;
		AlignmentVisitor renderer = null;	
		BasicAlignment evaluatedAlignment = null;	
		Properties params = new Properties();
		String alignmentFileName = null;
		File outputAlignment = null;
		Properties p = new Properties();
		AlignmentParser aparser = new AlignmentParser(0);
		Alignment referenceAlignment = aparser.parse(new URI(StringUtilities.convertToFileURL(refalignPath)));

		AlignmentProcess a = new CompoundMatcher(weight);

		System.out.println("Matching " + onto1 + " and " + onto2 );
		System.out.println("Init with " + prefix.concat(onto1.toString().substring(2)) + " and " +  prefix.concat(onto2.toString().substring(2)));
		a.init( new URI(prefix.concat(onto1.toString().substring(2))), new URI(prefix.concat(onto2.toString().substring(2))));
		params = new Properties();
		params.setProperty("", "");
		a.align((Alignment)null, params);	

		//create an output file for all confidence thresholds [0.1, 0.2...0.9]
		if (conf.equals("ALL")) {

			double[] confidences = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
			double precision = 0;
			double recall = 0; 
			double fMeasure = 0;
			PRecEvaluator eval = null;
			Map<String, EvaluationScore> evaluationMap = new TreeMap<String, EvaluationScore>();


			for (int i = 0; i < confidences.length; i++) {
				EvaluationScore evalScore = new EvaluationScore();
				alignmentFileName = alignmentFolder + "/" + StringUtilities.stripOntologyName(onto1.toString()) + 
						"-" + StringUtilities.stripOntologyName(onto2.toString()) + "-CompoundSubsumptionMatcher"+confidences[i]+".rdf";

				outputAlignment = new File(alignmentFileName);

				writer = new PrintWriter(
						new BufferedWriter(
								new FileWriter(outputAlignment)), true); 
				renderer = new RDFRendererVisitor(writer);

				evaluatedAlignment = (BasicAlignment)(a.clone());
				evaluatedAlignment.normalise();
				evaluatedAlignment.cut(confidences[i]);

				//perform the evaluation here...
				eval = new PRecEvaluator(referenceAlignment, evaluatedAlignment);
				eval.eval(p);

				precision = Double.valueOf(eval.getResults().getProperty("precision").toString());
				recall = Double.valueOf(eval.getResults().getProperty("recall").toString());
				fMeasure = Double.valueOf(eval.getResults().getProperty("fmeasure").toString());

				evalScore.setPrecision(precision);
				evalScore.setRecall(recall);
				evalScore.setfMeasure(fMeasure);
				evaluationMap.put(String.valueOf(confidences[i]), evalScore);			

				evaluatedAlignment.render(renderer);
				writer.flush();
				writer.close();

			}
			Evaluator.evaluateSingleMatcherThresholds(evaluationMap, evaluationFilePath+"/CompoundSubsumptionMatcher");

			//or if only a single confidence is requested...
		} else {

			alignmentFileName = alignmentFolder + "/" + StringUtilities.stripOntologyName(onto1.toString()) + 
					"-" + StringUtilities.stripOntologyName(onto2.toString()) + "-CompoundSubsumptionMatcher"+conf+".rdf";

			outputAlignment = new File(alignmentFileName);

			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(outputAlignment)), true); 
			renderer = new RDFRendererVisitor(writer);

			evaluatedAlignment = (BasicAlignment)(a.clone());

			evaluatedAlignment.normalise();

			evaluatedAlignment.cut(Double.parseDouble(conf));

			evaluatedAlignment.render(renderer);
			writer.flush();
			writer.close();
		}

		long estimatedTime = ((System.currentTimeMillis() - startTime)/1000)/60;
		System.out.println("\nCompound Subsumption Matcher completed in " + estimatedTime + " minutes!");
	}

	private static void runLexicalSubsumptionMatcher(File onto1, File onto2, String conf, double weight, String refalignPath, String alignmentFolder, String evaluationFilePath) throws AlignmentException, URISyntaxException, IOException {
		System.out.println("Starting Lexical Subsumption Matcher");
		long startTime = System.currentTimeMillis();

		PrintWriter writer = null;
		AlignmentVisitor renderer = null;	
		BasicAlignment evaluatedAlignment = null;	
		Properties params = new Properties();
		String alignmentFileName = null;
		File outputAlignment = null;
		Properties p = new Properties();
		AlignmentParser aparser = new AlignmentParser(0);
		Alignment referenceAlignment = aparser.parse(new URI(StringUtilities.convertToFileURL(refalignPath)));

		AlignmentProcess a = new LexicalSubsumptionMatcher(weight);

		System.out.println("Matching " + onto1 + " and " + onto2 );
		System.out.println("Init with " + prefix.concat(onto1.toString().substring(2)) + " and " +  prefix.concat(onto2.toString().substring(2)));
		a.init( new URI(prefix.concat(onto1.toString().substring(2))), new URI(prefix.concat(onto2.toString().substring(2))));
		params = new Properties();
		params.setProperty("", "");
		a.align((Alignment)null, params);	

		//create an output file for all confidence thresholds [0.1, 0.2...0.9]
		if (conf.equals("ALL")) {

			double[] confidences = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
			double precision = 0;
			double recall = 0; 
			double fMeasure = 0;
			PRecEvaluator eval = null;
			Map<String, EvaluationScore> evaluationMap = new TreeMap<String, EvaluationScore>();


			for (int i = 0; i < confidences.length; i++) {
				EvaluationScore evalScore = new EvaluationScore();
				alignmentFileName = alignmentFolder + "/" + StringUtilities.stripOntologyName(onto1.toString()) + 
						"-" + StringUtilities.stripOntologyName(onto2.toString()) + "-LexicalSubsumptionMatcher"+confidences[i]+".rdf";

				outputAlignment = new File(alignmentFileName);

				writer = new PrintWriter(
						new BufferedWriter(
								new FileWriter(outputAlignment)), true); 
				renderer = new RDFRendererVisitor(writer);

				evaluatedAlignment = (BasicAlignment)(a.clone());
				evaluatedAlignment.normalise();
				evaluatedAlignment.cut(confidences[i]);

				//perform the evaluation here...
				eval = new PRecEvaluator(referenceAlignment, evaluatedAlignment);
				eval.eval(p);

				precision = Double.valueOf(eval.getResults().getProperty("precision").toString());
				recall = Double.valueOf(eval.getResults().getProperty("recall").toString());
				fMeasure = Double.valueOf(eval.getResults().getProperty("fmeasure").toString());

				evalScore.setPrecision(precision);
				evalScore.setRecall(recall);
				evalScore.setfMeasure(fMeasure);
				evaluationMap.put(String.valueOf(confidences[i]), evalScore);			

				evaluatedAlignment.render(renderer);
				writer.flush();
				writer.close();

			}
			Evaluator.evaluateSingleMatcherThresholds(evaluationMap, evaluationFilePath+"/LexicalSubsumptionMatcher");

			//or if only a single confidence is requested...
		} else {

			alignmentFileName = alignmentFolder + "/" + StringUtilities.stripOntologyName(onto1.toString()) + 
					"-" + StringUtilities.stripOntologyName(onto2.toString()) + "-LexicalSubsumptionMatcher"+conf+".rdf";

			outputAlignment = new File(alignmentFileName);

			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(outputAlignment)), true); 
			renderer = new RDFRendererVisitor(writer);

			evaluatedAlignment = (BasicAlignment)(a.clone());

			evaluatedAlignment.normalise();

			evaluatedAlignment.cut(Double.parseDouble(conf));

			evaluatedAlignment.render(renderer);
			writer.flush();
			writer.close();

		}

		long estimatedTime = ((System.currentTimeMillis() - startTime)/1000)/60;
		System.out.println("\nLexical Subsumption Matcher completed in " + estimatedTime + " minutes!");
	}

	private static void runOppositeSubclassSubsumptionMatcher(File ontoFile1, File ontoFile2, String conf, double weight, String refalignPath, String alignmentFolder, String evaluationFilePath) throws AlignmentException, URISyntaxException, IOException, OWLOntologyCreationException {
		System.out.println("Starting Opposite Subclass Subsumption Matcher");
		long startTime = System.currentTimeMillis();

		PrintWriter writer = null;
		AlignmentVisitor renderer = null;	
		BasicAlignment evaluatedAlignment = null;

		Properties params = new Properties();
		String ontologyParameter1 = null;
		String ontologyParameter2 = null;	
		GraphCreator creator = null;
		OWLOntologyManager manager = null;
		OWLOntology onto1 = null;
		OWLOntology onto2 = null;
		Label labelO1 = null;
		Label labelO2 = null;
		String alignmentFileName = null;
		File outputAlignment = null;
		Properties p = new Properties();
		AlignmentParser aparser = new AlignmentParser(0);
		Alignment referenceAlignment = aparser.parse(new URI(StringUtilities.convertToFileURL(refalignPath)));

		//create a new instance of the neo4j database in each run
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		String dbName = String.valueOf(timestamp.getTime());
		File dbFile = new File("/Users/audunvennesland/Documents/phd/development/Neo4J_new/" + dbName);				
		GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(dbFile);
		registerShutdownHook(db);

		ontologyParameter1 = StringUtilities.stripPath(ontoFile1.toString());
		ontologyParameter2 = StringUtilities.stripPath(ontoFile2.toString());

		//create new graphs
		manager = OWLManager.createOWLOntologyManager();
		onto1 = manager.loadOntologyFromOntologyDocument(ontoFile1);
		onto2 = manager.loadOntologyFromOntologyDocument(ontoFile2);

		labelO1 = DynamicLabel.label( ontologyParameter1 );
		labelO2 = DynamicLabel.label( ontologyParameter2 );

		creator = new GraphCreator(db);
		creator.createOntologyGraph(onto1, labelO1);
		creator.createOntologyGraph(onto2, labelO2);

		AlignmentProcess a = new OppositeSubclassMatcher(ontologyParameter1,ontologyParameter2, db, weight);

		System.out.println("Matching " + ontoFile1 + " and " + ontoFile2 );
		a.init( new URI(prefix.concat(ontoFile1.toString().substring(2))), new URI(prefix.concat(ontoFile2.toString().substring(2))));
		params = new Properties();
		params.setProperty("", "");
		a.align((Alignment)null, params);	

		//create an output file for all confidence thresholds [0.1, 0.2...0.9]
		if (conf.equals("ALL")) {

			double[] confidences = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
			double precision = 0;
			double recall = 0; 
			double fMeasure = 0;
			PRecEvaluator eval = null;
			Map<String, EvaluationScore> evaluationMap = new TreeMap<String, EvaluationScore>();


			for (int i = 0; i < confidences.length; i++) {
				EvaluationScore evalScore = new EvaluationScore();
				alignmentFileName = alignmentFolder + "/" + StringUtilities.stripOntologyName(ontoFile1.toString()) + 
						"-" + StringUtilities.stripOntologyName(ontoFile2.toString()) + "-OppositeSubclassSubsumptionMatcher"+confidences[i]+".rdf";

				outputAlignment = new File(alignmentFileName);

				writer = new PrintWriter(
						new BufferedWriter(
								new FileWriter(outputAlignment)), true); 
				renderer = new RDFRendererVisitor(writer);

				evaluatedAlignment = (BasicAlignment)(a.clone());
				evaluatedAlignment.normalise();
				evaluatedAlignment.cut(confidences[i]);

				//perform the evaluation here...
				eval = new PRecEvaluator(referenceAlignment, evaluatedAlignment);
				eval.eval(p);

				precision = Double.valueOf(eval.getResults().getProperty("precision").toString());
				recall = Double.valueOf(eval.getResults().getProperty("recall").toString());
				fMeasure = Double.valueOf(eval.getResults().getProperty("fmeasure").toString());

				evalScore.setPrecision(precision);
				evalScore.setRecall(recall);
				evalScore.setfMeasure(fMeasure);
				evaluationMap.put(String.valueOf(confidences[i]), evalScore);			

				evaluatedAlignment.render(renderer);
				writer.flush();
				writer.close();

			}
			Evaluator.evaluateSingleMatcherThresholds(evaluationMap, evaluationFilePath+"/OppositeSubclassSubsumptionMatcher");

			//or if only a single confidence is requested...
		} else {

			alignmentFileName = alignmentFolder + "/" + StringUtilities.stripOntologyName(onto1.toString()) + 
					"-" + StringUtilities.stripOntologyName(onto2.toString()) + "-OppositeSubclassSubsumptionMatcher"+conf+".rdf";

			outputAlignment = new File(alignmentFileName);

			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(outputAlignment)), true); 
			renderer = new RDFRendererVisitor(writer);

			evaluatedAlignment = (BasicAlignment)(a.clone());

			evaluatedAlignment.normalise();

			evaluatedAlignment.cut(Double.parseDouble(conf));

			evaluatedAlignment.render(renderer);
			writer.flush();
			writer.close();

		}

		long estimatedTime = ((System.currentTimeMillis() - startTime)/1000)/60;
		System.out.println("\nOpposite Subclass Subsumption Matcher completed in " + estimatedTime + " minutes!");
	}

	private static void runClosestParentSubsumptionMatcher(File ontoFile1, File ontoFile2, String conf, double weight, String refalignPath, String alignmentFolder, String evaluationFilePath) throws AlignmentException, URISyntaxException, IOException, OWLOntologyCreationException {
		System.out.println("Starting Closest Parent Subsumption Matcher");
		long startTime = System.currentTimeMillis();

		PrintWriter writer = null;
		AlignmentVisitor renderer = null;	
		BasicAlignment evaluatedAlignment = null;

		Properties params = new Properties();
		String ontologyParameter1 = null;
		String ontologyParameter2 = null;	
		GraphCreator creator = null;
		OWLOntologyManager manager = null;
		OWLOntology onto1 = null;
		OWLOntology onto2 = null;
		Label labelO1 = null;
		Label labelO2 = null;
		String alignmentFileName = null;
		File outputAlignment = null;
		Properties p = new Properties();
		AlignmentParser aparser = new AlignmentParser(0);
		Alignment referenceAlignment = aparser.parse(new URI(StringUtilities.convertToFileURL(refalignPath)));

		//create a new instance of the neo4j database in each run
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		String dbName = String.valueOf(timestamp.getTime());
		File dbFile = new File("/Users/audunvennesland/Documents/phd/development/Neo4J_new/" + dbName);				
		GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(dbFile);
		registerShutdownHook(db);

		ontologyParameter1 = StringUtilities.stripPath(ontoFile1.toString());
		ontologyParameter2 = StringUtilities.stripPath(ontoFile2.toString());

		//create new graphs
		manager = OWLManager.createOWLOntologyManager();
		onto1 = manager.loadOntologyFromOntologyDocument(ontoFile1);
		onto2 = manager.loadOntologyFromOntologyDocument(ontoFile2);

		labelO1 = DynamicLabel.label( ontologyParameter1 );
		labelO2 = DynamicLabel.label( ontologyParameter2 );

		creator = new GraphCreator(db);
		creator.createOntologyGraph(onto1, labelO1);
		creator.createOntologyGraph(onto2, labelO2);

		AlignmentProcess a = new ClosestParentMatcher(ontologyParameter1,ontologyParameter2, db, weight);

		System.out.println("Matching " + ontoFile1 + " and " + ontoFile2 );
		a.init( new URI(prefix.concat(ontoFile1.toString().substring(2))), new URI(prefix.concat(ontoFile2.toString().substring(2))));
		params = new Properties();
		params.setProperty("", "");
		a.align((Alignment)null, params);	

		//create an output file for all confidence thresholds [0.1, 0.2...0.9]
		if (conf.equals("ALL")) {

			double[] confidences = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
			double precision = 0;
			double recall = 0; 
			double fMeasure = 0;
			PRecEvaluator eval = null;
			Map<String, EvaluationScore> evaluationMap = new TreeMap<String, EvaluationScore>();


			for (int i = 0; i < confidences.length; i++) {
				EvaluationScore evalScore = new EvaluationScore();
				alignmentFileName = alignmentFolder + "/" + StringUtilities.stripOntologyName(ontoFile1.toString()) + 
						"-" + StringUtilities.stripOntologyName(ontoFile2.toString()) + "-ClosestParentSubsumptionMatcher"+confidences[i]+".rdf";

				outputAlignment = new File(alignmentFileName);

				writer = new PrintWriter(
						new BufferedWriter(
								new FileWriter(outputAlignment)), true); 
				renderer = new RDFRendererVisitor(writer);

				evaluatedAlignment = (BasicAlignment)(a.clone());
				evaluatedAlignment.normalise();
				evaluatedAlignment.cut(confidences[i]);

				//perform the evaluation here...
				eval = new PRecEvaluator(referenceAlignment, evaluatedAlignment);
				eval.eval(p);

				precision = Double.valueOf(eval.getResults().getProperty("precision").toString());
				recall = Double.valueOf(eval.getResults().getProperty("recall").toString());
				fMeasure = Double.valueOf(eval.getResults().getProperty("fmeasure").toString());

				evalScore.setPrecision(precision);
				evalScore.setRecall(recall);
				evalScore.setfMeasure(fMeasure);
				evaluationMap.put(String.valueOf(confidences[i]), evalScore);			

				evaluatedAlignment.render(renderer);
				writer.flush();
				writer.close();

			}
			Evaluator.evaluateSingleMatcherThresholds(evaluationMap, evaluationFilePath+"/ClosestParentSubsumptionMatcher");

			//or if only a single confidence is requested...
		} else {

			alignmentFileName = alignmentFolder + "/" + StringUtilities.stripOntologyName(ontoFile1.toString()) + 
					"-" + StringUtilities.stripOntologyName(ontoFile2.toString()) + "-ClosestParentSubsumptionMatcher"+conf+".rdf";

			outputAlignment = new File(alignmentFileName);

			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(outputAlignment)), true); 
			renderer = new RDFRendererVisitor(writer);

			evaluatedAlignment = (BasicAlignment)(a.clone());

			evaluatedAlignment.normalise();

			evaluatedAlignment.cut(Double.parseDouble(conf));

			evaluatedAlignment.render(renderer);
			writer.flush();
			writer.close();

		}

		long estimatedTime = ((System.currentTimeMillis() - startTime)/1000)/60;
		System.out.println("\nClosest Parent Subsumption Matcher completed in " + estimatedTime + " minutes!");
	}

	private static void registerShutdownHook(final GraphDatabaseService db)
	{
		Runtime.getRuntime().addShutdownHook( new Thread()
		{
			@Override
			public void run()
			{
				db.shutdown();

			}
		} );
	}
}



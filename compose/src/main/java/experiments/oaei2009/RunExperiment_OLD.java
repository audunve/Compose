package experiments.oaei2009;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

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

import backup.ParentMatcher;
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.eval.PRecEvaluator;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import graph.Graph;
import matchercombination.ParallelCombination;
import matchercombination.SequentialCombination;
import subsumptionmatching.CompoundMatcher;
import subsumptionmatching.OppositeSubclassMatcher;
import subsumptionmatching.LexicalSubsumptionMatcher_oldest;
import utilities.StringUtilities;

/**
 * @author audunvennesland
 * 13. nov. 2017 
 */
public class RunExperiment_OLD {

	final static double threshold = 0.9;
	final static File ontologyDir = new File("./files/OAEI2009/allontologies");
	final static File sourceFile = new File("./files/OAEI2009/101/101.rdf");
	final static String prefix = "file:";

	//for the combination strategies
	final static File topFolder = new File("./files/OAEI2009/alignments");


	public static void main(String[] args) throws AlignmentException, URISyntaxException, IOException, OWLOntologyCreationException {

		//run individual matchers

		System.out.println("\nRunning Compound Matcher");
		runCompoundMatcher();

		System.out.println("\nRunning Parent Matcher");
		runParentMatcher();

		System.out.println("\nRunning OppositeSubclass Matcher");
		runOppositeSubclassMatcher();

		System.out.println("\nRunning WNHyponym Matcher");
		runWNHyponymMatcher();

		//run combinations
		System.out.println("\nCombination strategy: Weighted Sequential Combination");
		runWeightedSequentialCombination();

		System.out.println("\nCombination strategy: Parallel Priority");
		runParallelPriority();

		System.out.println("\nCombination strategy: Parallel Simple Vote");
		runParallelSimpleVote();

		//------------perform evaluation
		
	}

	private static void runWeightedSequentialCombination() throws AlignmentException, IOException {


		File[] dirs = topFolder.listFiles();

		for (int i = 0; i < dirs.length; i++) {

			//get the name of the folder to use for the stored (combined) alignment
			String folderName = dirs[i].getPath();
			String combinedAlignmentName = folderName.substring(folderName.length() -3);
			System.out.println("Combining alignments in dataset " + combinedAlignmentName); 


			File[] files = null;
			if (dirs[i].isDirectory()) {
				files = dirs[i].listFiles();

				//TODO: Create an ArrayList<Alignment> and use the new WeightedSequentialCombination method
				Alignment a = SequentialCombination.weightedSequentialComposition4(files[1], files[2], files[0], files[3]);

				//store the alignment
				File outputAlignment = new File("./files/OAEI2009/combinedAlignments/" + combinedAlignmentName + "/WeightedSequentialCombination-" + getMatcherName(files[1].getName()) + "-" + getMatcherName(files[2].getName()) + "-" + getMatcherName(files[0].getName()) + "-" + getMatcherName(files[3].getName()) + ".rdf");

				PrintWriter writer = new PrintWriter(
						new BufferedWriter(
								new FileWriter(outputAlignment)), true); 
				AlignmentVisitor renderer = new RDFRendererVisitor(writer);

				a.render(renderer);
				writer.flush();
				writer.close();

			}
		}
		
		System.out.println("Weighted Sequential Combination completed!");
	}
	
	

	//completeMatchWithPriority4
	private static void runParallelPriority() throws AlignmentException, IOException {

		File[] dirs = topFolder.listFiles();

		for (int i = 0; i < dirs.length; i++) {

			//get the name of the folder to use for the stored (combined) alignment
			String folderName = dirs[i].getPath();
			String combinedAlignmentName = folderName.substring(folderName.length() -3);
			System.out.println("Combining alignments in dataset " + combinedAlignmentName); 


			File[] files = null;
			if (dirs[i].isDirectory()) {
				files = dirs[i].listFiles();
				
				//run for all permutations of matcher order
				
				
				Alignment a = ParallelCombination.completeMatchWithPriority4(files[1], files[2], files[0], files[3]);

				//store the alignment
				File outputAlignment = new File("./files/OAEI2009/combinedAlignments/" + combinedAlignmentName + "/ParallelPriority-" + getMatcherName(files[1].getName()) + "-" + getMatcherName(files[2].getName()) + "-" + getMatcherName(files[0].getName()) + "-" + getMatcherName(files[3].getName()) + ".rdf");

				PrintWriter writer = new PrintWriter(
						new BufferedWriter(
								new FileWriter(outputAlignment)), true); 
				AlignmentVisitor renderer = new RDFRendererVisitor(writer);

				a.render(renderer);
				writer.flush();
				writer.close();

			}
		}
		
		System.out.println("\nPriority Combination completed!");
	}

		private static void runParallelSimpleVote() throws AlignmentException, IOException {

			File[] dirs = topFolder.listFiles();

			for (int i = 0; i < dirs.length; i++) {

				//get the name of the folder to use for the stored (combined) alignment
				String folderName = dirs[i].getPath();
				String combinedAlignmentName = folderName.substring(folderName.length() -3);
				System.out.println("Combining alignments in dataset " + combinedAlignmentName); 
				ArrayList<Alignment> alignments = null;
				AlignmentParser parser = null;
				Alignment a = null;
				File outputAlignment = null;
				PrintWriter writer = null;
				AlignmentVisitor renderer= null;

				File[] files = null;
				if (dirs[i].isDirectory()) {
					files = dirs[i].listFiles();
					
					//need to create a set of alignments from the files[]
					alignments = new ArrayList<Alignment>();
					parser = new AlignmentParser();

					
					for (int j = 0; j < files.length; j++) {
						alignments.add(parser.parse(files[j].toURI().toString()));
					}
					
					a = ParallelCombination.simpleVote(alignments);

					//store the alignment
					outputAlignment = new File("./files/OAEI2009/combinedAlignments/" + combinedAlignmentName + "/ParallelSimpleVote-2-" + getMatcherName(files[0].getName()) + "-" + getMatcherName(files[1].getName()) + "-" + getMatcherName(files[2].getName()) + "-" + getMatcherName(files[3].getName()) + ".rdf");

					writer = new PrintWriter(
							new BufferedWriter(
									new FileWriter(outputAlignment)), true); 
					renderer = new RDFRendererVisitor(writer);

					a.render(renderer);
					writer.flush();
					writer.close();

				}
			}
			
			System.out.println("\nSimpleVote Combination completed!");
		}

	private static void runCompoundMatcher() throws AlignmentException, URISyntaxException, IOException {

		File[] filesInDir = ontologyDir.listFiles();
		PrintWriter writer = null;
		AlignmentVisitor renderer = null;	
		BasicAlignment evaluatedAlignment = null;	
		Properties params = new Properties();

		for (int i = 0; i < filesInDir.length; i++) {
			AlignmentProcess a = new CompoundMatcher();

			System.out.println("Matching " + sourceFile + " and " + filesInDir[i] );
			a.init( new URI(prefix.concat(sourceFile.toString().substring(2))), new URI(prefix.concat(filesInDir[i].toString().substring(2))));
			params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	

			String alignmentFileName = "./files/OAEI2009/alignments/" + StringUtilities.stripOntologyName(filesInDir[i].toString()) + "/" + StringUtilities.stripOntologyName(sourceFile.toString()) + 
					"-" + StringUtilities.stripOntologyName(filesInDir[i].toString()) + "-Compound"+threshold+".rdf";

			File outputAlignment = new File(alignmentFileName);


			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(outputAlignment)), true); 
			renderer = new RDFRendererVisitor(writer);

			evaluatedAlignment = (BasicAlignment)(a.clone());

			evaluatedAlignment.normalise();

			evaluatedAlignment.cut(threshold);

			evaluatedAlignment.render(renderer);
			writer.flush();
			writer.close();

		}
		System.out.println("\nCompound matcher completed!");
	}

	private static void runParentMatcher() throws AlignmentException, URISyntaxException, IOException, OWLOntologyCreationException {

		File[] filesInDir = ontologyDir.listFiles();
		PrintWriter writer = null;
		AlignmentVisitor renderer = null;	
		BasicAlignment evaluatedAlignment = null;

		Properties params = new Properties();
		String ontologyParameter1 = null;
		String ontologyParameter2 = null;	
		Graph creator = null;
		OWLOntologyManager manager = null;
		OWLOntology o1 = null;
		OWLOntology o2 = null;
		Label labelO1 = null;
		Label labelO2 = null;

		for (int i = 0; i < filesInDir.length; i++) {

			//create a new instance of the neo4j database in each run
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			String dbName = String.valueOf(timestamp.getTime());
			File dbFile = new File("/Users/audunvennesland/Documents/phd/development/Neo4J_new/" + dbName);				
			GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(dbFile);
			registerShutdownHook(db);

			ontologyParameter1 = StringUtilities.stripPath(sourceFile.toString());
			ontologyParameter2 = StringUtilities.stripPath(filesInDir[i].toString());

			//create new graphs
			manager = OWLManager.createOWLOntologyManager();
			o1 = manager.loadOntologyFromOntologyDocument(sourceFile);
			o2 = manager.loadOntologyFromOntologyDocument(filesInDir[i]);
			labelO1 = DynamicLabel.label( ontologyParameter1 );
			labelO2 = DynamicLabel.label( ontologyParameter2 );

			creator = new Graph(db);
			creator.createOntologyGraph(o1, labelO1);
			creator.createOntologyGraph(o2, labelO2);

			AlignmentProcess a = new ParentMatcher(ontologyParameter1,ontologyParameter2, db);

			System.out.println("Matching " + sourceFile + " and " + filesInDir[i] );
			a.init( new URI(prefix.concat(sourceFile.toString().substring(2))), new URI(prefix.concat(filesInDir[i].toString().substring(2))));
			params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	

			String alignmentFileName = "./files/OAEI2009/alignments/" + StringUtilities.stripOntologyName(filesInDir[i].toString()) + "/" + StringUtilities.stripOntologyName(sourceFile.toString()) + 
					"-" + StringUtilities.stripOntologyName(filesInDir[i].toString()) + "-Parent"+threshold+".rdf";

			File outputAlignment = new File(alignmentFileName);


			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(outputAlignment)), true); 
			renderer = new RDFRendererVisitor(writer);

			evaluatedAlignment = (BasicAlignment)(a.clone());

			evaluatedAlignment.normalise();

			evaluatedAlignment.cut(threshold);

			evaluatedAlignment.render(renderer);
			writer.flush();
			writer.close();


		}
		System.out.println("\nParent matcher completed!");
	}

	private static void runOppositeSubclassMatcher() throws AlignmentException, URISyntaxException, IOException, OWLOntologyCreationException {

		File[] filesInDir = ontologyDir.listFiles();
		PrintWriter writer = null;
		AlignmentVisitor renderer = null;	
		BasicAlignment evaluatedAlignment = null;

		Properties params = new Properties();
		String ontologyParameter1 = null;
		String ontologyParameter2 = null;	
		Graph creator = null;
		OWLOntologyManager manager = null;
		OWLOntology o1 = null;
		OWLOntology o2 = null;
		Label labelO1 = null;
		Label labelO2 = null;


		for (int i = 0; i < filesInDir.length; i++) {

			//create a new instance of the neo4j database in each run
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			String dbName = String.valueOf(timestamp.getTime());
			File dbFile = new File("/Users/audunvennesland/Documents/phd/development/Neo4J_new/" + dbName);				
			GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(dbFile);
			registerShutdownHook(db);

			ontologyParameter1 = StringUtilities.stripPath(sourceFile.toString());
			ontologyParameter2 = StringUtilities.stripPath(filesInDir[i].toString());

			//create new graphs
			manager = OWLManager.createOWLOntologyManager();
			o1 = manager.loadOntologyFromOntologyDocument(sourceFile);
			o2 = manager.loadOntologyFromOntologyDocument(filesInDir[i]);
			labelO1 = DynamicLabel.label( ontologyParameter1 );
			labelO2 = DynamicLabel.label( ontologyParameter2 );

			creator = new Graph(db);
			creator.createOntologyGraph(o1, labelO1);
			creator.createOntologyGraph(o2, labelO2);

			AlignmentProcess a = new OppositeSubclassMatcher(ontologyParameter1,ontologyParameter2, db);

			System.out.println("Matching " + sourceFile + " and " + filesInDir[i] );
			a.init( new URI(prefix.concat(sourceFile.toString().substring(2))), new URI(prefix.concat(filesInDir[i].toString().substring(2))));
			params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	

			String alignmentFileName = "./files/OAEI2009/alignments/" + StringUtilities.stripOntologyName(filesInDir[i].toString()) + "/" + StringUtilities.stripOntologyName(sourceFile.toString()) + 
					"-" + StringUtilities.stripOntologyName(filesInDir[i].toString()) + "-OppositeSubclass"+threshold+".rdf";

			File outputAlignment = new File(alignmentFileName);


			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(outputAlignment)), true); 
			renderer = new RDFRendererVisitor(writer);

			evaluatedAlignment = (BasicAlignment)(a.clone());

			evaluatedAlignment.normalise();

			evaluatedAlignment.cut(threshold);

			evaluatedAlignment.render(renderer);
			writer.flush();
			writer.close();


		}
		System.out.println("\nOpposite subclass matcher completed!");
	}

	private static void runWNHyponymMatcher() throws AlignmentException, URISyntaxException, IOException {

		File[] filesInDir = ontologyDir.listFiles();
		PrintWriter writer = null;
		AlignmentVisitor renderer = null;	
		BasicAlignment evaluatedAlignment = null;

		Properties params = new Properties();

		for (int i = 0; i < filesInDir.length; i++) {
			AlignmentProcess a = new LexicalSubsumptionMatcher_oldest();

			System.out.println("Matching " + sourceFile + " and " + filesInDir[i] );
			a.init( new URI(prefix.concat(sourceFile.toString().substring(2))), new URI(prefix.concat(filesInDir[i].toString().substring(2))));
			params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	

			String alignmentFileName = "./files/OAEI2009/alignments/" + StringUtilities.stripOntologyName(filesInDir[i].toString()) + "/" + StringUtilities.stripOntologyName(sourceFile.toString()) + 
					"-" + StringUtilities.stripOntologyName(filesInDir[i].toString()) + "-WNHyponym"+threshold+".rdf";

			File outputAlignment = new File(alignmentFileName);


			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(outputAlignment)), true); 
			renderer = new RDFRendererVisitor(writer);

			evaluatedAlignment = (BasicAlignment)(a.clone());

			evaluatedAlignment.normalise();

			evaluatedAlignment.cut(threshold);

			evaluatedAlignment.render(renderer);
			writer.flush();
			writer.close();

		}
		System.out.println("\nWordNet Hyponym matcher completed!");
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

	private static String getMatcherName(String inputAlignmentName) {

		String matcherName = inputAlignmentName.substring(inputAlignmentName.lastIndexOf("-") + 1, inputAlignmentName.lastIndexOf("-") +4);

		return matcherName;
	}
	
	public static void permute(String[] arr){
	    permuteHelper(arr, 0);
	}

	private static void permuteHelper(String[] arr, int index){
	    if(index >= arr.length - 1){ //If we are at the last element - nothing left to permute
	        System.out.print("[");
	        for(int i = 0; i < arr.length - 1; i++){
	            System.out.print(arr[i] + ", ");
	        }
	        if(arr.length > 0) 
	            System.out.print(arr[arr.length - 1]);
	        System.out.println("]");
	        return;
	    }

	    for(int i = index; i < arr.length; i++){ //For each index in the sub array arr[index...end]

	        //Swap the elements at indices index and i
	        String t = arr[index];
	        arr[index] = arr[i];
	        arr[i] = t;

	        //Recurse on the sub array arr[index+1...end]
	        permuteHelper(arr, index+1);

	        //Swap the elements back
	        t = arr[index];
	        arr[index] = arr[i];
	        arr[i] = t;
	    }
	}

}

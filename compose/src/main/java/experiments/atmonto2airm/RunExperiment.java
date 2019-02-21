package experiments.atmonto2airm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.util.Properties;

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

import backup.ClosestParentMatcher;
import backup.EditMatcher_remove;
import backup.GraphCreator;
import backup.TrigramMatcher;
import backup.WNHirstStOnge;
import backup.WNJiangConrath;
import backup.WNLin;
import backup.WNResnik;
import backup.WNRiWordNetDistance;
import backup.WNWuPalmer;
import subsumptionmatching.ClosestParentMatcher;
import equivalencematching.DefinitionsEquivalenceMatcher;
import equivalencematching.StringEquivalenceMatcher;
import equivalencematching.GraphMatcher;
import equivalencematching.LexicalMatcherWordNet;
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import graph.Graph;
import utilities.AlignmentOperations;
import utilities.StringUtilities;

public class RunExperiment {

	final static double threshold = 0.6;

	final static File ontoFile1 = new File("./files/ESWC_ATMONTO_AIRM/ATMOntoCoreMerged.owl");
	final static File ontoFile2 = new File("./files/ESWC_ATMONTO_AIRM/airm-mono.owl");
	final static String prefix = "file:";


	public static void main(String[] args) throws AlignmentException, URISyntaxException, IOException, OWLOntologyCreationException {

		//run individual matchers

//				System.out.println("\nRunning Edit Distance Matcher");
//				runEditMatcher();
//
//				System.out.println("\nRunning ISub Matcher");
//				runISubMatcher();
//
//				System.out.println("\nRunning Trigram Matcher");
//				runTrigramMatcher() ;
//
				System.out.println("\nRunning Definitions Matcher");
				runDefinitionsMatcher();
	
/*				System.out.println("\nRunning Property Matcher");
				runPropertyMatcher();
		
				System.out.println("\nRunning Range Matcher");
				runRangeMatcher();*/

//				System.out.println("\nRunning Structural Proximity Matcher");
//				runStructProxMatcher();
				
//				System.out.println("\nRunning Closest Parent Matcher");
//				runClosestParentMatcher();
//			    System.out.println("The Closest Parent Matcher has completed!");

/*				System.out.println("\nRunning WordNet Lin Matcher");
				runWNLinMatcher();
			
				System.out.println("\nRunning WordNet HirstStOnge Matcher");
				runWNHirstStOngeMatcher();
	
				System.out.println("\nRunning WordNet JiangConrath Matcher");
				runWNJiangConrathMatcher();
	
				System.out.println("\nRunning WordNet Wu-Palmer Matcher");
				runWNWuPalmerMatcher();
	
				System.out.println("\nRunning WordNet RiWordNetDistance Matcher");
				runWNRiWordNetDistanceMatcher();
	
				System.out.println("\nRunning WordNet WN Synonym Matcher");
				runWNSynMatcher();
				
				System.out.println("\nRunning WordNet WN Resnik Matcher");
				runWNResnikMatcher();
				*/
				

	}
	
	private static void runClosestParentMatcher() throws AlignmentException, URISyntaxException, IOException, OWLOntologyCreationException {

		PrintWriter writer = null;
		AlignmentVisitor renderer = null;	
		BasicAlignment evaluatedAlignment = null;

		Properties params = new Properties();
		String ontologyParameter1 = null;
		String ontologyParameter2 = null;	
		GraphCreator creator = null;
		OWLOntologyManager manager = null;
		OWLOntology o1 = null;
		OWLOntology o2 = null;
		Label labelO1 = null;
		Label labelO2 = null;
		double[] thresholds = {0.5, 0.7, 0.9, 0.95};
		String alignmentFileName = null;
		File outputAlignment = null;

			//create a new instance of the neo4j database in each run
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			String dbName = String.valueOf(timestamp.getTime());
			//final File dbFile = new File("/Users/audunvennesland/Documents/phd/development/Neo4J_new/test");
			File dbFile = new File("/Users/audunvennesland/Documents/phd/development/Neo4J_new/" + dbName);				
			GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(dbFile);
			registerShutdownHook(db);

			ontologyParameter1 = StringUtilities.stripPath(ontoFile1.toString());
			ontologyParameter2 = StringUtilities.stripPath(ontoFile2.toString());

			//create new graphs
			manager = OWLManager.createOWLOntologyManager();
			o1 = manager.loadOntologyFromOntologyDocument(ontoFile1);
			o2 = manager.loadOntologyFromOntologyDocument(ontoFile2);
			labelO1 = DynamicLabel.label( ontologyParameter1 );
			labelO2 = DynamicLabel.label( ontologyParameter2 );

			creator = new GraphCreator(db);
			creator.createOntologyGraph(o1, labelO1);
			creator.createOntologyGraph(o2, labelO2);

			AlignmentProcess a = new ClosestParentMatcher(ontologyParameter1,ontologyParameter2, db);

			System.out.println("Matching " + ontoFile1 + " and " + ontoFile2 );
			a.init( new URI(prefix.concat(ontoFile1.toString().substring(2))), new URI(prefix.concat(ontoFile2.toString().substring(2))));
			params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	
			for (int i = 0; i < thresholds.length; i++) {

			alignmentFileName = "./files/ATMONTO2AIRMExperiment/alignments-09/" + StringUtilities.stripOntologyName(ontoFile1.toString()) + 
						"-" + StringUtilities.stripOntologyName(ontoFile2.toString()) + "-ClosestParent"+threshold+".rdf";

			outputAlignment = new File(alignmentFileName);


			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(outputAlignment)), true); 
			renderer = new RDFRendererVisitor(writer);

			evaluatedAlignment = (BasicAlignment)(a.clone());

			evaluatedAlignment.normalise();

			evaluatedAlignment.cut(thresholds[i]);

			evaluatedAlignment.render(renderer);
			writer.flush();
			writer.close();
			}

		System.out.println("\nClosest Parent matcher completed!");
	}

	private static void runEditMatcher() throws AlignmentException, URISyntaxException, IOException {

		AlignmentProcess a = new EditMatcher_remove();
		a.init(ontoFile1.toURI(), ontoFile2.toURI());
		Properties params = new Properties();
		params.setProperty("", "");
		a.align((Alignment)null, params);	
		AlignmentVisitor renderer = null;
		BasicAlignment evaluatedAlignment = null;
		PrintWriter writer = null;

		String alignmentFileName = "./files/ATMONTO2AIRMExperiment/alignments-09/" + StringUtilities.stripOntologyName(ontoFile1.toString()) + 
				"-" + StringUtilities.stripOntologyName(ontoFile2.toString()) + "-Edit"+threshold+".rdf";

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

	private static void runISubMatcher() throws AlignmentException, URISyntaxException, IOException {

		AlignmentProcess a = new StringEquivalenceMatcher();
		a.init(ontoFile1.toURI(), ontoFile2.toURI());
		Properties params = new Properties();
		params.setProperty("", "");
		a.align((Alignment)null, params);	
		AlignmentVisitor renderer = null;
		BasicAlignment evaluatedAlignment = null;
		PrintWriter writer = null;

		String alignmentFileName = "./files/ATMONTO2AIRMExperiment/alignments-09/" + StringUtilities.stripOntologyName(ontoFile1.toString()) + 
				"-" + StringUtilities.stripOntologyName(ontoFile2.toString()) + "-ISub"+threshold+".rdf";

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

	private static void runTrigramMatcher() throws AlignmentException, URISyntaxException, IOException {

		AlignmentProcess a = new TrigramMatcher();
		a.init(ontoFile1.toURI(), ontoFile2.toURI());
		Properties params = new Properties();
		params.setProperty("", "");
		a.align((Alignment)null, params);	
		AlignmentVisitor renderer = null;
		BasicAlignment evaluatedAlignment = null;
		PrintWriter writer = null;

		String alignmentFileName = "./files/ATMONTO2AIRMExperiment/alignments-09/" + StringUtilities.stripOntologyName(ontoFile1.toString()) + 
				"-" + StringUtilities.stripOntologyName(ontoFile2.toString()) + "-Trigram"+threshold+".rdf";

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
	
	

	private static void runDefinitionsMatcher() throws AlignmentException, URISyntaxException, IOException {

		AlignmentProcess a = new DefinitionsEquivalenceMatcher();
		a.init(ontoFile1.toURI(), ontoFile2.toURI());
		Properties params = new Properties();
		params.setProperty("", "");
		a.align((Alignment)null, params);	
		AlignmentVisitor renderer = null;
		BasicAlignment evaluatedAlignment = null;
		PrintWriter writer = null;

		String alignmentFileName = "./files/ATMONTO2AIRMExperiment/alignments-06/" + StringUtilities.stripOntologyName(ontoFile1.toString()) + 
				"-" + StringUtilities.stripOntologyName(ontoFile2.toString()) + "-DefinitionsMatcher"+threshold+".rdf";

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

//	private static void runPropertyMatcher() throws AlignmentException, URISyntaxException, IOException {
//
//		AlignmentProcess a = new PropertyMatcher();
//		a.init(ontoFile1.toURI(), ontoFile2.toURI());
//		Properties params = new Properties();
//		params.setProperty("", "");
//		a.align((Alignment)null, params);	
//		AlignmentVisitor renderer = null;
//		BasicAlignment evaluatedAlignment = null;
//		PrintWriter writer = null;
//
//		String alignmentFileName = "./files/ATMONTO2AIRMExperiment/alignments-09/" + StringUtilities.stripOntologyName(ontoFile1.toString()) + 
//				"-" + StringUtilities.stripOntologyName(ontoFile2.toString()) + "-PropertyMatcher"+threshold+".rdf";
//
//		File outputAlignment = new File(alignmentFileName);
//
//
//		writer = new PrintWriter(
//				new BufferedWriter(
//						new FileWriter(outputAlignment)), true); 
//		renderer = new RDFRendererVisitor(writer);
//
//		evaluatedAlignment = (BasicAlignment)(a.clone());
//
//		evaluatedAlignment.normalise();
//
//		evaluatedAlignment.cut(threshold);
//
//		evaluatedAlignment.render(renderer);
//		writer.flush();
//		writer.close();
//
//	}

//	private static void runRangeMatcher() throws AlignmentException, URISyntaxException, IOException {
//
//		AlignmentProcess a = new RangeMatcher();
//		a.init(ontoFile1.toURI(), ontoFile2.toURI());
//		Properties params = new Properties();
//		params.setProperty("", "");
//		a.align((Alignment)null, params);	
//		AlignmentVisitor renderer = null;
//		BasicAlignment evaluatedAlignment = null;
//		PrintWriter writer = null;
//
//		String alignmentFileName = "./files/ATMONTO2AIRMExperiment/alignments-09/" + StringUtilities.stripOntologyName(ontoFile1.toString()) + 
//				"-" + StringUtilities.stripOntologyName(ontoFile2.toString()) + "-RangeMatcher"+threshold+".rdf";
//
//		File outputAlignment = new File(alignmentFileName);
//
//
//		writer = new PrintWriter(
//				new BufferedWriter(
//						new FileWriter(outputAlignment)), true); 
//		renderer = new RDFRendererVisitor(writer);
//
//		evaluatedAlignment = (BasicAlignment)(a.clone());
//
//		evaluatedAlignment.normalise();
//
//		evaluatedAlignment.cut(threshold);
//
//		evaluatedAlignment.render(renderer);
//		writer.flush();
//		writer.close();
//
//	}


	@SuppressWarnings("deprecation")
	private static void runStructProxMatcher() throws AlignmentException, URISyntaxException, IOException, OWLOntologyCreationException {

		//create a new instance of the neo4j database in each run
		String ontologyParameter1 = null;
		String ontologyParameter2 = null;	
		Graph creator = null;
		OWLOntologyManager manager = null;
		OWLOntology o1 = null;
		OWLOntology o2 = null;
		Label labelO1 = null;
		Label labelO2 = null;
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		String dbName = String.valueOf(timestamp.getTime());
		File dbFile = new File("/Users/audunvennesland/Documents/phd/development/Neo4J_new/" + dbName);	
		System.out.println("Creating a new database");
		GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(dbFile);
		System.out.println("Database created");
		//registerShutdownHook(db);

		ontologyParameter1 = StringUtilities.stripPath(ontoFile1.toString());
		ontologyParameter2 = StringUtilities.stripPath(ontoFile2.toString());

		//create new graphs
		manager = OWLManager.createOWLOntologyManager();
		o1 = manager.loadOntologyFromOntologyDocument(ontoFile1);
		o2 = manager.loadOntologyFromOntologyDocument(ontoFile2);

		labelO1 = DynamicLabel.label( ontologyParameter1 );
		labelO2 = DynamicLabel.label( ontologyParameter2 );

		System.out.println("Creating ontology graphs");
		creator = new Graph(db);
		
		creator.createOntologyGraph(o1, labelO1);
		creator.createOntologyGraph(o2, labelO2);

		//now matching the ontologies (i.e. using the graph representation of the ontologies)
		System.out.println("Sending " + ontologyParameter1 + " , " + ontologyParameter2 + ", " + db + " to StructuralAlignment");
		AlignmentProcess a = new GraphMatcher(ontologyParameter1, ontologyParameter2, db);
		a.init(ontoFile1.toURI(), ontoFile2.toURI());
		Properties params = new Properties();
		params.setProperty("", "");
		a.align((Alignment)null, params);	
		AlignmentVisitor renderer = null; 
		BasicAlignment evaluatedAlignment = null;
		PrintWriter writer = null;

		//need to create the ontology graphs


		String alignmentFileName = "./files/ATMONTO2AIRMExperiment/alignments-09/" + StringUtilities.stripOntologyName(ontoFile1.toString()) + 
				"-" + StringUtilities.stripOntologyName(ontoFile2.toString()) + "-StructProxMatcher"+threshold+".rdf";

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
	
	private static void runWNLinMatcher() throws AlignmentException, URISyntaxException, IOException {

		AlignmentProcess a = new WNLin();
		a.init(ontoFile1.toURI(), ontoFile2.toURI());
		Properties params = new Properties();
		params.setProperty("", "");
		a.align((Alignment)null, params);	
		AlignmentVisitor renderer = null;
		BasicAlignment evaluatedAlignment = null;
		PrintWriter writer = null;

		String alignmentFileName = "./files/ATMONTO2AIRMExperiment/alignments-09/" + StringUtilities.stripOntologyName(ontoFile1.toString()) + 
				"-" + StringUtilities.stripOntologyName(ontoFile2.toString()) + "-WNLin"+threshold+".rdf";

		File outputAlignment = new File(alignmentFileName);


		writer = new PrintWriter(
				new BufferedWriter(
						new FileWriter(outputAlignment)), true); 
		renderer = new RDFRendererVisitor(writer);

		evaluatedAlignment = (BasicAlignment)(a.clone());

		evaluatedAlignment.normalise();
		
		AlignmentOperations.normalizeConfidence(evaluatedAlignment);

		evaluatedAlignment.cut(threshold);

		evaluatedAlignment.render(renderer);
		writer.flush();
		writer.close();

	}
	
	private static void runWNResnikMatcher() throws AlignmentException, URISyntaxException, IOException {

		AlignmentProcess a = new WNResnik();
		a.init(ontoFile1.toURI(), ontoFile2.toURI());
		Properties params = new Properties();
		params.setProperty("", "");
		a.align((Alignment)null, params);	
		AlignmentVisitor renderer = null;
		BasicAlignment evaluatedAlignment = null;
		PrintWriter writer = null;

		String alignmentFileName = "./files/ATMONTO2AIRMExperiment/alignments-06/" + StringUtilities.stripOntologyName(ontoFile1.toString()) + 
				"-" + StringUtilities.stripOntologyName(ontoFile2.toString()) + "-WNLResnik"+threshold+".rdf";

		File outputAlignment = new File(alignmentFileName);


		writer = new PrintWriter(
				new BufferedWriter(
						new FileWriter(outputAlignment)), true); 
		renderer = new RDFRendererVisitor(writer);

		evaluatedAlignment = (BasicAlignment)(a.clone());

		evaluatedAlignment.normalise();
		
		AlignmentOperations.normalizeConfidence(evaluatedAlignment);

		evaluatedAlignment.cut(threshold);

		evaluatedAlignment.render(renderer);
		writer.flush();
		writer.close();

	}
	
	private static void runWNHirstStOngeMatcher() throws AlignmentException, URISyntaxException, IOException {

		AlignmentProcess a = new WNHirstStOnge();
		a.init(ontoFile1.toURI(), ontoFile2.toURI());
		Properties params = new Properties();
		params.setProperty("", "");
		a.align((Alignment)null, params);	
		AlignmentVisitor renderer = null;
		BasicAlignment evaluatedAlignment = null;
		PrintWriter writer = null;

		String alignmentFileName = "./files/ATMONTO2AIRMExperiment/alignments-09/" + StringUtilities.stripOntologyName(ontoFile1.toString()) + 
				"-" + StringUtilities.stripOntologyName(ontoFile2.toString()) + "-WNHirstStOngeMatcher"+threshold+".rdf";

		File outputAlignment = new File(alignmentFileName);


		writer = new PrintWriter(
				new BufferedWriter(
						new FileWriter(outputAlignment)), true); 
		renderer = new RDFRendererVisitor(writer);

		evaluatedAlignment = (BasicAlignment)(a.clone());

		evaluatedAlignment.normalise();
		
		//normalise or scale condidence between [0..1]
		AlignmentOperations.normalizeConfidence(evaluatedAlignment);

		evaluatedAlignment.cut(threshold);

		evaluatedAlignment.render(renderer);
		writer.flush();
		writer.close();

	}
	
	private static void runWNJiangConrathMatcher() throws AlignmentException, URISyntaxException, IOException {

		AlignmentProcess a = new WNJiangConrath();
		a.init(ontoFile1.toURI(), ontoFile2.toURI());
		Properties params = new Properties();
		params.setProperty("", "");
		a.align((Alignment)null, params);	
		AlignmentVisitor renderer = null;
		BasicAlignment evaluatedAlignment = null;
		PrintWriter writer = null;

		String alignmentFileName = "./files/ATMONTO2AIRMExperiment/alignments-09/" + StringUtilities.stripOntologyName(ontoFile1.toString()) + 
				"-" + StringUtilities.stripOntologyName(ontoFile2.toString()) + "-WNJiangConrathMatcher"+threshold+".rdf";

		File outputAlignment = new File(alignmentFileName);


		writer = new PrintWriter(
				new BufferedWriter(
						new FileWriter(outputAlignment)), true); 
		renderer = new RDFRendererVisitor(writer);

		evaluatedAlignment = (BasicAlignment)(a.clone());

		evaluatedAlignment.normalise();
		
		AlignmentOperations.normalizeConfidence(evaluatedAlignment);

		evaluatedAlignment.cut(threshold);

		evaluatedAlignment.render(renderer);
		writer.flush();
		writer.close();

	}
	
	private static void runWNWuPalmerMatcher() throws AlignmentException, URISyntaxException, IOException {

		AlignmentProcess a = new WNWuPalmer();
		a.init(ontoFile1.toURI(), ontoFile2.toURI());
		Properties params = new Properties();
		params.setProperty("", "");
		a.align((Alignment)null, params);	
		AlignmentVisitor renderer = null;
		BasicAlignment evaluatedAlignment = null;
		PrintWriter writer = null;

		String alignmentFileName = "./files/ATMONTO2AIRMExperiment/alignments-09/" + StringUtilities.stripOntologyName(ontoFile1.toString()) + 
				"-" + StringUtilities.stripOntologyName(ontoFile2.toString()) + "-WNWuPalmerMatcher"+threshold+".rdf";

		File outputAlignment = new File(alignmentFileName);


		writer = new PrintWriter(
				new BufferedWriter(
						new FileWriter(outputAlignment)), true); 
		renderer = new RDFRendererVisitor(writer);

		evaluatedAlignment = (BasicAlignment)(a.clone());

		evaluatedAlignment.normalise();
		
		AlignmentOperations.normalizeConfidence(evaluatedAlignment);
		
		evaluatedAlignment.cut(threshold);

		evaluatedAlignment.render(renderer);

		writer.flush();
		writer.close();

	}

	private static void runWNRiWordNetDistanceMatcher() throws AlignmentException, URISyntaxException, IOException {

		AlignmentProcess a = new WNRiWordNetDistance();
		a.init(ontoFile1.toURI(), ontoFile2.toURI());
		Properties params = new Properties();
		params.setProperty("", "");
		a.align((Alignment)null, params);	
		AlignmentVisitor renderer = null;
		BasicAlignment evaluatedAlignment = null;
		PrintWriter writer = null;

		String alignmentFileName = "./files/ATMONTO2AIRMExperiment/alignments-09/" + StringUtilities.stripOntologyName(ontoFile1.toString()) + 
				"-" + StringUtilities.stripOntologyName(ontoFile2.toString()) + "-WNRiWordNetDistance"+threshold+".rdf";

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
	
	private static void runWNSynMatcher() throws AlignmentException, URISyntaxException, IOException {

		AlignmentProcess a = new LexicalMatcherWordNet();
		a.init(ontoFile1.toURI(), ontoFile2.toURI());
		Properties params = new Properties();
		params.setProperty("", "");
		a.align((Alignment)null, params);	
		AlignmentVisitor renderer = null;
		BasicAlignment evaluatedAlignment = null;
		PrintWriter writer = null;

		String alignmentFileName = "./files/ATMONTO2AIRMExperiment/alignments-09/" + StringUtilities.stripOntologyName(ontoFile1.toString()) + 
				"-" + StringUtilities.stripOntologyName(ontoFile2.toString()) + "-WNSynMatcher"+threshold+".rdf";

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



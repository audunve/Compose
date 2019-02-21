package experiments.oaei2011;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.util.Map;
import java.util.Map.Entry;
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

import equivalencematching.DefinitionsEquivalenceMatcher;
import equivalencematching.GraphMatcher;
import equivalencematching.LexicalMatcherWordNet;
import equivalencematching.PropertyMatcher;
import equivalencematching.RangeMatcher_slow;
import equivalencematching.StringEquivalenceMatcher;
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.BasicConfidence;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.rel.A5AlgebraRelation;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import graph.Graph;
import matchercombination.Harmony;
import net.didion.jwnl.JWNLException;
import ontologyprofiling.OntologyProfiler;
import utilities.StringUtilities;

public class RunExperimentHarmonyEQ {
	
	static String onto1 = "303";
	static String onto2 = "304";

	final static File ontoFile1 = new File("./files/_PHD_EVALUATION/OAEI2011/ONTOLOGIES/" + onto1+onto2 + "/" + onto1+onto2 + "-" + onto1 + ".rdf");
	final static File ontoFile2 = new File("./files/_PHD_EVALUATION/OAEI2011/ONTOLOGIES/" + onto1+onto2 + "/" + onto1+onto2 + "-" + onto2 + ".rdf");
	final static String prefix = "file:";
	final static String storePath = "./files/_PHD_EVALUATION/OAEI2011/HARMONY/EQUIVALENCE/"+ onto1+onto2;

	public static void main(String[] args) throws AlignmentException, URISyntaxException, IOException, OWLOntologyCreationException, JWNLException {

		//retrieve ontology profiling scores in Map
		System.out.println("...creating ontology profiles...");
		Map<String, Double> ontologyProfilingScores = OntologyProfiler.computeOntologyProfileScores(ontoFile1, ontoFile2);
		System.out.println("...ontology profiles created...");

		System.out.println("The ontology profile scores are: ");
		for (Entry<String, Double> e : ontologyProfilingScores.entrySet()) {
			System.out.println("Profiling metric: " + e.getKey() + ", Score: " + e.getValue());
		}
		
		
		//run individual matchers
		System.out.println("\nRunning String Equivalence Matcher (SEM)");
		runStringEquivalenceMatcher(ontologyProfilingScores.get("cne"));

		System.out.println("\nRunning Definitions Equivalence Matcher (DEM)");
		runDefinitionsEquivalenceMatcher(ontologyProfilingScores.get("dte"));

		System.out.println("\nRunning Property Matcher (PM)");
		runPropertyMatcher(ontologyProfilingScores.get("pf"));

		System.out.println("\nRunning Range Matcher (RM)");
		runRangeMatcher(ontologyProfilingScores.get("pf"));

		System.out.println("\nRunning Graph Matcher (GM)");
		runGraphMatcher(ontologyProfilingScores.get("sa"));

		System.out.println("\nRunning Lexical Matcher (LM)");
		runLexicalMatcher(ontologyProfilingScores.get("srw"));			

		//once all relevant matchers are run, compute their Harmony alignments and values
		URIAlignment harmonyAlignment = Harmony.computeHarmonyAlignment(storePath);

		//store the computed Harmony alignment
		URIAlignment storedHarmonyAlignment = new URIAlignment();

		String alignmentFileName = "./files/_PHD_EVALUATION/OAEI2011/HARMONY/EQUIVALENCE/ComputedHarmonyAlignment_" + onto1+onto2 + ".rdf";

		File outputAlignment = new File(alignmentFileName);

		PrintWriter writer = new PrintWriter(
				new BufferedWriter(
						new FileWriter(outputAlignment)), true); 
		AlignmentVisitor renderer = new RDFRendererVisitor(writer);

		//need to initialise the alignment with ontology URIs and the type of relation (e.g. A5AlgebraRelation) otherwise exceptions are thrown
		URI onto1URI = harmonyAlignment.getOntology1URI();
		URI onto2URI = harmonyAlignment.getOntology2URI();
		storedHarmonyAlignment.init( onto1URI, onto2URI, A5AlgebraRelation.class, BasicConfidence.class );

		storedHarmonyAlignment = (URIAlignment) harmonyAlignment.clone();

		storedHarmonyAlignment.normalise();

		storedHarmonyAlignment.render(renderer);
		writer.flush();
		writer.close();

	}

	private static void runStringEquivalenceMatcher(double weight) throws AlignmentException, URISyntaxException, IOException {

		AlignmentProcess a = new StringEquivalenceMatcher(weight);
		a.init(ontoFile1.toURI(), ontoFile2.toURI());
		Properties params = new Properties();
		params.setProperty("", "");
		a.align((Alignment)null, params);	
		AlignmentVisitor renderer = null;
		BasicAlignment evaluatedAlignment = null;
		PrintWriter writer = null;

		String alignmentFileName = storePath + "/" + StringUtilities.stripOntologyName(ontoFile1.toString()) + 
				"-" + StringUtilities.stripOntologyName(ontoFile2.toString()) + "-StringEquivalenceMatcher.rdf";

		File outputAlignment = new File(alignmentFileName);


		writer = new PrintWriter(
				new BufferedWriter(
						new FileWriter(outputAlignment)), true); 
		renderer = new RDFRendererVisitor(writer);

		evaluatedAlignment = (BasicAlignment)(a.clone());

		evaluatedAlignment.normalise();

		evaluatedAlignment.render(renderer);
		writer.flush();
		writer.close();

	}


	private static void runDefinitionsEquivalenceMatcher(double weight) throws AlignmentException, URISyntaxException, IOException {

		AlignmentProcess a = new DefinitionsEquivalenceMatcher(weight);
		a.init(ontoFile1.toURI(), ontoFile2.toURI());
		Properties params = new Properties();
		params.setProperty("", "");
		a.align((Alignment)null, params);	
		AlignmentVisitor renderer = null;
		BasicAlignment evaluatedAlignment = null;
		PrintWriter writer = null;

		String alignmentFileName = storePath + "/" + StringUtilities.stripOntologyName(ontoFile1.toString()) + 
				"-" + StringUtilities.stripOntologyName(ontoFile2.toString()) + "-DefinitionsEquivalenceMatcher.rdf";

		File outputAlignment = new File(alignmentFileName);


		writer = new PrintWriter(
				new BufferedWriter(
						new FileWriter(outputAlignment)), true); 
		renderer = new RDFRendererVisitor(writer);

		evaluatedAlignment = (BasicAlignment)(a.clone());

		evaluatedAlignment.normalise();

		evaluatedAlignment.render(renderer);
		writer.flush();
		writer.close();

	}

	private static void runPropertyMatcher(double weight) throws AlignmentException, URISyntaxException, IOException, OWLOntologyCreationException {


		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto1 = manager.loadOntologyFromOntologyDocument(ontoFile1);
		OWLOntology onto2 = manager.loadOntologyFromOntologyDocument(ontoFile2);

		AlignmentProcess a = new PropertyMatcher(onto1, onto2, weight);
		a.init(ontoFile1.toURI(), ontoFile2.toURI());
		Properties params = new Properties();
		params.setProperty("", "");
		a.align((Alignment)null, params);	
		AlignmentVisitor renderer = null;
		BasicAlignment evaluatedAlignment = null;
		PrintWriter writer = null;

		String alignmentFileName = storePath + "/" + StringUtilities.stripOntologyName(ontoFile1.toString()) + 
				"-" + StringUtilities.stripOntologyName(ontoFile2.toString()) + "-PropertyMatcher.rdf";

		File outputAlignment = new File(alignmentFileName);


		writer = new PrintWriter(
				new BufferedWriter(
						new FileWriter(outputAlignment)), true); 
		renderer = new RDFRendererVisitor(writer);

		evaluatedAlignment = (BasicAlignment)(a.clone());

		evaluatedAlignment.normalise();

		evaluatedAlignment.render(renderer);
		writer.flush();
		writer.close();

	}

	private static void runRangeMatcher(double weight) throws AlignmentException, URISyntaxException, IOException {

		AlignmentProcess a = new RangeMatcher_slow(weight);
		a.init(ontoFile1.toURI(), ontoFile2.toURI());
		Properties params = new Properties();
		params.setProperty("", "");
		a.align((Alignment)null, params);	
		AlignmentVisitor renderer = null;
		BasicAlignment evaluatedAlignment = null;
		PrintWriter writer = null;

		String alignmentFileName = storePath + "/" + StringUtilities.stripOntologyName(ontoFile1.toString()) + 
				"-" + StringUtilities.stripOntologyName(ontoFile2.toString()) + "-RangeMatcher.rdf";

		File outputAlignment = new File(alignmentFileName);


		writer = new PrintWriter(
				new BufferedWriter(
						new FileWriter(outputAlignment)), true); 
		renderer = new RDFRendererVisitor(writer);

		evaluatedAlignment = (BasicAlignment)(a.clone());

		evaluatedAlignment.normalise();

		evaluatedAlignment.render(renderer);
		writer.flush();
		writer.close();

	}


	private static void runGraphMatcher(double weight) throws AlignmentException, URISyntaxException, IOException, OWLOntologyCreationException {

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
		GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(dbFile);

		ontologyParameter1 = StringUtilities.stripPath(ontoFile1.toString());
		ontologyParameter2 = StringUtilities.stripPath(ontoFile2.toString());

		//create new graphs
		manager = OWLManager.createOWLOntologyManager();
		o1 = manager.loadOntologyFromOntologyDocument(ontoFile1);
		o2 = manager.loadOntologyFromOntologyDocument(ontoFile2);

		labelO1 = DynamicLabel.label( ontologyParameter1 );
		labelO2 = DynamicLabel.label( ontologyParameter2 );

		creator = new Graph(db);

		creator.createOntologyGraph(o1, labelO1);
		creator.createOntologyGraph(o2, labelO2);

		//now matching the ontologies (i.e. using the graph representation of the ontologies)
		AlignmentProcess a = new GraphMatcher(ontologyParameter1, ontologyParameter2, db, weight);
		a.init(ontoFile1.toURI(), ontoFile2.toURI());
		Properties params = new Properties();
		params.setProperty("", "");
		a.align((Alignment)null, params);	
		AlignmentVisitor renderer = null; 
		BasicAlignment evaluatedAlignment = null;
		PrintWriter writer = null;

		//need to create the ontology graphs

		String alignmentFileName = storePath + "/" + StringUtilities.stripOntologyName(ontoFile1.toString()) + 
				"-" + StringUtilities.stripOntologyName(ontoFile2.toString()) + "-GraphMatcher.rdf";

		File outputAlignment = new File(alignmentFileName);


		writer = new PrintWriter(
				new BufferedWriter(
						new FileWriter(outputAlignment)), true); 
		renderer = new RDFRendererVisitor(writer);

		evaluatedAlignment = (BasicAlignment)(a.clone());

		evaluatedAlignment.normalise();

		evaluatedAlignment.render(renderer);
		writer.flush();
		writer.close();

	}

	private static void runLexicalMatcher(double weight) throws AlignmentException, URISyntaxException, IOException {

		AlignmentProcess a = new LexicalMatcherWordNet(weight);
		a.init(ontoFile1.toURI(), ontoFile2.toURI());
		Properties params = new Properties();
		params.setProperty("", "");
		a.align((Alignment)null, params);	
		AlignmentVisitor renderer = null;
		BasicAlignment evaluatedAlignment = null;
		PrintWriter writer = null;

		String alignmentFileName = storePath + "/" + StringUtilities.stripOntologyName(ontoFile1.toString()) + 
				"-" + StringUtilities.stripOntologyName(ontoFile2.toString()) + "-LexicalMatcher.rdf";

		File outputAlignment = new File(alignmentFileName);


		writer = new PrintWriter(
				new BufferedWriter(
						new FileWriter(outputAlignment)), true); 
		renderer = new RDFRendererVisitor(writer);

		evaluatedAlignment = (BasicAlignment)(a.clone());

		evaluatedAlignment.normalise();

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



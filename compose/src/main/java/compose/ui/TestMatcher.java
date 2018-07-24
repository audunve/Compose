package compose.ui;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.Properties;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
//Alignment API classes
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import compose.graph.GraphCreator;
import compose.matchers.equivalence.EditMatcher;
import compose.matchers.equivalence.ISubMatcher;
import compose.matchers.equivalence.InstanceMatcher;
import compose.matchers.equivalence.SuperclassMatcher;
import compose.matchers.equivalence.TrigramMatcher;
import compose.matchers.equivalence.WordNetMatcher;
import compose.matchers.subsumption.AncestorMatcher;
import compose.matchers.subsumption.CompoundMatcher;
import compose.matchers.subsumption.ParentMatcher;
import compose.matchers.subsumption.WNHyponymMatcher;
import compose.misc.StringUtilities;
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;


public class TestMatcher {
	
	//static Logger logger = LoggerFactory.getLogger(AncestorMatcher.class);

	public static void main(String[] args) throws AlignmentException, IOException, URISyntaxException, OWLOntologyCreationException {
		
		//logger.info("Hello from TestMatcher");

		/*** 1. SELECT THE MATCHER TO BE RUN ***/
		final String MATCHER = "SUBSUMPTION_PATH";

		/*** 2. SELECT THE TWO ONTOLOGIES TO BE MATCHED ***/
		//File ontoFile1 = new File("./files/wndomainexperiment/SchemaOrg/schema-org.owl");
		//File ontoFile2 = new File("./files/wndomainexperiment/efrbroo.owl");
		
		File ontoFile1 = new File("./files/ontologies/biblio_2015.rdf");
		File ontoFile2 = new File("./files/ontologies/BIBO.owl");
		//File ontoFile1 = new File("./files/Path/schema-org.owl");
		//File ontoFile2 = new File("./files/Path/schema-org.owl");
		
		///Users/audunvennesland/Documents/phd/development/Neo4J_new
		
		/*** 3. SELECT THE NEO4J DATABASE FILE (FOR THE STRUCTURAL MATCHERS ONLY) ***/
		final File dbFile = new File("/Users/audunvennesland/Documents/phd/development/Neo4J_new/test");
		

		/*** INITIAL VALUES, NO NEED TO TOUCH THESE ***/
		double threshold;
		String alignmentFileName = null;
		File outputAlignment = null;
		String ontologyParameter1 = null;
		String ontologyParameter2 = null;
		PrintWriter writer = null;
		AlignmentVisitor renderer = null;
		Properties params = new Properties();
		AlignmentProcess a = null;
		
		/*** USED FOR INCLUDING THE ONTOLOGY FILE NAMES IN THE COMPUTED ALIGNMENT FILE ***/
		String onto1 = StringUtilities.stripPath(ontoFile1.toString());
		String onto2 = StringUtilities.stripPath(ontoFile2.toString());

		switch(MATCHER) {

		case "STRING":
			a = new ISubMatcher();
			//a = new EditMatcher();
			//a = new TrigramMatcher();
			threshold = 0.8;

			a.init(ontoFile1.toURI(), ontoFile2.toURI());
			params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	
			
			System.err.println("The a alignment contains " + a.nbCells() + " correspondences");

			alignmentFileName = "./files/wndomainsexperiment/alignments/" + onto1 + "-" + onto2 + "-ISub08.rdf";

			outputAlignment = new File(alignmentFileName);

			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(outputAlignment)), true); 
			renderer = new RDFRendererVisitor(writer);

			BasicAlignment StringAlignment = (BasicAlignment)(a.clone());
			
			

			StringAlignment.cut(threshold);

			StringAlignment.render(renderer);
			
			System.err.println("The StringAlignment contains " + StringAlignment.nbCells() + " correspondences");
			writer.flush();
			writer.close();

			System.out.println("Matching completed!");
			break;
			
			
		case "INSTANCE":
			
			a = new InstanceMatcher();
			threshold = 0.1;

			a.init(ontoFile1.toURI(), ontoFile2.toURI());
			params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	

			//alignmentFileName = "./files/UoA/test/" + onto1 + "-" + onto2 + "/Instances.rdf";

			outputAlignment = new File(alignmentFileName);

			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(outputAlignment)), true); 
			renderer = new RDFRendererVisitor(writer);

			BasicAlignment InstanceAlignment = (BasicAlignment)(a.clone());

			InstanceAlignment.cut(threshold);

			InstanceAlignment.render(renderer);
			writer.flush();
			writer.close();

			System.out.println("Matching completed!");
			break;
			
			

		case "WORDNET":
			
			a = new WordNetMatcher();
			threshold = 0.6;

			a.init(ontoFile1.toURI(), ontoFile2.toURI());
			params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	

			alignmentFileName = "./files/COMPOSE-ClassEq_WordNet.rdf";

			outputAlignment = new File(alignmentFileName);

			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(outputAlignment)), true); 
			renderer = new RDFRendererVisitor(writer);

			BasicAlignment wordNetAlignment = (BasicAlignment)(a.clone());

			wordNetAlignment.cut(threshold);

			wordNetAlignment.render(renderer);
			writer.flush();
			writer.close();

			System.out.println("Matching completed!");
			break;

		case "GRAPHALIGNMENT":

			GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(dbFile);
			registerShutdownHook(db);

			ontologyParameter1 = StringUtilities.stripPath(ontoFile1.toString());
			ontologyParameter2 = StringUtilities.stripPath(ontoFile2.toString());

			System.out.println("Passing " + ontologyParameter1 + " and " + ontologyParameter2 + " to the structural matcher");
			a = new SuperclassMatcher(ontologyParameter1,ontologyParameter2, db);
			threshold = 0.6;

			a.init(ontoFile1.toURI(), ontoFile2.toURI());
			params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	
			
			//alignmentFileName = "./files/OAEI2011/" + onto1 + "-" + onto2 + "/COMPOSE-ClassEq_Graph.rdf";		

			outputAlignment = new File(alignmentFileName);

			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(outputAlignment)), true); 
			renderer = new RDFRendererVisitor(writer);

			BasicAlignment structuralAlignment = (BasicAlignment)(a.clone());

			structuralAlignment.cut(threshold);

			structuralAlignment.render(renderer);
			
			writer.flush();
			writer.close();

			System.out.println("Matching completed!");
			break;
			
		/*case "SUBSUMPTION_SUBCLASS":

			db = new GraphDatabaseFactory().newEmbeddedDatabase(dbFile);
			registerShutdownHook(db);

			ontologyParameter1 = StringUtils.stripPath(ontoFile1.toString());
			ontologyParameter2 = StringUtils.stripPath(ontoFile2.toString());
			System.out.println("Passing " + ontologyParameter1 + " and " + ontologyParameter2 + " to Subsumption_SubClass_Alignment.java");
			
			//create new graphs
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			OWLOntology o1 = manager.loadOntologyFromOntologyDocument(ontoFile1);
			OWLOntology o2 = manager.loadOntologyFromOntologyDocument(ontoFile2);
			Label labelO1 = DynamicLabel.label( ontologyParameter1 );
			Label labelO2 = DynamicLabel.label( ontologyParameter2 );
			
			GraphCreator creator = new GraphCreator(db);
			creator.createOntologyGraph(o1, labelO1);
			creator.createOntologyGraph(o2, labelO2);
			
			//perform the matching
			a = new ParentMatcher(ontologyParameter1,ontologyParameter2, db);
			threshold = 0.6;
			a.init(ontoFile1.toURI(), ontoFile2.toURI());
			params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	
		
			alignmentFileName = "./files/PathMatcher/TestPathMatcher.rdf";	

			outputAlignment = new File(alignmentFileName);

			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(outputAlignment)), true); 
			renderer = new RDFRendererVisitor(writer);

			BasicAlignment subsumptionSubClassAlignment = (BasicAlignment)(a.clone());

			subsumptionSubClassAlignment.cut(threshold);

			subsumptionSubClassAlignment.render(renderer);
			writer.flush();
			writer.close();

			System.out.println("Matching completed!");
			break;
			*/
	
			
		case "SUBSUMPTION_PATH":

			db = new GraphDatabaseFactory().newEmbeddedDatabase(dbFile);
			registerShutdownHook(db);

			ontologyParameter1 = StringUtilities.stripPath(ontoFile1.toString());
			ontologyParameter2 = StringUtilities.stripPath(ontoFile2.toString());
			System.out.println("Passing " + ontologyParameter1 + " and " + ontologyParameter2 + " to Subsumption_SubClass_Alignment.java");
			
			//create new graphs
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			OWLOntology o1 = manager.loadOntologyFromOntologyDocument(ontoFile1);
			OWLOntology o2 = manager.loadOntologyFromOntologyDocument(ontoFile2);
			Label labelO1 = DynamicLabel.label( ontologyParameter1 );
			Label labelO2 = DynamicLabel.label( ontologyParameter2 );
			
			GraphCreator creator = new GraphCreator(db);
			creator.createOntologyGraph(o1, labelO1);
			creator.createOntologyGraph(o2, labelO2);
			
			//perform the matching
			a = new AncestorMatcher(ontologyParameter1,ontologyParameter2, db);
			threshold = 0.6;
			a.init(ontoFile1.toURI(), ontoFile2.toURI());
			params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	
		
			alignmentFileName = "./files/Path/PathMatcher-biblio2bibo.rdf";	

			outputAlignment = new File(alignmentFileName);

			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(outputAlignment)), true); 
			renderer = new RDFRendererVisitor(writer);

			BasicAlignment subsumptionPathAlignment = (BasicAlignment)(a.clone());

			subsumptionPathAlignment.cut(threshold);

			subsumptionPathAlignment.render(renderer);
			writer.flush();
			writer.close();

			System.out.println("Matching completed!");
			break;

		case "SUBSUMPTION_COMPOUND":
			
			a = new CompoundMatcher();
			threshold = 0.6;

			a.init(ontoFile1.toURI(), ontoFile2.toURI());
			params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	

			//alignmentFileName = "./files/OAEI-16-conference/alignments/" + onto1 + "-" + onto2 + "/COMPOSE-Subsumption_String.rdf";		

			outputAlignment = new File(alignmentFileName);

			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(outputAlignment)), true); 
			renderer = new RDFRendererVisitor(writer);

			BasicAlignment subsumptionCompoundAlignment = (BasicAlignment)(a.clone());

			subsumptionCompoundAlignment.cut(threshold);

			subsumptionCompoundAlignment.render(renderer);
			writer.flush();
			writer.close();

			System.out.println("Matching completed!");
			break;

		

		case "SUBSUMPTION_WORDNET":
			
			a = new WNHyponymMatcher();
			threshold = 0.6;

			a.init(ontoFile1.toURI(), ontoFile2.toURI());
			params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	

			//alignmentFileName = "./files/OAEI-16-conference/alignments/" + onto1 + "-" + onto2 + "/Subsumption_WordNet.rdf";	

			outputAlignment = new File(alignmentFileName);

			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(outputAlignment)), true); 
			renderer = new RDFRendererVisitor(writer);

			BasicAlignment subsumptionWordNetAlignment = (BasicAlignment)(a.clone());

			subsumptionWordNetAlignment.cut(threshold);

			subsumptionWordNetAlignment.render(renderer);
			writer.flush();
			writer.close();

			System.out.println("Matching completed!");
			break;
			

		
			
		


		}

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
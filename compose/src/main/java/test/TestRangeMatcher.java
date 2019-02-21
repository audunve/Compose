package test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import equivalencematching.PropertyMatcher;
import equivalencematching.RangeMatcher;
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import net.didion.jwnl.JWNLException;
import ontologyprofiling.OntologyProfiler;
import utilities.StringUtilities;

public class TestRangeMatcher {
	
	final static File ontoFile1 = new File("./files/SATest1.owl");
	final static File ontoFile2 = new File("./files/SATest2.owl");
	final static String prefix = "file:";
	final static String storePath = "./files/_PHD_EVALUATION/BIBFRAME-SCHEMAORG/HARMONY";
	
	public static void main(String[] args) throws ClassNotFoundException, IOException, OWLOntologyCreationException, JWNLException, AlignmentException, URISyntaxException {
		
		System.out.println("...creating ontology profiles...");
		Map<String, Double> ontologyProfilingScores = OntologyProfiler.computeOntologyProfileScores(ontoFile1, ontoFile2);
		System.out.println("...ontology profiles created...");
		
		System.out.println("The ontology profile scores are: ");
		for (Entry<String, Double> e : ontologyProfilingScores.entrySet()) {
			System.out.println("Profiling metric: " + e.getKey() + ", Score: " + e.getValue());
		}
		
		System.out.println("\nRunning Range Matcher (RM)");
		long startTime = System.currentTimeMillis();
		runRangeMatcher(ontologyProfilingScores.get("pf"));
		long endTime = System.currentTimeMillis();

		long duration = (endTime - startTime); 
		
		System.out.println("The Property Matcher took " + (duration/1000) + " seconds to complete");
		
		
	}
	
	private static void runRangeMatcher(double weight) throws AlignmentException, URISyntaxException, IOException, OWLOntologyCreationException {


		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto1 = manager.loadOntologyFromOntologyDocument(ontoFile1);
		OWLOntology onto2 = manager.loadOntologyFromOntologyDocument(ontoFile2);

		AlignmentProcess a = new RangeMatcher(onto1, onto2, weight);
		a.init(ontoFile1.toURI(), ontoFile2.toURI());
		Properties params = new Properties();
		params.setProperty("", "");
		a.align((Alignment)null, params);	
		AlignmentVisitor renderer = null;
		BasicAlignment evaluatedAlignment = null;
		PrintWriter writer = null;

		String alignmentFileName = storePath + "/" + StringUtilities.stripOntologyName(ontoFile1.toString()) + 
				"-" + StringUtilities.stripOntologyName(ontoFile2.toString()) + "-RangeMatcherv2.rdf";

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

}

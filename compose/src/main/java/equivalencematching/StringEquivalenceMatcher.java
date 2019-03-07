package equivalencematching;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.Properties;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import evaluation.Evaluator;
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.ontowrap.OntowrapException;
import matchercombination.Harmony;
import matchercombination.HarmonySubsumption;
import subsumptionmatching.CompoundMatcher;
import subsumptionmatching.LexicalSubsumptionMatcher;
import utilities.AlignmentOperations;
import utilities.ISub;

/**
 * This string matcher implements the iSub string matching algorithm written by Stolios et al in the paper "A String Metric for Ontology Alignment".
 * @author audunvennesland
 * 2. feb. 2017
 */
public class StringEquivalenceMatcher extends ObjectAlignment implements AlignmentProcess {

	ISub isubMatcher = new ISub();
	
	double weight;
	
	
	public StringEquivalenceMatcher(double weight) {
		
		this.weight = weight;
		
	}
	
	//test method
		public static void main(String[] args) throws OWLOntologyCreationException, AlignmentException, URISyntaxException, IOException {

//			File ontoFile1 = new File("./files/_PHD_EVALUATION/BIBFRAME-SCHEMAORG/ONTOLOGIES/bibframe.rdf");
//			File ontoFile2 = new File("./files/_PHD_EVALUATION/BIBFRAME-SCHEMAORG/ONTOLOGIES/schema-org.owl");
//			String referenceAlignment = "./files/_PHD_EVALUATION/BIBFRAME-SCHEMAORG/REFALIGN/ReferenceAlignment-BIBFRAME-SCHEMAORG-EQUIVALENCE.rdf";

			File ontoFile1 = new File("./files/_PHD_EVALUATION/ATMONTO-AIRM/ONTOLOGIES/ATMOntoCoreMerged.owl");
			File ontoFile2 = new File("./files/_PHD_EVALUATION/ATMONTO-AIRM/ONTOLOGIES/airm-mono.owl");
			String referenceAlignment = "./files/_PHD_EVALUATION/ATMONTO-AIRM/REFALIGN/ReferenceAlignment-ATMONTO-AIRM-EQUIVALENCE.rdf";

			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			OWLOntology sourceOntology = manager.loadOntologyFromOntologyDocument(ontoFile1);
			OWLOntology targetOntology = manager.loadOntologyFromOntologyDocument(ontoFile2);

			double testWeight = 1.0;

			AlignmentProcess a = new StringEquivalenceMatcher(testWeight);
			a.init(ontoFile1.toURI(), ontoFile2.toURI());
			Properties params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	
			BasicAlignment stringEquivalenceMatcherAlignment = new BasicAlignment();

			stringEquivalenceMatcherAlignment = (BasicAlignment) (a.clone());

			stringEquivalenceMatcherAlignment.normalise();

			//evaluate the Harmony alignment
			BasicAlignment harmonyAlignment = Harmony.getHarmonyAlignment(stringEquivalenceMatcherAlignment);
			System.out.println("The Harmony alignment contains " + harmonyAlignment.nbCells() + " cells");
			Evaluator.evaluateSingleAlignment(harmonyAlignment, referenceAlignment);

			System.out.println("Printing Harmony Alignment: ");
			for (Cell c : harmonyAlignment) {
				System.out.println(c.getObject1() + " " + c.getObject2() + " " + c.getRelation().getRelation() + " " + c.getStrength());
			}

			

			System.out.println("\nThe alignment contains " + stringEquivalenceMatcherAlignment.nbCells() + " relations");

			System.out.println("Evaluation with no cut threshold:");
			Evaluator.evaluateSingleAlignment(stringEquivalenceMatcherAlignment, referenceAlignment);

			System.out.println("Evaluation with threshold 0.2:");
			stringEquivalenceMatcherAlignment.cut(0.2);
			Evaluator.evaluateSingleAlignment(stringEquivalenceMatcherAlignment, referenceAlignment);

			System.out.println("Evaluation with threshold 0.4:");
			stringEquivalenceMatcherAlignment.cut(0.4);
			Evaluator.evaluateSingleAlignment(stringEquivalenceMatcherAlignment, referenceAlignment);

			System.out.println("Evaluation with threshold 0.6:");
			stringEquivalenceMatcherAlignment.cut(0.4);
			Evaluator.evaluateSingleAlignment(stringEquivalenceMatcherAlignment, referenceAlignment);

			System.out.println("Evaluation with threshold 0.9:");
			stringEquivalenceMatcherAlignment.cut(0.9);
			Evaluator.evaluateSingleAlignment(stringEquivalenceMatcherAlignment, referenceAlignment);

			System.out.println("Printing relations at 0.9:");
			for (Cell c : stringEquivalenceMatcherAlignment) {
				System.out.println(c.getObject1() + " " + c.getObject2() + " " + c.getRelation().getRelation() + " " + c.getStrength());
			}

		}

	public void align(Alignment alignment, Properties param) throws AlignmentException {

		try {
			// Match classes
			for ( Object cl2: ontology2().getClasses() ){
				for ( Object cl1: ontology1().getClasses() ){

					// add mapping into alignment object 
					addAlignCell(cl1,cl2, "=", weight*iSubScore(cl1,cl2));  
				}

			}

		} catch (Exception e) { e.printStackTrace(); }
	}

	/**
	 * This method returns a measure computed from two input OWL entities (processed as strings) using iSub algorithm (Stolios et al, 2005)
	 * @param o1 object representing an OWL entitiy
	 * @param o2 object representing an OWL entitiy
	 * @return a similarity scored computed from the ISub algorithm
	 * @throws OntowrapException
	 */
	public double iSubScore(Object o1, Object o2) throws OntowrapException {

		//get the objects (entities)
		String s1 = ontology1().getEntityName(o1).toLowerCase();
		String s2 = ontology2().getEntityName(o2).toLowerCase();

		double measure = isubMatcher.score(s1, s2);

		return measure;

	}
	

}



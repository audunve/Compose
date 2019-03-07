package equivalencematching;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import evaluation.Evaluator;
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import matchercombination.Harmony;
import wordembedding.VectorExtractor;

/**
 * Matches concepts from two ontologies based on their global vectors (average of label vectors and comment vectors).
 * @author audunvennesland
 *
 */
public class WELabelMatcher extends ObjectAlignment implements AlignmentProcess {

	double weight;
	static OWLOntology sourceOntology;
	static OWLOntology targetOntology;
	static Map<String, double[]> vectorMapSourceOntology = new HashMap<String, double[]>();
	static Map<String, double[]> vectorMapTargetOntology = new HashMap<String, double[]>();

	String vectorFile;

	public WELabelMatcher(OWLOntology onto1, OWLOntology onto2, String vectorFile, double weight) {
		this.weight = weight;
		sourceOntology = onto1;
		targetOntology = onto2;
		this.vectorFile = vectorFile;
	}
	
	//test method
	public static void main(String[] args) throws OWLOntologyCreationException, AlignmentException, URISyntaxException, IOException {
		
//		File ontoFile1 = new File("./files/SATest1.owl");
//		File ontoFile2 = new File("./files/SATest2.owl");
//		String referenceAlignment = "./files/ReferenceAlignmentSATest.rdf";
//		String vectorFile = "./files//_PHD_EVALUATION/EMBEDDINGS/wikipedia_trained.txt";

		File ontoFile1 = new File("./files/_PHD_EVALUATION/BIBFRAME-SCHEMAORG/ONTOLOGIES/bibframe.rdf");
		File ontoFile2 = new File("./files/_PHD_EVALUATION/BIBFRAME-SCHEMAORG/ONTOLOGIES/schema-org.owl");
		String referenceAlignment = "./files/_PHD_EVALUATION/BIBFRAME-SCHEMAORG/REFALIGN/ReferenceAlignment-BIBFRAME-SCHEMAORG-EQUIVALENCE.rdf";
		String vectorFile = "./files//_PHD_EVALUATION/EMBEDDINGS/wikipedia_trained.txt";

//		File ontoFile1 = new File("./files/_PHD_EVALUATION/ATMONTO-AIRM/ONTOLOGIES/ATMOntoCoreMerged.owl");
//		File ontoFile2 = new File("./files/_PHD_EVALUATION/ATMONTO-AIRM/ONTOLOGIES/airm-mono.owl");
//		String referenceAlignment = "./files/_PHD_EVALUATION/ATMONTO-AIRM/REFALIGN/ReferenceAlignment-ATMONTO-AIRM-EQUIVALENCE.rdf";
//		String vectorFile = "./files//_PHD_EVALUATION/EMBEDDINGS/skybrary_trained.txt";

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology sourceOntology = manager.loadOntologyFromOntologyDocument(ontoFile1);
		OWLOntology targetOntology = manager.loadOntologyFromOntologyDocument(ontoFile2);

		double testWeight = 1.0;

		AlignmentProcess a = new WELabelMatcher(sourceOntology, targetOntology, vectorFile, testWeight);
		a.init(ontoFile1.toURI(), ontoFile2.toURI());
		Properties params = new Properties();
		params.setProperty("", "");
		a.align((Alignment)null, params);	
		BasicAlignment weLabelMatcherAlignment = new BasicAlignment();

		weLabelMatcherAlignment = (BasicAlignment) (a.clone());

		weLabelMatcherAlignment.normalise();
		
		File outputAlignment = new File("./files/weLabelAlignmentATMONTO-AIRM.rdf");

		PrintWriter writer = new PrintWriter(
				new BufferedWriter(
						new FileWriter(outputAlignment)), true); 
		AlignmentVisitor renderer = new RDFRendererVisitor(writer);

		weLabelMatcherAlignment.render(renderer);
		
		System.err.println("The weLabelMatcherAlignment contains " + weLabelMatcherAlignment.nbCells() + " correspondences");
		writer.flush();
		writer.close();

		//evaluate the Harmony alignment
		BasicAlignment harmonyAlignment = Harmony.getHarmonyAlignment(weLabelMatcherAlignment);
		System.out.println("The Harmony alignment contains " + harmonyAlignment.nbCells() + " cells");
		Evaluator.evaluateSingleAlignment(harmonyAlignment, referenceAlignment);

		System.out.println("Printing Harmony Alignment: ");
		for (Cell c : harmonyAlignment) {
			System.out.println(c.getObject1() + " " + c.getObject2() + " " + c.getRelation().getRelation() + " " + c.getStrength());
		}

		System.out.println("\nThe alignment contains " + weLabelMatcherAlignment.nbCells() + " relations");

		System.out.println("Evaluation with no cut threshold:");
		Evaluator.evaluateSingleAlignment(weLabelMatcherAlignment, referenceAlignment);

		System.out.println("Evaluation with threshold 0.2:");
		weLabelMatcherAlignment.cut(0.2);
		Evaluator.evaluateSingleAlignment(weLabelMatcherAlignment, referenceAlignment);

		System.out.println("Evaluation with threshold 0.4:");
		weLabelMatcherAlignment.cut(0.4);
		Evaluator.evaluateSingleAlignment(weLabelMatcherAlignment, referenceAlignment);

		System.out.println("Evaluation with threshold 0.6:");
		weLabelMatcherAlignment.cut(0.4);
		Evaluator.evaluateSingleAlignment(weLabelMatcherAlignment, referenceAlignment);

		System.out.println("Evaluation with threshold 0.9:");
		weLabelMatcherAlignment.cut(0.9);
		Evaluator.evaluateSingleAlignment(weLabelMatcherAlignment, referenceAlignment);

		System.out.println("Printing relations at 0.9:");
		for (Cell c : weLabelMatcherAlignment) {
			System.out.println(c.getObject1() + " " + c.getObject2() + " " + c.getRelation().getRelation() + " " + c.getStrength());
		}

	}
	
	
	public static Map<String, double[]> createVectorMap (OWLOntology onto, String vectorFile) throws IOException {
		
		Map<String, double[]> vectors = new HashMap<String, double[]>();
		
		//create the vector map from the source vector file
		Map<String, ArrayList<Double>> vectorMap = VectorExtractor.createVectorMap (new File(vectorFile));
		ArrayList<Double> labelVector = new ArrayList<Double>();
		
		
		for (OWLClass cls : onto.getClassesInSignature()) {

			if (vectorMap.containsKey(cls.getIRI().getFragment().toLowerCase())) {
				
				
				labelVector = VectorExtractor.getLabelVector(cls.getIRI().getFragment(), vectorMap);
				
				double[] labelVectorArray = new double[labelVector.size()];
				for (int i = 0; i < labelVectorArray.length; i++) {
					labelVectorArray[i] = labelVector.get(i);
				}
				vectors.put(cls.getIRI().getFragment().toLowerCase(), labelVectorArray);
			}

			
		}
		
		
		return vectors;
		
		
	}

	public void align(Alignment alignment, Properties param) throws AlignmentException {

		try {
			vectorMapSourceOntology = createVectorMap(sourceOntology, vectorFile);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		try {
			vectorMapTargetOntology = createVectorMap(targetOntology, vectorFile);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		double[] sourceVectors = null;
		double[] targetVectors = null;

		double cosineSim = 0;
		

		try {
			// Match classes
			for ( Object sourceObject: ontology1().getClasses() ){
				for ( Object targetObject: ontology2().getClasses() ){

					String source = ontology1().getEntityName(sourceObject).toLowerCase();
					String target = ontology2().getEntityName(targetObject).toLowerCase();

					if (vectorMapSourceOntology.containsKey(source) && vectorMapTargetOntology.containsKey(target)) {
						
						sourceVectors = vectorMapSourceOntology.get(source);
						targetVectors = vectorMapTargetOntology.get(target);

						//ensure that both vectors have the same, correct size (not sure why they shouldn´t be...)
						if (sourceVectors.length == 300 && targetVectors.length == 300) {

							cosineSim = utilities.Cosine.cosineSimilarity(sourceVectors, targetVectors);

							addAlignCell(sourceObject, targetObject, "=", cosineSim);

						} else {
							addAlignCell(sourceObject, targetObject, "=", 0);

						}


					} else {
						addAlignCell(sourceObject, targetObject, "=", 0);
						
					}

				}
			}

		} catch (Exception e) { e.printStackTrace(); }
		



	}


}



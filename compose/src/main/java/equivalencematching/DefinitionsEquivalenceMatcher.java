package equivalencematching;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.ontosim.vector.CosineVM;
import utilities.OntologyOperations;
import utilities.VectorExtractor;

public class DefinitionsEquivalenceMatcher extends ObjectAlignment implements AlignmentProcess {

	double weight;
	static OWLOntology onto1;
	static OWLOntology onto2;
	static Map<String, Set<String>> classAndDefMapOnto1 = new HashMap<String, Set<String>>();
	static Map<String, Set<String>> classAndDefMapOnto2 = new HashMap<String, Set<String>>();

	static Map<String, ArrayList<Double>> wordAndVecMap = new HashMap<String, ArrayList<Double>>();

	public DefinitionsEquivalenceMatcher(OWLOntology onto1, OWLOntology onto2, Map<String, ArrayList<Double>> wordAndVecMap, double weight) {

		this.onto1 = onto1;
		this.onto2 = onto2;
		this.weight = weight;
		this.wordAndVecMap = wordAndVecMap;

	}

	//test method
	public static void main(String[] args) throws AlignmentException, IOException, OWLOntologyCreationException {
		File ontoFile1 = new File("./files/_PHD_EVALUATION/BIBFRAME-SCHEMAORG/ONTOLOGIES/bibframe.rdf");
		File ontoFile2 = new File("./files/_PHD_EVALUATION/BIBFRAME-SCHEMAORG/ONTOLOGIES/schema-org.owl");
		File vectorFile = new File("./files/_PHD_EVALUATION/EMBEDDINGS/processedFileWikipedia_lemmatized.txt");

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto1 = manager.loadOntologyFromOntologyDocument(ontoFile1);
		OWLOntology onto2 = manager.loadOntologyFromOntologyDocument(ontoFile2);

		Map<String, ArrayList<Double>> wordAndVecMap = VectorExtractor.createVectorMap(vectorFile);

		double testWeight = 1.0;

		AlignmentProcess a = new DefinitionsEquivalenceMatcher(onto1, onto2, wordAndVecMap, testWeight);
		a.init(ontoFile1.toURI(), ontoFile2.toURI());
		Properties params = new Properties();
		params.setProperty("", "");
		a.align((Alignment)null, params);	
		AlignmentVisitor renderer = null;
		BasicAlignment evaluatedAlignment = null;
		PrintWriter writer = null;

		String alignmentFileName = "./files/_PHD_EVALUATION/MATCHERTESTING/DefinitionsMatcher.rdf";

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

	public void align(Alignment alignment, Properties param) throws AlignmentException {

		double defSim = 0;


		Map<String, Set<String>> classAndDefMapOnto1 = null;
		try {
			classAndDefMapOnto1 = createClassAndDefMap(onto1);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		Map<String, Set<String>> classAndDefMapOnto2 = null;
		try {
			classAndDefMapOnto2 = createClassAndDefMap(onto2);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		try {
			// Match classes
			for ( Object target: ontology2().getClasses() ){
				for ( Object source: ontology1().getClasses() ){

					String s1 = ontology1().getEntityName(source).toLowerCase();
					String s2 = ontology2().getEntityName(target).toLowerCase();

					//get the definitions of source
					Set<String> sourceDef = classAndDefMapOnto1.get(s1);

					//get the definitions o target
					Set<String> targetDef = classAndDefMapOnto2.get(s2);

					if (sourceDef == null || sourceDef.isEmpty() || targetDef == null || targetDef.isEmpty()) {
						defSim = 0;
					} else {

						defSim = computeDefSim(sourceDef, targetDef, wordAndVecMap);
					}

					// add mapping into alignment object 
					addAlignCell(source,target, "=", weight*defSim);  
				}

			}

		} catch (Exception e) { e.printStackTrace(); }
	}

	public static double computeDefSim(Set<String> def1, Set<String> def2, Map<String, ArrayList<Double>> wordAndVecMap) {
		double sim = 0;
		double thisSim = 0;


		if (def1.isEmpty() || def2.isEmpty()) {
			sim = 0;
		} else {
			int counter = 0;
			for (String s : def1) {
				for (String t : def2) {
					if (wordAndVecMap.containsKey(s) && wordAndVecMap.containsKey(t)) {
						thisSim += computeCosSim(wordAndVecMap.get(s), wordAndVecMap.get(t));
						counter ++;
					}
				}
			}
			
			//avoids NaN
			if (thisSim != 0 || counter != 0) {
			sim = thisSim / (double) counter;
			//System.out.println("sim is " + sim + " (thisSim = " + thisSim + " , counter = " + counter + ")");
			}
		}

		return sim;
	}

	public Map<String, Set<String>> createClassAndDefMap(OWLOntology onto) throws IOException {

		Map<String, Set<String>> classAndDefMap = new HashMap<String, Set<String>>();

		//get the definition tokens for each class c and lemmatize each token
		for (OWLClass c : onto.getClassesInSignature()) {
			classAndDefMap.put(c.getIRI().getFragment().toLowerCase(), OntologyOperations.getLemmatizedClassDefinitionTokensFull(onto, c));
		}

		return classAndDefMap;

	}

	private static double computeCosSim(ArrayList<Double> a1, ArrayList<Double> a2) {
		double sim = 0;
		double measure = 0;


		double[] vec1 = ArrayUtils.toPrimitive(a1.toArray((new Double[a1.size()])));	
		double[] vec2 = ArrayUtils.toPrimitive(a2.toArray((new Double[a2.size()])));	

		//measure the cosine similarity between the vector dimensions of these two entities
		CosineVM cosine = new CosineVM();

		if (vec1 != null && vec2 != null) {

			sim = cosine.getSim(vec1, vec2);

		}

		//need to keep our sim within [0..1] 
		if (sim > 0 && sim <= 1) {
			measure = sim;
		} else {
			measure = 0;
		}

		return measure;
	}
}

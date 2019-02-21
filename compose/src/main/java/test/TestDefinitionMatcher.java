package test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import fr.inrialpes.exmo.ontosim.vector.CosineVM;
import utilities.OntologyOperations;
import utilities.StringUtilities;
import utilities.VectorExtractor;

public class TestDefinitionMatcher {
	
	public static void main(String[] args) throws IOException, OWLOntologyCreationException {
		File vectorFile = new File("./files/_PHD_EVALUATION/EMBEDDINGS/processedFileWikipedia_lemmatized.txt");
		
//		String multimedia = "Electronic resource that is a computer program (i.e., digitally encoded instructions intended to be processed and performed by a computer) "
//				+ "or which consists of multiple media types that are software driven, such as videogames.";
		String multimedia = "Software or multimedia";
		String softwareApplication = "A software application.";
		
		//get lemmatized tokens from the defs
		Set<String> multimedia_set = StringUtilities.tokenizeAndLemmatizeToSet(multimedia, true);
		System.out.println("tokens from multmedia definition are");
		for (String s : multimedia_set) {
			System.out.println(s);
		}
		Set<String> softwareApplication_set = StringUtilities.tokenizeAndLemmatizeToSet(softwareApplication, true);
		
		Map<String, ArrayList<Double>> wordAndVecMap = VectorExtractor.createVectorMap(vectorFile);
		
		double sim = computeDefSim(multimedia_set, softwareApplication_set, wordAndVecMap);
		
		System.out.println("sim is " + sim);
		
		File ontoFile = new File("./files/bibframe.rdf");
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);
		
		readDefinitions(onto);
		
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
			System.out.println("sim is " + sim + " (thisSim = " + thisSim + " , counter = " + counter + ")");
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
	
	public static void readDefinitions(OWLOntology onto) {
		
		for (OWLClass c : onto.getClassesInSignature()) {
		Set<String> defs = OntologyOperations.getClassDefinitionsFull(onto, c);
		for (String s : defs) {
			System.out.println(s);
		}
		}
		
	}

}

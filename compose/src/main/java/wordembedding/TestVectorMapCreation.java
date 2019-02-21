package wordembedding;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import utilities.StringUtilities;

public class TestVectorMapCreation {
	
	public static void main(String[] args) throws OWLOntologyCreationException, IOException {
		
		
//		Dataset 1
//		File ontoFile1 = new File("./files/ESWC_ATMONTO_AIRM/ATMOntoCoreMerged.owl");
//		File ontoFile2 = new File("./files/ESWC_ATMONTO_AIRM/airm-mono.owl");
//		File vectorFile = new File ("./files/skybrary_trained.txt");
		
		
//		Dataset 2
		File vectorFile = new File("./files/wikipedia-300.txt");
		File ontoFile1 = new File("./files/ESWC_ATMONTO_AIRM/bibframe.rdf");
		File ontoFile2 = new File("./files/ESWC_ATMONTO_AIRM/schema-org.owl");
//		long startTime = System.nanoTime();
//		Map<String, ArrayList<Double>> vectorMap = VectorExtractor.createVectorMap(vectorFile);
//		long stopTime = System.nanoTime();
//		System.out.println("The vector-map creatino took: " + (stopTime - startTime) + " ms");
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto1 = manager.loadOntologyFromOntologyDocument(ontoFile1);
		OWLOntology onto2 = manager.loadOntologyFromOntologyDocument(ontoFile2);
		
		Set<String> tokens = allTokens (onto1, onto2);
		System.out.println("There are " + tokens.size() + " tokens in the set");
		
		System.out.println("Trying to reduce vector file");
		reduceVectorFile(vectorFile, tokens);
		System.out.println("Vector file reduced");

	}
	
	public static Set<String> allTokens (OWLOntology onto1, OWLOntology onto2) {
		
		List<String> stopWordsList = Arrays.asList(
				"a", "an", "and", "are", "as", "at", "be", "but", "by",
				"for", "if", "in", "into", "is", "it",
				"no", "not", "of", "on", "or", "such",
				"that", "the", "their", "then", "there", "these",
				"they", "this", "to", "was", "will", "with"
				);
		
		Set<String> objectPropertyTokens = new HashSet<String>();
		Set<OWLObjectProperty> ops1 = onto1.getObjectPropertiesInSignature();
		Set<OWLObjectProperty> ops2 = onto2.getObjectPropertiesInSignature();
		
		for (OWLObjectProperty op1 : ops1) {
			for (OWLObjectProperty op2 : ops2) {
				Set<OWLAnnotation> op1_def = op1.getAnnotations(onto1);
				Set<OWLAnnotation> op2_def = op2.getAnnotations(onto2);

				for (OWLAnnotation s : op1_def) {
					String[] op1_def_array = s.getValue().toString().replaceAll("[^a-zA-Z0-9\\s]", "").toLowerCase().split(" ");
					for (String tokens1 : op1_def_array) {
						//remove stopwords and words containing less than 2 chars
						if (!stopWordsList.contains(tokens1) && tokens1 != null && !tokens1.isEmpty() && tokens1.length() > 2) {
						objectPropertyTokens.add(tokens1.replace("xsdstring","").replaceAll("[\\d]", "" ));
						}
					}
				}
				
				for (OWLAnnotation s : op2_def) {
					String[] op2_def_array = s.getValue().toString().replaceAll("[^a-zA-Z0-9\\s]", "").toLowerCase().split(" ");
					for (String tokens2 : op2_def_array) {
						//remove stopwords and words containing less than 2 chars
						if (!stopWordsList.contains(tokens2) && tokens2 != null && !tokens2.isEmpty() && tokens2.length() > 2) {
						objectPropertyTokens.add(tokens2.replace("xsdstring","").replaceAll("[\\d]", "" ));
						}
					}
				}
				
				
			}
		}

		return objectPropertyTokens;
	}
	
	public static void reduceVectorFile (File initialVectorFile, Set<String> tokens) throws IOException {
		String output = "./files/wikipedia_reduced.txt";
		File newVectorFile = new File (output);
		
		//create a vectormap from initialVectorFile
		Map<String, ArrayList<Double>> initialVectorMap = new HashMap<String, ArrayList<Double>> ();
		initialVectorMap = VectorExtractor.createVectorMap(initialVectorFile);
		System.out.println("Initial vector map contains " + initialVectorMap.size() + " entries");
		
		Map<String, ArrayList<Double>> reducedVectorMap = new HashMap<String, ArrayList<Double>> ();
		
		
		for (String s : tokens) {
			if (initialVectorMap.containsKey(s)) {
				reducedVectorMap.put(s, initialVectorMap.get(s));
			}
		}
		
		//print reduced vector map to file
		PrintWriter writer = new PrintWriter(
				new BufferedWriter(
						new FileWriter(newVectorFile)), true); 
		
		for (Entry<String, ArrayList<Double>> e : reducedVectorMap.entrySet()) {
			
//			writer.println(e.getKey() + " " + Arrays.toString(e.getValue().toArray()));	
			writer.println(e.getKey() + " " + e.getValue());	
		}
		
		System.out.println("\nReduced vector map contains " + reducedVectorMap.size() + " entries");
		
		writer.flush();
		writer.close();
		
		//return newVectorFile;
	}
 
}

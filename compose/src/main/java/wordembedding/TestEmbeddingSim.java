package wordembedding;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLClass;

import mismatchdetection.VectorSim;
import utilities.OntologyOperations;
import utilities.StringUtilities;

public class TestEmbeddingSim {
	
	private static File vectorFile = new File("./files/skybrary_trained_reduced.txt");
	
	public static void main(String[] args) throws OWLOntologyCreationException, IOException, AlignmentException {
		File ontoFile1 = new File("./files/ESWC_ATMONTO_AIRM/ATMOntoCoreMerged.owl");
		File ontoFile2 = new File("./files/ESWC_ATMONTO_AIRM/airm-mono.owl");
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto1 = manager.loadOntologyFromOntologyDocument(ontoFile1);
		OWLOntology onto2 = manager.loadOntologyFromOntologyDocument(ontoFile2);
		
//		TESTING TWO OBJECT PROPERTIES
//		Set<OWLObjectProperty> propsTerminal1 = OntologyOperations.getObjectProperties(onto1, OntologyOperations.getClass("Terminal", onto1));
//		System.out.println("There are " + propsTerminal1.size() + " object properties associated with class Terminal in ATMONTO");
//		
//		Set<OWLObjectProperty> propsTerminal2 = OntologyOperations.getObjectProperties(onto2, OntologyOperations.getClass("Terminal", onto2));
//		System.out.println("There are " + propsTerminal2.size() + " object properties associated with class Terminal in AIRM");
//		
//		double sim = embeddingSim(onto1, onto2, propsTerminal1, propsTerminal2);
//		
//		System.out.println("The sim between these classes is " + sim);
		
		
//		TESTING WHOLE ONTOLOGIES
		Set<String> bestRelations = new HashSet<String>();
		
		double embeddingScore = 0;
		
		for (OWLClass c1 : onto1.getClassesInSignature()) {
			for (OWLClass c2 : onto2.getClassesInSignature()) {
				//System.out.println("\nThe classes being matched are " + c1.getIRI().getFragment() + " and " + c2.getIRI().getFragment());
				embeddingScore = embeddingSim(onto1, onto2, OntologyOperations.getObjectProperties(onto1, c1), OntologyOperations.getObjectProperties(onto2, c2));
				
				if (embeddingScore > 0.80) {
					System.out.println("***THE EMBEDDING SIM IS ABOVE 0.80: " + embeddingScore);
					System.out.println("Adding " + c1.getIRI().getFragment() + " - " + c2.getIRI().getFragment() + ": " + String.valueOf(embeddingScore) + " to set");
					bestRelations.add(c1.getIRI().getFragment() + " - " + c2.getIRI().getFragment() + String.valueOf(embeddingScore));
				}
			}
		}
		
		System.out.println("The set bestRelations contains " + bestRelations.size() + " items");
	}
	
	
	public static double embeddingSim(OWLOntology onto1, OWLOntology onto2, Set<OWLObjectProperty> propSet1, Set<OWLObjectProperty> propSet2) throws IOException, AlignmentException {
		Map<String, ArrayList<Double>> vectorMap = VectorExtractor.createVectorMap(vectorFile);
		

		Set<String> op_ci_defs = null;
		Set<String> op_cj_defs = null;
		double embeddingSim = 0;
		int counter = 0;

		for (OWLObjectProperty op_ci : propSet1) {
			for (OWLObjectProperty op_cj : propSet2) {
				counter++;
				//System.err.println("The object properties matched are " + op_ci.getIRI().getFragment() + " and " + op_cj.getIRI().getFragment());
				
				StringBuilder ops1SB = new StringBuilder();
				StringBuilder ops2SB = new StringBuilder();
				
				op_ci_defs = OntologyOperations.getOPDefinitions(onto1, op_ci);
				for (String s : op_ci_defs) {
					ops1SB.append(StringUtilities.removeStopWords(s.replaceAll("[^a-zA-Z0-9\\s]", ""))); //remove all non-word characters/symbols
				}
				
				op_cj_defs = OntologyOperations.getOPDefinitions(onto2, op_cj);
				for (String s : op_cj_defs) {
					ops2SB.append(StringUtilities.removeStopWords(s.replaceAll("[^a-zA-Z0-9\\s]", ""))); //remove all non-word characters/symbols
				}
				
				//Have to ensure that neither of the stringbuilders are empty
				if (ops1SB.toString().trim().length() > 0 && ops2SB.toString().trim().length() > 0) {

					//System.err.println("Measuring embedding sim between " + ops1SB + " and " + ops2SB);
					embeddingSim += VectorSim.computeDefSim(ops1SB.toString(), ops2SB.toString(), vectorMap);
					//System.err.println("embeddingSim is " + embeddingSim);
				} else {
					embeddingSim = 0;
				}
			}
		}

		//System.out.println("Counter is " + counter);
		return (double)embeddingSim/(double) counter;
	}
	
	

}

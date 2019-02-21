package test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import utilities.OntologyOperations;
import utilities.WordNet;

public class TestDefinitionsSubsumptionMatcher {
	
	public static void main(String[] args) throws OWLOntologyCreationException, IOException {
		
		File ontoFile1 = new File("./files/bibframe.rdf");
		File ontoFile2 = new File("./files/schema-org.owl");
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto1 = manager.loadOntologyFromOntologyDocument(ontoFile1);
		OWLOntology onto2 = manager.loadOntologyFromOntologyDocument(ontoFile2);
				
		System.out.println("Preparing classAndDefMaps...");
		Map<String, Set<String>> classAndDefMapOnto1 = createClassAndDefMap(onto1);
		Map<String, Set<String>> classAndDefMapOnto2 = createClassAndDefMap(onto2);
		System.out.println("Finished preparing classAndDefMaps...");
		

		
		for (Entry<String, Set<String>> eS : classAndDefMapOnto1.entrySet()) {
			for (Entry<String, Set<String>> eT : classAndDefMapOnto2.entrySet()) {
				int hyponymCounter = 0;
				int hypernymCounter = 0;
				
				for (String sourceDef : eS.getValue()) {
					for (String targetDef : eT.getValue()) {
						if (WordNet.isHyponym(sourceDef, targetDef)) {
							hyponymCounter++;
						} else if (WordNet.isHyponym(targetDef, sourceDef)) {
							hypernymCounter++;
						}
					}
				}
				
				if (hyponymCounter >= 1 || hypernymCounter >= 1) 
				System.out.println(eS.getKey() + " - " + eT.getKey() + ": Hyponym: " + hyponymCounter + " | Hypernym: " + hypernymCounter);
				
//				System.out.println("HyponymCounter is " + hyponymCounter);
//				System.out.println("HypernymCounter is " + hypernymCounter);
			}
		}
		
		

		
		
	}


	public static Map<String, Set<String>> createClassAndDefMap(OWLOntology onto) throws IOException {

		Map<String, Set<String>> classAndDefMap = new HashMap<String, Set<String>>();

		//get the definition tokens for each class c and lemmatize each token
		for (OWLClass c : onto.getClassesInSignature()) {
			classAndDefMap.put(c.getIRI().getFragment().toLowerCase(), OntologyOperations.getLemmatizedClassDefinitionTokensFull(onto, c));
		}

		return classAndDefMap;

	}
	

}

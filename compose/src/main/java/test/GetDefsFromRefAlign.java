package test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import utilities.OntologyOperations;

import org.semanticweb.owl.align.Cell;

public class GetDefsFromRefAlign {

	public static void main(String[] args) throws AlignmentException, OWLOntologyCreationException, IOException {

		File refAlign = new File("./files/_PHD_EVALUATION/BIBFRAME-SCHEMAORG/REFALIGN/ReferenceAlignment-BIBFRAME-SCHEMAORG-SUB.rdf");
		File ontoFile1 = new File("./files/bibframe.rdf");
		File ontoFile2 = new File("./files/schema-org.owl");
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto1 = manager.loadOntologyFromOntologyDocument(ontoFile1);
		OWLOntology onto2 = manager.loadOntologyFromOntologyDocument(ontoFile2);
		AlignmentParser parser = new AlignmentParser();
		BasicAlignment referenceAlignment = (BasicAlignment) parser.parse(refAlign.toURI().toString());

		System.out.println("Preparing classAndDefMaps...");
		Map<String, Set<String>> fullDefMapOnto1 = fullDefinitionsMap(onto1);
		Map<String, Set<String>> fullDefMapOnto2 = fullDefinitionsMap(onto2);
		
		
		
		System.out.println("Finished preparing classAndDefMaps...");

		for (Cell c : referenceAlignment) {
			if (fullDefMapOnto1.containsKey(c.getObject1AsURI().getFragment().toLowerCase())) {
				Set<String> defS = fullDefMapOnto1.get(c.getObject1AsURI().getFragment().toLowerCase());
				System.out.println("Definition for concept " + c.getObject1AsURI().getFragment() + ": ");
				for (String s : defS) {
					System.out.println(s);
				}
			}
			
			if (fullDefMapOnto2 != null && fullDefMapOnto2.containsKey(c.getObject2AsURI().getFragment().toLowerCase())) {
				Set<String> defT = fullDefMapOnto2.get(c.getObject2AsURI().getFragment().toLowerCase());
				System.out.println("Definition for concept " + c.getObject2AsURI().getFragment() + ": ");
				for (String s : defT) {
					System.out.println(s);
				}
			} else {
				System.out.println("No concept");
			}
			
			System.out.println("\n");
		}
	}
	
	public static Map<String, Set<String>> fullDefinitionsMap(OWLOntology onto) throws IOException {

		Map<String, Set<String>> classAndDefMap = new HashMap<String, Set<String>>();

		//get the definition tokens for each class c and lemmatize each token
		for (OWLClass c : onto.getClassesInSignature()) {
			classAndDefMap.put(c.getIRI().getFragment().toLowerCase(), OntologyOperations.getClassDefinitionsFull(onto, c));
		}

		return classAndDefMap;

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

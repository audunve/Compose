package test;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class TestDefinitions {
	
	public static void main(String[] args) throws OWLOntologyCreationException {
		File ontoFile1 = new File("./files/SATest1.owl");
		File ontoFile2 = new File("./files/SATest2.owl");
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto1 = manager.loadOntologyFromOntologyDocument(ontoFile1);
		OWLOntology onto2 = manager.loadOntologyFromOntologyDocument(ontoFile2);
		
		for (OWLClass s : onto1.getClassesInSignature()) {
			Set<String> defsOnto1 = getClassDefinitionsFull(onto1, s);
			for (String st : defsOnto1) {
				System.out.println(st);
			}
		}
	}
	
	
	public static Set<String> getClassDefinitionsFull (OWLOntology onto, OWLClass c) {
		Set<String> definitions = new HashSet<String>();

		for (OWLClass cls : onto.getClassesInSignature()) {
			if (cls.equals(c)) {
				for (OWLAnnotationAssertionAxiom a : onto.getAnnotationAssertionAxioms(cls.getIRI())) {
					if (a.getValue().toString().split(" ").length >= 3) {
						definitions.add(a.getValue().toString().replaceAll("\"", ""));
					}
				}
			}

		}

		return definitions;

	}

}

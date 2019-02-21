package test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import utilities.OntologyOperations;
import utilities.StringUtilities;

public class TestAnnotationTokenEquality {
	
	final static double tokenEqualityThreshold = 0.2;
	
	public static void main(String[] args) throws OWLOntologyCreationException {
		
		File ontoFile1 = new File("./files/_PHD_EVALUATION/OAEI2011/ONTOLOGIES/301304/301304-301.rdf");
		File ontoFile2 = new File("./files/_PHD_EVALUATION/OAEI2011/ONTOLOGIES/301304/301304-304.rdf");
		
		System.out.println("The annotationTokenEquality is " + computeAnnotationTokenEquality(ontoFile1, ontoFile2));
	}
	
	public static double computeAnnotationTokenEquality(File ontoFile1, File ontoFile2) throws OWLOntologyCreationException {
		int commonEntities = 0; 
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto1 = manager.loadOntologyFromOntologyDocument(ontoFile1);
		OWLOntology onto2 = manager.loadOntologyFromOntologyDocument(ontoFile2);
		int numClassesTotalOnto1 = onto1.getClassesInSignature().size();
		int numClassesTotalOnto2 = onto2.getClassesInSignature().size();
		int minEntities = Math.min(numClassesTotalOnto1, numClassesTotalOnto2);
		
						
		for (OWLClass s : onto1.getClassesInSignature()) {
			for (OWLClass t : onto2.getClassesInSignature()) {
				
				ArrayList<String> tokensS = new ArrayList<String>();
				ArrayList<String> tokensT = new ArrayList<String>();

				Set<String> sDefinitions = OntologyOperations.cleanClassDefinitions(onto1, s);

				Set<String> tDefinitions = OntologyOperations.cleanClassDefinitions(onto2, t);
				
				
				for (String sDef : sDefinitions) {
					tokensS = StringUtilities.tokenize(StringUtilities.removeStopWords(sDef), true);
				}
				
				for (String tDef : tDefinitions) {
					tokensT = StringUtilities.tokenize(StringUtilities.removeStopWords(tDef), true);
				}

				double tokenEquality = computeTokenEquality(tokensS, tokensT);

				if (tokenEquality > tokenEqualityThreshold) {
					System.out.println("\n" + s.getIRI().getFragment() + " and " + t.getIRI().getFragment() + " have equal definitions");
					commonEntities++;
					System.out.println("CommonEntities is now " + commonEntities);
					System.out.println("\nTokens from definition of " + s.getIRI().getFragment() + "(onto1):");
					for (String sS : tokensS) {
						System.out.println(sS);
					}
					
					System.out.println("\nTokens from definition of " + t.getIRI().getFragment() + "(onto2):");
					for (String sT : tokensT) {
						System.out.println(sT);
					}
					break; //to ensure that we don´t compare several target concepts with a single source concept
				}
				
			}		
		}	

		return (double) commonEntities / (double) minEntities;
		
	}
	
	public static double computeTokenEquality(ArrayList<String> sourceList, ArrayList<String> targetList) {
		int counter = 0;
		int numAvgTokensInList = ( sourceList.size() + targetList.size() ) / 2;
		for (String s : sourceList) {
			for (String t : targetList) {
				if (s.equalsIgnoreCase(t)) {
					counter++;
				}
			}
		}
		return (double) counter / (double) numAvgTokensInList;
		
	}
	
	

}

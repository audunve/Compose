package test;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class TestConceptNameEquality {
	
	public static void main(String[] args) throws OWLOntologyCreationException {
		
		File ontoFile1 = new File("./files/_PHD_EVALUATION/OAEI2011/ONTOLOGIES/301303/301303-301.rdf");
		File ontoFile2 = new File("./files/_PHD_EVALUATION/OAEI2011/ONTOLOGIES/301303/301303-303.rdf");
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto1 = manager.loadOntologyFromOntologyDocument(ontoFile1);
		OWLOntology onto2 = manager.loadOntologyFromOntologyDocument(ontoFile2);
		
		double cne = computeConceptNameEquality(ontoFile1, ontoFile2);
		
		System.out.println("The cne is " + cne);
	}
	
	public static double computeConceptNameEquality(File ontoFile1, File ontoFile2) throws OWLOntologyCreationException {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto1 = manager.loadOntologyFromOntologyDocument(ontoFile1);
		OWLOntology onto2 = manager.loadOntologyFromOntologyDocument(ontoFile2);
		
		Set<String> lcs = new HashSet<String>();
		for (OWLClass s : onto1.getClassesInSignature()) {
			for (OWLClass t : onto2.getClassesInSignature()) {
				
				lcs.addAll(longestCommonSubstrings(s.getIRI().getFragment(), t.getIRI().getFragment()));
				if (longestCommonSubstrings(s.getIRI().getFragment(), t.getIRI().getFragment()).size() > 0) {
				System.out.println("Number of lcs for " + s.getIRI().getFragment() + " and " + t.getIRI().getFragment() + " is " + longestCommonSubstrings(s.getIRI().getFragment(), t.getIRI().getFragment()).size());
				break;
				}
			}
		}
		
		int minOntology = Math.min(onto1.getClassesInSignature().size(), onto2.getClassesInSignature().size());
		
		System.out.println("There are " + lcs.size() + " longest common substrings and " + minOntology + " concepts in the smaller ontology");
		
		double cne = (double) lcs.size() / (double) minOntology;
		return cne;
		
	}
	
	/**
	 * Returns a set of common substrings (after having compared character by
	 * character)
	 * 
	 * @param s:
	 *            an input string
	 * @param t:
	 *            an input string
	 * @return a set of common substrings among the input strings
	 */
	private static Set<String> longestCommonSubstrings(String s, String t) {

		int[][] table = new int[s.length()][t.length()];
		int lengthS = s.length();
		int lengthT = t.length();
		
		//make longest 80 percent of the average length of s and t
		int longest = (int)Math.round(((lengthS + lengthT) / 2) * 0.80);
		Set<String> result = new HashSet<String>();

		//English nouns usually contains more than 3 characters, so we limit the comparison of s and t > 3
		if (lengthS > 3 && lengthT > 3) {
		for (int i = 0; i < s.length(); i++) {
			for (int j = 0; j < t.length(); j++) {
				if (s.toLowerCase().charAt(i) != t.toLowerCase().charAt(j)) {
					continue;
				}

				table[i][j] = (i == 0 || j == 0) ? 1 : 1 + table[i - 1][j - 1];
				if (table[i][j] > longest) {
					longest = table[i][j];
					result.clear();
				}
				if (table[i][j] == longest) {
					result.add(s.substring(i - longest + 1, i + 1));
				}
			}
		}
		}
		
		return result;
	}

}

package test;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class TestLongestCommonSubstring {
	
	public static void main(String[] args) throws OWLOntologyCreationException {

		File ontoFile1 = new File("./files/bibframe.rdf");
		File ontoFile2 = new File("./files/schema-org.owl");
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto1 = manager.loadOntologyFromOntologyDocument(ontoFile1);
		OWLOntology onto2 = manager.loadOntologyFromOntologyDocument(ontoFile2);
		
		Set<String> lcs = new HashSet<String>();
		for (OWLClass s : onto1.getClassesInSignature()) {
			for (OWLClass t : onto2.getClassesInSignature()) {
				lcs.addAll(longestCommonSubstrings(s.getIRI().getFragment(), t.getIRI().getFragment()));
				
			}
		}
		
		System.out.println("lcs contains " + lcs.size() + " substrings");
		for (String s : lcs) {
			System.out.println(s);
		}
		
		int minOntology = 0;
		if (onto1.getClassesInSignature().size() > onto2.getClassesInSignature().size()) {
			minOntology = onto2.getClassesInSignature().size();
		} else {
			minOntology = onto1.getClassesInSignature().size();
		}
		
		double lcsRatio = (double) lcs.size() / (double) minOntology;
		
		System.out.println("The LCS Ratio is " + lcsRatio);
		
	}
	
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

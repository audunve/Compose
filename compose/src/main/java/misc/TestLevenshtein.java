package misc;

import fr.inrialpes.exmo.ontosim.string.*;



/**
 * @author audunvennesland
 * 24. mar. 2017 
 */
public class TestLevenshtein {
	
	public static void main(String[] args) {
		
		double lev_score = StringDistances.levenshteinDistance("conference_document", "document");
		double ham_score = StringDistances.hammingDistance("conference_document", "document");
		
		System.out.println("Levenshtein: " + (1 - lev_score));
		System.out.println("Hamming: " + (1 - ham_score));
	}
	
	

}

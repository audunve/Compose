package misc;

import rita.RiWordNet;

public class TestRitaWN {
	
	public static void main(String[] args) {
	
	RiWordNet wn = new RiWordNet("/Users/audunvennesland/Documents/PhD/Development/WordNet/WordNet-3.0/dict");
	
	//note that all "lemmas" must be lowercased
	String w1 = "motion picture";
	String w2 = "film";
	String[] synonyms = null;
	
	
	//"n" is for the part-of-speech (POS)
	float distance = wn.getDistance(w1, w2, "n");	
	System.out.println("The distance between " + w1 + " and " + w2 + " is: " + distance);
	
	synonyms = wn.getSynonyms("book", "n");
	
	for (int i = 0; i < synonyms.length; i++) {
		//System.out.println(synonyms[i]);
	}
	}

}

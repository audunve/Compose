package utilities;

import java.util.HashSet;
import java.util.Set;

public class Meronym {

	/**
	 * Retrieves all meronyms from the input word provided as parameter as well as synonyms to the input word and returns the meronyms as a set. 
	 * @param inputWord
	 * @return a Set of meronyms
	 */
	public static Set<String> getMeronymsFromSynonyms(String inputWord) {

		Set<String> meronymsFromSynonymSet = new HashSet<String>();

		// get synonyms
		Set<String> synSet = WordNet.getSynonymSet(inputWord.toLowerCase());

		// add also s to set of synonym
		synSet.add(inputWord.toLowerCase());

		Set<String> tempMerSet = new HashSet<String>();

		for (String s : synSet) {
			tempMerSet.addAll(WordNet.getMeronymSet(s));

			meronymsFromSynonymSet.addAll(tempMerSet);
		}

		return meronymsFromSynonymSet;

	}

	/**
	 * Retrieves all holonyms from the input word provided as parameter as well as synonyms to the input word and returns the holonyms as a set. 
	 * @param inputWord
	 * @return a Set of holonyms
	 */
	public static Set<String> getHolonymsFromSynonyms(String inputWord) {

		Set<String> holonymsFromSynonymSet = new HashSet<String>();

		// get synonyms
		Set<String> synSet = WordNet.getSynonymSet(inputWord.toLowerCase());

		// add also s to set of synonym
		synSet.add(inputWord.toLowerCase());

		Set<String> tempHolSet = new HashSet<String>();

		for (String s : synSet) {
			tempHolSet.addAll(WordNet.getMeronymSet(s));

			holonymsFromSynonymSet.addAll(tempHolSet);
		}

		return holonymsFromSynonymSet;

	}

	public static void main(String[] args) {

		String c1 = "Aircraft";
		String c2 = "Engine";
		
		Set<String> meronymSet = WordNet.getMeronymsFromSynonyms(c1);
		
		boolean equal = false;
		
		double confidence = 0; 
		
		for (String s : meronymSet) {
			System.out.println("Matching " + s + " with " + c2.toLowerCase());
			
			//check if the meronym is a compound / composite
			String[] array = s.split(" ");
			int arrayLength = array.length;
			
			for (int i = 0; i < array.length; i++) {
				if (array[i].equals(c2.toLowerCase())) {
					equal = true;
				}
			}
			

			//single word
			if (array.length == 1) {
				if (s.equals(c2.toLowerCase())) {
					System.out.println("We have a matching meronym");
					confidence = 1.0;
				}
			} else { //more than one word
				for (int i = 0; i < array.length; i++) {
					if (array[i].equals(c2.toLowerCase())) {
						equal = true;
					}
				}
				if (equal = true) {
					confidence = 0.8;
				}
			} 
			
			

		}
		
		System.out.println("The confidence is " + confidence);

	}

}

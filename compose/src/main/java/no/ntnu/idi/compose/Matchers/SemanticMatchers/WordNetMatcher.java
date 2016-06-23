package no.ntnu.idi.compose.Matchers.SemanticMatchers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import org.ivml.alimo.ISub;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;

import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.WordNetDatabase;
import fr.inrialpes.exmo.ontowrap.OntowrapException;
import org.semanticweb.owl.align.AlignmentProcess;
import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.ontosim.string.StringDistances;

/**
 * This WordNetMatcher class aligns the concepts from two ontologies using WordNet.
 * 
 */
public class WordNetMatcher extends ObjectAlignment implements AlignmentProcess{

	static WordNetDatabase database = WordNetDatabase.getFileInstance();
	private double result;
	static ISub isubmatcher = new ISub();
	private static double individualScore;

	public WordNetMatcher() {
	}

	/**
	 * The align() method is imported from the Alignment API and is modified to use the wordNetMatch method declared in this class
	 */
	public void align( Alignment alignment, Properties param ) throws AlignmentException {
		try {
			// Match classes
			for ( Object cl2: ontology2().getClasses() ){
				for ( Object cl1: ontology1().getClasses() ){
					// add mapping into alignment object 
					addAlignCell(cl1,cl2, "=", wordNetMatch(cl1,cl2));  
				}
			}

		} catch (Exception e) { e.printStackTrace(); }
	}
	
	private static boolean containedInWordNet(String inputWord) {

		System.setProperty("wordnet.database.dir", "/Users/audunvennesland/Documents/PhD/Development/WordNet/WordNet-3.0/dict");
		WordNetDatabase database = WordNetDatabase.getFileInstance();
		Synset[] synsets = database.getSynsets(inputWord);

		if (synsets.length > 0)
		{
			return true;
		}
		else
		{
			return false;
		}		

	}

	//TO-DO: Could try a composite matcher that combines synonym matching, vector model matching of descriptions
	/**
	 * The wordNetMatch() method has two objects (ontology entity names) as parameters, checks if both entity names are included in WordNet, if so gets the WordNet synsets associated with these two entity names and match each synonym within each synset 
	 * using the synsetMatch() method. 
	 * @param o1
	 * @param o2
	 * @return
	 * @throws AlignmentException
	 */
	public double wordNetMatch(Object o1, Object o2) throws AlignmentException {

		try {
			//get the objects (entities)
			String s1 = ontology1().getEntityName(o1);
			String s2 = ontology2().getEntityName(o2);

			//if both objects are contained in WordNet...
			if (containedInWordNet(s1) && containedInWordNet(s2)) {

				//...get their synonym sets
				Synset[] synsets1 = database.getSynsets(s1);
				Synset[] synsets2 = database.getSynsets(s2);

				//iterate through the synonym set of object 1 and see if any of the synonyms in synonym set of object 2 matches	
				for (int i = 0; i < synsets1.length; i++) {
				if (synsetMatch(synsets1, synsets2) == true) {
					result = 1;
				}
				else result = 0;
			}		
			}

		} catch ( OntowrapException owex ) {
			throw new AlignmentException( "Error getting entity name", owex );
		}
		return result;
	}

	/**
	 * This method removes duplicate entries of synonyms in a synset
	 * @param input
	 * @return
	 */
	public static Set<String> removeDuplicates(Synset[] input) {
		Set<String> set = new HashSet<String>();
	
		for (Synset s : input) {
			for (String word1 : s.getWordForms()) {
				set.add(word1);
			}
		}

		return set;
		
	}


	//Computes the average similarity of two synonym sets and based on this decides if two synonym sets match
	private static boolean synsetMatch(Synset[] s1, Synset[] s2) {
		boolean result = false;

		//double to hold the individual score from the synset match
		individualScore = 0;
		
		ArrayList<Double> totalScore = new ArrayList<Double>();
		
		//remove duplicates from the synsets
		Set<String> set1 = removeDuplicates(s1);
		Set<String> set2 = removeDuplicates(s2);
		Iterator<String> it1 = set1.iterator();
		Iterator<String> it2 = set2.iterator();
		
		//intermediate solution: not a good one, but put the set values in ArrayLists for easier processing
		ArrayList<String> wordsS1 = new ArrayList<String>();
		ArrayList<String> wordsS2 = new ArrayList<String>();
		
		while (it1.hasNext()) {
			wordsS1.add((String) it1.next());
		}
		
		while (it2.hasNext()) {
			wordsS2.add((String) it2.next());
		}

		//iterate through both synsets and perform pairwise string matching using iSub
		for (int i = 0; i < wordsS1.size(); i++) {
			for (int j = 0; j < wordsS2.size(); j++) {

				//using a similarity distance metric to measure similarity between the synonym sets of two input strings
				individualScore = isubmatcher.score(wordsS1.get(i).toLowerCase(),wordsS2.get(j).toLowerCase());		
				if (individualScore >= 0.2) {
				totalScore.add(individualScore);
				System.out.println("The similarity score between " + wordsS1.get(i).toLowerCase() + " and " + wordsS2.get(j).toLowerCase() +" is: " + (isubmatcher.score(wordsS1.get(i).toLowerCase(),wordsS2.get(j).toLowerCase())));				
			} 
			}
		}

		int numScores = totalScore.size();
		double sum = 0;
		
		for (int k = 0; k < numScores; k++) {
			sum += totalScore.get(k);			
		}

		double avgScore = sum/numScores;
		System.out.println("The average score is: " + avgScore);
		
		if (avgScore > 0.6) {
			result = true;
		} else {
			result = false;
		}

		return result;

	}

	public static void main(String[] args) {
		//Test if the words are contained in wordnet
		String s1 = "journal";
		String s2 = "book";

		if (containedInWordNet(s1) == true && containedInWordNet(s2) == true) {
			System.out.println(s1 + " and " + s2 + " are contained in WordNet");
		} else {
			System.out.println("Both words are not contained in WordNet");
		}


		Synset[] synsets1 = database.getSynsets(s1);
		Synset[] synsets2 = database.getSynsets(s2);


		if (synsetMatch(synsets1, synsets2) == true) {
			System.out.println("There is a match");
		} else {
			System.out.println("There is no match");
		}


	}

}

package no.ntnu.idi.compose.WordNet;

import java.util.ArrayList;

import org.semanticweb.owl.align.AlignmentException;

import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.ws4j.impl.Lesk;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.impl.HirstStOnge;
import edu.cmu.lti.ws4j.impl.JiangConrath;
import edu.cmu.lti.ws4j.impl.LeacockChodorow;
import edu.cmu.lti.ws4j.impl.Lin;
import edu.cmu.lti.ws4j.impl.Path;
import edu.cmu.lti.ws4j.impl.Resnik;
import edu.cmu.lti.ws4j.impl.WuPalmer;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;
import fr.inrialpes.exmo.ontowrap.OntowrapException;
import no.ntnu.idi.compose.Preprocess.Preprocessor;

public class WS4JTest {
	
	private static ILexicalDatabase db = new NictWordNet();
	
	
	/**
	 * Lesk (1985) proposed that the relatedness of two words is proportional to to the extent of overlaps of their dictionary definitions. 
	 * This LESK measure is based on adapted Lesk from Banerjee and Pedersen (2002) which uses WordNet as the dictionary for the word definitions. 
	 * Computational cost is relatively high due to combinations of linked synsets to explore definitions, and need to process these texts.
	 * LESK(s1, s2) = sum_{s1' in linked(s1), s2' in linked(s2)}(overlap(s1'.definition, s2'.definition)). 
	 * @param s1
	 * @param s2
	 * @return
	 */
	public static double computeLESK(String s1, String s2)  {

		
		WS4JConfiguration.getInstance().setMFS(true);
		double s = new Lesk(db).calcRelatednessOfWords(s1, s2);
		
		//need a work-around since some of the wu palmer scores are above 1.0 (not allowed to have a confidence level above 1.0)
		if (s > 1.0) {
			s = 1.0;
		}
		return s;
	}
	/**
	 * Idea is similar to JCN with small modification. LIN(s1, s2) = 2*IC(LCS(s1, s2) / (IC(s1) + IC(s2))
	 * @param s1
	 * @param s2
	 * @return
	 */
public static double computeLin(String s1, String s2)  {

		WS4JConfiguration.getInstance().setMFS(true);
		double s = new Lin(db).calcRelatednessOfWords(s1, s2);
		
		//need a work-around since some of the wu palmer scores are above 1.0 (not allowed to have a confidence level above 1.0)
		if (s > 1.0) {
			s = 1.0;
		}
		return s;
	}

/**
 * This relatedness measure is based on an idea that two lexicalized concepts are semantically close if their WordNet synsets are connected 
 * by a path that is not too long and that "does not change direction too often". 
 * Computational cost is relatively high since recursive search is done on subtrees in the horizontal, upward and downward directions. 
 * HSO(s1, s2) = const_C - path_length(s1, s2) - const_k * num_of_changes_of_directions(s1, s2) 
 * @param s1
 * @param s2
 * @return
 */
public static double computeHirstStOnge(String s1, String s2)  {

	
	WS4JConfiguration.getInstance().setMFS(true);
	double s = new HirstStOnge(db).calcRelatednessOfWords(s1, s2);
	
	//need a work-around since some of the wu palmer scores are above 1.0 (not allowed to have a confidence level above 1.0)
	if (s > 1.0) {
		s = 1.0;
	}
	return s;
}

/**
 * Originally a distance measure which also uses the notion of information content, but in the form of the conditional probability of 
 * encountering an instance of a child-synset given an instance of a parent synset.
 * JCN(s1, s2) = 1 / jcn_distance where jcn_distance(s1, s2) = IC(s1) + IC(s2) - 2*IC(LCS(s1, s2)); 
 * when it's 0, jcn_distance(s1, s2) = -Math.log_e( (freq(LCS(s1, s2).root) - 0.01D) / freq(LCS(s1, s2).root) ) 
 * so that we can have a non-zero distance which results in infinite similarity.
 * @param s1
 * @param s2
 * @return
 */
public static double computeJiangConrath(String s1, String s2)  {

	
	WS4JConfiguration.getInstance().setMFS(true);
	double s = new JiangConrath(db).calcRelatednessOfWords(s1, s2);
	
	//need a work-around since some of the wu palmer scores are above 1.0 (not allowed to have a confidence level above 1.0)
	if (s > 1.0) {
		s = 1.0;
	}
	return s;
}

/** 
 * Resnik defined the similarity between two synsets to be the information content of their lowest super-ordinate (most specific common subsumer) 
 * RES(s1, s2) = IC(LCS(s1, s2)). 
 * @param s1
 * @param s2
 * @return
 */
public static double computeResnik(String s1, String s2)  {

	
	WS4JConfiguration.getInstance().setMFS(true);
	double s = new Resnik(db).calcRelatednessOfWords(s1, s2);
	
	//need a work-around since some of the wu palmer scores are above 1.0 (not allowed to have a confidence level above 1.0)
	if (s > 1.0) {
		s = 1.0;
	}
	return s;
}


public static double computeLeacockChodorow(String s1, String s2)  {

	
	WS4JConfiguration.getInstance().setMFS(true);
	double s = new LeacockChodorow(db).calcRelatednessOfWords(s1, s2);
	
	//need a work-around since some of the wu palmer scores are above 1.0 (not allowed to have a confidence level above 1.0)
	if (s > 1.0) {
		s = 1.0;
	}
	return s;
}
/**
 * This measure calculates relatedness by considering the depths of the two synsets in the WordNet taxonomies, along with the depth of the LCS 
 * WUP(s1, s2) = 2*dLCS.depth / ( min_{dlcs in dLCS}(s1.depth - dlcs.depth)) + min_{dlcs in dLCS}(s2.depth - dlcs.depth) ), 
 * where dLCS(s1, s2) = argmax_{lcs in LCS(s1, s2)}(lcs.depth). 
 * @param s1
 * @param s2
 * @return
 */
public static double computeWuPalmer(String s1, String s2)  {

	WS4JConfiguration.getInstance().setMFS(true);
	double s = new WuPalmer(db).calcRelatednessOfWords(s1, s2);
	
	return s;
}

/**
 * This module computes the semantic relatedness of word senses by counting the number of nodes along the shortest path between the senses in the 'is-a' 
 * hierarchies of WordNet.
 * PATH(s1, s2) = 1 / path_length(s1, s2). 
 * @param s1
 * @param s2
 * @return
 */
public static double computePath(String s1, String s2)  {

	WS4JConfiguration.getInstance().setMFS(true);
	double s = new Path(db).calcRelatednessOfWords(s1, s2);
	
	return s;
}

	
	public static void main(String[] args) {
		
		String c1 = "Regular_author";
		String c2 = "Paper_Author";
		
		String s1 = Preprocessor.stringTokenize(c1, true).toLowerCase();
		String s2 = Preprocessor.stringTokenize(c2,true).toLowerCase();
		
		ArrayList<String> tokens_1 = Preprocessor.tokenize(s1, true);
		ArrayList<String> tokens_2 = Preprocessor.tokenize(s2, true);
		
		double score = 0; 
		double finalScore = 0;
		double accScore = 0;
		double correct = 0;
		double incorrect = 0;
		
		for (String s : tokens_1) {
			for (String t : tokens_2) {
				score = computeResnik(s,t);
				accScore = accScore + score;
				System.out.println(s + " and " + t + " scores " + score + " and the current score is " + accScore);
				
				if (score > 0.8) {
					correct++;
					break;
				} else {
					incorrect++;
				}
			}
		}
		
		double avgTokens = (tokens_1.size() + tokens_2.size()) / 2;
		double numTokens = tokens_1.size() + tokens_2.size();
		
		if (incorrect == 0.0) {
			incorrect = 10;		
		}
		finalScore = accScore / numTokens;
		//finalScore = (correct / incorrect) / avgTokens;
		System.out.println(finalScore);
	}

}

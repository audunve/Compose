package no.ntnu.idi.compose.MatchingComposition;

import java.util.Map;

import fr.inrialpes.exmo.ontowrap.Ontology;

/**
 * @author audunvennesland
 * @version 1.0
 * @created 21-apr-2016 10:34:52
 */
public class MatcherSelector {

	public MatcherSelector(){

	}

	public void finalize() throws Throwable {

	}

	/**
	 * Given two input ontologies as parameters a collection of matchers is returned
	 * as a map (SortedMap) where the matcher is ranked by score.
	 * 
	 * @param onto1
	 * @param onto2
	 */
	public Map selectMatcher(Ontology onto1, Ontology onto2){
		return null;
	}

}
package no.ntnu.idi.compose.Matchers;

import java.util.Map;

import fr.inrialpes.exmo.ontowrap.Ontology;

/**
 * @author audunvennesland
 * @version 1.0
 * @created 21-apr-2016 10:34:51
 */
public interface Matcher {

	/**
	 * 
	 * @param onto1
	 * @param onto2
	 * @param matcherComposition
	 */
	public void matchOntologies(Ontology onto1, Ontology onto2, Map matcherComposition);

	/**
	 * 
	 * @param onto1
	 * @param onto2
	 */
	public void matchOntologies(Ontology onto1, Ontology onto2);

}
package no.ntnu.idi.compose.Matchers;

import java.io.File;
import java.util.Map;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;

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
	public Map matchOntologies(File onto1, File onto2, Map matcherComposition);


}
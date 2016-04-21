package no.ntnu.idi.compose.Loading;

import java.io.File;

/**
 * @author audunvennesland
 * @version 1.0
 * @created 21-apr-2016 10:34:51
 */
public interface Loader {

	public void loadOntologyFromURI();

	/**
	 * 
	 * @param ontologyFile
	 */
	public void loadOntologyFromFile(File ontologyFile);

	/**
	 * 
	 * @param ontologyFile
	 */
	public String getOntologyFormat(File ontologyFile);

}
package no.ntnu.idi.compose.Loading;

import java.io.File;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;

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
	 * @throws OWLOntologyCreationException 
	 */
	public void loadInputOntologies(File file1, File file2) throws OWLOntologyCreationException;

	/**
	 * 
	 * @param ontologyFile
	 */
	public String getOntologyFormat(File ontologyFile);

}
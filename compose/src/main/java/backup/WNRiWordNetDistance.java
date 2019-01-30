package backup;

import java.util.Properties;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.Cell;

import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.ontowrap.OntowrapException;
import rita.RiWordNet;
import utilities.StringUtilities;

public class WNRiWordNetDistance extends ObjectAlignment implements AlignmentProcess {

	static RiWordNet database = new RiWordNet("/Users/audunvennesland/Documents/PhD/Development/WordNet/WordNet-3.0/dict");

	public WNRiWordNetDistance() {
	}

	/**
	 * Should implement a function that considers whether the string contains several tokens (e.g. compounds that could be matched individually)
	 * Do a first check to see if the strings can be decomposed to tokens or not first by checking the size.
	 */
	public void align( Alignment alignment, Properties param ) throws AlignmentException {
		try {
			// Match classes
			for ( Object cl2: ontology2().getClasses() ){
				for ( Object cl1: ontology1().getClasses() ){
					
					// add mapping into alignment object 
					addAlignCell(cl1,cl2, "=", wordNetMatchRiWordNetDistance(cl1,cl2));  
				}
				
			}

		} catch (Exception e) { e.printStackTrace(); }
	}
	
	/**
	 * The wordNetMatch() method has two objects (ontology entity names) as parameters, checks if both entity names are included in WordNet, if so their distance is computed (I think using Resnik)
	 * @param o1
	 * @param o2
	 * @return
	 * @throws AlignmentException
	 * @throws OntowrapException 
	 */
	public double wordNetMatchRiWordNetDistance(Object o1, Object o2) throws AlignmentException, OntowrapException {

		double distance = 0;
		
			//get the objects (entities)
			//need to split the strings
			String s1 = StringUtilities.stringTokenize(ontology1().getEntityName(o1),true).toLowerCase();
			String s2 = StringUtilities.stringTokenize(ontology2().getEntityName(o2),true).toLowerCase();

				//...measure their distance
				 distance = (1 - database.getDistance(s1, s2, "n"));

		return distance;
	}
	



}

package no.ntnu.idi.compose.Matchers;

import java.util.Properties;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;
import fr.inrialpes.exmo.align.impl.ObjectAlignment;

import fr.inrialpes.exmo.ontowrap.OntowrapException;
import rita.RiWordNet;

public class WordNetAlignment extends ObjectAlignment implements AlignmentProcess {

	static RiWordNet database = new RiWordNet("/Users/audunvennesland/Documents/PhD/Development/WordNet/WordNet-3.0/dict");

	public WordNetAlignment() {
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


		String[] synsets = database.getSynset(inputWord, "n");

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
	 * The wordNetMatch() method has two objects (ontology entity names) as parameters, checks if both entity names are included in WordNet, if so their distance is computed (I think using Resnik)
	 * @param o1
	 * @param o2
	 * @return
	 * @throws AlignmentException
	 * @throws OntowrapException 
	 */
	public double wordNetMatch(Object o1, Object o2) throws AlignmentException, OntowrapException {

		double distance = 0;
		double finalDistance = 0;
		

			//get the objects (entities)
			String s1 = ontology1().getEntityName(o1).toLowerCase();
			String s2 = ontology2().getEntityName(o2).toLowerCase();
			
			if (containedInWordNet(s1) == true && containedInWordNet(s2) == true) {

				//...measure their distance
				 distance = database.getDistance(s1, s2, "n");
				 
				 //printing the ontology objects and their measured distance
				 System.out.println(s1 + " - " + s2 + " with measure: " + distance);

		
		if (distance != 1.0) {
			finalDistance = 1-distance;
		} else {
			finalDistance = 0;
			
		}
			}
		return finalDistance;
	}

}

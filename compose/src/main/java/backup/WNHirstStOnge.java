package backup;

import java.util.Properties;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;

import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.ws4j.impl.HirstStOnge;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;
import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.ontowrap.OntowrapException;
import utilities.StringUtilities;

public class WNHirstStOnge extends ObjectAlignment implements AlignmentProcess {

	private static ILexicalDatabase db = new NictWordNet();

	public WNHirstStOnge() {
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
					addAlignCell(cl1,cl2, "=", computeWNHirstStOnge(cl1,cl2));  
				}
				
			}

		} catch (Exception e) { e.printStackTrace(); }
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
	public double computeWNHirstStOnge(Object o1, Object o2) throws AlignmentException, OntowrapException {

		WS4JConfiguration.getInstance().setMFS(true);
		
		//get the objects (entities)
		//need to split the strings
		String s1 = StringUtilities.stringTokenize(ontology1().getEntityName(o1),true).toLowerCase();
		String s2 = StringUtilities.stringTokenize(ontology2().getEntityName(o2),true).toLowerCase();
		
		double s = new HirstStOnge(db).calcRelatednessOfWords(s1, s2);

		return s;
	}

}

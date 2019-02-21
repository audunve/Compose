package backup;

import java.util.Properties;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;

import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.ws4j.impl.WuPalmer;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;
import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.ontowrap.OntowrapException;
import utilities.StringUtilities;

public class WNWuPalmer extends ObjectAlignment implements AlignmentProcess {

	private static ILexicalDatabase db = new NictWordNet();

	public WNWuPalmer() {
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
					addAlignCell(cl1,cl2, "=", computeWuPalmer(cl1,cl2));  
				}
				
			}

		} catch (Exception e) { e.printStackTrace(); }
	}
	
	/**
	 * This measure calculates relatedness by considering the depths of the two synsets in the WordNet taxonomies, along with the depth of the LCS 
	 * WUP(s1, s2) = 2*dLCS.depth / ( min_{dlcs in dLCS}(s1.depth - dlcs.depth)) + min_{dlcs in dLCS}(s2.depth - dlcs.depth) ), 
	 * where dLCS(s1, s2) = argmax_{lcs in LCS(s1, s2)}(lcs.depth). 
	 * @param s1
	 * @param s2
	 * @return
	 */
	public double computeWuPalmer(Object o1, Object o2) throws AlignmentException, OntowrapException {

		WS4JConfiguration.getInstance().setMFS(true);
		
		//get the objects (entities)
		//need to split the strings
		String s1 = StringUtilities.stringTokenize(ontology1().getEntityName(o1),true).toLowerCase();
		String s2 = StringUtilities.stringTokenize(ontology2().getEntityName(o2),true).toLowerCase();
		
		double s = new WuPalmer(db).calcRelatednessOfWords(s1, s2);

		return s;
	}

}

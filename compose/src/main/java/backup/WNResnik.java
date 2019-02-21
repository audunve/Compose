package backup;

import java.util.Properties;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;

import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.ws4j.impl.HirstStOnge;
import edu.cmu.lti.ws4j.impl.JiangConrath;
import edu.cmu.lti.ws4j.impl.Resnik;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;
import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.ontowrap.OntowrapException;
import utilities.StringUtilities;

public class WNResnik extends ObjectAlignment implements AlignmentProcess {

	private static ILexicalDatabase db = new NictWordNet();

	public WNResnik() {
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
					addAlignCell(cl1,cl2, "=", computeResnik(cl1,cl2));  
				}
				
			}

		} catch (Exception e) { e.printStackTrace(); }
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
	public double computeResnik(Object o1, Object o2) throws AlignmentException, OntowrapException {

		WS4JConfiguration.getInstance().setMFS(true);
		
		//get the objects (entities)
		//need to split the strings
		String s1 = StringUtilities.stringTokenize(ontology1().getEntityName(o1),true).toLowerCase();
		String s2 = StringUtilities.stringTokenize(ontology2().getEntityName(o2),true).toLowerCase();
		
		double s = new Resnik(db).calcRelatednessOfWords(s1, s2);

		return s;
	}

}

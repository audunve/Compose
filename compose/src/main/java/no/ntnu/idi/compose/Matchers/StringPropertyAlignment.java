package no.ntnu.idi.compose.Matchers;

import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;

import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.ontosim.string.StringDistances;
import fr.inrialpes.exmo.ontowrap.OntowrapException;
import no.ntnu.idi.compose.Preprocess.Preprocessor;
import no.ntnu.idi.compose.algorithms.ISub;

/**
 * Plain string-based property matcher based on edit distance
 * @author audunvennesland
 *
 */
public class StringPropertyAlignment extends ObjectAlignment implements AlignmentProcess {
	
	//static StringDistances distance = new StringDistances();
	static ISub iSubMatcher = new ISub();

	public void align(Alignment alignment, Properties param) throws AlignmentException {
		
		try {
			// Match classes
			for ( Object pr2: ontology2().getProperties() ){
				for ( Object pr1: ontology1().getProperties()){
			
					// add mapping into alignment object 
					addAlignCell(pr1,pr2, "=", propScore(pr1,pr2));  
				}
				
			}

		} catch (Exception e) { e.printStackTrace(); }
	}
	
	public double propScore(Object o1, Object o2) throws OntowrapException {
		
		
		//get the objects (entities)
				String s1 = Preprocessor.stringTokenize(ontology1().getEntityName(o1), true).toLowerCase();
				String s2 = Preprocessor.stringTokenize(ontology2().getEntityName(o2), true).toLowerCase();

				//double measure = StringDistances.levenshteinDistance(s1, s2);
				double measure = iSubMatcher.score(s1, s2);
				System.out.println("Matching " + s1 + " and " + s2 + " with score " + measure);
				
				return measure;
		
	}
		
	}

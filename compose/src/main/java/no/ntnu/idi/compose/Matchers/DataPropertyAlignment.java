package no.ntnu.idi.compose.Matchers;

import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.Cell;

import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.ontosim.string.StringDistances;
import fr.inrialpes.exmo.ontowrap.OntowrapException;
import no.ntnu.idi.compose.Preprocess.Preprocessor;
import no.ntnu.idi.compose.algorithms.ISub;

/**
 * Plain string-based property matcher based on edit distance
 * @author audunvennesland
 *
 */
public class DataPropertyAlignment extends ObjectAlignment implements AlignmentProcess {
	
	//static StringDistances distance = new StringDistances();
	static ISub iSubMatcher = new ISub();

	public void align(Alignment alignment, Properties param) throws AlignmentException {
		
		try {
			// Match classes
			for ( Object pr2: ontology2().getDataProperties() ){
				for ( Object pr1: ontology1().getDataProperties()){
			
					// add mapping into alignment object 
					addAlignCell(pr1,pr2, "=", propScore(pr1,pr2));  
				}
				
			}

		} catch (Exception e) { e.printStackTrace(); }
	}
	
	//must not just discard the correspondences if they are identified by the previous matcher, but not identified by this matcher
		public static Alignment matchAlignment(Alignment inputAlignment) throws AlignmentException {
			
			System.out.println("Running string alignment!");

			Alignment refinedAlignment = new URIAlignment();
			double score = 0;
			double threshold = 0.6;
			
			//match the objects (need to preprocess to remove URI) in every cell of the alignment
			for (Cell c : inputAlignment) {
				score = propScore(Preprocessor.getString(c.getObject1().toString()), Preprocessor.getString(c.getObject2().toString()));
				if (score > threshold) {
					refinedAlignment.addAlignCell(c.getObject1(), c.getObject2(), "=", increaseCellStrength(score));
				} else {
					refinedAlignment.addAlignCell(c.getObject1(), c.getObject2(), "=", reduceCellStrength(score));
					continue;
				}
			}
			return refinedAlignment;
		}
		
		public static double increaseCellStrength(double inputStrength) {

			double newStrength = inputStrength + (inputStrength * 0.10);

			if (newStrength > 1.0) {
				newStrength = 1.0;
			}

			return newStrength;
		}
		
		public static double reduceCellStrength(double inputStrength) {

			double newStrength = inputStrength - (inputStrength * 0.10);

			if (newStrength > 1.0) {
				newStrength = 1.0;
			}

			return newStrength;
		}
	
	public double propScore(Object o1, Object o2) throws OntowrapException {
		
		
		//get the objects (entities)
				String s1 = ontology1().getEntityName(o1).toLowerCase();
				String s2 = ontology2().getEntityName(o2).toLowerCase();

				double measure = iSubMatcher.score(s1, s2);
				
				return measure;
		
	}
	
public static double propScore(String inputString1, String inputString2)  {
		
		
		//get the objects (entities)
	String s1 = Preprocessor.stringTokenize(inputString1,true).toLowerCase();
	String s2 = Preprocessor.stringTokenize(inputString2,true).toLowerCase();

				double measure = iSubMatcher.score(s1, s2);
				
				return measure;
		
	}
		
	}

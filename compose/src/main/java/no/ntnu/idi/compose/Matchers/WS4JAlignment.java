package no.ntnu.idi.compose.Matchers;

import java.util.Properties;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;

import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.impl.HirstStOnge;
import edu.cmu.lti.ws4j.impl.JiangConrath;
import edu.cmu.lti.ws4j.impl.LeacockChodorow;
import edu.cmu.lti.ws4j.impl.Lesk;
import edu.cmu.lti.ws4j.impl.Lin;
import edu.cmu.lti.ws4j.impl.Path;
import edu.cmu.lti.ws4j.impl.Resnik;
import edu.cmu.lti.ws4j.impl.WuPalmer;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.ontowrap.OntowrapException;
import no.ntnu.idi.compose.Preprocess.Preprocessor;

public class WS4JAlignment extends ObjectAlignment implements AlignmentProcess {

	private static ILexicalDatabase db = new NictWordNet();
	
	static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	static OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
	
	/**
	 * The align() method is imported from the Alignment API and is modified to use the wordNetMatch method declared in this class
	 */
	public void align( Alignment alignment, Properties param ) throws AlignmentException {
		try {
			//for matching properties use the ontologyX.getProperties() method instead...
			// Match classes
			for ( Object cl2: ontology2().getClasses() ){
				for ( Object cl1: ontology1().getClasses() ){			
					// add mapping into alignment object 
					addAlignCell(cl1,cl2, "=", computeRESNIK(cl1,cl2));  
				}				
			}
		} catch (Exception e) { e.printStackTrace(); }
	}
	/**
	 * This matcher only focuses on matching entities in an already existing alignment and returns a new alignment computed using WordNet distances
	 * @param inputAlignment
	 * @return
	 * @throws AlignmentException
	 * @throws OntowrapException
	 */
	public static Alignment matchAlignment(Alignment inputAlignment) throws AlignmentException, OntowrapException {
		
		System.out.println("Running WordNet alignment!");

		Alignment refinedAlignment = new URIAlignment();
		double score = 0;
		double threshold = 0.9;
		
		//match the objects (need to preprocess to remove URI) in every cell of the alignment
		for (Cell c : inputAlignment) {
			score = computeAlignmentWUP(Preprocessor.getString(c.getObject1().toString()), Preprocessor.getString(c.getObject2().toString()));
			if (score > c.getStrength() && score > threshold) {
				refinedAlignment.addAlignCell(c.getObject1(), c.getObject2(), "=", score);
			} else {
				refinedAlignment.addAlignCell(c.getObject1(), c.getObject2(), "=", c.getStrength());
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
	
	public double computeWUP(Object o1, Object o2) throws AlignmentException, OntowrapException {
		//get the objects (entities)
		String s1 = Preprocessor.stringTokenize(ontology1().getEntityName(o1),true).toLowerCase();
		String s2 = Preprocessor.stringTokenize(ontology2().getEntityName(o2),true).toLowerCase();
		
		WS4JConfiguration.getInstance().setMFS(true);
		double s = new WuPalmer(db).calcRelatednessOfWords(s1, s2);
		
		//need a work-around since some of the wu palmer scores are above 1.0 (not allowed to have a confidence level above 1.0)
		if (s > 1.0) {
			s = 1.0;
		}
		return s;
	}
	
	public double computeLESK(Object o1, Object o2) throws AlignmentException, OntowrapException {
		//get the objects (entities)
		String s1 = Preprocessor.stringTokenize(ontology1().getEntityName(o1),true).toLowerCase();
		String s2 = Preprocessor.stringTokenize(ontology2().getEntityName(o2),true).toLowerCase();
		
		WS4JConfiguration.getInstance().setMFS(true);
		double s = new Lesk(db).calcRelatednessOfWords(s1, s2);
		
		//need a work-around since some of the wu palmer scores are above 1.0 (not allowed to have a confidence level above 1.0)
		if (s > 1.0) {
			s = 1.0;
		}
		return s;
	}
	
	public static double computeAlignmentWUP(String s1, String s2) throws AlignmentException, OntowrapException {

		WS4JConfiguration.getInstance().setMFS(true);
		double s = new WuPalmer(db).calcRelatednessOfWords(s1, s2);
		//need a work-around since some of the wu palmer scores are above 1.0 (not allowed to have a confidence level above 1.0)
		if (s > 1.0) {
			s = 1.0;
		}
		return s;
	}
	
	
	
	public double computeLIN(Object o1, Object o2) throws AlignmentException, OntowrapException {
		//get the objects (entities)
		String s1 = Preprocessor.stringTokenize(ontology1().getEntityName(o1),true).toLowerCase();
		String s2 = Preprocessor.stringTokenize(ontology2().getEntityName(o2),true).toLowerCase();
		
		WS4JConfiguration.getInstance().setMFS(true);
		double s = new Lin(db).calcRelatednessOfWords(s1, s2);
		
		//need a work-around since some of the scores are above 1.0 (not allowed to have a confidence level above 1.0)
				if (s > 1.0) {
					s = 1.0;
				}
				return s;
	}
	
	public double computeRESNIK(Object o1, Object o2) throws AlignmentException, OntowrapException {
		//get the objects (entities)
		String s1 = Preprocessor.stringTokenize(ontology1().getEntityName(o1),true).toLowerCase();
		String s2 = Preprocessor.stringTokenize(ontology2().getEntityName(o2),true).toLowerCase();
		
		WS4JConfiguration.getInstance().setMFS(true);
		double s = new Resnik(db).calcRelatednessOfWords(s1, s2);
		//need a work-around since some of the scores are above 1.0 (not allowed to have a confidence level above 1.0)
		if (s > 1.0) {
			s = 1.0;
		}
		return s;
}
	

}

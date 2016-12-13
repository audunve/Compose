package no.ntnu.idi.compose.Matchers;

import java.util.Properties;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;

import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.ws4j.impl.Lin;
import edu.cmu.lti.ws4j.impl.WuPalmer;
import edu.cmu.lti.ws4j.impl.Resnik;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;
import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.ontowrap.OntowrapException;
import no.ntnu.idi.compose.Preprocess.Preprocessor;

public class Property_WordNet extends ObjectAlignment implements AlignmentProcess {

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
			for ( Object cl2: ontology2().getProperties() ){
				for ( Object cl1: ontology1().getProperties() ){
			
					// add mapping into alignment object 
					addAlignCell(cl1,cl2, "=", computeRESNIK(cl1,cl2));  
				}		
			}

		} catch (Exception e) { e.printStackTrace(); }
	}
	
	public double computeWUP(Object o1, Object o2) throws AlignmentException, OntowrapException {
		//get the objects (entities)
		String s1 = Preprocessor.stripPrefix(ontology1().getEntityName(o1)).toLowerCase();
		System.out.println("S1 after preprocessing: " + s1);
		String s2 = Preprocessor.stripPrefix(ontology2().getEntityName(o2)).toLowerCase();
		System.out.println("S2 after preprocessing: " + s2);
		
		WS4JConfiguration.getInstance().setMFS(true);
		double s = new WuPalmer(db).calcRelatednessOfWords(s1, s2);
		//need a work-around since some of the wu palmer scores are above 1.0 (not allowed to have a confidence level above 1.0)
		if (s > 1.0) {
			s = 1.0;
		}
		
		System.out.println("Matching " + s1 + " and " + s2 + " with a score of " + s);
		
		return s;
	}
	
	public double computeLIN(Object o1, Object o2) throws AlignmentException, OntowrapException {
		
		String s1 = Preprocessor.stripPrefix(ontology1().getEntityName(o1)).toLowerCase();
		System.out.println("S1 after preprocessing: " + s1);
		String s2 = Preprocessor.stripPrefix(ontology2().getEntityName(o2)).toLowerCase();
		System.out.println("S2 after preprocessing: " + s2);
		
		WS4JConfiguration.getInstance().setMFS(true);
		double s = new Lin(db).calcRelatednessOfWords(s1, s2);
		
		System.out.println("Matching " + s1 + " and " + s2 + " with a score of " + s);
		
		return s;
	}
	
public double computeRESNIK(Object o1, Object o2) throws AlignmentException, OntowrapException {
		
		String s1 = Preprocessor.stripPrefix(ontology1().getEntityName(o1)).toLowerCase();
		System.out.println("S1 after preprocessing: " + s1);
		String s2 = Preprocessor.stripPrefix(ontology2().getEntityName(o2)).toLowerCase();
		System.out.println("S2 after preprocessing: " + s2);
		
		WS4JConfiguration.getInstance().setMFS(true);
		double s = new Resnik(db).calcRelatednessOfWords(s1, s2);
		//need a work-around since some of the wu palmer scores are above 1.0 (not allowed to have a confidence level above 1.0)
				if (s > 1.0) {
					s = 1.0;
				}
		System.out.println("Matching " + s1 + " and " + s2 + " with a score of " + s);
		
		return s;
	}

}

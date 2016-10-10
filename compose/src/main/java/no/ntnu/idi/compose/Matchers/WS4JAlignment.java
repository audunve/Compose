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
import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.ontowrap.OntowrapException;

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
					addAlignCell(cl1,cl2, "=", computeWUP(cl1,cl2));  
				}
				
			}

		} catch (Exception e) { e.printStackTrace(); }
	}
	
	public double computeWUP(Object o1, Object o2) throws AlignmentException, OntowrapException {
		//get the objects (entities)
		String s1 = ontology1().getEntityName(o1).toLowerCase();
		String s2 = ontology2().getEntityName(o2).toLowerCase();
		
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
		String s1 = ontology1().getEntityName(o1).toLowerCase();
		String s2 = ontology2().getEntityName(o2).toLowerCase();
		
		WS4JConfiguration.getInstance().setMFS(true);
		double s = new Lin(db).calcRelatednessOfWords(s1, s2);
		return s;
	}

}

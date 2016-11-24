package no.ntnu.idi.compose.Matchers;

import java.util.Properties;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;

import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.ontowrap.OntowrapException;

import no.ntnu.idi.compose.algorithms.ISub;

public class ISubAlignment extends ObjectAlignment implements AlignmentProcess {
	
	static ISub isubMatcher = new ISub();

	public void align(Alignment alignment, Properties param) throws AlignmentException {
		
		try {
			// Match classes
			for ( Object cl2: ontology2().getClasses() ){
				for ( Object cl1: ontology1().getClasses() ){
			
					// add mapping into alignment object 
					addAlignCell(cl1,cl2, "=", iSubScore(cl1,cl2));  
				}
				
			}

		} catch (Exception e) { e.printStackTrace(); }
	}
	
	public double iSubScore(Object o1, Object o2) throws OntowrapException {
		
		//System.out.println("Printing the names of the ontologies...");
		String onto1 = ontology1().getURI().toString();
		String onto2 = ontology2().getURI().toString();
		//System.out.println("The name of ontology 1 is: " + onto1);
		//System.out.println("The name of ontology 2 is: " + onto2);
		
		
		//get the objects (entities)
		String s1 = ontology1().getEntityName(o1).toLowerCase();
		String s2 = ontology2().getEntityName(o2).toLowerCase();
		
		//System.out.println("Matching " + s1 + " and " + s2);
		double measure = isubMatcher.score(s1, s2);
		
		return measure;
		
	}
		
	}



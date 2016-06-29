package no.ntnu.idi.compose.Matchers.SemanticMatchers;

import java.util.Properties;

//Alignment API classes
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;

//Alignment API implementation classes
import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.ontowrap.OntowrapException;

public class CompoundMatcher extends ObjectAlignment implements AlignmentProcess{

	public CompoundMatcher() {
	}

	public void align( Alignment alignment, Properties param ) throws AlignmentException {
		try {
			// Match classes
			for ( Object cl2: ontology2().getClasses() ){
				for ( Object cl1: ontology1().getClasses() ){
					// add mapping into alignment object 
					addAlignCell(cl1,cl2, findRelation(cl1,cl2), compoundMatch(cl1,cl2));  
					}
				}
			
		} catch (Exception e) { e.printStackTrace(); }
	}
	
	public double compoundMatch(Object o1, Object o2) throws AlignmentException {
		try {
			String s1 = ontology1().getEntityName(o1);
			String s2 = ontology2().getEntityName(o2);
			if (s1 == null || s2 == null) return 0.;
			
			if (isCompound(s1,s2) && !s1.equals(s2)) { 
				return 1.0;
			} else if (isCompound(s2,s1) && !s2.equals(s1)) { 
				return 1.0;
			}
			else { 
				return 0.;
			}
		} catch ( OntowrapException owex ) {
			throw new AlignmentException( "Error getting entity name", owex );
		}
	}
	
	public String findRelation(Object o1, Object o2) throws AlignmentException {
		try {
			String s1 = ontology1().getEntityName(o1);
			String s2 = ontology2().getEntityName(o2);
			if (s1 == null || s2 == null) return "0.";
			
			if (isCompound(s1,s2) && !s1.equals(s2)) { 
				return "is a type of";
			} else if (isCompound(s2,s1) && !s2.equals(s1)) { 
				return "has a type of";
			}
			else { 
				return "0";
			}
		} catch ( OntowrapException owex ) {
			throw new AlignmentException( "Error getting entity name", owex );
		}
	}

	public static boolean isCompound(String a, String b) {
		boolean test = false;

		String[] compounds = a.split("(?<=.)(?=\\p{Lu})");

		for (int i = 0; i < compounds.length; i++) {
			if (b.equals(compounds[i])) {
				test = true;
			}
		}

		return test;
	}
	
	public static void main(String[] args) {
		String s1 = "Vehicle";
		String s2 = "RoadVehicle";
		
		if (isCompound(s1,s2) == true) {
			System.out.println(s1 + " is subsumed by " + s2);
		}
		else {
			System.out.println(s1 + " is not subsumed by " + s2);
		}
	}
}
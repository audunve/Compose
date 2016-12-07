package no.ntnu.idi.compose.Matchers;


import java.util.Properties;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;

import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.ontowrap.OntowrapException;




public class Subsumption_Compound extends ObjectAlignment implements AlignmentProcess {
	
	
	final double THRESHOLD = 0.9;
	
	public Subsumption_Compound() {
	}
	
	

	public void align( Alignment alignment, Properties param ) throws AlignmentException {
		try {
			// Match classes
			for ( Object cl2: ontology2().getClasses() ){
				for ( Object cl1: ontology1().getClasses() ){
					// add mapping into alignment object 
					addAlignCell(cl1,cl2, findCompoundRelation(cl1,cl2), compoundMatch(cl1,cl2));  
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
	
	public String findCompoundRelation(Object o1, Object o2) throws AlignmentException {
		try {
			String s1 = ontology1().getEntityName(o1);
			String s2 = ontology2().getEntityName(o2);
			if (s1 == null || s2 == null) return "0.";
			
			String isA = "&lt;";
			String hasA = "&gt;";
			
			if (isCompound(s1,s2) && !s1.equals(s2)) { 
				return isA;
			} else if (isCompound(s2,s1) && !s2.equals(s1)) { 
				return hasA;
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
		String[] onto1 = {"Short Paper", "LongPaper", "Academic_Paper", "Short_Article"};
		String[] onto2 = {"Paper", "Article"};
		
		for (int i = 0; i < onto1.length; i++) {
			for (int j = 0; j < onto2.length; j++) {
				if (isCompound(onto1[i], onto2[j])) {
					System.out.println(onto1[i] + " < " + onto2[j]);
				} else {
					System.out.println(onto1[i] + " is not subsumed by " + onto2[j]);
				}
			}
		}

	}
}


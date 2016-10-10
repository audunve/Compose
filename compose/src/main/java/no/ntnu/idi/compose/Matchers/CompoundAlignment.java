package no.ntnu.idi.compose.Matchers;


import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.ivml.alimo.ISub;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.DirectedNeighborIndex;
import org.jgrapht.graph.DefaultEdge;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.ontowrap.HeavyLoadedOntology;
import fr.inrialpes.exmo.ontowrap.OntologyFactory;
import fr.inrialpes.exmo.ontowrap.OntowrapException;
import no.ntnu.idi.compose.Loading.OWLLoader;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;


public class CompoundAlignment extends ObjectAlignment implements AlignmentProcess {
	
	
	final double THRESHOLD = 0.9;
	
	public CompoundAlignment() {
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
		String s1 = "RoadVehicle";
		String s2 = "Vehicle";
		
		if (isCompound(s1,s2) == true) {
			System.out.println(s1 + " is subsumed by " + s2);
		}
		else {
			System.out.println(s1 + " is not subsumed by " + s2);
		}
	}
}


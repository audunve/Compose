package no.ntnu.idi.compose.Matchers;


import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.ivml.alimo.ISub;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.DirectedNeighborIndex;
import org.jgrapht.graph.DefaultEdge;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import fr.inrialpes.exmo.align.impl.ObjectAlignment;

import fr.inrialpes.exmo.ontowrap.OntowrapException;
import no.ntnu.idi.compose.Loading.GraphLoader;


public class SubsumptionAlignment extends ObjectAlignment implements AlignmentProcess {
	
	final double THRESHOLD = 0.9;
	
	public SubsumptionAlignment() {
	}
	

	public void align( Alignment alignment, Properties param ) throws AlignmentException {
		try {
			// Match classes
			for ( Object cl2: ontology2().getClasses() ){
				for ( Object cl1: ontology1().getClasses() ){
					// add mapping into alignment object 
					addAlignCell(cl1,cl2, findSubClassRelation(cl1,cl2), subClassOfMatch(cl1,cl2));  
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
	
	/**
	 * Match o1 and o2 for similarity and if their similarity is above a certain threshold...
	 * ...have the subclasses of each of them become the subclasses of the other one (unless they already exist).
	 * @param o1
	 * @param o2
	 * @return
	 * @throws AlignmentException
	 * @throws OntowrapException 
	 * @throws OWLOntologyCreationException 
	 */
	//TO-DO: Currently, as long as the same label exists in both ontologies (e.g. "MaritimeTransport" in TestTransport1.owl and TestTransport2.owl, the...
	//isSubClassOf method finds the superclass-list of both ontologies, not just for s1 which is the intention. This leads to false positives...
	public boolean isSubClassOf (String s1, String s2) throws OWLOntologyCreationException {
		//using ISub to find the similarity score
		ISub iSub = new ISub();
		
		//get superclasses of s1 and s2
		File f1 = new File("/Users/audunvennesland/Documents/PhD/Ontologies/TestOntologiesTransport/TestTransport1.owl");
		File f2 = new File("/Users/audunvennesland/Documents/PhD/Ontologies/TestOntologiesTransport/TestTransport2.owl");
		
		//transform to graph representation
		DirectedGraph<String, DefaultEdge> ontology1Graph = GraphLoader.createOWLGraph(f1);
		DirectedGraph<String, DefaultEdge> ontology2Graph = GraphLoader.createOWLGraph(f2);

		//get the neighborhood of the two graphs
		DirectedNeighborIndex<String, DefaultEdge> indexOfOntology1 = new DirectedNeighborIndex(ontology1Graph);
		DirectedNeighborIndex<String, DefaultEdge> indexOfOntology2 = new DirectedNeighborIndex(ontology2Graph);
		
		//get the superclasses
		List<String> superClasses = null;
		
		if (ontology1Graph.containsVertex(s1)) {
			superClasses = indexOfOntology1.predecessorListOf(s1);
		} else {
			superClasses = indexOfOntology2.predecessorListOf(s1);
		}
		
		boolean isSubClass = false;
		double currentScore = 0;
		
		//if there is a match between the superclasses of s1 and the class s2...
		//...then s1 should be a subclass of s2
		for (String i : superClasses) {
			currentScore = iSub.score(i.toString(), s2);	
			if (currentScore > THRESHOLD) {
				isSubClass = true;
				System.out.println("The similarity between " + i.toString() + " and " + s2 + " is " + currentScore);
				System.out.println("Hence " + s1 + " is a sub-class of " + s2);
			}
			}
		
		return isSubClass;
	}
	
	public double subClassOfMatch(Object o1, Object o2) throws AlignmentException, OWLOntologyCreationException, OntowrapException {
		String s1 = ontology1().getEntityName(o1);
		String s2 = ontology2().getEntityName(o2);

			
		//the similarity score should resemble the ISub-score when matching the two entities
			if (isSubClassOf(s1,s2)) { 
				return 1.0;
			} else if (isSubClassOf(s2,s1)) { 
				return 1.0;
			}
			else { 
				return 0.;
			}
	}
	
	public String findSubClassRelation(Object o1, Object o2) throws AlignmentException, OWLOntologyCreationException, OntowrapException {

			String s1 = ontology1().getEntityName(o1);
			String s2 = ontology2().getEntityName(o2);
			//if (s1 == null || s2 == null) return "0.";
			
			if (isSubClassOf(s1,s2)) { 
				return "is a type of";
			} else if (isSubClassOf(s2,s1)) { 
				return "has a type of";
			}
			else { 
				return "0";
			}

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


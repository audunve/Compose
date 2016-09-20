package no.ntnu.idi.compose.Matchers;

import java.io.File;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.ivml.alimo.ISub;
import org.jgrapht.DirectedGraph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.DirectedNeighborIndex;
import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.graph.DefaultEdge;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;


import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.ontowrap.OntowrapException;
import no.ntnu.idi.compose.Loading.GraphLoader;

public class StructuralAlignment extends ObjectAlignment implements AlignmentProcess {

		static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		static OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
		
		final double THRESHOLD = 0.8;
		
		/**
		 * The align() method is imported from the Alignment API and is modified to use the structural methods declared in this class
		 */
		public void align( Alignment alignment, Properties param ) throws AlignmentException {
			try {
				// Match classes
				for ( Object cl2: ontology2().getClasses() ){
					for ( Object cl1: ontology1().getClasses() ){
				
						// add mapping into alignment object 
						addAlignCell(cl1,cl2, "=", commonSubClassSim(cl1,cl2));  
					}				
				}

			} catch (Exception e) { e.printStackTrace(); }
		}
		/**
		 * Finds the structProx similarity considering the depth of the two parameter classes as well as the depth of their common superclass.
		 * A graph representation using JGraphT and the Floyd Warshall distance is applied.
		 * The formula is structProx(ci, cj) = (2 * depth(cij)) / depth(ci) + depth(cj)
		 * @param Object o1
		 * @param Object o2
		 * @return double s
		 * @throws AlignmentException
		 * @throws OntowrapException
		 * @throws OWLOntologyCreationException 
		 */
		public double commonSuperClassSim(Object o1, Object o2) throws AlignmentException, OntowrapException, OWLOntologyCreationException {
			//get the objects (entities)
			String s1 = ontology1().getEntityName(o1);
			String s2 = ontology2().getEntityName(o2);
			//double simThreshold = 0.6;
			//test
			//System.out.println("Matching " + s1 + " and " + s2);
			
			//for now I am using static references to the ontology files as I know these are the ontologies to be matched...
			//but need to find a more generic way of doing this (look at NameAndPropertyAlignment.java)
			//File f1 = new File("/Users/audunvennesland/Documents/PhD/Development/Experiments/OAEIBIBLIO2BIBO/Biblio_2015.rdf");
			//File f2 = new File("/Users/audunvennesland/Documents/PhD/Development/Experiments/OAEIBIBLIO2BIBO/BIBO.owl");
			File f1 = new File("/Users/audunvennesland/Documents/PhD/Ontologies/TestOntologiesTransport/TestTransport1.owl");
			File f2 = new File("/Users/audunvennesland/Documents/PhD/Ontologies/TestOntologiesTransport/TestTransport2.owl");
			
			DirectedGraph<String, DefaultEdge> owlGraphBiblio = GraphLoader.createOWLGraph(f1);
			DirectedGraph<String, DefaultEdge> owlGraphBIBO = GraphLoader.createOWLGraph(f2);
			
			//test
			//printing all vertices of the graphs
			//Set<String> ontology1Graph = owlGraphBiblio.vertexSet();
			//Set<String> ontology2Graph = owlGraphBiblio.vertexSet();
			
			/*System.out.println("Ontology 1 contains the following vertices");
			Iterator iter1 = ontology1Graph.iterator();
			while (iter1.hasNext()) {
				System.out.println(iter1.next().toString());
			}
			System.out.println("Ontology 2 contains the following vertices");
			Iterator iter2 = ontology2Graph.iterator();
			while (iter2.hasNext()) {
				System.out.println(iter2.next().toString());
			}*/
			
			//test
			//System.out.println("The TestTransport1 graph contains " + owlGraphBiblio.vertexSet().size() + " vertices, and " + owlGraphBiblio.edgeSet().size() + " edges");
			//System.out.println("The TestTransport2 graph contains " + owlGraphBIBO.vertexSet().size() + " vertices, and " + owlGraphBIBO.edgeSet().size() + " edges");

			
			//find the depth of the two classes (from owl:Thing)
			FloydWarshallShortestPaths<String, DefaultEdge> path1 = new FloydWarshallShortestPaths<String, DefaultEdge>(owlGraphBiblio);
			FloydWarshallShortestPaths<String, DefaultEdge> path2 = new FloydWarshallShortestPaths<String, DefaultEdge>(owlGraphBIBO);
			double distanceC1ToRoot = 0;
			double distanceC2ToRoot = 0;
			String root = "Thing";
			
			if (owlGraphBiblio.containsVertex(s1)) {
				distanceC1ToRoot = path1.shortestDistance(root, s1);
				//test
				//System.out.println("The ontology contains " + s1);
			}
			
			if (owlGraphBIBO.containsVertex(s2)) {
				distanceC2ToRoot = path2.shortestDistance(root, s2);
				//test
				//System.out.println("The ontology contains " + s2);
			}
			
			//find the ancestors of the two classes
			List<String> ancestors1 = new ArrayList<String>();
			List<String> ancestors2 = new ArrayList<String>();
			
			if (owlGraphBiblio.containsVertex(s1)) {
				ancestors1 = path1.getShortestPathAsVertexList(root, s1);	
			}
			//need to remove the vertex in question from the list so that this vertex is not considered in the following processing?
			//ancestors1.remove(s1);
			
			if (owlGraphBiblio.containsVertex(s2)) {
				ancestors2 = path2.getShortestPathAsVertexList(root, s2);		
			}
			//need to remove the vertex in question from the list so that this vertex is not considered in the following processing?
			//ancestors2.remove(s2);
			
			//match the ancestors and if the similarity is above 0.9 add the pair of ancestors to a map where...
			//...ancestor of O1 is key and ancestor of O2 is value
			double iSubSimScore = 0;
			ISub iSub = new ISub();
			
			//map to keep the pair of ancestors matching above the threshold
			Map<Object,Object> matchingMap = new HashMap<Object,Object>();
			
			//TO-DO: Should limit the ancestors to the parent class? Otherwise, if we try all combinations all the way up to the root (owl:Thing) eventually the... 
			//...root itself is compared and the similarity will be 1 (and highly unrealistic)! However, in such a case this metric will also be 0 since the depth of...
			//...the root is 0!

			for (Object i : ancestors1) {
				for (Object j : ancestors2) {
					iSubSimScore = iSub.score(i.toString(), j.toString());
					System.out.println("The commonSuperClassSim similarity score between " + i.toString() + " and " + j.toString() + " is " + iSubSimScore);
					//if the similarity between the ancestors is equal to or above the defined threshold these two ancestors are kept
					if(iSubSimScore >= THRESHOLD) {
						matchingMap.put(i.toString(), j.toString());
						
						//test
						//System.out.println(i.toString() + " and " + j.toString() + " are similar above the threshold");
					}				
				}
			}
			
			//iterate through the map of ancestors and compute the average distance to the root for the pair of ancestors and compute the structProx value
			double structProx = 0;
			double currentStructProx = 0;
			double avgAncestorDistanceToRoot = 0;
			
			//loop through the matchingMap containing key-value pairs of ancestors from O1 and O2 being similar over the given threshold
			for (Entry<Object, Object> entry : matchingMap.entrySet()) {
				
				avgAncestorDistanceToRoot = (path1.shortestDistance(root,entry.getKey().toString()) + path2.shortestDistance(root,entry.getValue().toString())) / 2;
				//test

				System.out.println("The average distance to root for " + entry.getKey().toString() + " and " + entry.getValue() + " is " + avgAncestorDistanceToRoot);
				currentStructProx = (2 * avgAncestorDistanceToRoot) / (distanceC1ToRoot + distanceC2ToRoot);
				//test
				System.out.println("The distance from " + s1 + " to " + root + " is " + distanceC1ToRoot);
				System.out.println("The distance from " + s2 + " to " + root + " is " + distanceC2ToRoot);
				if (currentStructProx > structProx) {
					//test
					System.out.println("Current struct prox is " + currentStructProx);
					structProx = currentStructProx;
				}

			}

			return structProx;
		}
		
		
		/**
		 * Matches the sub-classes of the two classes provided as parameters. If the aggregate sum of their sub-classes is above a given threshold then a weight w is given to the similarity score of the classes.
		 * @param o1
		 * @param o2
		 * @return
		 * @throws AlignmentException
		 * @throws OntowrapException
		 * @throws OWLOntologyCreationException 
		 */
		public double commonSubClassSim(Object o1, Object o2) throws AlignmentException, OntowrapException, OWLOntologyCreationException {
			//get the objects (entities)
			String s1 = ontology1().getEntityName(o1);
			String s2 = ontology2().getEntityName(o2);
			//double simThreshold = 0.6;
			ISub iSub = new ISub();
			
			File f1 = new File("/Users/audunvennesland/Documents/PhD/Ontologies/TestOntologiesTransport/TestTransport1.owl");
			File f2 = new File("/Users/audunvennesland/Documents/PhD/Ontologies/TestOntologiesTransport/TestTransport2.owl");
			
			DirectedGraph<String, DefaultEdge> owlGraphBiblio = GraphLoader.createOWLGraph(f1);			
			DirectedGraph<String, DefaultEdge> owlGraphBIBO = GraphLoader.createOWLGraph(f2);
		
			DirectedNeighborIndex<String, DefaultEdge> indexOfOntology1 = new DirectedNeighborIndex(owlGraphBiblio);
			DirectedNeighborIndex<String, DefaultEdge> indexOfOntology2 = new DirectedNeighborIndex(owlGraphBIBO);
			
			//find the sub-classes of the two parameter classes
			List<String> subClasses1 = indexOfOntology1.successorListOf(s1);
			List<String> subClasses2 = indexOfOntology2.successorListOf(s2);
			
			double currentSim = 0;
			double finalSim = 0;
			
			//test
			System.out.println("Trying " + s1 + " and " + s2 + " ...");
			//match the sub-classes associated with s1 and s2
			//at the moment the max score from the sub-class matching of each parameter class s1 and s2 is computed, not an aggregate similarity from all sub-classes
			for (String i : subClasses1) {
				for (String j : subClasses2) {
					currentSim = iSub.score(i, j);
					//test
					System.out.println("The similarity score of " + i + " and " + j + " is " + currentSim);
					if (currentSim > finalSim) {
						finalSim = currentSim;
					}
				}
			}

			return finalSim;
		}
		/**
		 * This method combines the commonSuperClassSim with the commonSubClassSim and averages their score into a combined neighborhood similarity score
		 * @param o1
		 * @param o2
		 * @return double neighborhoodSim
		 * @throws AlignmentException
		 * @throws OntowrapException
		 * @throws OWLOntologyCreationException
		 */
		public double neighborhoodSim(Object o1, Object o2) throws AlignmentException, OntowrapException, OWLOntologyCreationException {
			//get the objects (entities)
			String s1 = ontology1().getEntityName(o1);
			String s2 = ontology2().getEntityName(o2);
			double simThreshold = 0.8;
			ISub iSub = new ISub();
			
			double computeSuperClassSim = commonSuperClassSim(o1,o2);
			double computeSubClassSim = commonSubClassSim(o1,o2);

			double neighborhoodSim = (computeSuperClassSim + computeSubClassSim)/2;
			return neighborhoodSim;
		}
		
		
		/**
		 * Matches the domain and range classes of similar properties
		 * @param o1
		 * @param o2
		 * @return
		 * @throws AlignmentException
		 * @throws OntowrapException
		 */
		public double propertySim(Object o1, Object o2) throws AlignmentException, OntowrapException {
			//get the objects (entities)
			String s1 = ontology1().getEntityName(o1).toLowerCase();
			String s2 = ontology2().getEntityName(o2).toLowerCase();
			
			//to be computed
			double s = 0;
			return s;
		}
		
		/**
		 * Matches classes with properties
		 * @param o1
		 * @param o2
		 * @return
		 * @throws AlignmentException
		 * @throws OntowrapException
		 */
		public double classesAndPropertiesSim(Object o1, Object o2) throws AlignmentException, OntowrapException {
			//get the objects (entities)
			String s1 = ontology1().getEntityName(o1).toLowerCase();
			String s2 = ontology2().getEntityName(o2).toLowerCase();
			
			//to be computed
			double s = 0;
			return s;
		}
		
		/**
		 * If two classes are similar over a certain threshold, then the sub-classes of each should should be subsumed by the other class
		 * @param o1
		 * @param o2
		 * @return
		 * @throws AlignmentException
		 * @throws OntowrapException
		 */
		public double subClassSim(Object o1, Object o2) throws AlignmentException, OntowrapException {
			//get the objects (entities)
			String s1 = ontology1().getEntityName(o1).toLowerCase();
			String s2 = ontology2().getEntityName(o2).toLowerCase();
			
			//to be computed
			double s = 0;
			return s;
		}
		

		
		

	}


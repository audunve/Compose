package no.ntnu.idi.compose.Matchers;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.ivml.alimo.ISub;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.DirectedNeighborIndex;
import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
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
				
				//get the ontologies and represent them as OWL API ontologies
				OWLOntology o1;
				OWLOntology o2;

				File f1 = new File("/Users/audunvennesland/Documents/PhD/Ontologies/TestOntologiesTransport/TestTransport1.owl");
				File f2 = new File("/Users/audunvennesland/Documents/PhD/Ontologies/TestOntologiesTransport/TestTransport2.owl");
				
				System.out.println("...Trying to load the ontologies...");
				o1 = manager.loadOntologyFromOntologyDocument(f1);
				OWLReasoner reasoner1 = reasonerFactory.createReasoner(o1);
				o2 = manager.loadOntologyFromOntologyDocument(f2);
				OWLReasoner reasoner2 = reasonerFactory.createReasoner(o2);
				
				//create a structure to hold classes and their superclasses (for ontology 1)
				Set<OWLClass> cls1 = o1.getClassesInSignature();
				Iterator test = cls1.iterator();

				Map<String, String> classesAndSuperClasses1 = new HashMap();
				ArrayList<OWLClass> classList1 = new ArrayList<OWLClass>();
				
				for (OWLClass i : cls1) {
					classList1.add(i);
					System.out.println("Adding " + i + " to classList1");
				}
				//Iterate through the arraylist and for each class get the superclass belonging to it
				//Transform from OWLClass to String to simplify further processing...

				for (int i = 0; i < classList1.size(); i++) {
					OWLClass currentClass = classList1.get(i);
					
					System.out.println(classList1.get(i));
					NodeSet<OWLClass> n = reasoner1.getSuperClasses(currentClass, true);
					Set<OWLClass> s = n.getFlattened();

					for (OWLClass j : s) {
						classesAndSuperClasses1.put(currentClass.toString(), j.toString());
					}
				}
				
				Set classes = classesAndSuperClasses1.keySet();
				Iterator setIterator = classes.iterator();
				
				//create a structure to hold classes and their superclasses (for ontology 1)
				Set<OWLClass> cls2 = o2.getClassesInSignature();
				Map<String, String> classesAndSuperClasses2 = new HashMap();
				ArrayList<OWLClass> classList2 = new ArrayList<OWLClass>();
				
				for (OWLClass i : cls2) {
					classList1.add(i);
				}
				
				//Iterate through the arraylist and for each class get the superclass belonging to it
				for (int i = 0; i < classList2.size(); i++) {
					OWLClass currentClass = classList2.get(i);
					NodeSet<OWLClass> n = reasoner2.getSuperClasses(currentClass, true);
					Set<OWLClass> s = n.getFlattened();
					for (OWLClass j : s) {
						classesAndSuperClasses2.put(currentClass.toString(), j.toString());
						
					}
				}
				
				//create a graph representation of both ontologies
				 DirectedGraph<String, DefaultEdge> g1 =
			                new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
				 
					//add vertices to the graphs from the set of classes
				 Iterator itr1 = cls1.iterator();
				 while (itr1.hasNext()) {
					 g1.addVertex(itr1.next().toString());
				 }
				 
				 //add edges to the graph
			       Set<String> keySet1 = classesAndSuperClasses1.keySet();
			       //System.out.println("The size of the set of keys in classesAndSuperClasses1 is: " + keySet1.size());
			       Iterator<String> keyIterator1 = keySet1.iterator();
			       
			       //for every key get the value associated with it and put the key + value pair as edge in the graph
			       while(keyIterator1.hasNext()) {

			    	   String key = keyIterator1.next();
			    	   Collection<String> valueSet = classesAndSuperClasses1.values();
			    	  
			    	   //need to iterate through the value set and put each value along with corresponding key
			    	   Iterator<String> iterGraph1 = valueSet.iterator();
			    	   while(iterGraph1.hasNext()) {
			    	   
			    	  String value = iterGraph1.next().toString();
			    	  if (!value.toString().equals("Nothing")) {
			    	   g1.addEdge(key, value);
			    	  }
			       }
			       }
			       
			       System.out.println("Printing graph 1");
			       System.out.println(g1.toString());
				 
				 DirectedGraph<String, DefaultEdge> g2 =
			                new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
					
				 //add vertices to the graphs from the set of classes
				 Iterator itr2 = cls2.iterator();
				 while (itr2.hasNext()) {
					 g2.addVertex(itr2.next().toString());
				 }
				 
				//add edges to the graph
			       Set<String> keySet2 = classesAndSuperClasses2.keySet();
			       Iterator keyIterator2 = keySet2.iterator();

			       //for every key get the value associated with it and put the key + value pair as edge in the graph
			       System.out.println("Printing keys");
			       while(keyIterator2.hasNext()) {
			    	   String key = (String) keyIterator2.next();
			    	   System.out.println(key);
			    	   Collection<String> valueSet2 = classesAndSuperClasses2.values();
			    	   
			    	   //need to iterate through the value set and put each value along with corresponding key
			    	   Iterator<String> iterGraph2 = valueSet2.iterator();
			    	   while(iterGraph2.hasNext()) {
			    	   String value = iterGraph2.next().toString();
			    	   if (!value.toString().equals("Nothing")) {
			    	   g2.addEdge(key, value);
			    	   }
			       }
			       }

				double distanceC1ToRoot = 0;
				double distanceC2ToRoot = 0;
				String root = "owl:Thing";
				
				FloydWarshallShortestPaths<String, DefaultEdge> path1 = new FloydWarshallShortestPaths<String, DefaultEdge>(g1);
				FloydWarshallShortestPaths<String, DefaultEdge> path2 = new FloydWarshallShortestPaths<String, DefaultEdge>(g2);

				//test
				System.out.println("Printing the classes of ontology 1");
				Set ontology1Classes = ontology1().getClasses();
				Iterator it = ontology1Classes.iterator();
				while (it.hasNext()) {
					System.out.println(it.next().toString());
				}
				
				// Match classes
				for ( Object cl2: ontology2().getClasses() ){
					for ( Object cl1: ontology1().getClasses() ){
						//test
						System.out.println("---Printing Object cl1---");
						System.out.println(cl1.toString());
						
						if (g1.containsVertex(cl1.toString())) {
							distanceC1ToRoot = path1.shortestDistance(root, cl1.toString());
						}
						
						if (g2.containsVertex(cl2.toString())) {
							distanceC2ToRoot = path2.shortestDistance(root, cl2.toString());
						}
						
						//find the ancestors of the two classes as a list of vertices
						List<String> ancestors1 = new ArrayList<String>();
						ArrayList<String> ancestors2 = new ArrayList<String>();
						
						if (g1.containsVertex(cl1.toString()) && g1.containsVertex(root)) {
//							//test
//							System.out.println(cl1.toString() + " and owl:Thing is in Graph 1");
							ancestors1 = path1.getShortestPathAsVertexList(root, cl1.toString());	
							//System.out.println("The size of ancestors1 is " + ancestors1.size());
						} else {
							System.out.println(cl1.toString() + " or " + root + " is not in Graph1");
						}
						
						if (g2.containsVertex(cl2.toString()) && g2.containsVertex(root)) {
							System.out.println(cl2.toString() + " and owl:Thing is in Graph 2");
							ancestors2 = (ArrayList<String>) path2.getShortestPathAsVertexList(root, cl2.toString());	
							//System.out.println("The size of ancestors2 is " + ancestors2.size());
						} else {
							System.out.println(cl2.toString() + " or " + root + " is not in Graph2");
						}
						
						System.out.println("The size of ancestors1 is " + ancestors1.size());
						double iSubSimScore = 0;
						ISub iSub = new ISub();
						
						//map to keep the pair of ancestors matching above the threshold
						Map<String,String> matchingMap = new HashMap<String,String>();

						for (String i : ancestors1) {
							for (String j : ancestors2) {
								iSubSimScore = iSub.score(i.toString(), j.toString());
								//if the similarity between the ancestors is equal to or above the defined threshold these two ancestors are kept
								if (iSubSimScore >= THRESHOLD) {
									System.out.println("Putting " + i.toString() + " and " + j.toString() + " in the matching map");
									matchingMap.put(i.toString(), j.toString());
								}				
							}
						}
						
						//iterate through the map of ancestors and compute the average distance to the root for the pair of ancestors and compute the structProx value
						double structProx = 0;
						double currentStructProx = 0;
						double avgAncestorDistanceToRoot = 0;
						
						//loop through the matchingMap containing key-value pairs of ancestors from O1 and O2 being similar over the given threshold
						for (Entry<String, String> entry : matchingMap.entrySet()) {
							
							avgAncestorDistanceToRoot = (path1.shortestDistance(root,entry.getKey().toString()) + path2.shortestDistance(root,entry.getValue().toString())) / 2;
							currentStructProx = (2 * avgAncestorDistanceToRoot) / (distanceC1ToRoot + distanceC2ToRoot);

							if (currentStructProx > structProx) {
								structProx = currentStructProx;
							}

						}
				
						// add mapping into alignment object 
						addAlignCell(cl1,cl2, "=", structProx);  
					}				
				}

			} catch (Exception e) { e.printStackTrace(); }
		}
		

		
		/*public void align( Alignment alignment, Properties param ) throws AlignmentException {
			try {
				// Match classes
				for ( Object cl2: ontology2().getClasses() ){
					for ( Object cl1: ontology1().getClasses() ){
				
						// add mapping into alignment object 
						addAlignCell(cl1,cl2, "=", commonSuperClassSim(cl1,cl2));  
					}				
				}

			} catch (Exception e) { e.printStackTrace(); }
		}*/
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
			//File f1 = new File("/Users/audunvennesland/Documents/PhD/Development/Experiments/Conference2Ekaw/conference/Conference.owl");
			//File f2 = new File("/Users/audunvennesland/Documents/PhD/Development/Experiments/Conference2Ekaw/conference/ekaw.owl");
			File f1 = new File("/Users/audunvennesland/Documents/PhD/Ontologies/TestOntologiesTransport/TestTransport1.owl");
			File f2 = new File("/Users/audunvennesland/Documents/PhD/Ontologies/TestOntologiesTransport/TestTransport2.owl");
			
			DirectedGraph<String, DefaultEdge> owlGraphBiblio = GraphLoader.createOWLGraph(f1);
			DirectedGraph<String, DefaultEdge> owlGraphBIBO = GraphLoader.createOWLGraph(f2);
			
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
					//test
					//System.out.println("The commonSuperClassSim similarity score between " + i.toString() + " and " + j.toString() + " is " + iSubSimScore);
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
				//System.out.println("The average distance to root for " + entry.getKey().toString() + " and " + entry.getValue() + " is " + avgAncestorDistanceToRoot);
				currentStructProx = (2 * avgAncestorDistanceToRoot) / (distanceC1ToRoot + distanceC2ToRoot);
				//test
				//System.out.println("The distance from " + s1 + " to " + root + " is " + distanceC1ToRoot);
				//System.out.println("The distance from " + s2 + " to " + root + " is " + distanceC2ToRoot);
				if (currentStructProx > structProx) {
					//test
					//System.out.println("Current struct prox is " + currentStructProx);
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
			
			//File f1 = new File("/Users/audunvennesland/Documents/PhD/Ontologies/TestOntologiesTransport/TestTransport1.owl");
			//File f2 = new File("/Users/audunvennesland/Documents/PhD/Ontologies/TestOntologiesTransport/TestTransport2.owl");
			File f1 = new File("/Users/audunvennesland/Documents/PhD/Development/Experiments/OAEIBIBLIO2BIBO/Biblio_2015.rdf");
			File f2 = new File("/Users/audunvennesland/Documents/PhD/Development/Experiments/OAEIBIBLIO2BIBO/BIBO.owl");
			
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
			//System.out.println("Trying " + s1 + " and " + s2 + " ...");
			//match the sub-classes associated with s1 and s2
			//at the moment the max score from the sub-class matching of each parameter class s1 and s2 is computed, not an aggregate similarity from all sub-classes
			for (String i : subClasses1) {
				for (String j : subClasses2) {
					currentSim = iSub.score(i, j);
					//test
					//System.out.println("The similarity score of " + i + " and " + j + " is " + currentSim);
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


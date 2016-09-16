package no.ntnu.idi.compose.Loading;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.jgrapht.*;
import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.graph.*;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgrapht.traverse.GraphIterator;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiomIndex;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * @author audunvennesland
 *
 */
public class GraphLoader {
	
	static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	static OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
	
	//Constructor
	public GraphLoader() {}
	
	
	
	/**
	 * 
	 * @param ontoFile
	 * @return
	 * @throws OWLOntologyCreationException
	 */
	public static DirectedGraph<String, DefaultEdge> createOWLGraph(File ontoFile) throws OWLOntologyCreationException
    {
		
        DirectedGraph<String, DefaultEdge> g =
                new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);

        
		Multimap<OWLClass, OWLClass> multimap = OWLLoader.getClassesAndSubClasses(ontoFile);
		Collection c = multimap.entries();
		int numClassEntries = c.size();
		//test
		//System.out.println("There are: " + numClassEntries + " entries");

        //Add vertices from OWL classes
		Set keys = multimap.keySet();
		Iterator keyIterator = keys.iterator();
		while(keyIterator.hasNext()) {
			OWLClass key = (OWLClass) keyIterator.next();
			//test
			//System.out.println("Adding " + key + " as vertex in the graph");
			g.addVertex(key.toString());
		}
		
        
       // add edges
       Set keySet = multimap.keySet();
       //System.out.println("The size of the set of keys in the multimap is: " + keySet.size());
       Iterator keyIterator2 = keySet.iterator();
       //for every key get the value associated with it and put the key + value pair as edge in the graph
       while(keyIterator2.hasNext()) {
    	   OWLClass key = (OWLClass) keyIterator2.next();
    	   Collection<OWLClass> valueSet = multimap.get(key);
    	   //need to iterate through the value set and put each value along with corresponding key
    	   Iterator iter = valueSet.iterator();
    	   while(iter.hasNext()) {
    	   
    	   OWLClass value = (OWLClass) iter.next();
    	   if (!value.toString().equals("owl:Nothing")) {
    		 //test
    		   //System.out.println("Adding " + key + " and " + value + " as edge in the graph");
    	   g.addEdge(key.toString(), value.toString());
    	   }
       }
       }

        return g;
    }
	
	//Driver method
	public static void main(String[] args) throws OWLOntologyCreationException {
		
		File file1 = new File("/Users/audunvennesland/Documents/PhD/Ontologies/Cultural Heritage/BIBO/BIBO.owl");
		
		DirectedGraph<String, DefaultEdge> owlGraph = createOWLGraph(file1);
		
		//Finding the shortest path between two vertices (classes)
		FloydWarshallShortestPaths path = new FloydWarshallShortestPaths(owlGraph);
		String classA = "<http://xmlns.com/foaf/0.1/Document>";
		String classB = "<http://purl.org/ontology/bibo/LegalCaseDocument>";
		double FWSDistance = path.shortestDistance(classA,classB);
		System.out.println("The distance is: " + FWSDistance);
		
		
		//public DepthFirstIterator(Graph<V,E> g)
		System.out.println("Depth First Search through the directed graph:\n");
		GraphIterator<String, DefaultEdge> iterator = 
		new DepthFirstIterator<String, DefaultEdge>(owlGraph);
		while (iterator.hasNext()) {
		System.out.println( iterator.next() );
		}
		
		//System.out.println(owlGraph.toString());
		
		Set edges = owlGraph.edgeSet();
		Iterator edgeItr = edges.iterator();
		
		while(edgeItr.hasNext()) {
			System.out.println(edgeItr.next());
		}
		
	}

}

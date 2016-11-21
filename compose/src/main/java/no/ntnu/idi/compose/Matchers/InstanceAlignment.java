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
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.ontowrap.HeavyLoadedOntology;
import fr.inrialpes.exmo.ontowrap.OntologyFactory;
import fr.inrialpes.exmo.ontowrap.OntowrapException;
import no.ntnu.idi.compose.Loading.OWLLoader;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;


public class InstanceAlignment extends ObjectAlignment implements AlignmentProcess {
	
	
	final double THRESHOLD = 0.9;
	static ISub isubMatcher = new ISub();
	
	public InstanceAlignment() {
	}
	
	

public void align(Alignment alignment, Properties param) throws AlignmentException {
		
		try {
			// Match classes
			for ( Object cl2: ontology2().getClasses() ){
				for ( Object cl1: ontology1().getClasses() ){
			
					// add mapping into alignment object 
					addAlignCell(cl1,cl2, "=", instanceMatch(cl1,cl2));  
				}
				
			}

		} catch (Exception e) { e.printStackTrace(); }
	}
	
	public double instanceMatch(Object o1, Object o2) throws OntowrapException, OWLOntologyCreationException {

		//get the objects (entities)
/*		String s1 = ontology1().getEntityName(o1).toLowerCase();
		String s2 = ontology2().getEntityName(o2).toLowerCase();*/
		
		String s1 = ontology1().getEntityName(o1);
		String s2 = ontology2().getEntityName(o2);
		
		System.out.println("This operation will match " + s1 + " with " + s2);
		
		//get the ontologies (currently static)
		File file1 = new File("/Users/audunvennesland/Documents/PhD/Ontologies/TestOntologiesTransport/TestTransportWithInstances1.owl");
		File file2 = new File("/Users/audunvennesland/Documents/PhD/Ontologies/TestOntologiesTransport/TestTransportWithInstances2.owl");
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();		
		OWLOntology onto1 = manager.loadOntologyFromOntologyDocument(file1);
		OWLOntology onto2 = manager.loadOntologyFromOntologyDocument(file2);
		
		//get the instances associated with the two objects
		ArrayList<String> instanceListO1 = OWLLoader.getInstances(s1, onto1);
		System.out.println("There are " + instanceListO1.size() + " associated with " + s1);
		ArrayList<String> instanceListO2 = OWLLoader.getInstances(s2, onto2);
		System.out.println("There are " + instanceListO2.size() + " associated with " + s2);
		
		double measure = 0; 
		double currentMeasure = 0;
		
		for (int i = 0; i < instanceListO1.size(); i++) {
			for (int j = 0; j < instanceListO2.size(); j++) {
				System.out.println("Matching " + instanceListO1.get(i) + " with " + instanceListO2.get(j));
				currentMeasure = isubMatcher.score(instanceListO1.get(i), instanceListO2.get(j));
				if (currentMeasure > measure) {
					measure = currentMeasure;
				}
			}
		}

		manager.removeOntology(onto1);
		manager.removeOntology(onto2);
		
		return measure;
		
	}
		
	}


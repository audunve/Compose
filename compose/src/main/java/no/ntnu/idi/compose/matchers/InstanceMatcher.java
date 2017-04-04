package no.ntnu.idi.compose.matchers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import no.ntnu.idi.compose.algorithms.ISub;
import no.ntnu.idi.compose.loading.OWLLoader;

/**
 * @author audunvennesland
 * 13. feb. 2017 
 */
public class InstanceMatcher {
	
	static ISub isubMatcher = new ISub();

	public double matchInstances(File ontoFile1, File ontoFile2) throws OWLOntologyCreationException {

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();		
		OWLOntology onto1 = manager.loadOntologyFromOntologyDocument(ontoFile1);
		OWLOntology onto2 = manager.loadOntologyFromOntologyDocument(ontoFile2);

		Set<OWLClass> onto1Classes = onto1.getClassesInSignature();
		Set<OWLClass> onto2Classes = onto2.getClassesInSignature();
		//System.out.println("There are " + classes.size() + " classes in this ontology");
		Iterator<OWLClass> onto1ClassesIterator = onto1Classes.iterator();
		Iterator<OWLClass> onto2ClassesIterator = onto1Classes.iterator();

		//Make a string representation of the OWLClasses for onto1
		ArrayList<String> listClasses1 = new ArrayList<String> ();
		while(onto1ClassesIterator.hasNext()) {
			listClasses1.add(onto1ClassesIterator.next().getIRI().getFragment());
		}

		//Make a string representation of the OWLClasses for onto1
		ArrayList<String> listClasses2 = new ArrayList<String> ();
		while(onto2ClassesIterator.hasNext()) {
			listClasses2.add(onto2ClassesIterator.next().getIRI().getFragment());
		}
		
		ArrayList<String> instanceListClasses1 = new ArrayList<String>();
		System.out.println("The size of the class list is " + listClasses1.size());
		ArrayList<String> instanceListClasses2 = new ArrayList<String>();
		
		
		Map<String, ArrayList<String>> instanceMap1 = new HashMap<String, ArrayList<String>>();
		Map<String, ArrayList<String>> instanceMap2 = new HashMap<String, ArrayList<String>>();
		
		for (int i = 0; i < listClasses1.size(); i++) {
			
			instanceListClasses1 = OWLLoader.getInstances(listClasses1.get(i), onto1);
			if (instanceListClasses1.size() > 0 && !listClasses1.get(i).equals("Thing")) {
				instanceMap1.put(listClasses1.get(i), instanceListClasses1);
			for (int j = 0; j < instanceListClasses1.size(); j++) {
				System.out.println(instanceListClasses1.get(j));
			}
			}
		}
		
		for (int i = 0; i < listClasses2.size(); i++) {
			
			instanceListClasses2 = OWLLoader.getInstances(listClasses2.get(i), onto1);
			if (instanceListClasses2.size() > 0 && !listClasses2.get(i).equals("Thing")) {
				instanceMap2.put(listClasses2.get(i), instanceListClasses2);
			for (int j = 0; j < instanceListClasses2.size(); j++) {
				System.out.println(instanceListClasses2.get(j));
			}
			}
		}
		
		//iterate over the instanceMap
		for (Map.Entry<String,ArrayList<String>> entry : instanceMap1.entrySet()) {
			System.out.println(entry.getKey() + " --> " + entry.getValue());
			
		}
		
		//iterate over the instanceMap
		for (Map.Entry<String,ArrayList<String>> entry : instanceMap2.entrySet()) {
			System.out.println(entry.getKey() + " --> " + entry.getValue());
			
		}
		
		//match the instance (values) of each instance map and if above a threshold
		//output the equivalent classes
		for (Map.Entry<String, ArrayList<String>> entry01 : instanceMap1.entrySet()) {
			for (Map.Entry<String, ArrayList<String>> entry02 : instanceMap2.entrySet()) {
				for ()
			}
		}




		double instanceScore = 0;

		return instanceScore;
	}

	public static void main(String[] args) {

		//import the owl files
		File ontoFile1 = new File("./files/UoA/TestTransportWithInstances1.owl");
		File ontoFile2 = new File("./files/UoA/TestTransportWithInstances2.owl");
	}

}

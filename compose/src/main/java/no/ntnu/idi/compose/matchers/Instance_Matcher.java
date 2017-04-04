package no.ntnu.idi.compose.matchers;


import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.ivml.alimo.ISub;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;

import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.ontowrap.OntowrapException;
import no.ntnu.idi.compose.loading.OWLLoader;


public class Instance_Matcher extends ObjectAlignment implements AlignmentProcess {
	
	
	final double THRESHOLD = 0.1;
	static ISub isubMatcher = new ISub();
	static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	static OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
	
	Map<String, ArrayList<String>> instanceMapO1 = new HashMap<String, ArrayList<String>>();
	Map<String, ArrayList<String>> instanceMapO2 = new HashMap<String, ArrayList<String>>();
	
	public Instance_Matcher() {
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

	public double instanceMatch(Object o1, Object o2) throws OntowrapException, OWLOntologyCreationException, MalformedURLException {
		
		URI url1 = getOntology1URI();
		//String f1 = getOntology1URI().getRawPath();
		//System.out.println("The raw path is " + f1);
		URI url2 = getOntology2URI();
		
		String s1 = ontology1().getEntityName(o1);
		String s2 = ontology2().getEntityName(o2);
		
		//get the OWL API representation of the two ontologies to be matched
		manager = OWLManager.createOWLOntologyManager();
		IRI iriA = IRI.create(url1);
		OWLOntology ontA = manager.loadOntologyFromOntologyDocument(iriA);

		IRI iriB = IRI.create(url2);
		OWLOntology ontB = manager.loadOntologyFromOntologyDocument(iriB);
		
		//get the classes of the two ontologies and compare with o1 and o2
		Set<OWLClass> classesO1 = ontA.getClassesInSignature();
		Iterator<OWLClass> clsO1Itr = classesO1.iterator();
		Set<OWLClass> classesO2 = ontB.getClassesInSignature();
		Iterator<OWLClass> clsO2Itr = classesO1.iterator();
		
		//Make a string representation of the OWLClasses
				ArrayList<String> clsO1String = new ArrayList<String> ();
				while(clsO1Itr.hasNext()) {
					clsO1String.add(clsO1Itr.next().getIRI().getFragment());
				}
				
				ArrayList<String> clsO2String = new ArrayList<String> ();
				while(clsO2Itr.hasNext()) {
					clsO2String.add(clsO2Itr.next().getIRI().getFragment());
				}
		
				ArrayList<String> instanceO1List = new ArrayList<String>();
				ArrayList<String> instanceO2List = new ArrayList<String>();
				
				//create a map holding the class vs instances for ontology 1
				for (int i = 0; i < clsO1String.size(); i++) {				
					instanceO1List = OWLLoader.getInstances(clsO1String.get(i), ontA);
					if (instanceO1List.size() > 0 && !clsO1String.get(i).equals("Thing")) {
						instanceMapO1.put(clsO1String.get(i), instanceO1List);
					for (int j = 0; j < instanceO1List.size(); j++) {
						//System.out.println(instanceO1List.get(j));
					}
					}

				}
				
				//create a map holding the class vs instances for ontology 2
				for (int i = 0; i < clsO2String.size(); i++) {				
					instanceO2List = OWLLoader.getInstances(clsO2String.get(i), ontA);
					if (instanceO2List.size() > 0 && !clsO2String.get(i).equals("Thing")) {
						instanceMapO1.put(clsO2String.get(i), instanceO2List);
					for (int j = 0; j < instanceO2List.size(); j++) {
						//System.out.println(instanceO2List.get(j));
					}
					}

				}
				
				ArrayList<String> instancesO1 = new ArrayList<String>();
				System.out.println("The size of instancesO1 is " + instancesO1.size());
				ArrayList<String> instancesO2 = new ArrayList<String>();
				
				double measure = 0; 
				double currentMeasure = 0;
				
				//iterate the maps and if s1 is equal to any of the keys in the map and if s2 is equal to any of the keys in the map...
				//then compare their list of instances
				//If any of the instances in list of o1 matches with any of the instances in the list of o2...
				//update the measure with the currentMeasure
				for (Map.Entry<String,ArrayList<String>> mapO1 : instanceMapO1.entrySet()) {
					for (Map.Entry<String,ArrayList<String>> mapO2 : instanceMapO2.entrySet()) {
					if (mapO1.getKey().equals(s1) && mapO2.getKey().equals(s2)) {
						//compare their instances
						instancesO1 = mapO1.getValue();
						instancesO2 = mapO2.getValue();
						for (String instO1 : instancesO1){
							for (String instO2 : instancesO2){
								currentMeasure = isubMatcher.score(instO1, instO2);
								if (currentMeasure > measure) {
									measure = currentMeasure;
								}
							}
								
							}
						}
					}
					
				}	

		
/*		manager.removeOntology(ontA);
		manager.removeOntology(ontB);*/
		
		return measure;
		
	}
	

	

	}
	


package no.ntnu.idi.compose.Loading;

import java.io.File;
import java.util.Iterator;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;


/**
 * @author audunvennesland
 * @version 1.0
 * @created 21-apr-2016 10:34:52
 */
public class OWLLoader {

	public static OWLOntologyManager manager;

	public OWLLoader(){

	}

	/**
	 * 
	 * @param ontologyFile
	 */
	public void loadOntologyFromFile(File ontologyFile){

	}

	public void loadOntologyFromURI(){

	}

	/**
	 * 
	 * @param ontologyFile
	 */
	public String getOntologyFormat(File ontologyFile){
		return "";
	}

	

//Get overall statistics of the two input ontologies that will be used for further processing
	
	public static int getNumClasses(File file1, File file2) throws OWLOntologyCreationException {
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		
		OWLOntology onto1 = manager.loadOntologyFromOntologyDocument(file1);
		OWLOntology onto2 = manager.loadOntologyFromOntologyDocument(file2);
		
		int numClasses = onto1.getClassesInSignature().size() + onto2.getClassesInSignature().size();
		return numClasses;
	}
	
	public static int getNumObjectProperties(File file1, File file2) throws OWLOntologyCreationException {
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		
		OWLOntology onto1 = manager.loadOntologyFromOntologyDocument(file1);
		OWLOntology onto2 = manager.loadOntologyFromOntologyDocument(file2);
		int numObjectProperties = onto1.getObjectPropertiesInSignature().size() + onto2.getObjectPropertiesInSignature().size();
		return numObjectProperties;
	}
	
	public static int getNumIndividuals(File file1, File file2) throws OWLOntologyCreationException {

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		
		OWLOntology onto1 = manager.loadOntologyFromOntologyDocument(file1);
		OWLOntology onto2 = manager.loadOntologyFromOntologyDocument(file2);
		int numIndividuals = onto1.getIndividualsInSignature().size() + onto2.getIndividualsInSignature().size();
		return numIndividuals;
	}
	
	public static int getNumSubClasses(File file1) throws OWLOntologyCreationException {
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();		
		OWLOntology onto1 = manager.loadOntologyFromOntologyDocument(file1);
		
		//need a reasoner to get to the subclasses
		OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
		OWLReasoner reasoner = reasonerFactory.createReasoner(onto1);
		
		//a node set to hold the subclasses
		NodeSet<OWLClass> subcls;
		
		//get all classes in the ontology (including their IRI) and put them in a data structure
		Iterator<OWLClass> itr = onto1.getClassesInSignature().iterator();
		
		
		//counts number of subclasses for each ontology class
		int localNumSubClasses = 0;
		
		//counter to keep track of all subclasses over all ontology classes
		int counter = 0;
		
		
		//iterate over all classes and update counter
		while (itr.hasNext()) {
			localNumSubClasses = itr.next().getIRI().getClassesInSignature().size();
		}
			counter += localNumSubClasses;

		return localNumSubClasses;
	}
	
	public static int getNumAxioms(File file1) throws OWLOntologyCreationException {
		
		int numAxioms = 0;
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();		
		OWLOntology onto1 = manager.loadOntologyFromOntologyDocument(file1);
		
		numAxioms = onto1.getAxiomCount();
		
		
		return numAxioms;
	}
	
	public static boolean containsIndividuals(OWLClass ontoClass) {

		int numIndividuals = ontoClass.getIndividualsInSignature().size();
		
		if (numIndividuals > 0) {
		return true;} else {
			return false;
		}	
	}
	
	//test method
	public static void main(String[] args) throws OWLOntologyCreationException {
		
		//import the owl files
		File file1 = new File("/Users/audunvennesland/Documents/PhD/Ontologies/Cultural Heritage/BIBO/BIBO.owl");
		File file2 = new File("/Users/audunvennesland/Documents/PhD/Ontologies/Cultural Heritage/Bibtex Ontology/BibTex.owl");
		
		System.out.println("Number of classes in these two ontologies are: " + getNumClasses(file1, file2));
		System.out.println("Number of object properties in these two ontologies are: " + getNumObjectProperties(file1, file2));
		System.out.println("Number of individuals in these two ontologies are: " + getNumIndividuals(file1, file2));
		
		System.out.println("Number of subclasses of ontology " + getNumSubClasses(file1));
		
		System.out.println("Number of axioms for BIBO: " + getNumAxioms(file1));
		System.out.println("Number of axioms for BibTex: " + getNumAxioms(file2));

		
	}


}
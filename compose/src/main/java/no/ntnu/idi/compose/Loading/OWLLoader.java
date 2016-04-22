package no.ntnu.idi.compose.Loading;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiomIndex;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDocumentFormat;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
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


	private static OWLAxiomIndex ontology;
	static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	static OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();

	public OWLLoader(){

	}

	/**
	 * 
	 * @param ontologyFile
	 * @throws OWLOntologyCreationException 
	 */
	public static String getOntologyFormat(File ontoFile) throws OWLOntologyCreationException{

		//TO-DO: Currently throws a java.lang.NoSuchMethodError, so needs to be fixed.

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);

		OWLDocumentFormat format = manager.getOntologyFormat(onto);

		String ontologyFormat = format.toString();

		return ontologyFormat;
	}


	//Get overall statistics of the two input ontologies that will be used for further processing

	public static int getNumClasses(File ontoFile) throws OWLOntologyCreationException {

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);

		int numClasses = onto.getClassesInSignature().size();

		return numClasses;
	}

	public static int getNumObjectProperties(File ontoFile) throws OWLOntologyCreationException {

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);
		int numObjectProperties = onto.getObjectPropertiesInSignature().size();

		return numObjectProperties;
	}

	public static int getNumIndividuals(File ontoFile) throws OWLOntologyCreationException {

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);

		int numIndividuals = onto.getIndividualsInSignature().size();

		return numIndividuals;
	}

	public static int getNumSubClasses(File ontoFile) throws OWLOntologyCreationException {

		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);

		OWLReasoner reasoner = reasonerFactory.createReasoner(onto);

		OWLClass thisClass;
		NodeSet<OWLClass> subClasses;
		Iterator<OWLClass> itr = onto.getClassesInSignature().iterator();
		Map<OWLClass, NodeSet<OWLClass>> classesAndSubClasses = new HashMap<OWLClass, NodeSet<OWLClass>>();
		int subClassCount = 0;
		int totalSubClassCount = 0;

		while (itr.hasNext()) {
			thisClass = itr.next();
			subClasses = reasoner.getSubClasses(thisClass, true);
			subClassCount = subClasses.getNodes().size();
			classesAndSubClasses.put(thisClass, subClasses);
			totalSubClassCount += subClassCount;
		}		

		return totalSubClassCount;
	}


	public static int getNumAxioms(File ontoFile) throws OWLOntologyCreationException {

		int numAxioms = 0;

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();		
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);

		numAxioms = onto.getAxiomCount();


		return numAxioms;
	}

	public static int containsIndividuals(File ontoFile) throws OWLOntologyCreationException {
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();		
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);
		OWLReasoner reasoner = reasonerFactory.createReasoner(onto);
		Iterator<OWLClass> itr = onto.getClassesInSignature().iterator();
		int countClassesWithIndividuals = 0;
		
		OWLClass thisClass;
		
		while (itr.hasNext()) {
			thisClass = itr.next();
			if (!reasoner.getInstances(thisClass, true).isEmpty()) {
				countClassesWithIndividuals++;
			}
	
		}
		return countClassesWithIndividuals;
	}





	//test method
	public static void main(String[] args) throws OWLOntologyCreationException {

		//import the owl files
		//File file1 = new File("/Users/audunvennesland/Documents/PhD/Ontologies/Cultural Heritage/BIBO/BIBO.owl");
		File file2 = new File("/Users/audunvennesland/Documents/PhD/Ontologies/Cultural Heritage/Bibtex Ontology/BibTex.owl");

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();		
		//OWLOntology onto1 = manager.loadOntologyFromOntologyDocument(file1);

		//Iterator<OWLClass> itr = onto1.getClassesInSignature().iterator();

		//System.out.println("Number of classes in this ontology is: " + getNumClasses(file1));
		//System.out.println("Number of object properties in these two ontologies are: " + getNumObjectProperties(file1, file2));
		//System.out.println("Number of individuals in BIBO are: " + getNumIndividuals(file2));

		//System.out.println("Number of axioms for BIBO: " + getNumAxioms(file1));
		//System.out.println("Number of axioms for BibTex: " + getNumAxioms(file2));

		//System.out.println("Number of subclasses in BIBO are: " + getNumSubClasses(file1));
		
		System.out.println("BIBO contains " + containsIndividuals(file2) + " classes holding individuals" );







	}


}
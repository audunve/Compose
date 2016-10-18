package no.ntnu.idi.compose.Loading;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiomIndex;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDocumentFormat;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLNamedObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;

import no.ntnu.idi.compose.WordNet.WordNetLexicon;


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
	 * Get the ontology format (e.g. OWL 2) from an ontology
	 * @param ontoFile
	 * @throws OWLOntologyCreationException 
	 */
	public static String getOntologyFormat(File ontoFile) throws OWLOntologyCreationException{

		//TO-DO: Currently throws a java.lang.NoSuchMethodError, so needs to be fixed.

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);

		OWLDocumentFormat format = manager.getOntologyFormat(onto);

		String ontologyFormat = format.toString();

		manager.removeOntology(onto);

		return ontologyFormat;
	}
	
	
/*	*//**
	 * Method that retrieves all classes and corresponding sub-classes from an ontology and puts them in a multimap where the classes are key and their subclasses are values
	 * Note that this method parses the OWL classes to a string representation, which is problematic when there are duplicate classes (e.g. from other imported ontologies)
	 * @param ontoFile
	 * @throws OWLOntologyCreationException 
	 */
		
		public static Multimap<String,String> getClassesAndSubClassesToString(File ontoFile) throws OWLOntologyCreationException {
			
			OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);
			OWLReasoner reasoner = reasonerFactory.createReasoner(onto);
			Set<OWLClass> cls = onto.getClassesInSignature();
			Multimap<String, String> classesAndSubClasses = ArrayListMultimap.create();
			ArrayList<OWLClass> classList = new ArrayList<OWLClass>();
			
			for (OWLClass i : cls) {
				classList.add(i);
			}
			
			//Iterate through the arraylist and for each class get the subclasses belonging to it
			//Transform from OWLClass to String to simplify further processing...
			for (int i = 0; i < classList.size(); i++) {
				OWLClass currentClass = classList.get(i);
				NodeSet<OWLClass> n = reasoner.getSubClasses((OWLClassExpression) currentClass, true);
				NodeSet<OWLClass> o = reasoner.getSuperClasses(currentClass, true);
				Set<OWLClass> s = n.getFlattened();
				for (OWLClass j : s) {
					classesAndSubClasses.put(currentClass.getIRI().getFragment(), j.getIRI().getFragment());
				}
			}

			manager.removeOntology(onto);
			
			return classesAndSubClasses;
			
		}
		
		public static Map<String, String> getClassesAndSuperClasses (File ontoFile) throws OWLOntologyCreationException {
			
			OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);
			OWLReasoner reasoner = reasonerFactory.createReasoner(onto);
			Set<OWLClass> cls = onto.getClassesInSignature();
			Map<String, String> classesAndSuperClasses = new HashMap();
			ArrayList<OWLClass> classList = new ArrayList<OWLClass>();
			
			for (OWLClass i : cls) {
				classList.add(i);
			}
			
			//Iterate through the arraylist and for each class get the subclasses belonging to it
			//Transform from OWLClass to String to simplify further processing...
			for (int i = 0; i < classList.size(); i++) {
				OWLClass currentClass = classList.get(i);
				NodeSet<OWLClass> n = reasoner.getSuperClasses(currentClass, true);
				Set<OWLClass> s = n.getFlattened();
				for (OWLClass j : s) {
					classesAndSuperClasses.put(currentClass.getIRI().getFragment(), j.getIRI().getFragment());
				}
			}

			manager.removeOntology(onto);
			
			return classesAndSuperClasses;
			
		}
		
public static Map<String, String> getClassesAndSuperClasses (OWLOntology o) throws OWLOntologyCreationException {
			
			//OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);
			OWLReasoner reasoner = reasonerFactory.createReasoner(o);
			Set<OWLClass> cls = o.getClassesInSignature();
			Map<String, String> classesAndSuperClasses = new HashMap<String, String>();
			ArrayList<OWLClass> classList = new ArrayList<OWLClass>();
			
			for (OWLClass i : cls) {
				classList.add(i);
			}
			
			//Iterate through the arraylist and for each class get the subclasses belonging to it
			//Transform from OWLClass to String to simplify further processing...
			for (int i = 0; i < classList.size(); i++) {
				OWLClass currentClass = classList.get(i);
				NodeSet<OWLClass> n = reasoner.getSuperClasses(currentClass, true);
				Set<OWLClass> s = n.getFlattened();
				for (OWLClass j : s) {
					classesAndSuperClasses.put(currentClass.getIRI().getFragment(), j.getIRI().getFragment());
				}
			}

			manager.removeOntology(o);
			
			return classesAndSuperClasses;
			
		}
		
	
	public static Multimap<OWLClass,OWLClass> getClassesAndSubClasses(File ontoFile) throws OWLOntologyCreationException {
		
		File ontoF = new File(ontoFile.toURI());
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);
		OWLReasoner reasoner = reasonerFactory.createReasoner(onto);
		Set<OWLClass> cls = onto.getClassesInSignature();
		Multimap<OWLClass, OWLClass> classesAndSubClasses = ArrayListMultimap.create();
		ArrayList<OWLClass> classList = new ArrayList<OWLClass>();
		
		for (OWLClass i : cls) {
			classList.add(i);
		}
		
		//Iterate through the arraylist and for each class get the subclasses belonging to it
		//Transform from OWLClass to String to simplify further processing...
		for (int i = 0; i < classList.size(); i++) {
			OWLClass currentClass = classList.get(i);
			NodeSet<OWLClass> n = reasoner.getSubClasses((OWLClassExpression) currentClass, true);
			Set<OWLClass> s = n.getFlattened();
			for (OWLClass j : s) {
				classesAndSubClasses.put(currentClass, j);
			}
		}

		manager.removeOntology(onto);
		
		return classesAndSubClasses;
		
	}
	

			
		
		/**
		 * Method for retrieving all sub-classes from a single OWL class
		 * @param cls
		 * @param ontoFile
		 * @return A NodeSet<OWLClass> of subclasses
		 * @throws OWLOntologyCreationException
		 */
		public static NodeSet<OWLClass> getSubClasses(OWLClass cls, File ontoFile) throws OWLOntologyCreationException {
			
			OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);
			
			NodeSet<OWLClass> subClasses;
			OWLReasoner reasoner = reasonerFactory.createReasoner(onto);
			subClasses = reasoner.getSubClasses(cls, true);
			
			manager.removeOntology(onto);
			
			return subClasses;
		}
		
		public static NodeSet<OWLClass> getSuperClasses(OWLClass cls, File ontoFile) throws OWLOntologyCreationException {
			
			OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);
			
			NodeSet<OWLClass> subClasses;
			OWLReasoner reasoner = reasonerFactory.createReasoner(onto);
			subClasses = reasoner.getSuperClasses(cls, true);
			
			manager.removeOntology(onto);
			
			return subClasses;
		}


		/**
		 * Get number of classes in an ontology
		 * @param ontoFile
		 * @return numClasses
		 * @throws OWLOntologyCreationException
		 */
	public static int getNumClasses(File ontoFile) throws OWLOntologyCreationException {

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);
		int numClasses = onto.getClassesInSignature().size();

		manager.removeOntology(onto);

		return numClasses;
	}
	
	public static Set<OWLObjectProperty> getProperties(File ontoFile) throws OWLOntologyCreationException {
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);
		OWLReasoner reasoner = reasonerFactory.createReasoner(onto);

		Set<OWLObjectProperty> props = onto.getObjectPropertiesInSignature();

		return props;	
	}
	
	public Set<OWLObjectProperty> getObjectProperties(OWLClass c) {
		
		Set<OWLObjectProperty> props = c.getObjectPropertiesInSignature();
		
//		Map<OWLClass, OWLObjectProperty> classesAndProperties = new HashMap<OWLClass, OWLObjectProperty>();
		
		return props;
		
	}

	/**
	 * 
	 * @param ontoFile
	 * @return
	 * @throws OWLOntologyCreationException
	 */
	public static int getNumObjectProperties(File ontoFile) throws OWLOntologyCreationException {

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);
		int numObjectProperties = onto.getObjectPropertiesInSignature().size();

		manager.removeOntology(onto);

		return numObjectProperties;
	}

	public static int getNumIndividuals(File ontoFile) throws OWLOntologyCreationException {

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);

		int numIndividuals = onto.getIndividualsInSignature().size();
		

		manager.removeOntology(onto);

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

		manager.removeOntology(onto);

		return totalSubClassCount;
	}


	public static int getNumAxioms(File ontoFile) throws OWLOntologyCreationException {

		int numAxioms = 0;

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();		
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);

		numAxioms = onto.getAxiomCount();

		manager.removeOntology(onto);

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
		manager.removeOntology(onto);

		return countClassesWithIndividuals;
	}

	public static int getNumClassesWithoutComments(File ontoFile) throws OWLOntologyCreationException {

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();		
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);
		Iterator<OWLClass> itr = onto.getClassesInSignature().iterator();
		int countClassesWithComments = 0;
		int sumClasses = onto.getClassesInSignature().size();	

		IRI thisClass;

		while (itr.hasNext()) {
			thisClass = itr.next().getIRI();
			
			for (OWLAnnotationAssertionAxiom a : onto.getAnnotationAssertionAxioms(thisClass)) {
				if (a.getProperty().isComment()) {
					countClassesWithComments++;
				}
			}

			}
		
		manager.removeOntology(onto);
		
		int numClassesWithoutComments = sumClasses - countClassesWithComments;

		return numClassesWithoutComments;
	}
	
	public static int getNumClassesWithoutLabels(File ontoFile) throws OWLOntologyCreationException {

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();		
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);
		Iterator<OWLClass> itr = onto.getClassesInSignature().iterator();
		int countClassesWithLabels = 0;
		int sumClasses = onto.getClassesInSignature().size();

		IRI thisClass;

		while (itr.hasNext()) {
			thisClass = itr.next().getIRI();
			
			for (OWLAnnotationAssertionAxiom a : onto.getAnnotationAssertionAxioms(thisClass)) {
				if (a.getProperty().isLabel()) {
					countClassesWithLabels++;
				}
			}

			}
		
		manager.removeOntology(onto);
		
		int numClassesWithoutLabels = sumClasses - countClassesWithLabels;

		return numClassesWithoutLabels;
	}
	
	public static int containsObjectPropertyCommentsOrLabels(File ontoFile) throws OWLOntologyCreationException {

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();		
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);
		Iterator<OWLObjectProperty> itr_props = onto.getObjectPropertiesInSignature().iterator();
		int countObjectPropertiesWithCommentsOrLabels = 0;

		IRI thisObjectProperty;

		while (itr_props.hasNext()) {
			thisObjectProperty = itr_props.next().getIRI();
			
			for (OWLAnnotationAssertionAxiom a : onto.getAnnotationAssertionAxioms(thisObjectProperty)) {
				if (a.getProperty().isComment()) {
					countObjectPropertiesWithCommentsOrLabels++;
				}
			}

			}
		
		manager.removeOntology(onto);

		return countObjectPropertiesWithCommentsOrLabels;
	}
	
	public static int containsDataPropertyCommentsOrLabels(File ontoFile) throws OWLOntologyCreationException {

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();		
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);
		Iterator<OWLDataProperty> itr_props = onto.getDataPropertiesInSignature().iterator();
		int countDataPropertiesWithCommentsOrLabels = 0;

		IRI thisDataProperty;

		while (itr_props.hasNext()) {
			thisDataProperty = itr_props.next().getIRI();
			
			for (OWLAnnotationAssertionAxiom a : onto.getAnnotationAssertionAxioms(thisDataProperty)) {
				if (a.getProperty().isComment()) {
					countDataPropertiesWithCommentsOrLabels++;
				}
			}

			}
		
		manager.removeOntology(onto);

		return countDataPropertiesWithCommentsOrLabels;
	}


	public static double getWordNetCoverage(File ontoFile) throws OWLOntologyCreationException {
	
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();		
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);
		Iterator<OWLClass> itr = onto.getClassesInSignature().iterator();
		
		String thisClass;
		
		int numClasses = onto.getClassesInSignature().size();
		
		int counter = 0;
		
		while(itr.hasNext()) {
			thisClass = itr.next().getIRI().getFragment();
			if (WordNetLexicon.containedInWordNet(thisClass) == true) {
				counter++;			
			}		
		}
		
		double wordNetCoverage = (double)counter / (double)numClasses;
		
		return wordNetCoverage;	
	}
	
	public static boolean isCompound(String a) {
		boolean test = false;

		String[] compounds = a.split("(?<=.)(?=\\p{Lu})");

			if (compounds.length > 1) {
				test = true;
			}

		return test;
	}
	
	public static String replaceUnderscore (String input) {
		String newString = null;
		Pattern p = Pattern.compile( "_([a-zA-Z])" );
		Matcher m = p.matcher(input);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			m.appendReplacement(sb, m.group(1).toUpperCase());
		}
		
		m.appendTail(sb);
		newString = sb.toString();
		
		return newString;
	}
	
	public static double getNumCompounds(File ontoFile) throws OWLOntologyCreationException {
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();		
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);
		
		Iterator<OWLClass> itr = onto.getClassesInSignature().iterator();
		
		String thisClass;
		
		int numClasses = onto.getClassesInSignature().size();
		
		int counter = 0;
		
		while(itr.hasNext()) {
			thisClass = replaceUnderscore(itr.next().getIRI().getFragment());
			System.out.println("Testing " + thisClass.toString());
			if (isCompound(thisClass) == true) {
				counter++;			
				System.out.println("Now the counter is " + counter);
			}		
		}
		
		//double numCompounds = (counter / numClasses) * 100;
		double numCompounds = ((double)counter/(double)numClasses);
		
		return numCompounds;	
	}



	//test method
	public static void main(String[] args) throws OWLOntologyCreationException {

		//import the owl files
		File file1 = new File("/Users/audunvennesland/Documents/PhD/Ontologies/Cultural Heritage/BIBO/BIBO.owl");
		File file2 = new File("/Users/audunvennesland/Documents/PhD/Ontologies/OAEI/OAEI2015/Biblio/Biblio_2015.rdf");
		File file3 = new File("/Users/audunvennesland/Documents/PhD/Ontologies/Schema.org/schema.rdf");
		

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();		
		OWLOntology onto1 = manager.loadOntologyFromOntologyDocument(file1);
		
		

		/*Map<String, String> m = getClassesAndSuperClasses(onto1);
		Set<String> classes = m.keySet();
		Iterator<String> itr = classes.iterator();
		System.out.println("Printing the superclasses\n");
		while (itr.hasNext()) {
			System.out.println("The superclass of " + itr.next() + " is " + m.get(itr.next()));
		}*/
		
		/*Set<OWLClass> classes = onto1.getClassesInSignature();
		Map<OWLClass, Set<OWLObjectProperty>> classesAndObjectProperties = new HashMap<OWLClass, Set<OWLObjectProperty>>();
		
		Iterator<OWLClass> itr = classes.iterator();
		System.out.println("Printing object properties of each class");
		while (itr.hasNext()) {
			OWLClass thisClass = itr.next();
			Set<OWLObjectProperty> props = thisClass.getObjectPropertiesInSignature();
			classesAndObjectProperties.put(thisClass, props);
		}
		
		for (Map.Entry<OWLClass, Set<OWLObjectProperty>> entry : classesAndObjectProperties.entrySet()) {
			System.out.println("Key : " + entry.getKey() + " Value : " + entry.getValue().size());
		}*/
		

		
       Set<OWLObjectProperty> props = getProperties(file1);
       Iterator<OWLObjectProperty> propsItr = props.iterator();
       
       while(propsItr.hasNext()) {
    	   OWLObjectProperty prop = propsItr.next();
    	   System.out.println(prop);
       }
       
		manager.removeOntology(onto1);
		

	}


}
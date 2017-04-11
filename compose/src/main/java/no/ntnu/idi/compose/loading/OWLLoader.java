package no.ntnu.idi.compose.loading;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import org.apache.jena.ext.com.google.common.collect.ArrayListMultimap;
//import org.apache.jena.ext.com.google.common.collect.Multimap;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationSubject;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;

import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

import edu.wright.cheatham.propstring.Preprocessing;
import misc.WNDomain;
import misc.WordNetLexicon;
import net.didion.jwnl.JWNLException;
import no.ntnu.idi.compose.preprocessing.Preprocessor;


/**
 * @author audunvennesland
 * Date:02.02.2017
 * @version 1.0
 */
public class OWLLoader {

	/**
	 * An OWLOntologyManagermanages a set of ontologies. It is the main point for creating, loading and
	 * accessing ontologies.
	 */
	static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	/**
	 *  The OWLReasonerFactory represents a reasoner creation point.
	 */
	static OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
	
	/**
	 * A HashMap holding an OWLEntity as key and an ArrayList of instances associated with the OWLEntity
	 */
	private static HashMap<OWLEntity, ArrayList<String>> instanceMap = new HashMap<>();

	public OWLLoader(){

	}
	
	public static List<Long> getOffsetList(File ontoFile) throws OWLOntologyCreationException, FileNotFoundException, JWNLException {
		List<Long> offsetList = new ArrayList<Long>();
		
		//get a list of all class names in ontology
		 ArrayList<String> classNameList = createClassList(ontoFile);
		 System.out.println("The number of classes in classNameList is " + classNameList.size());
		
		//for every class name, get its domain by using the synset offset of the class as query
		 for (String s : classNameList) {
			 //get all offsets associated with s
			 offsetList = WNDomain.findSynsetOffset(s);
		 }
			
		return offsetList;
	}
	
	//public String findDomain(String WNDomainsFileName,String searchStr) 
	public static ArrayList<String> getDomainList(List<Long> offsetList) throws FileNotFoundException {
		String wnDomainsFile = "./files/wndomains/wn-domains-3.2-20070223.txt";
		
		ArrayList<String> domainList = new ArrayList<String>();
		
		String thisOffset = null;
		String thisDomain = null;
		
		for (Long l : offsetList) {
			thisOffset = l.toString();
			thisDomain = WNDomain.findDomain(wnDomainsFile, thisOffset);
			domainList.add(thisDomain);
		}
		
		return domainList;
	}

	/**
	 * Returns a Map<String, Integer> holding the domains included in an input ontology as key and a computed coverage (number of occurrences / total number of class names in the ontology)
	 * @param ontoFile
	 * @return a Map<[domain], [coverage]>
	 * @throws OWLOntologyCreationException
	 * @throws JWNLException 
	 * @throws FileNotFoundException 
	 *//*
	 public static Map<String, List<Long>> createOffsetMap(File ontoFile) throws OWLOntologyCreationException, FileNotFoundException, JWNLException {
		 Map<String, List<Long>> offsetMap = new HashMap<String, List<Long>>();
		 
		 List<Long> offsetList = new ArrayList<Long>();
		 
		 //get a list of all class names in ontology
		 ArrayList<String> classNameList = createClassList(ontoFile);
		 
		 //for every class name, get its domain by using the synset offset of the class as query
		 for (String s : classNameList) {
			 //get all offsets associated with s
			 offsetList = WNDomain.findSynsetOffset(s);
			 //put class name s as key and the list of offsets associated with s as value in the Map
			 offsetMap.put(s, offsetList);
		 }
		 
		 return offsetMap;		 
	 }
	 
	 public static Map<String, ArrayList<String>> createDomainMap(Map<String, List<Long>> offsetMap) {
		 Map<String, ArrayList<String>> domainMap = new HashMap<String, ArrayList<String>>();
		 ArrayList<String> domains = new ArrayList<String>();
		 List<Long> offsetList = new ArrayList<Long>();
		 Long l = null;
		 
		 //create an ArrayList of strings for the domain for every offset
		 //iterate through each entry of the offsetMap
		 String className = null;
		 for (Map.Entry<String,List<Long>> entry : offsetMap.entrySet()) {
			 className = entry.getKey();
			 offsetList = entry.getValue();
			 for (Long o : offsetList) {
				 domains.add(o.toString());
			 }
			 
			 System.out.println("Putting " + className + " to the domainMap");
			 domainMap.put(className, domains);
				
			}
		 
		 return domainMap;
	 }*/
	 
	 /**
	  * Returns an arraylist of class names in an ontology
	  * @param ontoFile The file path to the owl file
	  * @return an ArrayList<String> holding the class names of the input ontology
	  * @throws OWLOntologyCreationException
	  */
	 private static ArrayList<String> createClassList(File ontoFile) throws OWLOntologyCreationException {
		 ArrayList<String> classList = new ArrayList<String>();

		 OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		 OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);
		
		 Set<OWLClass> clsSet = onto.getClassesInSignature();
		 //Iterator<OWLClass> itr = clsSet.iterator();
		 String thisClass = null;

		 	for (OWLClass cl : clsSet) {
		 		 thisClass = cl.getIRI().getFragment();
				 System.out.println("The class is " + thisClass);
				 classList.add(Preprocessor.stringTokenize(thisClass, true));
				 System.out.println("Adding " + thisClass + " to the ArrayList");
				 System.out.println("The number of class names in classList is " + classList.size());
		 	}
		 
		 return classList; 
	 }
	
		/**
		 * Returns a Map holding a class as key and its superclass as value
		 * @param o the input OWL ontology from which classes and superclasses should be derived
		 * @return  classesAndSuperClasses a Map holding a class as key and its superclass as value
		 * @throws OWLOntologyCreationException An exception which describes an error during the creation of an ontology. If an ontology cannot be created then subclasses of this class will describe the reasons.
		 */
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
		

		
		
		/**
		 * Get all instances associated with a class in an ontology
		 * @param owlClass	the OWL class from which instances should be retrieved
		 * @param ontology 	the OWL ontology holding the owl class and its instances 
		 * @return instanceList an ArrayList (String) of instances
		 */
		@SuppressWarnings("deprecation")
		public static ArrayList<String> getInstances(String owlClass, OWLOntology ontology) {
			ArrayList<String> instanceList = new ArrayList<String>();
			
			OWLReasonerFactory reasonerFactory = new PelletReasonerFactory();
		    OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(ontology);
		    
		    for (OWLClass c : ontology.getClassesInSignature()) {

				if (c.getIRI().getFragment().equals(owlClass)) {
					//Test
					//System.out.println("Found the class " + owlClass);
					
					NodeSet<OWLNamedIndividual> instanceSet = reasoner.getInstances(c, false);
					Iterator<Node<OWLNamedIndividual>> itr = instanceSet.iterator();
					if (!itr.hasNext()) {
						//Test
						//System.out.println("There are no instances associated with " + c.getIRI().getFragment());
						break;
					} else {
					
					

					for (OWLNamedIndividual i : instanceSet.getFlattened()) {
						//Test
						//System.out.println("Adding " + i.getIRI().getFragment() + " to the list");
						instanceList.add(i.getIRI().getFragment());
					}
					}
				}
		    }
			
			return instanceList;
		}


		/**
		 * Get number of classes in an ontology
		 * @param ontoFile	the file path of the OWL ontology
		 * @return numClasses an integer stating how many OWL classes the OWL ontology has
		 * @throws OWLOntologyCreationException An exception which describes an error during the creation of an ontology. If an ontology cannot be created then subclasses of this class will describe the reasons.
		 */
	public static int getNumClasses(File ontoFile) throws OWLOntologyCreationException {

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);
		int numClasses = onto.getClassesInSignature().size();

		manager.removeOntology(onto);

		return numClasses;
	}
	


	/**
	 * Returns an integer stating how many object properties an OWL ontology has
	 * @param ontoFile	the file path of the input OWL ontology
	 * @return numObjectProperties an integer stating number of object properties in an OWL ontology
	 * @throws OWLOntologyCreationException An exception which describes an error during the creation of an ontology. If an ontology cannot be created then subclasses of this class will describe the reasons.
	 */
	public static int getNumObjectProperties(File ontoFile) throws OWLOntologyCreationException {

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);
		int numObjectProperties = onto.getObjectPropertiesInSignature().size();

		manager.removeOntology(onto);

		return numObjectProperties;
	}
	
	public static int getNumDataProperties(File ontoFile) throws OWLOntologyCreationException {

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);
		int numDataProperties = onto.getDataPropertiesInSignature().size();

		manager.removeOntology(onto);

		return numDataProperties;
	}

	/**
	 * Returns an integer stating how many individuals an OWL ontology has
	 * @param ontoFile the file path of the input OWL ontology
	 * @return numIndividuals an integer stating number of individuals in an OWL ontology
	 * @throws OWLOntologyCreationException An exception which describes an error during the creation of an ontology. If an ontology cannot be created then subclasses of this class will describe the reasons.
	 */
	public static int getNumIndividuals(File ontoFile) throws OWLOntologyCreationException {

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);

		int numIndividuals = onto.getIndividualsInSignature().size();
		

		manager.removeOntology(onto);

		return numIndividuals;
	}

	/**
	 * Returns an integer stating how many subclasses reside in an OWL ontology.
	 * The method iterates over all classes in the OWL ontology and for each class
	 * counts how many subclasses the current class have. This count is updated for 
	 * each class being iterated. 
	 * @param ontoFile	the file path of the input OWL ontology
	 * @return totalSubClassCount an integer stating number of subclasses in an OWL ontology
	 * @throws OWLOntologyCreationException An exception which describes an error during the creation of an ontology. If an ontology cannot be created then subclasses of this class will describe the reasons.
	 */
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


	/**
	 * Returns an integer stating how many of the classes in an OWL ontology contains individuals (members)
	 * @param ontoFile	the file path of the input OWL ontology
	 * @return countClassesWithIndividuals an integer stating number of classes having individuals/members in an OWL ontology
	 * @throws OWLOntologyCreationException An exception which describes an error during the creation of an ontology. If an ontology cannot be created then subclasses of this class will describe the reasons.
	 */
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

	/**
	 * Returns an integer stating how many of the classes in an OWL ontology do not have comment annotations 
	 * associated with them
	 * @param ontoFile	the file path of the input OWL ontology
	 * @return numClassesWithoutComments an integer stating number of classes not having annotations associated with them
	 * @throws OWLOntologyCreationException An exception which describes an error during the creation of an ontology. If an ontology cannot be created then subclasses of this class will describe the reasons.
	 */
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
	
	/**
	 * Returns an integer stating how many of the classes in an OWL ontology do not have label annotations 
	 * associated with them
	 * @param ontoFile	the file path of the input OWL ontology
	 * @return numClassesWithoutLabels an integer stating number of classes not having label annotations associated with them
	 * @throws OWLOntologyCreationException An exception which describes an error during the creation of an ontology. If an ontology cannot be created then subclasses of this class will describe the reasons.
	 */
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

	/**
	 * Returns a double stating the percentage of how many classes and object properties
	 * are present as words in WordNet. For object properties their prefix (e.g. isA, hasA, etc.)
	 * is stripped so only their "stem" is retained. 
	 * @param ontoFile the file path of the input OWL ontology
	 * @return wordNetCoverage a double stating a percentage of how many of the classes and object properties are represented in WordNet
	 * @throws OWLOntologyCreationException An exception which describes an error during the creation of an ontology. If an ontology cannot be created then subclasses of this class will describe the reasons.
	 */
	public static double getWordNetCoverage(File ontoFile) throws OWLOntologyCreationException {
	
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();		
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);
		Iterator<OWLClass> itr = onto.getClassesInSignature().iterator();
		Iterator<OWLObjectProperty> itrOP = onto.getObjectPropertiesInSignature().iterator();
		
		String thisClass;
		String thisOP;
		
		int numClasses = onto.getClassesInSignature().size();
		int numOPs = onto.getObjectPropertiesInSignature().size();
		
		int classCounter = 0;
		int OPCounter = 0;
		
		while(itr.hasNext()) {
			thisClass = itr.next().getIRI().getFragment();
			if (WordNetLexicon.containedInWordNet(thisClass) == true) {
				classCounter++;			
			}		
		}
		
		while (itrOP.hasNext()) {
			thisOP = Preprocessor.stripOPPrefix(itrOP.next().getIRI().getFragment());
			System.out.println("Trying to find " + thisOP + " in WordNet");
			if (WordNetLexicon.containedInWordNet(thisOP) == true) {
				System.out.println(thisOP + " is in WordNet");	
				
			OPCounter++;
			}
			
		}

		double wordNetClassCoverage = ((double)classCounter / (double)numClasses);
		double wordNetOPCoverage = ((double)OPCounter / (double)numOPs);
		
		double wordNetCoverage = (wordNetClassCoverage + wordNetOPCoverage) / 2;
		
		return wordNetCoverage;	
	}
	
	/**
	 * Returns a boolean stating whether a term is considered a compound term (e.g. ElectronicBook)
	 * @param a the input string tested for being compound or not
	 * @return boolean stating whether the input string is a compound or not
	 */
	public static boolean isCompound(String a) {
		boolean test = false;

		String[] compounds = a.split("(?<=.)(?=\\p{Lu})");

			if (compounds.length > 1) {
				test = true;
			}

		return test;
	}
	

	/**
	 * Returns a count of how many classes are considered compound words in an ontology
	 * @param ontoFile the file path of the input OWL ontology
	 * @return numCompounds a double stating the percentage of how many of the classes in the ontology are compounds
	 * @throws OWLOntologyCreationException An exception which describes an error during the creation of an ontology. If an ontology cannot be created then subclasses of this class will describe the reasons.
	 */
	public static double getNumClassCompounds(File ontoFile) throws OWLOntologyCreationException {
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();		
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);
		
		Iterator<OWLClass> itr = onto.getClassesInSignature().iterator();
		
		String thisClass;
		
		int numClasses = onto.getClassesInSignature().size();
		
		int counter = 0;
		
		while(itr.hasNext()) {
			thisClass = Preprocessor.replaceUnderscore(itr.next().getIRI().getFragment());
			//Test
			//System.out.println("Testing " + thisClass.toString());
			if (isCompound(thisClass) == true) {
				counter++;			
				//Test
				//System.out.println("Now the counter is " + counter);
			}		
		}
		
		//double numCompounds = (counter / numClasses) * 100;
		double numCompounds = ((double)counter/(double)numClasses);
		
		return numCompounds;	
	}
	
	/**
	 * Returns a count of how many object properties are considered compound words in an ontology. For object properties we need to strip the prefix (e.g. is, has) to get to the "stem" of
	 * the property. This is taken care of by the Preprocessor.stripOPPrefix method.
	 * @param ontoFile the file path of the input OWL ontology
	 * @return numCompounds a double stating the percentage of how many of the object properties in the ontology are compounds
	 * @throws OWLOntologyCreationException An exception which describes an error during the creation of an ontology. If an ontology cannot be created then subclasses of this class will describe the reasons.
	 */
	public static double getNumPropertyCompounds(File ontoFile) throws OWLOntologyCreationException {
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();		
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);
		
		Iterator<OWLObjectProperty> itr = onto.getObjectPropertiesInSignature().iterator();
		
		String thisOP;

		int numOPs = onto.getObjectPropertiesInSignature().size();
		System.out.println("Number of object properties in total: " + numOPs);
		
		int counter = 0;
		
		while(itr.hasNext()) {
			thisOP = Preprocessor.replaceUnderscore(Preprocessor.stripOPPrefix(itr.next().getIRI().getFragment()));
			System.out.println("Printing object property including the replaceUnderscore method: " + thisOP);
			if (isCompound(thisOP) == true) {
				counter++;			

			}		
		}

		double numCompounds = ((double)counter/(double)numOPs);
		
		return numCompounds;	
	}
	
	//test method
	public static void main(String[] args) throws OWLOntologyCreationException, FileNotFoundException, JWNLException {
		
		/*File ontoFile = new File("/Users/audunvennesland/Documents/AIXM.owl");
		//File ontoFile = new File("./files/UoA/dbpedia.owl");
		OWLOntologyManager airm_manager = OWLManager.createOWLOntologyManager();		
		OWLOntology AIRM_onto = airm_manager.loadOntologyFromOntologyDocument(ontoFile);
		
		int numClasses = OWLLoader.getNumClasses(ontoFile);
		int numObjectProperties = OWLLoader.getNumObjectProperties(ontoFile);
		int numDataProperties = OWLLoader.getNumDataProperties(ontoFile);
		int numIndividuals = OWLLoader.getNumIndividuals(ontoFile);
		
		System.out.println("There are " + numClasses + " classes in the AIRM ontology");
		System.out.println("There are " + numObjectProperties + " object properties in the AIRM ontology");
		System.out.println("There are " + numDataProperties + " data properties in the AIRM ontology");
		System.out.println("There are " + numIndividuals + " individulas in the AIRM ontology");*/
		

		//import the owl files
		File ontoFile1 = new File("./files/UoA/TestTransportWithInstances1.owl");
		File ontoFile2 = new File("./files/UoA/TestTransportWithInstances2.owl");
		//File ontoFile = new File("/Users/audunvennesland/Documents/PhD/Ontologies/DBPedia/dbpedia_2014.owl");
		File ontoFile = new File("/Users/audunvennesland/Documents/PhD/Ontologies/OAEI/OAEI2015/Biblio/Biblio_2015.rdf");
		//File ontoFile = new File("/Users/audunvennesland/Documents/PhD/Ontologies/Schema.org/schema.rdf");
		
		
		
		//public static ArrayList<String> getDomainList(List<Long> offsetList)
		
		//create offset list public static List<Long> getOffsetList(File ontoFile)
		List<Long> offsetList = getOffsetList(ontoFile);
		
		ArrayList<String> domainList = getDomainList(offsetList);
		System.out.println("The size of the domainList is " + domainList.size());
		//System.out.println(domainList.get(index));
		
		
		//testing createDomainProfile
		//Map<String, Integer> domainMap = createDomainProfile(ontoFile1);
		

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();		
		OWLOntology onto1 = manager.loadOntologyFromOntologyDocument(ontoFile1);
		OWLOntology onto2 = manager.loadOntologyFromOntologyDocument(ontoFile2);
		
		
		
		//testing instance matcher
		/*Map<String, ArrayList<String>> instanceMap1 = new HashMap<String, ArrayList<String>>();
		Map<String, ArrayList<String>> instanceMap2 = new HashMap<String, ArrayList<String>>();
		
		Set<OWLClass> classes = onto1.getClassesInSignature();
		//System.out.println("There are " + classes.size() + " classes in this ontology");
		Iterator<OWLClass> clsItr = classes.iterator();

		//Make a string representation of the OWLClasses
		ArrayList<String> clsString = new ArrayList<String> ();
		while(clsItr.hasNext()) {
			clsString.add(clsItr.next().getIRI().getFragment());
		}
		
		ArrayList<String> instanceList = new ArrayList<String>();
		System.out.println("The size of the class list is " + clsString.size());
		
		for (int i = 0; i < clsString.size(); i++) {
			
			instanceList = getInstances(clsString.get(i), onto1);
			if (instanceList.size() > 0 && !clsString.get(i).equals("Thing")) {
				instanceMap1.put(clsString.get(i), instanceList);
			for (int j = 0; j < instanceList.size(); j++) {
				System.out.println(instanceList.get(j));
			}
			}

		}
		
		//iterate over the instanceMap
		for (Map.Entry<String,ArrayList<String>> entry : instanceMap1.entrySet()) {
			System.out.println(entry.getKey() + " --> " + entry.getValue());
			
		}	*/
		

		

		//manager.removeOntology(onto1);
		
/*		final File ontologyDir = new File("./files/OAEI-16-conference/ontologies");
		File[] filesInDir = null;
		filesInDir = ontologyDir.listFiles();
		
		for (int i = 0; i < filesInDir.length; i++) {
			System.out.println("-------------------------");
			System.out.println("Printing statistics for " + filesInDir[i].getPath().toString());
			System.out.println("Number of classes: " + OWLLoader.getNumClasses(filesInDir[i]));
			System.out.println("Number of sub-classes: " + OWLLoader.getNumSubClasses(filesInDir[i]));
			System.out.println("Number of object properties: " + OWLLoader.getNumObjectProperties(filesInDir[i]));
			//System.out.println("All object properties: ");
			
			System.out.println("-------------------------");
		}*/
		
		//File file2 = new File("/Users/audunvennesland/Documents/PhD/Development/Experiments/OAEIBIBLIO2BIBO/BIBO.owl");
		//File ontoFile = new File("/Users/audunvennesland/Documents/PhD/Ontologies/OAEI/OAEI2015/Biblio/Biblio_2015.rdf");
		/*System.out.println("-------------------------");
		System.out.println("Printing statistics for " + ontoFile.getPath().toString());
		System.out.println("Number of classes: " + OWLLoader.getNumClasses(ontoFile));
		System.out.println("Number of sub-classes: " + OWLLoader.getNumSubClasses(ontoFile));
		System.out.println("Number of object properties: " + OWLLoader.getNumObjectProperties(ontoFile));
		System.out.println("-------------------------");
		
		double WNC = getWordNetCoverage(ontoFile);
		System.out.println("The WordNet Coverage for BIBO is " + WNC);*/
		
		
	/*	OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);

		
		//ArrayList<String> getObjectProperties(OWLClass c)
		
		ArrayList<String> propsList = new ArrayList<String>();
		
		Set<OWLClass> classSet = onto.getClassesInSignature();
		Iterator<OWLClass> itr = classSet.iterator();
		System.out.println("Printing properties");
		while (itr.hasNext()) {
			OWLClass thisClass = itr.next();
			propsList.addAll(getObjectProperties(onto,thisClass));
			}
		
		for (int i = 0; i < propsList.size(); i++) {
			System.out.println(propsList.get(i));
			
		}*/
		
/*		int domains = getNumDomainAxioms(ontoFile);
		int ranges = getNumRangeAxioms(ontoFile);
		int objectProperties = getNumObjectProperties(ontoFile);
		
		System.out.println("Number of object properties: " + objectProperties);
		System.out.println("Number of domain classes: " + domains);
		System.out.println("Number of range classes: " + ranges);*/



	}


}
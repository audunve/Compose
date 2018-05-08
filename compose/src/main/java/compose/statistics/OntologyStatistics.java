package compose.statistics;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;

import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

import compose.misc.StringUtils;
import compose.wordnet.RiWordNetOperations;
import compose.wordnet.WNDomain;
import compose.wordnet.WordNetOperations;
import fr.inrialpes.exmo.ontosim.string.StringDistances;
import net.didion.jwnl.JWNLException;
import rita.RiWordNet;

/**
 * @author audunvennesland Date:02.02.2017
 * @version 1.0
 */
public class OntologyStatistics {

	/**
	 * An OWLOntologyManagermanages a set of ontologies. It is the main point
	 * for creating, loading and accessing ontologies.
	 */
	static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	/**
	 * The OWLReasonerFactory represents a reasoner creation point.
	 */
	static OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();

	/**
	 * A HashMap holding an OWLEntity as key and an ArrayList of instances
	 * associated with the OWLEntity
	 */
	private static HashMap<OWLEntity, ArrayList<String>> instanceMap = new HashMap<>();

	static StringDistances ontoString = new StringDistances();

	/**
	 * Default constructor
	 */
	public OntologyStatistics() {

	}

	/**
	 * Returns a Map holding a class as key and its superclass as value
	 * 
	 * @param o
	 *            the input OWL ontology from which classes and superclasses
	 *            should be derived
	 * @return classesAndSuperClasses a Map holding a class as key and its
	 *         superclass as value
	 * @throws OWLOntologyCreationException
	 *             An exception which describes an error during the creation of
	 *             an ontology. If an ontology cannot be created then subclasses
	 *             of this class will describe the reasons.
	 */
	public static Map<String, String> getClassesAndSuperClasses(OWLOntology o) throws OWLOntologyCreationException {

		OWLReasoner reasoner = reasonerFactory.createReasoner(o);
		Set<OWLClass> cls = o.getClassesInSignature();
		Map<String, String> classesAndSuperClasses = new HashMap<String, String>();
		ArrayList<OWLClass> classList = new ArrayList<OWLClass>();

		for (OWLClass i : cls) {
			classList.add(i);
		}

		// Iterate through the arraylist and for each class get the subclasses
		// belonging to it
		// Transform from OWLClass to String to simplify further processing...
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
	 * 
	 * @param owlClass
	 *            the OWL class from which instances should be retrieved
	 * @param ontology
	 *            the OWL ontology holding the owl class and its instances
	 * @return instanceList an ArrayList (String) of instances
	 */
	@SuppressWarnings("deprecation")
	public static ArrayList<String> getInstances(String owlClass, OWLOntology ontology) {
		ArrayList<String> instanceList = new ArrayList<String>();

		OWLReasonerFactory reasonerFactory = new PelletReasonerFactory();
		OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(ontology);

		for (OWLClass c : ontology.getClassesInSignature()) {

			if (c.getIRI().getFragment().equals(owlClass)) {
				// Test
				// System.out.println("Found the class " + owlClass);

				NodeSet<OWLNamedIndividual> instanceSet = reasoner.getInstances(c, false);
				Iterator<Node<OWLNamedIndividual>> itr = instanceSet.iterator();
				if (!itr.hasNext()) {
					// Test
					// System.out.println("There are no instances associated
					// with " + c.getIRI().getFragment());
					break;
				} else {

					for (OWLNamedIndividual i : instanceSet.getFlattened()) {
						// Test
						// System.out.println("Adding " +
						// i.getIRI().getFragment() + " to the list");
						instanceList.add(i.getIRI().getFragment());
					}
				}
			}
		}

		return instanceList;
	}

	/**
	 * Get number of classes in an ontology
	 * 
	 * @param ontoFile
	 *            the file path of the OWL ontology
	 * @return numClasses an integer stating how many OWL classes the OWL
	 *         ontology has
	 * @throws OWLOntologyCreationException
	 *             An exception which describes an error during the creation of
	 *             an ontology. If an ontology cannot be created then subclasses
	 *             of this class will describe the reasons.
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
	 * 
	 * @param ontoFile
	 *            the file path of the input OWL ontology
	 * @return numObjectProperties an integer stating number of object
	 *         properties in an OWL ontology
	 * @throws OWLOntologyCreationException
	 *             An exception which describes an error during the creation of
	 *             an ontology. If an ontology cannot be created then subclasses
	 *             of this class will describe the reasons.
	 */
	public static int getNumObjectProperties(File ontoFile) throws OWLOntologyCreationException {

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);
		int numObjectProperties = onto.getObjectPropertiesInSignature().size();

		manager.removeOntology(onto);

		return numObjectProperties;
	}

	/**
	 * Returns an integer stating how many individuals an OWL ontology has
	 * 
	 * @param ontoFile
	 *            the file path of the input OWL ontology
	 * @return numIndividuals an integer stating number of individuals in an OWL
	 *         ontology
	 * @throws OWLOntologyCreationException
	 *             An exception which describes an error during the creation of
	 *             an ontology. If an ontology cannot be created then subclasses
	 *             of this class will describe the reasons.
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
	 * The method iterates over all classes in the OWL ontology and for each
	 * class counts how many subclasses the current class have. This count is
	 * updated for each class being iterated.
	 * 
	 * @param ontoFile
	 *            the file path of the input OWL ontology
	 * @return totalSubClassCount an integer stating number of subclasses in an
	 *         OWL ontology
	 * @throws OWLOntologyCreationException
	 *             An exception which describes an error during the creation of
	 *             an ontology. If an ontology cannot be created then subclasses
	 *             of this class will describe the reasons.
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
	 * Returns an integer stating how many of the classes in an OWL ontology
	 * contains individuals (members)
	 * 
	 * @param ontoFile
	 *            the file path of the input OWL ontology
	 * @return countClassesWithIndividuals an integer stating number of classes
	 *         having individuals/members in an OWL ontology
	 * @throws OWLOntologyCreationException
	 *             An exception which describes an error during the creation of
	 *             an ontology. If an ontology cannot be created then subclasses
	 *             of this class will describe the reasons.
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
	 * Returns an integer stating how many of the classes in an OWL ontology do
	 * not have comment annotations associated with them
	 * 
	 * @param ontoFile
	 *            the file path of the input OWL ontology
	 * @return numClassesWithoutComments an integer stating number of classes
	 *         not having annotations associated with them
	 * @throws OWLOntologyCreationException
	 *             An exception which describes an error during the creation of
	 *             an ontology. If an ontology cannot be created then subclasses
	 *             of this class will describe the reasons.
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
	 * Returns an integer stating how many of the classes in an OWL ontology do
	 * not have label annotations associated with them
	 * 
	 * @param ontoFile
	 *            the file path of the input OWL ontology
	 * @return numClassesWithoutLabels an integer stating number of classes not
	 *         having label annotations associated with them
	 * @throws OWLOntologyCreationException
	 *             An exception which describes an error during the creation of
	 *             an ontology. If an ontology cannot be created then subclasses
	 *             of this class will describe the reasons.
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
	 * Returns a double stating the percentage of how many classes and object
	 * properties are present as words in WordNet. For object properties their
	 * prefix (e.g. isA, hasA, etc.) is stripped so only their "stem" is
	 * retained.
	 * 
	 * @param ontoFile
	 *            the file path of the input OWL ontology
	 * @return wordNetCoverage a double stating a percentage of how many of the
	 *         classes and object properties are represented in WordNet
	 * @throws OWLOntologyCreationException
	 *             An exception which describes an error during the creation of
	 *             an ontology. If an ontology cannot be created then subclasses
	 *             of this class will describe the reasons.
	 * @throws JWNLException 
	 * @throws FileNotFoundException 
	 */
	public static double getWordNetCoverage(File ontoFile) throws OWLOntologyCreationException, FileNotFoundException, JWNLException {

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);
		Iterator<OWLClass> itr = onto.getClassesInSignature().iterator();
		Iterator<OWLObjectProperty> itrOP = onto.getObjectPropertiesInSignature().iterator();

		String thisClass;
		String thisOP;

		int numClasses = onto.getClassesInSignature().size();
		//int numOPs = onto.getObjectPropertiesInSignature().size();

		int classCounter = 0;
		int OPCounter = 0;

		while (itr.hasNext()) {
			thisClass = itr.next().getIRI().getFragment();
			if (WordNetOperations.containedInWordNet(thisClass) == true) {
				classCounter++;
			}
		}

		//17.08.2017: Disabling WC for object properties
		/*while (itrOP.hasNext()) {
			thisOP = StringUtils.stripPrefix(itrOP.next().getIRI().getFragment());
			System.out.println("Trying to find " + thisOP + " in WordNet");
			if (JWNLOperations.containedInWordNet(thisOP) == true) {
				System.out.println(thisOP + " is in WordNet");

				OPCounter++;
			}

		}*/

		//double wordNetClassCoverage = ((double) classCounter / (double) numClasses);
		double wordNetCoverage = ((double) classCounter / (double) numClasses);
		//double wordNetOPCoverage = ((double) OPCounter / (double) numOPs);

		//double wordNetCoverage = (wordNetClassCoverage + wordNetOPCoverage) / 2;

		return wordNetCoverage;
	}

	/**
	 * Returns the average number of hyponyms in WordNet for each class in an
	 * ontology
	 * 
	 * @param ontoFile:
	 *            an ontology file
	 * @return the average number of hyponyms per class in an ontology
	 * @throws OWLOntologyCreationException
	 */
	public static double getHyponymRichness(File ontoFile) throws OWLOntologyCreationException {

		RiWordNet database = new RiWordNet("/Users/audunvennesland/Documents/PhD/Development/WordNet/WordNet-3.0/dict");

		double hyponymRichness = 0;

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);

		Set<OWLClass> classes = onto.getClassesInSignature();

		for (OWLClass cl : classes) {
			String[] hyponyms = RiWordNetOperations
					.getHyponyms(StringUtils.stringTokenize(cl.getIRI().getFragment(), true));

			int numHyponyms = hyponyms.length;

			hyponymRichness += numHyponyms;
		}

		return (double) hyponymRichness / classes.size();
	}

	/**
	 * Returns the average number of synonyms in WordNet for each class in an
	 * ontology
	 * 
	 * @param ontoFile:
	 *            an ontology file
	 * @return the average number of synonyms per class in an ontology
	 * @throws OWLOntologyCreationException
	 */
	public static double getSynonymRichness(File ontoFile) throws OWLOntologyCreationException {

		RiWordNet database = new RiWordNet("/Users/audunvennesland/Documents/PhD/Development/WordNet/WordNet-3.0/dict");

		double synonymRichness = 0;

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);

		Set<OWLClass> classes = onto.getClassesInSignature();

		for (OWLClass cl : classes) {
			String[] synonyms = RiWordNetOperations
					.getSynonyms(StringUtils.stringTokenize(cl.getIRI().getFragment(), true));

			int numSynonyms = synonyms.length;

			synonymRichness += numSynonyms;
		}

		return (double) synonymRichness / classes.size();
	}
	
	/**
	 * Returns a measure stating how many WordNet domains a single ontology is
	 * associated with averaged by the number of classes in the ontology
	 * 
	 * @param ontoFile1:
	 *            an ontology file
	 * @param ontoFile2:
	 *            an ontology file
	 * @return a measure on how many WordNet domains two ontologies are
	 *         associated with (averaged by the total number of classes)
	 * @throws OWLOntologyCreationException
	 * @throws FileNotFoundException
	 * @throws JWNLException
	 */
	public static double domainDiversity(File ontoFile)
			throws OWLOntologyCreationException, FileNotFoundException, JWNLException {

		double domainDiversity = 0;

		ArrayList<String> domainsOnto1 = WNDomain.getDomainsFromFile(ontoFile);

		Set<String> allDomains = new HashSet<String>();
		allDomains.addAll(domainsOnto1);


		int domainsSize = allDomains.size();
		//System.out.println("Number of domains: " + domainsSize);

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto1 = manager.loadOntologyFromOntologyDocument(ontoFile);

		int classSize = onto1.getClassesInSignature().size();
		//System.out.println("Number of classes: " + classSize);

		domainDiversity = (double) domainsSize / (double) classSize;

		return domainDiversity;
	}

	/**
	 * Returns a measure stating how many WordNet domains two ontologies are
	 * associated with averaged by the number of classes in the ontologies
	 * 
	 * @param ontoFile1:
	 *            an ontology file
	 * @param ontoFile2:
	 *            an ontology file
	 * @return a measure on how many WordNet domains two ontologies are
	 *         associated with (averaged by the total number of classes)
	 * @throws OWLOntologyCreationException
	 * @throws FileNotFoundException
	 * @throws JWNLException
	 */
	public static double domainDiversity(File ontoFile1, File ontoFile2)
			throws OWLOntologyCreationException, FileNotFoundException, JWNLException {

		double domainDiversity = 0;

		ArrayList<String> domainsOnto1 = WNDomain.getDomainsFromFile(ontoFile1);
		ArrayList<String> domainsOnto2 = WNDomain.getDomainsFromFile(ontoFile2);

		Set<String> allDomains = new HashSet<String>();
		allDomains.addAll(domainsOnto1);
		allDomains.addAll(domainsOnto2);

		int domainsSize = allDomains.size();
		System.out.println("Number of domains: " + domainsSize);

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto1 = manager.loadOntologyFromOntologyDocument(ontoFile1);
		OWLOntology onto2 = manager.loadOntologyFromOntologyDocument(ontoFile2);

		int classSize = onto1.getClassesInSignature().size() + onto2.getClassesInSignature().size();
		System.out.println("Number of classes: " + classSize);

		domainDiversity = (double) domainsSize / (double) classSize;

		return domainDiversity;
	}

	/**
	 * Returns a boolean stating whether a term is considered a compound term
	 * (e.g. ElectronicBook)
	 * 
	 * @param a
	 *            the input string tested for being compound or not
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
	 * Returns a count of how many classes are considered compound words in an
	 * ontology
	 * 
	 * @param ontoFile
	 *            the file path of the input OWL ontology
	 * @return numCompounds a double stating the percentage of how many of the
	 *         classes in the ontology are compounds
	 * @throws OWLOntologyCreationException
	 *             An exception which describes an error during the creation of
	 *             an ontology. If an ontology cannot be created then subclasses
	 *             of this class will describe the reasons.
	 */
	public static double getNumClassCompounds(File ontoFile) throws OWLOntologyCreationException {

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);

		Iterator<OWLClass> itr = onto.getClassesInSignature().iterator();

		String thisClass;

		int numClasses = onto.getClassesInSignature().size();

		int counter = 0;

		while (itr.hasNext()) {
			thisClass = StringUtils.replaceUnderscore(itr.next().getIRI().getFragment());

			if (isCompound(thisClass) == true) {
				counter++;

			}
		}

		double numCompounds = ((double) counter / (double) numClasses);

		return numCompounds;
	}

	/**
	 * Returns how many characters the most common substring among two
	 * ontologies have
	 * 
	 * @param ontoFile1:
	 *            an ontology file
	 * @param ontoFile2:
	 *            an ontology file
	 * @return an integer stating how many characters the most common substring
	 *         among two ontologies consists of
	 * @throws OWLOntologyCreationException
	 */
	private static int mostCommonSubstringLength(File ontoFile1, File ontoFile2) throws OWLOntologyCreationException {

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto1 = manager.loadOntologyFromOntologyDocument(ontoFile1);
		OWLOntology onto2 = manager.loadOntologyFromOntologyDocument(ontoFile2);

		Set<OWLClass> onto1Classes = onto1.getClassesInSignature();
		Set<OWLClass> onto2Classes = onto2.getClassesInSignature();

		List<Integer> commonSubstringLengths = new ArrayList<Integer>();

		for (OWLClass cl1 : onto1Classes) {
			for (OWLClass cl2 : onto2Classes) {
				Set<String> result = longestCommonSubstrings(cl1.getIRI().getFragment(), cl2.getIRI().getFragment());
				for (String s : result) {
					int length = s.length();
					commonSubstringLengths.add(length);
				}
			}
		}

		int mostCommonSubstringLength = mostCommon(commonSubstringLengths);

		return mostCommonSubstringLength;

	}

	/**
	 * Counts the most common strings in a list
	 * 
	 * @param list:
	 *            a list of strings
	 * @return number of characters in the most represented string in a list
	 */
	private static <T> T mostCommon(List<T> list) {
		Map<T, Integer> map = new HashMap<>();

		for (T t : list) {
			Integer val = map.get(t);
			map.put(t, val == null ? 1 : val + 1);
		}

		Entry<T, Integer> max = null;

		for (Entry<T, Integer> e : map.entrySet()) {
			if (max == null || e.getValue() > max.getValue())
				max = e;
		}

		return max.getKey();
	}

	/**
	 * Returns a count of the number of common substring in two ontologies
	 * 
	 * @param ontoFile1:
	 *            an ontology file
	 * @param ontoFile2:
	 *            an ontology file
	 * @return an integer stating how many common substrings exist in two
	 *         ontologies
	 * @throws OWLOntologyCreationException
	 */
	private static int numCommonSubStrings(File ontoFile1, File ontoFile2) throws OWLOntologyCreationException {

		int commonSubStrings = 0;

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto1 = manager.loadOntologyFromOntologyDocument(ontoFile1);
		OWLOntology onto2 = manager.loadOntologyFromOntologyDocument(ontoFile2);

		Set<OWLClass> onto1Classes = onto1.getClassesInSignature();
		Set<OWLClass> onto2Classes = onto2.getClassesInSignature();

		for (OWLClass cl1 : onto1Classes) {
			for (OWLClass cl2 : onto2Classes) {
				Set<String> result = longestCommonSubstrings(cl1.getIRI().getFragment(), cl2.getIRI().getFragment());

				if (!result.isEmpty()) {
					System.out.println("We have a substring in " + cl1.getIRI().getFragment() + " and "
							+ cl2.getIRI().getFragment());

					for (String s : result) {
						System.out.println(s);
					}

					commonSubStrings++;
				}

			}
		}

		System.out.println("Number of longest common substrings are " + commonSubStrings
				+ " and number of classes in the ontology are " + (onto1Classes.size() + onto2Classes.size()));
		return commonSubStrings;

	}

	/**
	 * Returns a set of common substrings (after having compared character by
	 * character)
	 * 
	 * @param s:
	 *            an input string
	 * @param t:
	 *            an input string
	 * @return a set of common substrings among the input strings
	 */
	private static Set<String> longestCommonSubstrings(String s, String t) {
		int[][] table = new int[s.length()][t.length()];
		int longest = 5;
		Set<String> result = new HashSet<>();

		for (int i = 0; i < s.length(); i++) {
			for (int j = 0; j < t.length(); j++) {
				if (s.charAt(i) != t.charAt(j)) {
					continue;
				}

				table[i][j] = (i == 0 || j == 0) ? 1 : 1 + table[i - 1][j - 1];
				if (table[i][j] > longest) {
					longest = table[i][j];
					result.clear();
				}
				if (table[i][j] == longest) {
					result.add(s.substring(i - longest + 1, i + 1));
				}
			}
		}
		return result;
	}

	/**
	 * Returns a measure of how many common substrings two ontologies have
	 * averaged by the total number of classes in the ontologies
	 * 
	 * @param ontoFile1:
	 *            an ontology file
	 * @param ontoFile2:
	 *            an ontology file
	 * @return a double of how many common substrings divided by the total
	 *         number of classes in the ontologies
	 * @throws OWLOntologyCreationException
	 */
	public static double commonSubstringRatio(File ontoFile1, File ontoFile2) throws OWLOntologyCreationException {

		int numClasses = getNumClasses(ontoFile1) + getNumClasses(ontoFile2);
		int numCommonSubstrings = numCommonSubStrings(ontoFile1, ontoFile2);

		int commonSubstringRatio = numCommonSubstrings / numClasses;

		return (double) commonSubstringRatio;

	}

	// *** Methods not in use ***

	/*	*//**
			 * Returns a count of how many object properties are considered
			 * compound words in an ontology. For object properties we need to
			 * strip the prefix (e.g. is, has) to get to the "stem" of the
			 * property. This is taken care of by the StringUtils.stripOPPrefix
			 * method.
			 * 
			 * @param ontoFile
			 *            the file path of the input OWL ontology
			 * @return numCompounds a double stating the percentage of how many
			 *         of the object properties in the ontology are compounds
			 * @throws OWLOntologyCreationException
			 *             An exception which describes an error during the
			 *             creation of an ontology. If an ontology cannot be
			 *             created then subclasses of this class will describe
			 *             the reasons.
			 *//*
			 * public static double getNumPropertyCompounds(File ontoFile)
			 * throws OWLOntologyCreationException {
			 * 
			 * OWLOntologyManager manager =
			 * OWLManager.createOWLOntologyManager(); OWLOntology onto =
			 * manager.loadOntologyFromOntologyDocument(ontoFile);
			 * 
			 * Iterator<OWLObjectProperty> itr =
			 * onto.getObjectPropertiesInSignature().iterator();
			 * 
			 * String thisOP;
			 * 
			 * int numOPs = onto.getObjectPropertiesInSignature().size();
			 * System.out.println("Number of object properties in total: " +
			 * numOPs);
			 * 
			 * int counter = 0;
			 * 
			 * while(itr.hasNext()) { thisOP =
			 * StringUtils.replaceUnderscore(StringUtils.stripPrefix(itr.next().
			 * getIRI().getFragment())); //System.out.println(
			 * "Printing object property including the replaceUnderscore method: "
			 * + thisOP); if (isCompound(thisOP) == true) { counter++;
			 * 
			 * } }
			 * 
			 * double numCompounds = ((double)counter/(double)numOPs);
			 * 
			 * return numCompounds; }
			 */

	/*
	 * public static List<Long> getOffsetList(File ontoFile) throws
	 * OWLOntologyCreationException, FileNotFoundException, JWNLException {
	 * List<Long> offsetList = new ArrayList<Long>();
	 * 
	 * //get a list of all class names in ontology ArrayList<String>
	 * classNameList = createClassList(ontoFile); System.out.println(
	 * "The number of classes in classNameList is " + classNameList.size());
	 * 
	 * //for every class name, get its domain by using the synset offset of the
	 * class as query for (String s : classNameList) { //get all offsets
	 * associated with s offsetList = WNDomain.findSynsetOffset(s); }
	 * 
	 * return offsetList; }
	 */

	/*
	 * public static ArrayList<String> getDomainList(List<Long> offsetList)
	 * throws FileNotFoundException { String wnDomainsFile =
	 * "./files/wndomains/wn-domains-3.2-20070223.txt";
	 * 
	 * ArrayList<String> domainList = new ArrayList<String>();
	 * 
	 * String thisOffset = null; String thisDomain = null;
	 * 
	 * for (Long l : offsetList) { thisOffset = l.toString(); thisDomain =
	 * WNDomain.findDomain(wnDomainsFile, thisOffset);
	 * domainList.add(thisDomain); }
	 * 
	 * return domainList; }
	 */

	/**
	 * Returns a Map<String, Integer> holding the domains included in an input
	 * ontology as key and a computed coverage (number of occurrences / total
	 * number of class names in the ontology)
	 * 
	 * @param ontoFile
	 * @return a Map<[domain], [coverage]>
	 * @throws OWLOntologyCreationException
	 * @throws JWNLException
	 * @throws FileNotFoundException
	 *//*
		 * public static Map<String, List<Long>> createOffsetMap(File ontoFile)
		 * throws OWLOntologyCreationException, FileNotFoundException,
		 * JWNLException { Map<String, List<Long>> offsetMap = new
		 * HashMap<String, List<Long>>();
		 * 
		 * List<Long> offsetList = new ArrayList<Long>();
		 * 
		 * //get a list of all class names in ontology ArrayList<String>
		 * classNameList = createClassList(ontoFile);
		 * 
		 * //for every class name, get its domain by using the synset offset of
		 * the class as query for (String s : classNameList) { //get all offsets
		 * associated with s offsetList = WNDomain.findSynsetOffset(s); //put
		 * class name s as key and the list of offsets associated with s as
		 * value in the Map offsetMap.put(s, offsetList); }
		 * 
		 * return offsetMap; }
		 */

	/*
	 * public static Map<String, ArrayList<String>> createDomainMap(Map<String,
	 * List<Long>> offsetMap) { Map<String, ArrayList<String>> domainMap = new
	 * HashMap<String, ArrayList<String>>(); ArrayList<String> domains = new
	 * ArrayList<String>(); List<Long> offsetList = new ArrayList<Long>(); Long
	 * l = null;
	 * 
	 * //create an ArrayList of strings for the domain for every offset
	 * //iterate through each entry of the offsetMap String className = null;
	 * for (Map.Entry<String,List<Long>> entry : offsetMap.entrySet()) {
	 * className = entry.getKey(); offsetList = entry.getValue(); for (Long o :
	 * offsetList) { domains.add(o.toString()); }
	 * 
	 * System.out.println("Putting " + className + " to the domainMap");
	 * domainMap.put(className, domains);
	 * 
	 * }
	 * 
	 * return domainMap; }
	 */

	/* *//**
			 * Returns an arraylist of class names in an ontology
			 * 
			 * @param ontoFile
			 *            The file path to the owl file
			 * @return an ArrayList<String> holding the class names of the input
			 *         ontology
			 * @throws OWLOntologyCreationException
			 *//*
			 * private static ArrayList<String> createClassList(File ontoFile)
			 * throws OWLOntologyCreationException { ArrayList<String> classList
			 * = new ArrayList<String>();
			 * 
			 * OWLOntologyManager manager =
			 * OWLManager.createOWLOntologyManager(); OWLOntology onto =
			 * manager.loadOntologyFromOntologyDocument(ontoFile);
			 * 
			 * Set<OWLClass> clsSet = onto.getClassesInSignature();
			 * //Iterator<OWLClass> itr = clsSet.iterator(); String thisClass =
			 * null;
			 * 
			 * for (OWLClass cl : clsSet) { thisClass =
			 * cl.getIRI().getFragment(); System.out.println("The class is " +
			 * thisClass); classList.add(StringUtils.stringTokenize(thisClass,
			 * true)); System.out.println("Adding " + thisClass +
			 * " to the ArrayList"); System.out.println(
			 * "The number of class names in classList is " + classList.size());
			 * }
			 * 
			 * return classList; }
			 */

	/*
	 * public static int getNumDataProperties(File ontoFile) throws
	 * OWLOntologyCreationException {
	 * 
	 * OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	 * OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);
	 * int numDataProperties = onto.getDataPropertiesInSignature().size();
	 * 
	 * manager.removeOntology(onto);
	 * 
	 * return numDataProperties; }
	 */

	// test method
	public static void main(String[] args) throws OWLOntologyCreationException, FileNotFoundException, JWNLException {

		/*
		 * File ontoFile = new
		 * File("/Users/audunvennesland/Documents/AIXM.owl"); //File ontoFile =
		 * new File("./files/UoA/dbpedia.owl"); OWLOntologyManager airm_manager
		 * = OWLManager.createOWLOntologyManager(); OWLOntology AIRM_onto =
		 * airm_manager.loadOntologyFromOntologyDocument(ontoFile);
		 * 
		 * int numClasses = OntologyStatistics.getNumClasses(ontoFile); int
		 * numObjectProperties =
		 * OntologyStatistics.getNumObjectProperties(ontoFile); int
		 * numDataProperties =
		 * OntologyStatistics.getNumDataProperties(ontoFile); int numIndividuals
		 * = OntologyStatistics.getNumIndividuals(ontoFile);
		 * 
		 * System.out.println("There are " + numClasses +
		 * " classes in the AIRM ontology"); System.out.println("There are " +
		 * numObjectProperties + " object properties in the AIRM ontology");
		 * System.out.println("There are " + numDataProperties +
		 * " data properties in the AIRM ontology"); System.out.println(
		 * "There are " + numIndividuals + " individulas in the AIRM ontology");
		 */

		// import the owl files
		// File ontoFile1 = new
		// File("./files/OAEI-16-conference/ontologies/conference.owl");
		// File ontoFile2 = new
		// File("./files/OAEI-16-conference/ontologies/ekaw.owl");
		// File ontoFile1 = new
		// File("./files/OAEI-16-conference/ontologies/cmt.owl");
		// File ontoFile2 = new
		// File("./files/OAEI-16-conference/ontologies/confOf.owl");
		File ontoFile1 = new File("./files/ontologies/Biblio_2015.rdf");
		File ontoFile2 = new File("./files/ontologies/BIBO.owl");
		// File ontoFile1 = new
		// File("./files/UoA/TestTransportWithInstances1.owl");
		// File ontoFile2 = new
		// File("./files/UoA/TestTransportWithInstances2.owl");
		// File ontoFile = new
		// File("/Users/audunvennesland/Documents/PhD/Ontologies/DBPedia/dbpedia_2014.owl");
		// File ontoFile1 = new
		// File("/Users/audunvennesland/Documents/PhD/Ontologies/OAEI/OAEI2015/Biblio/Biblio_2015.rdf");
		// File ontoFile2 = new
		// File("/Users/audunvennesland/Documents/PhD/Ontologies/Schema.org/schema.rdf");

		// ******TESTING CommonSubString*********
		int comSubStrings = numCommonSubStrings(ontoFile1, ontoFile2);
		System.out.println("Number of common substrings: " + comSubStrings);

		System.out.println("The most common substring length is " + mostCommonSubstringLength(ontoFile1, ontoFile2));

		System.out.println("The common substring ratio is " + commonSubstringRatio(ontoFile1, ontoFile2));

		System.out.println("The Hyponym Richness for ontology 1 is " + getHyponymRichness(ontoFile1));
		System.out.println("The Hyponym Richness for ontology 2 is " + getHyponymRichness(ontoFile2));

		System.out.println("The Domain Diversity is: " + domainDiversity(ontoFile1, ontoFile2));

		// public static ArrayList<String> getDomainList(List<Long> offsetList)

		// create offset list public static List<Long> getOffsetList(File
		// ontoFile)
		// List<Long> offsetList = getOffsetList(ontoFile);

		// ArrayList<String> domainList = getDomainList(offsetList);
		// System.out.println("The size of the domainList is " +
		// domainList.size());
		// System.out.println(domainList.get(index));

		// testing createDomainProfile
		// Map<String, Integer> domainMap = createDomainProfile(ontoFile1);

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto1 = manager.loadOntologyFromOntologyDocument(ontoFile1);
		OWLOntology onto2 = manager.loadOntologyFromOntologyDocument(ontoFile2);

		// ********TESTING instance matcher*********
		/*
		 * Map<String, ArrayList<String>> instanceMap1 = new HashMap<String,
		 * ArrayList<String>>(); Map<String, ArrayList<String>> instanceMap2 =
		 * new HashMap<String, ArrayList<String>>();
		 * 
		 * Set<OWLClass> classes = onto1.getClassesInSignature();
		 * //System.out.println("There are " + classes.size() +
		 * " classes in this ontology"); Iterator<OWLClass> clsItr =
		 * classes.iterator();
		 * 
		 * //Make a string representation of the OWLClasses ArrayList<String>
		 * clsString = new ArrayList<String> (); while(clsItr.hasNext()) {
		 * clsString.add(clsItr.next().getIRI().getFragment()); }
		 * 
		 * ArrayList<String> instanceList = new ArrayList<String>();
		 * System.out.println("The size of the class list is " +
		 * clsString.size());
		 * 
		 * for (int i = 0; i < clsString.size(); i++) {
		 * 
		 * instanceList = getInstances(clsString.get(i), onto1); if
		 * (instanceList.size() > 0 && !clsString.get(i).equals("Thing")) {
		 * instanceMap1.put(clsString.get(i), instanceList); for (int j = 0; j <
		 * instanceList.size(); j++) { System.out.println(instanceList.get(j));
		 * } }
		 * 
		 * }
		 * 
		 * //iterate over the instanceMap for
		 * (Map.Entry<String,ArrayList<String>> entry : instanceMap1.entrySet())
		 * { System.out.println(entry.getKey() + " --> " + entry.getValue());
		 * 
		 * }
		 */

		// manager.removeOntology(onto1);

		/*
		 * final File ontologyDir = new
		 * File("./files/OAEI-16-conference/ontologies"); File[] filesInDir =
		 * null; filesInDir = ontologyDir.listFiles();
		 * 
		 * for (int i = 0; i < filesInDir.length; i++) {
		 * System.out.println("-------------------------"); System.out.println(
		 * "Printing statistics for " + filesInDir[i].getPath().toString());
		 * System.out.println("Number of classes: " +
		 * OntologyStatistics.getNumClasses(filesInDir[i])); System.out.println(
		 * "Number of sub-classes: " +
		 * OntologyStatistics.getNumSubClasses(filesInDir[i]));
		 * System.out.println("Number of object properties: " +
		 * OntologyStatistics.getNumObjectProperties(filesInDir[i]));
		 * //System.out.println("All object properties: ");
		 * 
		 * System.out.println("-------------------------"); }
		 */

		// File file2 = new
		// File("/Users/audunvennesland/Documents/PhD/Development/Experiments/OAEIBIBLIO2BIBO/BIBO.owl");
		// File ontoFile = new
		// File("/Users/audunvennesland/Documents/PhD/Ontologies/OAEI/OAEI2015/Biblio/Biblio_2015.rdf");
		/*
		 * System.out.println("-------------------------"); System.out.println(
		 * "Printing statistics for " + ontoFile.getPath().toString());
		 * System.out.println("Number of classes: " +
		 * OntologyStatistics.getNumClasses(ontoFile)); System.out.println(
		 * "Number of sub-classes: " +
		 * OntologyStatistics.getNumSubClasses(ontoFile)); System.out.println(
		 * "Number of object properties: " +
		 * OntologyStatistics.getNumObjectProperties(ontoFile));
		 * System.out.println("-------------------------");
		 * 
		 * double WNC = getWordNetCoverage(ontoFile); System.out.println(
		 * "The WordNet Coverage for BIBO is " + WNC);
		 */

		/*
		 * OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		 * OWLOntology onto =
		 * manager.loadOntologyFromOntologyDocument(ontoFile);
		 * 
		 * 
		 * //ArrayList<String> getObjectProperties(OWLClass c)
		 * 
		 * ArrayList<String> propsList = new ArrayList<String>();
		 * 
		 * Set<OWLClass> classSet = onto.getClassesInSignature();
		 * Iterator<OWLClass> itr = classSet.iterator(); System.out.println(
		 * "Printing properties"); while (itr.hasNext()) { OWLClass thisClass =
		 * itr.next(); propsList.addAll(getObjectProperties(onto,thisClass)); }
		 * 
		 * for (int i = 0; i < propsList.size(); i++) {
		 * System.out.println(propsList.get(i));
		 * 
		 * }
		 */

		/*
		 * int domains = getNumDomainAxioms(ontoFile); int ranges =
		 * getNumRangeAxioms(ontoFile); int objectProperties =
		 * getNumObjectProperties(ontoFile);
		 * 
		 * System.out.println("Number of object properties: " +
		 * objectProperties); System.out.println("Number of domain classes: " +
		 * domains); System.out.println("Number of range classes: " + ranges);
		 */

	}

}
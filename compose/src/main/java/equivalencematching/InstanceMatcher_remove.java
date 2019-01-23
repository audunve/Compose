package equivalencematching;

/*import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;*/

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Relation;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.xml.sax.ContentHandler;

import compose.statistics_delete.OntologyStatistics_deleted;
import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.ontowrap.HeavyLoadedOntology;
import fr.inrialpes.exmo.ontowrap.OntologyFactory;
import fr.inrialpes.exmo.ontowrap.OntowrapException;
import fr.inrialpes.exmo.ontowrap.owlapi30.OWLAPI3Ontology;
import utilities.ISub;

/**
 * A very basic instance matcher that takes two ontologies as input and compares instances for each class using the ISub similarity matching algorithm. 
 * @author audunvennesland
 * 13. feb. 2017 
 */
public class InstanceMatcher_remove extends ObjectAlignment implements AlignmentProcess {

	static ISub isubMatcher = new ISub();

	public void align(Alignment alignment, Properties param) throws AlignmentException {

		try {
			// Match classes
			for ( Object cl2: ontology2().getClasses() ){
				for ( Object cl1: ontology1().getClasses() ){

					// add mapping into alignment object 
					addAlignCell(cl1,cl2, "=", matchInstances(cl1,cl2));  
				}

			}

		} catch (Exception e) { e.printStackTrace(); }
	}

	/*	OntologyFactory.setDefaultFactory("fr.inrialpes.exmo.ontowrap.owlapi30.OWLAPI3OntologyFactory");
	URI u = new URI("file:examples/rdf/edu.umbc.ebiquity.publication.owl");
	ontology = OntologyFactory.getFactory().loadOntology(u);
	assertNotNull( ontology );
	assertTrue( ontology instanceof OWLAPI3Ontology );
	HeavyLoadedOntology onto = (HeavyLoadedOntology)ontology;*/

	public static double matchInstances(Object o1, Object o2) throws OWLOntologyCreationException, OntowrapException {

		//get the objects (entities)
		String s1 = ontology1().getEntityName(o1).toLowerCase();
		String s2 = ontology2().getEntityName(o2).toLowerCase();

		URI ontologyURI1 = ontology1().getFile();
		URI ontologyURI2 = ontology2().getFile();

		onto1 = OntologyFactory.getFactory().loadOntology(ontologyURI1);
		onto2 = OntologyFactory.getFactory().loadOntology(ontologyURI2);

		HeavyLoadedOntology heavyOnto1 = (HeavyLoadedOntology)onto1;
		HeavyLoadedOntology heavyOnto2 = (HeavyLoadedOntology)onto2;

		Set onto1Cls = heavyOnto1.getClasses();
		Set onto2Cls = heavyOnto2.getClasses();

		Iterator onto1ClsIt = onto1Cls.iterator();
		Iterator onto2ClsIt = onto2Cls.iterator();

		//Make a string representation of the OWLClasses for onto1
		ArrayList<String> listClassesOnto1 = new ArrayList<String> ();
		while(onto1ClsIt.hasNext()) {
			listClassesOnto1.add(onto1ClsIt.next().toString());
		}

		//Make a string representation of the OWLClasses for onto1
		ArrayList<String> listClassesOnto2 = new ArrayList<String> ();
		while(onto2ClsIt.hasNext()) {
			listClassesOnto2.add(onto2ClsIt.next().toString());
		}

		ArrayList<String> instanceListClasses1 = new ArrayList<String>();
		ArrayList<String> instanceListClasses2 = new ArrayList<String>();

		Map<String, ArrayList<String>> instanceMap1 = new HashMap<String, ArrayList<String>>();
		Map<String, ArrayList<String>> instanceMap2 = new HashMap<String, ArrayList<String>>();

		for (int i = 0; i < listClassesOnto1.size(); i++) {
			
			//instanceListClasses1 = heavyOnto1.getInstances(arg0, arg1, arg2, arg3)

			//instanceListClasses1 = OntologyStatistics.getInstances(listClassesOnto1.get(i), onto1);
			if (instanceListClasses1.size() > 0 && !listClassesOnto1.get(i).equals("Thing")) {
				instanceMap1.put(listClassesOnto1.get(i), instanceListClasses1);
				for (int j = 0; j < instanceListClasses1.size(); j++) {
				}
			}
		}

		for (int i = 0; i < listClassesOnto2.size(); i++) {

			//instanceListClasses2 = OntologyStatistics.getInstances(listClassesOnto2.get(i), onto2);
			if (instanceListClasses2.size() > 0 && !listClassesOnto2.get(i).equals("Thing")) {
				instanceMap2.put(listClassesOnto2.get(i), instanceListClasses2);
				for (int j = 0; j < instanceListClasses2.size(); j++) {
				}
			}
		}

		System.out.println("Instances ontology 1");
		for (Map.Entry<String,ArrayList<String>> entry1 : instanceMap1.entrySet()) {
			System.out.println(entry1.getKey() + " --> " + entry1.getValue());

		}

		System.out.println("Instances ontology 2");
		for (Map.Entry<String,ArrayList<String>> entry2 : instanceMap2.entrySet()) {
			System.out.println(entry2.getKey() + " --> " + entry2.getValue());

		}

		//match the instance (values) of each instance map using ISub and if above a threshold
		//output the equivalent classes
		for (Map.Entry<String, ArrayList<String>> entry01 : instanceMap1.entrySet()) {
			for (Map.Entry<String, ArrayList<String>> entry02 : instanceMap2.entrySet()) {
				ArrayList<String> entry01Values = entry01.getValue();
				ArrayList<String> entry02Values = entry02.getValue();
				for (String entry01Instance : entry01Values) {
					for (String entry02Instance : entry02Values) {
						double score = isubMatcher.score(entry01Instance, entry02Instance);
						if (score > 0.8) {
							System.out.println(entry01.getKey() + " and " + entry02.getKey() + " have similar instances (" + entry01Instance + " and " + entry02Instance + " with score " + score + ")");
						}
					}
				}
			}
		}




		double instanceScore = 0;

		return instanceScore;
	}

	/*public double matchInstances(Object o1, Object o2) throws OWLOntologyCreationException {

		//get the objects (entities)
		String s1 = ontology1().getEntityName(o1).toLowerCase();
		String s2 = ontology2().getEntityName(o2).toLowerCase();


		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();		
		OWLOntology onto1 = manager.loadOntologyFromOntologyDocument(ontoFile1);
		OWLOntology onto2 = manager.loadOntologyFromOntologyDocument(ontoFile2);

		Set<OWLClass> onto1Classes = onto1.getClassesInSignature();
		Set<OWLClass> onto2Classes = onto2.getClassesInSignature();

		Iterator<OWLClass> onto1ClassesIterator = onto1Classes.iterator();
		Iterator<OWLClass> onto2ClassesIterator = onto2Classes.iterator();

		//Make a string representation of the OWLClasses for onto1
		ArrayList<String> listClassesOnto1 = new ArrayList<String> ();
		while(onto1ClassesIterator.hasNext()) {
			listClassesOnto1.add(onto1ClassesIterator.next().getIRI().getFragment());
		}

		//Make a string representation of the OWLClasses for onto2
		ArrayList<String> listClassesOnto2 = new ArrayList<String> ();
		while(onto2ClassesIterator.hasNext()) {
			listClassesOnto2.add(onto2ClassesIterator.next().getIRI().getFragment());
		}

		ArrayList<String> instanceListClasses1 = new ArrayList<String>();
		ArrayList<String> instanceListClasses2 = new ArrayList<String>();

		Map<String, ArrayList<String>> instanceMap1 = new HashMap<String, ArrayList<String>>();
		Map<String, ArrayList<String>> instanceMap2 = new HashMap<String, ArrayList<String>>();

		for (int i = 0; i < listClassesOnto1.size(); i++) {

			instanceListClasses1 = OntologyStatistics.getInstances(listClassesOnto1.get(i), onto1);
			if (instanceListClasses1.size() > 0 && !listClassesOnto1.get(i).equals("Thing")) {
				instanceMap1.put(listClassesOnto1.get(i), instanceListClasses1);
			for (int j = 0; j < instanceListClasses1.size(); j++) {
			}
			}
		}

		for (int i = 0; i < listClassesOnto2.size(); i++) {

			instanceListClasses2 = OntologyStatistics.getInstances(listClassesOnto2.get(i), onto2);
			if (instanceListClasses2.size() > 0 && !listClassesOnto2.get(i).equals("Thing")) {
				instanceMap2.put(listClassesOnto2.get(i), instanceListClasses2);
			for (int j = 0; j < instanceListClasses2.size(); j++) {
			}
			}
		}

		System.out.println("Instances ontology 1");
		for (Map.Entry<String,ArrayList<String>> entry1 : instanceMap1.entrySet()) {
			System.out.println(entry1.getKey() + " --> " + entry1.getValue());

		}

		System.out.println("Instances ontology 2");
		for (Map.Entry<String,ArrayList<String>> entry2 : instanceMap2.entrySet()) {
			System.out.println(entry2.getKey() + " --> " + entry2.getValue());

		}

		//match the instance (values) of each instance map using ISub and if above a threshold
		//output the equivalent classes
		for (Map.Entry<String, ArrayList<String>> entry01 : instanceMap1.entrySet()) {
			for (Map.Entry<String, ArrayList<String>> entry02 : instanceMap2.entrySet()) {
				ArrayList<String> entry01Values = entry01.getValue();
				ArrayList<String> entry02Values = entry02.getValue();
				for (String entry01Instance : entry01Values) {
					for (String entry02Instance : entry02Values) {
						double score = isubMatcher.score(entry01Instance, entry02Instance);
						if (score > 0.8) {
							System.out.println(entry01.getKey() + " and " + entry02.getKey() + " have similar instances (" + entry01Instance + " and " + entry02Instance + " with score " + score + ")");
						}
					}
				}
			}
		}




		double instanceScore = 0;

		return instanceScore;
	}*/

	public static void main(String[] args) throws OWLOntologyCreationException {

		//import the owl files
		File ontoFile1 = new File("./files/UoA/TestTransportWithInstances1.owl");
		File ontoFile2 = new File("./files/UoA/TestTransportWithInstances2.owl");

		double test = matchInstances(ontoFile1, ontoFile2);
	}


}

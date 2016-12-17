package no.ntnu.idi.compose.propstring;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;

public class Subsumption_Domain_Range {

	static File outputAlignment = null;
	static ISub matcher = new ISub();

	private static Set<OWLObjectProperty> getProperties(File f1) throws Exception {

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(f1);

		Set<OWLObjectProperty> props = onto.getObjectPropertiesInSignature();

		return props;

	}

	/*	private static Map matchProperties(Set<OWLObjectProperty> props1, Set<OWLObjectProperty> props2) {
		Map<OWLObjectProperty, OWLObjectProperty> matchingMap = new HashMap<OWLObjectProperty, OWLObjectProperty>();

		Iterator<OWLObjectProperty> itrProps1 = props1.iterator();
		Iterator<OWLObjectProperty> itrProps2 = props2.iterator();

		double iSubScore = 0;

		for (OWLObjectProperty op1 : props1) {
			for (OWLObjectProperty op2 : props2) {
				iSubScore = matcher.score(op1.getIRI().getFragment(), op2.getIRI().getFragment());
				if (iSubScore > 0.2) {
					matchingMap.put(op1, op2);
				}		
			}
		}

		return matchingMap;
	}


	private static Set<OWLClass> getDomain(OWLObjectProperty op, OWLOntology ont) {

		Set<OWLClass> neighbors = new HashSet<>();

			Set<OWLClassExpression> temp = op.asOWLObjectProperty().getDomains(ont);
			for (OWLClassExpression t: temp) {
				if (t.isAnonymous()) {
					Set<OWLClass> components = t.getClassesInSignature();
					for (OWLClass component: components) {
						neighbors.add(component);
					}
			}
		}

		return neighbors;
	}


	private static Set<OWLClass> getRange(OWLObjectProperty op, OWLOntology ont) {

		Set<OWLClass> neighbors = new HashSet<>();

			Set<OWLClassExpression> temp = op.asOWLObjectProperty().getRanges(ont);
			for (OWLClassExpression t: temp) {
				if (t.isAnonymous()) {
					Set<OWLClass> components = t.getClassesInSignature();
					for (OWLClass component: components) {
						neighbors.add(component);
					}
			}
		}

		return neighbors;
	}*/

	//if this takes as parameter a set of classes (as OWLEntity, and with OWLOntology as parameter): 
	//for class c1, get the set of properties O for which this class is defined as domain
	//for each property set O, see if class c2 is defined as range
	//if true, c1 > c2

	public static Alignment matchProps(File onto1, File onto2) throws Exception {

		URIAlignment alignment = new URIAlignment();
		String relation = null;

		//initialize the propString matcher
		String name1 = "";
		String name2 = "";


		PropStringModified propsMatcher = new PropStringModified();

		propsMatcher.setNameA(name1);
		propsMatcher.setNameB(name2);

		Map<OWLClass, OWLClass> subsumptionMap = new HashMap<OWLClass, OWLClass>();

		//get the classes of onto1 and onto2
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology1 = manager.loadOntologyFromOntologyDocument(onto1);
		OWLOntology ontology2 = manager.loadOntologyFromOntologyDocument(onto2);

		Set<OWLClass> onto1Classes = ontology1.getClassesInSignature();
		Set<OWLClass> onto2Classes = ontology1.getClassesInSignature();

		//get list of matched properties from PropString
		Map<OWLEntity, OWLEntity> propsMap = propsMatcher.align(onto1.toURI(), onto2.toURI());
		System.out.println("The size of the properties map is " + propsMap.size());
		
		//printing the properties
		

		Set<OWLEntity> domainClsOnto1 = null;
		Set<OWLEntity> domainClsOnto2 = null;
		Set<OWLEntity> rangeClsOnto1 = null;
		Set<OWLEntity> rangeClsOnto2 = null;

		OWLEntity onto1DomainCls = null;
		OWLEntity onto1RangeCls = null;

		OWLEntity onto2DomainCls = null;
		OWLEntity onto2RangeCls = null;

		Cell subsumptionCell = null;

		//for each object property matching...
		for (Map.Entry<OWLEntity, OWLEntity> entry : propsMap.entrySet()) {
			//get the domain and range classes for both ontologies for this particular property
			//domainClsOnto1 = entry.getKey().asOWLObjectProperty().getDomains(ontology1);
			domainClsOnto1 = PropStringModified.getDomain(entry.getKey().asOWLObjectProperty(), ontology1);
			System.out.println("The number of domain classes declared for " + entry.getKey().asOWLObjectProperty().getIRI().getFragment() + " is " + domainClsOnto1.size());
			
			rangeClsOnto1 = PropStringModified.getRange(entry.getKey().asOWLObjectProperty(),ontology1);
			System.out.println("The number of range classes declared for " + entry.getKey().asOWLObjectProperty().getIRI().getFragment() + " is " + rangeClsOnto1.size());
			
			domainClsOnto2 = PropStringModified.getDomain(entry.getValue().asOWLObjectProperty(),ontology2);
			System.out.println("The number of domain classes declared for " + entry.getValue().asOWLObjectProperty().getIRI().getFragment() + " is " + domainClsOnto2.size());
			
			rangeClsOnto2 = PropStringModified.getRange(entry.getValue().asOWLObjectProperty(),ontology2);
			System.out.println("The number of range classes declared for " + entry.getValue().asOWLObjectProperty().getIRI().getFragment()  + " is " + rangeClsOnto2.size());
			
			System.out.println("\n");
			
			if (domainClsOnto1.isEmpty() || rangeClsOnto2.isEmpty()) {
				System.out.println("There are no domain or range classes associated with this property");

				
			} else {
				onto1DomainCls = domainClsOnto1.iterator().next();
				onto2RangeCls = rangeClsOnto2.iterator().next();
				//check if the domain and range class != null
				if (onto1DomainCls != null || onto2RangeCls != null) {
					relation = "&gt;";
					subsumptionCell = alignment.addAlignCell(onto1DomainCls.asOWLClass().getIRI().toURI(), onto2RangeCls.asOWLClass().getIRI().toURI(), relation, 1.0);
				} else {
					System.out.println("There are no domain or range classes for this property correspondence!");
				}
			}
		 
		
		if (rangeClsOnto1.isEmpty() || domainClsOnto2.isEmpty()) {
			System.out.println("There are no domain or range classes associated with this property");

			
		} else {
			onto1RangeCls = rangeClsOnto1.iterator().next();
			onto2DomainCls = domainClsOnto2.iterator().next();
			//check if the domain and range class != null
			if (onto1RangeCls != null || onto2DomainCls != null) {
				relation = "&lt;";
				subsumptionCell = alignment.addAlignCell(onto1RangeCls.asOWLClass().getIRI().toURI(), onto2DomainCls.asOWLClass().getIRI().toURI(), relation, 1.0);
			} else {
				System.out.println("There are no domain or range classes for this property correspondence!");
			}
		}
		}
	



return alignment;

}

public static void main(String[] args) throws Exception {


	//File ontoFile1 = new File("/Users/audunvennesland/Documents/PhD/Ontologies/TestTransportWithInstances1.owl");
	//File ontoFile2 = new File("/Users/audunvennesland/Documents/PhD/Ontologies/TestTransportWithInstances2.owl");
	
	File ontoFile1 = new File("./data/ontologies/Biblio_2015.rdf");
	File ontoFile2 = new File("./data/ontologies/BIBO.owl");

	OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	OWLOntology onto1 = manager.loadOntologyFromOntologyDocument(ontoFile1);
	OWLOntology onto2 = manager.loadOntologyFromOntologyDocument(ontoFile2);

	Alignment propsAlignment = matchProps(ontoFile1, ontoFile2);
	
	//storing the alignment
	String alignmentFileName = "./data/ontologies/Subsumption_Domain_Range_Test.rdf";
	
	File outputAlignment = new File(alignmentFileName);

FileOutputStream os = new FileOutputStream(outputAlignment);

PrintWriter writer = new PrintWriter (
	new OutputStreamWriter(os, "UTF-8" ), true);

AlignmentVisitor renderer = new RDFRendererVisitor(writer);
propsAlignment.render(renderer);
writer.close();


System.out.println("Completed matching process!");






	/*Set<OWLObjectProperty> props1 = getProperties(ontoFile1);

		int domainIterator = 0;
		System.out.println("Printing all domain classes for " + ontoFile1.getPath());
		Set<OWLClassExpression> op1DomainSet = null;
		for (OWLObjectProperty o : props1) {
			op1DomainSet = o.getDomains(onto1);
			for (OWLClassExpression cls : op1DomainSet) {
				domainIterator++;
				System.out.println(domainIterator + "." + cls.toString());
			}
		}

		int rangeIterator = 0;
		System.out.println("Printing all range classes for " + ontoFile1.getPath());
		Set<OWLClassExpression> op1RangeSet = null;
		for (OWLObjectProperty o : props1) {
			op1RangeSet = o.getRanges(onto1);
			for (OWLClassExpression cls : op1RangeSet) {
				rangeIterator++;
				System.out.println(rangeIterator + "." + cls.toString());
			}
		}


		Set<OWLObjectProperty> props2 = getProperties(ontoFile2);

		Map<OWLObjectProperty, OWLObjectProperty> matchingMap = new HashMap<OWLObjectProperty, OWLObjectProperty>();

		matchingMap = matchProperties(props1, props2);

		OWLObjectProperty thisOP1 = null;
		OWLObjectProperty thisOP2 = null;
		Set<OWLClass> thisDomainSet1 = null;
		Set<OWLClass> thisDomainSet2 = null;
		Set<OWLClass> thisRangeSet1 = null;
		Set<OWLClass> thisRangeSet2 = null;

		for (Map.Entry<OWLObjectProperty, OWLObjectProperty> entry : matchingMap.entrySet()) {
			thisOP1 = entry.getKey();
			thisOP2 = entry.getValue();
			thisDomainSet1 = getDomain(thisOP1, onto1);
			thisDomainSet2 = getDomain(thisOP2, onto2);
			thisRangeSet1 = getRange(thisOP1, onto1);
			thisRangeSet2 = getRange(thisOP2, onto2);

			System.out.println(thisOP1.getIRI().getFragment() + " and " + thisOP2.getIRI().getFragment() + " are considered equal");

			if (!thisDomainSet1.isEmpty()) {
			System.out.println("The domain classes are for " + thisOP1.getIRI().getFragment());
			for (OWLClass s : thisDomainSet1) {
				System.out.println("- " + s.getIRI().getFragment());
			}
			} else {
				System.out.println("No domain classes for " + thisOP1.getIRI().getFragment());
			}

			if (!thisDomainSet2.isEmpty()) {
			System.out.println("The domain classes are for " + thisOP2.getIRI().getFragment());
			for (OWLClass s : thisDomainSet2) {
				System.out.println("- " + s.getIRI().getFragment());
			}
			} else {
				System.out.println("No domain classes for " + thisOP2.getIRI().getFragment());
			}

			System.out.println("\n");

			if (!thisRangeSet1.isEmpty()) {
			System.out.println("The range classes are for " + thisOP1.getIRI().getFragment());
			for (OWLClass s : thisRangeSet1) {
				System.out.println("- " + s.getIRI().getFragment());
			}
			} else {
				System.out.println("No range classes for " + thisOP1.getIRI().getFragment());
			}


			if (!thisRangeSet2.isEmpty()) {
			System.out.println("The range classes are for " + thisOP2.getIRI().getFragment());
			for (OWLClass s : thisRangeSet2) {
				System.out.println("- " + s.getIRI().getFragment());
			}
			} else {
				System.out.println("No range classes for " + thisOP2.getIRI().getFragment());
			}

			System.out.println("\n");

			Set<OWLObjectProperty> propsSet = onto1.getObjectPropertiesInSignature();



			for (OWLObjectProperty op : propsSet) {
				op.getDomains(onto1);
			}



		}*/
}

}


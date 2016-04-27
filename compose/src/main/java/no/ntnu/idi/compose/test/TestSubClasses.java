package no.ntnu.idi.compose.test;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;

public class TestSubClasses {
	
	
	static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	static OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
	public static final AxiomType<OWLSubClassOfAxiom> SUBCLASS_OF = null;


public static void main(String[] args) throws Exception {

	File file = new File("/Users/audunvennesland/Documents/PhD/Ontologies/Cultural Heritage/BIBO/BIBO.owl");
	OWLOntology bibo = manager.loadOntologyFromOntologyDocument(file);

	OWLReasoner reasoner = reasonerFactory.createReasoner(bibo);
	
	OWLClass thisClass;
	//Set<OWLClass> classes = bibo.getClassesInSignature();
	NodeSet<OWLClass> subClasses;
	Iterator<OWLClass> itr = bibo.getClassesInSignature().iterator();
	Map<OWLClass, NodeSet<OWLClass>> classesAndSubClasses = new HashMap<OWLClass, NodeSet<OWLClass>>();
	int subClassCount = 0;
	int totalSubClassCount = 0;

	
	while (itr.hasNext()) {
		thisClass = itr.next();
		subClasses = reasoner.getSubClasses(thisClass, true);
		subClassCount = subClasses.getNodes().size();
		System.out.println(thisClass.toString() + " contains " + subClassCount + " subclass(es)");
		//System.out.println(thisClass.toString());
		//System.out.println(subClasses.toString());
		classesAndSubClasses.put(thisClass, subClasses);
		totalSubClassCount += subClassCount;
	}
	

System.out.println(totalSubClassCount);

	}
	}
	



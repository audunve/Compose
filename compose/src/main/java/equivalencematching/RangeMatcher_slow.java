package equivalencematching;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.ontowrap.OntowrapException;

public class RangeMatcher_slow extends ObjectAlignment implements AlignmentProcess {
	
	double weight;
	static Map<String, Set<String>> rangeMapOnto1 = new HashMap<String, Set<String>>();
	static Map<String, Set<String>> rangeMapOnto2 = new HashMap<String, Set<String>>();
	
	public RangeMatcher_slow(double weight) {
		this.weight = weight;
	}
	
	public void align(Alignment alignment, Properties param) throws AlignmentException {
		
		try {
			// Match classes
			for ( Object cl2: ontology2().getClasses() ){
				for ( Object cl1: ontology1().getClasses() ){
					
					// add mapping into alignment object 
					addAlignCell(cl1,cl2, "=", weight*getRangeSim(cl1,cl2));  
				}

			}

		} catch (Exception e) { e.printStackTrace(); }
	}
	

	public double getRangeSim(Object o1, Object o2) throws OntowrapException, OWLOntologyCreationException {

		//get the objects (entities)
		String s1 = ontology1().getEntityName(o1).toLowerCase();
		String s2 = ontology2().getEntityName(o2).toLowerCase();
		
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		
		String ontoFile1 = ontology1().getFile().toString();
		String ontoFile2 = ontology2().getFile().toString();
		
		String ontoFile1Path = ontoFile1.replace("file:", "");
		String ontoFile2Path = ontoFile2.replace("file:", "");
		
		OWLOntology onto1 = manager.loadOntologyFromOntologyDocument(new File(ontoFile1Path));
		OWLOntology onto2 = manager.loadOntologyFromOntologyDocument(new File(ontoFile2Path));
		
		Set<String> range1 = getRange(onto1, s1);
		Set<String> range2 = getRange(onto2, s2);
		
		
		double jaccardSim = utilities.Jaccard.jaccardSetSim(range1, range2);
		double measure = 0;
		
		if (jaccardSim > 0 && jaccardSim <= 1.0) {
			measure = jaccardSim;
		} else {
			measure = 0;
		}
		
		return measure;
	
	}
	
	private static Set<String> getRange(OWLOntology onto, String clsString) {
		
		Set<OWLClass> allClasses = onto.getClassesInSignature();		
		
		Set<OWLObjectProperty> ops = new HashSet<OWLObjectProperty>();
		
		Set<String> range = new HashSet<String>();
		
		for (OWLClass cls : allClasses) {
			if (cls.getIRI().getFragment().toLowerCase().equals(clsString)) {
			
				for (OWLObjectPropertyDomainAxiom op : onto.getAxioms(AxiomType.OBJECT_PROPERTY_DOMAIN)) {
				if (op.getDomain().equals(cls)) {
					for (OWLObjectProperty oop : op.getObjectPropertiesInSignature()) {
						ops.add(oop);
					}
				}
			}
			}
		}
			
		//get the range classes from the object properties 
		for (OWLObjectProperty oop : ops) {
			Set<OWLClassExpression> rangeCls = oop.getRanges(onto);
			for (OWLClassExpression oce : rangeCls) {
				if (!oce.isAnonymous()) {
				range.add(oce.asOWLClass().getIRI().getFragment());
				}
			}
		}
				
		
		return range;
		
	}
	
		
		}



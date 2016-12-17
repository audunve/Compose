package edu.wright.cheatham.propstring;



import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.manchester.cs.owl.owlapi.OWLAnnotationPropertyImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

public class StringUtils {
	
	public static String getString(OWLEntity e, OWLOntology ontology) {
		
		String label = e.getIRI().toString();
		
		if (label.contains("#")) {
			label = label.substring(label.indexOf('#')+1);
			return label;
		}
		
		if (label.contains("/")) {
			label = label.substring(label.lastIndexOf('/')+1);
			return label;
		}
		
    	Set<OWLAnnotation> labels = e.getAnnotations(
    			ontology, new OWLAnnotationPropertyImpl(
    			IRI.create("http://www.w3.org/2000/01/rdf-schema#label")));
    	
        if (labels != null && labels.size() > 0) {
    		label = ((OWLAnnotation) labels.toArray()[0]).getValue().toString();
    		if (label.startsWith("\"")) {
    			label = label.substring(1);
    		}
    		
    		if (label.contains("\"")) {
    			label = label.substring(0, label.lastIndexOf('"'));
    		}
    	}
    	
    	return label;
	}
	
	
	public static String getString(String label) {
		
		if (label.contains("#")) {
			label = label.substring(label.indexOf('#')+1);
			return label;
		}
		
		if (label.contains("/")) {
			label = label.substring(label.lastIndexOf('/')+1);
			return label;
		}
    	
    	return label;
	}
}

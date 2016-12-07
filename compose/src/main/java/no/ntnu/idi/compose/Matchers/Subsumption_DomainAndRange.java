package no.ntnu.idi.compose.Matchers;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.ontowrap.HeavyLoadedOntology;
import fr.inrialpes.exmo.ontowrap.OntowrapException;
import no.ntnu.idi.compose.Loading.OWLLoader;
import no.ntnu.idi.compose.algorithms.ISub;

public class Subsumption_DomainAndRange extends ObjectAlignment implements AlignmentProcess {
	
/*	get ontology concepts
	for each concept 
		get object properties
			for each object property
				match object properties using propString
					get domain and range
						if c1.domain is similar to c2.range
							c1 > c2*/
						
    private HeavyLoadedOntology<Object> honto1 = null;
    private HeavyLoadedOntology<Object> honto2 = null;
    
    ISub matcher = new ISub();
    
    public void init(Object o1, Object o2, Object ontologies) throws AlignmentException {
    	super.init( o1, o2, ontologies );
    	if ( !( getOntologyObject1() instanceof HeavyLoadedOntology
    		&& getOntologyObject2() instanceof HeavyLoadedOntology ))
    	    throw new AlignmentException( "NameAndPropertyAlignment requires HeavyLoadedOntology ontology loader" );
        }
	
public void align(Alignment alignment, Properties param) throws AlignmentException {
		
	//loadInit( alignment );
	honto1 = (HeavyLoadedOntology<Object>)getOntologyObject1();
	honto2 = (HeavyLoadedOntology<Object>)getOntologyObject2();
	
	Set props1 = null;
	Set props2 = null;
	
	Map matchedProps = null;
	
	double score = 0; 
	double iSubScore = 0;
	
	//get a list of properties associated with
	try {
		props1 = honto1.getObjectProperties();
		props2 = honto2.getObjectProperties();
		
		
	} catch (OntowrapException e1) {
		// FIXME Auto-generated catch block
		e1.printStackTrace();
	}
	
	//match properties
	for (int i = 0; i < props1.size(); i++) {
		for (int j = 0; j < props2.size(); j++) {

			iSubScore = matcher.score(props1.iterator().next().toString(), props2.iterator().next().toString());
			if (iSubScore > 0.3) {
				matchedProps.put(props1.iterator().next().toString(),props2.iterator().next().toString());
			}
			
		}
	}
	
		try {
			// Match classes
			for ( Object cl2: ontology2().getClasses() ){
				for ( Object cl1: ontology1().getClasses() ){
			
					// add mapping into alignment object 
					addAlignCell(cl1,cl2, "=", score);  
				}
				
			}

		} catch (Exception e) { e.printStackTrace(); }
	}
	
	
	
	
}

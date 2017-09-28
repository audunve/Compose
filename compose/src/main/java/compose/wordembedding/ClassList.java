package compose.wordembedding;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.util.OWLOntologyWalker;
import org.semanticweb.owlapi.util.OWLOntologyWalkerVisitor;

import compose.misc.StringUtils;
import compose.statistics.OntologyStatistics;

/**
 * @author audunvennesland
 * 30. aug. 2017 
 */
public class ClassList {
	
	/**
	 * An OWLOntologyManagermanages a set of ontologies. It is the main point
	 * for creating, loading and accessing ontologies.
	 */
	static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	/**
	 * The OWLReasonerFactory represents a reasoner creation point.
	 */
	static OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
	
	static OWLDataFactory factory = manager.getOWLDataFactory();

	
	public static void main(String [] args) throws OWLOntologyCreationException, IOException {
		
		//import ontology
		File ontoFile = new File("./files/OAEI2011/301-302/301.rdf");
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);
		
		
		//create classlist
		Set<OWLClass> classes = onto.getClassesInSignature();
		
		for (OWLClass cls : classes) {
			System.out.println("conceptUri: " + cls.getIRI().toString());
			String strictLabel = StringUtils.getString(cls.getIRI().toString());
			System.out.println("Strict label: " + strictLabel.toLowerCase());
			//if the label is a compound, then get the individual words
			if (OntologyStatistics.isCompound(strictLabel)) {
				System.out.println("individual words in label: ");
				String[] temp_compounds = strictLabel.split("(?<=.)(?=\\p{Lu})");
				for (int i = 0; i < temp_compounds.length; i++) {
					System.out.println(temp_compounds[i].toLowerCase());
				}
			}
			
			for(OWLAnnotation a : cls.getAnnotations(onto, factory.getRDFSComment())) {
			    OWLAnnotationValue value = a.getValue();
			    if(value instanceof OWLLiteral) {
			        System.out.println("comment: " + ((OWLLiteral) value).getLiteral());   
			        String comment = ((OWLLiteral) value).getLiteral().toString();
			        String commentWOStopWords = StringUtils.removeStopWordsFromString(comment);
			        System.out.println("comment without stopwords: " + commentWOStopWords);
			    }
			}
			System.out.println("global:");
			System.out.println("\n");
		}

		

		
	}

}

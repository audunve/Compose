package utilities;

import java.io.File;
import java.io.PrintStream;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;



/** 
 * Prints the subsumption hiearchy of an ontology starting from Thing. This hierarchy can be used to import OWL ontologies in S-Match. This code is borrowed from a tutorial on the OWL API.
 * @author audunvennesland (originally Sean Bechhofer)
 */
public class PrintSubsumptionHiearchy {
    private static int INDENT = 4;
    private final OWLReasonerFactory reasonerFactory;
    private final OWLOntology ontology;
    private final PrintStream out;

    private PrintSubsumptionHiearchy(OWLReasonerFactory reasonerFactory,
            OWLOntology _ontology) {
        this.reasonerFactory = reasonerFactory;
        ontology = _ontology;
        out = System.out;
    }

    /** Print the class hierarchy for the given ontology from this class down,
     * assuming this class is at the given level. Makes no attempt to deal
     * sensibly with multiple inheritance. */
    private void printHierarchy(OWLClass clazz) throws OWLException {
        OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(ontology);
        printHierarchy(reasoner, clazz, 0);
        /* Now print out any unsatisfiable classes */
        for (OWLClass cl : ontology.getClassesInSignature()) {
            if (!reasoner.isSatisfiable(cl)) {
                //out.println("XXX: " + labelFor(cl));
                out.println("XXX: " + cl.getIRI().getFragment());
            }
        }
        reasoner.dispose();
    }

    private String labelFor(OWLClass clazz) {
        /*
         * Use a visitor to extract label annotations
         */
        LabelExtractor le = new LabelExtractor();
        Set<OWLAnnotation> annotations = clazz.getAnnotations(ontology);
        for (OWLAnnotation anno : annotations) {
            anno.accept(le);
        }
        /* Print out the label if there is one. If not, just use the class URI */
        if (le.getResult() != null) {
            return le.getResult().toString();
        } else {
            return clazz.getIRI().toString();
        }
    }

    /** Print the class hierarchy from this class down, assuming this class is at
     * the given level. Makes no attempt to deal sensibly with multiple
     * inheritance. */
    private void printHierarchy(OWLReasoner reasoner, OWLClass clazz, int level)
            throws OWLException {
        /*
         * Only print satisfiable classes -- otherwise we end up with bottom
         * everywhere
         */
        if (reasoner.isSatisfiable(clazz)) {
            for (int i = 0; i < level * INDENT; i++) {
                out.print("\t");
            }
            out.println(labelFor(clazz));
            /* Find the children and recurse */
            for (OWLClass child : reasoner.getSubClasses(clazz, true).getFlattened()) {
                if (!child.equals(clazz)) {
                    printHierarchy(reasoner, child, level + 1);
                }
            }
        }
    }
    
    
    public static void main(String[] args) throws OWLException, InstantiationException,
            IllegalAccessException, ClassNotFoundException {
    	
    	 	OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
    	
    	 	//the file for which its subsumption hiearchy will be printed
		File ontoFile = new File("./files/ESWC_WordEmbedding_Experiment/ATMONTO-AIRM-O/ontologies/ATMOntoCoreMerged.owl");
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);

		//gets Thing
        OWLClass clazz = manager.getOWLDataFactory().getOWLThing();
        
        //Create a new SimpleHierarchy object with the given reasoner.
        PrintSubsumptionHiearchy simpleHierarchy = new PrintSubsumptionHiearchy(
        		reasonerFactory, onto);

        // Print the hierarchy including thing
        simpleHierarchy.printHierarchy(clazz);
    }
}
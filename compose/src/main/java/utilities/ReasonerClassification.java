package utilities;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

public class ReasonerClassification {
	
	public static void main(String[] args) throws OWLOntologyCreationException {
		
		File ontoFile = new File("./files/_PHD_EVALUATION/BIBFRAME-SCHEMAORG/ONTOLOGIES/MergeBibframe-Schemaorg.owl");
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();		
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);
		PelletReasoner pr = PelletReasonerFactory.getInstance().createReasoner( onto );
		


		//using the Hermit reasoner
		//Reasoner reasoner=new Reasoner(onto);
		//Reasoner reasoner = new Reasoner.ReasonerFactory().createReasoner(onto);
		pr.prepareReasoner();
		pr.precomputeInferences(InferenceType.CLASS_HIERARCHY);
		
		for (OWLClass cl : onto.getClassesInSignature()) {
			NodeSet<OWLClass> subclassSet = pr.getSubClasses(cl, false);
			for (Node<OWLClass> sub : subclassSet) {
				System.out.println(sub.getRepresentativeElement().getIRI());
			}
		}
		
		
	}

}

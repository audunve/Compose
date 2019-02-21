package test;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import it.uniroma1.lcl.babelnet.BabelNet;
import utilities.BabelNetOperations;
import utilities.StringUtilities;

public class TestBabelNetProfiling {
	
	final static BabelNet bn = BabelNet.getInstance();
	
	public static void main(String[] args) throws OWLOntologyCreationException {
		
		File ontoFile1 = new File("./files/_PHD_EVALUATION/BIBFRAME-SCHEMAORG/ONTOLOGIES/bibframe.rdf");
		File ontoFile2 = new File("./files/_PHD_EVALUATION/BIBFRAME-SCHEMAORG/ONTOLOGIES/schema-org.owl");
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto1 = manager.loadOntologyFromOntologyDocument(ontoFile1);
		OWLOntology onto2 = manager.loadOntologyFromOntologyDocument(ontoFile2);
		
		System.out.println("\n*** Hyponym Richness (BabelNet) ***");
		System.out.println("The Hyponym Richness (HR) (BabelNet) of " + ontoFile1.getName() + " and " + ontoFile2.getName() + " is: "
				+ round((computeHyponymRichnessBabelNet(ontoFile1, ontoFile2)), 2));
		
		
		System.out.println("\n*** Synonym Richness (BabelNet) ***");
		System.out.println("The Synonym Richness (SR) (BabelNet) of " + ontoFile1.getName() + " and " + ontoFile2.getName() + " is: "
				+ round((computeSynonymRichnessBabelNet(ontoFile1, ontoFile2)), 2));
		
		System.out.println("\n*** Lexical Coverage (BabelNet) ***");
		System.out.println("The Lexical Coverage (BabelNet) of " + ontoFile1.getName() + " and " + ontoFile2.getName() + " is: "
				+ round((computeBabelNetCoverageComp(ontoFile1, ontoFile2)), 2));
		
	}
	
	public static double computeHyponymRichnessBabelNet(File ontoFile1, File ontoFile2) throws OWLOntologyCreationException {

		double hyponymCounterOnto1 = 0;
		double hyponymCounterOnto2 = 0;

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto1 = manager.loadOntologyFromOntologyDocument(ontoFile1);
		OWLOntology onto2 = manager.loadOntologyFromOntologyDocument(ontoFile2);

		int numClassesTotalOnto1 = onto1.getClassesInSignature().size();
		int numClassesTotalOnto2 = onto2.getClassesInSignature().size();
		
		for (OWLClass cl : onto1.getClassesInSignature()) {
			ArrayList<String> hyponyms = BabelNetOperations.getHyponyms(cl.getIRI().getFragment());
			
			if (hyponyms.size() > 0) {
				hyponymCounterOnto1++;
			}
		}
		
		for (OWLClass cl : onto2.getClassesInSignature()) {
			ArrayList<String> hyponyms = BabelNetOperations.getHyponyms(cl.getIRI().getFragment());
			
			if (hyponyms.size() > 0) {
				hyponymCounterOnto2++;
			}

		}

		double hyponymRichnessOnto1 = (double) hyponymCounterOnto1 / (double) numClassesTotalOnto1;
		double hyponymRichnessOnto2 = (double) hyponymCounterOnto2 / (double) numClassesTotalOnto2;

		return (hyponymRichnessOnto1 + hyponymRichnessOnto2) / 2;
	}

	
	/**
	 * Returns the average number of synonyms in BabelNet for each class in an
	 * ontology
	 * 
	 * @param ontoFile:
	 *            an ontology file
	 * @return the average number of synonyms per class in an ontology
	 * @throws OWLOntologyCreationException
	 */
	public static double computeSynonymRichnessBabelNet(File ontoFile1, File ontoFile2) throws OWLOntologyCreationException {

		double synonymCounterOnto1 = 0;
		double synonymCounterOnto2 = 0;

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto1 = manager.loadOntologyFromOntologyDocument(ontoFile1);
		OWLOntology onto2 = manager.loadOntologyFromOntologyDocument(ontoFile2);

		int numClassesTotalOnto1 = onto1.getClassesInSignature().size();
		int numClassesTotalOnto2 = onto2.getClassesInSignature().size();
		
		for (OWLClass cl : onto1.getClassesInSignature()) {
			Set<String> synonyms = BabelNetOperations.
					getSynonyms(StringUtilities.stringTokenize(cl.getIRI().getFragment(), true));
			
			if (synonyms.size() > 0) {
				synonymCounterOnto1++;
			}

		}
		
		for (OWLClass cl : onto2.getClassesInSignature()) {
			Set<String> synonyms = BabelNetOperations.
					getSynonyms(StringUtilities.stringTokenize(cl.getIRI().getFragment(), true));
			
			if (synonyms.size() > 0) {
				synonymCounterOnto2++;
			}

		}

		double synonymRichnessOnto1 = (double) synonymCounterOnto1 / (double) numClassesTotalOnto1;
		double synonymRichnessOnto2 = (double) synonymCounterOnto2 / (double) numClassesTotalOnto2;

		return (synonymRichnessOnto1 + synonymRichnessOnto2) / 2;
	}
	
	public static double computeBabelNetCoverageComp(File ontoFile1, File ontoFile2) throws OWLOntologyCreationException {
		
		double BC = (BabelNetOperations.getBabelNetCoverageComp(ontoFile1)
				+ BabelNetOperations.getBabelNetCoverageComp(ontoFile2)) / 2;
		
		
		return BC;
		
		
	}
	
	private static double round(double value, int places) {
		if (places < 0)
			throw new IllegalArgumentException();

		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}
}

package no.ntnu.idi.compose.profiling;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import fr.inrialpes.exmo.ontowrap.OntowrapException;
import no.ntnu.idi.compose.loading.OWLLoader;

public class OntologyProfiler {
	
	static OntologyProcessor processor = new OntologyProcessor();
	
public static void main(String[] args) throws OWLOntologyCreationException, URISyntaxException, OntowrapException, IOException {
		
	File ontologyDir = new File("./files/experiment_eswc17/ontologies");
	File[] filesInDir = ontologyDir.listFiles();
	for (File file: filesInDir) {
		if (!file.isDirectory()) {
			
			//System.out.println(file);
			System.out.println("The Num Class Compounds (CNC) of " + file.getName() + " is:  " + OWLLoader.getNumClassCompounds(file));
			System.out.println("The Common Substrings (CS) of " + file.getName() + " is:  " + OWLLoader.getNumClassCompounds(file));
			System.out.println("The Inheritance Richness (IR) of " + file.getName() + " is:  " + OntologyProcessor.computeInheritanceRichness(file));
			//System.out.println("The Annotation Coverage (AC) of " + file.getName() + " is:  " + OntologyProcessor.computeNullLabelOrComment(file));
			System.out.println("The Relationship Richness (RR) of " + file.getName() + " is:  " + OntologyProcessor.computeRelationshipRichness(file));
			System.out.println("The WordNet Coverage (WC) of " + file.getName() + " is:  " + OntologyProcessor.computeWordNetCoverage(file));
			System.out.println("The Hyponymy Richness of " + file.getName() + " is:  " + OWLLoader.getHyponymRichness(file));
			System.out.println("The Synonym Richness of " + file.getName() + " is:  " + OWLLoader.getSynonymRichness(file));
			//System.out.println("The Num Property Compounds (NPC) of " + file.getName() + " is:  " + OWLLoader.getNumPropertyCompounds(file));
			//System.out.println("The Class Richness (CR) of " + file.getName() + " is:  " + OntologyProcessor.computeClassRichness(file));
			//System.out.println("The Average Population (OP) of " + file.getName() + " is:  " + OntologyProcessor.computeAveragePopulation(file));
			System.out.println("\n");
			
		}
		
	}
		
	}

}

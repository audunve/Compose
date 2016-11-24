package no.ntnu.idi.compose.Loading;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import fr.inrialpes.exmo.ontowrap.OntowrapException;
import no.ntnu.idi.compose.Processing.OntologyProcessor;

public class OntologyProfiler {
	
	static OntologyProcessor processor = new OntologyProcessor();
	
public static void main(String[] args) throws OWLOntologyCreationException, URISyntaxException, OntowrapException, IOException {
		
	File ontologyDir = new File("./files/OAEI-16-conference/conference");
	File[] filesInDir = ontologyDir.listFiles();
	for (File file: filesInDir) {
		if (!file.isDirectory()) {
			

			System.out.println("The Inheritance Richness (IR) of " + file.getName() + " is:  " + OntologyProcessor.computeInheritanceRichness(file));
			System.out.println("The NullLabelOrComment (N) of " + file.getName() + " is:  " + OntologyProcessor.computeNullLabelOrComment(file));
			System.out.println("The Relationship Richness (RR) of " + file.getName() + " is:  " + OntologyProcessor.computeRelationshipRichness(file));
			System.out.println("The WordNet Coverage (WC) of " + file.getName() + " is:  " + OntologyProcessor.computeWordNetCoverage(file));
			System.out.println("The Num Compounds (NC) of " + file.getName() + " is:  " + OWLLoader.getNumCompounds(file));
			System.out.println("The Class Richness (CR) of " + file.getName() + " is:  " + OntologyProcessor.computeClassRichness(file));
			System.out.println("The Average Population (OP) of " + file.getName() + " is:  " + OntologyProcessor.computeAveragePopulation(file));
			System.out.println("\n");
			System.out.println(file);
		}
		
	}
		
	}

}

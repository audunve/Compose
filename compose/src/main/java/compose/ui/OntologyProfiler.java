package compose.ui;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import compose.profiling.OntologyProcessor;
import compose.statistics.OntologyStatistics;
import fr.inrialpes.exmo.ontowrap.OntowrapException;
import net.didion.jwnl.JWNLException;

public class OntologyProfiler {
	
	static OntologyProcessor processor = new OntologyProcessor();
	
public static void main(String[] args) throws OWLOntologyCreationException, URISyntaxException, OntowrapException, IOException, JWNLException {
	
	File ontoFile = new File("./files/OAEI-16-conference/ontologies/Conference.owl");
	
	double wc = OntologyStatistics.getWordNetCoverage(ontoFile);
	
	System.out.println("The WC is " + wc);
		
	File ontologyDir = new File("./files/experiment_eswc17/ontologies");
	File[] filesInDir = ontologyDir.listFiles();
	/*for (File file: filesInDir) {
		if (!file.isDirectory()) {
			
			//System.out.println(file);
			System.out.println("The Num Class Compounds (CNC) of " + file.getName() + " is:  " + OntologyStatistics.getNumClassCompounds(file));
			System.out.println("The Common Substrings (CS) of " + file.getName() + " is:  " + OntologyStatistics.getNumClassCompounds(file));
			System.out.println("The Inheritance Richness (IR) of " + file.getName() + " is:  " + OntologyProcessor.computeInheritanceRichness(file));
			//System.out.println("The Annotation Coverage (AC) of " + file.getName() + " is:  " + OntologyProcessor.computeNullLabelOrComment(file));
			System.out.println("The Relationship Richness (RR) of " + file.getName() + " is:  " + OntologyProcessor.computeRelationshipRichness(file));
			try {
				System.out.println("The WordNet Coverage (WC) of " + file.getName() + " is:  " + OntologyProcessor.computeWordNetCoverage(file));
			} catch (Exception e) {
				// FIXME Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("The Hyponymy Richness of " + file.getName() + " is:  " + OntologyStatistics.getHyponymRichness(file));
			System.out.println("The Synonym Richness of " + file.getName() + " is:  " + OntologyStatistics.getSynonymRichness(file));
			//System.out.println("The Num Property Compounds (NPC) of " + file.getName() + " is:  " + OntologyStatistics.getNumPropertyCompounds(file));
			//System.out.println("The Class Richness (CR) of " + file.getName() + " is:  " + OntologyProcessor.computeClassRichness(file));
			//System.out.println("The Average Population (OP) of " + file.getName() + " is:  " + OntologyProcessor.computeAveragePopulation(file));
			System.out.println("\n");
			
		}
		
	}*/
		
	}

}

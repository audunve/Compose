package no.ntnu.idi.compose.Evaluate;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;

import fr.inrialpes.exmo.align.parser.AlignmentParser;

public class MatcherComposition {
	
	File outputAlignment = null;

	Properties params = new Properties();
	AlignmentProcess a = null;
	AlignmentParser inputParser = new AlignmentParser(0);

	
public static void main(String[] args) throws AlignmentException, IOException, URISyntaxException {
		
		final String scenario = "sequential_complete";
		final String dataset = "biblio-bibo";
		File f1 = new File ("./files/experiment_eswc17/ontologies/biblio.rdf");
		File f2 = new File ("./files/experiment_eswc17/ontologies/bibo.owl");
		
		Alignment a1 = null;
		Alignment a2 = null;
		Alignment a3 = null;
		
		switch(scenario) {
		
		//sequential compositions
		
		//Complete match: All ontology concepts for the two input ontologies are matched by each matcher m, but the correspondences 
		//identified by the previous matcher are given some weight.
		case "sequential_complete":			
			a1 = SequentialComposition.produceStringMatcherAlignment(f1.toURI(), f2.toURI(), dataset);
			a2 = SequentialComposition.produceWordNetMatcherAlignment(f1.toURI(), f2.toURI(), dataset);
			a3 = SequentialComposition.produceStructuralMatcherAlignment(f1.toURI(), f2.toURI(), dataset);
			
		break;
			
		//Partial match: All correspondences in A1 (from m1) are transferred to m2 for refinement, whereby m2 only processes these 
		//correspondences.
		case "sequential_partial_1":
			a1 = SequentialComposition.produceStringMatcherAlignment(f1.toURI(), f2.toURI(), dataset);
			a2 = SequentialComposition.produceWordNetMatcherAlignment(f1.toURI(), f2.toURI(), dataset);
			
			//iterate over each cell a1 and replace if a2 produces a better score for a given cell
			
		break;
		//Partial match: Only the correspondences in A1 (from m1) above a certain threshold are transferred to m2, whereby m2 only 
		//processes these correspondences.	
		case "sequential_partial_2":
			
		break;
			
			
		//parallel compositions	
		case "parallel_complete":
			
		break;
		
		//hybrid compositions
		case "hybrid_complete":
			
		break;
			
		case "hybrid_partial_1":
			
		break;
			
		case "hybrid_partial_2":
		
		break;

		
		}
			
	}

}

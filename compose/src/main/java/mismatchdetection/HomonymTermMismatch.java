package mismatchdetection;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;

import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.BasicConfidence;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.rel.A5AlgebraRelation;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import utilities.AlignmentOperations;
import utilities.OntologyOperations;
import utilities.StringUtilities;
import utilities.WordNet;
import wordembedding.VectorExtractor;

public class HomonymTermMismatch {
	
	private static final File vectorFile = new File("./files/skybrary_trained_reduced.txt");
	
	public static URIAlignment detectHomonymTermMismatch(BasicAlignment inputAlignment, OWLOntology onto1, OWLOntology onto2) throws AlignmentException, IOException {
		URIAlignment homonymTermMismatchAlignment = new URIAlignment();
		double threshold = 0;
				
		//need to copy the URIs from the input alignment to the new alignment
		URI onto1URI = inputAlignment.getOntology1URI();
		URI onto2URI = inputAlignment.getOntology2URI();
		homonymTermMismatchAlignment.init(onto1URI, onto2URI, A5AlgebraRelation.class, BasicConfidence.class);

		for (Cell c : inputAlignment) {

			Set<OWLObjectProperty> props_ci = OntologyOperations.getObjectProperties(onto1, OntologyOperations.getClass(c.getObject1AsURI().getFragment(), onto1));
			Set<OWLObjectProperty> props_cj = OntologyOperations.getObjectProperties(onto2, OntologyOperations.getClass(c.getObject2AsURI().getFragment(), onto2));
			
			if (c.getObject1AsURI().getFragment().equals(c.getObject2AsURI().getFragment()) && isHomonym(c.getObject1AsURI().getFragment().toLowerCase()) && isHomonym(c.getObject2AsURI().getFragment().toLowerCase())
					&& getPropSim(onto1, onto2, props_ci, props_cj) > threshold) {
				homonymTermMismatchAlignment.addAlignCell(c.getObject1(), c.getObject2(), "=", 1.0);
			}
		}
		
		System.out.println("The homonoym terms mismatch algorithm detected " +  homonymTermMismatchAlignment.nbCells() + " mismatch relations");
		
		BasicAlignment filteredAlignment = AlignmentOperations.createDiffAlignment(inputAlignment, homonymTermMismatchAlignment);
		
		return (URIAlignment) filteredAlignment;

	}
	
	private static boolean isHomonym(String s) {
		int senses = WordNet.getNumSenses(s);
		
		if (senses > 1) {
			return true;
		} else return false;
	}
	
	/** TODO: Include data properties for the word embedding similarity, code needs to be optimised w.r.t. runtime performance, really slow right now. 
	 * Measures similarity between properties using both PropString (syntactic similarity) and word embeddings (semantic similarity)
	 * @param onto1 OWLOntology source ontology
	 * @param onto2 OWLOntology target ontology
	 * @param propSet1 set of object properties related to the source ontology
	 * @param propSet2 set of object properties related to the target ontology
	 * @return a similarity measure stating to what extent the sets of properties are similar or not. 
	 * @throws AlignmentException
	 * @throws IOException
	   Nov 26, 2018
	 */
	private static double getPropSim(OWLOntology onto1, OWLOntology onto2, Set<OWLObjectProperty> propSet1, Set<OWLObjectProperty> propSet2) throws AlignmentException, IOException {


		double sim = 0;

		//get alignment from PropString
		double propStringSim = 0;
		int counter = 0;
		double embeddingSim = 0;
		int counter2 = 0;
		Map<String, ArrayList<Double>> vectorMap = VectorExtractor.createVectorMap(vectorFile);
		StringBuilder ops1SB = new StringBuilder();
		StringBuilder ops2SB = new StringBuilder();

		File propStringAlignmentFile = new File("./files/propstringalignment.rdf");
		AlignmentParser parser = new AlignmentParser();
		BasicAlignment inputAlignment = (BasicAlignment) parser.parse(propStringAlignmentFile.toURI().toString());
		//check if the alignment contains properties op_ci and op_cj
		for (OWLObjectProperty op_ci : propSet1) {
			for (OWLObjectProperty op_cj : propSet2) {
				for (Cell c : inputAlignment) {
					if (c.getObject1AsURI().getFragment().toLowerCase().equals(op_ci.getIRI().getFragment().toLowerCase()) || 
							c.getObject1AsURI().getFragment().toLowerCase().equals(op_cj.getIRI().getFragment().toLowerCase())
							&& 
							c.getObject2AsURI().getFragment().toLowerCase().equals(op_ci.getIRI().getFragment().toLowerCase()) || 
							c.getObject2AsURI().getFragment().toLowerCase().equals(op_cj.getIRI().getFragment().toLowerCase())) {
						propStringSim = c.getStrength();

					} else {
						propStringSim = 0;
					}

				}

				counter++;		
				System.out.println("Test: propStringSim is " + propStringSim + " counter is " + counter);

				//get sim using word embeddings				
				//get definitions for ci
				Set<String> op_ci_defs = OntologyOperations.getOPDefinitions(onto1, op_ci);

				for (String s : op_ci_defs) {
					ops1SB.append(StringUtilities.removeStopWords(s.replaceAll("[^a-zA-Z0-9\\s]", ""))); //remove all non-word characters/symbols
				}

				//get definitions for cj
				Set<String> op_cj_defs = OntologyOperations.getOPDefinitions(onto2, op_cj);

				for (String s : op_cj_defs) {
					ops2SB.append(StringUtilities.removeStopWords(s.replaceAll("[^a-zA-Z0-9\\s]", ""))); //remove all non-word characters/symbols
				}

				//System.out.println("Test: Computing embeddingSim between " + ops1SB.toString() + " AND " + ops2SB.toString());
				//Have to ensure that neither of the stringbuilders are empty
				if (ops1SB.length() > 3 && ops2SB.length() > 3) {
					//System.out.println("Test: Lengths ops1SB: " + ops1SB.length() + ", ops2SB: " + ops2SB.length());
					embeddingSim += VectorSim.computeDefSim(ops1SB.toString(), ops2SB.toString(), vectorMap);
				} else {
					embeddingSim = 0;
				}
				counter2++;
				System.out.println("Test: EmbeddingSim is " + embeddingSim + " counter2 is " + counter2);
			}
		}

		//get the average propStringSim
		propStringSim = propStringSim/counter;
		System.out.println("Test: average propStringSim is " + propStringSim);

		//get the average embeddingSim
		embeddingSim = embeddingSim/counter2;
		System.out.println("Test: average embeddingSim is " + embeddingSim);

		sim = (propStringSim + embeddingSim)/2;
		System.out.println("Test: sim (propStringSim and embeddingSim) is " + sim);

		return sim;
	}

}

package mismatchdetection;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.BasicConfidence;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.rel.A5AlgebraRelation;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import net.didion.jwnl.JWNLException;
import utilities.Jaccard;
import utilities.OntologyOperations;
import utilities.StringUtilities;
import utilities.WordNet;
import wordembedding.VectorExtractor;

public class SynonymTermsMismatch {

	//private static File vectorFile = new File("./files/wikipedia-300.txt");
	private static File vectorFile = new File("./files/wikipedia-reduced.txt");

	public static void main(String[] args) throws AlignmentException, OWLOntologyCreationException, JWNLException, IOException {

		File inputAlignmentFile = new File("./files/ESWC_ATMONTO_AIRM/aml-atmonto-airm.rdf");
		String output = "./files/ESWC_ATMONTO_AIRM/Evaluation/Bibframe-Schemaorg/SynonymTermsMismatchAlignmentEmbeddingSimilarity.rdf";

		File ontoFile1 = new File("./files/ESWC_ATMONTO_AIRM/bibframe.rdf");
		File ontoFile2 = new File("./files/ESWC_ATMONTO_AIRM/schema-org.owl");

		//parse the alignment file
		AlignmentParser parser = new AlignmentParser();
		BasicAlignment amlAlignment = (BasicAlignment) parser.parse(inputAlignmentFile.toURI().toString());
		System.out.println("The input alignment contains " + amlAlignment.nbCells() + " relations");

		URIAlignment extendedAlignment = detectSynonymTermsMismatches(amlAlignment, ontoFile1, ontoFile2, vectorFile);
		System.out.println("The extended alignment contains " + extendedAlignment.nbCells() + " relations");

		//write produced alignment to file
		PrintWriter writer = new PrintWriter(
				new BufferedWriter(
						new FileWriter(output)), true); 
		AlignmentVisitor renderer = new RDFRendererVisitor(writer);

		extendedAlignment.render(renderer);

		writer.flush();
		writer.close();

	}

	/** TODO: A good approach for measuring synonym similarity (general classes include a large number of synonyms that may falsely conclude false positives).
	 * Checks if two pairs of classes are equivalent on the basis of the similarity between their synonyms and their properties. The synonym similarity is computed by Jaccard, and the property similarity
	 * is measured using both PropString (syntactic similarity) and word embeddings (semantic similarity)
	 * @param onto1  source ontology
	 * @param onto2  target ontology
	 * @return an alignment holding all relations considered synonym terms "mismatch"
	 * @throws AlignmentException
	 * @throws JWNLException
	 * @throws IOException
	   Nov 26, 2018
	 * @throws OWLOntologyCreationException 
	 */
	public static URIAlignment detectSynonymTermsMismatches (BasicAlignment inputAlignment, File ontoFile1, File ontoFile2, File vectorFile) throws AlignmentException, JWNLException, IOException, OWLOntologyCreationException {
		URIAlignment extendedAlignment = new URIAlignment();

		//need to copy the URIs from the input alignment to the new alignment
		URI onto1URI = inputAlignment.getOntology1URI();
		URI onto2URI = inputAlignment.getOntology2URI();
		extendedAlignment.init(onto1URI, onto2URI, A5AlgebraRelation.class, BasicConfidence.class);

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto1 = manager.loadOntologyFromOntologyDocument(ontoFile1);
		OWLOntology onto2 = manager.loadOntologyFromOntologyDocument(ontoFile2);
		Map<String, ArrayList<Double>> vectorMap = VectorExtractor.createVectorMap(vectorFile);

		for (OWLClass ci : onto1.getClassesInSignature()) {
			for (OWLClass cj : onto2.getClassesInSignature()) {
				double propSim = 0;
				double synSim = 0;

				Set<String> synsets_ci = WordNet.getSynonymSet(ci.getIRI().getFragment().toLowerCase());
				Set<String> synsets_cj = WordNet.getSynonymSet(cj.getIRI().getFragment().toLowerCase());
				
				Set<OWLObjectProperty> props_ci = OntologyOperations.getObjectProperties(onto1, ci);
				Set<OWLObjectProperty> props_cj = OntologyOperations.getObjectProperties(onto2, cj);				

				if (!props_ci.isEmpty() && !props_cj.isEmpty()) {
					propSim = getPropSim(onto1, onto2, props_ci, props_cj, vectorMap);
				}

				if (!synsets_ci.isEmpty() && !synsets_cj.isEmpty()) {
					synSim = Jaccard.jaccardSetSim(synsets_ci, synsets_cj);

				}
				
				if (synSim > 0.1) {
					System.err.println(ci + " and " + cj + " have similar synonyms!");
				}

				if (synSim > 0.1 && propSim > 0.6) {

					//cell strength is (synSim+propSim)/2
					extendedAlignment.addAlignCell(ci.getIRI().toURI(), cj.getIRI().toURI(), "=", propSim);
				}
			}


		}

		System.out.println("The synonym terms detection identified " + extendedAlignment.nbCells() + " additional relations");

		//combine the input alignment and the extended alignment
		//extendedAlignment.ingest(inputAlignment);

		return extendedAlignment;
	}

	/**
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
	private static double getPropSim(OWLOntology onto1, OWLOntology onto2, Set<OWLObjectProperty> propSet1, Set<OWLObjectProperty> propSet2, Map<String, ArrayList<Double>> vectorMap) throws AlignmentException, IOException {

		double sim = 0;

		//get alignment from PropString
		double propStringSim = 0;
		int counter = 0;
		double embeddingSim = 0;
		int counter2 = 0;

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

				//get sim using word embeddings				
				//get definitions for ci
				Set<String> op_ci_defs = OntologyOperations.getOPDefinitions(onto1, op_ci);
				
				Set<OWLAnnotation> op_ci_def = op_ci.getAnnotations(onto1);

				for (OWLAnnotation s : op_ci_def) {
					ops1SB.append(StringUtilities.removeStopWords(s.getValue().toString().replaceAll("[^a-zA-Z0-9\\s]", ""))); //remove all non-word characters/symbols
				}

				//get definitions for cj
				Set<String> op_cj_defs = OntologyOperations.getOPDefinitions(onto2, op_cj);
				Set<OWLAnnotation> op_cj_def = op_cj.getAnnotations(onto2);

				for (OWLAnnotation s : op_cj_def) {
					ops2SB.append(StringUtilities.removeStopWords(s.getValue().toString().replaceAll("[^a-zA-Z0-9\\s]", ""))); //remove all non-word characters/symbols
				}

				System.out.println("Finding embeddings for " + ops1SB.toString() + " AND " + ops2SB.toString());
				//Have to ensure that neither of the stringbuilders are empty
				if (ops1SB.toString().trim().length() > 0 && ops2SB.toString().trim().length() > 0) {
					embeddingSim += VectorSim.computeDefSim(ops1SB.toString(), ops2SB.toString(), vectorMap);
				} else {
					embeddingSim = 0;
				}
				counter2++;
			}
		}

		//get the average propStringSim
		propStringSim = propStringSim/counter;
		//System.out.println("Test: average propStringSim is " + propStringSim);

		//get the average embeddingSim
		embeddingSim = embeddingSim/counter2;
		System.out.println("Test: average embeddingSim is " + embeddingSim);

				sim = (propStringSim + embeddingSim)/2;
				System.out.println("Test: sim (propStringSim and embeddingSim) is " + sim);

		return sim;
	}

	public static double embeddingSim(OWLOntology onto1, OWLOntology onto2, Set<OWLObjectProperty> propSet1, Set<OWLObjectProperty> propSet2) throws IOException, AlignmentException {
		Map<String, ArrayList<Double>> vectorMap = VectorExtractor.createVectorMap(vectorFile);
		

		Set<String> op_ci_defs = null;
		Set<String> op_cj_defs = null;
		double embeddingSim = 0;
		int counter = 0;

		for (OWLObjectProperty op_ci : propSet1) {
			for (OWLObjectProperty op_cj : propSet2) {
				counter++;
				
				StringBuilder ops1SB = new StringBuilder();
				StringBuilder ops2SB = new StringBuilder();
				
				op_ci_defs = OntologyOperations.getOPDefinitions(onto1, op_ci);
				for (String s : op_ci_defs) {
					ops1SB.append(StringUtilities.removeStopWords(s.replaceAll("[^a-zA-Z0-9\\s]", ""))); //remove all non-word characters/symbols
				}
				
				op_cj_defs = OntologyOperations.getOPDefinitions(onto2, op_cj);
				for (String s : op_cj_defs) {
					ops2SB.append(StringUtilities.removeStopWords(s.replaceAll("[^a-zA-Z0-9\\s]", ""))); //remove all non-word characters/symbols
				}
				
				//Have to ensure that neither of the stringbuilders are empty
				if (ops1SB.toString().trim().length() > 0 && ops2SB.toString().trim().length() > 0) {

					embeddingSim += VectorSim.computeDefSim(ops1SB.toString(), ops2SB.toString(), vectorMap);
				} else {
					embeddingSim = 0;
				}
			}
		}

		return (double)embeddingSim/(double) counter;
	}
	
	
	public static double getPropStringSim (OWLOntology onto1, OWLOntology onto2) throws AlignmentException {
		double sim = 0;
		
		File propStringAlignmentFile = new File("./files/propstringalignment.rdf");
		AlignmentParser parser = new AlignmentParser();
		BasicAlignment propStringAlignment = (BasicAlignment) parser.parse(propStringAlignmentFile.toURI().toString());
		
		double propStringSim = 0;
		int counter = 0;

		for (OWLClass ci : onto1.getClassesInSignature()) {
			for (OWLClass cj : onto2.getClassesInSignature()) {
				Set<OWLObjectProperty> props_ci = OntologyOperations.getObjectProperties(onto1, ci);
				Set<OWLObjectProperty> props_cj = OntologyOperations.getObjectProperties(onto2, cj);	
				for (OWLObjectProperty op_ci : props_ci) {
					for (OWLObjectProperty op_cj : props_cj) {

						for (Cell c : propStringAlignment) {
							if (c.getObject1AsURI().getFragment().toLowerCase().equals(op_ci.getIRI().getFragment().toLowerCase()) || 
									c.getObject1AsURI().getFragment().toLowerCase().equals(op_cj.getIRI().getFragment().toLowerCase())
									&& 
									c.getObject2AsURI().getFragment().toLowerCase().equals(op_ci.getIRI().getFragment().toLowerCase()) || 
									c.getObject2AsURI().getFragment().toLowerCase().equals(op_cj.getIRI().getFragment().toLowerCase())) {
								propStringSim = c.getStrength();

							} else {
								propStringSim = 0;
							}

						

						counter++;		

					}
				}
			}
				sim = propStringSim/counter;
			}
		}
		
		return sim;
	
	}

	public static BasicAlignment propStringAlignment(OWLOntology onto1, OWLOntology onto2) throws AlignmentException {
		BasicAlignment propStringAlignment = new BasicAlignment();
		File propStringAlignmentFile = new File("./files/propstringalignment.rdf");
		AlignmentParser parser = new AlignmentParser();
		BasicAlignment inputAlignment = (BasicAlignment) parser.parse(propStringAlignmentFile.toURI().toString());
		double propStringSim = 0;
		int counter = 0;

		for (OWLClass ci : onto1.getClassesInSignature()) {
			for (OWLClass cj : onto2.getClassesInSignature()) {
				Set<OWLObjectProperty> props_ci = OntologyOperations.getObjectProperties(onto1, ci);
				Set<OWLObjectProperty> props_cj = OntologyOperations.getObjectProperties(onto2, cj);	
				for (OWLObjectProperty op_ci : props_ci) {
					for (OWLObjectProperty op_cj : props_cj) {

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

					}
				}
			}
		}
		return propStringAlignment;
	}

}

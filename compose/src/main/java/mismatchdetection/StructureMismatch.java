package mismatchdetection;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import com.google.common.base.Joiner;

import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.BasicConfidence;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.rel.A5AlgebraRelation;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import net.didion.jwnl.JWNLException;
import utilities.AlignmentOperations;
import utilities.Jaccard;
import utilities.OntologyOperations;
import utilities.StringUtilities;
import utilities.WordNet;

public class StructureMismatch {
	
	public static void main(String[] args) throws IOException, AlignmentException, OWLOntologyCreationException, JWNLException {
		/* ATMONTO-AIRM DATASET */
		File inputAlignmentFile = new File("./files/ESWC_ATMONTO_AIRM/Evaluation/ATMONTO-AIRM/aml-atmonto-airm.rdf");
		String output = "./files/ESWC_ATMONTO_AIRM/Evaluation/ATMONTO-AIRM/structureMismatchAlignment.rdf";
//		
		File ontoFile1 = new File("./files/ESWC_ATMONTO_AIRM/ATMOntoCoreMerged.owl");
		File ontoFile2 = new File("./files/ESWC_ATMONTO_AIRM/airm-mono.owl");
		
		/* BIBFRAME-SCHEMA.ORG DATASET */
//		File inputAlignmentFile = new File("./files/ESWC_ATMONTO_AIRM/Evaluation/Bibframe-Schemaorg/bibframe-schemaorg-aml.rdf");
//		String output = "./files/ESWC_ATMONTO_AIRM/Evaluation/Bibframe-Schemaorg/structureMismatchAlignment.rdf";		
//		File ontoFile1 = new File("./files/ESWC_ATMONTO_AIRM/bibframe.rdf");
//		File ontoFile2 = new File("./files/ESWC_ATMONTO_AIRM/schema-org.owl");
		
		/* SWEET-ATMONTO DATASET */
//		File inputAlignmentFile = new File("./files/ESWC_ATMONTO_AIRM/Evaluation/Envo-Sweet/envo-sweet-aml.rdf");
//		String output = "./files/ESWC_ATMONTO_AIRM/Evaluation/Envo-Sweet/structureMismatchAlignment.rdf";		
//		File ontoFile1 = new File("./files/ESWC_ATMONTO_AIRM/envo.owl");
//		File ontoFile2 = new File("./files/ESWC_ATMONTO_AIRM/sweet.owl");
		
		AlignmentParser parser = new AlignmentParser();
		BasicAlignment amlAlignment = (BasicAlignment) parser.parse(inputAlignmentFile.toURI().toString());
		
		System.out.println("The input alignment contains " + amlAlignment.nbCells() + " relations");
		
		URIAlignment structureMismatchAlignment = detectStructureMismatches(amlAlignment, ontoFile1, ontoFile2);
		
		System.out.println("The structure mismatch alignment contains " + structureMismatchAlignment.nbCells() + " relations");

		//write produced alignment to file
		PrintWriter writer = new PrintWriter(
				new BufferedWriter(
						new FileWriter(output)), true); 
		AlignmentVisitor renderer = new RDFRendererVisitor(writer);

		structureMismatchAlignment.render(renderer);
		
		writer.flush();
		writer.close();
	}
	
	/** TODO: Remove the detected mismatch relations from the input alignment
	 * Iterates through relations in an already produced alignment, retrieves the context (domain and range classes linked to object properties of each pair of related classes), and measures the similarity between these
	 * contexts using Jaccard similarity.
	 * @param inputAlignmentFile
	 * @param onto1 source ontology
	 * @param onto2 target ontology
	 * @return an alignment that includes relations with context similarity above a certain threshold
	 * @throws AlignmentException
	   Nov 26, 2018
	 * @throws OWLOntologyCreationException 
	 * @throws JWNLException 
	 * @throws FileNotFoundException 
	 */
	public static URIAlignment detectStructureMismatches (BasicAlignment inputAlignment, File ontoFile1, File ontoFile2) throws AlignmentException, OWLOntologyCreationException, FileNotFoundException, JWNLException {
		URIAlignment structureMismatchAlignment = new URIAlignment();
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto1 = manager.loadOntologyFromOntologyDocument(ontoFile1);
		OWLOntology onto2 = manager.loadOntologyFromOntologyDocument(ontoFile2);
		
		//need to copy the URIs from the input alignment to the new alignment
		URI onto1URI = inputAlignment.getOntology1URI();
		URI onto2URI = inputAlignment.getOntology2URI();
		structureMismatchAlignment.init(onto1URI, onto2URI, A5AlgebraRelation.class, BasicConfidence.class);
		
		double sim = 0;


		for (Cell c : inputAlignment) {

			Set<OWLClass> contextC1 = OntologyOperations.getClassesTwoStepsAway(onto1, OntologyOperations.getClass(c.getObject1AsURI().getFragment(), onto1));

			Set<OWLClass> contextC2 = OntologyOperations.getClassesTwoStepsAway(onto2, OntologyOperations.getClass(c.getObject2AsURI().getFragment(), onto2));
			
			System.err.println("\nMatching the context classes of " + c.getObject1AsURI().getFragment() + " and " + c.getObject2AsURI().getFragment());
			
			//sim = simpleSimLabels(contextC1, contextC2, onto1, onto2);
			sim = simpleSim(contextC1, contextC2);
			if (sim == 0) {
				structureMismatchAlignment.addAlignCell(c.getObject1(), c.getObject2(), c.getRelation().getRelation(), sim);
			}
			
//			System.out.println("\ncontextC1 has " + contextC1.size() + " classes");
//			System.out.println("contextC1:");
//			for (OWLClass c1 : contextC1) {
//				System.out.println(c1.getIRI().getFragment());
//			}
//			System.out.println("\ncontextC2 has " + contextC2.size() + " classes");
//			for (OWLClass c2 : contextC2) {
//				System.out.println(c2.getIRI().getFragment());
//			}
			
			/* IN CASE WU-PALMER SHOULD BE USED */
			//sim = wordNetSim(contextC1, contextC2);
			//System.out.println("The sim between " + c.getObject1AsURI().getFragment() + " and " + c.getObject2AsURI().getFragment() + " is " + sim) ;
			//using this tweak to avoid out-of-boundary exceptions from the alignment api
//			if (sim > 0 && sim < 1.0) {
//				structureMismatchAlignment.addAlignCell(c.getObject1(), c.getObject2(), c.getRelation().getRelation(), sim);
//			}
		}
		
		System.out.println("The structure mismatch algorithm detected " +  structureMismatchAlignment.nbCells() + " mismatch relations");
		
		BasicAlignment filteredAlignment = AlignmentOperations.createDiffAlignment(inputAlignment, structureMismatchAlignment);
		
		//return (URIAlignment) structureMismatchAlignment;
		return (URIAlignment) filteredAlignment;

	}
	
	public static double wordNetSim (Set<OWLClass> set1, Set<OWLClass> set2) throws FileNotFoundException, JWNLException {
		double sim = 0;

		ArrayList<Double> simList = new ArrayList<Double>();
				
		for (OWLClass ci : set1) {
			for (OWLClass cj : set2) {
					double wupScore = WordNet.computeWuPalmer(ci.getIRI().getFragment().toLowerCase(), cj.getIRI().getFragment().toLowerCase());
					
					if (wupScore > 0) {
					simList.add(wupScore);	
					}
		
			}
			}
		
		System.out.println("There are " + simList.size() + " similarity scores in the arraylist");
		int listSize = simList.size();
		double totals = 0;
		for (Double d : simList) {
			totals += d;
		}
		sim = totals / (double) listSize;

		return sim;
	}
	
	public static double simpleSim (Set<OWLClass> set1, Set<OWLClass> set2) {

		int counter = 0;
		
		for (OWLClass ci : set1) {
			for (OWLClass cj : set2) {
				if (ci.getIRI().getFragment().equals(cj.getIRI().getFragment())) {
					//System.out.println(ci + " and " + cj + " are equal");
					counter++;
				} else {
					//System.out.println(ci + " and " + cj + " are NOT equal");
				}
			}
		}
		System.out.println("Sim is " + (double)counter/10);
		return ((double)counter/10);
		
	}
	
	public static double simpleSimLabels (Set<OWLClass> set1, Set<OWLClass> set2, OWLOntology onto1, OWLOntology onto2) {

		int counter = 0;
		
		for (OWLClass ci : set1) {
			for (OWLClass cj : set2) {
					if (OntologyOperations.getLabel(ci, onto1).equals(OntologyOperations.getLabel(cj, onto2))) {
					System.out.println(OntologyOperations.getLabel(ci, onto1) + " and " + OntologyOperations.getLabel(cj, onto2) + " are equal");
					counter++;
				} else {
					System.out.println(OntologyOperations.getLabel(ci, onto1) + " and " + OntologyOperations.getLabel(cj, onto2)+ " are NOT equal");
				}
			}
		}
		System.out.println("Sim is " + (double)counter/10);
		return ((double)counter/10);
		
	}

	/**
	 * jaccardSetSim = [number of common elements] / [total num elements] - [number of common elements]
	 * @param s1
	 * @param s2
	 * @return
	 */
	public static double jaccardSetSim (Set<OWLClass> set1, Set<OWLClass> set2) {
		

		int intersection = 0;
		
		for (OWLClass s1 : set1) {
			for (OWLClass s2 : set2) {
				//System.out.println("Does " + s1.getIRI().getFragment() + " match " + s2.getIRI().getFragment() + "?");
				if (s1.equals(s2)) {
					intersection += 1;
				}
			}
		}

		int union = (set1.size() + set2.size()) - intersection;
		
		double jaccardSetSim = (double) intersection / (double) union;
		
		System.err.println("The JaccardSetSim is " + jaccardSetSim);
		
		return jaccardSetSim;
	}
	
	/* BACKUP */
	/*
	 * 
	/**
	 * Computes the Jaccard similarity (intersection over union) between two definitions
	 * @param s1 source definition (i.e. sets of tokens)
	 * @param s2 target definition (i.e. sets of tokens)
	 * @return a measure stating how similar two definitions are
	 * @throws IOException
	   Nov 26, 2018
	 */
//	
//
//	
	/**
	 * Measures similarity between two sets of synonyms associated with classes ci and cj
	 * @param ci the source class
	 * @param cj the target class
	 * @return a measure stating the similarity between two sets of synonyms
	 * @throws FileNotFoundException
	 * @throws JWNLException
	   Nov 26, 2018
	 */
	private static double measureSynSim(OWLClass ci, OWLClass cj) throws FileNotFoundException, JWNLException {

		double sim = 0;
		Set<String> synSet_ci = new HashSet<String>();
		Set<String> synSet_cj = new HashSet<String>();

		if (WordNet.containedInWordNet(ci.getIRI().getFragment().toLowerCase()) && 
				WordNet.containedInWordNet(cj.getIRI().getFragment().toLowerCase())) {

			synSet_ci = WordNet.getSynonymSet(ci.getIRI().getFragment().toLowerCase());
			synSet_cj = WordNet.getSynonymSet(cj.getIRI().getFragment().toLowerCase());

		}

		sim = Jaccard.jaccardSetSim(synSet_ci, synSet_cj);
		
		System.err.println("The measureSynSim is " + sim);

		return sim;

	}
//	
//
//	
	private static double computeJaccardSim(String s1, String s2) throws IOException {

		double sim = 0;

		//remove stopwords
		String s1WOStop = StringUtilities.removeStopWords(s1);
		String s2WOStop = StringUtilities.removeStopWords(s2);

		String[] s1Array = s1WOStop.split(" ");
		String[] s2Array = s2WOStop.split(" ");		

		Set<String> s1Set = new HashSet<String>(Arrays.asList(s1Array));
		Set<String> s2Set = new HashSet<String>(Arrays.asList(s2Array));

		sim = Jaccard.jaccardSetSim(s1Set, s2Set);

		return sim;

	}
///**
//	 * Given an OWLClass cls (belonging to OWLOntology onto) classes linked to this class via object properties are retrieved and put into a set.
//	 * @param onto the ontology holding the class cls
//	 * @param cls the class for which "context" will be retrieved
//	 * @return a context set containing domain and range classes linked to cls via object properties
//	   Nov 26, 2018
//	 */
//	private static Set<String> getContext (OWLOntology onto, OWLClass cls) {
//		Set<String> context = new HashSet<String>();
//
//		Set<OWLObjectProperty> ops_whereDomain = OntologyOperations.getObjectPropertiesDomain(onto, cls); 
//
//		for (OWLObjectProperty op : ops_whereDomain) {
//			context.addAll(OntologyOperations.getRangeClassesString(onto, op));
//		}
//
//		Set<OWLObjectProperty> ops_whereRange = OntologyOperations.getObjectPropertiesRange(onto, cls); 
//		for (OWLObjectProperty op : ops_whereRange) {
//			context.addAll(OntologyOperations.getDomainClassesString(onto, op));
//		}
//
//		return context;
//	}
/** TODO: Implement so that an input alignment is parameter and all relations from it are iterated in the beginning of the code, 
	 * 
	 * @param onto1 the source ontology
	 * @param onto2 the target ontology
	 * @return the input alignment without detected structure mismatch relations
	 * @throws IOException
	 * @throws JWNLException
	   Nov 26, 2018
	 */
	public static Set<String> detectStructureMismatches2(OWLOntology onto1, OWLOntology onto2) throws IOException, JWNLException {
		Set<String> a_filtered = new HashSet<String>();
		double threshold = 0.3;

		Set<OWLObjectProperty> propSet_ci = new HashSet<OWLObjectProperty>();
		Set<OWLObjectProperty> propSet_cj = new HashSet<OWLObjectProperty>();

		//iterate all classes in both ontologies
		for (OWLClass ci : onto1.getClassesInSignature()) {
			for (OWLClass cj : onto2.getClassesInSignature()) {

				if (ci.getIRI().getFragment().equals(cj.getIRI().getFragment()) || 
						measureSynSim(ci, cj) > threshold) {
					propSet_ci = OntologyOperations.getObjectProperties(onto1, ci);
					propSet_cj = OntologyOperations.getObjectProperties(onto2, cj);

					for (OWLObjectProperty op_ci : propSet_ci) {
						for (OWLObjectProperty op_cj : propSet_cj) {

							//check if their domain classes are equal							
							for (String domain_op_ci : OntologyOperations.getDomainClassesString(onto1, op_ci)) {
								for (String domain_op_cj : OntologyOperations.getDomainClassesString(onto2, op_cj)) {
									if (domain_op_ci.equals(domain_op_cj)) {
										//then their range classes...
										for (String range_op_ci : OntologyOperations.getRangeClassesString(onto1, op_ci)) {
											for (String range_op_cj : OntologyOperations.getRangeClassesString(onto2, op_cj)) {
												if (range_op_ci.equals(range_op_cj) && measureLinguisticSemInt(onto1, onto2, op_ci, op_cj) == false) {
													System.out.println(op_ci + " and " + op_cj + " have the same domain and range class");
													a_filtered.add("ATMONTO: " + ci + " - AIRM-O: " + cj);
												} else {
													//break;
												}

											}
										}
									}
								}
							}

						}
					}
				}
			}
		}


		return a_filtered;
	}



	private static boolean measureLinguisticSemInt(OWLOntology onto1, OWLOntology onto2, OWLObjectProperty op_ci, OWLObjectProperty op_cj) throws IOException {
		boolean similar = false;

		//get the domain class(es)
		Set<String> op_ci_domains = OntologyOperations.getDomainClassesString(onto1, op_ci);
		Set<String> op_cj_domains = OntologyOperations.getDomainClassesString(onto2, op_cj);

		//get definitions of domain classes
		Set<String> defs_op_ci_domains = OntologyOperations.getOPDefinitions(onto1, op_ci);
		Set<String> defs_op_cj_domains = OntologyOperations.getOPDefinitions(onto1, op_ci);

		//get the range class(es)
		Set<String> op_ci_ranges = OntologyOperations.getRangeClassesString(onto1, op_ci);
		Set<String> op_cj_ranges = OntologyOperations.getRangeClassesString(onto2, op_cj);

		//get definitions of range classes

		//get synonyms from WordNet
		String[] syns_op_ci = WordNet.getSynonyms(op_ci.getIRI().getFragment());
		String[] syns_op_cj = WordNet.getSynonyms(op_cj.getIRI().getFragment());

		//join the items (string) from all above sets using the Joiner from Guava. This string represents 
		//positive evidence, that is, those features that suggest similarity if common items are found among the properties
		String positive_evidence_op_ci = Joiner.on(" ").join(op_ci_domains, defs_op_ci_domains, op_ci_ranges, syns_op_ci).replaceAll("[^a-zA-Z0-9]", " ");
		System.out.println("Testing the positive_evidence_op_ci");
		System.out.println(positive_evidence_op_ci);
		String positive_evidence_op_cj = Joiner.on(" ").join(op_cj_domains, defs_op_cj_domains, op_cj_ranges, syns_op_cj).replaceAll("[^a-zA-Z0-9]", " ");

		//tokenisation is taken care of by the JaccardSim, so all we have to do is provide it with two strings for which their
		//set similarity will be measured
		double jaccardSimPositiveEvidence = computeJaccardSim(positive_evidence_op_ci, positive_evidence_op_cj);


		//get hyponyms
		String[] hyponyms_op_ci = WordNet.getHyponyms(op_ci.getIRI().getFragment());
		String[] hyponyms_op_cj = WordNet.getHyponyms(op_cj.getIRI().getFragment());

		//get hyperonyms
		String[] hyperonyms_op_ci = WordNet.getHypernyms(op_ci.getIRI().getFragment());
		String[] hyperonyms_op_cj = WordNet.getHypernyms(op_cj.getIRI().getFragment());

		//get meronyms
		String[] meronyms_op_ci = WordNet.getMeronyms(op_ci.getIRI().getFragment());
		String[] meronyms_op_cj = WordNet.getMeronyms(op_cj.getIRI().getFragment());

		//join the items (string) from all above sets using the Joiner from Guava. This string represents 
		//negative evidence, that is, those features that suggest dissimilarity if common items are found among the properties
		String negative_evidence_op_ci = Joiner.on(" ").join(hyponyms_op_ci, hyperonyms_op_ci, meronyms_op_ci).replaceAll("[^a-zA-Z0-9]", " ");
		String negative_evidence_op_cj = Joiner.on(" ").join(hyponyms_op_cj, hyperonyms_op_cj, meronyms_op_cj).replaceAll("[^a-zA-Z0-9]", " ");

		//tokenisation is taken care of by the JaccardSim, so all we have to do is provide it with two strings for which their
		//set similarity will be measured
		double jaccardSimNegativeEvidence = computeJaccardSim(negative_evidence_op_ci, negative_evidence_op_cj);

		if (jaccardSimPositiveEvidence > jaccardSimNegativeEvidence) {
			similar = true;
		} else {
			similar = false;
		}

		return similar;
	}

}

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.util.Version;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;

import com.google.common.base.Joiner;

import wordembedding.VectorExtractor;
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.BasicConfidence;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.rel.A5AlgebraRelation;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import fr.inrialpes.exmo.ontosim.vector.CosineVM;
import fr.inrialpes.exmo.ontowrap.OntowrapException;
import net.didion.jwnl.JWNLException;
import utilities.AlignmentOperations;
import utilities.Jaccard;
import utilities.MathUtils;
import utilities.OntologyOperations;
import utilities.StringUtilities;
import utilities.WordNet;
import wordembedding.VectorConcept;

public class MismatchDetection {


	private static File vectorFile = new File("./files/skybrary_trained.txt");


	public static void main(String[] args) throws OntowrapException, IOException, OWLOntologyCreationException, AlignmentException, JWNLException {
/*
		File inputAlignmentFile = new File("./files/ESWC_ATMONTO_AIRM/v2-19102018/Equivalence/AML-ATMONTO-AIRM-05.rdf");
		File ontoFile1 = new File("./files/ESWC_ATMONTO_AIRM/ATMOntoCoreMerged.owl");
		File ontoFile2 = new File("./files/ESWC_ATMONTO_AIRM/airm-mono.owl");
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto1 = manager.loadOntologyFromOntologyDocument(ontoFile1);
		OWLOntology onto2 = manager.loadOntologyFromOntologyDocument(ontoFile2);

		 Testing the measureContextInt method 
		//		BasicAlignment filteredAlignment = measureContextInt (inputAlignmentFile, onto1, onto2);

		 Testing addSynonymTermsMismatches 
		//		BasicAlignment extendedAlignment = addSynonymTermsMismatches(onto1, onto2);
		//		System.out.println("Test: extendedAlignment contains " + extendedAlignment.nbCells() + " cells");
		//		for (Cell c : extendedAlignment) {
		//			System.out.println(c.getObject1() + " - " + c.getObject2());
		//		}

		 Testing removeStructureMismatches 
		//		Set<String> structureMismatches = removeStructureMismatches(onto1, onto2);
		//		System.out.println("Structure Mismatches identified");
		//		for (String s : structureMismatches) {
		//			System.out.println(s);
		//		}

		 Testing wordNetRepresentation 
		//		System.out.println("WordNet contains synsets for " + wordNetRepresentation(onto1, onto2) + " classes");

		 Testing detectGranularityMismatches() 
		Set<String> granularityMismatches = detectGranularityMismatches(inputAlignmentFile, onto1, onto2);
		System.out.println("The granularity mismatches are: ");
		for (String s : granularityMismatches) {
			System.out.println(s);
		}
		
		 Testing numSynsets 
		String word = "book";
		
		System.out.println("Test: " + word + " has " + getNumSynsets(word) + " senses");
		
		 Testing detectCategorisationMismatches 
		detectCategorisationMismatches();
		
		*/
		
		/* Testing concept scope mismatches */
		//public static BasicAlignment detectConceptScopeMismatch(BasicAlignment inputAlignment) throws AlignmentException {
		File amlAlignmentFile = new File("./files/ESWC_ATMONTO_AIRM/aml-atmonto-airm.rdf");
		String output = "./files/ESWC_ATMONTO_AIRM/conceptscopemismath.rdf";
		
		AlignmentParser parser = new AlignmentParser();
		BasicAlignment amlAlignment = (BasicAlignment) parser.parse(amlAlignmentFile.toURI().toString());
		System.out.println("amlAlignment contains " + amlAlignment.nbCells() + " cells");
		URIAlignment conceptScopeMismatches = detectConceptScopeMismatch(amlAlignment);
		
		System.out.println("conceptScopeMismatches contains " + conceptScopeMismatches.nbCells() + " cells");
		
		PrintWriter writer = new PrintWriter(
				new BufferedWriter(
						new FileWriter(output)), true); 
		AlignmentVisitor renderer = new RDFRendererVisitor(writer);

		conceptScopeMismatches.render(renderer);
		
		writer.flush();
		writer.close();
		
		BasicAlignment testAlignment = AlignmentOperations.createDiffAlignment(amlAlignment, conceptScopeMismatches);
		System.out.println("testAlignment contains " + testAlignment.nbCells() + " cells");


	}
	
	public static Set<String> detectCategorisationMismatches(/*OWLOntology onto1, OWLOntology onto2*/) {
		Set<String> categorisationMismatches = new HashSet<String>();
		
		String cls1 = "animal";		
		String subcls1_1 = "mammal";
		String subcls1_2 = "bird";
		
		String cls2 = "animal";
		String subcls2_1 = "carnivore";
		String subcls2_2 = "herbivore";
		
		System.out.println("Sim between " + subcls1_1 + " and " + subcls2_1 + " using LESK: " + WordNet.computeLESK(subcls1_1, subcls2_1));
		System.out.println("Sim between " + subcls1_1 + " and " + subcls2_2 + " using LESK: " + WordNet.computeLESK(subcls1_1, subcls2_2));
		System.out.println("Sim between " + subcls1_2 + " and " + subcls2_1 + " using LESK: " + WordNet.computeLESK(subcls1_2, subcls2_1));
		System.out.println("Sim between " + subcls1_2 + " and " + subcls2_2 + " using LESK: " + WordNet.computeLESK(subcls1_2, subcls2_2));
		
		System.out.println("Sim between " + subcls1_1 + " and " + subcls2_1 + " using Jiang-Conrath: " + WordNet.computeJiangConrath(subcls1_1, subcls2_1));
		System.out.println("Sim between " + subcls1_1 + " and " + subcls2_2 + " using Jiang-Conrath: " + WordNet.computeJiangConrath(subcls1_1, subcls2_2));
		System.out.println("Sim between " + subcls1_2 + " and " + subcls2_1 + " using Jiang-Conrath: " + WordNet.computeJiangConrath(subcls1_2, subcls2_1));
		System.out.println("Sim between " + subcls1_2 + " and " + subcls2_2 + " using Jiang-Conrath: " + WordNet.computeJiangConrath(subcls1_2, subcls2_2));
		
		System.out.println("Sim between " + subcls1_1 + " and " + subcls2_1 + " using Lin: " + WordNet.computeLin(subcls1_1, subcls2_1));
		System.out.println("Sim between " + subcls1_1 + " and " + subcls2_2 + " using Lin: " + WordNet.computeLin(subcls1_1, subcls2_2));
		System.out.println("Sim between " + subcls1_2 + " and " + subcls2_1 + " using Lin: " + WordNet.computeLin(subcls1_2, subcls2_1));
		System.out.println("Sim between " + subcls1_2 + " and " + subcls2_2 + " using Lin: " + WordNet.computeLin(subcls1_2, subcls2_2));
		
		System.out.println("Sim between " + subcls1_1 + " and " + subcls2_1 + " using WUP: " + WordNet.computeWuPalmer(subcls1_1, subcls2_1));
		System.out.println("Sim between " + subcls1_1 + " and " + subcls2_2 + " using WUP: " + WordNet.computeWuPalmer(subcls1_1, subcls2_2));
		System.out.println("Sim between " + subcls1_2 + " and " + subcls2_1 + " using WUP: " + WordNet.computeWuPalmer(subcls1_2, subcls2_1));
		System.out.println("Sim between " + subcls1_2 + " and " + subcls2_2 + " using WUP: " + WordNet.computeWuPalmer(subcls1_2, subcls2_2));

		return categorisationMismatches;
	}

	/** TODO: We need to defined criteria for determining the threshold for stating that two classes really are a granularity mismatch on the basis of their properties and subclasses.
	 * Detects "granularity mismatches" by comparing the number of data properties, object properties, and subclasses of two classes forming a relation in an input alignment.
	 * @param inputAlignmentFile an already computed alignment holding a set of relations
	 * @param onto1 the source ontology
	 * @param onto2 the target ontology
	 * @return the input alignment - detected granularity mismatches
	 * @throws AlignmentException
	   Nov 26, 2018
	 */
	public static Set<String> detectGranularityMismatches(File inputAlignmentFile, OWLOntology onto1, OWLOntology onto2) throws AlignmentException {
		Set<String> granularityMismatches = new HashSet<String>();
		AlignmentParser parser = new AlignmentParser();
		BasicAlignment inputAlignment = (BasicAlignment) parser.parse(inputAlignmentFile.toURI().toString());

		for (Cell c : inputAlignment) {
			System.out.println("\nTesting " + c.getObject1AsURI().getFragment() + " - " + c.getObject2AsURI().getFragment());
			Set<OWLObjectProperty> ops1 = OntologyOperations.getObjectProperties(onto1, OntologyOperations.getClass(c.getObject1AsURI().getFragment(), onto1));
			for (OWLObjectProperty op : ops1) {
				System.out.println(op);
			}
			
			Set<OWLObjectProperty> ops2 = OntologyOperations.getObjectProperties(onto2, OntologyOperations.getClass(c.getObject2AsURI().getFragment(), onto2));
			for (OWLObjectProperty op : ops2) {
				System.out.println(op);
			}
			
			Set<OWLDataProperty> dps1 = OntologyOperations.getDataProperties(onto1, OntologyOperations.getClass(c.getObject1AsURI().getFragment(), onto1));
			for (OWLDataProperty dp : dps1) {
				System.out.println(dp);
			}
			
			Set<OWLDataProperty> dps2 = OntologyOperations.getDataProperties(onto2, OntologyOperations.getClass(c.getObject2AsURI().getFragment(), onto2));
			for (OWLDataProperty dp : dps2) {
				System.out.println(dp);
			}
			
			Set<String> cls1 = OntologyOperations.getEntitySubclasses(onto1, OntologyOperations.getClass(c.getObject1AsURI().getFragment(), onto1));
			Set<String> cls2 = OntologyOperations.getEntitySubclasses(onto2, OntologyOperations.getClass(c.getObject2AsURI().getFragment(), onto2));
			
		
			System.out.println("There are " + ops1.size() + " object properties for " + c.getObject1AsURI().getFragment());
			System.out.println("There are " + ops2.size() + " object properties for " + c.getObject2AsURI().getFragment());
			
			System.out.println("There are " + dps1.size() + " data properties for " + c.getObject1AsURI().getFragment());
			System.out.println("There are " + dps2.size() + " data properties for " + c.getObject2AsURI().getFragment());
			
			System.out.println("There are " + cls1.size() + " subclasses for " + c.getObject1AsURI().getFragment());
			System.out.println("There are " + cls2.size() + " subclasses for " + c.getObject2AsURI().getFragment());
			
			/* Weighting properties and subclasses */
			//object properties are weighted 1.5
			double opWeight1 = ops1.size() * 1.5;
			double opWeight2 = ops2.size() * 1.5;
			
			//data properties are weighted 1.2
			double dpWeight1 = dps1.size() * 1.2;
			double dpWeight2 = dps2.size() * 1.2;
			
			//subclasses are weighted 1.5
			double subclsWeight1 = cls1.size() * 1.5;
			double subclsWeight2 = cls2.size() * 1.5;
			
			double sumWeight1 = opWeight1+dpWeight1+subclsWeight1;
			double sumWeight2 = opWeight2+dpWeight2+subclsWeight2;
			double diffWeight = Math.abs(sumWeight1-sumWeight2);
			double percWeight = diffWeight/(sumWeight1+sumWeight2)*100;
			
			if (percWeight > 25) {
				granularityMismatches.add(c.getObject1AsURI().getFragment() + " - " + c.getObject2AsURI().getFragment());
			}
			
			System.out.println("sumWeight1: " + sumWeight1 + " and sumWeight2: " + sumWeight2);
			
			/* Percentage difference */
//			int diffOps = Math.abs(ops1.size()-ops2.size());
//			int sumOps = ops1.size() + ops2.size();
//			System.out.println("The sum of ops is " + sumOps);
//			System.out.println("The diff of ops is " + diffOps);
//			double percentageOps = ((double)diffOps / (double)sumOps) * 100;
//			System.out.println("The percentage of ops is " + percentageOps);
//			
//			int diffDps = Math.abs(dps1.size()-dps2.size());
//			int sumDps = dps1.size() + dps2.size();
//			System.out.println("The sum of dps is " + sumDps);
//			System.out.println("The diff of dps is " + diffDps);
//			double percentageDps = ((double)diffDps / (double)sumDps) * 100;
//			System.out.println("The percentage of dps is " + percentageDps);
//			
//			int diffcls = Math.abs(cls1.size()-cls2.size());
//			int sumCls = cls1.size() + cls2.size();
//			System.out.println("The sum of cls is " + sumCls);
//			System.out.println("The diff of cls is " + diffcls);
//			double percentageCls = ((double)diffcls / (double)sumCls) * 100;
//			System.out.println("The percentage of cls is " + percentageCls);
//			
//			if (percentageOps <= 50 && percentageCls <= 50 && percentageDps <= 75) {
//				granularityMismatches.add(c.getObject1AsURI().getFragment() + " - " + c.getObject2AsURI().getFragment());
//			}

		}


		return granularityMismatches;
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
	 */
	public static BasicAlignment detectStructureMismatches1 (File inputAlignmentFile, OWLOntology onto1, OWLOntology onto2) throws AlignmentException {

		AlignmentParser parser = new AlignmentParser();
		BasicAlignment inputAlignment = (BasicAlignment) parser.parse(inputAlignmentFile.toURI().toString());

		BasicAlignment filteredAlignment = new BasicAlignment();
		double sim = 0;


		for (Cell c : inputAlignment) {

			Set<String> contextC1 = getContext(onto1, OntologyOperations.getClass(c.getObject1AsURI().getFragment(), onto1));

			Set<String> contextC2 = getContext(onto2, OntologyOperations.getClass(c.getObject2AsURI().getFragment(), onto2));

			sim = Jaccard.jaccardSetSim(contextC1, contextC2);

			if (sim > 0) {
				filteredAlignment.addAlignCell(c.getObject1(), c.getObject2(), c.getRelation().getRelation(), sim);
				System.out.println("\nTest: The context classes of " + c.getObject1AsURI().getFragment() + " consists of: ");
				for (String s : contextC1) {
					System.out.println(s);
				}
				System.out.println("\nTest: The context classes of " + c.getObject2AsURI().getFragment() + " consists of: ");
				for (String s : contextC2) {
					System.out.println(s);
				}

			}

		}

		System.out.println("\nThe filtered alignment consists of: ");
		for (Cell c : filteredAlignment) {
			System.out.println(c.getObject1AsURI().getFragment() + " - " + c.getObject2AsURI().getFragment() + " with context similarity: " + c.getStrength());
		}

		return filteredAlignment;
	}
	
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
							for (String domain_op_ci : OntologyOperations.getDomainClasses(onto1, op_ci)) {
								for (String domain_op_cj : OntologyOperations.getDomainClasses(onto2, op_cj)) {
									if (domain_op_ci.equals(domain_op_cj)) {
										//then their range classes...
										for (String range_op_ci : OntologyOperations.getRangeClasses(onto1, op_ci)) {
											for (String range_op_cj : OntologyOperations.getRangeClasses(onto2, op_cj)) {
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
	
	/** TODO: A good approach for measuring synonym similarity (general classes include a large number of synonyms that may falsely conclude false positives).
	 * Checks if two pairs of classes are equivalent on the basis of the similarity between their synonyms and their properties. The synonym similarity is computed by Jaccard, and the property similarity
	 * is measured using both PropString (syntactic similarity) and word embeddings (semantic similarity)
	 * @param onto1 OWLOntology source ontology
	 * @param onto2 OWLOntology target ontology
	 * @return an alignment holding all relations considered synonym terms "mismatch"
	 * @throws AlignmentException
	 * @throws JWNLException
	 * @throws IOException
	   Nov 26, 2018
	 */
	public static BasicAlignment detectSynonymTermsMismatches (OWLOntology onto1, OWLOntology onto2) throws AlignmentException, JWNLException, IOException {
		BasicAlignment extendedAlignment = new BasicAlignment();
		double threshold = 0.0;


		for (OWLClass ci : onto1.getClassesInSignature()) {
			for (OWLClass cj : onto2.getClassesInSignature()) {
				Set<String> synsets_ci = WordNet.getSynonymSet(ci.getIRI().getFragment().toLowerCase());

				Set<String> synsets_cj = WordNet.getSynonymSet(cj.getIRI().getFragment().toLowerCase());

				Set<OWLObjectProperty> props_ci = OntologyOperations.getObjectProperties(onto1, ci);
				Set<OWLObjectProperty> props_cj = OntologyOperations.getObjectProperties(onto2, cj);

				double propSim = getPropSim(onto1, onto2, props_ci, props_cj);
				double synSim = Jaccard.jaccardSetSim(synsets_ci, synsets_cj);

				if (/*synSim > 0.1 &&*/ propSim > 0.2) {

					extendedAlignment.addAlignCell(ci.getIRI(), cj.getIRI(), "=", 1.0);
				}
			}


		}

		return extendedAlignment;
	}
	
	/** TODO: Remove the detected mismatch relations from the input alignment
	 * Detects "concept scope mismatches" on the basis of the following "compound pattern": the part component of the part-whole relationship includes the name of
	 * its whole as its qualifying compound. For example, an [aircraft]Engine represent a part of aircraft. 
	 * @param inputAlignment an already computed alignment
	 * @return the input alignment - the detected mismatch relations (cells)
	 * @throws AlignmentException
	   Nov 26, 2018
	 */
	public static URIAlignment detectConceptScopeMismatch(BasicAlignment inputAlignment) throws AlignmentException {
		URIAlignment filteredAlignment = new URIAlignment();
		
		URI onto1URI = inputAlignment.getOntology1URI();
		URI onto2URI = inputAlignment.getOntology2URI();
		
		filteredAlignment.init(onto1URI, onto2URI, A5AlgebraRelation.class, BasicConfidence.class);
		
		String qualifier = null;
		String compoundHead = null;
		for (Cell c : inputAlignment) {
			if (StringUtilities.isCompoundWord(c.getObject1AsURI().getFragment())) {
				qualifier = StringUtilities.getCompoundQualifier(c.getObject1AsURI().getFragment());
				compoundHead = StringUtilities.getCompoundHead(c.getObject1AsURI().getFragment());
				
				//e.g. [Cloud]Layer - Cloud || Aircraft[Flow]-Flow
				if (qualifier.toLowerCase().equals(c.getObject2AsURI().getFragment().toLowerCase()) || compoundHead.toLowerCase().equals(c.getObject2AsURI().getFragment().toLowerCase())) {
					filteredAlignment.addAlignCell(c.getObject1(), c.getObject2(), c.getRelation().getRelation(), c.getStrength());
				}

			} else if (StringUtilities.isCompoundWord(c.getObject2AsURI().getFragment())) {
				qualifier = StringUtilities.getCompoundQualifier(c.getObject2AsURI().getFragment());
				compoundHead = StringUtilities.getCompoundHead(c.getObject2AsURI().getFragment());
				//e.g. [Sector] || Location-Reference[Location]
				if (qualifier.toLowerCase().equals(c.getObject1AsURI().getFragment().toLowerCase()) || compoundHead.toLowerCase().equals(c.getObject1AsURI().getFragment().toLowerCase())) {
					filteredAlignment.addAlignCell(c.getObject1(), c.getObject2(), c.getRelation().getRelation(), c.getStrength());
				}
			}
		}

		System.out.println("The filtered alignment contains " + filteredAlignment.nbCells() + " cells");
		
		return filteredAlignment;

	}
	
	/**
	 * Returns the number of senses of a given keyword in WordNet. This is used to approximate the probability of whether a word is a homonym or not.
	 * @param word
	 * @return
	   Nov 26, 2018
	 */
	private static int getNumSynsets(String word) {

		return WordNet.getNumSenses(word);
	}


	/**
	 * Checks how many of the classes in onto1 and onto2 are represented by synsets in WordNet
	 * @param onto1 OWLOntology 1
	 * @param onto2 OWLOntology 2
	 * @return an int stating how many classes are represented in WordNet
	 * @throws FileNotFoundException
	 * @throws JWNLException
	   Nov 26, 2018
	 */
	public static int wordNetRepresentation(OWLOntology onto1, OWLOntology onto2) throws FileNotFoundException, JWNLException {
		int rep = 0;	

		Set<OWLClass> onto1Cls = onto1.getClassesInSignature();
		Set<OWLClass> onto2Cls = onto2.getClassesInSignature();
		//merge the sets
		onto2Cls.addAll(onto1Cls);

		System.out.println("Test: There are " + onto2Cls.size() + " distinct classes in the two ontologies");

		for (OWLClass cls : onto2Cls) {
			if (WordNet.containedInWordNet(cls.getIRI().getFragment().toLowerCase())) {
				rep++;
			}
		}

		return rep;
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


	/**
	 * Given an OWLClass cls (belonging to OWLOntology onto) classes linked to this class via object properties are retrieved and put into a set.
	 * @param onto the ontology holding the class cls
	 * @param cls the class for which "context" will be retrieved
	 * @return a context set containing domain and range classes linked to cls via object properties
	   Nov 26, 2018
	 */
	private static Set<String> getContext (OWLOntology onto, OWLClass cls) {
		Set<String> context = new HashSet<String>();

		Set<OWLObjectProperty> ops_whereDomain = OntologyOperations.getObjectPropertiesDomain(onto, cls); 

		for (OWLObjectProperty op : ops_whereDomain) {
			context.addAll(OntologyOperations.getRangeClasses(onto, op));
		}

		Set<OWLObjectProperty> ops_whereRange = OntologyOperations.getObjectPropertiesRange(onto, cls); 
		for (OWLObjectProperty op : ops_whereRange) {
			context.addAll(OntologyOperations.getDomainClasses(onto, op));
		}

		return context;
	}


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

		return sim;

	}

	/*
	Note: I think using class labels and definitions from the domain and range classes just introduce noise as to what the 
	semantic intention of the object properties really is. We should focus on the properties only and use their labels (and perhaps 
	definitions) as a starting Point. If we include Cheathams PropString this also considers the domain and ranges of properties, so we can omit this here. 

	In the example by Visser the meronymic relations are different ('House' isMadeOf 'Brick' and 'House' hasComponent 'Brick'), in the first 'House' is the subject (this is made of X), 
	while in the second 'Brick' (X has a component brick) is the subject. 
	Both relations are probably "Component / Integral Object" types as suggested by Winston et al. I think that both are also of type function.  

	I think it will be very difficult to learn patterns of meronymic relations without any machine learning effort. 
	 */

	private static boolean measureLinguisticSemInt(OWLOntology onto1, OWLOntology onto2, OWLObjectProperty op_ci, OWLObjectProperty op_cj) throws IOException {
		boolean similar = false;

		//get the domain class(es)
		Set<String> op_ci_domains = OntologyOperations.getDomainClasses(onto1, op_ci);
		Set<String> op_cj_domains = OntologyOperations.getDomainClasses(onto2, op_cj);

		//get definitions of domain classes
		Set<String> defs_op_ci_domains = OntologyOperations.getOPDefinitions(onto1, op_ci);
		Set<String> defs_op_cj_domains = OntologyOperations.getOPDefinitions(onto1, op_ci);

		//get the range class(es)
		Set<String> op_ci_ranges = OntologyOperations.getRangeClasses(onto1, op_ci);
		Set<String> op_cj_ranges = OntologyOperations.getRangeClasses(onto2, op_cj);

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


	/**
	 * Computes the Jaccard similarity (intersection over union) between two definitions
	 * @param s1 source definition (i.e. sets of tokens)
	 * @param s2 target definition (i.e. sets of tokens)
	 * @return a measure stating how similar two definitions are
	 * @throws IOException
	   Nov 26, 2018
	 */
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


	/* BACKUP */


	/**
	 * Returns the average vector of all tokens represented in the RDFS comment for an OWL class
	 * @param onto The ontology holding the OWL class
	 * @param op The OWL object property
	 * @param vectorMap The map of vectors from en input vector file
	 * @return An average vector for all (string) tokens in an RDFS comment
	 * @throws IOException
	 *//*
	public static String getCommentVector(OWLOntology onto, OWLObjectProperty op, Map<String, ArrayList<Double>> vectorMap) throws IOException {


		Map<String, ArrayList<Double>> allCommentVectors = new HashMap<String, ArrayList<Double>>();
		StringBuffer sb = new StringBuffer();
		String comment = getComment(onto, op);
		String commentVector = null;

		ArrayList<Double> commentVectors = new ArrayList<Double>();

		if (comment != null && !comment.isEmpty()) {

			//create tokens from comment
			ArrayList<String> tokens = StringUtilities.tokenize(comment, true);

			//put all tokens that have an associated vector in the vectorMap in allCommentVectors along with the associated vector
			for (String s : tokens) {
				if (vectorMap.containsKey(s)) {
					commentVectors = vectorMap.get(s);

					allCommentVectors.put(s, commentVectors);

				} else {
					commentVectors = null;
				}

			}

			//create average vector representing all token vectors in each comment
			ArrayList<Double> avgs = new ArrayList<Double>();

			int numVectors = 0;

			for (Entry<String, ArrayList<Double>> e : vectorMap.entrySet()) {

				numVectors = e.getValue().size();

			}

			for (int i = 0; i < numVectors; i++) {

				ArrayList<Double> temp = new ArrayList<Double>();

				for (Entry<String, ArrayList<Double>> e : allCommentVectors.entrySet()) {

					ArrayList<Double> a = e.getValue();

					temp.add(a.get(i));

				}

				double avg = 0;

				int entries = temp.size();

				for (double d : temp) {
					avg += d;
				}

				double newAvg = avg/entries;


				if (newAvg != 0.0 && !Double.isNaN(newAvg)) {
					avgs.add(newAvg);

				}

			}

			for (double d : avgs) {
				sb.append(Double.toString(MathUtils.round(d, 6)) + " ");

			}

			commentVector = sb.toString();
		} else {
			commentVector = null;
		}

		return commentVector;

	}*/

	/**
	 * Returns a set of string tokens from the RDFS comment associated with an OWL class
	 * @param onto The ontology holding the OWL class
	 * @param cls The OWL class
	 * @return A string representing the set of tokens from a comment without stopwords
	 * @throws IOException
	 *//*
	public static String getComment (OWLOntology onto, OWLObjectProperty cls) throws IOException {

		String comment = null;
		String commentWOStopWords = null;

		for(OWLAnnotation a : cls.getAnnotations(onto, factory.getRDFSComment())) {
			OWLAnnotationValue value = a.getValue();
			if(value instanceof OWLLiteral) {
				comment = ((OWLLiteral) value).getLiteral().toString();
				commentWOStopWords = StringUtilities.removeStopWordsFromString(comment);
			}
		}

		return commentWOStopWords;

	}*/
	/*private static double getPropSim (OWLOntology onto1, OWLOntology onto2, Set<OWLObjectProperty> ops_ci, Set<OWLObjectProperty> ops_cj) throws IOException {
		double sim = 0; 

		Set<String> ops1 = new HashSet<String>();
		Set<String> ops2 = new HashSet<String>();

		StringBuilder ops1SB = new StringBuilder();
		StringBuilder ops2SB = new StringBuilder();

		//add pre-processed property name tokens into the sets (only the compound head, synonyms of the compound head)
		for (OWLObjectProperty op_ci : ops_ci) {

			if (StringUtilities.isCompoundWord(op_ci.getIRI().getFragment())) {
				StringUtilities.getCompoundHead(op_ci.getIRI().getFragment());
				ops1SB.append(StringUtilities.getCompoundHead(op_ci.getIRI().getFragment()));

			} else {
				ops1SB.append(op_ci.getIRI().getFragment());
			}


			//get definitions
			Set<String> op_ci_defs = OntologyOperations.getOPDefinitions(onto1, op_ci);

			for (String s : op_ci_defs) {
				ops1SB.append(s.replaceAll("[^a-zA-Z0-9\\s]", "")); //remove all non-word characters/symbols
			}

			//remove stopwords and tokenise
			ops1 = StringUtilities.tokenizeToSet(ops1SB.toString(), true);

		}

		//add pre-processed property name tokens into the sets (only the compound head, synonyms of the compound head)
		for (OWLObjectProperty op_cj : ops_cj) {

			if (StringUtilities.isCompoundWord(op_cj.getIRI().getFragment())) {
				StringUtilities.getCompoundHead(op_cj.getIRI().getFragment());
				ops2SB.append(StringUtilities.getCompoundHead(op_cj.getIRI().getFragment()));
			} else {
				ops2SB.append(op_cj.getIRI().getFragment());
			}

			//get definitions
			Set<String> op_cj_defs = OntologyOperations.getOPDefinitions(onto1, op_cj);

			for (String s : op_cj_defs) {
				ops2SB.append(s.replaceAll("[^a-zA-Z0-9\\s]", "")); //remove all non-word characters/symbols
			}

			//remove stopwords and tokenise
			ops2 = StringUtilities.tokenizeToSet(ops2SB.toString(), true);

		}
		sim = Jaccard.jaccardSetSim(ops1, ops2);

		return sim; 
	}*/

	/*public static double measureEmbeddingSemInt (String s1, String s2) throws OntowrapException, FileNotFoundException {

		//String vectorFileOnto1 = "./files/ATMONTO_AIRM/vectorfiles/ATMOntoCoreMerged.txt";
		//String vectorFileOnto2 = "./files/ATMONTO_AIRM/vectorfiles/airm-mono.txt";

		File vc1File = new File(vectorFileOnto1);
		File vc2File = new File(vectorFileOnto2);

		//		//get the vector concepts for the ontology files
		vc1File = new File(vectorFileOnto1); 
		vc2File = new File(vectorFileOnto2);

		//each concept in both ontologies being matched are represented as a set of VectorConcepts
		Set<VectorConcept> vc1Set = VectorConcept.populate(vc1File);
		Set<VectorConcept> vc2Set = VectorConcept.populate(vc2File);


		//test:System.out.println("Matching " + s1 + " and " + s2);

		double[] a1 = null;
		double[] a2 = null;


		//get the vectors of the two concepts being matched
		for (VectorConcept c1 : vc1Set) {		
			if (s1.equals(c1.getConceptLabel())) {
				a1 = ArrayUtils.toPrimitive(c1.getGlobalVectors().toArray((new Double[c1.getGlobalVectors().size()])));		
			} else {
			}
		}

		for (VectorConcept c2 : vc2Set) {
			if (s2.equals(c2.getConceptLabel())) {
				a2 = ArrayUtils.toPrimitive(c2.getGlobalVectors().toArray((new Double[c2.getGlobalVectors().size()])));			
			} else {
			}
		}

		//measure the cosine similarity between the vector dimensions of these two entities
		CosineVM cosine = new CosineVM();

		double measure = 0;
		if (a1 != null && a2 != null) {

			measure = cosine.getSim(a1, a2);

		}

		if (measure > 0.0) {
			//test:System.err.println("The similarity is " + measure);
		}

		//we do not allow similarity scores above 1.0
		if (measure > 0) {
			if (measure > 1.0) {
				measure = 1.0;
			}
			return measure;
		} else {
			return 0;
		}

	}*/
}

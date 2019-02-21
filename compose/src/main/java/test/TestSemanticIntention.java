package test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import com.google.common.base.Joiner;
import com.graphbuilder.curve.Point;

import utilities.Jaccard;
import utilities.OntologyOperations;
import utilities.StringUtilities;
import utilities.WordNet;

public class TestSemanticIntention {

	public static void main(String[] args) throws OWLOntologyCreationException, IOException {


		//		File ontoFile1 = new File("./files/OAEI-16-conference/ontologies/edas.owl");
		//		File ontoFile2 = new File("./files/OAEI-16-conference/ontologies/sigkdd.owl");
		File ontoFile1 = new File("./files/ESWC_ATMONTO_AIRM/ATMOntoCoreMerged.owl");
		File ontoFile2 = new File("./files/ESWC_ATMONTO_AIRM/airm-mono.owl");
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto1 = manager.loadOntologyFromOntologyDocument(ontoFile1);
		OWLOntology onto2 = manager.loadOntologyFromOntologyDocument(ontoFile2);

		Set<String> a_filtered = removeCovAndGranMismatches(onto1, onto2);

		System.out.println("These are the relations in A_filtered:");
		for (String s : a_filtered) {
			System.out.println(s);
		}

	}

/*
 * If class label is a compound we need to preprocess it, if the whole compound (e.g. "Aircraft engine") is present -> ok, if not, try to figure out which compound part is most representative for the label, "engine".
 */
	
	public static Set<String> removeHomonymTermsMismatches(OWLOntology onto1, OWLOntology onto2)	throws IOException {
		Set<String> aFiltered = new HashSet<String>();

		//iterate all classes in both ontologies
		for (OWLClass ci : onto1.getClassesInSignature()) {
			for (OWLClass cj : onto2.getClassesInSignature()) {
				
				if (ci.equals(cj) && isHomonym(ci) && isHomonym(cj)) {
					
				}
				
			}
		}
		
			


				return aFiltered;
			}

			public static Set<String> removeCovAndGranMismatches(OWLOntology onto1, OWLOntology onto2) throws IOException {
				Set<String> a_filtered = new HashSet<String>();

				Set<OWLObjectProperty> propSet_ci = new HashSet<OWLObjectProperty>();
				Set<OWLObjectProperty> propSet_cj = new HashSet<OWLObjectProperty>();

				//iterate all classes in both ontologies
				for (OWLClass ci : onto1.getClassesInSignature()) {
					for (OWLClass cj : onto2.getClassesInSignature()) {
						if (ci.getIRI().getFragment().equals(cj.getIRI().getFragment())) {
							propSet_ci = OntologyOperations.getObjectProperties(onto1, ci);
							propSet_cj = OntologyOperations.getObjectProperties(onto2, cj);

							for (OWLObjectProperty op_ci : propSet_ci) {
								for (OWLObjectProperty op_cj : propSet_cj) {

									//check if their domain classes are equal							
									for (String domain_op_ci : getDomainClasses(onto1, op_ci)) {
										for (String domain_op_cj : getDomainClasses(onto2, op_cj)) {
											if (domain_op_ci.equals(domain_op_cj)) {
												//then their range classes...
												for (String range_op_ci : getRangeClasses(onto1, op_ci)) {
													for (String range_op_cj : getRangeClasses(onto2, op_cj)) {
														if (range_op_ci.equals(range_op_cj) && measureSemInt(onto1, onto2, op_ci, op_cj) == false) {
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


			/*
	Note: I think using class labels and definitions from the domain and range classes just introduce noise as to what the 
	semantic intention of the object properties really is. We should focus on the properties only and use their labels (and perhaps 
	definitions) as a starting Point.class 

	In the example by Visser the meronymic relations are different ('House' isMadeOf 'Brick' and 'House' hasComponent 'Brick'), in the first 'House' is the subject (this is made of X), 
	while in the second 'Brick' (X has a component brick) is the subject. 
	Both relations are probably "Component / Integral Object" types as suggested by Winston et al. I think that both are also of type function.  

	I think it will be very difficult to learn patterns of meronymic relations without any machine learning effort. 
			 */

			private static boolean measureSemInt(OWLOntology onto1, OWLOntology onto2, OWLObjectProperty op_ci, OWLObjectProperty op_cj) throws IOException {
				boolean similar = false;

				//get the domain class(es)
				Set<String> op_ci_domains = getDomainClasses(onto1, op_ci);
				Set<String> op_cj_domains = getDomainClasses(onto2, op_cj);

				//get definitions of domain classes
				Set<String> defs_op_ci_domains = getDefinitions(onto1, op_ci);
				Set<String> defs_op_cj_domains = getDefinitions(onto1, op_ci);

				//get the range class(es)
				Set<String> op_ci_ranges = getRangeClasses(onto1, op_ci);
				Set<String> op_cj_ranges = getRangeClasses(onto2, op_cj);

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


			private static Set<String> getDefinitions (OWLOntology onto, OWLObjectProperty op) {
				Set<String> definitions = new HashSet<String>();

				for (OWLClass cls : onto.getClassesInSignature()) {

					for (OWLAnnotationAssertionAxiom a : onto.getAnnotationAssertionAxioms(cls.getIRI())) {
						if (a.getProperty().isComment()) {
							//need to use a.getValue() instead of a.getAnnotation() to avoid including 'Annotation rdfs comment' that is included before the real definition.
							definitions.add(a.getValue().toString());
						}
					}

				}

				return definitions;

			}

			private static Set<String> getDomainClasses (OWLOntology onto, OWLObjectProperty op) {
				Set<String> clsSet = new HashSet<String>();

				//get the domain class(es)
				Set<OWLClassExpression> domainClasses = op.getDomains(onto);

				for (OWLClassExpression exp : domainClasses) {
					if (!exp.isAnonymous()) { //need to check if exp represents an anonymous class (a class expression without an IRI identifier)
						clsSet.add(exp.asOWLClass().getIRI().getFragment());
					}
				}


				return clsSet;
			}

			private static Set<String> getRangeClasses (OWLOntology onto, OWLObjectProperty op) {
				Set<String> clsSet = new HashSet<String>();

				//get the domain class(es)
				Set<OWLClassExpression> rangeClasses = op.getRanges(onto);

				for (OWLClassExpression exp : rangeClasses) {
					if (!exp.isAnonymous()) { //need to check if exp represents an anonymous class (a class expression without an IRI identifier)
						clsSet.add(exp.asOWLClass().getIRI().getFragment());
					}
				}


				return clsSet;
			}

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

		}

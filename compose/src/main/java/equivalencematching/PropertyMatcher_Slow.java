package equivalencematching;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import utilities.SimilarityMetrics;
import utilities.StringUtilities;
import utilities.WordNet;

public class PropertyMatcher_Slow extends ObjectAlignment implements AlignmentProcess {
	
	OWLOntology onto1;
	OWLOntology onto2;
	double weight;
	final double confidence = 0.7;
	
	public PropertyMatcher_Slow(OWLOntology ontoFile1, OWLOntology ontoFile2, double weight) {
		onto1 = ontoFile1;
		onto2 = ontoFile2;
		this.weight = weight;
	}

	public void align(Alignment alignment, Properties param) throws AlignmentException {

		double sim = 0;

		try {
			// Match classes
			for ( Object cl2: ontology2().getClasses() ){
				for ( Object cl1: ontology1().getClasses() ){

					String s1 = ontology1().getEntityName(cl1).toLowerCase();
					String s2 = ontology2().getEntityName(cl2).toLowerCase();
										
					Set<String> props1 = getPropertiesAndSynonyms(onto1, s1);

					Set<String> props2 = getPropertiesAndSynonyms(onto2, s2);
					
					//JACCARD SIMILARITY WITH ISUB AND EQUAL CONCEPTS
					sim = SimilarityMetrics.jaccardSetSimISubEqualConcepts(confidence, s1, s2, props1, props2);
					if (sim > 0 && sim <= 1) {
						addAlignCell(cl1,cl2, "=", weight*sim);  
					} else {
						addAlignCell(cl1, cl2, "=", 0);
					}
				}
			}

		} catch (Exception e) { e.printStackTrace(); }
	}

	/* WHEN USING THE ONTOLOGY IRIs TO LOAD THE ONTOLOGIES */
//	public void align(Alignment alignment, Properties param) throws AlignmentException {
//
//		IRI onto1IRI = IRI.create(ontology1().getURI());
//		IRI onto2IRI = IRI.create(ontology2().getURI());
//
//		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
//
//		OWLOntology onto1 = null;
//		OWLOntology onto2 = null;
//
//		try {
//			onto1 = manager.loadOntologyFromOntologyDocument(onto1IRI);
//		} catch (OWLOntologyCreationException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//
//		try {
//			onto2 = manager.loadOntologyFromOntologyDocument(onto2IRI);
//		} catch (OWLOntologyCreationException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//
//		double jaccardSim = 0;
//
//		try {
//			// Match classes
//			for ( Object cl2: ontology2().getClasses() ){
//				for ( Object cl1: ontology1().getClasses() ){
//
//					String s1 = ontology1().getEntityName(cl1).toLowerCase();
//					String s2 = ontology2().getEntityName(cl2).toLowerCase();
//					
//					System.out.println("\nMatching " + s1 + " and " + s2);
//
//					Set<String> props1 = getProperties(onto1, s1);
//					System.out.println("Test: Properties of " + s1 + ":");
//					for (String s : props1) {
//						System.out.println(s);
//					}
//					Set<String> props2 = getProperties(onto2, s2);
//					System.out.println("Test: Properties of " + s2 + ":");
//					for (String s : props2) {
//						System.out.println(s);
//					}
//					jaccardSim = jaccardSetSim(props1, props2);
//					
//					System.out.println("Test: The jaccarSim is " + jaccardSim);
//
//					// add mapping into alignment object
//					//need to keep our sim within [0..1] 
//					if (jaccardSim > 0 && jaccardSim <= 1) {
//						addAlignCell(cl1,cl2, "=", jaccardSim);  
//					} else {
//						addAlignCell(cl1, cl2, "=", 0);
//					}
//				}
//
//			}
//
//		} catch (Exception e) { e.printStackTrace(); }
//	}


	/**
	 * Retrieves all properties and synonyms of them (from WordNet) where an OWLClass is the domain or the range (not for data properties). This includes both object and data properties.
	 * @param onto
	 * @param clsString
	 * @return
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	private static Set<String> getPropertiesAndSynonyms(OWLOntology onto, String clsString) throws ClassNotFoundException, IOException {

		Set<OWLClass> allClasses = onto.getClassesInSignature();		

		Set<String> ops = new HashSet<String>();
		Set<String> dps = new HashSet<String>();

		for (OWLClass cls : allClasses) {
			if (cls.getIRI().getFragment().toLowerCase().equals(clsString)) {

				for (OWLObjectPropertyDomainAxiom op : onto.getAxioms(AxiomType.OBJECT_PROPERTY_DOMAIN)) {
					if (op.getDomain().equals(cls)) {
						for (OWLObjectProperty oop : op.getObjectPropertiesInSignature()) {
							//get the core concept of the property
							ops.add(getPropertyCoreConcept(oop.getIRI().getFragment().substring(oop.getIRI().getFragment().lastIndexOf("-") +1)));
						}
					}
				}

				for (OWLObjectPropertyRangeAxiom op : onto.getAxioms(AxiomType.OBJECT_PROPERTY_RANGE)) {
					if (op.getRange().equals(cls)) {
						for (OWLObjectProperty oop : op.getObjectPropertiesInSignature()) {
							//get the core concept of the property
							ops.add(getPropertyCoreConcept(oop.getIRI().getFragment().substring(oop.getIRI().getFragment().lastIndexOf("-") +1)));
						}
					}
				}

				for (OWLDataPropertyDomainAxiom dp : onto.getAxioms(AxiomType.DATA_PROPERTY_DOMAIN)) {
					if (dp.getDomain().equals(cls)) {
						for (OWLDataProperty odp : dp.getDataPropertiesInSignature()) {
							//get the core concept of the property
							dps.add(getPropertyCoreConcept(odp.getIRI().getFragment().substring(odp.getIRI().getFragment().lastIndexOf("-") +1)));
						}
					}
				}

			}
		}

		//merge all object and data properties into one set
		Set<String> props = new HashSet<String>();
		props.addAll(ops);
		props.addAll(dps);
		
		//once all properties (i.e. their core concepts) have been collected, we retrieve their synonyms (nouns, verbs, and adjectives) from WordNet
		//the query parameter to WordNet is the lemma of the property label.
		Set<String> propsSynonyms = new HashSet<String>();
		
		for (String p : props) {
			propsSynonyms = WordNet.getAllSynonymSet(p.toLowerCase().replaceAll("\\s+", "")); //use the lemma + need to remove whitespace before querying WordNet
		}
		
		props.addAll(propsSynonyms);

		return props;

	}

	public static String getPropertyCoreConcept(String text) throws IOException, ClassNotFoundException {

		if (StringUtilities.isCompoundWord(text)) {
			text = StringUtilities.splitCompounds(text);
		}
		
		MaxentTagger maxentTagger = new MaxentTagger("edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger");
		String tag = maxentTagger.tagString(text);

		String[] eachTag = tag.split("\\s+");

		Multimap<String, String> posMap = LinkedListMultimap.create();
		for(int i = 0; i< eachTag.length; i++) {
			posMap.put(eachTag[i].split("_")[0], eachTag[i].split("_")[1]);
		}

		StringBuffer sb = new StringBuffer();
		for (Entry<String, String> e : posMap.entries()) {
			if (e.getValue().equals("VB") || e.getValue().equals("VBD") || e.getValue().equals("VBG") || e.getValue().equals("VBP") || e.getValue().equals("VBZ") || e.getValue().equals("VBN")) {
				if (e.getKey().length() > 3) {
					sb.append(e.getKey() + " ");
					break;
				}
			} else if (e.getValue().equals("JJ") || e.getValue().equals("JJR") || e.getValue().equals("JJS")) {
				sb.append(e.getKey() + " ");
			}

			else if (e.getValue().equals("NN") || e.getValue().equals("NNS") || e.getValue().equals("NNP") || e.getValue().equals("NNPS") || e.getValue().equals(".")) {
				sb.append(e.getKey() + " ");
				break;
			}

		}

		return sb.toString();
	}

}




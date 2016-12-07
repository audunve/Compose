package no.ntnu.idi.compose.Matchers;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;

import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.ontowrap.OntowrapException;
import no.ntnu.idi.compose.WordNet.RiWordNetOperations;
import no.ntnu.idi.compose.algorithms.ISub;
import rita.RiWordNet;

/*
Synonymy = Equal
Hyponymy = Is-a (More specific to more general, e.g. Car is-a Vehicle)
Hypernymy = Inverse Is-a (More general to more specific, e.g. Vehicle isMoreGeneralThan Car)
Meronymy = Part-of (e.g. Leg (isPartOf) Body)
Holonymy = Has-a (e.g. Body (has-a) Leg)
 */

public class Subsumption_WordNet extends ObjectAlignment implements AlignmentProcess {

	static RiWordNet database = new RiWordNet("/Users/audunvennesland/Documents/PhD/Development/WordNet/WordNet-3.0/dict");
	Map<String, Double> matchingMap = new HashMap<String, Double>();

	public Subsumption_WordNet() {
	}

	/**
	 * The align() method is imported from the Alignment API and is modified to use the wordNetMatch method declared in this class
	 */
	public void align( Alignment alignment, Properties param ) throws AlignmentException {

		try {
			// Match classes
			for ( Object cl2: ontology2().getClasses() ){
				for ( Object cl1: ontology1().getClasses() ){

					matchingMap = wordNetMatch(cl1, cl2);

					for (Map.Entry<String, Double> entry : matchingMap.entrySet()) {
						System.out.println("Key : " + entry.getKey() + " Value : " + entry.getValue());

						// add mapping into alignment object 
						addAlignCell(cl1,cl2, entry.getKey(), entry.getValue());  
					}

				}
			}

		} catch (Exception e) { e.printStackTrace(); }
	}


	public Map<String, Double> wordNetMatch (Object o1, Object o2) throws AlignmentException, OntowrapException {

		double finalDistance = 0;
		String relation = null;
		ISub iSubMatcher = new ISub();
		double iSubScore = 0;
		Map<String, Double> matchingMap = new HashMap<String, Double>();


		//get the objects (entities)
		String s1 = ontology1().getEntityName(o1).toLowerCase();
		String s2 = ontology2().getEntityName(o2).toLowerCase();

		if (s1.equals(s2)) {
			System.out.println(s1 + " and " + s2 + " are equivalent");
			matchingMap.put("=", 0.0);
		} else {
			System.out.println("--- Matching " + "s1:" + s1 + " and " + "s2:" + s2 + " ---");

			//TO-DO: should preprocess s1 and s2 (e.g. LectureNotes -> Lecture Notes) before getting data from WordNet
			String[] cls1_synonyms = RiWordNetOperations.getSynonyms(s1);
			String[] cls1_hyponyms = RiWordNetOperations.getHyponyms(s1);
			String[] cls2_synonyms = RiWordNetOperations.getSynonyms(s2);
			String[] cls2_hyponyms = RiWordNetOperations.getHyponyms(s2);

			//if cls2 is a part of the set of hyponyms of cls1 --> cls1 > cls2
			if (cls1_hyponyms.length > 0) {
				for (String hyponymCls1 : cls1_hyponyms) {
					System.out.println("Matching " + hyponymCls1 + " and " + s2 + " --> " + iSubScore);	
					iSubScore = iSubMatcher.score(hyponymCls1, s2);

					if (iSubMatcher.score(hyponymCls1, s2) > 0.9) {
						relation = "&gt;";
						System.out.println("Subsumption identified! : " + s1 + " " + relation + " " + s2);
						finalDistance = iSubScore;
						matchingMap.put(relation, finalDistance);
					}
				}
			} else {
				System.out.println(s1 + " does not exist in WordNet!");
			}


			//or vice versa, if cls1 is the part of the set of hyponyms of cls2 --> cls2 > cls1
			if (cls2_hyponyms.length > 0) {
				for (String hyponymCls2 : cls2_hyponyms) {
					iSubScore = iSubMatcher.score(hyponymCls2, s1);
					System.out.println("Matching " + hyponymCls2 + " and " + s1 + " --> " + iSubScore);

					if (iSubMatcher.score(hyponymCls2, s1) > 0.9) {
						relation = "&lt;";
						System.out.println("Subsumption identified! : " + s2 + " " + relation + " " + s1);
						finalDistance = iSubScore;
						matchingMap.put(relation, finalDistance);
					}
				}
			}  else {
				System.out.println(s2 + " does not exist in WordNet!");
			}
		}

		return matchingMap;
	}



}



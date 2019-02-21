package meronymmatching;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;

import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.ontowrap.OntowrapException;
import rita.RiWordNet;
import utilities.ISub;
import utilities.WordNet;
import utilities.StringUtilities;

/*
Synonymy = Equal
Hyponymy = Is-a (More specific to more general, e.g. Car is-a Vehicle)
Hypernymy = Inverse Is-a (More general to more specific, e.g. Vehicle isMoreGeneralThan Car)
Meronymy = Part-of (e.g. Leg (isPartOf) Body)
Holonymy = Has-a (e.g. Body (has-a) Leg)
 */

public class WNMeronymMatcher extends ObjectAlignment implements AlignmentProcess {

	Map<String, Double> matchingMap = new HashMap<String, Double>();


	public WNMeronymMatcher() {
	}


	public void align( Alignment alignment, Properties param ) throws AlignmentException {

		try {
			// Match classes
			for ( Object cl2: ontology2().getClasses() ){
				for ( Object cl1: ontology1().getClasses() ){

					matchingMap = meronymMatchSynonyms(cl1, cl2);

					for (Map.Entry<String, Double> entry : matchingMap.entrySet()) {
						//System.out.println("Key : " + entry.getKey() + " Value : " + entry.getValue());

						// add mapping into alignment object 
						addAlignCell(cl1,cl2, entry.getKey(), entry.getValue());  
					}

				}
			}

		} catch (Exception e) { e.printStackTrace(); }
	}

	public Map<String, Double> meronymMatchSimple (Object o1, Object o2) throws AlignmentException, OntowrapException {

		double finalDistance = 0;
		String relation = null;
		Map<String, Double> matchingMap = new HashMap<String, Double>();

		String s1 = StringUtilities.stringTokenize(ontology1().getEntityName(o1), true);
		String s2 = StringUtilities.stringTokenize(ontology2().getEntityName(o2), true);

		Set<String> cls1_meronyms = WordNet.getMeronymSet(s1);
		Set<String> cls2_meronyms = WordNet.getMeronymSet(s2);

		if (cls1_meronyms.size() > 0) {

			for (String meronymCls1 : cls1_meronyms) {

				if (meronymCls1.equals(s2)) {
					System.out.println(meronymCls1 + "equals" + s2);
					relation = "hasPart";
					finalDistance = 1.0;
					matchingMap.put(relation, finalDistance);
				}
			}
		}

		if (cls2_meronyms.size() > 0) {

			for (String meronymCls2: cls2_meronyms) {

				if (meronymCls2.equals(s1)) {
					System.out.println(meronymCls2 + "equals" + s1);
					relation = "partOf";
					finalDistance = 1.0;
					matchingMap.put(relation, finalDistance);
				}
			}
		}  else {
			//System.out.println(s2 + " does not exist in WordNet!");
		}


		return matchingMap;
	}

	public Map<String, Double> meronymMatchSynonyms (Object o1, Object o2) throws AlignmentException, OntowrapException {

		double finalDistance = 0;
		String relation = null;
		Map<String, Double> matchingMap = new HashMap<String, Double>();

		//get the objects (entities)
		//should get the "representative word" of the entity name, e.g. instead of "aircraftengine" -> "aircraft engine"
		String s1 = StringUtilities.stringTokenize(ontology1().getEntityName(o1), false).toLowerCase();
		String s2 = StringUtilities.stringTokenize(ontology2().getEntityName(o2), false).toLowerCase();
		
		System.out.println("--- Matching " + s1 + " and " + s2 + " ---");

		Set<String> cls1_meronyms = WordNet.getMeronymsFromSynonyms(s1);
		Set<String> cls2_meronyms = WordNet.getMeronymsFromSynonyms(s2);
		
		

		if (cls1_meronyms.size() > 0) {

			for (String meronymCls1 : cls1_meronyms) {

				if (meronymCls1.equals(s2)) {
					System.out.println(meronymCls1 + " equals " + s2);
					relation = "hasPart";
					finalDistance = 1.0;
					matchingMap.put(relation, finalDistance);
				}
			}
		}

		if (cls2_meronyms.size() > 0) {

			for (String meronymCls2: cls2_meronyms) {

				if (meronymCls2.equals(s1)) {
					System.out.println(meronymCls2 + " equals " + s1);
					relation = "partOf";
					finalDistance = 1.0;
					matchingMap.put(relation, finalDistance);
				}
			}
		}  else {
			//System.out.println(s2 + " does not exist in WordNet!");
		}


		return matchingMap;
	}
	
	public Map<String, Double> meronymMatchCompounds (Object o1, Object o2) throws AlignmentException, OntowrapException {

		double finalDistance = 0;
		String relation = null;
		Map<String, Double> matchingMap = new HashMap<String, Double>();

		//get the objects (entities)
		//need to represent the concept names as they are in order to get their compound structure
		String s1 = ontology1().getEntityName(o1);
		String s2 = ontology2().getEntityName(o2);

		Set<String> cls1_meronyms = WordNet.getMeronymSet(StringUtilities.stringTokenize(s1, false).toLowerCase());
		Set<String> cls2_meronyms = WordNet.getMeronymSet(StringUtilities.stringTokenize(s2, false).toLowerCase());

		//if the concepts are not equal
		if (!s1.equals(s2)) {
		
		if (cls1_meronyms.size() > 0) {

			for (String meronymCls1 : cls1_meronyms) {
				
				//break the meronyms of s1 into compound parts and check if any of the parts matches s2
					String[] compoundParts_meronymCls1 = meronymCls1.split(" ");
					
					for (int i = 0; i < compoundParts_meronymCls1.length; i++) {
						
						System.out.println("Trying the compound part " + compoundParts_meronymCls1[i] + " and s2: " 
					+ StringUtilities.stringTokenize(s2, false).toLowerCase());
						
						if (StringUtilities.stringTokenize(s2, false).toLowerCase().equals(compoundParts_meronymCls1[i])) {
							System.out.println(meronymCls1 + " equals " + s2);
							relation = "hasPart";
							finalDistance = 0.8;
							matchingMap.put(relation, finalDistance);
						}
					}
			}
		}

		if (cls2_meronyms.size() > 0) {

			for (String meronymCls2: cls2_meronyms) {

				//break the meronyms of s2 into compound parts and check if any of the parts matches s1
					String[] compoundParts_meronymCls2 = meronymCls2.split(" ");
					
					for (int i = 0; i < compoundParts_meronymCls2.length; i++) {
						
						System.out.println("Trying the compound part " + compoundParts_meronymCls2[i] + 
								" and s1: " + StringUtilities.stringTokenize(s1, false).toLowerCase());
						
						if (StringUtilities.stringTokenize(s1, false).toLowerCase().equals(compoundParts_meronymCls2[i])) {
							System.out.println(meronymCls2 + " equals " + s1);
							relation = "partOf";
							finalDistance = 0.8;
							matchingMap.put(relation, finalDistance);
						}
					}
					
				
			}
		}  
		else {
			//System.out.println(s2 + " does not exist in WordNet!");
		}
		}

		return matchingMap;
	}

	

	/**
	 * This method returns a measure computed from two input OWL entities (processed as strings) using iSub algorithm (Stolios et al, 2005)
	 * @param o1 object representing an OWL entitiy
	 * @param o2 object representing an OWL entitiy
	 * @return a similarity scored computed from the ISub algorithm
	 * @throws OntowrapException
	 */
	public double meronymScore(Object o1, Object o2) throws OntowrapException {

		//get the objects (entities)
		//should get the "representative word" of the entity name, e.g. instead of "aircraftengine" -> "aircraft engine"
		String s1 = StringUtilities.stringTokenize(ontology1().getEntityName(o1), false).toLowerCase();
		String s2 = StringUtilities.stringTokenize(ontology2().getEntityName(o2), false).toLowerCase();

		double measure = 0;
		String relation = null;		
		boolean match = false;

		//get all meronyms of s1
		Set<String> merS1 = WordNet.getMeronymsFromSynonyms(s1);
		//Set<String> merS1 = WordNet.getMeronymSet(s1);

		//get all meronyms of s2
		Set<String> merS2 = WordNet.getMeronymsFromSynonyms(s2);
		//Set<String> merS2 = WordNet.getMeronymSet(s2);

		//check if any of the meronyms of s2 matches s1
		for (String s : merS2) {
			if (s.equals(s1)) {
				System.out.println("\nWe have a match with s1: " + s1 + " and s2: " + s2);
				System.out.println("the meronym of s2: " + s + " matches the concept " + s1);
				match = true;
			}
		}

		if (match == true) {
			System.out.println("match is true for " + s1 + " and " + s2);
			measure = 1.0;
		} else {
			measure = 0.0;
		}

		return measure;

	}
}

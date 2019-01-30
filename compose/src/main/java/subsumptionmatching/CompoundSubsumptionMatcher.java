package subsumptionmatching;

import java.util.ArrayList;
import java.util.Properties;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;

import utilities.RiWordNetOperations_delete;
import utilities.WordNet;
import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.ontowrap.OntowrapException;
import utilities.Relation;



/**
 * CompoundMatcher identifies subsumption relations based on so-called compounds, that is, a word comprised of individual words (e.g. electronicBook)
 * @author audunvennesland
 *
 */
public class CompoundSubsumptionMatcher extends ObjectAlignment implements AlignmentProcess {


	final double THRESHOLD = 0.9;

	public CompoundSubsumptionMatcher() {
	}



	public void align( Alignment alignment, Properties param ) throws AlignmentException {
		try {
			// Match classes
			for ( Object cl2: ontology2().getClasses() ){
				for ( Object cl1: ontology1().getClasses() ){
					// add mapping into alignment object 
					addAlignCell(cl1,cl2, findCompoundRelation(cl1,cl2), compoundMatchWithSynonyms(cl1,cl2));  
				}
			}

		} catch (Exception e) { e.printStackTrace(); }
	}

//	public Relation getRelation(Object o1, Object o2) throws AlignmentException, OntowrapException {
//
//		Relation newRelation = new Relation();
//
//		String s1 = ontology1().getEntityName(o1);
//		String s2 = ontology2().getEntityName(o2);
//
//		double score = 0;
//		String relationType = null;
//
//		String[] s1Compounds = s1.split("(?<=.)(?=\\p{Lu})");
//		String[] s2Compounds = s2.split("(?<=.)(?=\\p{Lu})");
//
//		if (s1Compounds.length > 2 && s2Compounds.length > 2) {
//
//			if (s2.equals(s1Compounds[s1Compounds.length-1]) || s2.equals(s1Compounds[s1Compounds.length-1]+s1Compounds[s1Compounds.length-2] + s1Compounds[s1Compounds.length-3])) {
//
//				score = 1.0;
//				relationType = ">";
//
//				newRelation.setConcept1(s2);
//				newRelation.setConcept2(s1);
//				newRelation.setRelationType(relationType);
//				newRelation.setConfidence(score);
//			}
//
//			if (s1.equals(s2Compounds[s2Compounds.length-1]) || s1.equals(s2Compounds[s2Compounds.length-1]+s2Compounds[s2Compounds.length-2] + s2Compounds[s2Compounds.length-3])) {
//
//				score = 1.0;
//				relationType = ">";
//
//				score = 1.0;
//				relationType = ">";
//				newRelation.setConcept1(s2);
//				newRelation.setConcept2(s1);
//				newRelation.setRelationType(relationType);
//				newRelation.setConfidence(score);
//			}
//
//		} else if (s1Compounds.length > 1 && s2Compounds.length > 1) {
//
//			if (s2.equals(s1Compounds[s1Compounds.length-1]) || s2.equals(s1Compounds[s1Compounds.length-1]+s1Compounds[s1Compounds.length-2])) {
//
//				score = 0.8;
//				relationType = "<";
//			}
//
//			if (s1.equals(s2Compounds[s2Compounds.length-1]) || s1.equals(s2Compounds[s2Compounds.length-1]+s2Compounds[s2Compounds.length-2])) {
//
//				score = 0.8;
//				relationType = ">";
//			}
//		}
//
//
//		return newRelation;
//
//
//	}

	public static double getCompoundScore(String a, String b) {

		double score = 0;

		String[] compounds = a.split("(?<=.)(?=\\p{Lu})");

		if (compounds.length > 2) {

			if (b.equals(compounds[compounds.length-1]) || b.equals(compounds[compounds.length-1]+compounds[compounds.length-2] + compounds[compounds.length-3])) {

				score = 1.0;
			}

		} else if (compounds.length > 1) {

			if (b.equals(compounds[compounds.length-1]) || b.equals(compounds[compounds.length-1]+compounds[compounds.length-2])) {

				score = 0.8;
			}
		}

		return score;
	}



	public double compoundMatch(Object o1, Object o2) throws AlignmentException {
		try {
			String s1 = ontology1().getEntityName(o1);
			String s2 = ontology2().getEntityName(o2);

			if (s1 == null || s2 == null) {
				return 0.;
			}

			else if (isCompound(s1,s2) && !s1.equals(s2)) { 
				return 1.0;
			} else if (isCompound(s2,s1) && !s2.equals(s1)) { 
				return 1.0;
			}
			else { 
				return 0.;
			}
		} catch ( OntowrapException owex ) {
			throw new AlignmentException( "Error getting entity name", owex );
		}
	}

	public double compoundMatchWithSynonyms(Object o1, Object o2) throws AlignmentException {
		try {
			String s1 = ontology1().getEntityName(o1);
			String s2 = ontology2().getEntityName(o2);
			
			//if (s1 == null || s2 == null) return 0;

			if (isCompoundSyn(s1,s2) && !s1.equals(s2)) { 
				return 1.0;
			} else if (isCompoundSyn(s2,s1) && !s2.equals(s1)) { 
				return 1.0;
			}
			else { 
				return 0.;
			}
		} catch ( OntowrapException owex ) {
			throw new AlignmentException( "Error getting entity name", owex );
		}
	}

	public String findCompoundRelation(Object o1, Object o2) throws AlignmentException {
		try {
			String s1 = ontology1().getEntityName(o1);
			String s2 = ontology2().getEntityName(o2);
			if (s1 == null || s2 == null) return "0.";

			String isA = "&lt;";
			String hasA = "&gt;";

			if (isCompoundSyn(s1,s2) && !s1.equals(s2)) { 
				return isA;
			} else if (isCompoundSyn(s2,s1) && !s2.equals(s1)) { 
				return hasA;
			}
			else { 
				return "0";
			}
		} catch ( OntowrapException owex ) {
			throw new AlignmentException( "Error getting entity name", owex );
		}
	}


	public static boolean isCompound(String a, String b) {
		boolean test = false;

		String[] compounds = a.split("(?<=.)(?=\\p{Lu})");

		if (compounds.length > 2) {

			if (b.equals(compounds[compounds.length-1]) || b.equals(compounds[compounds.length-1]+compounds[compounds.length-2] + compounds[compounds.length-3])) {

				test = true;
			}

		} else if (compounds.length > 1) {

			if (b.equals(compounds[compounds.length-1]) || b.equals(compounds[compounds.length-1]+compounds[compounds.length-2])) {

				test = true;
			}
		}
		return test;

	}

	/**
	 * Find synonyms of concept b in WordNet and compares all of them to the compound head of concept a. If any of the synonyms are equal to the 
	 * compound head of a, b < a. 
	 * @param a
	 * @param b
	 * @return
	 */
	public static boolean isCompoundSyn(String a, String b) {
		boolean test = false;

		String compoundHead = lastToken(a).toLowerCase();

		ArrayList<String> synonyms = getSynonyms(b);
		synonyms.add(b.toLowerCase());

		for (String s : synonyms) {
			if (s.toLowerCase().equals(compoundHead)) {
				test = true;
			}
		}

		return test;
	}


	public static ArrayList<String> getSynonyms(String a) {


		ArrayList<String> synonyms = new ArrayList<String>();

		String[] synsets = WordNet.getSynonyms(a.toLowerCase());

		for (int i = 0; i < synsets.length; i++) {
			synonyms.add(synsets[i].toLowerCase());
		}

		return synonyms;
	}

	public static String lastToken(String inputString) {

		String[] compounds = inputString.split("(?<=.)(?=\\p{Lu})");

		int last = compounds.length;

		return compounds[last-1];
	}


	public static void main(String[] args) {
		String[] onto1 = {"AirportServiceVehicle", "CanadianAirport", "DeicingTruck", "QRoute", "RadialRoute"};
		String[] onto2 = {"Vehicle", "Aerodrome", "Route"};

		for (int i = 0; i < onto1.length; i++) {
			for (int j = 0; j < onto2.length; j++) {
				if (isCompoundSyn(onto1[i], onto2[j])) {
					System.out.println(onto1[i] + " < " + onto2[j]);
				} else {
					System.out.println(onto1[i] + " is not subsumed by " + onto2[j]);
				}
			}
		}

	}
}
package utilities;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owl.align.Cell;

import utilities.Relation;


public class Normalization {
	
	public static void main(String[] args) {
		
		Relation r = new Relation();
		r.setConcept1("C1_1");
		r.setConcept2("C2_1");
		r.setRelationType("=");
		r.setConfidence(5.0);
		
		Relation s = new Relation();
		s.setConcept1("C1_2");
		s.setConcept2("C2_2");
		s.setRelationType("=");
		s.setConfidence(8.0);
		
		Relation t = new Relation();
		t.setConcept1("C1_3");
		t.setConcept2("C2_3");
		t.setRelationType("=");
		t.setConfidence(2.0);
		
		Set<Relation> relSet = new HashSet<Relation>();
		relSet.add(r);
		relSet.add(s);
		relSet.add(t);
		
		Set<Relation> relSetNorm = normalizeConfidence(relSet);
		for (Relation re : relSetNorm) {
			System.out.println(re.getConcept1() + " - " + re.getConcept2() + ": " + re.getConfidence());
		}
		
	}
	
	public static Set<Relation> normalizeConfidence (Set<Relation> relationSet) {
		
		Set<Relation> newRelationSet = new HashSet<Relation>();
		Relation rel = null;
		
		Set<Double> confidenceValues = new HashSet<Double>();
		for (Relation r : relationSet) {
			confidenceValues.add(r.getConfidence());
		}
		
		//get min value in dataset (A)
		double min = Collections.min(confidenceValues);
		
		//get max value in dataset (B)
		double max = Collections.max(confidenceValues);
		
		//set min value (a) in the normalized scale (i.e. 0)
		double normMin = 0;
		
		//set max value (b) in the normalized scale (i.e. 1.0)
		double normMax = 1.0;
		
		//calculate the normalized value for all entities (x) in the dataset
		//a + (x-A)(b-a) / (B-a)
		
		double thisConfidence = 0;		
		
		for (Relation r : relationSet) {
			rel = new Relation();
			thisConfidence = normMin + (r.getConfidence()-min)*(normMax-normMin) / (max-normMin);
			rel.setConcept1(r.getConcept1());
			rel.setConcept2(r.getConcept2());
			rel.setRelationType("=");
			rel.setConfidence(thisConfidence);
			newRelationSet.add(rel);

		}
		
		return newRelationSet;
		
		
	}

}

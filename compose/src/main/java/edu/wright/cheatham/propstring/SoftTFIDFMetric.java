package edu.wright.cheatham.propstring;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;

import com.wcohen.ss.BasicStringWrapperIterator;
import com.wcohen.ss.JaroWinkler;
import com.wcohen.ss.SoftTFIDF;
import com.wcohen.ss.api.StringWrapper;
import com.wcohen.ss.api.Tokenizer;
import com.wcohen.ss.tokens.SimpleTokenizer;

public class SoftTFIDFMetric {

	private SoftTFIDF distance;
	private double jwThreshold = 0.9;
	
	
	public SoftTFIDFMetric() {
		super();
		
	    Tokenizer tokenizer = new SimpleTokenizer(true, false);
		distance = new SoftTFIDF(tokenizer, new JaroWinkler(), jwThreshold);
	}
	
	
	public SoftTFIDFMetric(double threshold) {
		super();
		
		jwThreshold = threshold;
	    Tokenizer tokenizer = new SimpleTokenizer(true, false);
		distance = new SoftTFIDF(tokenizer, new JaroWinkler(), jwThreshold);
	}
	
	
	public void init(OWLOntology ontologyA, OWLOntology ontologyB) {
		
        List<StringWrapper> list = new ArrayList<StringWrapper>();
        
		Set<OWLEntity> set = ontologyA.getSignature();

		for (OWLEntity e: set) {
			String label = Preprocessing.preprocess(e, ontologyA);
			list.add(distance.prepare(label));
		}
		
		set = ontologyB.getSignature();

		for (OWLEntity e: set) {
			String label = Preprocessing.preprocess(e, ontologyB);
			list.add(distance.prepare(label));
		}
        
        distance.train(new BasicStringWrapperIterator(list.iterator()));
	}
	
	
	public void init(Set<OWLEntity> setA, OWLOntology ontologyA, 
			Set<OWLEntity> setB, OWLOntology ontologyB) {
		
        List<StringWrapper> list = new ArrayList<StringWrapper>();

		for (OWLEntity e: setA) {
			String label = Preprocessing.preprocess(e, ontologyA);
			list.add(distance.prepare(label));
		}

		for (OWLEntity e: setB) {
			String label = Preprocessing.preprocess(e, ontologyB);
			list.add(distance.prepare(label));
		}
        
        distance.train(new BasicStringWrapperIterator(list.iterator()));
	}
	

	public double compute(String a, String b) {
        return distance.score(a, b);
	}
	
	public static void main(String[] args) {
		
		String s1 = "vehicle transport human person diff";
		String s2 = "vehicle transport human person";
		
		SoftTFIDFMetric measure = new SoftTFIDFMetric();
		
		System.out.println("The score between s1 and s2 is " + measure.compute(s1, s2));
		
	}
}

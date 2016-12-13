package no.ntnu.idi.compose.Matchers;

import java.util.Properties;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;

import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.ontowrap.OntowrapException;
import no.ntnu.idi.compose.Preprocess.Preprocessor;
import fr.inrialpes.exmo.ontosim.string.CommonWords;


public class AnnotationsAlignment extends ObjectAlignment implements AlignmentProcess {


	final double threshold = 0.05;
	Analyzer analyzer = new StopAnalyzer();

	/**
	 * Instantiates the CommonWords object from OntoSim
	 */
	static CommonWords commonWords = new CommonWords();

	public void align(Alignment alignment, Properties param) throws AlignmentException {

		try {
			// Match classes
			for ( Object cl2: ontology2().getClasses() ){
				for ( Object cl1: ontology1().getClasses() ){

					// add mapping into alignment object 
					addAlignCell(cl1,cl2, "=", jaccardScore(cl1,cl2));  
				}

			}

		} catch (Exception e) { e.printStackTrace(); }
	}

	public double jaccardScore(Object o1, Object o2) throws OntowrapException {

		Set<String> o1Annotations = ontology1().getEntityAnnotations(o1);
		Set<String> o2Annotations = ontology2().getEntityAnnotations(o2);

		String processedStringS1 = Preprocessor.tokenize(analyzer,Preprocessor.join(o1Annotations,""));
		String processedStringS2 = Preprocessor.tokenize(analyzer,Preprocessor.join(o2Annotations,""));
		
		//remove duplicates
		String deduplicatedStringS1 = Preprocessor.removeDuplicates(processedStringS1);
		String deduplicatedStringS2 = Preprocessor.removeDuplicates(processedStringS2);

		String[] s1Array = Preprocessor.split(deduplicatedStringS1);
		String[] s2Array = Preprocessor.split(deduplicatedStringS2);

		double score = 0;

		if (s1Array.length > 1 && s2Array.length > 1) {

			int total = 0;
			int similar = 0;
			int intersection = 0;
			int union = 0;

			similar = Preprocessor.commonWords(s1Array,s2Array);
			intersection = similar;
			total = (s1Array.length + s2Array.length);
			union = total - similar;

			score = (double)intersection/(double)union;

			if (score > threshold) {
				System.out.println(o1.toString() + " and " + o2.toString() + " has a Jaccard score of " + score);
				System.out.println("[" + processedStringS1 + "] " + " and " + "[" + processedStringS2 + "]");
				System.out.println("\n");
			}


		} else {
			score = 0;
		}

		if (score > threshold) {
			return score;
		} else {
			return 0;
		}


	}

	public double commonWordScore(Object o1, Object o2) throws OntowrapException {

		//get the objects (entities)
		String s1 = ontology1().getEntityName(o1).toLowerCase();
		String s2 = ontology2().getEntityName(o2).toLowerCase();

		Set<String> o1Annotations = ontology1().getEntityAnnotations(o1);
		String joinedO1 = Preprocessor.join(o1Annotations, " ");
		System.out.println("The annotation of " + s1 + " from " + ontology1().getURI().toString() + " is " + joinedO1);

		Set<String> o2Annotations = ontology2().getEntityAnnotations(o2);
		String joinedO2 = Preprocessor.join(o2Annotations, " ");
		System.out.println("The annotation of " + s2 + " from " + ontology2().getURI().toString() + " is " + joinedO2);

		double measure = commonWords.getSim(joinedO1, joinedO2);
		System.out.println("The Common Word Score for " + s1 + " and " + s2 + " is " + measure);

		return measure;



	}

}



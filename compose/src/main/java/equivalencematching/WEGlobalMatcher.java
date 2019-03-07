package equivalencematching;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;

import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.ontosim.vector.CosineVM;
import fr.inrialpes.exmo.ontowrap.OntowrapException;
import wordembedding.VectorConcept;

/**
 * Matches concepts from two ontologies based on their global vectors (average of label vectors and comment vectors).
 * @author audunvennesland
 *
 */
public class WEGlobalMatcher extends ObjectAlignment implements AlignmentProcess {
	
	//TODO: The vector files should be created in run-time
	
	 static String vectorFileOnto1 = "./files/ATMOntoCoreMerged.txt";
	 static String vectorFileOnto2 = "./files/airm-mono.txt";
	
	 File vc1File = new File(vectorFileOnto1);
	 File vc2File = new File(vectorFileOnto2);
	
		public WEGlobalMatcher(String vectorFileOnto1Name, String vectorFileOnto2Name) {
			vectorFileOnto1 = vectorFileOnto1Name;
			vectorFileOnto2 = vectorFileOnto2Name;
		}
		
		//standard constructor
		public WEGlobalMatcher() {

		}
		
		public static void main(String[] args) throws AlignmentException {
			File ontoFile1 = new File("./files/_PHD_EVALUATION/ATMONTO-AIRM/ONTOLOGIES/ATMOntoCoreMerged.owl");
			File ontoFile2 = new File("./files/_PHD_EVALUATION/ATMONTO-AIRM/ONTOLOGIES/airm-mono.owl");
			
			AlignmentProcess a = new WEGlobalMatcher(vectorFileOnto1, vectorFileOnto2);
			a.init(ontoFile1.toURI(), ontoFile2.toURI());
			Properties params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	
			BasicAlignment WEGlobalMatcherAlignment = new BasicAlignment();

			WEGlobalMatcherAlignment = (BasicAlignment) (a.clone());

			WEGlobalMatcherAlignment.normalise();
			
			
		}

	public void align(Alignment alignment, Properties param) throws AlignmentException {
		
		int counter = 0;
		
		//need to make sure that the vector files are applied in the same order as the ontologies
		String onto1 = ontology1().getFile().toString();
		String onto1Substring = onto1.substring(onto1.lastIndexOf("/")+1, onto1.lastIndexOf("."));
		String onto2 = ontology2().getFile().toString();
		String onto2Substring = onto2.substring(onto2.lastIndexOf("-")+1, onto2.lastIndexOf("."));
		
		String vector1 = vectorFileOnto1;
		String vector1Substring = vector1.substring(vector1.lastIndexOf("/")+1, vector1.lastIndexOf("."));
		String vector2 = vectorFileOnto2;
		String vector2Substring = vector2.substring(vector2.lastIndexOf("-")+1, vector2.lastIndexOf("."));
		
		File newVector1 = null;
		File newVector2 = null;
		
		System.out.println("onto1: " + onto1 + ", vector1: " + vector1.toString());
		System.out.println("onto1Substring: " + onto1Substring + ", vector1Substring: " + vector1Substring);
		
		if (onto1Substring.equals(vector1Substring)) {
			System.out.println("The ontofile and the vectorfile match");
			newVector1 = new File(vector1);
			newVector2 = new File(vector2);
		} else {
			newVector1 = new File(vector2);
			newVector2 = new File(vector1);
		}

		try {
			// Match classes
			for ( Object cl2: ontology2().getClasses() ){
				for ( Object cl1: ontology1().getClasses() ){

					// add mapping into alignment object 
					addAlignCell(cl1,cl2, "=", wordembeddingScore(cl1,cl2, newVector1, newVector2));  
					
					counter++;
					System.out.println("\n" + counter + " out of 140910 operations run");
				}

			}

		} catch (Exception e) { e.printStackTrace(); }
	}


	public double wordembeddingScore(Object o1, Object o2, File vc1File, File vc2File) throws OntowrapException, FileNotFoundException {
		

//		//get the vector concepts for the ontology files
		vc1File = new File(vectorFileOnto1); 
		vc2File = new File(vectorFileOnto2);

		//each concept in both ontologies being matched are represented as a set of VectorConcepts
		Set<VectorConcept> vc1Set = VectorConcept.populate(vc1File);
		Set<VectorConcept> vc2Set = VectorConcept.populate(vc2File);

		//get the objects (entities) being matched
		String s1 = ontology1().getEntityName(o1).toLowerCase();
		String s2 = ontology2().getEntityName(o2).toLowerCase();
		
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

	}




}



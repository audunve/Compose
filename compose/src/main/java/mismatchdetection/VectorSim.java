package mismatchdetection;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import fr.inrialpes.exmo.align.impl.BasicConfidence;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.rel.A5AlgebraRelation;
import fr.inrialpes.exmo.ontosim.vector.CosineVM;
import utilities.OntologyOperations;
import utilities.StringUtilities;
import utilities.VectorExtractor;

public class VectorSim {

	private static File Skybrary_vectorFile = new File("./files/_PHD_EVALUATION/EMBEDDINGS/processedFile_Skybrary.txt");
	private static File Wikipedia_vectorFile = new File("./files/_PHD_EVALUATION/EMBEDDINGS/processedFileWikipedia_lemmatized.txt");

	public static void main(String[] args) throws IOException, OWLOntologyCreationException, AlignmentException {
		
//		File ontoFile1 = new File("./files/ESWC_ATMONTO_AIRM/Bibframe.rdf");
//		File ontoFile2 = new File("./files/ESWC_ATMONTO_AIRM/schema-org.owl");
//		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
//		OWLOntology onto1 = manager.loadOntologyFromOntologyDocument(ontoFile1);
//		OWLOntology onto2 = manager.loadOntologyFromOntologyDocument(ontoFile2);	
//		
//		Map<String, ArrayList<Double>> vectorMap = VectorExtractor.createVectorMap(WikipediaReduced_vectorFile);
//		
//		Set<String> relationsWithHighestEmbeddingScore = new HashSet<String>();
//		
//		double defSim = 0;
//		
//		for (OWLClass ci : onto1.getClassesInSignature()) {
//			for (OWLClass cj : onto2.getClassesInSignature()) {
//					defSim = computeDefSim(StringUtilities.splitCompounds(ci.getIRI().getFragment()), StringUtilities.splitCompounds(cj.getIRI().getFragment()), vectorMap);
//					if (defSim > 0.95) {
//						relationsWithHighestEmbeddingScore.add(ci.getIRI().getFragment() + " - " + cj.getIRI().getFragment() + ": " + defSim);
//				}
//			}
//		}
//		
//		
//		System.out.println("relationsWithHighestEmbeddingScore contains " + relationsWithHighestEmbeddingScore.size() + " relations");
//		
//		for (String s : relationsWithHighestEmbeddingScore) {
//			System.out.println(s);
//		}
		
		String def1 = "Information about the organization and arrangement of a collection of items. For instance, for computer files, "
				+ "organization and arrangement information may be the file structure and sort sequence of a file; for visual materials, "
				+ "this information may be how a collection is arranged.";
		String def2 = "An organization that provides flights for passengers.";
		
		Set<String> def1Set = StringUtilities.tokenizeToSet(def1.replaceAll("[^a-zA-Z0-9\\s]", ""), true);
	
		Map<String, ArrayList<Double>> vectorMap = VectorExtractor.createVectorMap(Wikipedia_vectorFile);

		double sim = computeDefSim(def1, def2, vectorMap);
		
		System.out.println("The similarity is " + sim);
		
		/*Map<String, ArrayList<Double>> vectorMap = VectorExtractor.createVectorMap(WikipediaReduced_vectorFile);
		
		File ontoFile1 = new File("./files/bibframe.rdf");
		File ontoFile2 = new File("./files/schema-org.owl");
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto1 = manager.loadOntologyFromOntologyDocument(ontoFile1);
		OWLOntology onto2 = manager.loadOntologyFromOntologyDocument(ontoFile2);
		
		Set<String> onto1Defs = new HashSet<String>();
		URIAlignment classDefAlignment = new URIAlignment();
		
		URI onto1URI = onto1.getOntologyID().getOntologyIRI().toURI();
		URI onto2URI = onto2.getOntologyID().getOntologyIRI().toURI();
		
		classDefAlignment.init( onto1URI, onto2URI, A5AlgebraRelation.class, BasicConfidence.class );
		
		for (OWLClass ci : onto1.getClassesInSignature()) {
			onto1Defs.addAll(OntologyOperations.getClassDefinitionsFull(onto1, ci));
		}

		Set<String> onto2Defs = new HashSet<String>();
		
		for (OWLClass cj : onto2.getClassesInSignature()) {
			onto2Defs.addAll(OntologyOperations.getClassDefinitionsFull(onto2, cj));			
		}
		
		
		Set<String> simDefs = new HashSet<String>();
		
		double defSim = 0;
		
		for (String ci : onto1Defs) {
			for (String cj : onto2Defs) {
				//if (ops1SB.toString().trim().length() > 0 && ops2SB.toString().trim().length() > 0) {
				System.out.println("Matching " + ci + " and " + cj);
				if ((!ci.isEmpty() && ci != null) && (!cj.isEmpty() && cj != null)) {
				defSim = computeDefSim(ci, cj, vectorMap);
				} else {
					defSim = 0;
				}
				if (defSim > 0.9 ) {
					simDefs.add(ci + ";" + cj + ";" + defSim);
				}
			}
		}
		
		System.out.println("Printing relations and their definition similarity. There are " + simDefs.size() + " relations");
		for (String s : simDefs) {
			System.out.println(s);
		}*/
		
	}

	public static double computeDefSim (String def1, String def2, Map<String, ArrayList<Double>> vectorMap) throws IOException {
		double sim = 0;
		ArrayList<ArrayList<Double>> vectorsDef1 = new ArrayList<ArrayList<Double>>();
		ArrayList<ArrayList<Double>> vectorsDef2 = new ArrayList<ArrayList<Double>>();

		//if (ops1SB.toString().trim().length() > 0 && ops2SB.toString().trim().length() > 0) {
		
		//get the individual words from the defs, remove stopwords and non-character symbols
		
		Set<String> def1Set = StringUtilities.tokenizeToSet(def1.replaceAll("[^a-zA-Z0-9\\s]", ""), true);

		for (String s : def1Set) {
			if (vectorMap.containsKey(s)) {
				vectorsDef1.add(vectorMap.get(s));
			}
		}
		
		
		Set<String> def2Set = StringUtilities.tokenizeToSet(def2.replaceAll("[^a-zA-Z0-9\\s]", ""), true);

		for (String s : def2Set) {
			if (vectorMap.containsKey(s)) {
				vectorsDef2.add(vectorMap.get(s));
			}
		}
		
		//get their average vectors
		ArrayList<Double> def1Vectors = getAVGVectors(vectorsDef1);

		ArrayList<Double> def2Vectors = getAVGVectors(vectorsDef2);
		
		if (!def1Vectors.isEmpty() && !def2Vectors.isEmpty()) {
		
		sim = computeCosSim(def1Vectors, def2Vectors);
		} else {
			sim = 0;
		}

		return sim;
	}

	private static ArrayList<Double> getAVGVectors(ArrayList<ArrayList<Double>> a_input) {
		
		//we need to create average scores for each vector dimension (i.e. the rows of a vector matrix)
		//each averaged vector is represented as an item in the avgs ArrayList
		ArrayList<Double> avgs = new ArrayList<Double>();
		

		//get the number (dimension) of vectors for each entry (should be 300 since we deal with 300 dimensional vectors)
		int numVectors = 300;

		//creating a temporary arraylist<double> that will be used to compute average vectors for each vector dimension
		ArrayList<Double> temp = new ArrayList<Double>();

		for (int i = 0; i < numVectors; i++) {

			//get the vectors for each word
			for (ArrayList<Double> e : a_input) {

				ArrayList<Double> a = e;
				//put each vector in temp
				temp.add(a.get(i));

			}

			double avg = 0;

			//number of entries to create an average from
			int entries = temp.size();

			//for each vector (d) in the temp arraylist
			for (double d : temp) {
				avg += d;
			}


			double newAvg = avg/entries;

			//ensure that vectors are not 0.0 or NaN
			if (newAvg != 0.0 && !Double.isNaN(newAvg)) {
				avgs.add(newAvg);

			}

		}

		return avgs;
	}

	private static double computeCosSim(ArrayList<Double> a1, ArrayList<Double> a2) {
		double sim = 0;

		double[] vec1 = ArrayUtils.toPrimitive(a1.toArray((new Double[a1.size()])));	
		double[] vec2 = ArrayUtils.toPrimitive(a2.toArray((new Double[a2.size()])));	

		//measure the cosine similarity between the vector dimensions of these two entities
		CosineVM cosine = new CosineVM();

		if (vec1 != null && vec2 != null) {

			sim = cosine.getSim(vec1, vec2);

		}

		return sim;
	}

}

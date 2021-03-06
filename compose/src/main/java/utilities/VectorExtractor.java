package utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Map.Entry;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;

import utilities.StringUtilities;
import utilities.MathUtils;

/**
 * Extracts VectorConcepts by retrieving vectors from a Word Embedding file according to concepts in an ontology.
 * @author audunvennesland
 * 21. sep. 2017 
 */
public class VectorExtractor {

	/**
	 * An OWLOntologyManager manages a set of ontologies. It is the main point
	 * for creating, loading and accessing ontologies.
	 */
	static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	/**
	 * The OWLReasonerFactory represents a reasoner creation point.
	 */
	static OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();

	static OWLDataFactory factory = manager.getOWLDataFactory();

	//private static DecimalFormat df6 = new DecimalFormat(".######");

	public static String getConceptURI(OWLClass cls) {
		String conceptURI = cls.getIRI().toString();

		return conceptURI;
	}

	/**
	 * Returns the label (i.e. the concept name without prefix) from an OWL class
	 * @param cls the input OWL class
	 * @return label (string) without prefix
	 */
	public static String getLabel (OWLClass cls) {
		String label = cls.getIRI().getFragment().toString().toLowerCase();

		return label;
	}

	/**
	 * Returns a set of string tokens from the RDFS comment associated with an OWL class
	 * @param onto The ontology holding the OWL class
	 * @param cls The OWL class
	 * @return A string representing the set of tokens from a comment without stopwords
	 * @throws IOException
	 */
	public static String getComment (OWLOntology onto, OWLClass cls) throws IOException {

		String comment = null;
		String commentWOStopWords = null;

		for(OWLAnnotation a : cls.getAnnotations(onto, factory.getRDFSComment())) {
			OWLAnnotationValue value = a.getValue();
			if(value instanceof OWLLiteral) {
				comment = ((OWLLiteral) value).getLiteral().toString();
				commentWOStopWords = StringUtilities.removeStopWords(comment);
			}
		}

		return commentWOStopWords;

	}

	/**
	 * Returns a boolean stating if two input strings are equal
	 * @param input An input string from the vector file checked for equality
	 * @param cls An input string representing the class label checked for equality
	 * @return A boolean stating if two strings are equal
	 */
	public static boolean stringMatch(String input, String cls) {
		boolean match = false;

		if (input.equals(cls)) {
			match = true;
		} else {
			match = false;
		}


		return match;
	}

	/**
	 * Returns a boolean stating whether a term is considered a compound term
	 * (e.g. ElectronicBook)
	 * 
	 * @param a
	 *            the input string tested for being compound or not
	 * @return boolean stating whether the input string is a compound or not
	 */
	public static boolean isCompound(String a) {
		boolean test = false;

		String[] compounds = a.split("(?<=.)(?=\\p{Lu})");

		if (compounds.length > 1) {
			test = true;
		}

		return test;
	}

	

	/**
	 * Checks whether a string contains only letters 
	 * @param name The string checked for containing only letters
	 * @return A boolean stating if a string contains only letters
	 */
	public static boolean isAlpha(String name) {
		char[] chars = name.toCharArray();

		for (char c : chars) {
			if(!Character.isLetter(c)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Averages a set of vectors
	 * @param inputVectors ArrayList holding a set of input vectors
	 * @return an average of all input vectors
	 */
	public static double averageVectors (ArrayList<Double> inputVectors) {

		int num = inputVectors.size();

		double sum = 0;

		for (Double d : inputVectors) {
			sum+=d;
		}

		double averageVectors = sum/num;

		return averageVectors;
	}



	/**
	 * Takes a file of words and corresponding vectors and creates a Map where the word in each line is key and the vectors are values (as ArrayList<Double>)
	 * @param vectorFile A file holding a word and corresponding vectors on each line
	 * @return A Map<String, ArrayList<Double>> where the key is a word and the value is a list of corresponding vectors
	 * @throws FileNotFoundException
	 */
	public static Map<String, ArrayList<Double>> createVectorMap (File vectorFile) throws FileNotFoundException {

		Map<String, ArrayList<Double>> vectorMap = new HashMap<String, ArrayList<Double>>();

		Scanner sc = new Scanner(vectorFile);

		//read the file holding the vectors and extract the concept word (first word in each line) as key and the vectors as ArrayList<Double> as value in a Map
		while (sc.hasNextLine()) {

			String line = sc.nextLine();
			String[] strings = line.split(" ");

			//get the word, not the vectors
			String word1 = strings[0];

			//get the vectors and put them in an array list
			ArrayList<Double> vec = new ArrayList<Double>();
			for (int i = 1; i < strings.length; i++) {
				vec.add(Double.valueOf(strings[i]));
			}
			//put the word and associated vectors in the vectormap
			vectorMap.put(word1, vec);

		}
		sc.close();

		return vectorMap;
	}

	/**
	 * Checks if the vectorMap contains the label of an OWL class as key and if so the vectors of the label are returned. 
	 * @param cls An input OWL class
	 * @param vectorMap The Map holding words and corresponding vectors
	 * @return a set of vectors (as a string) associated with the label
	 */
	public static String getLabelVector(OWLClass cls, Map<String, ArrayList<Double>> vectorMap) {

		StringBuffer sb = new StringBuffer();

		ArrayList<Double> labelVectors = new ArrayList<Double>();
		Map<String, ArrayList<Double>> compoundVectors = new HashMap<String, ArrayList<Double>>();
		String labelVector = null;
		String label = cls.getIRI().getFragment().toString();

		//if the class name is not a compound, turn it into lowercase, 
		if (!isCompound(label)) {

			String lcLabel = label.toLowerCase();

			//if the class name is in the vectormap, get its vectors
			if (vectorMap.containsKey(lcLabel)) {
				labelVectors = vectorMap.get(lcLabel);

				for (double d : labelVectors) {
					sb.append(Double.toString(d) + " ");

				}

			} else {

				labelVectors = null;
			}

			labelVector = sb.toString();

			//if the class name is a compound, split the compounds, and if the vectormap contains ANY of the compounds, extract the vectors from 
			//the compound parts and average them in order to return the vector for the compound class name
		} else if (isCompound(label)) {

			//get the compounds and check if any of them are in the vector file
			String[] compounds = label.split("(?<=.)(?=\\p{Lu})");


			for (int i = 0; i < compounds.length; i++) {
				if (vectorMap.containsKey(compounds[i].toLowerCase())) {
					labelVectors = vectorMap.get(compounds[i].toLowerCase());

					compoundVectors.put(compounds[i].toLowerCase(), labelVectors);


				} else {
					labelVectors = null;
				}
			}

			//we need to create average scores for each vector dimension (i.e. the rows of a vector matrix)
			ArrayList<Double> avgs = new ArrayList<Double>();

			//get the number (dimension) of vectors for each entry (should be 300)
			int numVectors = 300;
			ArrayList<Double> temp = new ArrayList<Double>();

			//creating a temporary arraylist<double> that will be used to compute average vectors for each vector
			for (int i = 0; i < numVectors; i++) {


				for (Entry<String, ArrayList<Double>> e : compoundVectors.entrySet()) {

					ArrayList<Double> a = e.getValue();

					temp.add(a.get(i));

				}


				double avg = 0;

				//number of entries to create an average from
				int entries = temp.size();

				//for each vector (d) in the temporary arraylist
				for (double d : temp) {
					avg += d;

				}

				double newAvg = avg/entries;

				//ensure that vectors are not 0.0 or NaN
				if (newAvg != 0.0 && !Double.isNaN(newAvg)) {
					avgs.add(newAvg);

				}

			}

			for (double d : avgs) {
				sb.append(Double.toString(MathUtils.round(d, 6)) + " ");

			}

			labelVector = sb.toString();

		}

		//the label vector is averaged across all compound parts that are in the vectormap
		return labelVector;


	}



	/**
	 * Returns the average vector of all tokens represented in the RDFS comment for an OWL class
	 * @param onto The ontology holding the OWL class
	 * @param cls The OWL class
	 * @param vectorMap The map of vectors from en input vector file
	 * @return An average vector for all (string) tokens in an RDFS comment
	 * @throws IOException
	 */
	public static String getCommentVector(OWLOntology onto, OWLClass cls, Map<String, ArrayList<Double>> vectorMap) throws IOException {


		Map<String, ArrayList<Double>> allCommentVectors = new HashMap<String, ArrayList<Double>>();
		StringBuffer sb = new StringBuffer();
		String comment = getComment(onto, cls);
		String commentVector = null;

		ArrayList<Double> commentVectors = new ArrayList<Double>();

		if (comment != null && !comment.isEmpty()) {

			//create tokens from comment
			ArrayList<String> tokens = StringUtilities.tokenize(comment, true);

			//put all tokens that have an associated vector in the vectorMap in allCommentVectors along with the associated vector
			for (String s : tokens) {
				if (vectorMap.containsKey(s)) {
					commentVectors = vectorMap.get(s);

					allCommentVectors.put(s, commentVectors);

				} else {
					commentVectors = null;
				}

			}

			//create average vector representing all token vectors in each comment
			ArrayList<Double> avgs = new ArrayList<Double>();

			int numVectors = 0;

			for (Entry<String, ArrayList<Double>> e : vectorMap.entrySet()) {

				numVectors = e.getValue().size();

			}

			for (int i = 0; i < numVectors; i++) {

				ArrayList<Double> temp = new ArrayList<Double>();

				for (Entry<String, ArrayList<Double>> e : allCommentVectors.entrySet()) {

					ArrayList<Double> a = e.getValue();

					temp.add(a.get(i));

				}

				double avg = 0;

				int entries = temp.size();

				for (double d : temp) {
					avg += d;
				}

				double newAvg = avg/entries;


				if (newAvg != 0.0 && !Double.isNaN(newAvg)) {
					avgs.add(newAvg);

				}

			}

			for (double d : avgs) {
				sb.append(Double.toString(MathUtils.round(d, 6)) + " ");

			}

			commentVector = sb.toString();
		} else {
			commentVector = null;
		}

		return commentVector;

	}



	/**
	 * Returns a "global vector", that is an average of a label vector and a comment vector
	 * @param labelVector The average vector for an OWL class´ label
	 * @param commentVector The average vector for all (string) tokens in the OWL class´ RDFS comment
	 * @return a set of vectors averaged between label vectors and comment vectors
	 */
	public static String getGlobalVector(String labelVector, String commentVector) {

		//average label vectors and comment vectors into global vectors
		String[] v1 = null;
		String[] v2 = null;
		StringBuilder sb = new StringBuilder();
		ArrayList<Double> globalVectors = new ArrayList<Double>();
		ArrayList<Double> labelVectors = new ArrayList<Double>();
		ArrayList<Double> commentVectors = new ArrayList<Double>();


		//there is always a label vector
		if (labelVector != null ) {
			v1 = labelVector.split(" ");

			if (v1 != null) {
				for (String s : v1) {
					if (!s.isEmpty()) {
						labelVectors.add(Double.valueOf(s));
					}
				}
			} else {
				labelVectors = null;
			}

		}

		//a fixed dimension of vectors is 300
		int numVectors = 300;

		//if there also are comment vectors, we average the label vector and the comment vector (already averaged between all token vectors for each comment) into a global vector
		if (commentVector!= null && !commentVector.isEmpty()) {
			v2 = commentVector.split(" ");

			for (String t : v2) {
				if (!t.isEmpty()) {
					commentVectors.add(Double.valueOf(t));
				} else {
					commentVectors = null;
				}
			}

			double average = 0;
			for (int i = 0; i < numVectors; i++) {
				if (labelVectors.size() < 1 && commentVectors.size() < 1) {
					sb = null;
				} else if (labelVectors.size() < 1 && commentVectors.size() > 0) {
					average = commentVectors.get(i);
				} else if (labelVectors.size() > 0 && commentVectors.size() < 1) { 
					average = labelVectors.get(i);
				} else {

					if (labelVectors.get(i) == 0.0) {
						average = commentVectors.get(i);
					} else if (commentVectors.get(i) == 0.0) {
						average = labelVectors.get(i);
					} else {

						average = (labelVectors.get(i) + commentVectors.get(i)) / 2;
					}
				}
				globalVectors.add(average);

			}

		} 

		//round the vector value to 6 decimals
		for (double d : globalVectors) {
			sb.append(MathUtils.round(d, 6) + " ");
		}

		String globalVector = sb.toString();

		return globalVector;

	}



	
	/**
	 * The main method
	 * @param args
	 * @throws OWLOntologyCreationException
	 * @throws IOException
	 */
	public static void main(String[] args) throws OWLOntologyCreationException, IOException {

		Scanner scanner = new Scanner(new 
				InputStreamReader(System.in));

		//read the ontology files folder from console
		System.out.println("Enter path to ontology file folder: ");  
		String ontoFileName = scanner.nextLine();

		//read the vector file from console
		System.out.println("Enter path to vector file:");  
		String vectorFileName = scanner.nextLine(); 

		//time with extraction process starts (to present run-time after .the process is completed)
		final long start = System.nanoTime();

		final File ontologyDir = new File(ontoFileName);
		File[] filesInDir = null;

		filesInDir = ontologyDir.listFiles();

		for (int i = 0; i < filesInDir.length; i++) {

			//import ontology
			File ontoFile = new File(filesInDir[i].toString());
			Map<String, ArrayList<Double>> vectorMap = createVectorMap (new File(vectorFileName));
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);
			Set<OWLClass> classes = onto.getClassesInSignature();

			PrintWriter writer = new PrintWriter("./files/ATMONTO_AIRM/vectorOutput" + StringUtilities.stripOntologyName(filesInDir[i].toString()) + ".txt");

			for (OWLClass cls : classes) {

				String labelVector = getLabelVector(cls, vectorMap);
				String commentVector = getCommentVector(onto, cls, vectorMap);
				
				if ((labelVector != null && !labelVector.isEmpty()) && (commentVector == null || commentVector.isEmpty())) {
					writer.println("conceptUri: " + getConceptURI(cls));
					writer.println("label: " + getLabel(cls));
					writer.println("label vector: " + labelVector);
					writer.println("comment: " + null);
					writer.println("comment vector: " + "no vectors for these comment tokens");
					writer.println("global vector: " + labelVector);
					writer.println("\n");
				} else if (labelVector != null && !labelVector.isEmpty()) {
					writer.println("conceptUri: " + getConceptURI(cls));
					writer.println("label: " + getLabel(cls));
					writer.println("label vector: " + labelVector);
					writer.println("comment: " + getComment(onto, cls));
					writer.println("comment vector: " + commentVector);
					writer.println("global vector: " +getGlobalVector(labelVector, commentVector));
					writer.println("\n");
				}


				}
				writer.flush();
				writer.close();

			}
		
			System.out.println("Vectors created!");

			final long duration = System.nanoTime() - start;
			long sec = duration/1000000000;

			System.out.println("The vector extraction took " + sec + " seconds");
		}

	}

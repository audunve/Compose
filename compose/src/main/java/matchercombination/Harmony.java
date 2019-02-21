package matchercombination;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Cell;

import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.BasicConfidence;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.rel.A5AlgebraRelation;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import utilities.AlignmentOperations;
import utilities.Relation;
import utilities.RelationComparator;

public class Harmony {

	public static void main(String[] args) throws AlignmentException, IOException {
		
		//Compute Harmony alignment from folder of alignments
		String folderName = "./files/_PHD_EVALUATION/BIBFRAME-SCHEMAORG/HARMONY/initialAlignments";
		URIAlignment harmonyAlignment = computeHarmonyAlignment(folderName);
		System.out.println("The harmonyAlignment contains " + harmonyAlignment.nbCells() + " relations");
		
		//store the computed Harmony alignment
		AlignmentVisitor renderer = null;
		URIAlignment storedAlignment = new URIAlignment();
		PrintWriter writer = null;

		String alignmentFileName = "./files/_PHD_EVALUATION/BIBFRAME-SCHEMAORG/HARMONY/ComputedHarmonyAlignment.rdf";

		File outputAlignment = new File(alignmentFileName);

		writer = new PrintWriter(
				new BufferedWriter(
						new FileWriter(outputAlignment)), true); 
		renderer = new RDFRendererVisitor(writer);

		URI onto1URI = harmonyAlignment.getOntology1URI();
		URI onto2URI = harmonyAlignment.getOntology2URI();
		
		System.out.println("The URIs are: ");
		System.out.println("onto1URI: " + onto1URI);
		System.out.println("onto2URI: " + onto2URI);
		
		//need to initialise the alignment with ontology URIs and the type of relation (e.g. A5AlgebraRelation) otherwise exceptions are thrown
		storedAlignment.init( onto1URI, onto2URI, A5AlgebraRelation.class, BasicConfidence.class );
		
		storedAlignment = (URIAlignment) harmonyAlignment.clone();

		storedAlignment.normalise();

		storedAlignment.render(renderer);
		writer.flush();
		writer.close();
		
		

//		File inputAlignmentFile = new File("./files/_PHD_EVALUATION/BIBFRAME-SCHEMAORG/HARMONY/bibframe-schema-org-PropertyMatcher.rdf");
//		
//		URIAlignment harmonyAlignment = getHarmonyAlignment(inputAlignmentFile);
//		
//		AlignmentVisitor renderer = null;
//		URIAlignment storedAlignment = null;
//		PrintWriter writer = null;
//
//		String alignmentFileName = "./files/_PHD_EVALUATION/BIBFRAME-SCHEMAORG/HARMONY/Harmony-PropertyMatcher.rdf";
//
//		File outputAlignment = new File(alignmentFileName);
//
//		writer = new PrintWriter(
//				new BufferedWriter(
//						new FileWriter(outputAlignment)), true); 
//		renderer = new RDFRendererVisitor(writer);
//
//		storedAlignment = (URIAlignment) harmonyAlignment.clone();
//
//		storedAlignment.normalise();
//
//		storedAlignment.render(renderer);
//		writer.flush();
//		writer.close();
//
//		//get harmonyvalue for an alignment
//		Map<URIAlignment, Double> harmonyValue = getHarmonyValue(inputAlignmentFile);
//		for (Entry<URIAlignment, Double> e : harmonyValue.entrySet()) {
//			System.out.println("The harmony value is " + e.getValue());
//		}

	}
	
	public static URIAlignment computeHarmonyAlignment(String folderName) throws AlignmentException {
		URIAlignment temp_harmonyAlignment = new URIAlignment();
		Map<URIAlignment, Double> hMap = new HashMap<URIAlignment, Double>();
		
		File folder = new File(folderName);
		File[] filesInDir = folder.listFiles();
		
		for (int i = 0; i < filesInDir.length; i++) {
			
			//get Harmony alignment and Harmony Value
			//System.out.println("Getting the harmony value from alignment " + filesInDir[i]);
			Map<URIAlignment, Double> localHMap = getHarmonyValue(filesInDir[i]);
			hMap.putAll(localHMap);
			
		}
		
		//integrate all localHMap alignment cells after computing their Harmony value
		for (Entry<URIAlignment, Double> e : hMap.entrySet()) {
			double weight = e.getValue();
			
			URI onto1URI = e.getKey().getOntology1URI();
			URI onto2URI = e.getKey().getOntology2URI();
			
			//need to initialise the alignment with ontology URIs and the type of relation (e.g. A5AlgebraRelation) otherwise exceptions are thrown
			temp_harmonyAlignment.init( onto1URI, onto2URI, A5AlgebraRelation.class, BasicConfidence.class );
			
			for (Cell c : e.getKey()) {
				temp_harmonyAlignment.addAlignCell(c.getObject1(), c.getObject2(), c.getRelation().getRelation(), c.getStrength()*weight);
			}
		}
		
		//normalize the confidence of all cells between [0..1]
		//AlignmentOperations.normalizeConfidence(harmonyAlignment);
		
		//remove relations with 0 confidence
		AlignmentOperations.removeZeroConfidenceRelations(temp_harmonyAlignment);
		URIAlignment harmonyAlignment = (URIAlignment) temp_harmonyAlignment.clone();
		
		return harmonyAlignment;
	}
	
	
	/**
	 * Computes the harmony relations from an initial alignment. The harmony relations are those relations that in a similarity matrix has the highest confidence values both row-wise and column-wise
	 * @param alignmentFile
	 * @return
	 * @throws AlignmentException
	   Feb 1, 2019
	 */
	private static Map<URIAlignment, Double> getHarmonyValue(File alignmentFile) throws AlignmentException {
		
		Map<URIAlignment, Double> harmonyMap = new HashMap<URIAlignment, Double>();
		
		AlignmentParser parser = new AlignmentParser();
		BasicAlignment inputAlignment = (BasicAlignment)parser.parse(alignmentFile.toURI().toString());

		//need to initialise the alignment with ontology URIs and the type of relation (e.g. A5AlgebraRelation) otherwise exceptions are thrown
		URIAlignment harmonyAlignment = new URIAlignment();
		harmonyAlignment.init( inputAlignment.getOntology1URI(), inputAlignment.getOntology2URI(), A5AlgebraRelation.class, BasicConfidence.class );
		
		//used for calculating the Harmony value
		int numInitialRelations = inputAlignment.nbCells();
		
		//create similarity matrix
		Relation[][] similarityMatrix = createSimMatrix(inputAlignment);
		
		ArrayList<Relation> harmonyRelations = new ArrayList<Relation>();
		ArrayList<Relation> rowMax = getRowMax(similarityMatrix);
		ArrayList<Relation> colMax = getColMax(similarityMatrix);
		
		for (Relation r : rowMax) {
			for (Relation c : colMax) {
				if (r.getConcept1().equals(c.getConcept1()) && 
						r.getConcept2().equals(c.getConcept2()) && 
						r.getRelationType().equals(c.getRelationType()) && 
						r.getConfidence() == c.getConfidence()) {
					harmonyAlignment.addAlignCell(URI.create(r.getConcept1()), URI.create(r.getConcept2()), r.getRelationType(), r.getConfidence());
					harmonyRelations.add(r);
				}
			}
		}
		
		int numFinalRelations = harmonyAlignment.nbCells();
		
		double harmonyValue = (double) numFinalRelations / (double) numInitialRelations;
		
		harmonyMap.put(harmonyAlignment, harmonyValue);
				
		return harmonyMap;
		
	}
	
	/**
	 * Computes the harmony relations from an initial alignment. The harmony relations are those relations that in a similarity matrix has the highest confidence values both row-wise and column-wise
	 * @param alignmentFile
	 * @return
	 * @throws AlignmentException
	   Feb 1, 2019
	 */
	public static URIAlignment getHarmonyAlignment(File alignmentFile) throws AlignmentException {
		
		AlignmentParser parser = new AlignmentParser();
		BasicAlignment inputAlignment = (BasicAlignment)parser.parse(alignmentFile.toURI().toString());

		//need to initialise the alignment with ontology URIs and the type of relation (e.g. A5AlgebraRelation) otherwise exceptions are thrown
		URIAlignment harmonyAlignment = new URIAlignment();
		harmonyAlignment.init( inputAlignment.getOntology1URI(), inputAlignment.getOntology2URI(), A5AlgebraRelation.class, BasicConfidence.class );
		
		
		//create similarity matrix
		Relation[][] similarityMatrix = createSimMatrix(inputAlignment);
		
		ArrayList<Relation> harmonyRelations = new ArrayList<Relation>();
		ArrayList<Relation> rowMax = getRowMax(similarityMatrix);
		ArrayList<Relation> colMax = getColMax(similarityMatrix);
		
		for (Relation r : rowMax) {
			for (Relation c : colMax) {
				if (r.getConcept1().equals(c.getConcept1()) && 
						r.getConcept2().equals(c.getConcept2()) && 
						r.getRelationType().equals(c.getRelationType()) && 
						r.getConfidence() == c.getConfidence()) {
					harmonyAlignment.addAlignCell(URI.create(r.getConcept1()), URI.create(r.getConcept2()), r.getRelationType(), r.getConfidence());
					harmonyRelations.add(r);
				}
			}
		}
				
		return harmonyAlignment;
		
	}
	
	public URIAlignment combineHarmonyAlignments(String folderName) throws AlignmentException {
		URIAlignment combinedHarmonyAlignment = new URIAlignment();
		
		//combine all alignments into a single alignment
		combinedHarmonyAlignment = AlignmentOperations.combineAlignments(folderName);
		
		
		return combinedHarmonyAlignment;
	}
	

	/**
	 * Creates a matrix from an alignment where the sources and targets are alphabetically sorted to represent them properly in a similarity matrix
	 * @param a input alignment
	 * @return matrix (2D array) holding Relation objects (concept1, concept2, relation type, confidence)
	 * @throws AlignmentException
	   Jan 30, 2019
	 */
	private static Relation[][] createSimMatrix (Alignment a) throws AlignmentException {

		ArrayList<Relation> relArray = new ArrayList<Relation>();
		Relation rel = null;

		for (Cell c : a) {
			relArray.add(rel = new Relation(c.getObject1().toString(), c.getObject2().toString(), c.getRelation().getRelation(), c.getStrength()));
		}

		Collections.sort(relArray, new RelationComparator());

		int numDistinctSources = getNumDistinctSources(relArray);
		int numDistinctTargets = getNumDistinctTargets(relArray);

		Relation[][] simMatrix = new Relation[numDistinctSources][numDistinctTargets];
		int temp = 0;
		for (int i = 0; i < numDistinctSources;i++) {
			for (int j = 0; j < numDistinctTargets; j++) {
				simMatrix[i][j] = relArray.get(temp);
				temp++;
			}
		}

		return simMatrix;
	}

	/**
	 * Retrieves the relations that has the highest confidence value row-wise from a similarity matrix
	 * @param matrix a similarity matrix where source concepts of the relation is represented in rows and target concepts are represented in columns
	 * @return an ArrayList<Relation> with the relations having the highest confidence values row-wise
	   Feb 1, 2019
	 */
	private static ArrayList<Relation> getRowMax (Relation[][] matrix) {

		ArrayList<Relation> rowMaxes = new ArrayList<Relation>();

		int bestRow;
		int bestCol;
		int bestColInRow = 0;

		double rowMax;

		for (int row = 0; row < matrix.length; row++) {
			rowMax = 0;
			bestRow = 0;
			bestCol = bestColInRow;
			for (int col = 0; col < matrix[0].length; col++) {
				bestCol = bestColInRow;
				double colMax = 0;
				colMax = matrix[row][col].getConfidence();

				if (colMax > rowMax) {
					rowMax = colMax;
					bestRow = row;
					bestCol = col;
					bestColInRow = bestCol;				
				}
			} 
			//System.out.println("Highest value in row " + row + " is [" + bestRow + "]["+ bestCol + "]");
			rowMaxes.add(matrix[bestRow][bestCol]);
		}

		return rowMaxes;
	}


	/**
	 * Retrieves the relations that has the highest confidence value column-wise from a similarity matrix
	 * @param matrix matrix a similarity matrix where source concepts of the relation is represented in rows and target concepts are represented in columns
	 * @return an ArrayList<Relation> with the relations having the highest confidence values column-wise
	   Feb 1, 2019
	 */
	private static ArrayList<Relation> getColMax (Relation[][] matrix) {

		ArrayList<Relation> colMaxes = new ArrayList<Relation>();

		int bestCol;
		int bestRow;
		int bestRowInCol = 0;
		double colMax;
		
		for (int col = 0; col < matrix[0].length; col++) {
			colMax = 0;
			bestCol = 0;
			bestRow = bestRowInCol;
			for (int row = 0; row < matrix.length; row++) {
				bestRow = bestRowInCol;
				double rowMax = 0;
				rowMax = matrix[row][col].getConfidence();
				//System.out.println("rowMax for " + matrix[row][col].getConcept1Fragment() + " - " + matrix[row][col].getConcept1Fragment() + " is " + rowMax);
				
				if (rowMax > colMax) {
					
					colMax = rowMax;
					bestCol = col;
					bestRow = row;
					bestRowInCol = bestRow;
				}
			}
			//System.out.println("Highest value in column " + col + " is [" + bestRow + "]["+ bestCol + "]");
			colMaxes.add(matrix[bestRow][bestCol]);
		}
		

		return colMaxes;
	}


	/**
	 * Helper-method that counts the number of distinct source objects from an alignment (a matrix)
	 * @param cellArray
	 * @return
	 * @throws AlignmentException
	   Feb 1, 2019
	 */
	private static int getNumDistinctSources (ArrayList<Relation> cellArray) throws AlignmentException {

		Set<String> sources = new HashSet<String>();

		for (Relation r : cellArray) {
			sources.add(r.getConcept1Fragment());
		}
		int numDistinctSources = sources.size();

		return numDistinctSources;

	}

	/**
	 * Helper-method that counts the number of distinct target objects from an alignment (a matrix)
	 * @param cellArray
	 * @return
	 * @throws AlignmentException
	   Feb 1, 2019
	 */
	private static int getNumDistinctTargets (ArrayList<Relation> cellArray) throws AlignmentException {

		Set<String> targets = new HashSet<String>();

		for (Relation r : cellArray) {
			targets.add(r.getConcept2Fragment());
		}
		int numDistinctTargets = targets.size();

		return numDistinctTargets;

	}



}

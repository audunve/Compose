package test;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;

import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.BasicConfidence;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.rel.A5AlgebraRelation;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import utilities.Relation;
import utilities.RelationComparator;

public class TestSemanticMatrix {

	public static void main(String[] args) throws AlignmentException {

		File alignmentFile = new File("./files/alignment.rdf");
		AlignmentParser parser = new AlignmentParser();
		BasicAlignment a = (BasicAlignment)parser.parse(alignmentFile.toURI().toString());

		Relation[][] matrix = populateMatrix(a);

		System.out.println("Printing similarity matrix");
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				System.out.print(matrix[i][j].getConcept1Fragment() + " - " + matrix[i][j].getConcept2Fragment() + " (" + matrix[i][j].getConfidence() + ")" +  " ");
			}
			System.out.println();
		}

		System.out.println("\nGet the max of each row");
		ArrayList<Relation> maxRowArray = getRowMax(matrix);

		for (Relation r : maxRowArray) {
			System.out.println(r.getConcept1Fragment() + " - " + r.getConcept2Fragment() + " : " + r.getConfidence());
		}
		
		System.out.println("\nGet the max of each column");
		ArrayList<Relation> maxColArray = getColMax(matrix);

		for (Relation r : maxColArray) {
			System.out.println(r.getConcept1Fragment() + " - " + r.getConcept2Fragment() + " : " + r.getConfidence());
		}
		
		URIAlignment harmonyAlignment = getHarmonyAlignment(alignmentFile);
		for (Cell c : harmonyAlignment) {
			System.out.println(c.getObject1AsURI().getFragment() + " - " + c.getObject2AsURI().getFragment() + " - " + c.getRelation().getRelation() + " - " + c.getStrength());
		}

	}
	
	public static URIAlignment getHarmonyAlignment(File alignmentFile) throws AlignmentException {
		
		AlignmentParser parser = new AlignmentParser();
		BasicAlignment a = (BasicAlignment)parser.parse(alignmentFile.toURI().toString());

		//need to initialise the alignment with ontology URIs and the type of relation (e.g. A5AlgebraRelation) otherwise exceptions are thrown
		URIAlignment harmonyAlignment = new URIAlignment();
		harmonyAlignment.init( a.getOntology1URI(), a.getOntology2URI(), A5AlgebraRelation.class, BasicConfidence.class );
		
		ArrayList<Relation> harmonyRelations = new ArrayList<Relation>();
		ArrayList<Relation> rowMax = getRowMax(populateMatrix(a));
		ArrayList<Relation> colMax = getColMax(populateMatrix(a));
		
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

	/**
	 * Creates a matrix from an alignment where the sources and targets are alphabetically sorted
	 * @param a input alignment
	 * @return matrix (2D array) holding Relation objects (concept1, concept2, relation type, confidence)
	 * @throws AlignmentException
	   Jan 30, 2019
	 */
	public static Relation[][] populateMatrix (Alignment a) throws AlignmentException {

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

	public static ArrayList<Relation> getRowMax (Relation[][] matrix) {

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



	public static ArrayList<Relation> getColMax (Relation[][] matrix) {

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



	private static int getNumDistinctSources (ArrayList<Relation> cellArray) throws AlignmentException {

		Set<String> sources = new HashSet<String>();

		for (Relation r : cellArray) {
			sources.add(r.getConcept1Fragment());
		}
		int numDistinctSources = sources.size();

		return numDistinctSources;

	}

	private static int getNumDistinctTargets (ArrayList<Relation> cellArray) throws AlignmentException {

		Set<String> targets = new HashSet<String>();

		for (Relation r : cellArray) {
			targets.add(r.getConcept2Fragment());
		}
		int numDistinctTargets = targets.size();

		return numDistinctTargets;

	}

	// Function to get max element 
	public static void largestInRow(int no_of_rows, double[][] arr) { 
		int i = 0; 

		// Initialize max to 0 at beginning 
		// of finding max element of each row 
		double max = 0; 
		double[] result = new double[no_of_rows]; 
		while (i < no_of_rows) { 
			for (int j = 0; j < arr[i].length; j++) { 
				if (arr[i][j] > max) { 
					max = arr[i][j]; 
				} 
			} 
			result[i] = max; 
			max = 0; 
			i++; 

		} 
		System.out.println(max );
		//printArray(result); 

	} 

	// Function to find the maximum 
	// element of each column. 
	public static void largestInColumn(int cols, double[][] arr) 
	{ 

		for (int i = 0; i < cols; i++) { 

			// Initialize max to 0 at begining 
			// of finding max element of each column 
			double maxm = arr[0][i]; 
			for (int j = 1; j < arr[i].length; j++) 
				if (arr[j][i] > maxm) 
					maxm = arr[j][i]; 

			System.out.println(maxm); 
		} 
	} 


}

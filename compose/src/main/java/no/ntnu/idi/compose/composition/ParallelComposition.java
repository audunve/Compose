package no.ntnu.idi.compose.composition;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.Properties;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Cell;

import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import no.ntnu.idi.compose.matchers.ClassEq_String_Matcher;


/**
 * This class represents a parallel composition, that is, it compares the set of input alignments, and uses different methods to compute a final alignment. 
 * @author audunvennesland
 * 2. feb. 2017
 */
public class ParallelComposition {

	/**
	 * Returns an alignment produced by including only correspondences that are contained in minimum two of the input alignments
	 * @param alignmentFile1 the first input alignment
	 * @param alignmentFile2 the second input alignment
	 * @param alignmentFile3 the third input alignment
	 * @return an alignment containing correspondences that are represented by minimum two input alignments
	 * @throws AlignmentException Base class for all Alignment Exceptions
	 */
	public static Alignment simpleVote(File alignmentFile1, File alignmentFile2, File alignmentFile3) throws AlignmentException {

		Alignment finalAlignment = new URIAlignment();

		//load the alignments
		AlignmentParser parser = new AlignmentParser();
		BasicAlignment a1 = (BasicAlignment)parser.parse(alignmentFile1.toURI().toString());
		BasicAlignment a2 = (BasicAlignment)parser.parse(alignmentFile2.toURI().toString());
		BasicAlignment a3 = (BasicAlignment)parser.parse(alignmentFile3.toURI().toString());

		//TO-DO: Fix this approach! 
		System.out.println("Starting alignment matching!");
		//compare all three alignments, and if a correspondence (a cell) is represented in two (or more) alignments, include it in the final alignment.
		for (Cell cell1 : a1) {
			for (Cell cell2 : a2) {
				for (Cell cell3 : a3) {
					//if cell3.thisCell equals either cell2.thisCell || cell1.thisCell, add cell3.thisCell to the final alignment
					//System.out.println("Checking if " + cell1.getObject1() + " - " + cell1.getObject2() + 
						//	" equals " + cell2.getObject1() + " - " + cell2.getObject2());
					//if (cell1.equals(cell2) || cell1.equals(cell3) || cell2.equals(cell3)) {
					if (cell1.getObject1().equals(cell2.getObject1()) && cell1.getObject2().equals(cell2.getObject2()) || cell1.getObject1().equals(cell3.getObject1()) && cell1.getObject2().equals(cell3.getObject2())) {
						System.out.println("We have a match!");
						System.out.println("Adding " + cell1.getObject1() + " - " + cell1.getObject2() + " to the final alignment");
						finalAlignment.addAlignCell(cell1.getObject1(), cell1.getObject2(), "=", cell1.getStrength());
						
					}
				}
			}
		}


		return finalAlignment;	

	}

	/**
	 * Returns an alignment where the matchers producing them are given different priority depending on the initial ontology profiling. 
	 * This priority determines how much weight alignment correspondences from each matcher should have in the final alignment aggregation.
	 * @param alignmentFile1 the first input alignment
	 * @param alignmentFile2 the second input alignment
	 * @param alignmentFile3 the third input alignment
	 * @return an alignment where correspondences from "prioritized" matchers are preferred
	 * @throws AlignmentException Base class for all Alignment Exceptions
	 */
	public static Alignment completeMatchWithPriority(File alignmentFile1, File alignmentFile2, File alignmentFile3) throws AlignmentException {

		//load the alignments
		AlignmentParser parser = new AlignmentParser();
		BasicAlignment a1 = (BasicAlignment)parser.parse(alignmentFile1.toURI().toString());
		BasicAlignment a2 = (BasicAlignment)parser.parse(alignmentFile2.toURI().toString());
		BasicAlignment a3 = (BasicAlignment)parser.parse(alignmentFile3.toURI().toString());

		//BasicAlignment completeMatchAlignment = new BasicAlignment();
		Alignment completeMatchAlignment = new URIAlignment();
		//BasicAlignment intermediateAlignment = new BasicAlignment();
		Alignment intermediateAlignment = new URIAlignment();

		System.out.println("Printing the initial alignment (wordnet)");
		for (Cell c : a1) {
			System.out.println(c.getObject1() + " - " + c.getObject2() + " : " + c.getStrength());
		}

		//compare correspondences (cells) in a1 and a2. a1 is the prioritised matcher. 
		//If a cell in a2 contains an object that already exists in a1, that cell in a2 should be discarded and the cell in a1 is added to the 
		//intermediate alignment.
		for (Cell cell1 : a1) {
			for (Cell cell2 : a2) {				
				//if the cells aren´t equal (contains similar entities) we can add a new cell
				//if (cell2.getObject1().equals(cell1.getObject1()) || cell2.getObject2().equals(cell1.getObject2())) {
				if (!cell1.equals(cell2)) {
					//if (cell2.compareTo(cell1) == 1) {
					intermediateAlignment.addAlignCell(cell1.getObject1(), cell1.getObject2(), "=", cell1.getStrength());
					//System.out.println("Adding " + cell1.getObject1() + " and " + cell1.getObject2() + " to the intermediate alignment");
					//if the cells are equal in any of the objects, we only add cells from the first alignment
				} else {
					intermediateAlignment.addAlignCell(cell2.getObject1(), cell2.getObject2(), "=", cell2.getStrength());
					//continue;
				}
			}
		}

		System.out.println("Printing the intermediate alignment: ");
		for (Cell c : intermediateAlignment) {
			System.out.println(c.getObject1().toString() + " - " + c.getObject2().toString() + " : " + c.getStrength());
		}

		//compare correspondences (cells) in the current alignment. If a cell in a3 already exists in the previous alignment, add strength to it. 
		for (Cell cell2 : intermediateAlignment) {
			for (Cell cell3 : a3) {
				//if the cells aren´t equal (contains similar entities) we can add a new cell
				//if (cell3.getObject1().equals(cell2.getObject1()) || cell3.getObject2().equals(cell2.getObject2())) {
				if (!cell2.equals(cell3)) {
					completeMatchAlignment.addAlignCell(cell2.getObject1(), cell2.getObject2(), "=", cell2.getStrength());
					//break;
					//if the cells are not equal in any of their objects, we can add cells from a3
				} else {
					completeMatchAlignment.addAlignCell(cell3.getObject1(), cell3.getObject2(), "=", cell3.getStrength());
					//continue;
				}
			}
		}

		System.out.println("Printing the final alignment: ");
		for (Cell c : completeMatchAlignment) {
			System.out.println(c.getObject1().toString() + " - " + c.getObject2().toString() + " : " + c.getStrength());
		}

		return completeMatchAlignment;		
	}


	/**
	 * Test method
	 * @param args
	 * @throws AlignmentException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static void main(String[] args) throws AlignmentException, IOException, URISyntaxException {

		File a1 = new File("./files/experiment_eswc17/alignments/biblio-bibo/a1.rdf");
		File a2 = new File("./files/experiment_eswc17/alignments/biblio-bibo/a2.rdf");
		File a3 = new File("./files/experiment_eswc17/alignments/biblio-bibo/a3.rdf");

		BasicAlignment newAlignment = (BasicAlignment) completeMatchWithPriority(a1, a2, a3);

		//store the new alignment
		File outputAlignment = new File("./files/experiment_eswc17/alignments/biblio-bibo/a4.rdf");

		PrintWriter writer = new PrintWriter(
				new BufferedWriter(
						new FileWriter(outputAlignment)), true); 
		AlignmentVisitor renderer = new RDFRendererVisitor(writer);

		newAlignment.render(renderer);
		writer.flush();
		writer.close();



	}
}

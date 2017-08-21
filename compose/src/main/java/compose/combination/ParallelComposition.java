package compose.combination;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Cell;

import compose.matchers.ISubMatcher;
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.parser.AlignmentParser;


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
	 *//*
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
					System.out.println("Checking if " + cell1.getObject1() + " - " + cell1.getObject2() + 
							" equals " + cell2.getObject1() + " - " + cell2.getObject2());
					//if (cell1.equals(cell2) || cell1.equals(cell3) || cell2.equals(cell3)) {
					if (cell1.getObject1().equals(cell2.getObject1()) && cell1.getObject2().equals(cell2.getObject2()) || cell1.getObject1().equals(cell3.getObject1()) && cell1.getObject2().equals(cell3.getObject2())) {
						System.out.println("We have a match!");
						System.out.println("Adding " + cell1.getObject1() + " - " + cell1.getObject2() + " to the final alignment");
						finalAlignment.addAlignCell(cell1.getObject1(), cell1.getObject2(), "=", cell1.getStrength());
						
					}
				}
			}
		}
		
		System.out.println("Completed alignment matching!");

		return finalAlignment;	

	}*/
	
	public static BasicAlignment simpleVote(Set<Alignment> inputAlignments) throws AlignmentException {

		BasicAlignment simpleVoteAlignment = new URIAlignment();

		Set<Cell> allCells = new HashSet<Cell>();

		// get all cells in all alignments and put them in a set
		for (Alignment a : inputAlignments) {
			// for all cells C in each input alignment
			for (Cell c : a) {
				allCells.add(c);
			}
		}

		// put all cells in an alignment
		BasicAlignment allCellsAlignment = new URIAlignment();
		for (Cell c1 : allCells) {
			allCellsAlignment.addAlignCell(c1.getObject1(), c1.getObject2(), c1.getRelation().toString(),
					c1.getStrength());
		}

		int numAlignments = inputAlignments.size();

		Set<Cell> todel = new HashSet<Cell>();
		Set<Cell> toKeep = new HashSet<Cell>();

		for (Cell currentCell : allCellsAlignment) {

			if (!todel.contains(currentCell)) {
				// get all cells that has the same object1 as c1
				Set<Cell> cells2 = allCellsAlignment.getAlignCells1(currentCell.getObject1());

				if (cells2.size() > 1) {
					// placeholder for cells that contains the same object1 and
					// object 2 as c1
					Set<Cell> toCheck = new HashSet<Cell>();

					Object o2 = currentCell.getObject2();

					for (Cell c2 : cells2) {
						if (o2.equals(c2.getObject2())) {
							toCheck.add(c2);
						}

					}

					if (toCheck.size() >= (numAlignments - 1)) {

						for (Cell c : toCheck) {

							if (c != currentCell && c.getRelation().equals(currentCell.getRelation())) {
								todel.add(c);
								toKeep.add(currentCell);
							}
						}
					}

				} else {
					// toKeep.add(currentCell);
				}
			}
		}

		for (Cell c : toKeep) {
			simpleVoteAlignment.addAlignCell(c.getObject1(), c.getObject2(), c.getRelation().toString(),
					c.getStrength());
		}

		System.out.println("simpleVoteAlignment contains " + simpleVoteAlignment.nbCells() + " cells");

		for (Cell c : simpleVoteAlignment) {
			System.out.println(c.getObject1() + " - " + c.getObject2() + " - " + c.getRelation().toString() + " - "
					+ c.getStrength());
		}

		return simpleVoteAlignment;
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
	public static Alignment completeMatchWithPriority3(File alignmentFile1, File alignmentFile2, File alignmentFile3) throws AlignmentException {

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
	 * Returns an alignment where the matchers producing them are given different priority depending on the initial ontology profiling. 
	 * This priority determines how much weight alignment correspondences from each matcher should have in the final alignment aggregation.
	 * @param alignmentFile1 the first input alignment
	 * @param alignmentFile2 the second input alignment
	 * @param alignmentFile3 the third input alignment
	 * @param alignmentFile3 the fourth input alignment
	 * @return an alignment where correspondences from "prioritized" matchers are preferred
	 * @throws AlignmentException Base class for all Alignment Exceptions
	 */
	public static Alignment completeMatchWithPriority4(File alignmentFile1, File alignmentFile2, File alignmentFile3, File alignmentFile4) throws AlignmentException {

		//load the alignments
		AlignmentParser parser = new AlignmentParser();
		BasicAlignment priAlignment = (BasicAlignment)parser.parse(alignmentFile1.toURI().toString());
		BasicAlignment a2 = (BasicAlignment)parser.parse(alignmentFile2.toURI().toString());
		BasicAlignment a3 = (BasicAlignment)parser.parse(alignmentFile3.toURI().toString());
		BasicAlignment a4 = (BasicAlignment)parser.parse(alignmentFile4.toURI().toString());

		Alignment completeMatchAlignment = new URIAlignment();
		Alignment intermediateAlignment = new URIAlignment();


		//compare correspondences (cells) in priAlignment and a2. priAlignment is from the prioritised matcher. 
		//If a cell in a2 contains an object that already exists in priAlignment, that cell in a2 should be discarded and the cell in priAlignment is added to the 
		//intermediate alignment.
		for (Cell priCell : priAlignment) {
			for (Cell cell2 : a2) {				
				//if the cells aren´t equal (contains similar entities) we can add a new cell
				if (!priCell.equals(cell2)) {
					completeMatchAlignment.addAlignCell(priCell.getObject1(), priCell.getObject2(), "=", priCell.getStrength());
					//if the cells are equal in any of the objects, we only add cells from the first alignment
				} else {
					completeMatchAlignment.addAlignCell(cell2.getObject1(), cell2.getObject2(), "=", cell2.getStrength());
					//continue;
				}
			}
		}

		System.out.println("Printing the intermediate alignment: ");
		for (Cell c : intermediateAlignment) {
			System.out.println(c.getObject1().toString() + " - " + c.getObject2().toString() + " : " + c.getStrength());
		}

		//compare correspondences (cells) in the current alignment. If a cell in a3 already exists in the previous alignment, add strength to it. 
		for (Cell priCell : priAlignment) {
			for (Cell cell3 : a3) {
				//if the cells aren´t equal (contains similar entities) we can add a new cell
				if (!priCell.equals(cell3)) {
					completeMatchAlignment.addAlignCell(priCell.getObject1(), priCell.getObject2(), "=", priCell.getStrength());
					//break;
					//if the cells are not equal in any of their objects, we can add cells from a3
				} else {
					completeMatchAlignment.addAlignCell(cell3.getObject1(), cell3.getObject2(), "=", cell3.getStrength());
					//continue;
				}
			}
		}
		
		//compare correspondences (cells) in the current alignment. If a cell in a3 already exists in the previous alignment, add strength to it. 
				for (Cell priCell : priAlignment) {
					for (Cell cell4 : a4) {
						//if the cells aren´t equal (contains similar entities) we can add a new cell
						if (!priCell.equals(cell4)) {
							completeMatchAlignment.addAlignCell(priCell.getObject1(), priCell.getObject2(), "=", priCell.getStrength());
							//break;
							//if the cells are not equal in any of their objects, we can add cells from a3
						} else {
							completeMatchAlignment.addAlignCell(cell4.getObject1(), cell4.getObject2(), "=", cell4.getStrength());
							//continue;
						}
					}
				}
		
		

		System.out.println("Printing the final alignment: ");
		for (Cell c : completeMatchAlignment) {
			System.out.println(c.getObject1().toString() + " - " + c.getObject2().toString() + " : " + c.getStrength());
		}
		
		((BasicAlignment) completeMatchAlignment).normalise();

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

		File af1 = new File("./files/experiment_eswc17/alignments/biblio-bibo/Subsumption_WordNet.rdf");
		File af2 = new File("./files/experiment_eswc17/alignments/biblio-bibo/Subsumption_SubClass.rdf");
		File af3 = new File("./files/experiment_eswc17/alignments/biblio-bibo/Subsumption_Compound.rdf");
		File af4 = new File("./files/experiment_eswc17/alignments/biblio-bibo/PathMatcher-biblio2bibo.rdf");
		

		BasicAlignment parallelWithPriorityAlignment = (BasicAlignment) completeMatchWithPriority4(af3, af2, af4, af1);

		//BasicAlignment newAlignment = (BasicAlignment) completeMatchWithPriority(a1, a2, a3);
		//BasicAlignment newAlignment = (BasicAlignment) simpleVote(af3, af1, af2);
		
		
		AlignmentParser parser = new AlignmentParser();
		BasicAlignment a1 = (BasicAlignment) parser.parse(af1.toURI().toString());
		BasicAlignment a2 = (BasicAlignment) parser.parse(af2.toURI().toString());
		BasicAlignment a3 = (BasicAlignment) parser.parse(af3.toURI().toString());

		Set<Alignment> inputAlignments = new HashSet<Alignment>();
		inputAlignments.add(a1);
		inputAlignments.add(a2);
		inputAlignments.add(a3);
		
		
		BasicAlignment intersectRelaxedAlignment = (BasicAlignment) simpleVote(inputAlignments);

		//store the new alignment
		//File outputAlignment = new File("./files/GUITest/TestSimpleVote.rdf");
		File outputAlignment = new File("./files/GUITest/TestParallelPriority.rdf");
		//File outputAlignment = new File("./files/GUITest/TestIntersectRelaxed.rdf");
		//File outputAlignment = new File("./files/OAEI2011/301-302/TestPriorityVote.rdf");
		

		PrintWriter writer = new PrintWriter(
				new BufferedWriter(
						new FileWriter(outputAlignment)), true); 
		AlignmentVisitor renderer = new RDFRendererVisitor(writer);

		parallelWithPriorityAlignment.render(renderer);
		writer.flush();
		writer.close();



	}
}

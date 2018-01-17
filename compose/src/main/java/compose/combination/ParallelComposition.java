package compose.combination;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Relation;

import compose.eval.Evaluator;
import compose.matchers.equivalence.ISubMatcher;
import compose.misc.StringUtils;
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.BasicRelation;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.eval.PRecEvaluator;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.parser.AlignmentParser;


/**
 * This class represents a parallel composition, that is, it compares the set of input alignments, and uses different methods to compute a final alignment. 
 * @author audunvennesland
 * 2. feb. 2017
 */

//TO-DO: Should have a combination strategy where all matchers select and offer their "best" correspondences. This could be based on a general threshold for all matchers, or an individual threshold for each matcher (perhaps depending on the profiling scores)
public class ParallelComposition {

	/**
	 * Creates an alignment that includes correspondences that are computed by n-x matchers (e.g. 3 of 4 matchers)
	 * @param inputAlignments A list of all alignments produced by the matchers involved
	 * @return an alignment that includes the "voted" set of correspondences 
	 * @throws AlignmentException
	 */
	public static BasicAlignment simpleVote(ArrayList<Alignment> inputAlignments) throws AlignmentException {

		BasicAlignment simpleVoteAlignment = new URIAlignment();

		ArrayList<Cell> allCells = new ArrayList<Cell>();

		// get all cells in all alignments and put them in a set
		for (Alignment a : inputAlignments) {
			// for all cells C in each input alignment
			for (Cell c : a) {
				allCells.add(c);
			}
		}

		int numAlignments = inputAlignments.size();

		ArrayList<Cell> todel = new ArrayList<Cell>();
		ArrayList<Cell> toKeep = new ArrayList<Cell>();

		for (Cell currentCell : allCells) {
			
			if (!todel.contains(currentCell)) {
				
				// get all cells that has the same object1 as c1
				ArrayList<Cell> sameObj1 = new ArrayList<Cell>();				
				for (Cell c : allCells) {
					if (c.getObject1().equals(currentCell.getObject1())) {
						sameObj1.add(c);
					}
				}
							
				//why bigger than 1 and not?
				if (sameObj1.size() > 1) {
					
					// placeholder for cells that contains the same object1 and
					// object 2 as c1 AND that has the same relation type as currentCell
					ArrayList<Cell> toCheck = new ArrayList<Cell>();

					Object o2 = currentCell.getObject2();
					Relation rCurrent = currentCell.getRelation();

					//checking if the cells in sameObj1 also have the same object 2 as "currentCell", AND that their relation type is the same -> if so add the cells to "toCheck"
					for (Cell c2 : sameObj1) {
						if (o2.equals(c2.getObject2()) && rCurrent.equals(c2.getRelation())) {
							toCheck.add(c2);
						}

					}
										
					//if the number of cells in toCheck (those that have the same object1 and object 2 as currentCell) is represented by numAlignments-1 (e.g.3 of 4 alignments)
					if (toCheck.size() >= (numAlignments - 2)) {

						for (Cell c : toCheck) {

							//checking that c (this cell) in fact is not currentCell
							if (c != currentCell) {
								toKeep.add(c);
								todel.add(currentCell);
								
							}
						}
					}

				} else {
					
				}
			}
		}
		

		for (Cell c : toKeep) {
			simpleVoteAlignment.addAlignCell(c.getObject1(), c.getObject2(), StringUtils.validateRelationType(c.getRelation().getRelation()),
					c.getStrength());
		}


		return simpleVoteAlignment;
	}
	


		/**
		 * NOTE: Currently used in the ComposeGUIMainMenu, but should be replace with simpleVote(ArrayList<Alignment> inputAlignments)
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
					intermediateAlignment.addAlignCell(cell1.getObject1(), cell1.getObject2(), StringUtils.validateRelationType(cell1.getRelation().getRelation()), cell1.getStrength());
					//System.out.println("Adding " + cell1.getObject1() + " and " + cell1.getObject2() + " to the intermediate alignment");
					//if the cells are equal in any of the objects, we only add cells from the first alignment
				} else {
					intermediateAlignment.addAlignCell(cell2.getObject1(), cell2.getObject2(), StringUtils.validateRelationType(cell2.getRelation().getRelation()), cell2.getStrength());
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
					completeMatchAlignment.addAlignCell(cell2.getObject1(), cell2.getObject2(), StringUtils.validateRelationType(cell2.getRelation().getRelation()), cell2.getStrength());
					//break;
					//if the cells are not equal in any of their objects, we can add cells from a3
				} else {
					completeMatchAlignment.addAlignCell(cell3.getObject1(), cell3.getObject2(), StringUtils.validateRelationType(cell3.getRelation().getRelation()), cell3.getStrength());
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
	//TO-DO: Should have an ordered list of the alignments as parameter instead of individual files
	public static Alignment completeMatchWithPriority4(File alignmentFile1, File alignmentFile2, File alignmentFile3, File alignmentFile4) throws AlignmentException {

		//load the alignments
		//TO-DO: Should have an ordered list of the alignments
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
		
		//((BasicRelation) (c1.getRelation())).getPrettyLabel()
		for (Cell priCell : priAlignment) {
			for (Cell cell2 : a2) {				
				//if the cells aren´t equal (contains similar entities) we can add a new cell
				if (!priCell.equals(cell2)) {
					completeMatchAlignment.addAlignCell(priCell.getObject1(), priCell.getObject2(), StringUtils.validateRelationType(priCell.getRelation().getRelation()), priCell.getStrength());
					//if the cells are equal in any of the objects, we only add cells from the first alignment
				} else {
					completeMatchAlignment.addAlignCell(cell2.getObject1(), cell2.getObject2(), StringUtils.validateRelationType(cell2.getRelation().getRelation()), cell2.getStrength());
					//continue;
				}
			}
		}

		//System.out.println("Printing the intermediate alignment: ");
		//for (Cell c : intermediateAlignment) {
		//	System.out.println(c.getObject1().toString() + " - " + c.getObject2().toString() + " : " + c.getStrength());
		//}

		//compare correspondences (cells) in the current alignment. If a cell in a3 already exists in the previous alignment, add strength to it. 
		for (Cell priCell : priAlignment) {
			for (Cell cell3 : a3) {
				//if the cells aren´t equal (contains similar entities) we can add a new cell
				if (!priCell.equals(cell3)) {
					completeMatchAlignment.addAlignCell(priCell.getObject1(), priCell.getObject2(), StringUtils.validateRelationType(priCell.getRelation().getRelation()), priCell.getStrength());
					//break;
					//if the cells are not equal in any of their objects, we can add cells from a3
				} else {
					completeMatchAlignment.addAlignCell(cell3.getObject1(), cell3.getObject2(), StringUtils.validateRelationType(cell3.getRelation().getRelation()), cell3.getStrength());
					//continue;
				}
			}
		}
		
		//compare correspondences (cells) in the current alignment. If a cell in a3 already exists in the previous alignment, add strength to it. 
				for (Cell priCell : priAlignment) {
					for (Cell cell4 : a4) {
						//if the cells aren´t equal (contains similar entities) we can add a new cell
						if (!priCell.equals(cell4)) {
							completeMatchAlignment.addAlignCell(priCell.getObject1(), priCell.getObject2(), StringUtils.validateRelationType(priCell.getRelation().getRelation()), priCell.getStrength());
							//break;
							//if the cells are not equal in any of their objects, we can add cells from a3
						} else {
							completeMatchAlignment.addAlignCell(cell4.getObject1(), cell4.getObject2(), StringUtils.validateRelationType(cell4.getRelation().getRelation()), cell4.getStrength());
							//continue;
						}
					}
				}
		
		

//		System.out.println("Printing the final alignment: ");
//		for (Cell c : completeMatchAlignment) {
//			System.out.println(c.getObject1().toString() + " - " + c.getObject2().toString() + " : " + c.getStrength());
//		}
		
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

		File af1 = new File("./files/OAEI2009/alignments/103/101-103-Compound0.9.rdf");
		File af2 = new File("./files/OAEI2009/alignments/103/101-103-OppositeSubclass0.9.rdf");
		File af3 = new File("./files/OAEI2009/alignments/103/101-103-Parent0.9.rdf");
		File af4 = new File("./files/OAEI2009/alignments/103/101-103-WNHyponym0.9.rdf");
		
		ArrayList<File> files = new ArrayList();
		
		files.add(af3);
		files.add(af1);
		files.add(af2);
		files.add(af4);

		//BasicAlignment parallelWithPriorityAlignment = (BasicAlignment) completeMatchWithPriority4(files.get(0), files.get(1), files.get(2), files.get(3));
		//System.out.println("Running " + files.get(0).getName() + ", " + files.get(1).getName() + ", " + files.get(2).getName() + ", " + files.get(3).getName());
		
		AlignmentParser parser = new AlignmentParser();
		BasicAlignment a1 = (BasicAlignment) parser.parse(af1.toURI().toString());
		BasicAlignment a2 = (BasicAlignment) parser.parse(af2.toURI().toString());
		BasicAlignment a3 = (BasicAlignment) parser.parse(af3.toURI().toString());
		BasicAlignment a4 = (BasicAlignment) parser.parse(af4.toURI().toString());

		ArrayList<Alignment> inputAlignments = new ArrayList<Alignment>();
		inputAlignments.add(a1);
		inputAlignments.add(a2);
		inputAlignments.add(a3);
		inputAlignments.add(a4);
		
		int numCells = (a1.nbCells() + a2.nbCells() + a3.nbCells() + a4.nbCells());
		
		System.out.println("Printing all cells in inputAlignments (" + numCells + ") cells");
		
		for (Alignment a : inputAlignments) {
			for (Cell c : a) {
				System.out.println(c.getObject1AsURI().getFragment() + " - " + c.getObject2AsURI().getFragment() + " : " + c.getRelation().getRelation() + " : " + c.getStrength());
			}
		}
		
		BasicAlignment simpleVoteAlignment = simpleVote(inputAlignments);

		File outputAlignment = new File("./files/OAEI2009/combinedAlignments/SimpleVote(comp-osc-par-wn).rdf");

		PrintWriter writer = new PrintWriter(
				new BufferedWriter(
						new FileWriter(outputAlignment)), true); 
		AlignmentVisitor renderer = new RDFRendererVisitor(writer);

		simpleVoteAlignment.render(renderer);
		writer.flush();
		writer.close();
		
		//perform evaluation
		Evaluator eval = new Evaluator();

		String evaluatedAlignment = outputAlignment.getAbsolutePath();
		String referenceAlignment = "./files/OAEI2009/103/refalign.rdf";
		
		eval.evaluateSingleAlignment(evaluatedAlignment,referenceAlignment);
		

	}
}

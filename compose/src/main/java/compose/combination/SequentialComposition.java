package compose.combination;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Cell;

import compose.misc.StringUtils;
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.parser.AlignmentParser;


public class SequentialComposition {

	static double threshold;
	static File outputAlignment = null;

	/**
	 * Returns an alignment where correspondences that are identified both by the previous matcher and the current matcher are strengthened. 
	 * This combination strategy considers the order of the alignments (i.e. only the first matcher (get(0)) in the ArrayList
	 *  
	 * @param inputAlignments an ArrayList of all alignments to be combined
	 * @return an alignment with weighted correspondences
	 * @throws AlignmentException
	 */
	public static Alignment weightedSequentialComposition(ArrayList<Alignment> inputAlignments) throws AlignmentException {

		Alignment newAlignment = new URIAlignment();

		//set the first alignment in the array list as "prioritised alignment" and remove it from the arraylist
		Alignment priAlignment = inputAlignments.get(0);
		
		//create a list of cells from the "prioritised alignment"
		ArrayList<Cell> priCellsList = new ArrayList<Cell>();
		for (Cell c : priAlignment) {
			priCellsList.add(c);
		}
		
		//create a list of cells from the other alignments
		ArrayList<Cell> allOtherCellsList = new ArrayList<Cell>();		
		for (Alignment a : inputAlignments) {
			for (Cell c : a) {
				allOtherCellsList.add(c);
			}
		}
		
		//map to hold number of occurrences of each cell from the prioritised alignment in the other alignments
		Map<Cell, Integer> cellCountMap = new HashMap<Cell, Integer>();

		for (Cell c1 : priCellsList) {
			int counter = 0;
			for (Cell c2: allOtherCellsList) {
				if (c2.equals(c1)) {
					counter+=1;
				} 
				
			}
			cellCountMap.put(c1, counter);
		}
		
		System.out.println("Printing cellCountMap");
		
		for (Entry<Cell, Integer> entry : cellCountMap.entrySet()) {
			System.out.println(entry.getKey().getObject1AsURI().getFragment() + " - " + entry.getKey().getObject2AsURI().getFragment() + " : " + entry.getKey().getRelation().getRelation() + " = " + entry.getValue());
		}
		
		for (Entry<Cell, Integer> e : cellCountMap.entrySet()) {
			//if no other alignments have this cell -> reduce its confidence by 50 percent
			if (e.getValue() == (0)) {
				newAlignment.addAlignCell(e.getKey().getObject1(), e.getKey().getObject2(), StringUtils.validateRelationType(e.getKey().getRelation().getRelation()), e.getKey().getStrength()-0.41);
				//if one other alignment have this cell
			} else if (e.getValue() == (1)) {
				newAlignment.addAlignCell(e.getKey().getObject1(), e.getKey().getObject2(), StringUtils.validateRelationType(e.getKey().getRelation().getRelation()), e.getKey().getStrength()-0.2);
			//if two other alignments have this cell
			} else if (e.getValue() == (2)) {
				newAlignment.addAlignCell(e.getKey().getObject1(), e.getKey().getObject2(), StringUtils.validateRelationType(e.getKey().getRelation().getRelation()), e.getKey().getStrength());
			//if all other alignments have this cell
			} else if (e.getValue() == (3)) {
				newAlignment.addAlignCell(e.getKey().getObject1(), e.getKey().getObject2(), StringUtils.validateRelationType(e.getKey().getRelation().getRelation()), e.getKey().getStrength()*2);
			}
		}
		

		//remove duplicates before returning the completed alignment
		((BasicAlignment) newAlignment).normalise();

		//test
		//System.err.println("newAlignment now contains " + newAlignment.nbCells() + " cells");

		return newAlignment;		
	}
	
	/**
	 * NOTE: Currently used by ComposeGUIMainMenu, but should rather use weightedSequentialComposition(ArrayList<Alignment> inputAlignments)
	 * Returns an alignment where correspondences that are identified both by the previous matcher and the current matcher. 
	 * If the correspondence is new, that is, identified only by the current matcher, or if the correspondence is only identified by the previous matcher(s) 
	 * and not the current one, the correspondence is added to the alignment with equally reduced weight. 
	 * The weighting scheme applied in this study is to add (or reduce) 12 percent to the confidence of the correspondence. 
	 * @param alignmentFile1 the first input alignment
	 * @param alignmentFile2 the second input alignment
	 * @param alignmentFile3 the third input alignment
	 * @return an alignment with weighted correspondences
	 * @throws AlignmentException
	 */
	public static Alignment weightedSequentialComposition3(File alignmentFile1, File alignmentFile2, File alignmentFile3) throws AlignmentException {

		//BasicAlignment completeMatchAlignment = new BasicAlignment();
		Alignment completeMatchAlignment = new URIAlignment();
		//BasicAlignment intermediateAlignment = new BasicAlignment();
		Alignment intermediateAlignment = new URIAlignment();

		//load the alignments
		AlignmentParser parser = new AlignmentParser();
		BasicAlignment a1 = (BasicAlignment)parser.parse(alignmentFile1.toURI().toString());
		BasicAlignment a2 = (BasicAlignment)parser.parse(alignmentFile2.toURI().toString());
		BasicAlignment a3 = (BasicAlignment)parser.parse(alignmentFile3.toURI().toString());


		//compare correspondences (cells) in a1 and a2. If a cell in a2 already exists in a1, add strength to it.
		for (Cell cell1 : a1) {
			for (Cell cell2 : a2) {				
				//if the cells are equal (contains similar entities)
				if (cell2.getObject1().equals(cell1.getObject1()) && cell2.getObject2().equals(cell1.getObject2())) {
					//if the strength in the previous alignment is higher, this cell is added + weight
					if (cell1.getStrength() >= cell2.getStrength()) {
						intermediateAlignment.addAlignCell(cell1.getObject1(), cell1.getObject2(), cell1.getRelation().toString(), increaseCellStrength(cell1.getStrength()));
						//if the current cells strength is higher, this cells strength is retained
					} else if (cell2.getStrength() >= cell1.getStrength()) {
						intermediateAlignment.addAlignCell(cell2.getObject1(), cell2.getObject2(), cell2.getRelation().toString(), cell2.getStrength());
					}
					//if the cells are not equal, we add the cells from both the previous alignment and the current alignment, but give them reduced strength.
				} else {
					intermediateAlignment.addAlignCell(cell1.getObject1(), cell1.getObject2(), cell1.getRelation().toString(), reduceCellStrength(cell1.getStrength()));
					intermediateAlignment.addAlignCell(cell2.getObject1(), cell2.getObject2(), cell2.getRelation().toString(), reduceCellStrength(cell2.getStrength()));
					continue;
				}
			}
		}

		//compare correspondences (cells) in the current alignment. If a cell in a3 already exists in the previous alignment, add strength to it. 
		for (Cell cell2 : intermediateAlignment) {
			for (Cell cell3 : a3) {
				//if the cells are equal (contains similar entities)
				if (cell3.getObject1().equals(cell2.getObject1()) && cell3.getObject2().equals(cell2.getObject2())) {
					//if the strength in the previous alignment is higher, this cell is added + weight
					if (cell2.getStrength() >= cell3.getStrength()) {
						completeMatchAlignment.addAlignCell(cell2.getObject1(), cell2.getObject2(), cell2.getRelation().toString(), increaseCellStrength(cell2.getStrength()));
						//if the current cells strength is higher, this is retained
					} else if (cell3.getStrength() >= cell2.getStrength()) {
						completeMatchAlignment.addAlignCell(cell3.getObject1(), cell3.getObject2(), cell3.getRelation().toString(), cell3.getStrength());
					}
					//if the cells are not equal, we add the cells from both the previous alignment and the current alignment, but give them reduced strength.
				} else {
					completeMatchAlignment.addAlignCell(cell2.getObject1(), cell2.getObject2(), cell2.getRelation().toString(), reduceCellStrength(cell2.getStrength()));
					completeMatchAlignment.addAlignCell(cell3.getObject1(), cell3.getObject2(), cell3.getRelation().toString(), reduceCellStrength(cell3.getStrength()));
					continue;
				}
			}
		}
		
		//remove duplicates before returning the completed alignment
		((BasicAlignment) completeMatchAlignment).normalise();

		return completeMatchAlignment;		
	}


	/**
	 * Returns an alignment where correspondences that are identified both by the previous matcher and the current matcher. 
	 * If the correspondence is new, that is, identified only by the current matcher, or if the correspondence is only identified by the previous matcher(s) 
	 * and not the current one, the correspondence is added to the alignment with equally reduced weight. 
	 * The weighting scheme applied in this study is to add (or reduce) 12 percent to the confidence of the correspondence. 
	 * @param alignmentFile1 the first input alignment
	 * @param alignmentFile2 the second input alignment
	 * @param alignmentFile3 the third input alignment
	 * @param alignmentFile4 the fourth input alignment
	 * @return an alignment with weighted correspondences
	 * @throws AlignmentException
	 */
	public static Alignment weightedSequentialComposition4(File alignmentFile1, File alignmentFile2, File alignmentFile3, File alignmentFile4) throws AlignmentException {

		Alignment firstAlignment = new URIAlignment();
		Alignment secondAlignment = new URIAlignment();
		Alignment finalAlignment = new URIAlignment();


		//load the alignments
		AlignmentParser parser = new AlignmentParser();
		BasicAlignment a1 = (BasicAlignment)parser.parse(alignmentFile1.toURI().toString());
		BasicAlignment a2 = (BasicAlignment)parser.parse(alignmentFile2.toURI().toString());
		BasicAlignment a3 = (BasicAlignment)parser.parse(alignmentFile3.toURI().toString());
		BasicAlignment a4 = (BasicAlignment)parser.parse(alignmentFile4.toURI().toString());


		//compare correspondences (cells) in a1 and a2. If a cell in a2 already exists in a1, add strength to it.
		for (Cell cell1 : a1) {
			for (Cell cell2 : a2) {				
				//if the cells are equal (contains similar entities)
				if (cell2.getObject1().equals(cell1.getObject1()) && cell2.getObject2().equals(cell1.getObject2())) {
					//if the strength in the previous alignment is higher, this cell is added + weight
					if (cell1.getStrength() >= cell2.getStrength()) {
						firstAlignment.addAlignCell(cell1.getObject1(), cell1.getObject2(), StringUtils.validateRelationType(cell1.getRelation().getRelation()), increaseCellStrength(cell1.getStrength()));
						//if the current cells strength is higher, this cells strength is retained
					} else if (cell2.getStrength() >= cell1.getStrength()) {
						firstAlignment.addAlignCell(cell2.getObject1(), cell2.getObject2(), StringUtils.validateRelationType(cell2.getRelation().getRelation()), cell2.getStrength());
					}
					//if the cells are not equal, we add the cells from both the previous alignment and the current alignment, but give them reduced strength.
				} else {
					firstAlignment.addAlignCell(cell1.getObject1(), cell1.getObject2(), StringUtils.validateRelationType(cell1.getRelation().getRelation()), reduceCellStrength(cell1.getStrength()));
					firstAlignment.addAlignCell(cell2.getObject1(), cell2.getObject2(), StringUtils.validateRelationType(cell2.getRelation().getRelation()), reduceCellStrength(cell2.getStrength()));
					continue;
				}
			}
		}


		//compare correspondences (cells) in the current alignment. If a cell in a3 already exists in the previous alignment, add strength to it. 
		for (Cell cell2 : firstAlignment) {
			for (Cell cell3 : a3) {
				//if the cells are equal (contains similar entities)
				if (cell3.getObject1().equals(cell2.getObject1()) && cell3.getObject2().equals(cell2.getObject2())) {
					//if the strength in the previous alignment is higher, this cell is added + weight
					if (cell2.getStrength() >= cell3.getStrength()) {
						secondAlignment.addAlignCell(cell2.getObject1(), cell2.getObject2(), StringUtils.validateRelationType(cell2.getRelation().getRelation()), increaseCellStrength(cell2.getStrength()));
						//if the current cells strength is higher, this is retained
					} else if (cell3.getStrength() >= cell2.getStrength()) {
						secondAlignment.addAlignCell(cell3.getObject1(), cell3.getObject2(), StringUtils.validateRelationType(cell3.getRelation().getRelation()), cell3.getStrength());
					}
					//if the cells are not equal, we add the cells from both the previous alignment and the current alignment, but give them reduced strength.
				} else {
					secondAlignment.addAlignCell(cell2.getObject1(), cell2.getObject2(), StringUtils.validateRelationType(cell2.getRelation().getRelation()), reduceCellStrength(cell2.getStrength()));
					secondAlignment.addAlignCell(cell3.getObject1(), cell3.getObject2(), StringUtils.validateRelationType(cell3.getRelation().getRelation()), reduceCellStrength(cell3.getStrength()));
					continue;
				}
			}
		}


		//compare cells with the current alignment and alignment 4
		for (Cell cell4 : secondAlignment) {
			for (Cell cell5 : a4) {
				if (cell5.getObject1().equals(cell4.getObject1()) && cell5.getObject2().equals(cell4.getObject2())) {
					//if the strength in the previous alignment is higher, this cell is added + weight
					if (cell4.getStrength() >= cell5.getStrength()) {
						finalAlignment.addAlignCell(cell5.getObject1(), cell5.getObject2(), StringUtils.validateRelationType(cell5.getRelation().getRelation()), cell5.getStrength());
					} else if (cell5.getStrength() >= cell4.getStrength()) {
						finalAlignment.addAlignCell(cell5.getObject1(), cell5.getObject2(), StringUtils.validateRelationType(cell5.getRelation().getRelation()), cell5.getStrength());
					}
					//if the cells are not equal, we add the cells from both the previous alignment and the current alignment, but give them reduced strength
				} else {
					finalAlignment.addAlignCell(cell4.getObject1(), cell4.getObject2(), StringUtils.validateRelationType(cell4.getRelation().getRelation()), reduceCellStrength(cell4.getStrength()));
					finalAlignment.addAlignCell(cell5.getObject1(), cell5.getObject2(), StringUtils.validateRelationType(cell5.getRelation().getRelation()), reduceCellStrength(cell5.getStrength()));
					continue;
				}
			}
		}


		//remove duplicates before returning the completed alignment
		((BasicAlignment) finalAlignment).normalise();

		return finalAlignment;		
	}

	/**
	 * Returns an alignment produced by processing the input alignments in sequence, comparing if alignments contain equal correspondences and keeping the highest strength (confidence)
	 * If there are new correspondences discovered while processing each alignment they are all maintained in the final alignment. 
	 * @param alignmentFile1
	 * @param alignmentFile2
	 * @param alignmentFile3
	 * @return
	 * @throws AlignmentException
	 */
	public static Alignment nonWeightedCompleteMatch(File alignmentFile1, File alignmentFile2, File alignmentFile3) throws AlignmentException {

		Alignment completeMatchAlignment = new URIAlignment();
		Alignment intermediateAlignment = new URIAlignment();

		//load the alignments
		AlignmentParser parser = new AlignmentParser();
		BasicAlignment a1 = (BasicAlignment)parser.parse(alignmentFile1.toURI().toString());
		BasicAlignment a2 = (BasicAlignment)parser.parse(alignmentFile2.toURI().toString());
		BasicAlignment a3 = (BasicAlignment)parser.parse(alignmentFile3.toURI().toString());

		//compare correspondences (cells) in a1 and a2. 
		for (Cell cell1 : a1) {
			for (Cell cell2 : a2) {				
				//if the cells are equal (contains similar entities)
				if (cell2.getObject1().equals(cell1.getObject1()) && cell2.getObject2().equals(cell1.getObject2())) {
					//if the strength in the previous alignment is higher, this cell is added 
					if (cell1.getStrength() >= cell2.getStrength()) {
						intermediateAlignment.addAlignCell(cell1.getObject1(), cell1.getObject2(), StringUtils.validateRelationType(cell1.getRelation().getRelation()), cell1.getStrength());
						//if the current cells strength is higher, this cells strength is retained
					} else if (cell2.getStrength() >= cell1.getStrength()) {
						intermediateAlignment.addAlignCell(cell2.getObject1(), cell2.getObject2(), StringUtils.validateRelationType(cell2.getRelation().getRelation()), cell2.getStrength());
					}
					//if the cells are not equal, we add the cells from both the previous alignment and the current alignment.
				} else {
					intermediateAlignment.addAlignCell(cell1.getObject1(), cell1.getObject2(), StringUtils.validateRelationType(cell1.getRelation().getRelation()), cell1.getStrength());
					intermediateAlignment.addAlignCell(cell2.getObject1(), cell2.getObject2(), StringUtils.validateRelationType(cell2.getRelation().getRelation()), cell2.getStrength());
					continue;
				}
			}
		}

		//compare correspondences (cells) in the current alignment.  
		for (Cell cell2 : intermediateAlignment) {
			for (Cell cell3 : a3) {
				//if the cells are equal (contains similar entities)
				if (cell3.getObject1().equals(cell2.getObject1()) && cell3.getObject2().equals(cell2.getObject2())) {
					//if the strength in the previous alignment is higher, this cell is added
					if (cell2.getStrength() >= cell3.getStrength()) {
						completeMatchAlignment.addAlignCell(cell2.getObject1(), cell2.getObject2(), StringUtils.validateRelationType(cell2.getRelation().getRelation()), cell2.getStrength());
						//if the current cells strength is higher, this is retained
					} else if (cell3.getStrength() >= cell2.getStrength()) {
						completeMatchAlignment.addAlignCell(cell3.getObject1(), cell3.getObject2(), StringUtils.validateRelationType(cell3.getRelation().getRelation()), cell3.getStrength());
					}
					//if the cells are not equal, we add the cells from both the previous alignment and the current alignment.
				} else {
					completeMatchAlignment.addAlignCell(cell2.getObject1(), cell2.getObject2(), StringUtils.validateRelationType(cell2.getRelation().getRelation()), cell2.getStrength());
					completeMatchAlignment.addAlignCell(cell3.getObject1(), cell3.getObject2(), StringUtils.validateRelationType(cell3.getRelation().getRelation()), cell3.getStrength());
					continue;
				}
			}
		}

		//TO-DO: should remove duplicates before returning the completed alignment
		return completeMatchAlignment;		
	}


	/**
	 * Increases an input value by 12 percent
	 * @param inputStrength the strength/confidence for a correspondence to be increased
	 * @return a value 12 percent higher than its input value
	 */
	public static double increaseCellStrength(double inputStrength) {

		double newStrength = inputStrength + (inputStrength * 0.12);

		if (newStrength > 1.0) {
			newStrength = 1.0;
		}

		return newStrength;
	}

	/**
	 * Decreases an input value by 12 percent
	 * @param inputStrength the strength/confidence for a correspondence to be decreased
	 * @return a value 12 percent lower than its input value
	 */
	public static double reduceCellStrength(double inputStrength) {

		double newStrength = inputStrength - (inputStrength * 0.12);

		if (newStrength > 1.0) {
			newStrength = 1.0;
		}

		return newStrength;
	}

	public static void main(String[] args) throws AlignmentException, IOException, URISyntaxException {

		String task = "103/101-103-";


		File af1 = new File("./files/OAEI2009/alignments/"+task+"OppositeSubclass0.9.rdf");
		File af2 = new File("./files/OAEI2009/alignments/"+task+"Compound0.9.rdf");
		File af3 = new File("./files/OAEI2009/alignments/"+task+"WNHyponym0.9.rdf");
		File af4 = new File("./files/OAEI2009/alignments/"+task+"Parent0.9.rdf");

		ArrayList<Alignment> inputAlignments = new ArrayList<Alignment>();

		AlignmentParser parser = new AlignmentParser();
		BasicAlignment a1 = (BasicAlignment) parser.parse(af1.toURI().toString());
		BasicAlignment a2 = (BasicAlignment) parser.parse(af2.toURI().toString());
		BasicAlignment a3 = (BasicAlignment) parser.parse(af3.toURI().toString());
		BasicAlignment a4 = (BasicAlignment) parser.parse(af4.toURI().toString());

		inputAlignments.add(a2);
		inputAlignments.add(a3);
		inputAlignments.add(a4);
		inputAlignments.add(a1);

		Alignment newAlignment = weightedSequentialComposition(inputAlignments);

		System.out.println("\n");
		for (Cell c : newAlignment) {
			System.out.println(c.getObject1AsURI().getFragment() + " - " + c.getObject2AsURI().getFragment() + " : " + c.getRelation().getRelation() + " : " + c.getStrength());
		}

	}
}

package no.ntnu.idi.compose.alignmentCombiner;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Cell;

import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.parser.AlignmentParser;

/**
 * @author audunvennesland 31. mar. 2017
 */
public class SimpleVoteAlgo {

	public static Alignment createVotedAlignment(Set<Alignment> inputAlignments) throws AlignmentException {

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

		Set<Cell> todel = new HashSet<Cell>();
		Set<Cell> toKeep = new HashSet<Cell>();
		BasicAlignment votedAlignment = new URIAlignment();

		for (Cell currentCell : allCellsAlignment) {

			if (!todel.contains(currentCell)) {
				//get all cells that has the same object1 as c1
				Set<Cell> cells2 = allCellsAlignment.getAlignCells1(currentCell.getObject1());

				if (cells2.size() > 1) {
					//placeholder for cells that contains the same object1 and object 2 as c1
					Set<Cell> toCheck = new HashSet<Cell>();

					Object o2 = currentCell.getObject2();

					for (Cell c2 : cells2) {
						//if this cell (c2) contains the same object1 and object2 as the current cell we move them to "toCheck"
						if (o2.equals(c2.getObject2())) {
							toCheck.add(c2);
						//if not, this cell is "unique" and we keep it	
						} else {
							toKeep.add(currentCell);

						}
					}

					if (toCheck.size() > 1) {

						double confidence = currentCell.getStrength();
						//these are the cells that contain the same object1 and object2 as the current cell
						for (Cell c : toCheck) {
							//check if the relations match, if so we sum their confidence
							if (c != currentCell && c.getRelation().equals(currentCell.getRelation())) {
								confidence = (confidence + c.getStrength());
								currentCell.setStrength(confidence);
								//once the confidence is summed, we can forget about the other duplicate cells, and only keep the current cell (with summed confidence)
								todel.add(c);
								toKeep.add(currentCell);

							}
						}
					}

				} else {
					//if the current cell is unique, that is, the only cell with this combination of object1 and object2, we keep it
					toKeep.add(currentCell);
				}
			}

		}
		//move all cells "to keep" over to the alignment
		for (Cell c : toKeep) {
			votedAlignment.addAlignCell(c.getObject1(), c.getObject2(), c.getRelation().toString(), c.getStrength());
		}

		//remove duplicates
		votedAlignment.normalise();

		return votedAlignment;

	}
	
	public static Alignment rankAlignment(BasicAlignment a) throws AlignmentException {
		BasicAlignment rankedAlignment = new URIAlignment();
		Set<Cell> cellSet = new HashSet<Cell>();		
		for (Cell c : a) {
			cellSet.add(c);
		}

		List<Cell> cellList = new LinkedList<Cell>();
		cellList.addAll(cellSet);
		Collections.<Cell>sort(cellList);

		for (Cell c1 : cellList) {
			rankedAlignment.addAlignCell(c1.getObject1(), c1.getObject2(), c1.getRelation().toString(), c1.getStrength());
		}	
		
		return rankedAlignment;
	}
	
	public static BasicAlignment createFinalVotedAlignment(BasicAlignment a) throws AlignmentException {
		
		BasicAlignment finalAlignment = new URIAlignment();
		Set<Cell> todel = new HashSet<Cell>();
		Set<Cell> toKeep = new HashSet<Cell>();
		
		for (Cell currentCell : a) {

			if (!todel.contains(currentCell)) {
				
				//get all cells that has the same object1 as c1
				Set<Cell> cells2 = a.getAlignCells1(currentCell.getObject1());

				if (cells2.size() > 1) {

				for (Cell c2 : cells2) {
					//if the confidence of c2 is higher than currentCell, we add c2 to the set
					if (c2.getStrength() > currentCell.getStrength()) {
						toKeep.add(c2);
					}
				}

				} else {
					//if the current cell is unique, that is, the only cell with this combination of object1 and object2, we keep it
					toKeep.add(currentCell);
				}
			}
		}
		
		for (Cell c : toKeep) {
			finalAlignment.addAlignCell(c.getObject1(), c.getObject2(), c.getRelation().toString(), c.getStrength());
		}
		

		return finalAlignment;
		
		
	}
	
	/*public 
	
	for (Cell currentCell : allCellsAlignment) {

		if (!todel.contains(currentCell)) {
			//get all cells that has the same object1 as c1
			Set<Cell> cells2 = allCellsAlignment.getAlignCells1(currentCell.getObject1());

			if (cells2.size() > 1) {
				//placeholder for cells that contains the same object1 and object 2 as c1
				Set<Cell> toCheck = new HashSet<Cell>();

				Object o2 = currentCell.getObject2();

				for (Cell c2 : cells2) {
					//if this cell (c2) contains the same object1 and object2 as the current cell we move them to "toCheck"
					if (o2.equals(c2.getObject2())) {
						toCheck.add(c2);
					//if not, this cell is "unique" and we keep it	
					} else {
						toKeep.add(currentCell);

					}
				}

				if (toCheck.size() > 1) {

					double confidence = currentCell.getStrength();
					//these are the cells that contain the same object1 and object2 as the current cell
					for (Cell c : toCheck) {
						//check if the relations match, if so we sum their confidence
						if (c != currentCell && c.getRelation().equals(currentCell.getRelation())) {
							confidence = (confidence + c.getStrength());
							currentCell.setStrength(confidence);
							//once the confidence is summed, we can forget about the other duplicate cells, and only keep the current cell (with summed confidence)
							todel.add(c);
							toKeep.add(currentCell);

						}
					}
				}

			} else {
				//if the current cell is unique, that is, the only cell with this combination of object1 and object2, we keep it
				toKeep.add(currentCell);
			}
		}

	}
}*/


	public static void main(String[] args) throws AlignmentException, IOException {

		File af1 = new File("./files/alignmentCombiner/conference-ekaw/conference-ekaw-alignment-aml.rdf");
		File af2 = new File("./files/alignmentCombiner/conference-ekaw/conference-ekaw-alignment-logmap.rdf");
		File af3 = new File("./files/alignmentCombiner/conference-ekaw/conference-ekaw-alignment-compose.rdf");

		AlignmentParser parser = new AlignmentParser();
		BasicAlignment a1 = (BasicAlignment) parser.parse(af1.toURI().toString());
		BasicAlignment a2 = (BasicAlignment) parser.parse(af2.toURI().toString());
		BasicAlignment a3 = (BasicAlignment) parser.parse(af3.toURI().toString());

		Set<Alignment> inputAlignments = new HashSet<Alignment>();
		inputAlignments.add(a1);
		inputAlignments.add(a2);
		inputAlignments.add(a3);

		System.out.println("1. Creating the first basic simple vote alignment");
		BasicAlignment votedAlignment = (BasicAlignment) createVotedAlignment(inputAlignments);
		
		System.out.println("2. Ranking correspondences");
		BasicAlignment rankedAlignment = (BasicAlignment) rankAlignment(votedAlignment);
		
		System.out.println("3. Creating the final simple vote alignment");
		BasicAlignment finalAlignment = createFinalVotedAlignment(rankedAlignment);
		
		// store the new alignment
		File outputAlignment = new File("./files/alignmentCombiner/simplevote/simplevote.rdf");

		PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(outputAlignment)), true);
		AlignmentVisitor renderer = new RDFRendererVisitor(writer);

		System.out.println("4. Printing the final simple vote alignment to file");
		finalAlignment.render(renderer);
		writer.flush();
		writer.close();

	}

}

package no.ntnu.idi.compose.alignmentCombiner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Cell;

import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.BasicRelation;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.parser.AlignmentParser;

/**
 * @author audunvennesland 28. mar. 2017
 */
public class Intersection {

	// TO-DO: Implement a more dynamic approach that has no restriction on three
	// alignments
	public static Alignment intersectStrictly(File alignmentFile1, File alignmentFile2, File alignmentFile3)
			throws AlignmentException {

		// the aggregated alignment being returned from the method
		Alignment finalAlignment = new URIAlignment();

		// load the alignments
		AlignmentParser parser = new AlignmentParser();
		BasicAlignment a1 = (BasicAlignment) parser.parse(alignmentFile1.toURI().toString());
		BasicAlignment a2 = (BasicAlignment) parser.parse(alignmentFile2.toURI().toString());
		BasicAlignment a3 = (BasicAlignment) parser.parse(alignmentFile3.toURI().toString());

		for (Cell c1 : a1) {
			for (Cell c2 : a2) {
				for (Cell c3 : a3) {

					// check if the cell provided by all three alignments is
					// equal, then we can just add either of the cells to the
					// final alignment
					// the confidence added is the average confidence of all
					// three cells
					// checking C1 and C2
					if ((c1.getObject1AsURI().getFragment().equals(c2.getObject1AsURI().getFragment())
							&& c1.getObject2AsURI().getFragment().equals(c2.getObject2AsURI().getFragment())
							&& c1.getRelation().equals(c2.getRelation()))
							// checking C1 and C3
							&& (c1.getObject1AsURI().getFragment().equals(c3.getObject1AsURI().getFragment())
									&& c1.getObject2AsURI().getFragment().equals(c3.getObject2AsURI().getFragment())
									&& c1.getRelation().equals(c3.getRelation()))
							// checking C2 and C3
							&& (c2.getObject1AsURI().getFragment().equals(c2.getObject1AsURI().getFragment())
									&& c2.getObject2AsURI().getFragment().equals(c3.getObject2AsURI().getFragment())
									&& c2.getRelation().equals(c3.getRelation())))

					{

						finalAlignment.addAlignCell(c1.getObject1(), c1.getObject2(),
								((BasicRelation) (c1.getRelation())).getPrettyLabel(),
								((c1.getStrength() + c2.getStrength() + c3.getStrength())) / 3);

					}
				}
			}
		}

		System.out.println("Strict intersection alignment created!");

		return finalAlignment;

	}

	public static BasicAlignment intersectRelaxed(Set<Alignment> inputAlignments) throws AlignmentException {

		BasicAlignment relaxedIntersectAlignment = new URIAlignment();

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
			relaxedIntersectAlignment.addAlignCell(c.getObject1(), c.getObject2(), c.getRelation().toString(),
					c.getStrength());
		}

		// remove duplicates - no need
		// relaxedIntersectAlignment.normalise();

		System.out.println("relaxedIntersectAlignment contains " + relaxedIntersectAlignment.nbCells() + " cells");

		for (Cell c : relaxedIntersectAlignment) {
			System.out.println(c.getObject1() + " - " + c.getObject2() + " - " + c.getRelation().toString() + " - "
					+ c.getStrength());
		}

		return relaxedIntersectAlignment;
	}

	public static void main(String[] args) throws AlignmentException, IOException {

		File a3 = new File("./files/alignmentCombiner/conference-ekaw/conference-ekaw-alignment-compose.rdf");
		File a1 = new File("./files/alignmentCombiner/conference-ekaw/conference-ekaw-alignment-aml.rdf");
		File a2 = new File("./files/alignmentCombiner/conference-ekaw/conference-ekaw-alignment-logmap.rdf");

		BasicAlignment strictIntersection = (BasicAlignment) intersectStrictly(a1, a2, a3);

		// store the new alignment
		File strictIntersectionFile = new File("./files/alignmentCombiner/intersection/intersection-strict.rdf");

		PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(strictIntersectionFile)), true);
		AlignmentVisitor renderer = new RDFRendererVisitor(writer);

		strictIntersection.render(renderer);
		writer.flush();
		writer.close();

		AlignmentParser parser = new AlignmentParser();
		BasicAlignment al1 = (BasicAlignment) parser.parse(a1.toURI().toString());
		BasicAlignment al2 = (BasicAlignment) parser.parse(a2.toURI().toString());
		BasicAlignment al3 = (BasicAlignment) parser.parse(a3.toURI().toString());

		Set<Alignment> inputAlignments = new HashSet<Alignment>();
		inputAlignments.add(al1);
		inputAlignments.add(al2);
		inputAlignments.add(al3);

		BasicAlignment relaxedIntersection = intersectRelaxed(inputAlignments);
		
		// store the new alignment
		File relaxedIntersectionFile = new File("./files/alignmentCombiner/intersection/intersection-relaxed.rdf");

		writer = new PrintWriter(new BufferedWriter(new FileWriter(relaxedIntersectionFile)), true);
		renderer = new RDFRendererVisitor(writer);

		strictIntersection.render(renderer);
		writer.flush();
		writer.close();

	}
}

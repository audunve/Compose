package no.ntnu.idi.compose.alignmentCombiner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Cell;

import de.unima.alcomox.ExtractionProblem;
import de.unima.alcomox.Settings;
import de.unima.alcomox.exceptions.AlcomoException;
import de.unima.alcomox.mapping.Mapping;
import de.unima.alcomox.ontology.IOntology;
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.BasicRelation;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.parser.AlignmentParser;

/**
 * @author audunvennesland 26. mar. 2017
 */
public class SubgraphStrategy {

	/**
	 * 
	 * @param onto1 source ontology (used for checking consistency of the alignment)
	 * @param onto2 target ontology (used for checking consistency of the alignment)
	 * @param inputAlignments a set of Alignment objects formatted according to the Alignment format
	 * @return returns merged alignments from the recursive method buildAlignments()
	 * @throws AlignmentException
	 * @throws AlcomoException
	 * @throws IOException
	 */
	public static Set<Set<Set<Cell>>> initStrategy(String onto1, String onto2, Set<Alignment> inputAlignments)
			throws AlignmentException, AlcomoException, IOException {

		// uses the createVotedAlignment method from SimpleVote that sums the
		// confidence scores of identical correspondences
		// and selects the ones with the highest score
		Alignment inputSet = SimpleVoteAlgo.createVotedAlignment(inputAlignments);

		// transforms into set of cells (easier to work with than Alignment
		// objects)
		Set<Cell> input = new HashSet<Cell>();

		for (Cell c : inputSet) {
			input.add(c);
		}

		Iterator<Cell> aItr = input.iterator();

		// set to hold merged alignments
		Set<Set<Cell>> r_set = new HashSet<Set<Cell>>();

		// finds the "best" cell, that is the cell with the highest confidence
		Cell bestCell = findBestCell(input);

		Set<Cell> r = new HashSet<Cell>();

		r.add(bestCell);
		r_set.add(r);

		// set to hold the candidate correspondences
		Set<Cell> a_marked = new HashSet<Cell>();

		// while there still are candidate correspondences in a
		while (aItr.hasNext()) {

			Cell nextCell = aItr.next();

			if (!nextCell.equals(bestCell)) {
				a_marked.add(nextCell);
			}

		}

		System.out.println("InitStrategy: rSet contains " + r_set.size() + " alignments");
		System.out.println("InitStrategy: a_marked contains " + a_marked.size() + " cells");

		System.out.println("InitStrategy...Building alignments....");

		// need to integrate the results from buildAlignments
		Set<Set<Set<Cell>>> mergedAlignments = new HashSet<Set<Set<Cell>>>();
		mergedAlignments.add(buildAlignments(onto1, onto2, a_marked, r_set));
		System.out.println("InitStrategy(): There are " + mergedAlignments.size() + " merged alignments");

		return mergedAlignments;

	}

	public static Set<Set<Cell>> buildAlignments(String onto1, String onto2, Set<Cell> a_input,
			Set<Set<Cell>> mergedAlignments) throws AlignmentException, AlcomoException, IOException {

		// if there are no cells in a_input return null
		if (a_input.isEmpty()) {
			return null;
		}

		/*// rank the cells according to confidence strength - not sure if useful
		TreeSet<Cell> aInput = rankAlignment(a_input);*/

// 		test
//		System.err.println("a_Input contains " + a_input.size() + " cell(s)");
//
//		for (Cell c : a_input) {
//			System.err.println("- " + c.getObject1AsURI().getFragment() + " - " + c.getObject2AsURI().getFragment());
//		}

		Set<Cell> a_marked = new HashSet<Cell>();
		Set<Cell> r_marked = new HashSet<Cell>();
		Set<Set<Cell>> maTmp = new HashSet<Set<Cell>>();

		for (Cell a : a_input) {

			Cell bestCell = findBestCell(a_input);
//			System.err.println("The bestCell from input is " + bestCell.getObject1AsURI().getFragment() + " - "
//					+ bestCell.getObject2AsURI().getFragment());

			Cell thisCell = a;
//			System.err.println("Checking thisCell " + thisCell.getObject1AsURI().getFragment() + " - "
//					+ thisCell.getObject2AsURI().getFragment());

			if (!thisCell.equals(bestCell)) {
				a_marked.add(thisCell);
//				System.err.println("adding " + thisCell + " to a_marked");

			}

//			System.err.println("2 a_marked contains " + a_marked.size() + " cells.");

			Iterator<Set<Cell>> maItr = mergedAlignments.iterator();

			while (maItr.hasNext()) {
				Set<Cell> r = maItr.next();

				for (Cell c_r : r) {
					r_marked.add(c_r);
				}

				if (!r_marked.contains(bestCell)) {
					r_marked.add(bestCell);
//					System.out.println("Adding " +bestCell.getObject1AsURI().getFragment() + " " + bestCell.getRelation().toString() + " " + 
//					bestCell.getObject2AsURI().getFragment() + " " + bestCell.getStrength() + " to r_marked");

					maTmp.addAll(mergedAlignments);

					if (isConsistent(onto1, onto2, r_marked)) {
						
						System.err.println("r_marked checked for consistency:");
						
						for (Cell c : r_marked) {
							System.err.println(c.getObject1AsURI().getFragment() + " " + ((BasicRelation)(c.getRelation())).getPrettyLabel() + " " + c.getObject2AsURI().getFragment() + " " + c.getStrength());
							
						}

						System.err.println("They are consistent!");

						System.err.println("a_marked contains " + a_marked.size() + " cells.");

						Iterator<Cell> a_markedItr = a_marked.iterator();
						while (a_markedItr.hasNext()) {
							Cell a_markedCell = a_markedItr.next();
//							System.err.println("Contents of a_markedCell: ");
//							System.err.println(a_markedCell.getObject1AsURI().getFragment() + " - "
//									+ a_markedCell.getObject2AsURI().getFragment());

							// check if any of the objects (concepts) in the
							// remaining alignment are equal to any of the
							// objects in the bestCell (being added to r_marked)
							// if so, the cell in the remaining alignment should
							// be removed to ensure a "stable marriage".
							if (!bestCell.getObject1().equals(a_markedCell.getObject1())
									|| !bestCell.getObject2().equals(a_markedCell.getObject2())) {
								a_marked.add(a_markedCell);
							} else {
								System.err.println(bestCell.getObject1AsURI().getFragment() + " - "
										+ bestCell.getObject2AsURI().getFragment() + " and "
										+ a_markedCell.getObject1AsURI().getFragment() + " - "
										+ a_markedCell.getObject2AsURI().getFragment() + " have equal objects");
							}
						}

						// This is throwing a concurrentModificationException if we don´t move the contents from 'mergedAlignments' to 'maTmp'
						maTmp.add(r_marked);

						/*System.err.println("maTmp now contains " + maTmp.size() + " merged alignments");
						for (Set<Cell> s : maTmp) {
							System.out.println("---" + s.size() + "---");
							for (Cell c : s) {
								System.out.println(c.getObject1AsURI().getFragment() + " " + ((BasicRelation)(c.getRelation())).getPrettyLabel() + " " + c.getObject2AsURI().getFragment() + " " + c.getStrength());
							}
						}*/

					} else {
						System.out.println("Not consistent!");
					}

				} // end if

			} // end while

		} // end for
		System.err.println("a_marked now contains: " + a_marked.size() + " cells");

		System.err.println("Recursive call next...");
		buildAlignments(onto1, onto2, a_marked, maTmp);
		return maTmp;

	}

	/**
	 * Finds the cell with the highest confidence among other cells in the set
	 * 
	 * @param a set of cells in which the cell with highest confidence is to be identified
	 * @return the cell (correspondence) with the highest confidence
	 * @throws AlignmentException
	 */
	public static Cell findBestCell(Set<Cell> a) throws AlignmentException {

		Cell bestCell = null;
		double thisScore = 0;
		double score = 0;

		for (Cell c : a) {
			thisScore = c.getStrength();
			if (thisScore > score) {
				score = thisScore;
				bestCell = c;
			}
		}
		return bestCell;
	}

	/**
	 * Ranks an alignment (set of cells) according to confidence scores
	 * 
	 * @param input set of cells to be ranked
	 * @return ranked set of cells
	 * @throws AlignmentException
	 */
	public static TreeSet<Cell> rankAlignment(Set<Cell> input) throws AlignmentException {

		BasicAlignment ranked = new URIAlignment();

		for (Cell c : input) {
			ranked.addAlignCell(c.getObject1(), c.getObject2(), c.getRelation().toString(), c.getStrength());
		}

		TreeSet<Cell> rankedAlignment = ranked.getSortedIterator();

		return rankedAlignment;
	}

	/**
	 * Returns a set of cell where a single cell provided as parameter is
	 * removed
	 * 
	 * @param removedCell cell to be removed
	 * @param a set of cells for which a single cell will be removed
	 * @return remaining set of cells (- removed cell)
	 * @throws AlignmentException
	 */
	public static Set<Cell> getRest(Cell removedCell, Set<Cell> a) throws AlignmentException {

		Set<Cell> restSet = new HashSet<Cell>();

		for (Cell c : a) {
			restSet.add(c);
		}

		restSet.remove(removedCell);

		return restSet;

	}

	/**
	 * Uses the consistency facility from Alcomo
	 * 
	 * @param onto1 source ontology
	 * @param onto2 target ontology
	 * @param a set of cells from the alignment to be checked for consistency
	 * @return boolean isConsistent stating if the checked alignment is consistent with the input ontologies
	 * @throws AlcomoException
	 * @throws AlignmentException
	 * @throws IOException
	 */
	public static boolean isConsistent(String onto1, String onto2, Set<Cell> a)
			throws AlcomoException, AlignmentException, IOException {

		boolean isConsistent = true;

		Settings.BLACKBOX_REASONER = Settings.BlackBoxReasoner.PELLET;
		Settings.ONE_TO_ONE = false;

		IOntology sourceOnt = new IOntology(onto1);
		IOntology targetOnt = new IOntology(onto2);

		// Experienced some problems with "illegal" semantic correspondences due
		// to alignment format objects representing the relations, so
		// implemented some tests and transformations to overcome them. This
		// must be checked...

		BasicAlignment alignment = new URIAlignment();
		for (Cell c : a) {

			if (c.getRelation().toString().equals("fr.inrialpes.exmo.align.impl.BasicRelation@5a703e8b")) {
				alignment.addAlignCell(c.getObject1(), c.getObject2(), ">", c.getStrength());
			} else {
				alignment.addAlignCell(c.getObject1(), c.getObject2(), "=", c.getStrength());
			}
		}

		// store the new alignment
		File outputAlignment = new File("./files/ER2017/consistency/alignment.rdf");

		PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(outputAlignment)), true);
		AlignmentVisitor renderer = new RDFRendererVisitor(writer);

		alignment.render(renderer);
		writer.flush();
		writer.close();

		String alignmentPath = "file:/Users/audunvennesland/git/Compose/compose/files/ER2017/consistency/alignment.rdf";

		Mapping mapping = new Mapping(alignmentPath);
		mapping.applyThreshhold(0.6);

		ExtractionProblem ep = new ExtractionProblem(ExtractionProblem.ENTITIES_CONCEPTSPROPERTIES,
				ExtractionProblem.METHOD_OPTIMAL, ExtractionProblem.REASONING_EFFICIENT);

		ep.bindSourceOntology(sourceOnt);
		ep.bindTargetOntology(targetOnt);
		ep.bindMapping(mapping);

		ep.solve();

		if (ep.isCoherentExtraction()) {
			isConsistent = true;
		} else {
			isConsistent = false;
		}

		return isConsistent;

	}

	/**
	 * Test method
	 * @param args
	 * @throws AlignmentException
	 * @throws AlcomoException
	 * @throws IOException
	 */
	public static void main(String[] args) throws AlignmentException, AlcomoException, IOException {

		// import the ontologies
		String onto1 = "files/alignmentCombiner/conference-ekaw/Conference.owl";
		String onto2 = "files/alignmentCombiner/conference-ekaw/ekaw.owl";

		// import the alignment files
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

		/*
		 * File testFile = new
		 * File("./files/alignmentCombiner/conference-ekaw/SubGraphTest.rdf");
		 * AlignmentParser parser = new AlignmentParser(); BasicAlignment
		 * testAlignment = (BasicAlignment)
		 * parser.parse(testFile.toURI().toString());
		 * 
		 * Set<Alignment> testAlignments = new HashSet<Alignment>();
		 * testAlignments.add(testAlignment);
		 * 
		 * Set<Cell> testSet = new HashSet<Cell>();
		 * 
		 * for (Cell c : testAlignment) { testSet.add(c); }
		 */

		Set<Set<Set<Cell>>> ma = initStrategy(onto1, onto2, inputAlignments);

		System.out.println("Number of merged alignments: ");
		if (!ma.isEmpty()) {
			System.out.println(ma.size());
		} else {
			System.out.println("MergedAlignments is empty");
		}

		System.out.println("Printing the merged alignments: ");
		for (Set<Set<Cell>> s : ma) {
			for (Set<Cell> c : s) {
				for (Cell cell : c) {
					System.out.println(
							cell.getObject1AsURI().getFragment() + " - " + cell.getObject2AsURI().getFragment());
				}
			}
			System.out.println("\n");
		}

	}

}

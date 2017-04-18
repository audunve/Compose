package no.ntnu.idi.compose.alignmentCombiner;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.parser.AlignmentParser;

import de.unima.alcomox.ExtractionProblem;
import de.unima.alcomox.Settings;
import de.unima.alcomox.exceptions.AlcomoException;
import de.unima.alcomox.mapping.Characteristic;
import de.unima.alcomox.mapping.Mapping;
import de.unima.alcomox.ontology.IOntology;

/**
 * @author audunvennesland 26. mar. 2017
 */
public class SubgraphStrategy {

	public static Set<Set<Cell>> initStrategy(String onto1, String onto2, Set<Alignment> inputAlignments)
			throws AlignmentException, AlcomoException {

		Alignment inputSet = SimpleVoteAlgo.createVotedAlignment(inputAlignments);
		
		//prefer working with sets, not Alignment objects from Alignment API
		Set<Cell> input = new HashSet<Cell>();

		for (Cell c : inputSet) {
			input.add(c);
		}

		Iterator<Cell> aItr = input.iterator();

		Set<Set<Cell>> mergedAlignments = new HashSet<Set<Cell>>();
		
		Cell bestCell = findBestCell(input);
		Set<Cell> r = new HashSet<Cell>();
		Set<Cell> a_marked = new HashSet<Cell>();
		
		while (aItr.hasNext()) {

			if (!r.contains(bestCell)) {
			r.add(bestCell);
			}

			Cell nextCell = aItr.next();

			if (!nextCell.equals(bestCell)) {
				a_marked.add(nextCell);

			}
			mergedAlignments.add(r);
				
		}		

		return buildAlignments(onto1, onto2, a_marked, mergedAlignments);

	}
	
	public static Set<Set<Cell>> buildAlignments(String onto1, String onto2, Set<Cell> a_input,
			Set<Set<Cell>> mergedAlignments) throws AlignmentException, AlcomoException {
		

		//if there are no cells in a_input return null
		if (a_input.isEmpty()) {
			return null;
		}
		
		//rank the cells according to confidence strength - not sure if useful
		//TreeSet<Cell> aInput = rankAlignment(a_input);
		
			System.out.println("a_Input contains " + a_input.size() + " cell(s)");
			
			for (Cell c: a_input) {
				System.out.println("- " + c.getObject1AsURI().getFragment() + " - " + c.getObject2AsURI().getFragment());
			}
				
		Set<Cell> a_marked = new HashSet<Cell>();
		Set<Cell> r_marked = new HashSet<Cell>();
		Set<Set<Cell>> maTmp = new HashSet<Set<Cell>>();

		for (Cell a : a_input) {
			
			Cell bestCell = findBestCell(a_input);
			System.out.println("The bestCell from input is " + bestCell.getObject1AsURI().getFragment() + " - " + bestCell.getObject2AsURI().getFragment());
			
			Cell thisCell = a;
			System.out.println("Checking thisCell " + thisCell.getObject1AsURI().getFragment() + " - " + thisCell.getObject2AsURI().getFragment());

			if (!thisCell.equals(bestCell)) {
				a_marked.add(thisCell);
				System.err.println("adding " + thisCell + " to a_marked");

			}//end if
			
			System.err.println("2 a_marked contains " + a_marked.size() + " cells.");
			
			Iterator<Set<Cell>> maItr = mergedAlignments.iterator();
			while (maItr.hasNext()) {
				Set<Cell> r = maItr.next();
				
				for (Cell c_r : r) {
				r_marked.add(c_r);	
				}

				if (!r_marked.contains(bestCell)) {
				r_marked.add(bestCell);		
				
				
				maTmp.addAll(mergedAlignments);
				
				System.out.println("maTmp now contains " + maTmp.size() + " merged alignments");		
				
				
				//TO-DO: Implement isConsistent from Alcomo
				if (isConsistent(onto1, onto2, r_marked)) {
					
					System.err.println("They are consistent!");
					
					System.err.println("a_marked contains " + a_marked.size() + " cells.");
					
					Iterator<Cell> a_markedItr = a_marked.iterator();
					while (a_markedItr.hasNext()) {
						Cell a_markedCell = a_markedItr.next();
						System.err.println("Contents of a_markedCell: ");
						System.err.println(a_markedCell.getObject1AsURI().getFragment() + " - " + a_markedCell.getObject2AsURI().getFragment());
						
						if (!bestCell.getObject1().equals(a_markedCell.getObject1()) || !bestCell.getObject2().equals(a_markedCell.getObject2())) {
							a_marked.add(a_markedCell);
						} else {
							System.err.println(bestCell.getObject1AsURI().getFragment() + " - " + bestCell.getObject2AsURI().getFragment() + " and " +
						a_markedCell.getObject1AsURI().getFragment() + " - " + a_markedCell.getObject2AsURI().getFragment() + " have equal objects");
						}
					}
					
					//TO-DO: This is throwing a concurrentModificationException if we don´t move the contents from 'mergedAlignments' to 'maTmp'
					maTmp.add(r_marked);	
	
				} //end if	
				
		} //end if
				
			} //end for
				
		} //end while
		System.out.println("a_marked now contains: "+ a_marked.size() + " cells");
		
		System.out.println("Recursive call next...");
		buildAlignments(onto1, onto2, a_marked, maTmp);	
		return mergedAlignments;

	}
	
	
	/* Backup: Generates merged alignments, but not correct ones
	 * public static Set<Set<Cell>> buildAlignments(OWLOntology onto1, OWLOntology onto2, Set<Cell> a_input,
			Set<Set<Cell>> mergedAlignments) throws AlignmentException {
		

		//if there are no cells in a_input return null
		if (a_input.isEmpty()) {
			return null;
		}
		
		//rank the cells according to confidence strength - not sure if useful
		//TreeSet<Cell> aInput = rankAlignment(a_input);
		
			System.out.println("a_Input contains " + a_input.size() + " cell(s)");
			
			for (Cell c: a_input) {
				System.out.println("- " + c.getObject1AsURI().getFragment() + " - " + c.getObject2AsURI().getFragment());
			}
		
			//find the currently best cell in a_input
		Cell bestCell = findBestCell(a_input);
		System.out.println("The bestCell from input is " + bestCell.getObject1AsURI().getFragment() + " - " + bestCell.getObject2AsURI().getFragment());
		
		//initialize a_marked that will hold all cells - bestCell
		Set<Cell> a_marked = new HashSet<Cell>();
		
		//initialize r_marked that will hold each existing merged alignments + the best cell (these will be checked for consistency)
		Set<Cell> r_marked = new HashSet<Cell>();

		Iterator<Cell> aItr = a_input.iterator();
		
		//iterating through each cell in a_input
		while (aItr.hasNext()) {
			
			Cell thisCell = aItr.next();
			System.out.println("Checking thisCell " + thisCell.getObject1AsURI().getFragment() + " - " + thisCell.getObject2AsURI().getFragment());
			
			//as long as the current cell is not the same as the best cell, add current cell to a_marked for another iteration
			if (!thisCell.equals(bestCell)) {
				a_marked.add(thisCell);
				//System.out.println("Adding " + thisCell.getObject1AsURI().getFragment() + " - " + thisCell.getObject2AsURI() + " to a_merged");
			}//end if
			
			//add all existing merged alignments to r_marked
			for (Set<Cell> r : mergedAlignments) {
				r_marked.addAll(r);	
			} //end for

				//if r_marked does not already contain bestCell, add bestCell to r_marked
				if (!r_marked.contains(bestCell)) {
				r_marked.add(bestCell);
				//System.out.println("Adding " + bestCell.getObject1AsURI().getFragment() + " - " + bestCell.getObject2AsURI().getFragment() + " to r");
				} //end if
				
				//TO-DO: Implement isConsistent from Alcomo
				if (isConsistent(onto1, onto2, r_marked)) {
					
					//System.out.println("Adding " + r_marked.toString() + " to mergedAlignments");
					mergedAlignments.add(r_marked);					
				} //end if	
		} //end while
		System.out.println("a_marked now contains: "+ a_marked.size());
		
		System.out.println("Recursive call next...");

		buildAlignments(onto1, onto2, a_marked, mergedAlignments);
		
		//System.out.println("The merged alignment (where r_marked contains " + r_marked.size() + " cells) is consistent!");
		
		return mergedAlignments;	
		
	}*/
	
	
	
	// find the cell with the highest score
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

	// Sort by strength, requires a TreeSet, not a Set
	public static TreeSet<Cell> rankAlignment(Set<Cell> input) throws AlignmentException {

		BasicAlignment ranked = new URIAlignment();

		for (Cell c : input) {
			ranked.addAlignCell(c.getObject1(), c.getObject2(), c.getRelation().toString(), c.getStrength());
		}

		TreeSet<Cell> rankedAlignment = ranked.getSortedIterator();

		return rankedAlignment;
	}

	// return an alignment where a cell is removed
	public static Set<Cell> getRest(Cell removedCell, Set<Cell> a) throws AlignmentException {

		Set<Cell> restSet = new HashSet<Cell>();

		for (Cell c : a) {
			restSet.add(c);
		}

		restSet.remove(removedCell);

		return restSet;

	}

	public static boolean isConsistent(String onto1, String onto2, Set<Cell> a) throws AlcomoException {
		
		boolean isConsistent = true;
		
		/*Settings.BLACKBOX_REASONER = Settings.BlackBoxReasoner.PELLET;
		Settings.ONE_TO_ONE = false;
		
		IOntology sourceOnt = new IOntology(onto1);
		IOntology targetOnt = new IOntology(onto2);
		
		//TO-DO: print the set of alignment cells to disk so that it can be parsed by Alcomo
		String alignmentPath = null;
		
		Mapping mapping = new Mapping(alignmentPath);
		mapping.applyThreshhold(0.6);
		
		ExtractionProblem ep = new ExtractionProblem(
				ExtractionProblem.ENTITIES_CONCEPTSPROPERTIES,
				ExtractionProblem.METHOD_OPTIMAL,
				ExtractionProblem.REASONING_EFFICIENT
				);
		
		ep.bindSourceOntology(sourceOnt);
		ep.bindTargetOntology(targetOnt);
		ep.bindMapping(mapping);
		
		ep.solve();
		
		if (ep.isCoherentExtraction()) {
			isConsistent = true;
		} else {
			isConsistent = false;
		}	*/	

		return isConsistent;

	}

	public static void main(String[] args) throws AlignmentException, AlcomoException {

		// import the ontologies

		/*// import the alignment files
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

		Set<Cell> testSet = new HashSet<Cell>();

		for (Cell c : a1) {
			testSet.add(c);
		}*/
		
		File testFile = new File("./files/alignmentCombiner/conference-ekaw/SubGraphTest.rdf");
		AlignmentParser parser = new AlignmentParser();
		BasicAlignment testAlignment = (BasicAlignment) parser.parse(testFile.toURI().toString());
		
		Set<Alignment> testAlignments = new HashSet<Alignment>();
		testAlignments.add(testAlignment);
		
		Set<Cell> testSet = new HashSet<Cell>();

		for (Cell c : testAlignment) {
			testSet.add(c);
		}
		

		// import the owl files
		String onto1 = "files/alignmentCombiner/conference-ekaw/Conference.owl";
		String onto2 = "files/alignmentCombiner/conference-ekaw/ekaw.owl";
		
		Set<Set<Cell>> ma = initStrategy(onto1, onto2, testAlignments);
		 
		System.out.println("Number of merged alignments: ");
		if (!ma.isEmpty()) {
		System.out.println(ma.size());
		} else {
			System.out.println("MergedAlignments is empty");
		}
		
		System.out.println("Printing the merged alignments: ");
		for (Set<Cell> s : ma) {
			for (Cell c : s) {
				System.out.println(c.getObject1AsURI().getFragment() + " - " + c.getObject2AsURI().getFragment());
			}
			System.out.println("\n");
		}
		
		 

	}

}

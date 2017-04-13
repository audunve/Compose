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

/**
 * @author audunvennesland 26. mar. 2017
 */
public class SubgraphStrategy {

	public static Set<Set<Cell>> initStrategy(OWLOntology onto1, OWLOntology onto2, Set<Alignment> inputAlignments)
			throws AlignmentException {

		Alignment inputSet = SimpleVoteAlgo.createVotedAlignment(inputAlignments);
		Set<Cell> input = new HashSet<Cell>();

		for (Cell c : inputSet) {
			input.add(c);
		}

		Iterator<Cell> aItr = input.iterator();

		Set<Set<Cell>> mergedAlignments = new HashSet<Set<Cell>>();
		Set<Set<Cell>> r_set = new HashSet<Set<Cell>>();
		
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
			r_set.add(r);
			
	
		}
		
		if (!a_marked.isEmpty()) {
			
		mergedAlignments = buildAlignments(onto1, onto2, a_marked, r_set);
		
		}
		
		System.out.println("Sending from inputStrategy to buildAlignments: " + a_marked.size() + " (a_marked) " + r.size() + " (r)");

		return mergedAlignments;

	}
	
	public static Set<Set<Cell>> buildAlignments(OWLOntology onto1, OWLOntology onto2, Set<Cell> a_input,
			Set<Set<Cell>> mergedAlignments) throws AlignmentException {
		
		
		
		//if there are no cells in a_input return null
		if (a_input.isEmpty()) {
			return null;
			
			//if a_input still contains cells - process them
		} else {
		
			System.out.println("a_input contains " + a_input.size() + " cell(s)");
			
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
			}
			
			//add all existing merged alignments to r_marked
			for (Set<Cell> r : mergedAlignments) {
				r_marked.addAll(r);	
				//System.out.println("...Adding content from mergedAlignments to r_marked...");
			}

				//if r_marked does not already contain bestCell, add bestCell to r_marked
				if (!r_marked.contains(bestCell)) {
				r_marked.add(bestCell);
				System.out.println("Adding " + bestCell.getObject1AsURI().getFragment() + " - " + bestCell.getObject2AsURI().getFragment() + " to r");
				}
				
				//TO-DO: Implement isConsistent from Alcomo
				if (isConsistent(onto1, onto2, r_marked)) {
					
					System.out.println("Adding " + r_marked.toString() + " to mergedAlignments");
					mergedAlignments.add(r_marked);
					
				}
				buildAlignments(onto1, onto2, a_marked, mergedAlignments);
		}
		//System.out.println("The merged alignment (where r_marked contains " + r_marked.size() + " cells is consistent!");
		return mergedAlignments;
		
		//System.out.println("r_marked now contains: "+ r_marked.size());
		
		} //end else
		
		//return mergedAlignments;
		
	}
	
	/*public static Set<Set<Cell>> buildAlignments(OWLOntology onto1, OWLOntology onto2, Set<Cell> a_input,
			Set<Set<Cell>> mergedAlignments) throws AlignmentException {
		
		if (a_input.isEmpty()) {
			return null;
		}
		
		Cell bestCell = findBestCell(a_input);
		System.out.println("The bestCell from input is " + bestCell.getObject1AsURI().getFragment() + " - " + bestCell.getObject2AsURI());
		Set<Cell> a_marked = new HashSet<Cell>();

		Iterator<Cell> aItr = a_input.iterator();
		
		while (aItr.hasNext()) {
			
			Cell thisCell = aItr.next();
			//System.out.println("Checking thisCell " + thisCell.getObject1AsURI().getFragment() + " - " + thisCell.getObject2AsURI());
			
			if (!thisCell.equals(bestCell)) {
				a_marked.add(thisCell);
				//System.out.println("Adding " + thisCell.getObject1AsURI().getFragment() + " - " + thisCell.getObject2AsURI() + " to a_merged");
			}

			Set<Cell> r_marked = new HashSet<Cell>();
			for (Set<Cell> r : mergedAlignments) {
				r_marked.addAll(r);	
				
				System.out.println("r_marked already contains: ");
				for (Cell rCell : r) {
					System.out.println(rCell.getObject1AsURI().getFragment() + " - " + rCell.getObject2AsURI().getFragment());
				}
				
				
				if (!r_marked.contains(bestCell)) {
				r_marked.add(bestCell);
				System.out.println("Adding " + bestCell.getObject1AsURI().getFragment() + " - " + bestCell.getObject2AsURI() + " to r");
				}
				
				//TO-DO: Implement isConsistent from Alcomo
				if (isConsistent(onto1, onto2, r_marked)) {
					
					mergedAlignments.add(r_marked);
				}
			}

		}
		

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

	public static boolean isConsistent(OWLOntology onto1, OWLOntology onto2, Set<Cell> a) {

		boolean isConsistent = true;

		return isConsistent;

	}

	public static void main(String[] args) throws AlignmentException, OWLOntologyCreationException {

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
		File ontoFile1 = new File("./files/alignmentCombiner/conference-ekaw/Conference.owl");
		File ontoFile2 = new File("./files/alignmentCombiner/conference-ekaw/ekaw.owl");

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto1 = manager.loadOntologyFromOntologyDocument(ontoFile1);
		OWLOntology onto2 = manager.loadOntologyFromOntologyDocument(ontoFile2);

		
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

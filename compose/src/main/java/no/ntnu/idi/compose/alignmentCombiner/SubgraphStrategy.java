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

		//TreeSet<Cell> a = rankAlignment(input);

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
			
			System.out.println("r contains " + r.size() + " cells");
			
			for (Cell r_cell : r) {
				System.out.println(r_cell.getObject1AsURI() + " - " + r_cell.getObject2AsURI());
			}
			
	
		}
		
		mergedAlignments = buildAlignments(onto1, onto2, a_marked, r_set);
		
		System.out.println("Sending to buildAlignments: " + a_marked.size() + " (a_marked) " + r.size() + " (r)");

		return mergedAlignments;

	}
	
	public static Set<Set<Cell>> buildAlignments(OWLOntology onto1, OWLOntology onto2, Set<Cell> a_input,
			Set<Set<Cell>> mergedAlignments) throws AlignmentException {
		
		if (a_input.isEmpty()) {
			return null;
		}
		
		Cell bestCell = findBestCell(a_input);
		Set<Cell> a_marked = new HashSet<Cell>();

		Iterator<Cell> aItr = a_input.iterator();
		
		while (aItr.hasNext()) {
			
			Cell thisCell = aItr.next();
			
			if (!thisCell.equals(bestCell)) {
				a_marked.add(thisCell);
			}

			Set<Cell> r_marked = new HashSet<Cell>();
			for (Set<Cell> r : mergedAlignments) {
				r_marked.addAll(r);	
				r_marked.add(bestCell);
				
				if (isConsistent(onto1, onto2, r_marked)) {
					
					mergedAlignments.add(r_marked);
				}
			}

		}
		

		return mergedAlignments;
		
	}
	
	/*public static Set<Set<Cell>> buildAlignments(OWLOntology onto1, OWLOntology onto2, Set<Cell> a_input,
			Set<Set<Cell>> mergedAlignments) throws AlignmentException {
		
		if (a_input.isEmpty()) {
			return null;
		}
		
		//rank the cells in a_input into a
		TreeSet<Cell> a = rankAlignment(a_input); 
		
		Iterator<Cell> aItr = a.iterator();
		
		while (aItr.hasNext()) {
			for (Set<Cell> r : mergedAlignments) {
				System.out.println("Testing r : ");
				Set<Cell> r_marked = new HashSet<Cell>();
				r_marked.addAll(r);
				if (isConsistent(onto1, onto2, r_marked)) {
					mergedAlignments.add(r_marked);
					return buildAlignments(onto1, onto2, a, mergedAlignments);
				}
			}
			aItr.remove();
		}
		return mergedAlignments;
		
	}*/
	
/*	// this is a recursive method that builds a merged alignment if
			public static Set<Set<Cell>> buildAlignments(OWLOntology onto1, OWLOntology onto2, Set<Cell> a_input,
					Set<Set<Cell>> mergedAlignments) throws AlignmentException {

				if (a_input.isEmpty()) {
					return null;
				}

				TreeSet<Cell> a = rankAlignment(a_input); 
				
				Iterator<Cell> aItr = a.iterator();
				while (aItr.hasNext()) {
					Cell bestCell = aItr.next();
					//iterate through existing R´s in mergedAlignment
					Iterator<Set<Cell>> mergedAlignmentsItr = mergedAlignments.iterator();
					while (mergedAlignmentsItr.hasNext()) {
						Set<Cell> r = mergedAlignmentsItr.next();
						r.add(bestCell);
						if (isConsistent(onto1, onto2, r)) {
							//TO-DO: Check if objects are equal (line 28-32) for stable marriage
							mergedAlignments.add(r);
							//aItr.remove();
							//return mergedAlignments;
							} //end if
						} //end for
					} //end while
				return mergedAlignments;
				} //end function
*/				
				
	
	/*// this is a recursive method that builds a merged alignment if
		public static Set<Set<Cell>> buildAlignments(OWLOntology onto1, OWLOntology onto2, Set<Cell> a_input,
				Set<Set<Cell>> mergedAlignments) throws AlignmentException {

			if (a_input.isEmpty()) {
				return null;
			}

			TreeSet<Cell> a = rankAlignment(a_input); // not sure if this ranking is
														// needed
			
			Set<Cell> a_marked = getRest(bestCell, a);
			for (Cell c : a) {
				Cell bestCell = findBestCell(a_input);

				//TO-DO: Initialize the Set<Cell> r_marked before the loop (or try with a while loop and iterator to
				//avoid the ConcurrentModificationException
				Set<Cell> r_marked = new HashSet<Cell>();
				for (Set<Cell> rItr : mergedAlignments) {
					for (Cell cl : rItr) {
						
						
						if (!r_marked.contains(cl)) {
							System.out.println("Adding " + cl.getObject1AsURI().getFragment() + " - " + cl.getObject2AsURI().getFragment() + " to r_marked (cl)");
						r_marked.add(cl);
				}
					}
					System.out.println("Adding " + bestCell.getObject1AsURI().getFragment() + " - " + bestCell.getObject2AsURI().getFragment() + " to r_marked (bestCell)");
					r_marked.add(bestCell);

					if (isConsistent(onto1, onto2, r_marked)) {
						Iterator<Cell> a_markedItr = a_marked.iterator();

						while (a_markedItr.hasNext()) {
							Cell a_markedCell = a_markedItr.next();

							if (bestCell.getObject1().equals(a_markedCell.getObject1())
									|| bestCell.getObject2().equals(a_markedCell.getObject2())) {
								a_markedItr.remove();
							} // end if
						} // end while

						mergedAlignments.add(r_marked);
						//System.out.println("Size of mergedAlignments: " + mergedAlignments.size());
						// return buildAlignments(onto1, onto2, a_marked,
						// mergedAlignments);
					} // end if
				} // end for
			} // end for
			return buildAlignments(onto1, onto2, a_marked, mergedAlignments);

		}*/

	/*// this is a recursive method that builds a merged alignment if
	public static Set<Set<Cell>> buildAlignments(OWLOntology onto1, OWLOntology onto2, Set<Cell> a_input,
			Set<Set<Cell>> mergedAlignments) throws AlignmentException {

		if (a_input.isEmpty()) {
			return null;
		}

		TreeSet<Cell> a = rankAlignment(a_input); // not sure if this ranking is
													// needed
		Cell bestCell = findBestCell(a_input);
		Set<Cell> a_marked = getRest(bestCell, a);
		for (Cell c : a) {

			//TO-DO: Initialize the Set<Cell> r_marked before the loop (or try with a while loop and iterator to
			//avoid the ConcurrentModificationException
			Set<Cell> r_marked = new HashSet<Cell>();
			for (Set<Cell> rItr : mergedAlignments) {
				for (Cell cl : rItr) {
					
					
					if (!r_marked.contains(cl)) {
						System.out.println("Adding " + cl.getObject1AsURI().getFragment() + " - " + cl.getObject2AsURI().getFragment() + " to r_marked (cl)");
					r_marked.add(cl);
			}
				}
				System.out.println("Adding " + bestCell.getObject1AsURI().getFragment() + " - " + bestCell.getObject2AsURI().getFragment() + " to r_marked (bestCell)");
				r_marked.add(bestCell);

				if (isConsistent(onto1, onto2, r_marked)) {
					Iterator<Cell> a_markedItr = a_marked.iterator();

					while (a_markedItr.hasNext()) {
						Cell a_markedCell = a_markedItr.next();

						if (bestCell.getObject1().equals(a_markedCell.getObject1())
								|| bestCell.getObject2().equals(a_markedCell.getObject2())) {
							a_markedItr.remove();
						} // end if
					} // end while

					mergedAlignments.add(r_marked);
					//System.out.println("Size of mergedAlignments: " + mergedAlignments.size());
					// return buildAlignments(onto1, onto2, a_marked,
					// mergedAlignments);
				} // end if
			} // end for
		} // end for
		return buildAlignments(onto1, onto2, a_marked, mergedAlignments);

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

		Set<Cell> rankedAlignment = rankAlignment(testSet);

		// import the owl files
		File ontoFile1 = new File("./files/alignmentCombiner/conference-ekaw/Conference.owl");
		File ontoFile2 = new File("./files/alignmentCombiner/conference-ekaw/ekaw.owl");

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto1 = manager.loadOntologyFromOntologyDocument(ontoFile1);
		OWLOntology onto2 = manager.loadOntologyFromOntologyDocument(ontoFile2);

		
		Set<Set<Cell>> mergedAlignments = initStrategy(onto1, onto2, testAlignments);
		 
		System.out.println("Number of merged alignments " +
		mergedAlignments.size());
		
		System.out.println("Printing merged alignments");
		
		for (Set<Cell> a : mergedAlignments) { System.out.println("Printing merged alignment "); for (Cell c : a) {
		System.out.println(c.getObject1AsURI().getFragment() + " - " + c.getObject2AsURI().getFragment() + " - " +
		c.getRelation().toString() + " - " + c.getStrength()); } }
		 

	}

}

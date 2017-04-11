package no.ntnu.idi.compose.alignmentCombiner;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import org.apache.jena.ext.com.google.common.collect.Iterators;
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

	public static void initStrategy(OWLOntology onto1, OWLOntology onto2, Set<Alignment> inputAlignments)
			throws AlignmentException {
		
		Alignment inputSet = SimpleVoteAlgo.createVotedAlignment(inputAlignments);
		Set<Cell> input = new HashSet<Cell>();
		
		for (Cell c : inputSet) {
			input.add(c);
		}
		
		TreeSet<Cell> a = rankAlignment(input);
		
		Iterator<Cell> aItr = a.iterator();
		
		Set<Set<Cell>> mergedAlignments = new HashSet<Set<Cell>>();
		
		while (aItr.hasNext()) {
			Cell bestCell = findBestCell(a);
			Set<Cell> r = new HashSet<Cell>();
			r.add(bestCell);
			Set<Cell> a_marked = getRest(bestCell, a);
			mergedAlignments.add(r);
			mergedAlignments = buildAlignments(onto1, onto2, a_marked, mergedAlignments);
		}

		/*Set<Set<Cell>> mergedAlignments = new HashSet<Set<Cell>>();
		Set<Cell> subGraph = new HashSet<Cell>();

		BasicAlignment allAlignmentsInitial = (BasicAlignment) SimpleVoteAlgo.createVotedAlignment(inputAlignments);

		Alignment allAlignments = (Alignment) allAlignmentsInitial.clone();
		
		//create a set to hold all cells in allAlignments
		Set<Cell> cellSet = new HashSet<Cell>();
		
		for (Cell c : allAlignments) {
		cellSet.add(c);
		}
		
		//rank cellSet by highest conf
		TreeSet<Cell> 
		
		Set<Cell> temp = new HashSet<Cell>();
		temp.addAll(cellSet);
		
		
		
		
		Iterator<Cell> itrCell = cellSet.iterator();
		
		
		//Cell bestCell = findBestCorrespondence(temp);
		while (!cellSet.isEmpty()) {
		while (itrCell.hasNext()) {
			Cell thisCell = itrCell.next();
			
			if (isBestCell(thisCell, temp)) {
				System.out.println(thisCell.getObject1AsURI().getFragment() + " - " + thisCell.getObject2AsURI().getFragment() + " is the best cell");
				subGraph.add(thisCell);
				mergedAlignments.add(subGraph);
				Set<Cell> rest = getRest(thisCell,temp);
				mergedAlignments = buildAlignments(onto1, onto2, rest, mergedAlignments);
				itrCell.remove();
				temp.remove(thisCell);
			} else {
				System.out.println(thisCell.getObject1AsURI().getFragment() + " - " + thisCell.getObject2AsURI().getFragment() + " is not the best cell");
				continue;
			}
		}*/
			
			
		//}
		
/*		while (itrCell.hasNext()) {
			Cell thisCell = itrCell.next();
			System.out.println("thisCell: " + thisCell.getObject1AsURI().getFragment() + " - " + thisCell.getObject2AsURI().getFragment());
			
			Cell bestCell = findBestCorrespondence(temp);
			System.out.println("bestCell: " + bestCell.getObject1AsURI().getFragment() + " - " + bestCell.getObject2AsURI().getFragment());
			
			if (thisCell.equals(bestCell)) {
				System.out.println("bestCell matches thisCell");
				subGraph.add(bestCell);
				Set<Cell> rest = getRest(bestCell,temp);
				//System.out.println("rest contains " + rest.size() + " cells");		
				mergedAlignment = buildAlignments(onto1, onto2, rest, subGraph);
				itrCell.remove();
			}
			temp.remove(bestCell);
			//System.out.println("temp contains " + temp.size() + " cells");
		}*/
		
		
//		return mergedAlignments;

	}
	
	// this is a recursive method that builds a merged alignment if
		public static Set<Set<Cell>> buildAlignments(OWLOntology onto1, OWLOntology onto2, Set<Cell> a_input, Set<Set<Cell>> mergedAlignments)
				throws AlignmentException {
			
			
			if (a_input.isEmpty()) {
				return null;
			}
			
			TreeSet<Cell> a = rankAlignment(a_input); //not sure if this ranking is needed
			Cell bestCell = findBestCell(a_input);
			Set<Cell> a_marked = getRest(bestCell, a);
			for (Cell c : a) {

				for (Set<Cell> r_marked : mergedAlignments) {
					r_marked.add(bestCell);
					
					if (isConsistent(onto1, onto2, r_marked)) {
						Iterator<Cell> a_markedItr = a_marked.iterator();
						
						while (a_markedItr.hasNext()) {
							Cell a_markedCell = a_markedItr.next();
							
							if (bestCell.getObject1().equals(a_markedCell.getObject1()) || bestCell.getObject2().equals(a_markedCell.getObject2())) {
								a_markedItr.remove();
							} //end if
						} //end while
						
						mergedAlignments.add(r_marked);
						//return buildAlignments(onto1, onto2, a_marked, mergedAlignments);
					} 
				}
				
			}
			return buildAlignments(onto1, onto2, a_marked, mergedAlignments);
			
		}
			
			/*Iterator<Cell> aItr = a.iterator();
			
			Set<Cell> a_marked = new HashSet<Cell>();
			
			while (aItr.hasNext()) {
				Cell thisCell = aItr.next();
				
				for (Set<Cell> r_marked : mergedAlignments) {
					r_marked.add(thisCell);
					a.remove(thisCell);
					a_marked.addAll(a);
					
					//TO-DO: Implement isConsistent properly, now all are consistent
					if (isConsistent(onto1, onto2, r_marked)) {
						Iterator<Cell> a_markedItr = a_marked.iterator();
						
						while (a_markedItr.hasNext()) {
							Cell c = a_markedItr.next();
							
							if (c.getObject1().equals(thisCell.getObject1()) || c.getObject2().equals(thisCell.getObject2())) {
								a_markedItr.remove();
							}
							
							mergedAlignments.add(r_marked);
							return buildAlignments(onto1, onto2, a, mergedAlignments);
							
						}
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
	
	// if the score of thisCell (c) equals the max score of the set, isBestCell = true
	public static boolean isBestCell(Cell c, Set<Cell> a) throws AlignmentException {
		
		double conf = c.getStrength();
		System.out.println("Score of thisCell: " + conf);
		
		Set<Double> scores = new HashSet<Double>();
		
		for (Cell cl : a) {
			scores.add(cl.getStrength());
		}
		
		double highScore = Collections.max(scores);
		System.out.println("Max score of the set: " + highScore);
		
		boolean isBest = false;

		if (conf == highScore) {
			isBest = true;
		} else {isBest = false;}
		
		return isBest;

		
	}
	
	//Sort by strength, requires a TreeSet, not a Set
	public static TreeSet<Cell> rankAlignment (Set<Cell> input) throws AlignmentException {
		
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

		// import the alignment files
		File af1 = new File("./files/alignmentCombiner/conference-ekaw/conference-ekaw-alignment-aml.rdf");
		File af2 = new File("./files/alignmentCombiner/conference-ekaw/conference-ekaw-alignment-logmap.rdf");
		File af3 = new File("./files/alignmentCombiner/conference-ekaw/conference-ekaw-alignment-compose.rdf");

		AlignmentParser parser = new AlignmentParser();
		BasicAlignment a1 = (BasicAlignment) parser.parse(af1.toURI().toString());
		BasicAlignment a2 = (BasicAlignment) parser.parse(af2.toURI().toString());
		BasicAlignment a3 = (BasicAlignment) parser.parse(af3.toURI().toString());

		// Cell bestCell = findBestCorrespondence(a2);

		Set<Alignment> inputAlignments = new HashSet<Alignment>();
		inputAlignments.add(a1);
		inputAlignments.add(a2);
		inputAlignments.add(a3);
		
		Set<Cell> testSet = new HashSet<Cell>();
		
		for (Cell c : a1) {
			testSet.add(c);
		}
		
		
		Set<Cell> rankedAlignment = rankAlignment(testSet);

		/*// import the owl files
		File ontoFile1 = new File("./files/alignmentCombiner/conference-ekaw/Conference.owl");
		File ontoFile2 = new File("./files/alignmentCombiner/conference-ekaw/ekaw.owl");

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto1 = manager.loadOntologyFromOntologyDocument(ontoFile1);
		OWLOntology onto2 = manager.loadOntologyFromOntologyDocument(ontoFile2);

		Set<Set<Cell>> mergedAlignments = initStrategy(onto1, onto2, inputAlignments);

		System.out.println("Number of merged alignments " + mergedAlignments.size());

		System.out.println("Printing merged alignments");

		for (Set<Cell> a : mergedAlignments) {
			System.out.println("Printing merged alignment ");
			for (Cell c : a) {
				System.out.println(c.getObject1AsURI().getFragment() + " - " + c.getObject2AsURI().getFragment() + " - "
						+ c.getRelation().toString() + " - " + c.getStrength());
			}
		}*/

	}

}



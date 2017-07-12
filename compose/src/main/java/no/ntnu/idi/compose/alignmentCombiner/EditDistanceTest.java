package no.ntnu.idi.compose.alignmentCombiner;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;

import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.BasicCell;
import fr.inrialpes.exmo.align.parser.AlignmentParser;

/**
 * @author audunvennesland
 * 20. apr. 2017 
 */
public class EditDistanceTest {

	public static void main(String[] args) throws AlignmentException {

		File testFile1 = new File("./files/alignmentCombiner/conference-ekaw/Edit1.rdf");
		File testFile2 = new File("./files/alignmentCombiner/conference-ekaw/Edit2.rdf");

		AlignmentParser parser = new AlignmentParser(); 
		parser.parse(testFile1.toURI().toString());
		BasicAlignment a1 = (BasicAlignment) parser.parse(testFile1.toURI().toString());
		BasicAlignment a2 = (BasicAlignment) parser.parse(testFile2.toURI().toString());

		int distance = computeDistance(a1, a2);

		System.out.println("The distance is " + distance);


	}

	public static int computeDistance(Alignment a1, Alignment a2 ) throws AlignmentException {

		Set<Cell> noMatch = new HashSet<Cell>();
		Set<Cell> modification = new HashSet<Cell>();
		Set<Cell> equals = new HashSet<Cell>();

		for (Cell c1 : a1) {
			//get all cells in a2 that have the same object 1 as c1
			Set<Cell> s2 = a2.getAlignCells1( c1.getObject1() );

			System.out.println("Checking "  + c1.getObject1AsURI().getFragment() + " - " + c1.getObject2AsURI().getFragment());

			if (s2 == null) {
				System.out.println("Adding "  + c1.getObject1AsURI().getFragment() + " - " + c1.getObject2AsURI().getFragment() + " to noMatch (+2)");
				noMatch.add(c1);	
			}

			if (s2 != null) {

				for (Cell c2 : s2) {
					//if both objects are the same for both cells
					if (c1.getObject2().equals(c2.getObject2()) && c1.getRelation().equals(c2.getRelation())) {
						System.out.println("Adding "  + c1.getObject1AsURI().getFragment() + " - " + c1.getObject2AsURI().getFragment() + " to equals (0)");
						equals.add(c1);

					} else {
						//their relation type doesn´t match -> modification
						modification.add(c1);
						System.out.println("Adding "  + c1.getObject1AsURI().getFragment() + " - " + c1.getObject2AsURI().getFragment() + " to modification (+1) since its relation type is different from " +
								c2.getObject1AsURI().getFragment() + " - " + c2.getObject2AsURI().getFragment());
					}
				}
			}
		}

		int noMatchSize = noMatch.size();
		int modifications = modification.size();

		return (noMatchSize * 2) + (modifications * 1);

	}

}



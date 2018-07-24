package compose.misc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Cell;

import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.BasicRelation;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.eval.PRecEvaluator;
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

	public static void main(String[] args) throws AlignmentException, IOException, URISyntaxException {

		File af1 = new File("./files/experiment_06032018/datasets/d1/alignments/equivalence/aixm_airportheliport-aerodromeinfrastructure-PropertyMatcher0.5.rdf");
		File af2 = new File("./files/experiment_06032018/datasets/d1/alignments/equivalence/aixm_airportheliport-aerodromeinfrastructure-WNSyn0.95.rdf");
		File af3 = new File("./files/experiment_06032018/datasets/d1/alignments/equivalence/aixm_airportheliport-aerodromeinfrastructure-ISub0.95.rdf");

		AlignmentParser parser = new AlignmentParser();
		BasicAlignment a1 = (BasicAlignment) parser.parse(af1.toURI().toString());
		BasicAlignment a2 = (BasicAlignment) parser.parse(af2.toURI().toString());
		BasicAlignment a3 = (BasicAlignment) parser.parse(af3.toURI().toString());

		Set<Alignment> inputAlignments = new HashSet<Alignment>();
		inputAlignments.add(a1);
		inputAlignments.add(a2);
		inputAlignments.add(a3);
		
		String experiment = "301-302";

		System.out.println("Computing intersect Strict");
		BasicAlignment intersect_strict = (BasicAlignment) intersectStrictly(af1, af2, af3);
		
		// store the new alignment
		File outputAlignment_intersect_strict = new File("./files/ER2017/"+experiment+"/intersect_strict_norm.rdf");
		
		PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(outputAlignment_intersect_strict)), true);
		AlignmentVisitor renderer = new RDFRendererVisitor(writer);
		
		System.out.println("Printing the intersect strict alignment to file");
		intersect_strict.render(renderer);
		writer.flush();
		writer.close();
			
		System.out.println("Computing intersect relaxed");
		BasicAlignment intersect_relaxed = intersectRelaxed(inputAlignments);
		
		// store the new alignment
		File outputAlignment_intersect_relaxed = new File("./files/ER2017/"+experiment+"/intersect_relaxed_norm.rdf");

		writer = new PrintWriter(new BufferedWriter(new FileWriter(outputAlignment_intersect_relaxed)), true);
		renderer = new RDFRendererVisitor(writer);

		System.out.println("Printing the intersect relaxed alignment to file");
		intersect_relaxed.render(renderer);
		writer.flush();
		writer.close();
		
		System.out.println("Evaluating the intersect strict alignments against the reference alignment");
		AlignmentParser aparser = new AlignmentParser(0);
		
		Alignment referenceAlignment = aparser.parse(new URI("file:files/ER2017/"+experiment+"/"+experiment+"_refalign.rdf"));
		Alignment evaluatedAlignment = aparser.parse(new URI("file:files/ER2017/"+experiment+"/intersect_strict.rdf"));
		Properties p = new Properties();
		
		PRecEvaluator eval_intersect_strict = new PRecEvaluator(referenceAlignment, evaluatedAlignment);
		
		eval_intersect_strict.eval(p);
		System.out.println("------------------------------");
		System.out.println("Evaluator scores intersect strict:");
		System.out.println("------------------------------");
		System.out.println("F-measure: " + eval_intersect_strict.getResults().getProperty("fmeasure").toString());
		System.out.println("Precision: " + eval_intersect_strict.getResults().getProperty("precision").toString());
		System.out.println("Recall: " + eval_intersect_strict.getResults().getProperty("recall").toString());
		
		System.out.println("\nEvaluating the intersect relaxed alignments against the reference alignment");
		aparser = new AlignmentParser(0);
		
		referenceAlignment = aparser.parse(new URI("file:files/ER2017/"+experiment+"/"+experiment+"_refalign.rdf"));
		evaluatedAlignment = aparser.parse(new URI("file:files/ER2017/"+experiment+"/intersect_relaxed.rdf"));
		p = new Properties();
		
		PRecEvaluator eval_intersect_relaxed = new PRecEvaluator(referenceAlignment, evaluatedAlignment);
		
		eval_intersect_relaxed.eval(p);
		System.out.println("------------------------------");
		System.out.println("Evaluator scores intersect relaxed:");
		System.out.println("------------------------------");
		System.out.println("F-measure: " + eval_intersect_relaxed.getResults().getProperty("fmeasure").toString());
		System.out.println("Precision: " + eval_intersect_relaxed.getResults().getProperty("precision").toString());
		System.out.println("Recall: " + eval_intersect_relaxed.getResults().getProperty("recall").toString());

	}

	}


package no.ntnu.idi.compose.composition;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.Properties;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Cell;

import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import no.ntnu.idi.compose.matchers.ClassEq_String_Matcher;


public class SequentialComposition {

	static double threshold;
	static File outputAlignment = null;


	/**
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
	public static Alignment weightedSequentialComposition(File alignmentFile1, File alignmentFile2, File alignmentFile3) throws AlignmentException {

		//BasicAlignment completeMatchAlignment = new BasicAlignment();
		Alignment completeMatchAlignment = new URIAlignment();
		//BasicAlignment intermediateAlignment = new BasicAlignment();
		Alignment intermediateAlignment = new URIAlignment();

		//load the alignments
		AlignmentParser parser = new AlignmentParser();
		BasicAlignment a1 = (BasicAlignment)parser.parse(alignmentFile1.toURI().toString());
		BasicAlignment a2 = (BasicAlignment)parser.parse(alignmentFile2.toURI().toString());
		BasicAlignment a3 = (BasicAlignment)parser.parse(alignmentFile3.toURI().toString());
		
		//System.err.println( c.getObject1()+" "+((BasicRelation)(c.getRelation())).getPrettyLabel()+" "+c.getObject2() );

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
		
		//TO-DO: should remove duplicates before returning the completed alignment


		return completeMatchAlignment;		
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

		//BasicAlignment completeMatchAlignment = new BasicAlignment();
		Alignment completeMatchAlignment = new URIAlignment();
		//BasicAlignment intermediateAlignment = new BasicAlignment();
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
						intermediateAlignment.addAlignCell(cell1.getObject1(), cell1.getObject2(), cell1.getRelation().toString(), cell1.getStrength());
						//if the current cells strength is higher, this cells strength is retained
					} else if (cell2.getStrength() >= cell1.getStrength()) {
						intermediateAlignment.addAlignCell(cell2.getObject1(), cell2.getObject2(), cell2.getRelation().toString(), cell2.getStrength());
					}
					//if the cells are not equal, we add the cells from both the previous alignment and the current alignment.
				} else {
					intermediateAlignment.addAlignCell(cell1.getObject1(), cell1.getObject2(), cell1.getRelation().toString(), cell1.getStrength());
					intermediateAlignment.addAlignCell(cell2.getObject1(), cell2.getObject2(), cell2.getRelation().toString(), cell2.getStrength());
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
						completeMatchAlignment.addAlignCell(cell2.getObject1(), cell2.getObject2(), cell2.getRelation().toString(), cell2.getStrength());
						//if the current cells strength is higher, this is retained
					} else if (cell3.getStrength() >= cell2.getStrength()) {
						completeMatchAlignment.addAlignCell(cell3.getObject1(), cell3.getObject2(), cell3.getRelation().toString(), cell3.getStrength());
					}
					//if the cells are not equal, we add the cells from both the previous alignment and the current alignment.
				} else {
					completeMatchAlignment.addAlignCell(cell2.getObject1(), cell2.getObject2(), cell2.getRelation().toString(), cell2.getStrength());
					completeMatchAlignment.addAlignCell(cell3.getObject1(), cell3.getObject2(), cell3.getRelation().toString(), cell3.getStrength());
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

		File a1 = new File("./files/OAEI2011/301-303/COMPOSE-Subsumption_String.rdf");
		File a2 = new File("./files/OAEI2011/301-303/COMPOSE-Subsumption_WordNet.rdf");
		File a3 = new File("./files/OAEI2011/301-303/COMPOSE-Subsumption_SubClass.rdf");

		BasicAlignment newAlignment = (BasicAlignment) weightedSequentialComposition(a1, a2, a3);

		//store the new alignment
		File outputAlignment = new File("./files/OAEI2011/301-303/TestSequential.rdf");

		PrintWriter writer = new PrintWriter(
				new BufferedWriter(
						new FileWriter(outputAlignment)), true); 
		AlignmentVisitor renderer = new RDFRendererVisitor(writer);

		newAlignment.render(renderer);
		writer.flush();
		writer.close();
		
		/*BasicAlignment partialMatchAlignment = (BasicAlignment) partialMatch(outputAlignment);
		
		File partialMatchAlignmentFile = new File("./files/experiment_eswc17/alignments/biblio-bibo/a8.rdf");
		
		writer = new PrintWriter(
				new BufferedWriter(
						new FileWriter(partialMatchAlignmentFile)), true); 
		renderer = new RDFRendererVisitor(writer);

		partialMatchAlignment.render(renderer);
		writer.flush();
		writer.close();*/


	}
}

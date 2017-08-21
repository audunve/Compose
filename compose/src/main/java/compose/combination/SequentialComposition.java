package compose.combination;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Cell;

import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.parser.AlignmentParser;


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
	public static Alignment weightedSequentialComposition3(File alignmentFile1, File alignmentFile2, File alignmentFile3) throws AlignmentException {

		//BasicAlignment completeMatchAlignment = new BasicAlignment();
		Alignment completeMatchAlignment = new URIAlignment();
		//BasicAlignment intermediateAlignment = new BasicAlignment();
		Alignment intermediateAlignment = new URIAlignment();

		//load the alignments
		AlignmentParser parser = new AlignmentParser();
		BasicAlignment a1 = (BasicAlignment)parser.parse(alignmentFile1.toURI().toString());
		BasicAlignment a2 = (BasicAlignment)parser.parse(alignmentFile2.toURI().toString());
		BasicAlignment a3 = (BasicAlignment)parser.parse(alignmentFile3.toURI().toString());


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
		
		//remove duplicates before returning the completed alignment
		((BasicAlignment) completeMatchAlignment).normalise();

		return completeMatchAlignment;		
	}


	/**
	 * Returns an alignment where correspondences that are identified both by the previous matcher and the current matcher. 
	 * If the correspondence is new, that is, identified only by the current matcher, or if the correspondence is only identified by the previous matcher(s) 
	 * and not the current one, the correspondence is added to the alignment with equally reduced weight. 
	 * The weighting scheme applied in this study is to add (or reduce) 12 percent to the confidence of the correspondence. 
	 * @param alignmentFile1 the first input alignment
	 * @param alignmentFile2 the second input alignment
	 * @param alignmentFile3 the third input alignment
	 * @param alignmentFile4 the fourth input alignment
	 * @return an alignment with weighted correspondences
	 * @throws AlignmentException
	 */
	public static Alignment weightedSequentialComposition4(File alignmentFile1, File alignmentFile2, File alignmentFile3, File alignmentFile4) throws AlignmentException {

		Alignment firstAlignment = new URIAlignment();
		Alignment secondAlignment = new URIAlignment();
		Alignment finalAlignment = new URIAlignment();


		//load the alignments
		AlignmentParser parser = new AlignmentParser();
		BasicAlignment a1 = (BasicAlignment)parser.parse(alignmentFile1.toURI().toString());
		BasicAlignment a2 = (BasicAlignment)parser.parse(alignmentFile2.toURI().toString());
		BasicAlignment a3 = (BasicAlignment)parser.parse(alignmentFile3.toURI().toString());
		BasicAlignment a4 = (BasicAlignment)parser.parse(alignmentFile4.toURI().toString());
		

		//compare correspondences (cells) in a1 and a2. If a cell in a2 already exists in a1, add strength to it.
		for (Cell cell1 : a1) {
			for (Cell cell2 : a2) {				
				//if the cells are equal (contains similar entities)
				if (cell2.getObject1().equals(cell1.getObject1()) && cell2.getObject2().equals(cell1.getObject2())) {
					//if the strength in the previous alignment is higher, this cell is added + weight
					if (cell1.getStrength() >= cell2.getStrength()) {
						firstAlignment.addAlignCell(cell1.getObject1(), cell1.getObject2(), cell1.getRelation().toString(), increaseCellStrength(cell1.getStrength()));
						//if the current cells strength is higher, this cells strength is retained
					} else if (cell2.getStrength() >= cell1.getStrength()) {
						firstAlignment.addAlignCell(cell2.getObject1(), cell2.getObject2(), cell2.getRelation().toString(), cell2.getStrength());
					}
					//if the cells are not equal, we add the cells from both the previous alignment and the current alignment, but give them reduced strength.
				} else {
					firstAlignment.addAlignCell(cell1.getObject1(), cell1.getObject2(), cell1.getRelation().toString(), reduceCellStrength(cell1.getStrength()));
					firstAlignment.addAlignCell(cell2.getObject1(), cell2.getObject2(), cell2.getRelation().toString(), reduceCellStrength(cell2.getStrength()));
					continue;
				}
			}
		}


		//compare correspondences (cells) in the current alignment. If a cell in a3 already exists in the previous alignment, add strength to it. 
		for (Cell cell2 : firstAlignment) {
			for (Cell cell3 : a3) {
				//if the cells are equal (contains similar entities)
				if (cell3.getObject1().equals(cell2.getObject1()) && cell3.getObject2().equals(cell2.getObject2())) {
					//if the strength in the previous alignment is higher, this cell is added + weight
					if (cell2.getStrength() >= cell3.getStrength()) {
						secondAlignment.addAlignCell(cell2.getObject1(), cell2.getObject2(), cell2.getRelation().toString(), increaseCellStrength(cell2.getStrength()));
						//if the current cells strength is higher, this is retained
					} else if (cell3.getStrength() >= cell2.getStrength()) {
						secondAlignment.addAlignCell(cell3.getObject1(), cell3.getObject2(), cell3.getRelation().toString(), cell3.getStrength());
					}
					//if the cells are not equal, we add the cells from both the previous alignment and the current alignment, but give them reduced strength.
				} else {
					secondAlignment.addAlignCell(cell2.getObject1(), cell2.getObject2(), cell2.getRelation().toString(), reduceCellStrength(cell2.getStrength()));
					secondAlignment.addAlignCell(cell3.getObject1(), cell3.getObject2(), cell3.getRelation().toString(), reduceCellStrength(cell3.getStrength()));
					continue;
				}
			}
		}

		
		//compare cells with the current alignment and alignment 4
		for (Cell cell4 : secondAlignment) {
			for (Cell cell5 : a4) {
				if (cell5.getObject1().equals(cell4.getObject1()) && cell5.getObject2().equals(cell4.getObject2())) {
					//if the strength in the previous alignment is higher, this cell is added + weight
					if (cell4.getStrength() >= cell5.getStrength()) {
						finalAlignment.addAlignCell(cell5.getObject1(), cell5.getObject2(), cell5.getRelation().toString(), cell5.getStrength());
					} else if (cell5.getStrength() >= cell4.getStrength()) {
						finalAlignment.addAlignCell(cell5.getObject1(), cell5.getObject2(), cell5.getRelation().toString(), cell5.getStrength());
					}
					//if the cells are not equal, we add the cells from both the previous alignment and the current alignment, but give them reduced strength
				} else {
					finalAlignment.addAlignCell(cell4.getObject1(), cell4.getObject2(), cell4.getRelation().toString(), reduceCellStrength(cell4.getStrength()));
					finalAlignment.addAlignCell(cell5.getObject1(), cell5.getObject2(), cell5.getRelation().toString(), reduceCellStrength(cell5.getStrength()));
					continue;
				}
			}
		}
		
		
		//remove duplicates before returning the completed alignment
		((BasicAlignment) finalAlignment).normalise();

		return finalAlignment;		
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

		Alignment completeMatchAlignment = new URIAlignment();
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

		File a1 = new File("./files/experiment_eswc17/alignments/biblio-bibo/Subsumption_WordNet.rdf");
		File a2 = new File("./files/experiment_eswc17/alignments/biblio-bibo/Subsumption_SubClass.rdf");
		File a3 = new File("./files/experiment_eswc17/alignments/biblio-bibo/Subsumption_Compound.rdf");
		File a4 = new File("./files/experiment_eswc17/alignments/biblio-bibo/PathMatcher-biblio2bibo.rdf");
		

		BasicAlignment newAlignment = (BasicAlignment) weightedSequentialComposition4(a3, a2, a4, a1);

		//store the new alignment
		File outputAlignment = new File("./files/experiment_eswc17/alignments/biblio-bibo/web-intelligence-17-weightedSequentialCombination/path-compound-subclass-wordnet.rdf");

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

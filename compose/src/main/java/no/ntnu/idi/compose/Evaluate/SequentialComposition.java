package no.ntnu.idi.compose.Evaluate;

import java.io.File;
import java.util.Properties;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;

import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import no.ntnu.idi.compose.Matchers.ISubAlignment;


public class SequentialComposition {

	static double threshold;
	static File outputAlignment = null;


	public static Alignment completeMatch(File alignmentFile1, File alignmentFile2, File alignmentFile3) throws AlignmentException {

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
						intermediateAlignment.addAlignCell(cell1.getObject1(), cell1.getObject2(), "=", increaseCellStrength(cell1.getStrength()));
						//if the current cells strength is higher, this cells strength is retained
					} else if (cell2.getStrength() >= cell1.getStrength()) {
						intermediateAlignment.addAlignCell(cell2.getObject1(), cell2.getObject2(), "=", cell2.getStrength());
					}
					//if the cells are not equal, we add the cells from both the previous alignment and the current alignment, but give them reduced strength.
				} else {
					intermediateAlignment.addAlignCell(cell1.getObject1(), cell1.getObject2(), "=", reduceCellStrength(cell1.getStrength()));
					intermediateAlignment.addAlignCell(cell2.getObject1(), cell2.getObject2(), "=", reduceCellStrength(cell2.getStrength()));
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
						completeMatchAlignment.addAlignCell(cell2.getObject1(), cell2.getObject2(), "=", increaseCellStrength(cell2.getStrength()));
						//if the current cells strength is higher, this is retained
					} else if (cell3.getStrength() >= cell2.getStrength()) {
						completeMatchAlignment.addAlignCell(cell3.getObject1(), cell3.getObject2(), "=", cell3.getStrength());
					}
					//if the cells are not equal, we add the cells from both the previous alignment and the current alignment, but give them reduced strength.
				} else {
					completeMatchAlignment.addAlignCell(cell2.getObject1(), cell2.getObject2(), "=", reduceCellStrength(cell2.getStrength()));
					completeMatchAlignment.addAlignCell(cell3.getObject1(), cell3.getObject2(), "=", reduceCellStrength(cell3.getStrength()));
					continue;
				}
			}
		}
		
		//TO-DO: should remove duplicates before returning the completed alignment


		return completeMatchAlignment;		
	}

	public static Alignment partialMatch(File alignmentFile1) throws AlignmentException {

		//load the alignment
		AlignmentParser parser = new AlignmentParser();
		BasicAlignment a1 = (BasicAlignment)parser.parse(alignmentFile1.toURI().toString());
		
		Properties params = new Properties();
		params.setProperty("", "");
		
		//run a matcher with an already created alignment

		Alignment StringAlignment = (Alignment) ISubAlignment.matchAlignment(a1);

		return StringAlignment;

	}

	public static double increaseCellStrength(double inputStrength) {

		double newStrength = inputStrength + (inputStrength * 0.10);

		if (newStrength > 1.0) {
			newStrength = 1.0;
		}

		return newStrength;
	}
	
	public static double reduceCellStrength(double inputStrength) {

		double newStrength = inputStrength - (inputStrength * 0.10);

		if (newStrength > 1.0) {
			newStrength = 1.0;
		}

		return newStrength;
	}

	/*public static void main(String[] args) throws AlignmentException, IOException, URISyntaxException {

		File a1 = new File("./files/experiment_eswc17/alignments/biblio-bibo/a1.rdf");
		File a2 = new File("./files/experiment_eswc17/alignments/biblio-bibo/a2.rdf");
		File a3 = new File("./files/experiment_eswc17/alignments/biblio-bibo/a3.rdf");

		BasicAlignment newAlignment = (BasicAlignment) completeMatch(a1, a2, a3);

		//store the new alignment
		File outputAlignment = new File("./files/experiment_eswc17/alignments/biblio-bibo/a4.rdf");

		PrintWriter writer = new PrintWriter(
				new BufferedWriter(
						new FileWriter(outputAlignment)), true); 
		AlignmentVisitor renderer = new RDFRendererVisitor(writer);

		newAlignment.render(renderer);
		writer.flush();
		writer.close();
		
		BasicAlignment partialMatchAlignment = (BasicAlignment) partialMatch(outputAlignment);
		
		File partialMatchAlignmentFile = new File("./files/experiment_eswc17/alignments/biblio-bibo/a8.rdf");
		
		writer = new PrintWriter(
				new BufferedWriter(
						new FileWriter(partialMatchAlignmentFile)), true); 
		renderer = new RDFRendererVisitor(writer);

		partialMatchAlignment.render(renderer);
		writer.flush();
		writer.close();


	}*/
}

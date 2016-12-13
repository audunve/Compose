package no.ntnu.idi.compose.Evaluate;

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
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import no.ntnu.idi.compose.Matchers.ISubAlignment;



public class ParallelComposition {


	public static Alignment completeMatch(File alignmentFile1, File alignmentFile2, File alignmentFile3) throws AlignmentException {

		BasicAlignment completeMatchAlignment = new BasicAlignment();
		BasicAlignment intermediateAlignment = new BasicAlignment();

		//load the alignments
		AlignmentParser parser = new AlignmentParser();
		BasicAlignment a1 = (BasicAlignment)parser.parse(alignmentFile1.toURI().toString());
		BasicAlignment a2 = (BasicAlignment)parser.parse(alignmentFile2.toURI().toString());
		BasicAlignment a3 = (BasicAlignment)parser.parse(alignmentFile3.toURI().toString());

		//compare correspondences (cells) in a1 and a2. If a cell in a2 already exists in a1, add strength to it.
		for (Cell cell1 : a1) {
			for (Cell cell2 : a2) {				
				if (cell2.getObject1().equals(cell1.getObject1()) && cell2.getObject2().equals(cell1.getObject2())) {
					if (cell1.getStrength() >= cell2.getStrength()) {
						intermediateAlignment.addAlignCell(cell1.getObject1(), cell1.getObject2(), "=", increaseCellStrength(cell1.getStrength()));
					} else if (cell2.getStrength() >= cell1.getStrength()) {
						intermediateAlignment.addAlignCell(cell2.getObject1(), cell2.getObject2(), "=", cell2.getStrength());
					}
				} else {
					continue;
				}
			}
		}

		//compare correspondences (cells) in the current alignment. If a cell in a3 already exists in the previous alignment, add strength to it. 
		for (Cell cell2 : intermediateAlignment) {
			for (Cell cell3 : a3) {
				if (cell3.getObject1().equals(cell2.getObject1()) && cell3.getObject2().equals(cell2.getObject2())) {
					if (cell2.getStrength() >= cell3.getStrength()) {
						completeMatchAlignment.addAlignCell(cell2.getObject1(), cell2.getObject2(), "=", increaseCellStrength(cell2.getStrength()));
					} else if (cell3.getStrength() >= cell2.getStrength()) {
						completeMatchAlignment.addAlignCell(cell3.getObject1(), cell3.getObject2(), "=", cell3.getStrength());
					}
				} else {
					continue;
				}
			}
		}

		return completeMatchAlignment;		
	}

	public static Alignment partialMatch(File alignmentFile1) throws AlignmentException {

		//load the alignment
		AlignmentParser parser = new AlignmentParser();
		BasicAlignment a1 = (BasicAlignment)parser.parse(alignmentFile1.toURI().toString());
		
		Properties params = new Properties();
		params.setProperty("", "");
		
		//run a matcher with an already created alignment

		Alignment StringAlignment = ISubAlignment.matchAlignment(a1);

		return StringAlignment;

	}

	public static double increaseCellStrength(double inputStrength) {

		double newStrength = inputStrength + (inputStrength * 0.10);

		if (newStrength > 1.0) {
			newStrength = 1.0;
		}

		return newStrength;
	}

	public static void main(String[] args) throws AlignmentException, IOException, URISyntaxException {

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


	}
}

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



public class HybridComposition {


	//the alignments are merged using the ingest() method from the Alignment API
		public static Alignment merge(File alignmentFile1, File alignmentFile2, File alignmentFile3) throws AlignmentException {

			//load the alignments
			AlignmentParser parser = new AlignmentParser();
			BasicAlignment a1 = (BasicAlignment)parser.parse(alignmentFile1.toURI().toString());
			BasicAlignment a2 = (BasicAlignment)parser.parse(alignmentFile2.toURI().toString());
			BasicAlignment a3 = (BasicAlignment)parser.parse(alignmentFile3.toURI().toString());

			//BasicAlignment ingestISubAndSmoaAlignment = (BasicAlignment)(basicISubAlignment.clone());
			//ingestISubAndSmoaAlignment.ingest(basicSmoaAlignment);

			BasicAlignment a1_a2_merged = (BasicAlignment)(a1.clone());
			a1_a2_merged.ingest(a2);

			BasicAlignment a2_a3_merged = (BasicAlignment)(a1_a2_merged.clone());
			a2_a3_merged.ingest(a3);

			BasicAlignment completeMatchAlignment = (BasicAlignment)(a2_a3_merged.clone());

			return completeMatchAlignment;		
		}

	/*public static Alignment partialMatch(File alignmentFile1) throws AlignmentException {

		//load the alignment
		AlignmentParser parser = new AlignmentParser();
		BasicAlignment a1 = (BasicAlignment)parser.parse(alignmentFile1.toURI().toString());
		
		Properties params = new Properties();
		params.setProperty("", "");
		
		//run a matcher with an already created alignment

		BasicAlignment StringAlignment = ISubAlignment.matchAlignment(a1);

		return StringAlignment;

	}*/

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

		BasicAlignment newAlignment = (BasicAlignment) merge(a1, a2, a3);

		//store the new alignment
		File outputAlignment = new File("./files/experiment_eswc17/alignments/biblio-bibo/a4.rdf");

		PrintWriter writer = new PrintWriter(
				new BufferedWriter(
						new FileWriter(outputAlignment)), true); 
		AlignmentVisitor renderer = new RDFRendererVisitor(writer);

		newAlignment.render(renderer);
		writer.flush();
		writer.close();
		
		//BasicAlignment partialMatchAlignment = (BasicAlignment) partialMatch(outputAlignment);
		
		File partialMatchAlignmentFile = new File("./files/experiment_eswc17/alignments/biblio-bibo/a8.rdf");
		
		writer = new PrintWriter(
				new BufferedWriter(
						new FileWriter(partialMatchAlignmentFile)), true); 
		renderer = new RDFRendererVisitor(writer);

		//partialMatchAlignment.render(renderer);
		writer.flush();
		writer.close();


	}
}

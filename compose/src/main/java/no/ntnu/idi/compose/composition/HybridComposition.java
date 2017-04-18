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
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import no.ntnu.idi.compose.matchers.ClassEq_String_Matcher;


/**
 * This class computes an alignment from a hybrid composition. That is, it takes as input a set of alignments computed from each involved matcher independently 
 * and then merge their results based on the ingest() method from the Alignment API. The ingest() method puts all the correspondences from the input alignments together. 
 * @author audunvennesland
 * 2. feb. 2017
 */
public class HybridComposition {

	/**
	 * The alignments are merged using the ingest() method from the Alignment API
	 * @param alignmentFile1 the first input alignment
	 * @param alignmentFile2 the second input alignment
	 * @param alignmentFile3 the third input alignment
	 * @return completeMatchAlignment as a merged alignment from the input alignments
	 * @throws AlignmentException Base class for all Alignment Exceptions
	 */
		public static Alignment merge(File alignmentFile1, File alignmentFile2, File alignmentFile3) throws AlignmentException {

			//load the alignments
			AlignmentParser parser = new AlignmentParser();
			BasicAlignment a1 = (BasicAlignment)parser.parse(alignmentFile1.toURI().toString());
			BasicAlignment a2 = (BasicAlignment)parser.parse(alignmentFile2.toURI().toString());
			BasicAlignment a3 = (BasicAlignment)parser.parse(alignmentFile3.toURI().toString());

			BasicAlignment a1_a2_merged = (BasicAlignment)(a1.clone());
			a1_a2_merged.ingest(a2);

			BasicAlignment a2_a3_merged = (BasicAlignment)(a1_a2_merged.clone());
			a2_a3_merged.ingest(a3);

			BasicAlignment completeMatchAlignment = (BasicAlignment)(a2_a3_merged.clone());

			return completeMatchAlignment;		
		}


	/**
	 * Test method
	 * @param args
	 * @throws AlignmentException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static void main(String[] args) throws AlignmentException, IOException, URISyntaxException {

		File a1 = new File("./files/OAEI2011/301-303/COMPOSE-Subsumption_String.rdf");
		File a2 = new File("./files/OAEI2011/301-303/COMPOSE-Subsumption_WordNet.rdf");
		File a3 = new File("./files/OAEI2011/301-303/COMPOSE-Subsumption_SubClass.rdf");

		BasicAlignment newAlignment = (BasicAlignment) merge(a1, a2, a3);

		//store the new alignment
		File outputAlignment = new File("./files/OAEI2011/301-303/TestHybrid.rdf");

		PrintWriter writer = new PrintWriter(
				new BufferedWriter(
						new FileWriter(outputAlignment)), true); 
		AlignmentVisitor renderer = new RDFRendererVisitor(writer);

		newAlignment.render(renderer);
		writer.flush();
		writer.close();
		
		
		/*File partialMatchAlignmentFile = new File("./files/experiment_eswc17/alignments/conference-ekaw/a8.rdf");
		
		writer = new PrintWriter(
				new BufferedWriter(
						new FileWriter(partialMatchAlignmentFile)), true); 
		renderer = new RDFRendererVisitor(writer);

		writer.flush();
		writer.close();

		System.out.println("Aggregation completed!");*/
	}
}

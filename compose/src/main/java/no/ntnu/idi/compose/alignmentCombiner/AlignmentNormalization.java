package no.ntnu.idi.compose.alignmentCombiner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Cell;

import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.parser.AlignmentParser;

/**
 * @author audunvennesland 18. apr. 2017
 */
public class AlignmentNormalization {

	public static BasicAlignment normaliseScore(BasicAlignment aToBeNormalised, BasicAlignment a2, BasicAlignment a3) {

		double avgConf_a1 = aToBeNormalised.avgConfidence();
		System.out.println("Avg conf a1: " + avgConf_a1);
		double avgConf_a2 = a2.avgConfidence();
		System.out.println("Avg conf a2: " + avgConf_a2);
		double avgConf_a3 = a3.avgConfidence();
		System.out.println("Avg conf a3: " + avgConf_a3);

		double addedStrength = 100 * (((avgConf_a2 + avgConf_a3) / 2) - (avgConf_a1));
		System.out.println("addedStrength is " + addedStrength);

		for (Cell c : aToBeNormalised) {
			double currentStrength = c.getStrength();
			System.out.println("CurrentStrength is " + currentStrength);
			c.setStrength(currentStrength + ((addedStrength / 100) * currentStrength));
		}

		BasicAlignment normalisedAlignment = aToBeNormalised;
		return normalisedAlignment;
	}

	public static void main(String[] args) throws AlignmentException, IOException {

		String experiment = "303304";

		File af1 = new File("./files/ER2017/" + experiment + "/303-304-logmap.rdf");
		File af2 = new File("./files/ER2017/" + experiment + "/303-304-aml.rdf");
		File af3 = new File("./files/ER2017/" + experiment + "/303-304-compose.rdf");

		AlignmentParser parser = new AlignmentParser();
		BasicAlignment a1 = (BasicAlignment) parser.parse(af1.toURI().toString());
		BasicAlignment a2 = (BasicAlignment) parser.parse(af2.toURI().toString());
		BasicAlignment a3 = (BasicAlignment) parser.parse(af3.toURI().toString());

		BasicAlignment normalisedAlignment = normaliseScore(a3, a1, a2);

		for (Cell c : normalisedAlignment) {
			System.out.println(c.getObject1AsURI().getFragment() + " - " + c.getObject2AsURI().getFragment() + " - "
					+ c.getStrength());
		}

		// store the new alignment
		File outputAlignment = new File("./files/ER2017/" + experiment + "/303-304-compose_norm.rdf");

		PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(outputAlignment)), true);
		AlignmentVisitor renderer = new RDFRendererVisitor(writer);

		System.out.println("Printing the normalised alignment to file");
		normalisedAlignment.render(renderer);
		writer.flush();
		writer.close();

	}
}

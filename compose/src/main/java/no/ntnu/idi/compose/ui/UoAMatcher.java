package no.ntnu.idi.compose.ui;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.Properties;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.AlignmentVisitor;

import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
//import no.ntnu.idi.compose.matchers.InstanceMatcher;
import no.ntnu.idi.compose.matchers.Instance_Matcher;

public class UoAMatcher {
	
	public static void main(String[] args) throws AlignmentException, IOException, URISyntaxException {

		//Threshold for similarity score for which correspondences should be considered
		double threshold;
		final String MATCHER = "INSTANCE_STRING";
		String alignmentFileName = null;
		String onto1 = "ontolearning";
		String onto2 = "dbpedia";
		File ontoFile1 = new File("./files/UoA/TestTransportWithInstances1.owl");
		File ontoFile2 = new File("./files/UoA/TestTransportWithInstances2.owl");
		//File ontoFile1 = new File("./files/UoA/dbpedia.owl");
		//File ontoFile2 = new File("./files/UoA/yago.owl");
		File outputAlignment = null;

		PrintWriter writer = null;
		AlignmentVisitor renderer = null;

		//Parameters defining the (string) matching method to be applied
		Properties params = new Properties();

		AlignmentProcess a = null;

		switch(MATCHER) {

		case "INSTANCE_STRING":
			a = new Instance_Matcher();
			threshold = 0.6;

			//a.init(ontoFile1.toURI(), ontoFile2.toURI());
			a.init(ontoFile1.toURI(), ontoFile2.toURI());
			params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	

			alignmentFileName = "./files/UoA/" + onto1 + "-" + onto2 + "/Instance_String.rdf";

			outputAlignment = new File(alignmentFileName);

			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(outputAlignment)), true); 
			renderer = new RDFRendererVisitor(writer);

			BasicAlignment StringAlignment = (BasicAlignment)(a.clone());

			StringAlignment.cut(threshold);

			StringAlignment.render(renderer);
			writer.flush();
			writer.close();

			System.out.println("Matching completed!");
			break;


		}

	}


}

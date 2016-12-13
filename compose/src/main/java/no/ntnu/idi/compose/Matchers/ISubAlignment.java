package no.ntnu.idi.compose.Matchers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Cell;

import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import fr.inrialpes.exmo.ontowrap.OntowrapException;
import no.ntnu.idi.compose.Preprocess.Preprocessor;
import no.ntnu.idi.compose.algorithms.ISub;

public class ISubAlignment extends ObjectAlignment implements AlignmentProcess {

	static ISub isubMatcher = new ISub();

	public void align(Alignment alignment, Properties param) throws AlignmentException {

		try {
			// Match classes
			for ( Object cl2: ontology2().getClasses() ){
				for ( Object cl1: ontology1().getClasses() ){

					// add mapping into alignment object 
					addAlignCell(cl1,cl2, "=", iSubScore(cl1,cl2));  
				}

			}

		} catch (Exception e) { e.printStackTrace(); }
	}

	public double iSubScore(Object o1, Object o2) throws OntowrapException {


		//get the objects (entities)
		String s1 = ontology1().getEntityName(o1).toLowerCase();
		String s2 = ontology2().getEntityName(o2).toLowerCase();

		double measure = isubMatcher.score(s1, s2);

		return measure;

	}

	//must not just discard the correspondences if they are identified by the previous matcher, but not identified by this matcher
	public static Alignment matchAlignment(Alignment inputAlignment) throws AlignmentException {
		
		System.out.println("Running string alignment!");

		Alignment refinedAlignment = new URIAlignment();
		double score = 0;
		double threshold = 0.9;
		
		//match the objects (need to preprocess to remove URI) in every cell of the alignment
		for (Cell c : inputAlignment) {
			score = isubMatcher.score(Preprocessor.getString(c.getObject1().toString()), Preprocessor.getString(c.getObject2().toString()));
			if (score > threshold) {
				refinedAlignment.addAlignCell(c.getObject1(), c.getObject2(), "=", increaseCellStrength(score));
			} else {
				refinedAlignment.addAlignCell(c.getObject1(), c.getObject2(), "=", reduceCellStrength(score));
				continue;
			}
		}
		return refinedAlignment;
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

	/*public static void main(String[] args) throws AlignmentException, IOException {

		File alignmentFile1 = new File("./files/experiment_eswc17/alignments/biblio-bibo/classEq_String.rdf");

		//load the alignments
		AlignmentParser parser = new AlignmentParser();
		BasicAlignment a1 = (BasicAlignment)parser.parse(alignmentFile1.toURI().toString());

		BasicAlignment a7 = matchAlignment(a1);

		//store the new alignment
		File outputAlignmentFile = new File("./files/experiment_eswc17/alignments/biblio-bibo/a7.rdf");

		PrintWriter writer = new PrintWriter(
				new BufferedWriter(
						new FileWriter(outputAlignmentFile)), true); 
		AlignmentVisitor renderer = new RDFRendererVisitor(writer);

		a7.render(renderer);
		writer.flush();
		writer.close();

	}*/

}



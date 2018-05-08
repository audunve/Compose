package compose.misc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Cell;

import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.parser.AlignmentParser;

/**
 * @author audunvennesland
 * 19. aug. 2017 
 */
public class AlignmentOperations {
	
	/**
	 * Increases an input value by 12 percent
	 * @param inputStrength the strength/confidence for a correspondence to be increased
	 * @return a value 12 percent higher than its input value
	 */
	public static double increaseCellStrength(double inputStrength, double addition) {

		double newStrength = inputStrength + (inputStrength * (addition/100));

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
	public static double reduceCellStrength(double inputStrength, double reduction) {

		double newStrength = inputStrength - (inputStrength * (reduction/100));

		if (newStrength > 1.0) {
			newStrength = 1.0;
		}

		return newStrength;
	}
	
	/**
	 * Prints all the cells in an alignment with the objects being represented in the string format (not their URIs)
	 * @param inputAlignment
	 * @throws AlignmentException
	 */
	public static void printAlignmentAsString(BasicAlignment inputAlignment) throws AlignmentException {
		
		for (Cell c : inputAlignment) {
			System.out.println(c.getObject1AsURI().getFragment() + " - " + c.getObject2AsURI().getFragment() + " - " + c.getRelation().getRelation() + " - " + c.getStrength());
		}
		
	}
	
	/**
	 * Prints only the objects (entities) from all the cells in an alignment with the objects being represented in the string format (not their URIs)
	 * @param inputAlignment
	 * @throws AlignmentException
	 */
	public static void printAlignmentEntitiesAsString(BasicAlignment inputAlignment) throws AlignmentException {
		
		for (Cell c : inputAlignment) {
			System.out.println(c.getObject1AsURI().getFragment() + " - " + c.getObject2AsURI().getFragment());
		}
		
	}
	
	public static BasicAlignment createDiffAlignment(File alignment1, File alignment2) throws AlignmentException {
		
		AlignmentParser parser = new AlignmentParser();
		AlignmentParser parser2 = new AlignmentParser(1);
		
		BasicAlignment a1 = (BasicAlignment) parser.parse(alignment1.toURI().toString());
		BasicAlignment a2 = (BasicAlignment) parser2.parse(alignment2.toURI().toString());
		
		BasicAlignment diffAlignment = new BasicAlignment();
		
		diffAlignment = (BasicAlignment) a1.diff(a2);
		
		return diffAlignment;
		
	}
	
	/**
	 * Testing
	 * @param args
	 * @throws AlignmentException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws AlignmentException, IOException {
		
		File af = new File("./files/wndomainsexperiment/alignments/bibframe-schemaorg-ISub08.rdf");

		AlignmentParser parser = new AlignmentParser();
		BasicAlignment a1 = (BasicAlignment) parser.parse(af.toURI().toString());
		
		BasicAlignment a1Inversed = a1.inverse();
		
		System.out.println("The alignment contains " + a1.nbCells() + " correspondences");
		
		for (Cell c : a1) {
			System.out.println(c.getObject1() + " " + c.getObject2());
		}
		
		System.out.println("Inversed alignment\n:");
		
		for (Cell c : a1Inversed) {
			System.out.println(c.getObject1() + " " + c.getObject2());
		}
		
		printAlignmentEntitiesAsString(a1);
		
		File alignmentFile1 = new File("./files/wndomainsexperiment/alignments/dbpedia-sumo-ISub08.rdf");
		File alignmentFile2 = new File("./files//wndomainsexperiment/alignments/operations/dbpedia-sumo/test.rdf");

		
		BasicAlignment diffAlignment = createDiffAlignment(alignmentFile1, alignmentFile2);
		
		System.out.println("The diffAlignment contains " + diffAlignment.nbCells() + " cells");
		
		//store the computed alignment to file
		String diffAlignmentFileName = "./files//wndomainsexperiment/alignments/operations/dbpedia-sumo/diff.rdf";
		File outputAlignment = new File(diffAlignmentFileName);
		
		PrintWriter writer = new PrintWriter(
				new BufferedWriter(
						new FileWriter(outputAlignment)), true); 
		AlignmentVisitor renderer = new RDFRendererVisitor(writer);

		diffAlignment.render(renderer);

		writer.flush();
		writer.close();
	}

}

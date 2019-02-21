package evaluation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Scanner;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import mismatchdetection.ConceptScopeMismatch;
import mismatchdetection.StructureMismatch;
import net.didion.jwnl.JWNLException;
import utilities.StringUtilities;
import wordnetdomains.AlignmentFilter;

public class PostMatchingEvaluation {

	public static void main(String[] args) throws AlignmentException, URISyntaxException, JWNLException, IOException, OWLOntologyCreationException {

		// create a scanner so we can read the command-line input
		Scanner scanner = new Scanner(System.in);

		System.out.println("Starting the Post-matching Evaluation Application");

		System.out.print("\nSelect Single Alignment (1), Alignment Folder (2): ");
		String singleOrFolder = scanner.next();

		String inputAlignmentPath = null;
		String inputFolderPath = null;
		
		if (singleOrFolder.equals("1")) {
			System.out.print("\nEnter path to alignment to be inspected: ");
			inputAlignmentPath = scanner.next();
		} else {
			System.out.print("\nEnter path to folder holding alignments to be inspected: ");
			inputFolderPath = scanner.next();	
		}
		
//		System.out.print("\nEnter path to ontology 1: ");
//		String onto1Path = scanner.next();
//
//		System.out.print("Enter path to ontology 2: ");
//		String onto2Path = scanner.next();
		
		String onto1Path = "./files/_PHD_EVALUATION/ATMONTO-AIRM/ONTOLOGIES/ATMOntoCoreMerged.owl";
		String onto2Path = "./files/_PHD_EVALUATION/ATMONTO-AIRM/ONTOLOGIES/airm-mono.owl";
		
		System.out.println("Enter path for the output files: ");
		String outputPath = scanner.next();

		String postMatchingTechnique = null;
		
		System.out.println("\n1: Domain Dissimilarity");
		System.out.println("2: Concept Scope Mismatch");
		System.out.println("3: Structure Mismatch");
		System.out.println("4: Alignment Incoherence Check (Alcomo)");
		System.out.println("5: All Post-matching techniques");
		System.out.print("\nSelect from the above options: ");
		
		postMatchingTechnique = scanner.next();
		
		System.out.println("Enter path to the reference alignment: ");
		String refAlignPath = scanner.next();
		
		switch(postMatchingTechnique) {
		
		case "1":
			if (singleOrFolder == "1") {
			runDomainDissimilarity(inputAlignmentPath, outputPath, refAlignPath);
			} else {
			runDomainDissimilarityFolder(inputFolderPath, outputPath, refAlignPath);
			}
			break;
		
		case "2":
			runConceptScopeMismatchDetection(inputAlignmentPath, inputFolderPath, outputPath, refAlignPath);
			break;
		
		case "3":
			runStructureMismatchDetection(inputAlignmentPath, inputFolderPath, outputPath, refAlignPath, onto1Path, onto2Path);
			break;
		
		case "4":
			runAlignmentIncoherenceDetection(inputAlignmentPath, inputFolderPath, outputPath, refAlignPath);
			break;
		
		case "5":
			runDomainDissimilarity(inputAlignmentPath, outputPath, refAlignPath);
			runConceptScopeMismatchDetection(inputAlignmentPath, inputFolderPath, outputPath, refAlignPath);
			runStructureMismatchDetection(inputAlignmentPath, inputFolderPath, outputPath, refAlignPath, onto1Path, onto2Path);
			runAlignmentIncoherenceDetection(inputAlignmentPath, inputFolderPath, outputPath, refAlignPath);
			break;	
		}

	}
	
	private static void runDomainDissimilarity(String inputAlignmentPath, String outputPath, String refAlignPath) throws AlignmentException, URISyntaxException, JWNLException, IOException {
		AlignmentParser aparser = new AlignmentParser(0);
		URIAlignment inputAlignment = (URIAlignment) aparser.parse(new URI(StringUtilities.convertToFileURL(inputAlignmentPath)));
		URIAlignment outputAlignment = new URIAlignment();
		File outputAlignmentPath = new File(outputPath);
		
		//evaluate the input alignment
		Evaluator.evaluateSingleAlignment(inputAlignment, refAlignPath);
		
		PrintWriter writer = null;
		AlignmentVisitor renderer = null;


		outputAlignment = AlignmentFilter.filterAlignment((BasicAlignment) inputAlignment);

		
		writer = new PrintWriter(
				new BufferedWriter(
						new FileWriter(outputAlignmentPath)), true); 
		renderer = new RDFRendererVisitor(writer);
		
		writer.flush();
		writer.close();
		
		//evaluate the resulting alignment
		Evaluator.evaluateSingleAlignment(outputAlignment, refAlignPath);
		
		
	}
	
	private static void runDomainDissimilarityFolder(String inputFolderPath, String outputPath, String refAlignPath) throws AlignmentException, URISyntaxException, JWNLException, IOException {
		AlignmentParser aparser = new AlignmentParser(0);
		URIAlignment inputAlignment = new URIAlignment();
		URIAlignment outputAlignment = new URIAlignment();
		File outputAlignmentPath = null;
		
		PrintWriter writer = null;
		AlignmentVisitor renderer = null;
		
		File folder = new File(inputFolderPath);
		File[] filesInDir = folder.listFiles();

		for (int i = 0; i < filesInDir.length; i++) {
			inputAlignment = (URIAlignment) aparser.parse(new URI(StringUtilities.convertToFileURL(filesInDir[i].getPath())));
			outputAlignmentPath = new File(outputPath + "/DomainDissimilarity_" + filesInDir[i].getName());
			
			outputAlignment = AlignmentFilter.filterAlignment((BasicAlignment) inputAlignment);
			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(outputAlignmentPath)), true); 
			renderer = new RDFRendererVisitor(writer);
			
			writer.flush();
			writer.close();
		}
	}
	
	private static void runConceptScopeMismatchDetection(String inputAlignmentPath, String inputFolderPath, String outputPath, String refAlignPath) throws AlignmentException, URISyntaxException, JWNLException, IOException {
		AlignmentParser aparser = new AlignmentParser(0);
		URIAlignment inputAlignment = (URIAlignment) aparser.parse(new URI(StringUtilities.convertToFileURL(inputAlignmentPath)));
		URIAlignment outputAlignment = new URIAlignment();
		File outputAlignmentPath = new File(outputPath);
		
		//evaluate the input alignment
		Evaluator.evaluateSingleAlignment(inputAlignment, refAlignPath);
		
		PrintWriter writer = null;
		AlignmentVisitor renderer = null;

		
		if (inputAlignmentPath == null && inputFolderPath == null) {
			System.err.println("There is no path for input alignment nor alignment folder!");
		} else if (inputAlignmentPath != null) {
			//run AlignmentFilter for single alignment
			outputAlignment = ConceptScopeMismatch.detectConceptScopeMismatch((BasicAlignment) inputAlignment);
		} else {
			//run AlignmentFilter on all alignments in folder
		}
		
		writer = new PrintWriter(
				new BufferedWriter(
						new FileWriter(outputAlignmentPath)), true); 
		renderer = new RDFRendererVisitor(writer);
		
		writer.flush();
		writer.close();
		
		//evaluate the resulting alignment
		Evaluator.evaluateSingleAlignment(outputAlignment, refAlignPath);
	}
	
	private static void runStructureMismatchDetection(String inputAlignmentPath, String inputFolderPath, String outputPath, String refAlignPath, String onto1Path, String onto2Path) throws AlignmentException, URISyntaxException, IOException, OWLOntologyCreationException, JWNLException {
		AlignmentParser aparser = new AlignmentParser(0);
		URIAlignment inputAlignment = (URIAlignment) aparser.parse(new URI(StringUtilities.convertToFileURL(inputAlignmentPath)));
		URIAlignment outputAlignment = new URIAlignment();
		File outputAlignmentPath = new File(outputPath);
		
		File onto1File = new File(onto1Path);
		File onto2File = new File(onto2Path);
		
		//evaluate the input alignment
		Evaluator.evaluateSingleAlignment(inputAlignment, refAlignPath);
		
		PrintWriter writer = null;
		AlignmentVisitor renderer = null;

		
		if (inputAlignmentPath == null && inputFolderPath == null) {
			System.err.println("There is no path for input alignment nor alignment folder!");
		} else if (inputAlignmentPath != null) {
			//run AlignmentFilter for single alignment
			outputAlignment = StructureMismatch.detectStructureMismatches((BasicAlignment) inputAlignment, onto1File, onto2File);
		} else {
			//run AlignmentFilter on all alignments in folder
		}
		
		writer = new PrintWriter(
				new BufferedWriter(
						new FileWriter(outputAlignmentPath)), true); 
		renderer = new RDFRendererVisitor(writer);
		
		writer.flush();
		writer.close();
		
		//evaluate the resulting alignment
		Evaluator.evaluateSingleAlignment(outputAlignment, refAlignPath);
	}
	
	private static void runAlignmentIncoherenceDetection(String inputAlignmentPath, String inputFolderPath, String outputPath, String refAlignPath) throws AlignmentException, URISyntaxException {
		AlignmentParser aparser = new AlignmentParser(0);
		Alignment inputAlignment = aparser.parse(new URI(StringUtilities.convertToFileURL(inputAlignmentPath)));
		URIAlignment outputAlignment = new URIAlignment();
		
		if (inputAlignmentPath == null && inputFolderPath == null) {
			System.err.println("There is no path for input alignment nor alignment folder!");
		} else if (inputAlignmentPath != null) {
			//run AlignmentFilter for single alignment
			System.out.println("Running Alignment Incoherence detection on single alignment");
		} else {
			//run AlignmentFilter for all alignments in folder
			System.out.println("Running Alignment Incoherence detection on a folder of alignments");
		}
	}
	
	

}

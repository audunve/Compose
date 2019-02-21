package ui;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;

import evaluation.Evaluator;
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
//import matchercombination.AutoWeightPlusPlus_delete;
import matchercombination.ParallelCombination;
import matchercombination.SequentialCombination;
import utilities.StringUtilities;

public class CombinationUI {

	public static void main(String[] args) throws AlignmentException, IOException, URISyntaxException {
		
		
		String folderName = "./files/Amazon-GoogleProducts/alignments/test";
		String referenceAlignment = "./files/Amazon-GoogleProducts/refalign.rdf";
		
		//get all alignments in a folder into an array list
		File alignmentsFolder = new File (folderName);
		
		File[] filesInDir = alignmentsFolder.listFiles();
		
		ArrayList<Alignment> inputAlignments = new ArrayList<Alignment>();
		
		AlignmentParser aparser = new AlignmentParser(0);
		
		for (int i = 0; i < filesInDir.length; i++) {
			String URI = StringUtilities.convertToFileURL(folderName) + "/" + StringUtilities.stripPath(filesInDir[i].toString());
			inputAlignments.add(aparser.parse(new URI(URI)));

		}


		//Weighted Sequential Combination
		BasicAlignment weightedSequentialAlignment = (BasicAlignment) SequentialCombination.weightedSequentialCombination(inputAlignments);
		Evaluator.evaluateSingleAlignment(weightedSequentialAlignment, referenceAlignment);

//		File outputAlignment = new File("./files/Amazon-GoogleProducts/combination/WSC.rdf");
//		PrintWriter writer = new PrintWriter(
//				new BufferedWriter(
//						new FileWriter(outputAlignment)), true); 
//		AlignmentVisitor renderer = new RDFRendererVisitor(writer);
//
//		weightedSequentialAlignment.render(renderer);
//		writer.flush();
//		writer.close();

		//Simple Vote
		BasicAlignment simpleVoteAlignment = ParallelCombination.simpleVote(inputAlignments);
		Evaluator.evaluateSingleAlignment(simpleVoteAlignment, referenceAlignment);

		//Store the combined alignment
//		outputAlignment = new File("./files/Amazon-GoogleProducts/combination/SimpleVote.rdf");
//		writer = new PrintWriter(
//				new BufferedWriter(
//						new FileWriter(outputAlignment)), true); 
//		renderer = new RDFRendererVisitor(writer);
//
//		simpleVoteAlignment.render(renderer);
//		writer.flush();
//		writer.close();
		
		//Autoweight++
		
//		ArrayList<BasicAlignment> initialAlignmentsForAutoweight = new ArrayList<BasicAlignment>();
//		for (Alignment a : inputAlignments) {
//			initialAlignmentsForAutoweight.add((BasicAlignment)a);
//		}
//
//		
//		BasicAlignment autoweightAlignment = AutoWeightPlusPlus_delete.runAutoweightPlusPlus(initialAlignmentsForAutoweight);
//		
//		Evaluator.evaluateSingleAlignment(autoweightAlignment, referenceAlignment);

		//Store the combined alignment
//		outputAlignment = new File("./files/Amazon-GoogleProducts/combination/Autoweight++.rdf");
//		writer = new PrintWriter(
//				new BufferedWriter(
//						new FileWriter(outputAlignment)), true); 
//		renderer = new RDFRendererVisitor(writer);
//
//		autoweightAlignment.render(renderer);
//		writer.flush();
//		writer.close();

	}

}

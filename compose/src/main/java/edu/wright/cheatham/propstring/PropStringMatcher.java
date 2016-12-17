package edu.wright.cheatham.propstring;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentVisitor;

import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;

public class PropStringMatcher {
	
	final static File ontologyDir = new File("./experiment_eswc17/ontologies");
	final static File[] filesInDir = ontologyDir.listFiles();
	final static String prefix = "file:";
	static File outputAlignment = null;
	
	public static void main(String[] args) throws Exception {

		
		for (int i = 0; i < filesInDir.length; i++) {
			for (int j = i+1; j < filesInDir.length; j++) {
			if (filesInDir[i].isFile() && filesInDir[j].isFile() && i!=j) {
				System.out.println("Matching " + filesInDir[i] + " and " + filesInDir[j] );
				
				File f1 = filesInDir[i];
				File f2 = filesInDir[j];

				String name1 = "";
				String name2 = "";

				PropString matcher = new PropString();

				matcher.setNameA(name1);
				matcher.setNameB(name2);
				
				Alignment alignment = matcher.align(f1.toURI(), f2.toURI());

				String alignmentFileName = "./experiment_eswc17/alignments/" + StringProcessor.stripOntologyName(filesInDir[i].toString()) + 
						"-" + StringProcessor.stripOntologyName(filesInDir[j].toString()) + "/PropString-recall.rdf";
				
				outputAlignment = new File(alignmentFileName);

		FileOutputStream os = new FileOutputStream(outputAlignment);

		PrintWriter writer = new PrintWriter (
				new OutputStreamWriter(os, "UTF-8" ), true);

		AlignmentVisitor renderer = new RDFRendererVisitor(writer);
		alignment.render(renderer);
		writer.close();

		System.out.println("Completed matching process!");
	}
			}
		}
	}

}

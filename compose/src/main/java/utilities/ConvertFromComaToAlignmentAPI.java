package utilities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;

public class ConvertFromComaToAlignmentAPI {
	
	public static void main(String[] args) throws OWLOntologyCreationException, FileNotFoundException, AlignmentException, IOException {
		
		//public static URIAlignment convertFromComaToAlignmentAPI (String comaTextFilePath, OWLOntology onto1, OWLOntology onto2) 
		String comaFile = "./files/ESWC_WordEmbedding_Experiment/OAEI2011/coma/301302-coma.txt";
		File ontoFile1 = new File("./files/ESWC_WordEmbedding_Experiment/OAEI2011/coma/ontologies/301303/301303-301.rdf");
		File ontoFile2 = new File("./files/ESWC_WordEmbedding_Experiment/OAEI2011/coma/ontologies/301303/301303-303.rdf");
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto1 = manager.loadOntologyFromOntologyDocument(ontoFile1);
		OWLOntology onto2 = manager.loadOntologyFromOntologyDocument(ontoFile2);
		
		URIAlignment alignment = AlignmentOperations.convertFromComaToAlignmentAPI(comaFile, onto1, onto2);
		System.out.println("The alignment contains " + alignment.nbCells() + " cells");
		
		//store the alignment
		String output = "./files/ESWC_WordEmbedding_Experiment/OAEI2011/coma/alignments/301303.rdf";
		
		File outputAlignment = new File(output);

		PrintWriter writer = new PrintWriter(
				new BufferedWriter(
						new FileWriter(outputAlignment)), true); 
		AlignmentVisitor renderer = new RDFRendererVisitor(writer);
		
		alignment.render(renderer);

		writer.flush();
		writer.close();
		
	}

}

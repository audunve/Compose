package mismatchdetection;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import net.didion.jwnl.JWNLException;

import com.wcohen.ss.JaroWinkler;
import com.wcohen.ss.Levenstein;
import com.wcohen.ss.api.StringDistance;

import utilities.ISub;
import utilities.OntologyOperations;
import utilities.StringUtilities;

public class CalcEditDistanceRefAlign {
	
	final static ISub isubMatcher = new ISub();
	
	public static void main(String[] args) throws AlignmentException, OWLOntologyCreationException, FileNotFoundException, JWNLException {
		
		////compose/files/ESWC_ATMONTO_AIRM/Evaluation/ReferenceAlignments/ReferenceAlignment-EQ.rdf
		File refalignFile = new File("./files/ESWC_ATMONTO_AIRM/Evaluation/ReferenceAlignments/ReferenceAlignment-EQ.rdf");
		//File refalignFile = new File("./files/ESWC_WordEmbedding_Experiment/OAEI2011/ref_alignments/303304/303-304.rdf");
		AlignmentParser parser = new AlignmentParser();
		BasicAlignment refalign = (BasicAlignment) parser.parse(refalignFile.toURI().toString());	

		System.out.println("The avg ISub sim for the reference alignment is " + calcAvgAlignmentSim(refalign));
		
		File ontoFile1 = new File("./files/ESWC_WordEmbedding_Experiment/OAEI2011/ontologies/303304/303304-303.rdf");
		File ontoFile2 = new File("./files/ESWC_WordEmbedding_Experiment/OAEI2011/ontologies/303304/303304-304.rdf");
		
		double wnCoverageOnto1 = OntologyOperations.getWordNetCoverage(ontoFile1);
		double wnCoverageOnto2 = OntologyOperations.getWordNetCoverage(ontoFile2);
		
		System.out.println("The average wnCoverage for these two files is " + (wnCoverageOnto1 + wnCoverageOnto2)/2);


	}
	
	
	private static double calcEditDistance(String s1, String s2) {

		return new Levenstein().score(s1, s2);

	}
	
	private static double getISubScore(String s1, String s2) {
		
		return isubMatcher.score(s1, s2);
		
	}
	
	
	public static double calcAvgAlignmentSim (BasicAlignment alignment) throws AlignmentException {

		double tempSim = 0;
		int equalCounter = 0;
		
		for (Cell c : alignment) {
			System.out.println("Test: Sim is " + getISubScore(c.getObject1AsURI().getFragment(), c.getObject2AsURI().getFragment()));
			tempSim += getISubScore(c.getObject1AsURI().getFragment(), c.getObject2AsURI().getFragment());
			if (c.getObject1AsURI().getFragment().equals(c.getObject2AsURI().getFragment())) {
				equalCounter++;
			}
			
		}
		System.out.println("Test: the tempSim is " + tempSim + " and there are " + alignment.nbCells() + " relations");
		System.out.println("There are " + equalCounter + " equal concepts in this ontology");
		return tempSim / (double) alignment.nbCells();
	}
	

	public double calcAvgEditDistance(String refAlignFolder) throws AlignmentException, URISyntaxException {
		double avgEdit = 0;
		
		File folder = new File(refAlignFolder);
		File[] filesInDir = folder.listFiles();
		
		Alignment alignment = null;
		AlignmentParser aparser = new AlignmentParser(0);
		
		for (int i = 0; i < filesInDir.length; i++) {
			
			String URI = StringUtilities.convertToFileURL(refAlignFolder) + "/" + StringUtilities.stripPath(filesInDir[i].toString());

			alignment = aparser.parse(new URI(URI));
		}
		
		
		return avgEdit;
	}
	
	public static double calcISub(String s1, String s2) {

		return 100 - new Levenstein().score(s1, s2);

	}

	public double calcAvgISub(File refAlignFolder) {
		double avgEdit = 0;
		
		
		return avgEdit;
	}
}

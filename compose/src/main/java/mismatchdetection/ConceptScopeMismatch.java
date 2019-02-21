package mismatchdetection;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;

import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Cell;

import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.BasicConfidence;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.rel.A5AlgebraRelation;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import fr.inrialpes.exmo.ontowrap.OntowrapException;
import utilities.AlignmentOperations;
import utilities.StringUtilities;

public class ConceptScopeMismatch {
	
	public static void main(String[] args) throws AlignmentException, OntowrapException, IOException {
		
		/* ATMONTO-AIRM DATASET */
//		File inputAlignmentFile = new File("./files/ESWC_ATMONTO_AIRM/Evaluation/ATMONTO-AIRM/aml-atmonto-airm.rdf");
//		String output = "./files/ESWC_ATMONTO_AIRM/Evaluation/ATMONTO-AIRM/conceptScopeMismatchAlignment.rdf";
		
		/* BIBFRAME-SCHEMA.ORG DATASET */
//		File inputAlignmentFile = new File("./files/ESWC_ATMONTO_AIRM/Evaluation/Bibframe-Schemaorg/bibframe-schemaorg-aml.rdf");
//		String output = "./files/ESWC_ATMONTO_AIRM/Evaluation/Bibframe-Schemaorg/conceptScopeMismatchAlignment.rdf";	
		
		/* SWEET-ATMONTO DATASET */
		File inputAlignmentFile = new File("./files/ESWC_ATMONTO_AIRM/Evaluation/SWEET-ATMONTO/sweet-atmonto-aml.rdf");
		String output = "./files/ESWC_ATMONTO_AIRM/Evaluation/SWEET-ATMONTO/conceptScopeMismatchAlignment.rdf";		


		
		//parse the alignment file
		AlignmentParser parser = new AlignmentParser();
		BasicAlignment amlAlignment = (BasicAlignment) parser.parse(inputAlignmentFile.toURI().toString());
		
		System.out.println("The input alignment contains " + amlAlignment.nbCells() + " relations");
		
		URIAlignment conceptScopeMismatchAlignment = detectConceptScopeMismatch(amlAlignment);
				
		//write produced alignment to file
		PrintWriter writer = new PrintWriter(
				new BufferedWriter(
						new FileWriter(output)), true); 
		AlignmentVisitor renderer = new RDFRendererVisitor(writer);

		conceptScopeMismatchAlignment.render(renderer);
		
		System.out.println("The filtered alignment contains " + conceptScopeMismatchAlignment.nbCells() + " mismatch relations");
		
		writer.flush();
		writer.close();
		
		String a = "NavigationAid";
		String b = "RadioNavigationAid";
		
		System.out.println(isCompound(a,b));
		
		

	}
	
	/** 
	 * Detects "concept scope mismatches" on the basis of the following "compound pattern": the part component of the part-whole relationship includes the name of
	 * its whole as its qualifying compound. For example, an [aircraft]Engine represent a part of aircraft. 
	 * @param inputAlignment an already computed alignment
	 * @return the input alignment - the detected mismatch relations (cells)
	 * @throws AlignmentException
	   Nov 26, 2018
	 * @throws IOException 
	 */

	public static URIAlignment detectConceptScopeMismatch(BasicAlignment inputAlignment) throws AlignmentException {
		URIAlignment conceptScopeMismatchAlignment = new URIAlignment();
		
		//need to copy the URIs from the input alignment to the new alignment
		URI onto1URI = inputAlignment.getOntology1URI();
		URI onto2URI = inputAlignment.getOntology2URI();
		conceptScopeMismatchAlignment.init(onto1URI, onto2URI, A5AlgebraRelation.class, BasicConfidence.class);
		
		String qualifier = null;
		String compoundHead = null;
		for (Cell c : inputAlignment) {
			if (StringUtilities.isCompoundWord(c.getObject1AsURI().getFragment())) {
				qualifier = StringUtilities.getCompoundQualifier(c.getObject1AsURI().getFragment());
				compoundHead = StringUtilities.getCompoundHead(c.getObject1AsURI().getFragment());
				
				//e.g. [Cloud]Layer - Cloud || Aircraft[Flow]-Flow
				if (qualifier.toLowerCase().equals(c.getObject2AsURI().getFragment().toLowerCase()) || compoundHead.toLowerCase().equals(c.getObject2AsURI().getFragment().toLowerCase())) {
					conceptScopeMismatchAlignment.addAlignCell(c.getObject1(), c.getObject2(), c.getRelation().getRelation(), c.getStrength());
				}

			} else if (StringUtilities.isCompoundWord(c.getObject2AsURI().getFragment())) {
				qualifier = StringUtilities.getCompoundQualifier(c.getObject2AsURI().getFragment());
				compoundHead = StringUtilities.getCompoundHead(c.getObject2AsURI().getFragment());
				//e.g. [Sector] || Location-Reference[Location]
				if (qualifier.toLowerCase().equals(c.getObject1AsURI().getFragment().toLowerCase()) || compoundHead.toLowerCase().equals(c.getObject1AsURI().getFragment().toLowerCase())) {
					conceptScopeMismatchAlignment.addAlignCell(c.getObject1(), c.getObject2(), c.getRelation().getRelation(), c.getStrength());
				}
			}
		}
		
		System.out.println("The concept scope mismatch algorithm detected " +  conceptScopeMismatchAlignment.nbCells() + " mismatch relations");
		
		BasicAlignment filteredAlignment = AlignmentOperations.createDiffAlignment(inputAlignment, conceptScopeMismatchAlignment);
		
//		return (URIAlignment) conceptScopeMismatchAlignment;
		return (URIAlignment) filteredAlignment;

	}
	//String a = "Navigation|Aid";
	//String b = "Radio|Navigation|Aid";
	public static boolean isCompound(String a, String b) {
		
		boolean test = false;

		String[] acompounds = a.split("(?<=.)(?=\\p{Lu})");
		String[] bcompounds = b.split("(?<=.)(?=\\p{Lu})");
		
		System.out.println("length-1: " + bcompounds[bcompounds.length-1]);
		System.out.println("length-2: " + bcompounds[bcompounds.length-2]);
		System.out.println("length-3: " + bcompounds[bcompounds.length-3]);

		if (acompounds.length > 2 && bcompounds.length > 2) {
			
			System.err.println("true");

			//if (RadioNavigationAid.equals(Aid) || (RadioNavigationAid.equals(X|X|Aid)
			if (b.equals(acompounds[acompounds.length-1]) || b.equals(acompounds[acompounds.length-1]+acompounds[acompounds.length-2] + acompounds[acompounds.length-3])) {

				test = true;				
			}  
			
			//if (NavigationAid.equals(Aid) || (NavigationAid.equals(Radio|Navigation|Aid)
			System.out.println("Trying: " + a + " = " + bcompounds[bcompounds.length-1]);
			if (a.equals(bcompounds[bcompounds.length-1]) || a.equals(bcompounds[bcompounds.length-1]+bcompounds[acompounds.length-2] + bcompounds[bcompounds.length-3])) {
				
				test = true;
			}
			
			//if (NavigationAid.equals(NavigationAid) || (NavigationAid.equals(Radio|Navigation|Aid)
			System.out.println("Trying: " + a + " = " + bcompounds[bcompounds.length-1]);
			if (a.equals(bcompounds[bcompounds.length-1]) || a.equals(bcompounds[bcompounds.length-3]+bcompounds[acompounds.length-2] + bcompounds[bcompounds.length-1])) {
				
				test = true;
			}
			
			
		}
		 else if (acompounds.length > 1) {

			if (b.equals(acompounds[acompounds.length-1]) || b.equals(acompounds[acompounds.length-1]+acompounds[acompounds.length-2])) {

				test = true;
			}
		}
		return test;

	}

}

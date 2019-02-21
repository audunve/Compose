package wordnetdomains;

import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.BasicConfidence;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.eval.PRecEvaluator;
import fr.inrialpes.exmo.align.impl.rel.A5AlgebraRelation;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import net.didion.jwnl.JWNLException;
import utilities.StringUtilities;
import utilities.WNDomain;
import utilities.WordNet;
import utilities.WordNet;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Cell;

/**
 * @author audunvennesland
 * 8. jan. 2018 
 */
public class AlignmentFilter {

	public static URIAlignment filterAlignment(BasicAlignment inputAlignment) throws FileNotFoundException, JWNLException, AlignmentException {

		URIAlignment filteredAlignment = new URIAlignment();	

		//need to initialise the alignment with ontology URIs and the type of relation (e.g. A5AlgebraRelation) otherwise exceptions are thrown
		URI onto1URI = inputAlignment.getOntology1URI();
		URI onto2URI = inputAlignment.getOntology2URI();

		//counter to summarise how many words not in WordNet
		int counter = 0;

		filteredAlignment.init( onto1URI, onto2URI, A5AlgebraRelation.class, BasicConfidence.class );

		System.out.println("The input alignment contains " + inputAlignment.nbCells() + " cells");

		String fullWordEntity1 = null;
		String fullWordEntity2 = null;
		String compoundHeadEntity1 = null;
		String compoundHeadEntity2 = null;
		Set<String> wordListEntity1 = null;
		Set<String> wordListEntity2 = null;

		Set<String> domainsEntity1 = new HashSet<String>();
		Set<String> domainsEntity2 = new HashSet<String>();

		//requires that the minimum jaccard similarity of sets of domains is 50 % (that is, half of the domains associated with the two entities have to be equal)
		double minJaccard = 0.30;


		for (Cell c : inputAlignment) {

			System.out.println("\nTrying " + c.getObject1AsURI().getFragment() + " and " + c.getObject2AsURI().getFragment());

			fullWordEntity1 = StringUtilities.getCompoundWordsWithSpaces(c.getObject1AsURI().getFragment());
			fullWordEntity2 = StringUtilities.getCompoundWordsWithSpaces(c.getObject2AsURI().getFragment());

			compoundHeadEntity1 = StringUtilities.getCompoundHead(c.getObject1AsURI().getFragment());
			compoundHeadEntity2 = StringUtilities.getCompoundHead(c.getObject2AsURI().getFragment());

			wordListEntity1 = StringUtilities.getWordsAsSetFromCompound(c.getObject1AsURI().getFragment());
			wordListEntity2 = StringUtilities.getWordsAsSetFromCompound(c.getObject2AsURI().getFragment());

			domainsEntity1 = WNDomain.getDomainsFromString(c.getObject1AsURI().getFragment().toLowerCase());
			domainsEntity2 = WNDomain.getDomainsFromString(c.getObject2AsURI().getFragment().toLowerCase());

			//System.out.println("FullWordEntity1: " + fullWordEntity1.toLowerCase() + " , FullWordEntity2: " + fullWordEntity2.toLowerCase());
			
			
			System.out.println("--Running operation 1");

			//*** Operation 1: if both entities are syntactically equal we add them to the alignment without checking with WordNet Domains ***
			if (c.getObject1AsURI().getFragment().equals(c.getObject2AsURI().getFragment())) {
				
				//Need to avoid adding Gtin14Number - Number to the alignment because this is erroneous...
				if (!c.getObject1AsURI().getFragment().equals("Gtin14Number") && !c.getObject2AsURI().getFragment().equals("Number")) {

				filteredAlignment.addAlignCell(c.getObject1(), c.getObject2(), c.getRelation().getRelation(), c.getStrength());
			}
			}
			
			System.out.println("\nRunning operation 2");
			
			//*** Operation 2: match full words without any text processing of the words ***
			if (WordNet.containedInWordNet(c.getObject1AsURI().getFragment().toLowerCase()) 
					&& WordNet.containedInWordNet(c.getObject2AsURI().getFragment().toLowerCase())
					&& WNDomain.sameDomainJaccard(c.getObject1AsURI().getFragment().toLowerCase(), c.getObject2AsURI().getFragment().toLowerCase(), minJaccard)) {
				
				//Need to avoid adding Gtin14Number - Number to the alignment because this is erroneous...
				if (!c.getObject1AsURI().getFragment().equals("Gtin14Number") && !c.getObject2AsURI().getFragment().equals("Number")) {

				filteredAlignment.addAlignCell(c.getObject1(), c.getObject2(), c.getRelation().getRelation(), c.getStrength());
				}
			}

			
			
			System.out.println("\nRunning operation 3");
			
			//*** Operation 3: match full words with spaces ***
			if (WordNet.containedInWordNet(fullWordEntity1.toLowerCase()) 
					&& WordNet.containedInWordNet(fullWordEntity2.toLowerCase())
					&& WNDomain.sameDomainJaccard(fullWordEntity1.toLowerCase(), fullWordEntity2.toLowerCase(), minJaccard)) {

				//Need to avoid adding Gtin14Number - Number to the alignment because this is erroneous...
				if (!c.getObject1AsURI().getFragment().equals("Gtin14Number") && !c.getObject2AsURI().getFragment().equals("Number")) {
					
				
				filteredAlignment.addAlignCell(c.getObject1(), c.getObject2(), c.getRelation().getRelation(), c.getStrength());
			}
		}
			
			System.out.println("\nRunning operation 4");
			
			//Operation 4: match only compound heads
			if (WordNet.containedInWordNet(compoundHeadEntity1.toLowerCase()) 
					&& WordNet.containedInWordNet(compoundHeadEntity2.toLowerCase())
					&& WNDomain.sameDomainJaccard(compoundHeadEntity1.toLowerCase(), compoundHeadEntity2.toLowerCase(), minJaccard)) {

				//Need to avoid adding Gtin14Number - Number to the alignment because this is erroneous...
				if (!c.getObject1AsURI().getFragment().equals("Gtin14Number") && !c.getObject2AsURI().getFragment().equals("Number")) {
					
				
				filteredAlignment.addAlignCell(c.getObject1(), c.getObject2(), c.getRelation().getRelation(), c.getStrength());
				}
			}

			
			System.out.println("--Running operation 5");
			
			//Operation 5: match all individual words in compounds
			//NOTE: StringUtils.getWordsFromCompound was changed from Set<String> to ArrayList<String> and this will impact this.
			if (WNDomain.sameDomainJaccard(wordListEntity1, wordListEntity2, minJaccard)) {
				
				//Need to avoid adding Gtin14Number - Number to the alignment because this is erroneous...
				if (!c.getObject1AsURI().getFragment().equals("Gtin14Number") && !c.getObject2AsURI().getFragment().equals("Number")) {
					
				
				filteredAlignment.addAlignCell(c.getObject1(), c.getObject2(), c.getRelation().getRelation(), c.getStrength());
			
				}
			} 
			
			
			else {

				if (!WordNet.containedInWordNet(c.getObject1AsURI().getFragment().toLowerCase()) || !WordNet.containedInWordNet(c.getObject2AsURI().getFragment().toLowerCase())) {
					counter+=1;
				} else {
				} 

			}
		}	


		//return a new alignment
		return filteredAlignment;

	}

	public static void main(String[] args) throws AlignmentException, JWNLException, IOException {

		File alignmentFile = new File("./files/wndomainsexperiment/alignments/AML_bibframe-schemaorg.rdf");
		AlignmentParser parser = new AlignmentParser();
		BasicAlignment originalAlignment = (BasicAlignment) parser.parse(alignmentFile.toURI().toString());
		System.out.println("Without filtering the alignment contains " + originalAlignment.nbCells() + " cells");

		BasicAlignment filteredAlignment = filterAlignment(originalAlignment);

		System.out.println("After filtering the alignment contains " + filteredAlignment.nbCells() + " cells");


		File refAlignmentFile = new File("./files/wndomainsexperiment/alignments/refalign_bibframe-schemaorg-ISub08.rdf");
		AlignmentParser evalAlignParser = new AlignmentParser(1);
		Alignment referenceAlignment = evalAlignParser.parse(refAlignmentFile.toURI().toString());

		Properties p = new Properties();
		PRecEvaluator eval = new PRecEvaluator(referenceAlignment, filteredAlignment);

		eval.eval(p);

		System.out.println("------------------------------");
		System.out.println("Evaluator scores for " + filteredAlignment.getOntology1URI() + " 2 " + filteredAlignment.getOntology2URI());
		System.out.println("------------------------------");
		System.out.println("F-measure: " + eval.getResults().getProperty("fmeasure").toString());
		System.out.println("Precision: " + eval.getResults().getProperty("precision").toString());
		System.out.println("Recall: " + eval.getResults().getProperty("recall").toString());

		for (Cell c : filteredAlignment) {
			System.out.println(c.getObject1AsURI().getFragment() + " - " + c.getObject2AsURI().getFragment());
		}

		//store the computed alignment to file
		String alignmentFileName = "./files//wndomainsexperiment/alignments/operations/bibframe-schemaorg/AML_Ops1-2.rdf";
		File outputAlignment = new File(alignmentFileName);

		PrintWriter writer = new PrintWriter(
				new BufferedWriter(
						new FileWriter(outputAlignment)), true); 
		AlignmentVisitor renderer = new RDFRendererVisitor(writer);

		filteredAlignment.render(renderer);

		writer.flush();
		writer.close();
	}
}

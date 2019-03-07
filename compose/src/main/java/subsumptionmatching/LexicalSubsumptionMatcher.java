package subsumptionmatching;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.AlignmentVisitor;

import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.ontowrap.OntowrapException;
import net.didion.jwnl.JWNLException;
import rita.RiWordNet;
import utilities.LexicalConcept;
import utilities.SimpleRelation;
import utilities.StringUtilities;
import utilities.WordNet;

public class LexicalSubsumptionMatcher extends ObjectAlignment implements AlignmentProcess {

	static RiWordNet database = new RiWordNet("/Users/audunvennesland/Documents/PhD/Development/WordNet/WordNet-3.0/dict");
	Map<String, Double> matchingMap = new HashMap<String, Double>();
	double weight;

	public LexicalSubsumptionMatcher(double weight) {
		this.weight = weight;
	}

	//test method
	public static void main(String[] args) throws AlignmentException, IOException {
		File ontoFile1 = new File("./files/_PHD_EVALUATION/ATMONTO-AIRM/ONTOLOGIES/ATMOntoCoreMerged.owl");
		File ontoFile2 = new File("./files/_PHD_EVALUATION/ATMONTO-AIRM/ONTOLOGIES/airm-mono.owl");

		double testWeight = 1.0;

		AlignmentProcess a = new LexicalSubsumptionMatcher(testWeight);
		a.init(ontoFile1.toURI(), ontoFile2.toURI());
		Properties params = new Properties();
		params.setProperty("", "");
		a.align((Alignment)null, params);	
		AlignmentVisitor renderer = null;
		BasicAlignment evaluatedAlignment = null;
		PrintWriter writer = null;

		String alignmentFileName = "./files/_PHD_EVALUATION/MATCHERTESTING/LexicalSubsumptionMatcher.rdf";

		File outputAlignment = new File(alignmentFileName);


		writer = new PrintWriter(
				new BufferedWriter(
						new FileWriter(outputAlignment)), true); 
		renderer = new RDFRendererVisitor(writer);

		evaluatedAlignment = (BasicAlignment)(a.clone());

		evaluatedAlignment.normalise();

		evaluatedAlignment.render(renderer);
		writer.flush();
		writer.close();

	}

	/**
	 * The align() method is imported from the Alignment API and is modified to use the wordNetMatch method declared in this class
	 */
	public void align( Alignment alignment, Properties param ) throws AlignmentException {

		System.out.println("\nStarting Lexical Subsumption Matcher...");

		int numConceptsOnto1 = 0;
		int numConceptsOnto2 = 0;

		LexicalConcept lc = new LexicalConcept();

		Set<String> hyponyms = new HashSet<String>();
		Set<String> glossTokens = new HashSet<String>();
		
		Map<String, LexicalConcept> onto1LexicalMap = new HashMap<String, LexicalConcept>();
		Map<String, LexicalConcept> onto2LexicalMap = new HashMap<String, LexicalConcept>();

		String lexicalName = null;

		System.out.println("Retrieving lexical data for " + ontology1().getURI());
		long startTime1 = System.currentTimeMillis();

		try {
			for (Object source : ontology1().getClasses()) {
				lexicalName = WordNet.getLexicalName(ontology1().getEntityName(source)).toLowerCase();
				if (WordNet.containedInWordNet(lexicalName)) {
					hyponyms = WordNet.getAllHyponymsAsSet(lexicalName);
					glossTokens = StringUtilities.tokenizeToSet(WordNet.getGloss(lexicalName), true);
					lc = new LexicalConcept(lexicalName.replace(" ", ""), ontology1().getEntityURI(source), hyponyms, glossTokens);
					onto1LexicalMap.put(lexicalName.replace(" ", ""), lc);
				}
			}
		} catch (OntowrapException | IOException | JWNLException e3) {
			e3.printStackTrace();
		}
		long endTime1 = System.currentTimeMillis();

		System.out.println("The retrieval of lexical data for " + ontology1().getURI() + " took " + (endTime1 - startTime1) / 1000 + " seconds.");

		System.out.println("\nRetrieving lexical data for " + ontology2().getURI());
		long startTime2 = System.currentTimeMillis();
		try {
			for (Object target : ontology2().getClasses()) {
				lexicalName = WordNet.getLexicalName(ontology2().getEntityName(target)).toLowerCase();
				if (WordNet.containedInWordNet(lexicalName)) {
					hyponyms = WordNet.getAllHyponymsAsSet(lexicalName);
					glossTokens = StringUtilities.tokenizeToSet(WordNet.getGloss(lexicalName), true);
					lc = new LexicalConcept(lexicalName.replace(" ", ""), ontology2().getEntityURI(target), hyponyms, glossTokens);
					onto2LexicalMap.put(lexicalName.replace(" ", ""), lc);
				}
			}
		} catch (OntowrapException | IOException | JWNLException e3) {
			e3.printStackTrace();
		}

		long endTime2 = System.currentTimeMillis();

		System.out.println("The retrieval of lexical data for " + ontology2().getURI() + " took " + (endTime2 - startTime2) / 1000 + " seconds.");
		
		System.out.println("Starting matching process...");
		
		long startTimeMatchingProcess = System.currentTimeMillis();

		try {
			numConceptsOnto1 = ontology1().nbClasses();
			System.out.println("Ontology 1 contains " + numConceptsOnto1 + " classes");
		} catch (OntowrapException e2) {
			e2.printStackTrace();
		}

		try {
			numConceptsOnto2 = ontology2().nbClasses();
			System.out.println("Ontology 2 contains " + numConceptsOnto2 + " classes");
		} catch (OntowrapException e1) {
			e1.printStackTrace();
		}



		//if neither of the concepts are in WordNet -> give the relation a score of 0
		//if both concepts are in WordNet, we extract their hyponyms and their gloss
		//if the full source is a part of the set of hyponyms of target AND a part of the WordNet gloss of target --> source > target and score 1.0
		//if the compound head of source is a part of the set of hyponyms of target and the full source OR the compound head is a part of the WordNet gloss of target --> source > target and score 0.75
		//if the full source is a part of the set of hyponyms of target: source > target and a score of 0.5
		//if the compound head of source is a part of the set of hyponyms of target: source > target and score 0.25

		String sourceEntity = null;
		String targetEntity = null;
		//required to have their representation without lowercase for the compound analysis
		String sourceEntityNormalCase = null;
		String targetEntityNormalCase = null;
		
		Set<String> hyponymsSource = new HashSet<String>();
		Set<String> hyponymsTarget = new HashSet<String>();
		Set<String> glossSource = new HashSet<String>();
		Set<String> glossTarget = new HashSet<String>();

		try {

			for ( Object source: ontology1().getClasses() ){
				for ( Object target: ontology2().getClasses() ){
					
					//get the entity names for source and target to make the code more readable
					sourceEntity = ontology1().getEntityName(source).toLowerCase();
					targetEntity = ontology2().getEntityName(target).toLowerCase();
					sourceEntityNormalCase = ontology1().getEntityName(source);
					targetEntityNormalCase = ontology2().getEntityName(target);
					

					if (sourceEntity.equals(targetEntity)) {
						addAlignCell(source, target, "=", 0);
						System.out.println(source + " and " + target + " are the same");
					}
					
					//if source nor target is a lexicalconcept == they are not in wordnet, give the relation between them score 0
					else if (!onto1LexicalMap.containsKey(sourceEntity) || !onto2LexicalMap.containsKey(targetEntity)) {
						addAlignCell(source, target, "=", 0);
						System.out.println(sourceEntity + " and " + targetEntity + " are not in WordNet");

					} 
					
					//if both concepts are in WordNet, we compare their hyponyms and their gloss
					else if (onto1LexicalMap.containsKey(sourceEntity) && onto2LexicalMap.containsKey(targetEntity)) {
						//get the hyponyms of source and target entities
						hyponymsSource = onto1LexicalMap.get(sourceEntity).getHyponyms();
						hyponymsTarget = onto2LexicalMap.get(targetEntity).getHyponyms();
						//get the glosses of source and target entities
						glossSource = onto1LexicalMap.get(sourceEntity).getGlossTokens();
						glossTarget = onto2LexicalMap.get(targetEntity).getGlossTokens();
					}
										
					//if either hyponym set is empty -> score is 0
					if ((hyponymsSource == null || hyponymsSource.isEmpty()) || (hyponymsTarget == null || hyponymsTarget.isEmpty())) {
						addAlignCell(source, target, "=", 0);
						System.out.println("There are no hyponyms for EITHER " + source + " or " + target);
					}

					else {
						//if the full source is a part of the set of hyponyms of target AND a part of the WordNet gloss of target: source > target and score 1.0
						if (hyponymsTarget.contains(sourceEntity) && glossTarget.contains(sourceEntity)) {
							addAlignCell(source, target, "&lt;", 1.0);
						}
						//if the compound head of source is a part of the set of hyponyms of target AND the full source OR the compound head of source is a part of the WordNet gloss of target: source > target and score 0.75
						else if (StringUtilities.isCompoundWord(sourceEntityNormalCase) 
								&& hyponymsTarget.contains(StringUtilities.getCompoundHead(sourceEntityNormalCase))
								&& glossTarget.contains(sourceEntity)
								|| glossTarget.contains(StringUtilities.getCompoundHead(sourceEntityNormalCase).toLowerCase())) {
							addAlignCell(source, target, "&lt;", 0.75);
							System.out.println("The compound head of " + source + " is included in the hyponym list of " + target + ", and " +  source + " is in the gloss of " + target + " OR the compound head of " +
							source + " is in the gloss of " + target + " -> 0.75");
						}
						
						//if the full source is a part of the set of hyponyms of target: source > target and a score of 0.5
						else if(hyponymsTarget.contains(sourceEntity)) {
							addAlignCell(source, target, "&lt;", 0.5);
							System.out.println(source + " is included in the hyponym list of " + target + " -> 0.5");
						}
						
						//if the compound head of source is a part of the set of hyponyms of target: source > target and score 0.25
						else if (StringUtilities.isCompoundWord(sourceEntityNormalCase) && hyponymsTarget.contains(StringUtilities.getCompoundHead(sourceEntityNormalCase))) {
							addAlignCell(source, target, "&lt;", 0.25);
							System.out.println("The compound head of " + source + " is included in the hyponym list of " + target);
						}
						
						else if (hyponymsSource.contains(targetEntity) && glossSource.contains(targetEntity)) {
							addAlignCell(source, target, "&gt;", 1.0);
							System.out.println(target + " is included in the hyponym set of " + source + " AND in the gloss of " + target);
						}
						
						else if (StringUtilities.isCompoundWord(targetEntityNormalCase) 
								&& hyponymsSource.contains(StringUtilities.getCompoundHead(targetEntityNormalCase))
								&& glossSource.contains(targetEntity) 
								|| glossSource.contains(StringUtilities.getCompoundHead(targetEntityNormalCase).toLowerCase())) {
							addAlignCell(source, target, "&gt;", 0.75);
							System.out.println("The compound head of " + target + " is included in the hyponym set of " + source + " AND EITHER the compound head or the full concept of " + target + " is in the gloss of " + source);
						}
						
						else if (hyponymsSource.contains(targetEntity)) {
							addAlignCell(source, target, "&gt;", 0.5);
							System.out.println(target + " is included in the hyponym set of " + source);
						}
						
						else if (StringUtilities.isCompoundWord(targetEntityNormalCase)
								&& hyponymsSource.contains(StringUtilities.getCompoundHead(targetEntityNormalCase).toLowerCase())) {
							addAlignCell(source, target, "&gt;", 0.25);
							System.out.println("The compound head of " + target + " is included in the hyponym set of " + source);
						}
						
						else {
							addAlignCell(source, target, "=", 0);
							System.out.println("None of the rules apply for " + source + " and " + target);
						}
					}
				}

			}

		} catch (Exception e) { e.printStackTrace(); }

		long endTimeMatchingProcess = System.currentTimeMillis();

		System.out.println("The matching operation took " + (endTimeMatchingProcess - startTimeMatchingProcess) / 1000 + " seconds.");
	}

}



package subsumptionmatching;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
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
import utilities.Relation;
import utilities.SimpleRelation;
import utilities.StringUtilities;
import utilities.WordNet;

public class LexicalSubsumptionMatcher_oldest extends ObjectAlignment implements AlignmentProcess {

	static RiWordNet database = new RiWordNet("/Users/audunvennesland/Documents/PhD/Development/WordNet/WordNet-3.0/dict");
	Map<String, Double> matchingMap = new HashMap<String, Double>();
	double weight;

	public LexicalSubsumptionMatcher_oldest(double weight) {
		this.weight = weight;
	}
	
	//test method
	public static void main(String[] args) throws AlignmentException, IOException {
		File ontoFile1 = new File("./files/SATest1.owl");
		File ontoFile2 = new File("./files/SATest2.owl");
		
		double testWeight = 1.0;
		
		AlignmentProcess a = new LexicalSubsumptionMatcher_oldest(testWeight);
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
		long startTime = System.currentTimeMillis();

		try {
			// Match classes
			for ( Object cl2: ontology2().getClasses() ){
				for ( Object cl1: ontology1().getClasses() ){
					matchingMap = wordNetSubsumptionMatch(cl1, cl2);

					for (Map.Entry<String, Double> entry : matchingMap.entrySet()) {

						// add mapping into alignment object 
						addAlignCell(cl1,cl2, entry.getKey(), weight*entry.getValue());  
					}

				}
			}

		} catch (Exception e) { e.printStackTrace(); }
		
		long endTime = System.currentTimeMillis();
		System.out.println("Lexical Subsumption Matcher completed in " + (endTime - startTime) / 1000 + " seconds.");
	}

	//if the full source is a part of the set of hyponyms of target AND a part of the WordNet gloss of target --> source > target and score 1.0
	//if the compound head of source is a part of the set of hyponyms of target and the full source OR the compound head is a part of the WordNet gloss of target --> source > target and score 0.75
	//if the full source is a part of the set of hyponyms of target: source > target and a score of 0.5
	//if the compound head of source is a part of the set of hyponyms of target: source > target and score 0.25
	public Map<String, Double> wordNetSubsumptionMatch (Object o1, Object o2) throws AlignmentException, OntowrapException, FileNotFoundException, JWNLException, IOException {

		double finalDistance = 0;
		String relation = null;
		Map<String, Double> matchingMap = new HashMap<String, Double>();
		ArrayList<SimpleRelation> relationsList = new ArrayList<SimpleRelation>();
		SimpleRelation rel = null;

		String source = StringUtilities.stringTokenize(ontology1().getEntityName(o1), true).toLowerCase();
		String target = StringUtilities.stringTokenize(ontology2().getEntityName(o2), true).toLowerCase();
		
//		System.out.println("\nSource is: " + source + ", Target is: " + target);

		//if the concept names are equal we consider them as equivalent and give the relation a score of 0
		if (source.equals(target)) {
			rel = new SimpleRelation("=", 0.0);
			relationsList.add(rel);
			matchingMap.put("=", 0.0);
			
//			System.out.println(source + " equals " + target);
		
			//if neither of the concepts are in WordNet
		} else if (!WordNet.containedInWordNet(source) && !WordNet.containedInWordNet(target)) {			
			relation = "&lt;";
			finalDistance = 0.0;
			matchingMap.put(relation, finalDistance);
			
			rel = new SimpleRelation("&lt;", 0.0);
			relationsList.add(rel);
			
//			System.out.println(source + " and " + target + " is not in WordNet");
			
			//just to create a relation for all concept combinations when calculating the Harmony value
		} else if (!WordNet.containedInWordNet(source) || !WordNet.containedInWordNet(target)) {
			relation = "&lt;";
			finalDistance = 0.0;
			matchingMap.put(relation, finalDistance);
			
			rel = new SimpleRelation("&lt;", 0.0);
			relationsList.add(rel);
			
//			System.out.println("EITHER " + source + " or " + target + " is not in WordNet");

			//if both concepts are in WordNet, we extract their hyponyms and their gloss
		} else if (WordNet.containedInWordNet(source) && WordNet.containedInWordNet(target)) {
			
			Set<String> source_hyponyms = WordNet.getAllHyponymsAsSet(source);
			Set<String> target_hyponyms = WordNet.getAllHyponymsAsSet(target);
			
//			System.out.println("source_hyponyms contains " + source_hyponyms.size() + " items, " + " and target_hyponyms contains " + target_hyponyms.size() + "items");
			
			if (source_hyponyms.isEmpty() || target_hyponyms.isEmpty()) {
				relation = "&lt;";
				finalDistance = 0.0;
				matchingMap.put(relation, finalDistance);
				
				rel = new SimpleRelation("&lt;", 0.0);
				relationsList.add(rel);
				
//				System.out.println("There are no hyponyms for EITHER " + source + " or " + target);
			} else {
			//if the full source is a part of the set of hyponyms of target: source > target and score 1.0
					if (target_hyponyms.contains(source)) {

						relation = "&lt;";
						finalDistance = 1.0;
						matchingMap.put(relation, finalDistance);
						
						rel = new SimpleRelation("&lt;", 1.0);
						relationsList.add(rel);
						
//						System.out.println(source + " is included in the hyponym list of " + target + " and in the gloss of " + target + " -> 1.0");

						//if the compound head of source is a part of the set of hyponyms of target: source > target and score 0.75
					} else if (StringUtilities.isCompoundWord(source) 
							&& target_hyponyms.contains(StringUtilities.getCompoundHead(source).toLowerCase())) {
						relation = "&lt;";
						finalDistance = 0.75;
						matchingMap.put(relation, finalDistance);
						
						rel = new SimpleRelation("&lt;", 0.75);
						relationsList.add(rel);
						
//						System.out.println("The compound head of " + source + " is included in the hyponym list of " + target + ", and " +  source + " is in the gloss of " + target + " OR the compound head of " +
//						source + " is in the gloss of " + target + " -> 0.75");

					} 
					
					else if (source_hyponyms.contains(target)) {
						relation = "&gt;";
						finalDistance = 1.0;
						matchingMap.put(relation, finalDistance);
						
						rel = new SimpleRelation("&gt;", 1.0);
						relationsList.add(rel);
						
//						System.out.println(target + " is included in the hyponym set of " + source + " AND in the gloss of " + target);

						//if the compound head of target is a part of the set of hyponyms of source and the full target OR the compound head of target is a part of the WordNet gloss of source: target < source and score 0.75
					} else if (StringUtilities.isCompoundWord(target) 
							&& source_hyponyms.contains(StringUtilities.getCompoundHead(target).toLowerCase())) {
						relation = "&gt;";
						finalDistance = 0.75;
						matchingMap.put(relation, finalDistance);
						
						rel = new SimpleRelation("&gt;", 0.75);
						relationsList.add(rel);
						
//						System.out.println("The compound head of " + target + " is included in the hyponym set of " + source + " AND EITHER the compound head or the full concept of " + target + " is in the gloss of " + source);

					} else {
						relation = "&gt;";
						finalDistance = 0.0;
						matchingMap.put(relation, finalDistance);
						
						rel = new SimpleRelation("&gt;", 0.0);
						relationsList.add(rel);
						
//						System.out.println("None of the rules apply for " + source + " and " + target + " - 2");
					}
				}
				
		}
		
//		System.out.println("\nThe ArrayList with simple relations contains " + relationsList.size() + " relations");
//		for (SimpleRelation simpleR : relationsList) {
//			System.out.println(simpleR.getRelation() + " : " + simpleR.getConfidence());
//		}
//		System.out.println("matchingMap contains " + matchingMap.size() + " relations");
		return matchingMap;
		
	}

}


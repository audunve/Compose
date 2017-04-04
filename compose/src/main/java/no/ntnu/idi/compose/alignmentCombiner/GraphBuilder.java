package no.ntnu.idi.compose.alignmentCombiner;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Evaluator;

import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.eval.PRecEvaluator;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import fr.inrialpes.exmo.ontosim.string.*;

/**
 * @author audunvennesland
 * 4. apr. 2017 
 */
public class GraphBuilder {
	
	GraphDatabaseService db;
	static Properties p = new Properties();
	File ref = new File("./files/alignmentCombiner/conference-ekaw/conference-ekaw.rdf");
	

	/**
	 * Constructor to initialise the graph database
	 * @param db a GraphDatabaseService
	 */
	public GraphBuilder(GraphDatabaseService db) {
		
		this.db = db;
		
	}
	
	public void buildGraph(BasicAlignment refAlignment, Set<BasicAlignment> mergedAlignments) throws AlignmentException {

		Iterator<BasicAlignment> alignmentItr = mergedAlignments.iterator();
		
		//the best alignment in the mergeAlignments set is chosen as the root node of the graph
		BasicAlignment bestAlignment = findBestAlignment(refAlignment, mergedAlignments);
		
		try (Transaction tx = db.beginTx())
		
		
		{
			
			
		}
		
		
		
		while (alignmentItr.hasNext()) {
			
			
			
			//compare all alignments not already in the graph with the alignments in the graph - using Edit distance
			
			//When all distances have been computed, add the pairs of merged alignments with the smallest distance
			//to the graph (only the first node ma1, since the node ma2 is already in the graph
			
			//create edge between the two nodes of the graph
			
		}
		
	}
	//StringDistances.levenshteinDistance("conference_document", "document");
	//The Edit distance is based on the number of different cells in the alignments, for example if alignment 1 has
	//{ O=O, CD=D, R=PR }, while alignment 2 has { O=O, CD=D }, the edit distance between them is 1. 
	public double computeEdit (Set<Alignment> inputAlignments) {
		Double editDistance = 0.0;
		
		Map<Double, Set<Alignment>> distanceMap = new HashMap<Double, Set<Alignment>>();
		
		
		
		
		
		return editDistance;
		
	}
	
	
	public static BasicAlignment findBestAlignment (BasicAlignment refAlignment, Set <BasicAlignment> mergedAlignments) throws AlignmentException {
		
		double fMeasure = 0;
		double currentFMeasure = 0;
		
		BasicAlignment bestAlignment = new URIAlignment();
		
		PRecEvaluator eval = null;
		
		for (BasicAlignment a : mergedAlignments) {
		eval = new PRecEvaluator(refAlignment, a);
		eval.eval(p);
		
		currentFMeasure = Double.parseDouble(eval.getResults().getProperty("fmeasure").toString());
		
		if (currentFMeasure > fMeasure) {
			bestAlignment = a;
			fMeasure = currentFMeasure;
		}
		}
		
		return bestAlignment;
	}
	
	/**
	 * Registers a shutdown hook for the Neo4j instance so that it shuts down nicely when the VM exits
	 * @param graphDb a GraphDatabaseService
	 */
	private static void registerShutdownHook(final GraphDatabaseService graphDb)
	{
		Runtime.getRuntime().addShutdownHook( new Thread()
		{
			@Override
			public void run()
			{
				graphDb.shutdown();
			}
		} );
	}
	
	private static enum RelTypes implements RelationshipType
	{
		EDIT
	}
	
	public static void main(String[] args) throws AlignmentException {
		
		File aml = new File("./files/alignmentCombiner/conference-ekaw/conference-ekaw-alignment-aml.rdf");
		File logmap = new File("./files/alignmentCombiner/conference-ekaw/conference-ekaw-alignment-logmap.rdf");
		File compose = new File("./files/alignmentCombiner/conference-ekaw/conference-ekaw-alignment-compose.rdf");
		
		File ref = new File("./files/alignmentCombiner/conference-ekaw/conference-ekaw.rdf");

		AlignmentParser parser = new AlignmentParser();
		BasicAlignment amlAl = (BasicAlignment) parser.parse(aml.toURI().toString());
		BasicAlignment logmapAl = (BasicAlignment) parser.parse(logmap.toURI().toString());
		BasicAlignment composeAl = (BasicAlignment) parser.parse(compose.toURI().toString());
		BasicAlignment refAlign = (BasicAlignment) parser.parse(ref.toURI().toString());

		Set<BasicAlignment> inputAlignments = new HashSet<BasicAlignment>();
		inputAlignments.add(amlAl);
		inputAlignments.add(logmapAl);
		inputAlignments.add(composeAl);
		
		BasicAlignment bestAlignment = findBestAlignment(refAlign, inputAlignments);
		for (Cell c : bestAlignment) {
			System.out.println (c.getObject1AsURI().getFragment() + " - " + c.getObject2AsURI().getFragment() + " - " + c.getRelation().toString() + " - " + c.getStrength());
		}
		
		
		
	}

}

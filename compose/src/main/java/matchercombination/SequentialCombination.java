package matchercombination;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

//import compose.statistics_delete.Subclasses_delete;
//import compose.statistics_delete.Superclasses_delete;
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.BasicConfidence;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.rel.A5AlgebraRelation;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import utilities.OntologyOperations;
import utilities.StringUtilities;


public class SequentialCombination {

	static double threshold;
	static File outputAlignment = null;

	/**
	 * Returns an alignment where correspondences that are identified both by the previous matcher and the current matcher are strengthened. 
	 * This combination strategy considers the order of the alignments (i.e. only the first matcher (get(0)) in the ArrayList
	 *  
	 * @param inputAlignments an ArrayList of all alignments to be combined
	 * @return an alignment with weighted correspondences
	 * @throws AlignmentException
	 */
	public static Alignment weightedSequentialCombination(ArrayList<Alignment> inputAlignments) throws AlignmentException {

		Alignment newAlignment = new URIAlignment();
		newAlignment.setType("WSC");

		//set the first alignment in the array list as "prioritised alignment" and remove it from the arraylist
		Alignment priAlignment = inputAlignments.get(0);
		
		
		//create a list of cells from the "prioritised alignment"
		ArrayList<Cell> priCellsList = new ArrayList<Cell>();
		for (Cell c : priAlignment) {
			priCellsList.add(c);
		}
		
		//create a list of cells from the other alignments
		ArrayList<Cell> allOtherCellsList = new ArrayList<Cell>();		
		for (Alignment a : inputAlignments) {
			for (Cell c : a) {
				allOtherCellsList.add(c);
			}
		}
		
		//map to hold number of occurrences of each cell from the prioritised alignment in the other alignments
		Map<Cell, Integer> cellCountMap = new HashMap<Cell, Integer>();

		for (Cell c1 : priCellsList) {
			int counter = 0;
			for (Cell c2: allOtherCellsList) {
				if (c2.equals(c1)) {
					counter+=1;
				} 
				
			}
			cellCountMap.put(c1, counter);
		}
		
		//TO-DO: Count the number of alignments in inputAlignments and use this as a basis for the weights. E.g. instead of if (e.getValue() == 0) use if (e.getValue() == (numAlignments - numAlignments) or a more clever computation.
		//E.g. if all alignments contain a relation, then the confidence is max (1.0), if 3/4 of the alignments contain the relation, then the confidence is 0.75, etc. 
		
		
		for (Entry<Cell, Integer> e : cellCountMap.entrySet()) {
			//if no other alignments have this cell -> reduce its confidence by 50 percent
			if (e.getValue() == (0)) {
				newAlignment.addAlignCell(e.getKey().getObject1(), e.getKey().getObject2(), StringUtilities.validateRelationType(e.getKey().getRelation().getRelation()), e.getKey().getStrength()-0.41);
				//if one other alignment have this cell
			} else if (e.getValue() == (1)) {
				newAlignment.addAlignCell(e.getKey().getObject1(), e.getKey().getObject2(), StringUtilities.validateRelationType(e.getKey().getRelation().getRelation()), e.getKey().getStrength()-0.2);
			//if two other alignments have this cell
			} else if (e.getValue() == (2)) {
				newAlignment.addAlignCell(e.getKey().getObject1(), e.getKey().getObject2(), StringUtilities.validateRelationType(e.getKey().getRelation().getRelation()), e.getKey().getStrength());
			//if all other alignments have this cell
			} else if (e.getValue() == (3)) {
				newAlignment.addAlignCell(e.getKey().getObject1(), e.getKey().getObject2(), StringUtilities.validateRelationType(e.getKey().getRelation().getRelation()), 1.0);
			}
		}
		
		
		//remove duplicates before returning the completed alignment
		((BasicAlignment) newAlignment).normalise();


		return newAlignment;		
	}
	
	

	/**
	 * Returns an alignment produced by processing the input alignments in sequence, comparing if alignments contain equal correspondences and keeping the highest strength (confidence)
	 * If there are new correspondences discovered while processing each alignment they are all maintained in the final alignment. 
	 * @param alignmentFile1
	 * @param alignmentFile2
	 * @param alignmentFile3
	 * @return
	 * @throws AlignmentException
	 */
	public static Alignment nonWeightedCompleteMatch(File alignmentFile1, File alignmentFile2, File alignmentFile3) throws AlignmentException {

		Alignment completeMatchAlignment = new URIAlignment();
		Alignment intermediateAlignment = new URIAlignment();

		//load the alignments
		AlignmentParser parser = new AlignmentParser();
		BasicAlignment a1 = (BasicAlignment)parser.parse(alignmentFile1.toURI().toString());
		BasicAlignment a2 = (BasicAlignment)parser.parse(alignmentFile2.toURI().toString());
		BasicAlignment a3 = (BasicAlignment)parser.parse(alignmentFile3.toURI().toString());

		//compare correspondences (cells) in a1 and a2. 
		for (Cell cell1 : a1) {
			for (Cell cell2 : a2) {				
				//if the cells are equal (contains similar entities)
				if (cell2.getObject1().equals(cell1.getObject1()) && cell2.getObject2().equals(cell1.getObject2())) {
					//if the strength in the previous alignment is higher, this cell is added 
					if (cell1.getStrength() >= cell2.getStrength()) {
						intermediateAlignment.addAlignCell(cell1.getObject1(), cell1.getObject2(), StringUtilities.validateRelationType(cell1.getRelation().getRelation()), cell1.getStrength());
						//if the current cells strength is higher, this cells strength is retained
					} else if (cell2.getStrength() >= cell1.getStrength()) {
						intermediateAlignment.addAlignCell(cell2.getObject1(), cell2.getObject2(), StringUtilities.validateRelationType(cell2.getRelation().getRelation()), cell2.getStrength());
					}
					//if the cells are not equal, we add the cells from both the previous alignment and the current alignment.
				} else {
					intermediateAlignment.addAlignCell(cell1.getObject1(), cell1.getObject2(), StringUtilities.validateRelationType(cell1.getRelation().getRelation()), cell1.getStrength());
					intermediateAlignment.addAlignCell(cell2.getObject1(), cell2.getObject2(), StringUtilities.validateRelationType(cell2.getRelation().getRelation()), cell2.getStrength());
					continue;
				}
			}
		}

		//compare correspondences (cells) in the current alignment.  
		for (Cell cell2 : intermediateAlignment) {
			for (Cell cell3 : a3) {
				//if the cells are equal (contains similar entities)
				if (cell3.getObject1().equals(cell2.getObject1()) && cell3.getObject2().equals(cell2.getObject2())) {
					//if the strength in the previous alignment is higher, this cell is added
					if (cell2.getStrength() >= cell3.getStrength()) {
						completeMatchAlignment.addAlignCell(cell2.getObject1(), cell2.getObject2(), StringUtilities.validateRelationType(cell2.getRelation().getRelation()), cell2.getStrength());
						//if the current cells strength is higher, this is retained
					} else if (cell3.getStrength() >= cell2.getStrength()) {
						completeMatchAlignment.addAlignCell(cell3.getObject1(), cell3.getObject2(), StringUtilities.validateRelationType(cell3.getRelation().getRelation()), cell3.getStrength());
					}
					//if the cells are not equal, we add the cells from both the previous alignment and the current alignment.
				} else {
					completeMatchAlignment.addAlignCell(cell2.getObject1(), cell2.getObject2(), StringUtilities.validateRelationType(cell2.getRelation().getRelation()), cell2.getStrength());
					completeMatchAlignment.addAlignCell(cell3.getObject1(), cell3.getObject2(), StringUtilities.validateRelationType(cell3.getRelation().getRelation()), cell3.getStrength());
					continue;
				}
			}
		}

		//TO-DO: should remove duplicates before returning the completed alignment
		return completeMatchAlignment;		
	}
	
	/**
	 * This method takes an equivalence Alignment as input (produced by the WEMatcher) and identifies subsumption relations using subclasses of the equivalent classes.
	 * So the subclasses of entity 1 in the equivalence relation are subsumed by entity 2 and vice versa
	 * @param inputAlignmentFile
	 * @return
	 * @throws AlignmentException
	 * @throws OWLOntologyCreationException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static BasicAlignment wordEmbeddingSubsumptionSubclasses (BasicAlignment inputAlignment) throws AlignmentException, OWLOntologyCreationException, IOException, URISyntaxException {

		BasicAlignment subsumptionAlignment = new URIAlignment();

//
//		AlignmentParser parser = new AlignmentParser();
//		BasicAlignment inputAlignment = (BasicAlignment) parser.parse(inputAlignmentFile.toURI().toString());

		URI onto1URI = inputAlignment.getOntology1URI();
		URI onto2URI = inputAlignment.getOntology2URI();

		//need to initialise the alignment with ontology URIs and the type of relation (e.g. A5AlgebraRelation) otherwise exceptions are thrown
		subsumptionAlignment.init( onto1URI, onto2URI, A5AlgebraRelation.class, BasicConfidence.class );

		File ontoFile1 = new File(inputAlignment.getFile1().getRawPath());
		File ontoFile2 = new File(inputAlignment.getFile2().getRawPath());

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

		//get the ontologies from the alignment file
		OWLOntology onto1 = manager.loadOntologyFromOntologyDocument(ontoFile1);
		OWLOntology onto2 = manager.loadOntologyFromOntologyDocument(ontoFile2);

		Map<String, Set<String>> onto1ClassesAndSubclasses = OntologyOperations.getSubclasses(onto1);
		Map<String, Set<String>> onto2ClassesAndSubclasses = OntologyOperations.getSubclasses(onto2);


		//for each cell in the alignment
		//get all subclasses of e1 and make them subsumed by e2
		//then get all subclasses of e2 and make the subsumed by e1
		Set<String> subclasses = null;
		for (Cell c : inputAlignment) {

			if (onto1ClassesAndSubclasses.containsKey(c.getObject1().toString())) {

				subclasses = onto1ClassesAndSubclasses.get(c.getObject1().toString());

				for (String sc : subclasses) {
					subsumptionAlignment.addAlignCell(new URI(sc), c.getObject2AsURI(), "<", 1.0);
				}
			} if (onto2ClassesAndSubclasses.containsKey(c.getObject2().toString())) {

				subclasses = onto2ClassesAndSubclasses.get(c.getObject2().toString());

				for (String sc : subclasses) {
					subsumptionAlignment.addAlignCell(c.getObject1AsURI(), new URI(sc), ">", 1.0);
				}

			}
		}


		return subsumptionAlignment;


	}

	/**
	 * This method takes an equivalence Alignment as input (produced by the WEMatcher) and identifies subsumption relations using superclasses of the equivalent classes.
	 * So the superclasses of entity 1 in the equivalence relation subsumes entity 2 and vice versa
	 * @param inputAlignmentFile
	 * @return
	 * @throws AlignmentException
	 * @throws OWLOntologyCreationException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static BasicAlignment wordEmbeddingSubsumptionSuperclasses (BasicAlignment inputAlignment) throws AlignmentException, OWLOntologyCreationException, IOException, URISyntaxException {

		BasicAlignment subsumptionAlignment = new URIAlignment();


		//AlignmentParser parser = new AlignmentParser();
		//BasicAlignment inputAlignment = (BasicAlignment) parser.parse(inputAlignmentFile.toURI().toString());

		URI onto1URI = inputAlignment.getOntology1URI();
		URI onto2URI = inputAlignment.getOntology2URI();

		//need to initialise the alignment with ontology URIs and the type of relation (e.g. A5AlgebraRelation) otherwise exceptions are thrown
		subsumptionAlignment.init( onto1URI, onto2URI, A5AlgebraRelation.class, BasicConfidence.class );

		File ontoFile1 = new File(inputAlignment.getFile1().getRawPath());
		File ontoFile2 = new File(inputAlignment.getFile2().getRawPath());

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

		//get the ontologies from the alignment file
		OWLOntology onto1 = manager.loadOntologyFromOntologyDocument(ontoFile1);
		OWLOntology onto2 = manager.loadOntologyFromOntologyDocument(ontoFile2);

		Map<String, Set<String>> onto1ClassesAndSuperclasses = OntologyOperations.getSuperclasses(onto1);
		Map<String, Set<String>> onto2ClassesAndSuperclasses = OntologyOperations.getSuperclasses(onto2);


		Set<String> superclasses = null;

		for (Cell c : inputAlignment) {

			//if ontology 1 contains Cell.object1
			if (onto1ClassesAndSuperclasses.containsKey(c.getObject1().toString())) {

				superclasses = onto1ClassesAndSuperclasses.get(c.getObject1().toString());

				for (String sc : superclasses) {
					subsumptionAlignment.addAlignCell(new URI(sc), c.getObject2AsURI(), ">", 1.0);

				}

				//if ontology 2 contains Cell.object21
			}  if (onto2ClassesAndSuperclasses.containsKey(c.getObject1().toString())) {
				//System.err.println("onto2ClassesAndSuperclasses contain " + c.getObject1().toString() + " and onto1ClassesAndSuperclasses contain " + c.getObject1().toString());

				superclasses = onto2ClassesAndSuperclasses.get(c.getObject1().toString());

				for (String sc : superclasses) {
					subsumptionAlignment.addAlignCell(new URI(sc), c.getObject2AsURI(), ">", 1.0);
				}
				//if ontology 1 contains Cell.object 2
			}  if (onto1ClassesAndSuperclasses.containsKey(c.getObject2().toString())) {

				superclasses = onto1ClassesAndSuperclasses.get(c.getObject2().toString());

				for (String sc : superclasses) {
					subsumptionAlignment.addAlignCell(c.getObject1AsURI(), new URI(sc), "<", 1.0);
				}
			}
			//if ontology 2 contains Cell.object 2
			if (onto2ClassesAndSuperclasses.containsKey(c.getObject2().toString())) {

				superclasses = onto2ClassesAndSuperclasses.get(c.getObject2().toString());

				for (String sc : superclasses) {
					subsumptionAlignment.addAlignCell(c.getObject1AsURI(), new URI(sc), "<", 1.0);
				}

			}
		}


		return subsumptionAlignment;

	}

	public static BasicAlignment wordEmbeddingEquivalenceSuperclasses (BasicAlignment inputAlignment) throws AlignmentException, OWLOntologyCreationException, IOException, URISyntaxException {

		BasicAlignment equivalenceAlignment = new URIAlignment();


		//AlignmentParser parser = new AlignmentParser();
		//BasicAlignment inputAlignment = (BasicAlignment) parser.parse(inputAlignmentFile.toURI().toString());

		URI onto1URI = inputAlignment.getOntology1URI();
		URI onto2URI = inputAlignment.getOntology2URI();

		//need to initialise the alignment with ontology URIs and the type of relation (e.g. A5AlgebraRelation) otherwise exceptions are thrown
		equivalenceAlignment.init( onto1URI, onto2URI, A5AlgebraRelation.class, BasicConfidence.class );

		File ontoFile1 = new File(inputAlignment.getFile1().getRawPath());
		File ontoFile2 = new File(inputAlignment.getFile2().getRawPath());

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

		//get the ontologies from the alignment file
		OWLOntology onto1 = manager.loadOntologyFromOntologyDocument(ontoFile1);
		OWLOntology onto2 = manager.loadOntologyFromOntologyDocument(ontoFile2);

		//create a map holding the set of superclasses (value) for each class (key)
		Map<String, Set<String>> onto1ClassesAndSuperclasses = OntologyOperations.getSuperclasses(onto1);
		Map<String, Set<String>> onto2ClassesAndSuperclasses = OntologyOperations.getSuperclasses(onto2);

		//set of superclasses for each class in equivalence relation (will always be 1)
		Set<String> super1 = null;
		Set<String> super2 = null;

		//the superclass contained in the set of superclasses (will be transformed to URI within the coming for loop)
		String superCls1 = null;
		String superCls2 = null;


		for (Cell c : inputAlignment) {

			if (onto1ClassesAndSuperclasses.containsKey(c.getObject1().toString()) && 
					onto2ClassesAndSuperclasses.containsKey(c.getObject2().toString())) {

				//get the sets of superclasses for each class in the equivalence relation
				super1 = onto1ClassesAndSuperclasses.get(c.getObject1().toString());
				super2 = onto2ClassesAndSuperclasses.get(c.getObject2().toString());

				for (String s1 : super1) {
					superCls1 = s1;
				}

				for (String s2 : super2) {
					superCls2 = s2;
				}

				//the objects in the equivalence relation have to be URIs
				URI us1 = new URI(superCls1);
				URI us2 = new URI(superCls2);

				equivalenceAlignment.addAlignCell(us1, us2, "=", 1.0);
			}			
		}

		return equivalenceAlignment;


	}

	private static BasicAlignment mergeTwoAlignments (BasicAlignment a1, BasicAlignment a2) throws AlignmentException {

		BasicAlignment mergedAlignment = new BasicAlignment();

		mergedAlignment = (BasicAlignment) a1.clone();

		mergedAlignment.ingest(a2);

		return mergedAlignment;


	}


	/**
	 * Increases an input value by 12 percent
	 * @param inputStrength the strength/confidence for a correspondence to be increased
	 * @return a value 12 percent higher than its input value
	 */
	public static double increaseCellStrength(double inputStrength) {

		double newStrength = inputStrength + (inputStrength * 0.12);

		if (newStrength > 1.0) {
			newStrength = 1.0;
		}

		return newStrength;
	}

	/**
	 * Decreases an input value by 12 percent
	 * @param inputStrength the strength/confidence for a correspondence to be decreased
	 * @return a value 12 percent lower than its input value
	 */
	public static double reduceCellStrength(double inputStrength) {

		double newStrength = inputStrength - (inputStrength * 0.12);

		if (newStrength > 1.0) {
			newStrength = 1.0;
		}

		return newStrength;
	}

	/*public static void main(String[] args) throws AlignmentException, IOException, URISyntaxException {

		String task = "103/101-103-";


		File af1 = new File("./files/OAEI2009/alignments/"+task+"OppositeSubclass0.9.rdf");
		File af2 = new File("./files/OAEI2009/alignments/"+task+"Compound0.9.rdf");
		File af3 = new File("./files/OAEI2009/alignments/"+task+"WNHyponym0.9.rdf");
		File af4 = new File("./files/OAEI2009/alignments/"+task+"Parent0.9.rdf");

		ArrayList<Alignment> inputAlignments = new ArrayList<Alignment>();

		AlignmentParser parser = new AlignmentParser();
		BasicAlignment a1 = (BasicAlignment) parser.parse(af1.toURI().toString());
		BasicAlignment a2 = (BasicAlignment) parser.parse(af2.toURI().toString());
		BasicAlignment a3 = (BasicAlignment) parser.parse(af3.toURI().toString());
		BasicAlignment a4 = (BasicAlignment) parser.parse(af4.toURI().toString());

		inputAlignments.add(a2);
		inputAlignments.add(a3);
		inputAlignments.add(a4);
		inputAlignments.add(a1);

		Alignment newAlignment = weightedSequentialComposition(inputAlignments);

		System.out.println("\n");
		for (Cell c : newAlignment) {
			System.out.println(c.getObject1AsURI().getFragment() + " - " + c.getObject2AsURI().getFragment() + " : " + c.getRelation().getRelation() + " : " + c.getStrength());
		}
		
		//Store the combined alignment
				File outputAlignment = new File("./files/OAEI2009/alignments/test.rdf");

				PrintWriter writer = new PrintWriter(
						new BufferedWriter(
								new FileWriter(outputAlignment)), true); 
				AlignmentVisitor renderer = new RDFRendererVisitor(writer);

				newAlignment.render(renderer);
				writer.flush();
				writer.close();

	}*/
}

package no.ntnu.idi.compose.alignmentCombiner;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.parser.AlignmentParser;

/**
 * @author audunvennesland
 * 26. mar. 2017 
 */
public class SubgraphStrategy {

	public static Set<Alignment> initStrategy (OWLOntology onto1, OWLOntology onto2, Set<Alignment> inputAlignments) throws AlignmentException {

		Set<Alignment> rankedAlignments = new HashSet<Alignment>();

		//merge similar cells and put them in an Alignment
		//similar approach as SimpleVote? Assuming so...
		BasicAlignment inputAlignment = (BasicAlignment) SimpleVoteAlgo.createVotedAlignment(inputAlignments);

		//put all cells in a set R
		Set<Cell> R = new HashSet<Cell>();
		for (Cell c : inputAlignment) {
			System.out.println(c.getObject1AsURI().getFragment() + " - " + c.getObject2AsURI().getFragment() + " - " + c.getRelation().toString() + " - " + c.getStrength());
			R.add(c);
		}

		return rankedAlignments;

	}

	public Set<Alignment> buildAlignments (OWLOntology onto1, OWLOntology onto2, BasicAlignment inputAlignment) {

		//
		Set<Alignment> mergedAlignments = new HashSet<Alignment>();




		return mergedAlignments;

	}

	/*public boolean isConsistent (OWLOntology o1, OWLOntology o2, Set<Alignment> alignmentSet) {

		//TO-DO: See if the repair facility from LogMap can be used
		//LogMap requires two OWLOntology objects and MappingObjectStr, see uk.ac.ox.krr.logmap2.LogMap2_RepairFacility 

	}*/

	public static void main(String[] args) throws AlignmentException, OWLOntologyCreationException {

		File af1 = new File("./files/conference-ekaw/conference-ekaw-alignment-aml.rdf");
		File af2 = new File("./files/conference-ekaw/conference-ekaw-alignment-logmap.rdf");
		File af3 = new File("./files/conference-ekaw/conference-ekaw-alignment-compose.rdf");

		AlignmentParser parser = new AlignmentParser();
		BasicAlignment a1 = (BasicAlignment) parser.parse(af1.toURI().toString());
		BasicAlignment a2 = (BasicAlignment) parser.parse(af2.toURI().toString());
		BasicAlignment a3 = (BasicAlignment) parser.parse(af3.toURI().toString());

		Set<Alignment> inputAlignments = new HashSet<Alignment>();
		inputAlignments.add(a1);
		inputAlignments.add(a2);
		inputAlignments.add(a3);

		//import the owl files
		File ontoFile1 = new File("./files/conference-ekaw/Conference.owl");
		File ontoFile2 = new File("./files/conference-ekaw/ekaw.owl");

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();		
		OWLOntology onto1 = manager.loadOntologyFromOntologyDocument(ontoFile1);
		OWLOntology onto2 = manager.loadOntologyFromOntologyDocument(ontoFile2);
		
		Set<Alignment> mergedAlignments = initStrategy(onto1, onto2, inputAlignments);

	}


}

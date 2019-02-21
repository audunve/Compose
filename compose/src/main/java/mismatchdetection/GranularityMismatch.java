package mismatchdetection;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;

import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import utilities.OntologyOperations;

public class GranularityMismatch {
	
	/** TODO: We need to defined criteria for determining the threshold for stating that two classes really are a granularity mismatch on the basis of their properties and subclasses.
	 * Detects "granularity mismatches" by comparing the number of data properties, object properties, and subclasses of two classes forming a relation in an input alignment.
	 * @param inputAlignmentFile an already computed alignment holding a set of relations
	 * @param onto1 the source ontology
	 * @param onto2 the target ontology
	 * @return the input alignment - detected granularity mismatches
	 * @throws AlignmentException
	   Nov 26, 2018
	 */
	public static Set<String> detectGranularityMismatches(File inputAlignmentFile, OWLOntology onto1, OWLOntology onto2) throws AlignmentException {
		Set<String> granularityMismatches = new HashSet<String>();
		AlignmentParser parser = new AlignmentParser();
		BasicAlignment inputAlignment = (BasicAlignment) parser.parse(inputAlignmentFile.toURI().toString());

		for (Cell c : inputAlignment) {
			System.out.println("\nTesting " + c.getObject1AsURI().getFragment() + " - " + c.getObject2AsURI().getFragment());
			Set<OWLObjectProperty> ops1 = OntologyOperations.getObjectProperties(onto1, OntologyOperations.getClass(c.getObject1AsURI().getFragment(), onto1));
			for (OWLObjectProperty op : ops1) {
				System.out.println(op);
			}
			
			Set<OWLObjectProperty> ops2 = OntologyOperations.getObjectProperties(onto2, OntologyOperations.getClass(c.getObject2AsURI().getFragment(), onto2));
			for (OWLObjectProperty op : ops2) {
				System.out.println(op);
			}
			
			Set<OWLDataProperty> dps1 = OntologyOperations.getDataProperties(onto1, OntologyOperations.getClass(c.getObject1AsURI().getFragment(), onto1));
			for (OWLDataProperty dp : dps1) {
				System.out.println(dp);
			}
			
			Set<OWLDataProperty> dps2 = OntologyOperations.getDataProperties(onto2, OntologyOperations.getClass(c.getObject2AsURI().getFragment(), onto2));
			for (OWLDataProperty dp : dps2) {
				System.out.println(dp);
			}
			
			Set<String> cls1 = OntologyOperations.getEntitySubclasses(onto1, OntologyOperations.getClass(c.getObject1AsURI().getFragment(), onto1));
			Set<String> cls2 = OntologyOperations.getEntitySubclasses(onto2, OntologyOperations.getClass(c.getObject2AsURI().getFragment(), onto2));
			
		
			System.out.println("There are " + ops1.size() + " object properties for " + c.getObject1AsURI().getFragment());
			System.out.println("There are " + ops2.size() + " object properties for " + c.getObject2AsURI().getFragment());
			
			System.out.println("There are " + dps1.size() + " data properties for " + c.getObject1AsURI().getFragment());
			System.out.println("There are " + dps2.size() + " data properties for " + c.getObject2AsURI().getFragment());
			
			System.out.println("There are " + cls1.size() + " subclasses for " + c.getObject1AsURI().getFragment());
			System.out.println("There are " + cls2.size() + " subclasses for " + c.getObject2AsURI().getFragment());
			
			/* Weighting properties and subclasses */
			//object properties are weighted 1.5
			double opWeight1 = ops1.size() * 1.5;
			double opWeight2 = ops2.size() * 1.5;
			
			//data properties are weighted 1.2
			double dpWeight1 = dps1.size() * 1.2;
			double dpWeight2 = dps2.size() * 1.2;
			
			//subclasses are weighted 1.5
			double subclsWeight1 = cls1.size() * 1.5;
			double subclsWeight2 = cls2.size() * 1.5;
			
			double sumWeight1 = opWeight1+dpWeight1+subclsWeight1;
			double sumWeight2 = opWeight2+dpWeight2+subclsWeight2;
			double diffWeight = Math.abs(sumWeight1-sumWeight2);
			double percWeight = diffWeight/(sumWeight1+sumWeight2)*100;
			
			if (percWeight > 25) {
				granularityMismatches.add(c.getObject1AsURI().getFragment() + " - " + c.getObject2AsURI().getFragment());
			}
			
			System.out.println("sumWeight1: " + sumWeight1 + " and sumWeight2: " + sumWeight2);
			
			/* Percentage difference */
//			int diffOps = Math.abs(ops1.size()-ops2.size());
//			int sumOps = ops1.size() + ops2.size();
//			System.out.println("The sum of ops is " + sumOps);
//			System.out.println("The diff of ops is " + diffOps);
//			double percentageOps = ((double)diffOps / (double)sumOps) * 100;
//			System.out.println("The percentage of ops is " + percentageOps);
//			
//			int diffDps = Math.abs(dps1.size()-dps2.size());
//			int sumDps = dps1.size() + dps2.size();
//			System.out.println("The sum of dps is " + sumDps);
//			System.out.println("The diff of dps is " + diffDps);
//			double percentageDps = ((double)diffDps / (double)sumDps) * 100;
//			System.out.println("The percentage of dps is " + percentageDps);
//			
//			int diffcls = Math.abs(cls1.size()-cls2.size());
//			int sumCls = cls1.size() + cls2.size();
//			System.out.println("The sum of cls is " + sumCls);
//			System.out.println("The diff of cls is " + diffcls);
//			double percentageCls = ((double)diffcls / (double)sumCls) * 100;
//			System.out.println("The percentage of cls is " + percentageCls);
//			
//			if (percentageOps <= 50 && percentageCls <= 50 && percentageDps <= 75) {
//				granularityMismatches.add(c.getObject1AsURI().getFragment() + " - " + c.getObject2AsURI().getFragment());
//			}

		}


		return granularityMismatches;
	}

}

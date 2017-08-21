package compose.matchers;

import java.util.Properties;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;

import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.ontowrap.OntowrapException;
import compose.misc.StringUtils;
import rita.RiWordNet;

public class PropEq_WordNet_Matcher extends ObjectAlignment implements AlignmentProcess {

	private static ILexicalDatabase db = new NictWordNet();

	static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	static OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();

	static RiWordNet database = new RiWordNet("/Users/audunvennesland/Documents/PhD/Development/WordNet/WordNet-3.0/dict");

	/**
	 * The align() method is imported from the Alignment API and is modified to use the wordNetMatch method declared in this class
	 */
	public void align( Alignment alignment, Properties param ) throws AlignmentException {
		try {
			//for matching properties use the ontologyX.getProperties() method instead...
			// Match properties
			for ( Object cl2: ontology2().getProperties() ){
				for ( Object cl1: ontology1().getProperties() ){

					// add mapping into alignment object 
					addAlignCell(cl1,cl2, "=", wordNetMatch(cl1,cl2));  
				}		
			}

		} catch (Exception e) { e.printStackTrace(); }
	}

	//must not just discard the correspondences if they are identified by the previous matcher, but not identified by this matcher
	public static Alignment matchAlignment(Alignment inputAlignment) throws AlignmentException {

		System.out.println("Running wordnet alignment!");

		Alignment refinedAlignment = new URIAlignment();
		double score = 0;
		double threshold = 0.6;

		//match the objects (need to preprocess to remove URI) in every cell of the alignment
		for (Cell c : inputAlignment) {
			score = wordNetMatch(StringUtils.getString(c.getObject1().toString()), StringUtils.getString(c.getObject2().toString()));
			if (score > threshold) {
				refinedAlignment.addAlignCell(c.getObject1(), c.getObject2(), "=", increaseCellStrength(score));
			} else {
				refinedAlignment.addAlignCell(c.getObject1(), c.getObject2(), "=", reduceCellStrength(score));
				continue;
			}
		}
		return refinedAlignment;
	}

	public static double increaseCellStrength(double inputStrength) {

		double newStrength = inputStrength + (inputStrength * 0.10);

		if (newStrength > 1.0) {
			newStrength = 1.0;
		}

		return newStrength;
	}

	public static double reduceCellStrength(double inputStrength) {

		double newStrength = inputStrength - (inputStrength * 0.10);

		if (newStrength > 1.0) {
			newStrength = 1.0;
		}

		return newStrength;
	}


	public double wordNetMatch(Object o1, Object o2) throws AlignmentException, OntowrapException {

		double distance = 0;


		//get the objects (entities)
		//need to split the strings
		String s1 = StringUtils.stringTokenize(ontology1().getEntityName(o1),true).toLowerCase();
		String s2 = StringUtils.stringTokenize(ontology2().getEntityName(o2),true).toLowerCase();


		//...measure their distance
		distance = (1 - database.getDistance(s1, s2, "n"));

		//printing the ontology objects and their measured distance
		System.out.println(s1 + " - " + s2 + " with measure: " + distance);

		return distance;
	}
	
	public static double wordNetMatch(String inputString1, String inputString2) {

		double distance = 0;


		//get the objects (entities)
		//need to split the strings
		String s1 = StringUtils.stringTokenize(inputString1,true).toLowerCase();
		String s2 = StringUtils.stringTokenize(inputString2,true).toLowerCase();


		//...measure their distance
		distance = (1 - database.getDistance(s1, s2, "n"));

		//printing the ontology objects and their measured distance
		System.out.println(s1 + " - " + s2 + " with measure: " + distance);

		return distance;
	}

}

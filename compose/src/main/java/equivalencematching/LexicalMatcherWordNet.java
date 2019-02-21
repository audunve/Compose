package equivalencematching;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;
import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.ontowrap.OntowrapException;
import utilities.Jaccard;
import utilities.WordNet;
import utilities.WordNet;


public class LexicalMatcherWordNet extends ObjectAlignment implements AlignmentProcess {
	
	double weight;
	
	public LexicalMatcherWordNet(double weight) {
		
		this.weight = weight;
		
	}

	public void align(Alignment alignment, Properties param) throws AlignmentException {


		try {
			// Match classes
			for ( Object cl2: ontology2().getClasses() ){
				for ( Object cl1: ontology1().getClasses() ){

					// add mapping into alignment object 
					addAlignCell(cl1,cl2, "=", weight*wordNetMatch(cl1,cl2));  
				}
			}

		} catch (Exception e) { e.printStackTrace(); }
	}

	public double wordNetMatch(Object o1, Object o2) throws OntowrapException {

		String s1 = ontology1().getEntityName(o1);
		String s2 = ontology2().getEntityName(o2);
		
		

		String[] s1Array = s1.split("(?<=.)(?=\\p{Lu})");
		String[] s2Array = s2.split("(?<=.)(?=\\p{Lu})");

		double jaccardSim = 0;

		Set<String> synonymsS1 = new HashSet<String>();
		Set<String> synonymsS2 = new HashSet<String>();

		for (String s : s1Array) {
			List<String> s1List = Arrays.asList(WordNet.getSynonyms(s.toLowerCase()));
			for (String t : s1List) {
				synonymsS1.add(t);
			}
		}

		for (String s : s2Array) {
			List<String> s2List = Arrays.asList(WordNet.getSynonyms(s.toLowerCase()));
			for (String t : s2List) {
				synonymsS2.add(t);
			}
		}
		

		if (!synonymsS1.isEmpty() && !synonymsS2.isEmpty()) {
			jaccardSim = Jaccard.jaccardSetSim(synonymsS1, synonymsS2);
		}
		
		
		
//		if (jaccardSim > 0.7) {
//			
//			System.out.println("Matching " + s1 + " and " + s2);
//			System.out.println("The jaccardSim is " + jaccardSim);
//			
//			//print synonymsets
//			System.out.println("Synonym set 1: ");
//			for (String s : synonymsS1) {
//				System.out.println(s);
//			}
//			
//			System.out.println("Synonym set 2: ");
//			for (String s : synonymsS2) {
//				System.out.println(s);
//			}
//		}

		return jaccardSim;


	}
}

package mismatchdetection;

import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Set;

import net.didion.jwnl.JWNLException;
import utilities.WordNet;

public class CategorisationMismatch {
	
	public static void main(String[] args) throws FileNotFoundException, JWNLException {
		
		Set<String> categorisationMismatches = detectCategorisationMismatches();
	}
	
	public static Set<String> detectCategorisationMismatches(/*OWLOntology onto1, OWLOntology onto2*/) throws FileNotFoundException, JWNLException {
		Set<String> categorisationMismatches = new HashSet<String>();
		
		String cls1 = "animal";		
		String subcls1_1 = "mammal";
		String subcls1_2 = "bird";
		
		String cls2 = "animal";
		String subcls2_1 = "carnivore";
		String subcls2_2 = "herbivore";
		
		System.out.println("Sim between " + subcls1_1 + " and " + subcls2_1 + " using LESK: " + WordNet.computeLESK(subcls1_1, subcls2_1));
		System.out.println("Sim between " + subcls1_1 + " and " + subcls2_2 + " using LESK: " + WordNet.computeLESK(subcls1_1, subcls2_2));
		System.out.println("Sim between " + subcls1_2 + " and " + subcls2_1 + " using LESK: " + WordNet.computeLESK(subcls1_2, subcls2_1));
		System.out.println("Sim between " + subcls1_2 + " and " + subcls2_2 + " using LESK: " + WordNet.computeLESK(subcls1_2, subcls2_2));
		
		System.out.println("Sim between " + subcls1_1 + " and " + subcls2_1 + " using Jiang-Conrath: " + WordNet.computeJiangConrath(subcls1_1, subcls2_1));
		System.out.println("Sim between " + subcls1_1 + " and " + subcls2_2 + " using Jiang-Conrath: " + WordNet.computeJiangConrath(subcls1_1, subcls2_2));
		System.out.println("Sim between " + subcls1_2 + " and " + subcls2_1 + " using Jiang-Conrath: " + WordNet.computeJiangConrath(subcls1_2, subcls2_1));
		System.out.println("Sim between " + subcls1_2 + " and " + subcls2_2 + " using Jiang-Conrath: " + WordNet.computeJiangConrath(subcls1_2, subcls2_2));
		
		System.out.println("Sim between " + subcls1_1 + " and " + subcls2_1 + " using Lin: " + WordNet.computeLin(subcls1_1, subcls2_1));
		System.out.println("Sim between " + subcls1_1 + " and " + subcls2_2 + " using Lin: " + WordNet.computeLin(subcls1_1, subcls2_2));
		System.out.println("Sim between " + subcls1_2 + " and " + subcls2_1 + " using Lin: " + WordNet.computeLin(subcls1_2, subcls2_1));
		System.out.println("Sim between " + subcls1_2 + " and " + subcls2_2 + " using Lin: " + WordNet.computeLin(subcls1_2, subcls2_2));
		
		System.out.println("Sim between " + subcls1_1 + " and " + subcls2_1 + " using WUP: " + WordNet.computeWuPalmer(subcls1_1, subcls2_1));
		System.out.println("Sim between " + subcls1_1 + " and " + subcls2_2 + " using WUP: " + WordNet.computeWuPalmer(subcls1_1, subcls2_2));
		System.out.println("Sim between " + subcls1_2 + " and " + subcls2_1 + " using WUP: " + WordNet.computeWuPalmer(subcls1_2, subcls2_1));
		System.out.println("Sim between " + subcls1_2 + " and " + subcls2_2 + " using WUP: " + WordNet.computeWuPalmer(subcls1_2, subcls2_2));

		return categorisationMismatches;
	}

}

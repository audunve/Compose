package compose.wordembedding;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.util.Scanner;
import java.util.Set;

/**
 * @author audunvennesland
 * 11. sep. 2017 
 */

/**
 * Retrieves all vectors associated with an input string
 * @author audunvennesland
 * 11. sep. 2017
 */
public class Vectors {
	
	public static boolean isAlpha(String name) {
	    char[] chars = name.toCharArray();

	    for (char c : chars) {
	        if(!Character.isLetter(c)) {
	            return false;
	        }
	    }

	    return true;
	}
	
	public static boolean stringMatch(String input, String cls) {
		boolean match = false;

			if (input.equals(cls)) {
				match = true;
			} else {
				match = false;
			}
		
		
		return match;
	}
	
	public static ArrayList<OWLClass> getConceptURIs(File ontoFile) throws OWLOntologyCreationException {
		ArrayList<OWLClass> labels = new ArrayList<OWLClass>();
		
		//get all IRI´s
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);
		Set<OWLClass> classes = onto.getClassesInSignature();
		
		for (OWLClass cls : classes) {
			labels.add(cls);
		}

		manager.removeOntology(onto);

		return labels;
		
	}
	
	public static double averageLabelVectors (ArrayList<String> inputVectors) {
		
		
		ArrayList<Double> averagedVectors = new ArrayList<Double>();
		
		for (String s : inputVectors) {
			averagedVectors.add(Double.valueOf(s));
		}
		
		int num = averagedVectors.size();
		double sum = 0;
		
		for (Double d : averagedVectors) {
			sum+=d;
		}
		
		double averageVectors = sum/num;
		
		return averageVectors;
	}
	
	public static void main(String[] args) throws Exception {
		
/*		//testing getConceptURIs method
		File ontoFile2 = new File("./files/ontologies/BIBO.owl");
		ArrayList<String> concepts = getConceptURIs(ontoFile2);
		
		for (String s : concepts) {
			System.out.println(s);
		}
		
		
		
		
		*/
		
	    Scanner sc = new Scanner(new File("./files/wordembedding/vectors.txt"));
	    //Scanner sc = new Scanner(new File("wikipedia-300.txt"));
	    Map<String, ArrayList<String>> vectorMap = new HashMap<String, ArrayList<String>>();
	    Map<String, ArrayList<String>> finalVectorMap = new HashMap<String, ArrayList<String>>();

        ArrayList<String> classes = new ArrayList<String>();
        classes.add("in");
        classes.add("was");
        classes.add("the");
        
        
        while (sc.hasNextLine()) {

        	String line = sc.nextLine();
        	String[] strings = line.split(" ");
        	String word1 = strings[0];
        	
        	ArrayList<String> vec = new ArrayList<String>();
        	for (int i = 1; i < strings.length; i++) {
        		vec.add(strings[i]);
        	}
        	vectorMap.put(word1, vec);

        }
        
        System.out.println("The initial vectorMap contains " + vectorMap.size() + " entries");
        for (Entry<String, ArrayList<String>> en : vectorMap.entrySet()) {
        	System.out.println(en.getKey() + " " + en.getValue());
        }
        
        
        //iterate the vectorMap and extract only those entries that has a key that matches a class name
        for (Entry<String, ArrayList<String>> ent : vectorMap.entrySet()) {
        	
        	for (String str : classes) {
        		if (stringMatch(ent.getKey(), str)) {
        			finalVectorMap.put(ent.getKey(), ent.getValue());
        		}
        	}

        }
        
        System.out.println("\nNumber of entries in finalVectorMap: " + finalVectorMap.size());
        
        for (Entry<String, ArrayList<String>> entr : finalVectorMap.entrySet()) {
        	System.out.println(entr.getKey() + " " + entr.getValue() + " -> Average: " + averageLabelVectors(entr.getValue()));
        }


	}

}




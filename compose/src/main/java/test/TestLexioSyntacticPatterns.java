package test;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import utilities.OntologyOperations;
import utilities.StringUtilities;

public class TestLexioSyntacticPatterns {
	
public static void main(String[] args) throws OWLOntologyCreationException {
		
		File ontoFile = new File("./files/schema-org.owl");
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);
		
		Set<String> allDefs = new HashSet<String>();
		
		for (OWLClass c : onto.getClassesInSignature()) {
			allDefs.addAll(OntologyOperations.getClassDefinitionsFull(onto, c));
		}
		
		for (String s : allDefs) {
			System.out.println(s);
		}
		
		Map<String, Set<String>> hyponyms = new HashMap<String, Set<String>>();
		
		
		String str = "assigned by agencies such as distributors, publishers, or vendors";
		
		
		for (String s : allDefs) {
		String splitter = "e.g.";
		Pattern p = Pattern.compile("(.*?)" + splitter + "(.*)");
		String firstSubString = null;
		String secondSubString = null;
		Matcher m = p.matcher(s);
		if (m.matches()) {
		    firstSubString = m.group(1); // may be empty
		    String[] hypernymArray = firstSubString.split(" ");
		    System.out.println("\nThe hypernym is " + StringUtilities.getLemma(hypernymArray[hypernymArray.length-1]));
		    secondSubString = m.group(2); // may be empty
		    String[] hyponymArray = split(secondSubString);
		    for (int i = 0; i < hyponymArray.length; i++) {
		    	System.out.println("Hyponym:" + hyponymArray[i]);
		    }
		} else {
		    // splitter not found in str
		}
		}
		
		//if such as, check if 'a' or 'an' follows, they should be removed
		//if such as, if there is not 'a' or 'an' it is likely that the hyponyms are in plural, and the lemma should be used.
		//if such as, the hyponyms are separated by 'or', 'and', ','
		//if such as, 'etc.' should be disregarded
		//e.g. is followed by a comma (','), then the hyponyms are separated by comma
		//including is followed by the hyponyms separated by comma
		//if there are adjectives in front of the nouns/hyponyms they should be disregarded
		//the hyponyms may be compounds where the modifier and head are separated by space, so we should check if there are
		//compounds not separated by space among the concepts of the other ontology (e.g. news article - NewsArticle)
		
		
	}
	
	//such as:
	//Hypernym = get word immediately before indicator (such as)
	//Hyponyms = get list of words immediately after indicator (such as) separeted by ',' OR 'or' OR 'and' until reaching '.' or end of line.
	
	//distributors, publishers, or vendors
	public static String[] split (String input) {
		
		return input.split("(i) or |and|[,/]");
		
//		return input.split("or|and|,");
		
	}
	
	public Set<String> getHyponymsByComma (String text) {
		String[] hypsByComma = text.split(",");
		
		Set<String> hyps = new HashSet<String>(Arrays.asList(hypsByComma));
		
		return hyps;
	}
	
	public Set<String> getHyponymsByOr (String text) {
		String[] hypsByComma = text.split(" or ");
		
		Set<String> hyps = new HashSet<String>(Arrays.asList(hypsByComma));
		
		return hyps;
	}

}

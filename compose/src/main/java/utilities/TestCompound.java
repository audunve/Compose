package utilities;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.compound.CompoundWordTokenFilterBase;
import org.apache.lucene.analysis.compound.DictionaryCompoundWordTokenFilter;
import org.apache.lucene.analysis.compound.HyphenationCompoundWordTokenFilter;
import org.apache.lucene.analysis.compound.hyphenation.HyphenationTree;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;

public class TestCompound {
	
	public static void main(String[] args) throws OWLOntologyCreationException {

		String s = "2RadioNavigationAid";
		
		System.out.println(isCompoundWord(s));
		System.out.println(getCompoundWordsWithSpaces(s));
		System.out.println(getCompoundHead(s));
		System.out.println(getCompoundQualifier(s));
		

	

	}
	
	public static boolean isCompoundWord(String s) {
		String[] compounds = s.split("(?<=.)(?=\\p{Lu})");
		
		if (compounds.length > 1) {
			return true;
		} else {
			return false;
		}
		
	}
	

	
	public static String getCompoundWordsWithSpaces (String s) {
		
		//System.err.println("From getCompoundWordsWithSpaces: String s is " + s);
		
		StringBuffer sb = new StringBuffer();
		
		ArrayList<String> compoundWordsList = getWordsFromCompound(s);
		
		//System.err.println("From getCompoundWordsWithSpaces: The size of compoundWordsList is  " + compoundWordsList.size());
		
		for (String word : compoundWordsList) {
			
			sb.append(word + " ");
			
		}
		
		String compoundWordWithSpaces = sb.toString();
		
		return compoundWordWithSpaces;
	}
	
	public static String getCompoundHead(String s) {
		String[] compounds = s.split("(?<=.)(?=\\p{Lu})");
		
		String compoundHead = compounds[compounds.length-1];
		
		return compoundHead;
	}
	
	public static String getCompoundQualifier(String s) {
		String[] compounds = s.split("(?<=.)(?=\\p{Lu})");
		
		String compoundHead = compounds[0];
		
		return compoundHead;
	}
	
	public static ArrayList<String> getWordsFromCompound (String s) {
		String[] compounds = s.split("(?<=.)(?=\\p{Lu})");
		
		ArrayList<String> compoundWordsList = new ArrayList<String>();
		
		for (int i = 0; i < compounds.length; i++) {
			compoundWordsList.add(compounds[i]);
		}
		
		return compoundWordsList;
		
	}
	}


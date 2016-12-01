package no.ntnu.idi.compose.misc;


import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import fr.inrialpes.exmo.ontosim.string.CommonWords;

public class StringProcessor {

/*	*//**
	 * Instantiates the CommonWords object from OntoSim
	 *//*
	static CommonWords commonWords = new CommonWords();*/

	/**
	 * Takes a filename as input and strips the ".owl" suffix from this file
	 * @param fileName
	 * @return fileName
	 */
	public static String stripOntologyName(String fileName) {

		String trimmedPath = fileName.substring(fileName.lastIndexOf("/") + 1);
		String owl = ".owl";
		String rdf = ".rdf";
		String stripped = null;
		
		if (fileName.endsWith(".owl")) {
		stripped = trimmedPath.substring(0, trimmedPath.indexOf(owl));
		} else {
			stripped = trimmedPath.substring(0, trimmedPath.indexOf(rdf));
		}

		return stripped;
	}

	/**
	 * Takes a filename as input and removes the IRI prefix from this file
	 * @param fileName
	 * @return filename - without IRI
	 */
	public static String stripPath(String fileName) {
		String trimmedPath = fileName.substring(fileName.lastIndexOf("/") + 1);
		return trimmedPath;

	}
	
	public static String stripOPPrefix(String propertyName) {


		String has = "has";
		String is = "is";
		String is_a_ = "is_a_";
		String has_a_ = "has_a_";
		String stripped = null;
		
		if (propertyName.startsWith(has_a_)) {
		stripped = propertyName.replaceFirst(has_a_, "");
		} else if (propertyName.startsWith(is_a_)) {
		stripped = propertyName.replaceFirst(is_a_, "");
		} else if (propertyName.startsWith(is)) {
			stripped = propertyName.replaceFirst(is, "");
		} else if (propertyName.startsWith(has)) {
			stripped = propertyName.replaceFirst(has, "");
		} else {
			stripped = propertyName;
		}

		return stripped;
	}

	/**
	 * Takes a string as input, tokenizes it, and removes stopwords from this string
	 * @param analyzer
	 * @param str
	 * @return results - as a string of tokens, without stopwords
	 */
	public static String tokenize(Analyzer analyzer, String str) {
		String result = null;
	    StringBuilder sb = new StringBuilder();
	    
	    try {
	      TokenStream stream  = analyzer.tokenStream(null, new StringReader(str));
	      stream.reset();
	      while (stream.incrementToken()) {
	    	  sb.append(stream.getAttribute(CharTermAttribute.class).toString());
	    	  sb.append(" ");
	      }
	      stream.close();
	    } catch (IOException e) {

	      throw new RuntimeException(e);
	    }
	    
	    
	    result = sb.toString();
	    return result;
	  }

	/**
	 * Takes as input a Set of strings along with a separator (usually whitespace) and uses StringBuilder to create a string from the Set.
	 * @param set
	 * @param sep
	 * @return result
	 */
	public static String join(Set<String> set, String sep) {
		String result = null;
		if(set != null) {
			StringBuilder sb = new StringBuilder();
			Iterator<String> it = set.iterator();
			if(it.hasNext()) {
				sb.append(it.next());
			}
			while(it.hasNext()) {
				sb.append(sep).append(it.next());
			}
			result = sb.toString();
		}
		return result;
	}
	
	/**
	 * Takes as input a String and produces an array of Strings from this String
	 * @param s
	 * @return result
	 */
	public static String[] split(String s) {
		String[] result = s.split(" ");

		return result;
	}

	/**
	 * Takes as input two arrays of String and compares each string in one array with each string in the other array if they are equal
	 * @param s1
	 * @param s2
	 * @return results - basically an iterator that counts the number of equal strings in the two arrays
	 */
	public static int commonWords(String[] s1, String[] s2) {

		int results = 0;

		for (int i = 0; i < s1.length; i++) {
			for (int j = 0; j < s2.length; j++) {
				if (s1[i].equals(s2[j])) {
					results++;
				}
			}
		}

		return results;
	}
	
	public static String removeDuplicates(String s) {
		
		return new LinkedHashSet<String>(Arrays.asList(s.split(" "))).toString().replaceAll("(^\\[|\\]$)", "").replace(", ", " ");
		
		
	}


	

	public static void main(String[] args) {

		/*
		String fileName1 = "file:files/OAEI-16-conference/conference/sigkdd.owl";
		String fileName2 = "file:files/OAEI-16-conference/conference/PCS.owl";

		String alignmentFileName = "ISub-" + stripOntologyName(fileName1) + "-" + stripOntologyName(fileName2) + ".rdf";
		System.out.println(alignmentFileName);
		String label = stripPath(fileName1);
		System.out.println(label);
		 */
		
		String textWithDuplicates = "accepted paper become part proceedings note camera ready paper considered accepted paper accepted different form";
		String newText = removeDuplicates(textWithDuplicates);
		System.out.println(newText);

		StringProcessor sp = new StringProcessor();

		String text = "";
		String[] splitText = sp.split(text);

		for (int i = 0; i < splitText.length; i++) {
			System.out.println(splitText[i]);
		}

		String text2 = "";
		String[] splitText2 = sp.split(text2);
		
		String text3 = "has_a_Review";
		System.out.println(stripOPPrefix(text3));
		




	}



}

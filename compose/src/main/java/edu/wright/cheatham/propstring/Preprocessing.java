package edu.wright.cheatham.propstring;

import java.util.ArrayList;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;

public class Preprocessing {
	
	public static String preprocess(OWLEntity x, OWLOntology ontologyX) {
		String s = StringUtils.getString(x, ontologyX);
		
		s = s.replaceAll("wordnet_", "");
		s = s.replaceAll("wikicategory_", "");
		
		s = Preprocessing.stringTokenize(s, true);
		return s;
	}
	
	
	public static String preprocess(String label) {
		String s = StringUtils.getString(label);
		
		s = s.replaceAll("wordnet_", "");
		s = s.replaceAll("wikicategory_", "");
		
		s = Preprocessing.stringTokenize(s, true);
		return s;
	}
	

	private static ArrayList<String> tokenize(String s, boolean lowercase) {
		if (s == null) {
			return null;
		}

		ArrayList<String> strings = new ArrayList<String>();

		String current = "";
		Character prevC = 'x';

		for (Character c: s.toCharArray()) {

			if ((Character.isLowerCase(prevC) && Character.isUpperCase(c)) || 
					c == '_' || c == '-' || c == ' ' || c == '/' || c == '\\' || c == '>') {

				current = current.trim();

				if (current.length() > 0) {
					if (lowercase) 
						strings.add(current.toLowerCase());
					else
						strings.add(current);
				}

				current = "";
			}

			if (c != '_' && c != '-' && c != '/' && c != '\\' && c != '>') {
				current += c;
				prevC = c;
			}
		}

		current = current.trim();

		if (current.length() > 0) {
			// this check is to handle the id numbers in YAGO
			if (!(current.length() > 4 && Character.isDigit(current.charAt(0)) && 
					Character.isDigit(current.charAt(current.length()-1)))) {
				strings.add(current.toLowerCase());
			}
		}

		return strings;
	}

	private static String stringTokenize(String s, boolean lowercase) {
		String result = "";

		ArrayList<String> tokens = tokenize(s, lowercase);
		for (String token: tokens) {
			result += token + " ";
		}

		return result.trim();
	}
}

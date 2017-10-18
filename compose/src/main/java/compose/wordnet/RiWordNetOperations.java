package compose.wordnet;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import rita.RiWordNet;
import rita.wordnet.jwnl.wndata.IndexWord;

public class RiWordNetOperations {

	static RiWordNet database = new RiWordNet("/Users/audunvennesland/Documents/PhD/Development/WordNet/WordNet-3.0/dict");

	public static String[] getSynonyms(String inputWord) {
		String[] synonyms = database.getSynonyms(inputWord, "n");

		return synonyms;
	}
	
	public static String[] getAllSynonyms(String inputWord) {
		String[] synonyms = database.getAllSynonyms(inputWord, "n");

		return synonyms;
	}

	public static String[] getHyponyms(String inputWord) {
		//String[] hyponyms = database.getAllHyponyms(inputWord, "n");
		String[] hyponyms = database.getHyponyms(inputWord, "n");

		return hyponyms;
	}

	public static String[] getMeronyms(String inputWord) {
		String[] meronyms = database.getAllMeronyms(inputWord, "n");

		return meronyms;
	}

	public static String[] getHypernyms(String inputWord) {
		String[] hypernyms = database.getAllHypernyms(inputWord, "n");

		return hypernyms;
	}
	
	public static int[] getSynsetKeys(String inputWord) {
		//IndexWord ind = new IndexWord(inputWord, "n");
		int[] synsetKeys = database.getSenseIds(inputWord, "n");
		return synsetKeys;
	}
	
	public static String[] getSynsets(String inputWord) {
		String[] synset = database.getSynset(inputWord, "n");
		
		return synset;
	}

	public static void readWDDomains(File file) {

		FileInputStream fis = null;
		BufferedInputStream bis = null;
		DataInputStream dis = null;

		try {
			fis = new FileInputStream(file);

			// Here BufferedInputStream is added for fast reading.
			bis = new BufferedInputStream(fis);
			dis = new DataInputStream(bis);

			// dis.available() returns 0 if the file does not have more lines.
			while (dis.available() != 0) {

				// this statement reads the line from the file and print it to
				// the console.
				System.out.println(dis.readLine());
			}

			// dispose all the resources after using them.
			fis.close();
			bis.close();
			dis.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		
		File wdDomains = new File("./files/WDDomains/wn-domains-3.2-20070223");
		
		String inputWord = "publisher";
		String[] synonyms = getSynonyms(inputWord);
		String[] hypernyms = getHypernyms(inputWord);
		String[] hyponyms = getHyponyms(inputWord);
		int[] synsetKeys = getSynsetKeys(inputWord);
		
		System.out.println("---Printing synonyms---");
		for (String s : synonyms) {

			System.out.println(s); 
		}
		
		System.out.println("---Printing hypernyms---");
		
		for (String h : hypernyms) {
			System.out.println(h);
		}
		
		System.out.println("---Printing hyponyms---");
		
		for (String h : hyponyms) {
			System.out.println(h);
		}
		

	}



}






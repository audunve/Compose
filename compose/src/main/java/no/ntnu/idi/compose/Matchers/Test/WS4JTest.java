package no.ntnu.idi.compose.Matchers.Test;

import java.util.Iterator;
import java.util.List;

import edu.cmu.lti.jawjaw.pobj.POS;
import edu.cmu.lti.jawjaw.pobj.Synset;
import edu.cmu.lti.jawjaw.util.WordNetUtil;
import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.lexical_db.data.Concept;
import edu.cmu.lti.ws4j.Relatedness;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.impl.WuPalmer;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;

public class WS4JTest {
	
	private static ILexicalDatabase db = new NictWordNet();
	
	public static void main(String[] args) {
		
		String s1 = "car";
		String s2 = "test2";
	
	WS4JConfiguration.getInstance().setMFS(true);
	double s = new WuPalmer(db).calcRelatednessOfWords(s1, s2);
	
	
	String word = "car";
	List<Synset> synsets = WordNetUtil.wordToSynsets(word, POS.n);
	
	Iterator itr = synsets.iterator();
	
	while (itr.hasNext()) {
		System.out.println(itr.next().toString());
	}
	}
	


}

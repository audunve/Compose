package utilities;

import java.util.Set;

public class LexicalConcept {
	
	String conceptName;
	Set<String> hyponyms;
	Set<String> glossTokens;
	
	public LexicalConcept(String conceptName, Set<String> hyponyms, Set<String> glossTokens) {
		super();
		this.conceptName = conceptName;
		this.hyponyms = hyponyms;
		this.glossTokens = glossTokens;
	}

	public LexicalConcept() {

	}

	public String getConceptName() {
		return conceptName;
	}

	public void setConceptName(String conceptName) {
		this.conceptName = conceptName;
	}

	public Set<String> getHyponyms() {
		return hyponyms;
	}

	public void setHyponyms(Set<String> hyponyms) {
		this.hyponyms = hyponyms;
	}

	public Set<String> getGlossTokens() {
		return glossTokens;
	}

	public void setGlossTokens(Set<String> glossTokens) {
		this.glossTokens = glossTokens;
	}
	
	
	
	
	

}

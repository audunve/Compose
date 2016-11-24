package no.ntnu.idi.compose.misc;

public class StringProcessor {
	
	public static String stripOntologyName(String fileName) {
		
		String before = fileName.substring(fileName.lastIndexOf("/") + 1);
		String owl = ".owl";
		String after = before.substring(0, before.indexOf(owl));
		
		return after;
	}
	
	public static void main(String[] args) {
		
		String fileName1 = "file:files/OAEI-16-conference/conference/sigkdd.owl";
		String fileName2 = "file:files/OAEI-16-conference/conference/PCS.owl";
		
		String alignmentFileName = "ISub-" + stripOntologyName(fileName1) + "-" + stripOntologyName(fileName2) + ".rdf";
		System.out.println(alignmentFileName);
	}
	
	

}

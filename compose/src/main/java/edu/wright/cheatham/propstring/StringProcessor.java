package edu.wright.cheatham.propstring;



import java.io.File;


public class StringProcessor {
	
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
	
	public static String stripPath(String fileName) {
		String trimmedPath = fileName.substring(fileName.lastIndexOf("/") + 1);
		return trimmedPath;

	}
	
	public static void main(String[] args) {
		
		String fileName1 = "file:files/OAEI-16-conference/conference/sigkdd.owl";
		String fileName2 = "file:files/OAEI-16-conference/conference/PCS.owl";
		
		String alignmentFileName = "ISub-" + stripOntologyName(fileName1) + "-" + stripOntologyName(fileName2) + ".rdf";
		System.out.println(alignmentFileName);
		String label = stripPath(fileName1);
		System.out.println(label);
		
	}
	
	

}

package no.ntnu.idi.compose.wordembedding;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author audunvennesland
 * 23. mai 2017 
 */
public class RemoveDuplicates {
	
	public static void stripDuplicatesFromFile(File filename) throws IOException {
	    BufferedReader reader = new BufferedReader(new FileReader(filename));
	    Set<String> lines = new HashSet<String>(10000); // maybe should be bigger
	    String line;
	    while ((line = reader.readLine()) != null) {
	        lines.add(line);
	    }
	    reader.close();
	    BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
	    for (String unique : lines) {
	        writer.write(unique);
	        writer.newLine();
	    }
	    writer.close();
	}
	
	public static void main(String[] args) throws IOException {
		
		File filename = new File("./files/wordembedding/output/annotations.txt");
		stripDuplicatesFromFile(filename);
	}

}

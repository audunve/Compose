package no.ntnu.idi.compose.misc.wordembedding;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public class ListFiles {

	static File ontoDir = new File("./files/wordembedding");
	static File ontoFile = new File("./files/wordembedding/output/annotationProperties.txt");

     public static void main(String[] args) throws OWLOntologyCreationException, IOException
     {
         ListFiles lf = new ListFiles();
         lf.iterateFiles(lf.ontoDir);
         
         Labels.stripDuplicatesFromFile(ontoFile);

     }
     
     public void iterateFiles(File f) throws OWLOntologyCreationException{
         File files[];
         if(f.isFile()) {
             //System.out.println(f.getAbsolutePath());
        	 //File output = new File("./files/wordembedding/output/classes.txt");
        	 ArrayList<String> al = Labels.getAnnotationPropertyLabels(f);
        	 
        	 for (String s : al) {
        		 Labels.append(ontoFile, s); 
        	 }
        	 
        	 

         } else {
             files = f.listFiles();
             for (int i = 0; i < files.length; i++) {
            	 iterateFiles(files[i]);
             }
         }
     }
}

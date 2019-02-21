package utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class FileUtils {
	

	 
		public static void main(String[] args) throws IOException, OWLOntologyCreationException
		{
			//public static Set<String> getAllOntologyTokens(OWLOntology onto) {
			
			File ontoFile1 = new File("./files/_PHD_EVALUATION/ATMONTO-AIRM/ONTOLOGIES/ATMOntoCoreMerged.owl");
			File ontoFile2 = new File("./files/_PHD_EVALUATION/ATMONTO-AIRM/ONTOLOGIES/airm-mono.owl");
			File ontoFile3 = new File("./files/_PHD_EVALUATION/OAEI2011/ONTOLOGIES/301302/301302-301.rdf");
			File ontoFile4 = new File("./files/_PHD_EVALUATION/OAEI2011/ONTOLOGIES/301302/301302-302.rdf");
			File ontoFile5 = new File("./files/_PHD_EVALUATION/OAEI2011/ONTOLOGIES/301303/301303-303.rdf");
			File ontoFile6 = new File("./files/_PHD_EVALUATION/OAEI2011/ONTOLOGIES/301304/301304-304.rdf");
			
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			OWLOntology onto1 = manager.loadOntologyFromOntologyDocument(ontoFile1);
			OWLOntology onto2 = manager.loadOntologyFromOntologyDocument(ontoFile2);	
			OWLOntology onto3 = manager.loadOntologyFromOntologyDocument(ontoFile3);	
			OWLOntology onto4 = manager.loadOntologyFromOntologyDocument(ontoFile4);	
			OWLOntology onto5 = manager.loadOntologyFromOntologyDocument(ontoFile5);	
			OWLOntology onto6 = manager.loadOntologyFromOntologyDocument(ontoFile6);	
			
			Set<String> corpus = new HashSet<String>();
			corpus.addAll(OntologyOperations.getAllOntologyTokens(onto1));
			corpus.addAll(OntologyOperations.getAllOntologyTokens(onto2));
			corpus.addAll(OntologyOperations.getAllOntologyTokens(onto3));
			corpus.addAll(OntologyOperations.getAllOntologyTokens(onto4));
			corpus.addAll(OntologyOperations.getAllOntologyTokens(onto5));
			corpus.addAll(OntologyOperations.getAllOntologyTokens(onto6));
			
			System.out.println("The corpus contains " + corpus.size() + " tokens");
			for (String s : corpus) {
				System.out.println(s);
			}
			
//			String inputFile = "./files/_PHD_EVALUATION/EMBEDDINGS/wikipedia-300.txt";
//			
//			removeLines(corpus, inputFile);

			
			
		}
		
		
		public static File removeLines(Set<String> corpus, String inputFile) throws IOException {
			
			File processedFile = new File("./files/processedFile_Lemmatized_Wikipedia.txt");
			
			BufferedReader br=new BufferedReader(new FileReader(inputFile));
			
			//String buffer to store contents of the file
			StringBuffer sb=new StringBuffer("");
			
			String line;
			String wordToCheck = null;
			
			while((line=br.readLine())!=null) {
				
				//get the first word of the line
				
				int i = line.indexOf(' ');
				wordToCheck = line.substring(0, i);
				if (corpus.contains(wordToCheck)) {
					sb.append(line+"\n");
				}
				
			}
			
			br.close();
			
			FileWriter fw=new FileWriter(processedFile);
			//Write entire string buffer into the file
			fw.write(sb.toString());
			fw.close();
			
			
			return processedFile;
			
			
		}
		void delete(String filename, int startline, int numlines)
		{
			try
			{
				BufferedReader br=new BufferedReader(new FileReader(filename));
	 
				//String buffer to store contents of the file
				StringBuffer sb=new StringBuffer("");
	 
				//Keep track of the line number
				int linenumber=1;
				String line;
	 
				while((line=br.readLine())!=null)
				{
					//Store each valid line in the string buffer
					if(linenumber<startline||linenumber>=startline+numlines)
						sb.append(line+"\n");
					linenumber++;
				}
				if(startline+numlines>linenumber)
					System.out.println("End of file reached.");
				br.close();
	 
				FileWriter fw=new FileWriter(new File(filename));
				//Write entire string buffer into the file
				fw.write(sb.toString());
				fw.close();
			}
			catch (Exception e)
			{
				System.out.println("Something went horribly wrong: "+e.getMessage());
			}
		}

}

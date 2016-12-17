package misc;

import java.io.File;


public class Test {
	
	public static void main(String[] args) {
		

		//./files/OAEI-16-conference/conference/PCS.owl
		//should be: file:files/OAEI-16-conference/conference/PCS.owl
		//substring(2)
		
		File ontologyDir = new File("./files/OAEI-16-conference/conference");
		File[] filesInDir = ontologyDir.listFiles();
		String prefix = "file:";

		for (int i = 0; i < filesInDir.length; i++) {
			for (int j = i+1; j < filesInDir.length; j++) {
			if (filesInDir[i].isFile() && filesInDir[j].isFile() && i!=j) {
				System.out.println("Matching " + prefix.concat(filesInDir[i].toString().substring(2)) + " and " + prefix.concat(filesInDir[j].toString().substring(2)) );
			}
			}
		}
		
		String example = "./files/OAEI-16-conference/conference/PCS.owl";
		String before = example.substring(example.lastIndexOf("/") + 1);
		String owl = ".owl";
		String after = before.substring(0, before.indexOf(owl));
	    System.out.println(after);
			
		}
	}



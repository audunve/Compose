package no.ntnu.idi.compose.ui;
import java.io.File;

import javax.swing.JFileChooser;

/**
 * @author audunvennesland
 * 16. aug. 2017 
 */
public class OpenFile {
	
	JFileChooser fileChooser = new JFileChooser();

	StringBuilder sb1 = new StringBuilder();
	StringBuilder sb2 = new StringBuilder();
	
	File ontoFile1;
	File ontoFile2;
	
public File getFile() throws Exception {
		
		File f = null;
		
		if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			
			//get the file
			f = fileChooser.getSelectedFile();
			
			sb1.append(f.getName());
			
		} else {
			
		
			sb1.append("No file selected!");
		}
		
		return f;
		
	}
	
	
	public void printOntoFile1() throws Exception {
		
		if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			
			//get the file
			ontoFile1 = fileChooser.getSelectedFile();
			
			sb1.append(ontoFile1.getName());
			
		} else {
			
		
			sb1.append("No file selected!");
		}
		
	}
	
	public File getOntoFile1() throws Exception {
		
		File ontoFile1 = null;
		
		if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			
			//get the file
			ontoFile1 = fileChooser.getSelectedFile();
			
			sb1.append(ontoFile1.getName());
			
		} else {
			
		
			sb1.append("No file selected!");
		}
		
		return ontoFile1;
		
	}
	
	public void printOntoFile2() throws Exception {
		
		if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			
			//get the file
			ontoFile2 = fileChooser.getSelectedFile();
			
			sb1.append(ontoFile2.getName());
			
		} else {
			
		
			sb1.append("No file selected!");
		}
		
	}
	
public File getOntoFile2() throws Exception {
	
	File ontoFile2 = null;
		
		if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			
			//get the file
			ontoFile2 = fileChooser.getSelectedFile();
			
			sb2.append(ontoFile2.getName());
			
		} else {
			
		
			sb2.append("No file selected!");
		}
		
		return ontoFile2;
		
	}


	

}

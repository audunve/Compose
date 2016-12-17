package no.ntnu.idi.compose.preprocessing;

public class StringUtils {

	
	//could get all the bigrams from the string and check if some of them are in all uppercase
	public static boolean isAbbreviation(String s) {
		
		boolean isAbbreviation = false;
		
		int counter = 0;

		//iterate through the string
		    for (int i=0; i<s.length(); i++) {
		    
		        if (Character.isUpperCase(s.charAt(i))) {
		        	counter++;
		    }
		        if (counter > 2) {
		    isAbbreviation = true;
		} else {
			isAbbreviation = false;
		}
		    } 
		    
		    return isAbbreviation;
		}
		        
		        
		        
	
	public static void main(String[] args) {
		
		String test = "Chair_PC";
		
		System.out.println("Is " + test + " an abbreviation? Answer: " + isAbbreviation(test));
	}
		

		
	}
	


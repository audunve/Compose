package no.ntnu.idi.compose.preprocessing;

public class StringUtils {

	/**
	 * Checks if an input string is an abbreviation (by checking if there are two consecutive uppercased letters in the string)
	 * @param s input string
	 * @return boolean stating whether the input string represents an abbreviation
	 */
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
		        
		        
		        
	/**
	 * Test method
	 * @param args
	 */
	public static void main(String[] args) {
		
		String test = "Chair_TT";
		
		System.out.println("Is " + test + " an abbreviation? Answer: " + isAbbreviation(test));
	}
		

		
	}
	


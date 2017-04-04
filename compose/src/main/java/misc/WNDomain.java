package misc;

/**
 * @author audunvennesland
 * 6. mar. 2017 
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;

import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.Synset;

public class WNDomain {
	
	public static void testLongToString() {
		Long test = new Long(12345678);
    	String s = test.toString();
    	
    	System.out.println(s);
	}
	
	
	//retrieves the domain(s) given a synset offset as parameter
    public static String findDomain(String WNDomainsFileName,String searchStr) throws FileNotFoundException{    	
    	String domain = null;
        Scanner scan = new Scanner(new File(WNDomainsFileName));
        while(scan.hasNext()){
            String line = scan.nextLine().toLowerCase().toString();
            if(line.contains(searchStr)){
                //System.out.println(line);
                StringTokenizer tokenizer = new StringTokenizer(line, "	");
                List<String> parts = new ArrayList<String>();
                while(tokenizer.hasMoreTokens()) { 
                    String part = tokenizer.nextToken();
                    parts.add(part);
                }

                domain = parts.get(1);
            }
        }
        return domain;
    }
    
   public static List<Long> findSynsetOffset(String inputWord) throws FileNotFoundException, JWNLException {
	   Synset[] synsets = JWNLOperations.getSynsets(inputWord);
	   List<Long> offsetList = new ArrayList<Long>();
	   
	   if (synsets.length > 0) {
	   for (Synset s : synsets) {
		   offsetList.add(s.getOffset());		   
	   }
	   } else {
		   System.out.println("There are no synsets");
	   }

	   return offsetList;
   }
   
  
   
   public static boolean checkDomain(String w1, String w2) throws FileNotFoundException, JWNLException {
	   
	   boolean sameDomain = false;
	   
	   //get the list of offsets for the input words
	   List<Long> w1Offsets = findSynsetOffset(w1);
	   List<Long> w2Offsets = findSynsetOffset(w2);
	   
	   //get the domains from the offsets

	   
	   for (Long i : w1Offsets) {
		   for (Long j : w2Offsets) {
			   System.out.println("Checking " + i.longValue() + " and " + j.longValue());
			   if (i.longValue() == j.longValue()) {
				   System.out.println("They belong to the same domain!");
				   sameDomain = true;
				   
			   } else { 
				   
				   sameDomain = false;
			   }
		   }
	   }

	   return sameDomain;
   }
    	 
             

    public static void main(String[] args) throws FileNotFoundException, JWNLException{
        WNDomain fileSearch = new WNDomain();
        String inputWord = "Car";
        Synset[] synsets = JWNLOperations.getSynsets(inputWord);
        for (Synset s : synsets) {
        	System.out.println(s.toString());
        }
        
        System.out.println("The list of offsets for input word " + inputWord + " is: ");
        List<Long> offsetList = findSynsetOffset(inputWord);
        
        for (Long l : offsetList) {
        	System.out.println(l.longValue());
        }
        
        String dom = fileSearch.findDomain("./files/wndomains/wn-domains-3.2-20070223.txt", "2853224");
        System.out.println("The domain is " + dom);
        
        System.out.println("Checking if two words are from the same domain");
       	String w1 = "book";
       	String w2 = "journal";
       	
       	System.out.println("Are " + w1 + " and " + w2 + " from the same domain: " + checkDomain(w1, w2));
       	   	
       	
    }

}


package compose.wordnet;

/**
 * @author audunvennesland
 * 6. mar. 2017 
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.Synset;
import compose.misc.StringUtils;

public class WNDomain {
	
	
	/**
	    * Returns a list of offsets associated with an input word
	    * @param inputWord
	    * @return a list of synset offsets
	    * @throws FileNotFoundException
	    * @throws JWNLException
	    */
	   public static List<Long> findSynsetOffset(String inputWord) throws FileNotFoundException, JWNLException {
		   Synset[] synsets = JWNLOperations.getSynsets(inputWord);
		   List<Long> offsetList = new ArrayList<Long>();
		   
		   if (synsets.length == 0) {
			   System.out.println("There are no synsets");
		   } else {
			   for (Synset s : synsets) {
			   offsetList.add(s.getOffset());		   
		   }
		   }

		   return offsetList;
	   }
	   
	   /**
	    * Returns an arraylist of strings converted from offsets longs
	    * @param offset
	    * @return an arraylist of strings converted from offsets (long)
	 * @throws FileNotFoundException 
	    */
	   public static ArrayList<String> convertOffsetToString(List<Long> offset) throws FileNotFoundException {
		   ArrayList<String> synsets = new ArrayList<String>();
		   
		   for (Long l : offset) {
			   synsets.add(findDomain("./files/wndomains/wn-domains-3.2-20070223.txt", l.toString()));
		   }
		   //need to remove "factotum" since this is too generic
		   synsets.remove("factotum");
		   return synsets;
	   }
	   
	   /**
	     * Checks if two separate arraylists contain equal strings. 
	     * @param c1 list of domains associated with an ontology concept
	     * @param c2 list of domains associated with an ontology concept
	     * @return boolean stating whether the two input arraylist contains equal values
	     */
	   public static boolean sameDomain(ArrayList<String> c1, ArrayList<String> c2) {
		   boolean similar = false;
		   
		   for (String s1 : c1) {
			   for (String s2 : c2) {
				   if (s1.equals(s2)) {
					   similar = true;
					   break;
				   }
			   }
		   }
		   
		   return similar;
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
    
    	 
   /**
    * Returns an array list of domains associated with an ontology
    * @param ontoFile
    * @return
    * @throws OWLOntologyCreationException
    * @throws FileNotFoundException
    * @throws JWNLException
    */
   public static ArrayList<String> getDomains (File ontoFile) throws OWLOntologyCreationException, FileNotFoundException, JWNLException {
	   
	   ArrayList<String> domains = new ArrayList<String>();
	   

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();		
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);

		Set<OWLClass> classes = onto.getClassesInSignature();
		//System.out.println("Number of classes in ontology: " + classes.size());
	   
		for (OWLClass cls : classes) {
			//System.out.println("Trying " + cls.getIRI().getFragment().toLowerCase());
			List<Long> offsetList = findSynsetOffset(cls.getIRI().getFragment().toLowerCase());
			for (Long l : offsetList) {
				String offset = l.toString();
				String domain = findDomain("./files/wndomains/wn-domains-3.2-20070223.txt", offset);
				//System.out.println("Found domain for " + cls.getIRI().getFragment().toLowerCase() + ": " + domain);
				domains.add(domain);
			}
		}
		
		//need to remove duplicates
		Set<String> hs = new HashSet<>();
		hs.addAll(domains);
		domains.clear();
		domains.addAll(hs);
	   
	   return domains;
	   
   }
   
   public static Map<String, String> listDomains (File ontoFile) throws OWLOntologyCreationException, FileNotFoundException, JWNLException {
	   
	   Map<String, String> domains = new HashMap<String, String>();
	   

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();		
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);

		Set<OWLClass> classes = onto.getClassesInSignature();
		//System.out.println("Number of classes in ontology: " + classes.size());
	   
		for (OWLClass cls : classes) {
			List<Long> offsetList = findSynsetOffset(cls.getIRI().getFragment().toLowerCase());
			for (Long l : offsetList) {
				String offset = l.toString();
				String domain = findDomain("./files/wndomains/wn-domains-3.2-20070223.txt", offset);
				domains.put(cls.getIRI().getFragment().toLowerCase(), domain);
			}
		}
		
		//need to remove duplicates
		//Set<String> hs = new HashSet<>();
		//hs.addAll(domains);
		//domains.clear();
		//domains.addAll(hs);
	   
	   return domains;
	   
   }
   
   public static boolean sameDomain(String s1, String s2) throws FileNotFoundException, JWNLException {
	   
	   
	   List<Long> l1 = findSynsetOffset(s1);
	   List<Long> l2 = findSynsetOffset(s2);
	   
	   ArrayList<String> s1_offsetStrings = convertOffsetToString(l1);
	   ArrayList<String> s2_offsetStrings = convertOffsetToString(l2);
	   
	   boolean same = sameDomain(s1_offsetStrings, s2_offsetStrings);
	   
	   return same;
   }
             

    public static void main(String[] args) throws FileNotFoundException, JWNLException, OWLOntologyCreationException{
        WNDomain fileSearch = new WNDomain();
        
       	String s1 = "chair";
       	String s2 = "electric chair";
       	
       	List<Long> l1 = findSynsetOffset(s1);
       	List<Long> l2 = findSynsetOffset(s2);
       	
       	ArrayList<String> s1_offsetStrings = convertOffsetToString(l1);
       	ArrayList<String> s2_offsetStrings = convertOffsetToString(l2);
       	
       	System.out.println("--- Offset(s) for " + s1 + " ---");
       	for (String s1Offset : s1_offsetStrings) {
       		System.out.println(s1Offset);
       	}
       	
       	System.out.println("\n --- Offset(s) for " + s2 + "---");
       	for (String s2Offset : s2_offsetStrings) {
       		System.out.println(s2Offset);
       	}
       	
       	System.out.println("From the same domain?: " + sameDomain(s1_offsetStrings, s2_offsetStrings));
       	
       	
       	File ontoFile = new File("./files/OAEI-16-conference/ontologies/Biblio_2015.rdf");
       	
/*       	OWLOntologyManager manager = OWLManager.createOWLOntologyManager();		
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);
		
		Set<OWLClass> classes = onto.getClassesInSignature();
		
		//findDomain(String WNDomainsFileName,String searchStr)
		
		for (OWLClass o : classes) {
			System.out.println("Trying " + Preprocessor.stringTokenize(o.getIRI().getFragment().toLowerCase(), true));
			System.out.println(findDomain("./files/wndomains/wn-domains-3.2-20070223.txt", o.getIRI().getFragment().toLowerCase()));
		}
		
		
		for (OWLClass o : classes) {
			List<Long> offset = findSynsetOffset(o.getIRI().getFragment().toLowerCase());
			
			for (Long l : offset) {
				if (l != null) {
				System.out.println(l);
				} else {
					System.out.println("There is no offset for " + o.getIRI().getFragment().toLowerCase());
				}
			}
		}
       	*/
       	
       /*	ArrayList<String> domains = getDomains(ontoFile);
       	
       	System.out.println("Number of domains are " + domains.size());
       	
       	System.out.println("Printing domains for conference.owl");
       	
       	for (String s : domains) {
       		System.out.println(s);
       	}
       	


       	System.out.println("List of domains");
       	Map<String, String> domainMap = listDomains(ontoFile);
       	
       	for (Map.Entry<String, String> e : domainMap.entrySet()) {
       		System.out.println(e);
       		
       	}*/
       	

       	

       	   	
       	
    }

}


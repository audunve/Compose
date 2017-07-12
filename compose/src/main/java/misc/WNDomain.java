package misc;

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
import no.ntnu.idi.compose.preprocessing.Preprocessor;

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
    
   public static boolean compareDomains(List<Long> c1, List<Long> c2) {
	   
	   boolean similar = false;
	   String wnDomainsFile = "./files/wndomains/wn-domains-3.2-20070223.txt";
	   
	   ArrayList<String> c1Domains = new ArrayList<String>();
	   ArrayList<String> c2Domains = new ArrayList<String>();
	   
	   for (Long l1 : c1) {
		   c1Domains.add(String.valueOf(l1));
	   }
	   
	   for (Long l2 : c2) {
		   c2Domains.add(String.valueOf(l2));
	   }
	   
	   for (String s1 : c1Domains) {
		   for (String s2 : c2Domains) {
			   if (s1.equals(s2)) {
				   similar = true;
				   break;
			   }
		   }
	   }
	   
	   
	   return similar;
   }
    
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
    * Get the offsets of both input strings, and if some of the offsets are similar, the strings are from the same domain
    * @param w1
    * @param w2
    * @return
    * @throws FileNotFoundException
    * @throws JWNLException
    */
   public static boolean checkDomain(String w1, String w2) throws FileNotFoundException, JWNLException {
	   
	   
	   
	   //get the list of offsets for the input words
	   List<Long> w1Offsets = findSynsetOffset(w1);
	   List<Long> w2Offsets = findSynsetOffset(w2);
	   
	   boolean sameDomain = compareDomains(w1Offsets, w2Offsets);
	   
/*	   //get the domains from the offsets

	   
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
	   }*/

	   return sameDomain;
   }
   
   //public boolean matchDomains ()
    	 
   public static ArrayList<String> getDomains (File ontoFile) throws OWLOntologyCreationException, FileNotFoundException, JWNLException {
	   
	   ArrayList<String> domains = new ArrayList<String>();
	   

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();		
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);

		Set<OWLClass> classes = onto.getClassesInSignature();
		System.out.println("Number of classes in ontology: " + classes.size());
	   
		for (OWLClass cls : classes) {
			System.out.println("Trying " + cls.getIRI().getFragment().toLowerCase());
			List<Long> offsetList = findSynsetOffset(cls.getIRI().getFragment().toLowerCase());
			for (Long l : offsetList) {
				String offset = l.toString();
				String domain = findDomain("./files/wndomains/wn-domains-3.2-20070223.txt", offset);
				System.out.println("Found domain for " + cls.getIRI().getFragment().toLowerCase() + ": " + domain);
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
		System.out.println("Number of classes in ontology: " + classes.size());
	   
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
             

    public static void main(String[] args) throws FileNotFoundException, JWNLException, OWLOntologyCreationException{
        WNDomain fileSearch = new WNDomain();
        /*String inputWord = "automobile";
        Synset[] synsets = JWNLOperations.getSynsets(inputWord);
        for (Synset s : synsets) {
        	System.out.println(s.toString());
        }
        
        System.out.println("The list of offsets for input word " + inputWord + " is: ");
        List<Long> offsetList = findSynsetOffset(inputWord);
        
        for (Long l : offsetList) {
        	System.out.println(l.longValue());
        }
        
        String dom = findDomain("./files/wndomains/wn-domains-3.2-20070223.txt", "6013091");
        System.out.println("The domain is " + dom);
        
        System.out.println("Checking if two words are from the same domain");
       	String w1 = "car";
       	String w2 = "automobile";
       	
       	System.out.println("Are " + w1 + " and " + w2 + " from the same domain: " + checkDomain(w1, w2));*/
       	
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
       	
//       	ArrayList<String> domains = getDomains(ontoFile);
//       	
//       	System.out.println("Number of domains are " + domains.size());
//       	
//       	System.out.println("Printing domains for conference.owl");
//       	
//       	for (String s : domains) {
//       		System.out.println(s);
//       	}
//       	
//
//
//       	System.out.println("List of domains");
//       	Map<String, String> domainMap = listDomains(ontoFile);
//       	
//       	for (Map.Entry<String, String> e : domainMap.entrySet()) {
//       		System.out.println(e);
//       		
//       	}
       	
       	//checkDomain(String w1, String w2)
       	
       	//Map<String, String> listDomains (File ontoFile) 
       	
       	//public static List<Long> findSynsetOffset(String inputWord)
       	
       	String s1 = "person";
       	String s2 = "person";
       	
       	List<Long> l1 = findSynsetOffset(s1);
       	List<Long> l2 = findSynsetOffset(s2);
       	
       	System.out.println("--- Offset(s) for " + s1 + " ---");
       	for (Long l : l1) {
       		System.out.println(l);
       		System.out.println("The domain is " + findDomain("./files/wndomains/wn-domains-3.2-20070223.txt", l.toString()));
       	}
       	
    	System.out.println("\n --- Offset(s) for " + s2 + "---");
       	for (Long l : l2) {
       		System.out.println(l);
       		System.out.println("The domain is " + findDomain("./files/wndomains/wn-domains-3.2-20070223.txt", l.toString()));
       	}
       	
       	System.out.println("From the same domain?: " + checkDomain(s1, s2));
       	   	
       	
    }

}


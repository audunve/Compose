package test;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import it.uniroma1.lcl.babelnet.BabelNet;
import it.uniroma1.lcl.babelnet.BabelNetQuery;
import it.uniroma1.lcl.babelnet.BabelSense;
import it.uniroma1.lcl.babelnet.BabelSynset;
import it.uniroma1.lcl.babelnet.BabelSynsetID;
import it.uniroma1.lcl.babelnet.data.BabelPOS;
import it.uniroma1.lcl.babelnet.data.BabelSenseSource;
import it.uniroma1.lcl.jlt.util.Language;


public class TestBabelNet {
	
	final static BabelNet bn = BabelNet.getInstance();

	public static void main(String[] args) throws OWLOntologyCreationException, FileNotFoundException {
		/* CHECK HOW MANY CONCEPTS FROM ONTOLOGIES ARE CONTAINED IN BABELNET */
//		BabelNet bn = BabelNet.getInstance();
//		File ontoFile1 = new File("./files/ESWC_ATMONTO_AIRM/ATMOntoCoreMerged.owl");
//		File ontoFile2 = new File("./files/ESWC_ATMONTO_AIRM/airm-mono.owl");
//		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
//		OWLOntology onto1 = manager.loadOntologyFromOntologyDocument(ontoFile1);
//		OWLOntology onto2 = manager.loadOntologyFromOntologyDocument(ontoFile2);
//
//		System.out.println("BabeldNet contains synsets for " + babelNetRepresentation(onto1, onto2) + " classes");
		
		String s = "written";
		ArrayList<String> objectList = getBabelSynsetObjects(s);
		
		System.out.println("There are " + objectList.size() + " objects for " + s);
		
		ArrayList<String> synonyms = getSynonyms(s);
		System.out.println("There are " + synonyms.size() + " synsets for " + s);
		System.out.println("Printing synsets:");
		for (String syn : synonyms) {
			System.out.println(syn.substring(syn.lastIndexOf(":")+1));
		}
		

		
		ArrayList<String> hypernyms = getHypernyms(s);
		System.out.println("\nThere are " + hypernyms.size() + " hypernyms for " + s);
		System.out.println("Printing hypernyms:");
		for (String hyp : hypernyms) {
			System.out.println(hyp);
		}
		
		System.out.println("\nPrinting wikidata for route");
		ArrayList<String> routeExList = getWikidata("route");
		for (String rs : routeExList) {
			System.out.println(rs);
		}
		
		System.out.println("\nPrinting wikidata for trajectory");
		getWikidata("trajectory");
		
		
	}

	

	/**
	 * Checks if a word is contained in BabelNet
	 * @param inputWord
	 * @return
	   Dec 17, 2018
	 */
	private static boolean containedInBabelNet (String inputWord) {
		
		List<BabelSynset> synsets = bn.getSynsets(inputWord.toLowerCase(), Language.EN);
		if (synsets.size() > 0) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Retrieves all synonyms associated with a word in BabelNet
	 * @param inputWord
	 * @return list of synsets
	   Dec 17, 2018
	 */
	public static ArrayList<String> getSynonyms (String inputWord) {
	ArrayList<String> synonymList = new ArrayList<String>();	
	BabelNetQuery query = new BabelNetQuery.Builder(inputWord).from(Language.EN).build();
	List<BabelSynset> byl = bn.getSynsets(query);
	
	String synonym = null;
	for (BabelSynset b : byl) {
		synonym = b.toString();
		//remove the suffix (everything after #)
		if (synonym.contains("#")) {
			synonym = synonym.substring(0, synonym.indexOf("#"));
		}
		synonymList.add(synonym.substring(synonym.lastIndexOf(":")+1));
	}
		
	return synonymList;
	}
	
	/**
	 * Retrieves all hypernyms associated with a word in BabelNet
	 * @param inputWord
	 * @return list of hypernyms (as strings)
	   Dec 17, 2018
	 */
	public static ArrayList<String> getHypernyms (String inputWord) {
		ArrayList<String> hypernymsList = new ArrayList<String>();
		BabelSynset by = bn.getSynset(getKeyBabelSynsetObject(inputWord));
		for (String form : by.getOtherForms(Language.EN)) {
			hypernymsList.add(form);
		}
		
		return hypernymsList;
	}
	
	/**
	 * Retrieves the key BabelSynsetObject for a given string
	 * @param inputWord
	 * @return
	   Dec 17, 2018
	 */
	public static BabelSynsetID getKeyBabelSynsetObject (String inputWord) {
		BabelSynsetID id = null;
		for (BabelSynset synset : bn.getSynsets(inputWord, Language.EN)) {
			if (synset.isKeyConcept()) {
			id = synset.getID();

			}
		}
		
		return id;
	}
	
	/**
	 * Retrieves all BabelSynsetObjects for a given string
	 * @param inputWord
	 * @return
	   Dec 17, 2018
	 */
	public static ArrayList<String> getBabelSynsetObjects (String inputWord) {
		ArrayList<String> objectList = new ArrayList<String>();	
		for (BabelSynset synset : bn.getSynsets(inputWord, Language.EN)) {
			objectList.add(synset.getID().toString());
		}
		
		return objectList;
	}
		
	
	/**
	 * Retrieves all wikidata for an inputword
	 * @param inputWord
	   Jan 26, 2019
	 */
	public static ArrayList<String> getWikidata(String inputWord) {
		ArrayList<String> wikidataList = new ArrayList<String>();
		BabelSynset by = bn.getSynset(getKeyBabelSynsetObject(inputWord));
		for (BabelSense sense : by.getSenses(BabelSenseSource.WIKIDATA)) {
			//String sensekey = sense.getSensekey();
	        wikidataList.add(sense.getSensekey());
		}
		
		return wikidataList;
	}
	
	//TODO
	public static ArrayList<String> getMeronyms (String inputWord) {
		ArrayList<String> meronymsList = new ArrayList<String>();
		
		//for (BabelPointer bp : bn.get)
		
		return meronymsList;
	}
	
	
	
	//TODO
	public static ArrayList<String> getHolonyms (String inputWord) {
		ArrayList<String> holonymsList = new ArrayList<String>();
		
		return holonymsList;
	}
	
	
	/**
	 * Returns how many of onto1 and onto2 concepts are contained in BabelNet
	 * @param onto1
	 * @param onto2
	 * @return
	 * @throws FileNotFoundException
	   Dec 17, 2018
	 */
	public static int babelNetRepresentation(OWLOntology onto1, OWLOntology onto2) throws FileNotFoundException {
		int rep = 0;	

		Set<OWLClass> onto1Cls = onto1.getClassesInSignature();
		Set<OWLClass> onto2Cls = onto2.getClassesInSignature();
		//merge the sets
		onto2Cls.addAll(onto1Cls);

		for (OWLClass cls : onto2Cls) {

			if (containedInBabelNet(cls.getIRI().getFragment()))
				rep++;
		}

		return rep;
	}

}

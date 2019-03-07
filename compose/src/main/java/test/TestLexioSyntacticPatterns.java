package test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import net.didion.jwnl.JWNLException;
import utilities.OntologyOperations;
import utilities.StringUtilities;
import utilities.WordNet;

public class TestLexioSyntacticPatterns {

	public static void main(String[] args) throws OWLOntologyCreationException, FileNotFoundException, JWNLException {

		File ontoFile = new File("./files/schema-org.owl");
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);
		
		Set<String> extracts = new HashSet<String>();
		Set<String> allDefs = new HashSet<String>();

				for (OWLClass c : onto.getClassesInSignature()) {
					allDefs.addAll(OntologyOperations.getClassDefinitionsFull(onto, c));
					
					if (OntologyOperations.getClassDefinitionFull(onto, c).contains("such as") 
							|| OntologyOperations.getClassDefinitionFull(onto, c).contains("e.g.") 
							|| OntologyOperations.getClassDefinitionFull(onto, c).contains("for example") 
							|| OntologyOperations.getClassDefinitionFull(onto, c).contains("includes")
							|| OntologyOperations.getClassDefinitionFull(onto, c).contains("including")) {
					extracts = extractLexicoSyntacticPattern(OntologyOperations.getClassDefinitionFull(onto, c));
					
					System.out.println("\nPrinting out possible hyponyms associated with " + c.getIRI().getFragment() + " :");
					for (String s : extracts) {
						System.out.println(s);
					}
					System.out.println("Original definition:");
					System.out.println(OntologyOperations.getClassDefinitionsFull(onto, c));
					}
				}

//				for (String s : allDefs) {
//					System.out.println(s);
//				}

		Map<String, Set<String>> hyponyms = new HashMap<String, Set<String>>();


		String str = "assigned by agencies such as distributors, publishers, or vendors.";
		String egStr = "type of book, e.g. a monograph, a novel, or a text.";





		//if such as, check if 'a' or 'an' follows, they should be removed
		//if such as, if there is not 'a' or 'an' it is likely that the hyponyms are in plural, and the lemma should be used.
		//if such as, the hyponyms are separated by 'or', 'and', ','
		//if such as, 'etc.' should be disregarded
		//e.g. is followed by a comma (','), then the hyponyms are separated by comma
		//including is followed by the hyponyms separated by comma
		//if there are adjectives in front of the nouns/hyponyms they should be disregarded
		//the hyponyms may be compounds where the modifier and head are separated by space, so we should check if there are
		//compounds not separated by space among the concepts of the other ontology (e.g. news article - NewsArticle)


	}

	//such as:
	//Hypernym = get word immediately before indicator (such as)
	//Hyponyms = get list of words immediately after indicator (such as) separeted by ',' OR 'or' OR 'and' until reaching '.' or end of line.

	//distributors, publishers, or vendors


	public static Set<String> extractLexicoSyntacticPattern (String def) throws FileNotFoundException, JWNLException {
		String extract = null;
		String cut = null;
		String refined = null;

		if (def.contains("such as")) {
		extract = def.substring(def.indexOf("such as")+8, def.length());
		} 
		else if (def.contains("e.g.")) {
			extract = def.substring(def.indexOf("e.g.")+5, def.length());
		}
		else if (def.contains("for example")) {
			extract = def.substring(def.indexOf("for example")+12, def.length());
		}
		else if (def.contains("includes")) {
			extract = def.substring(def.indexOf("includes")+9, def.length());
		}
		else if (def.contains("including")) {
			extract = def.substring(def.indexOf("including")+10, def.length());
		}

		if (extract.contains(".")) {
			cut = extract.substring(0, extract.indexOf("."));
			refined = removeStopWords(cut);
		} else {
			refined = removeStopWords(extract);
		}

		String[] extractArray = refined.split(",| or ");

		Set<String> rawDefTerms = new HashSet<String>();

		for (int i = 0; i < extractArray.length; i++) {

			if (extractArray[i].contains(" a ")) {
				rawDefTerms.add(extractArray[i].replace(" a ", "").replaceAll("\\s", ""));
			}		
			else if (extractArray[i].contains(" an ")) {
				rawDefTerms.add(extractArray[i].replace(" an ", "").replaceAll("\\+", ""));
			}			
			else if (extractArray[i].contains(" or ")) {
				rawDefTerms.add(extractArray[i].replace(" or ", "").replaceAll("\\s", ""));
			} 
			else if (extractArray[i].contains(" and ")) {
				rawDefTerms.add(extractArray[i].replace(" and ", "").replaceAll("\\s", ""));
			} 
			else if (extractArray[i].contains(" etc ")) {
				rawDefTerms.add(extractArray[i].replace(" etc ", "").replaceAll("\\s", ""));
			}
			else {
				rawDefTerms.add(extractArray[i].replaceAll("\\s", ""));
			}
		}
		
		//extract only those terms that exist in wordnet
		Set<String> refinedDefTerms = new HashSet<String>();
		String wordNetLemma = null;
		for (String s : rawDefTerms) {
			if (WordNet.getWordNetLemma())
			if (WordNet.containedInWordNet(s)) {
				refinedDefTerms.add(s);
			}
		}

		return refinedDefTerms;
	}

//	public static Set<String> extractEG (String def) {
//
//		String extract = def.substring(def.indexOf("e.g.")+5, def.length());
//		String cut = null;
//		String refined = null;
//		if (extract.contains(".")) {
//		cut = extract.substring(0, extract.indexOf("."));		
//		refined = StringUtilities.removeStopWords(cut);
//		} else {
//			refined = StringUtilities.removeStopWords(extract);
//		}
//		
//		String[] extractArray = refined.split(",");
//
//		Set<String> defTerms = new HashSet<String>();
//
//		for (int i = 0; i < extractArray.length; i++) {
//
//			if (extractArray[i].contains(" a ")) {
//				defTerms.add(extractArray[i].replace(" a ", "").replaceAll("\\s", ""));
//			}			
//			else if (extractArray[i].contains(" an ")) {
//				defTerms.add(extractArray[i].replace(" an ", "").replaceAll("\\+", ""));
//			}			
//			else if (extractArray[i].contains(" or ")) {
//				defTerms.add(extractArray[i].replace(" or ", "").replaceAll("\\s", ""));
//			} 
//			else if (extractArray[i].contains(" and ")) {
//				defTerms.add(extractArray[i].replace(" and ", "").replaceAll("\\s", ""));
//			} 
//			else {
//				defTerms.add(extractArray[i].replaceAll("\\s", ""));
//			}
//		}
//
//		return defTerms;
//	}
//	
//	public static Set<String> extractInclude (String def) {
//
//		String extract = def.substring(def.indexOf("includes")+8, def.length());
//		String cut = null;
//		String refined = null;
//		if (extract.contains(".")) {
//			cut = extract.substring(0, extract.indexOf("."));
//			refined = removeStopWords(cut);
//		} else {
//			refined = removeStopWords(extract);
//		}
//
//		String[] extractArray = refined.split(",| or");
//
//		Set<String> defTerms = new HashSet<String>();
//
//		for (int i = 0; i < extractArray.length; i++) {
//
//			if (extractArray[i].contains(" a ")) {
//				defTerms.add(extractArray[i].replace(" a ", "").replaceAll("\\s", ""));
//			}		
//			else if (extractArray[i].contains(" an ")) {
//				defTerms.add(extractArray[i].replace(" an ", "").replaceAll("\\+", ""));
//			}			
//			else if (extractArray[i].contains(" or ")) {
//				defTerms.add(extractArray[i].replace(" or ", "").replaceAll("\\s", ""));
//			} 
//			else if (extractArray[i].contains(" and ")) {
//				defTerms.add(extractArray[i].replace(" and ", "").replaceAll("\\s", ""));
//			} 
//			else if (extractArray[i].contains(" etc ")) {
//				defTerms.add(extractArray[i].replace(" etc ", "").replaceAll("\\s", ""));
//			}
//			else {
//				defTerms.add(extractArray[i].replaceAll("\\s", ""));
//			}
//		}
//
//		return defTerms;
//	}



	public static String[] split (String input) {

		return input.split("(i) or |and|[,/]");


	}

	public Set<String> getHyponymsByComma (String text) {
		String[] hypsByComma = text.split(",");

		Set<String> hyps = new HashSet<String>(Arrays.asList(hypsByComma));

		return hyps;
	}

	public Set<String> getHyponymsByOr (String text) {
		String[] hypsByComma = text.split(" or ");

		Set<String> hyps = new HashSet<String>(Arrays.asList(hypsByComma));

		return hyps;
	}
	
	private static String removeStopWords (String inputString) {

		List<String> stopWordsList = Arrays.asList(
				"a", "an", "are", "as", "at", "be", "but", "by",
				"for", "if", "in", "into", "is", "it",
				"no", "not", "of", "on", "such",
				"that", "the", "their", "then", "there", "these",
				"they", "this", "to", "was", "will", "with"
				);

		String[] words = inputString.split(" ");
		ArrayList<String> wordsList = new ArrayList<String>();
		StringBuffer sb = new StringBuffer();

		for(String word : words)
		{
			String wordCompare = word.toLowerCase();
			if(!stopWordsList.contains(wordCompare))
			{
				wordsList.add(word);
			}
		}

		for (String str : wordsList){
			sb.append(str + " ");
		}

		return sb.toString();
	}

}

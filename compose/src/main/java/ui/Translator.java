package ui;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.google.cloud.translate.Translate.TranslateOption;

public class Translator {
	
	public static String translateWord(String input, String inputLanguage, String outputLanguage) {
		
		// Instantiates a client
	    Translate translate = TranslateOptions.getDefaultInstance().getService();
	    
		String output = null;
		
		Translation translation = translate.translate(
				input, 
				TranslateOption.sourceLanguage(inputLanguage), 
				TranslateOption.targetLanguage(outputLanguage));
		
		output = translation.getTranslatedText();
		
		return output;
	}
	
	public static String translateSentence(String input, String inputLanguage, String outputLanguage) {
		
		// Instantiates a client
	    Translate translate = TranslateOptions.getDefaultInstance().getService();
	    
		String output = null;
		
		Translation translation = translate.translate(
				input, 
				TranslateOption.sourceLanguage(inputLanguage), 
				TranslateOption.targetLanguage(outputLanguage));
		
		output = translation.getTranslatedText();
		
		return output;
	}
	
	public static void main(String... args) throws Exception {
		
		String inputWord = "bil";
		String inputSentence = "dette er en rød bil";
		
//		String output = translateWord(inputWord,"no", "en");
//		System.out.println("The English word for " + inputWord + " is: " + output);
		
		String outputSentence = translateWord(inputSentence,"no", "en");
		System.out.println("The English translation of the sentence " + inputSentence + " is: " + outputSentence);
		
		
		
	}
//	    // Instantiates a client
//	    Translate translate = TranslateOptions.getDefaultInstance().getService();
//
//	    // The text to translate
//	    String text = "Hello, world!";
//
//	    // Translates some text into Russian
//	    Translation translation =
//	        translate.translate(
//	            text,
//	            TranslateOption.sourceLanguage("en"),
//	            TranslateOption.targetLanguage("ru"));
//
//
//	    System.out.printf("Text: %s%n", text);
//	    System.out.printf("Translation: %s%n", translation.getTranslatedText());
//	  }

}

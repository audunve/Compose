package backup;

import org.semanticweb.owl.align.AlignmentProcess;

/**
 * This string-based matcher from the Alignment API implements the Levenshtein Distance. 
 * @author audunvennesland
 * 4. sep. 2017 
 */
public class EditMatcher_remove extends StringDistAlignment_remove implements AlignmentProcess {

	    /** Creation **/
	    public EditMatcher_remove() {
		methodName = "levenshteinDistance";
	    };
	}



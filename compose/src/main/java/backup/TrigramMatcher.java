package backup;

/**
 * This string-based matcher from the Alignment API implements n-gram (trigram).
 * @author audunvennesland
 * 4. sep. 2017 
 */

import org.semanticweb.owl.align.AlignmentProcess;

public class TrigramMatcher extends StringDistAlignment_remove implements AlignmentProcess {

    /** Creation **/
    public TrigramMatcher(){
	methodName = "ngramDistance";
    };
}

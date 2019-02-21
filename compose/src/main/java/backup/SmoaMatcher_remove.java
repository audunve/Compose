package backup;

/**
 * @author audunvennesland
 * 4. sep. 2017 
 */
import org.semanticweb.owl.align.AlignmentProcess;

public class SmoaMatcher_remove extends StringDistAlignment_remove implements AlignmentProcess {

    /** Creation **/
    public SmoaMatcher_remove(){
	methodName = "smoaDistance";
    };
}

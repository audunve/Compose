package backup;

/**
 * @author audunvennesland
 * 4. sep. 2017 
 */
import java.net.URI;
import java.util.Properties;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.AlignmentException;

import fr.inrialpes.exmo.ontosim.string.StringDistances;

import fr.inrialpes.exmo.align.impl.DistanceAlignment;
import fr.inrialpes.exmo.align.impl.MatrixMeasure;

/**
 * Represents an OWL ontology alignment. An ontology comprises a number of
 * collections. Each ontology has a number of classes, properties and
 * individuals, along with a number of axioms asserting information
 * about those objects.
 *
 * An improvement of that class is that, since it is based on names only,
 * it can match freely property names with class names...
 *
 * @author J�r�me Euzenat
 * @version $Id: StringDistAlignment.java 2065 2015-10-02 09:49:17Z euzenat $ 
 */

public class StringDistAlignment_remove extends DistanceAlignment implements AlignmentProcess {

    
    Method dissimilarity = null;
    String methodName = "equalDistance";

    protected class StringDistMatrixMeasure extends MatrixMeasure {
	public StringDistMatrixMeasure() {
	    similarity = false; // This is a distance matrix
	}
	public double measure( Object o1, Object o2 ) throws Exception {
	    String s1 = null;
	    String s2 = null;
	    try {
	    	//Audun: Gets the entity names and stores them as string1 and string2
		s1 = ontology1().getEntityName( o1 );
		s2 = ontology2().getEntityName( o2 );
	    } catch ( Exception owex ) {

	    };
	    // Unnamed entity = max distance
	    if ( s1 == null || s2 == null ) return 1.;
	    Object[] params = { s1.toLowerCase(), s2.toLowerCase() };
	    //logger.trace( "OB:{} ++ {} ==> {}", s1, s2, dissimilarity.invoke( null, params ) );
	    return ((Double)dissimilarity.invoke( null, params )).doubleValue();
	}
	public double classMeasure( Object cl1, Object cl2 ) throws Exception {
	    return measure( cl1, cl2 );
	}
	public double propertyMeasure( Object pr1, Object pr2 ) throws Exception{
	    return measure( pr1, pr2 );
	}
	public double individualMeasure( Object id1, Object id2 ) throws Exception{
	    return measure( id1, id2 );
	}
    }

    /**
     * Creation
     * (4.3) For compatibility reason with previous versions, the type is set to
     * "?*" so that the behaviour is the same.
     * In future version (5.0), this should be reverted to "**",
     * so the extractors will behave differently
     **/
    public StringDistAlignment_remove() {
	setSimilarity( new StringDistMatrixMeasure() );
	setType("?*");
	//setType("**");
    }

    /* Processing */
    public void align( Alignment alignment, Properties params ) throws AlignmentException {
	// Audun: Gets the technique listed in Properties params (e.g. Smoa)
	String f = params.getProperty("stringFunction");
	try {
	    if ( f != null ) methodName = f.trim();
	    Class<?>[] mParams = { String.class, String.class };
	    dissimilarity = StringDistances.class.getMethod( methodName, mParams );
	} catch ( NoSuchMethodException e ) {
	    throw new AlignmentException( "Unknown method for StringDistAlignment : "+params.getProperty("stringFunction"), e );
	}
//Audun: calls the align method from DistanceAlignment.java
	super.align( alignment, params );
    }

}
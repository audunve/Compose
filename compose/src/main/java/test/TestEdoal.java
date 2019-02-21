package test;

import java.io.File;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;

import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.edoal.EDOALAlignment;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import org.semanticweb.owl.align.Cell;

public class TestEdoal {
	
	public static void main(String [] args) throws AlignmentException {
		
		BasicAlignment alignment = null;
	    AlignmentParser aparser  = new AlignmentParser(0);
	    
	  //File eqReferenceAlignmentFile = new File("./files/ESWC_ATMONTO_AIRM/v2-19102018/Equivalence/ReferenceAlignment-EQ.rdf");
	    File edoalFile = new File("./files/EDOAL/gbo-gmo.edoal");
	    
	    alignment = (BasicAlignment) aparser.parse(edoalFile.toURI().toString());
	    
	    System.out.println("The edoal alignment contains: " + alignment.nbCells() + " cells");
		
	    for (Cell c : alignment) {
	    	System.out.println(c.getObject1AsURI() + " : " + c.getObject2AsURI());
	    
	    }
	    
	    
		
	}

}

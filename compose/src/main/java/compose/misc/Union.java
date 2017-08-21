package compose.misc;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Cell;

import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.BasicRelation;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.parser.AlignmentParser;

/**
 * @author audunvennesland
 * 28. mar. 2017 
 */
public class Union {
	
	//Join: any pair which is in only one alignment is preserved.
	//Meet: any pair which is in only one alignment is discarded.
	//Diff: any pair which is only in the first alignment is preserved.
	public static Alignment unionize (File alignmentFile1, File alignmentFile2, File alignmentFile3) throws AlignmentException {
	
	AlignmentParser parser = new AlignmentParser();
	BasicAlignment a1 = (BasicAlignment)parser.parse(alignmentFile1.toURI().toString());
	BasicAlignment a2 = (BasicAlignment)parser.parse(alignmentFile2.toURI().toString());
	BasicAlignment a3 = (BasicAlignment)parser.parse(alignmentFile3.toURI().toString());

	BasicAlignment union_a1_a2 = (BasicAlignment)(a1.join(a2));
	
	BasicAlignment union_a1_a2_clone = (BasicAlignment)(union_a1_a2.clone());
	
	BasicAlignment union_a1_a2_a3 = (BasicAlignment)(union_a1_a2_clone.join(a3));

	return union_a1_a2_a3;
}
	
	

	public static void main(String[] args) throws AlignmentException, IOException {
		
		File a1 = new File("./files/ER2017/302303/302-303-logmap_norm.rdf");
		File a2 = new File("./files/ER2017/302303/302-303-aml_norm.rdf");
		File a3 = new File("./files/ER2017/302303/302-303-compose_norm.rdf");
		

		Alignment unionizedAlignment = (BasicAlignment) unionize(a1, a2, a3);

		//store the new alignment
		File unionizedAlignmentFile = new File("./files/ER2017/302303/union.rdf");

		PrintWriter writer = new PrintWriter(
				new BufferedWriter(
						new FileWriter(unionizedAlignmentFile)), true); 
		AlignmentVisitor renderer = new RDFRendererVisitor(writer);

		unionizedAlignment.render(renderer);
		writer.flush();
		writer.close();
	
	}

}

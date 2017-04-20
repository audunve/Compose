package misc;

import java.util.Stack;

import org.semanticweb.owl.align.Alignment;

import fr.inrialpes.exmo.align.impl.URIAlignment;

/**
 * @author audunvennesland
 * 10. apr. 2017 
 */
public class StackExample {
	
	public static void main(String[] args) {
		
		Alignment a1 = new URIAlignment();
		Alignment a2 = new URIAlignment();
		Alignment a3 = new URIAlignment();
		Alignment a4 = new URIAlignment();
		Alignment a5 = new URIAlignment();
		
		Stack<Alignment> st = new Stack<Alignment>();
		
		st.push(a1);
		st.push(a2);
		st.push(a3);
		st.push(a4);
		st.push(a5);
		
		while (!st.isEmpty()) {
			System.out.println(st.pop());
		}
		
	}

}

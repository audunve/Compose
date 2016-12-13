package no.ntnu.idi.compose.Preprocess;

import java.util.Iterator;

public class NGramIterator implements Iterator<String> {

    String[] words;
    int pos = 0, n;

    public NGramIterator(int n, String str) {
        this.n = n;
        words = str.split(" ");
    }

    public boolean hasNext() {
        return pos < words.length - n + 1;
    }

    public String next() {
        StringBuilder sb = new StringBuilder();
        for (int i = pos; i < pos + n; i++)
            sb.append((i > pos ? " " : "") + words[i]);
        pos++;
        return sb.toString();
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
    
    public static void main(String[] args) {
    	String test = "Chair_PCI";
    	
    	NGramIterator itr = new NGramIterator(1, test);
    	
    	while (itr.hasNext()) {
    		System.out.println(itr.next());
    	}
    	
    }
}


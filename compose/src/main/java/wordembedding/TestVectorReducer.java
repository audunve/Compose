package wordembedding;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class TestVectorReducer {



	public static void main(String[] args) throws FileNotFoundException {

		File vectorFile = new File("./files/skybrary_trained_reduced.txt");

		//create a vectormap from initialVectorFile
		Map<String, ArrayList<Double>> initialVectorMap = new HashMap<String, ArrayList<Double>>();
		initialVectorMap = VectorExtractor.createVectorMap(vectorFile);
		System.out.println("Initial vector map contains " + initialVectorMap.size() + " entries");
		
		for (Entry<String, ArrayList<Double>> e : initialVectorMap.entrySet()) {
			String word = e.getKey();
			ArrayList<Double> arraylist = e.getValue();
			if (arraylist.size() < 300) {
				System.out.println("Only " + arraylist.size() + " vectors are associated with " + word);
			} else {
				System.out.println(arraylist.size() + " vectors are associated with " + word);
			}
		}
	}

}

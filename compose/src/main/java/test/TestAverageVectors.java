package test;

import java.util.ArrayList;

public class TestAverageVectors {
	
	public static void main(String[] args) {
		
		double vector1 = 0.40038517;
		double vector2 = -0.1624828;
//		double vector3 = 0.46895882;
//		double vector4 = 0.0026595138;
//		double vector5 = 1.5563283;
//		double vector6 = 0.1524737;
//		double vector7 = 0.050093103;
		
		ArrayList<Double> vectors = new ArrayList<Double>();
		vectors.add(vector1);
		vectors.add(vector2);
//		vectors.add(vector3);
//		vectors.add(vector4);
//		vectors.add(vector5);
//		vectors.add(vector6);
//		vectors.add(vector7);
		
		double avg = averageVectors(vectors);
		
		System.out.println("The average is " + avg);
		
		
	}
	
	/**
	 * Averages a set of vectors
	 * @param inputVectors ArrayList holding a set of input vectors
	 * @return an average of all input vectors
	 */
	public static double averageVectors (ArrayList<Double> inputVectors) {

		int num = inputVectors.size();

		double sum = 0;

		for (Double d : inputVectors) {
			sum+=d;
		}

		double averageVectors = sum/num;

		return averageVectors;
	}

}

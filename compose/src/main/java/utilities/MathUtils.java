package utilities;

import java.util.ArrayList;
import java.util.Collections;

/**
 * @author audunvennesland
 * 26. okt. 2017 
 */
public class MathUtils {
	
	public static void main(String[] args) {
		double high = 0.30;
		double low = 0.58;
		
		System.out.println("The result from running the Euzenat sigmoid function is " + sigmoidEuzenat(high));
		System.out.println("The result from running the RiMOM sigmoid function is " + sigmoidRiMom(high));
		System.out.println("The result from running the regular sigmoid function is " + sigmoid(high));
		
		int subConcepts = 1;
		int totalConcepts = 8;
		double ic = computeInformationContent(subConcepts, totalConcepts);
		System.out.println("The information content (IC) is " + ic);
		
		int props1 = 6;
		int props2 = 1;
		int props3 = 1;
		int props4 = 1;
		int props5 = 1; 
		int props6 = 1;
		
		ArrayList<Double> propsList = new ArrayList<Double>();
		propsList.add((double) props1);
		propsList.add((double) props2);
		propsList.add((double) props3);
		propsList.add((double) props4);
		propsList.add((double) props5);
		propsList.add((double) props6);
		
		double normalisedProps = normalise(propsList);
		System.out.println("The normalised properties for ontology 1 and 2 is " + normalisedProps);
		
	}
	
	/**
	 * Rounds a double to a specified number of digits after the decimal point
	 * @param value the double to be rounded
	 * @param places number of digits after decimal point
	 * @return rounded double
	 */
	public static double round(double value, int places) {
		if (places < 0) throw new IllegalArgumentException();

		long factor = (long) Math.pow(10, places);
		value = value * factor;
		long tmp = Math.round(value);
		return (double) tmp / factor;
	}
	
	public static double sigmoidRiMom(double x) {
	    return (1/( 1 + Math.pow(Math.E,(-5*(x-0.5)))));
	  }
	
	public static double sigmoidEuzenat(double x) {
	    return (1/( 1 + Math.pow(Math.E,(-12*(x-0.5)))));
	  }
	
	public static double sigmoid(double x) {
	    return (1/( 1 + Math.pow(Math.E,(-1*x))));
	  }
	
	public static double computeInformationContent(int subConcepts, int totalConcepts) {
		return 1-((Math.log((double)subConcepts + 1)) / Math.log((double)totalConcepts));
	}
	
	public static double computeListAverage(ArrayList<Double> list) {
		double sum = 0;
		
		for (Double d : list) {
			sum+=d;
		}
		
		return sum / list.size();
	}
	
	public static double normalise(ArrayList<Double> properties) {
		
		ArrayList<Double> normalisedPropValues = new ArrayList<Double>();
		
//		double max = Collections.max(properties);
//		double min = Collections.min(properties);
		
		double max = 10.0;
		double min = 0;
		
		
		double thisPropValue = 0;
		double normalisedProp = 0;
		
		for (Double d : properties) {
			thisPropValue = d;
			normalisedProp = (d-min) / (max-min);
			normalisedPropValues.add((d-min)/(max-min));
			System.out.println("Normalising " + d + " to :" + normalisedProp);

		}
		
		return computeListAverage(normalisedPropValues);
	}

}

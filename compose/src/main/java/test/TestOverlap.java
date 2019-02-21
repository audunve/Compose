package test;

import java.util.HashSet;
import java.util.Set;

import org.simmetrics.SetMetric;
import org.simmetrics.metrics.OverlapCoefficient;

public class TestOverlap {
	
	public static void main(String[] args) {
		
		Set<String> set1 = new HashSet<String>();
		set1.add("audun");
		set1.add("tone");
		set1.add("gabriel");
		set1.add("jesper");
		
		Set<String> set2 = new HashSet<String>();
		set2.add("irene");
		set2.add("tone");
		set2.add("gabriel");
		
		SetMetric<String> metric = new OverlapCoefficient<>();
		
		double result = metric.compare(set1, set2);
		System.out.println("The result is " + result);
		
	}
	
	
	/**
	 * The overlap coefficient measures the overlap between two sets. The similarity is defined as the size 
	 * of the intersection divided by the smaller of the size of the two sets.
	 * @param set1
	 * @param set2
	 * @return
	   Jan 27, 2019
	 */
	public static double overlapSim (Set<String> set1, Set<String> set2) {
		double overlap = 0;
		int intersection = 0;
		
		for (String s1 : set1) {
			for (String s2 : set2) {
				if (s1.equals(s2)) {
					intersection += 1;
				}
			}
		}
		
		if (set1.size() != 0 && set2.size() != 0) {
		if (set1.size() > set2.size()) {
			overlap = intersection / set2.size();
			System.out.println("From first loop: Overlap is " + overlap);
		} else if (set2.size() > set1.size() || set2.size() == set1.size()) {
			overlap = intersection / set1.size();
			System.out.println("From second loop: Overlap is " + overlap);
		} 
		} else {
			overlap = 0;
		}
		
		System.out.println("From return: Overlap is " + overlap);
		return overlap;
	}

}

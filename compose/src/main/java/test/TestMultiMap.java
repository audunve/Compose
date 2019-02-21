package test;

import java.util.Collections;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;

public class TestMultiMap {
	
	public static void main(String[] args) {
		
		Multimap<String, Double> map = SortedSetMultimap.create();
		map.put("=", 0.5);
		map.put("<", 0.75);
		map.put(">", 1.0);
		
		System.out.println(map);
		

		
	}
	

}

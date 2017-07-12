package misc;

import java.util.HashSet;
import java.util.Set;

/**
 * @author audunvennesland
 * 21. jun. 2017 
 * 
 */
class LongestCommonSubString {
	


public static Set<String> longestCommonSubstrings(String s, String t) {
    int[][] table = new int[s.length()][t.length()];
    int longest = 0;
    Set<String> result = new HashSet<>();

    for (int i = 0; i < s.length(); i++) {
        for (int j = 0; j < t.length(); j++) {
            if (s.charAt(i) != t.charAt(j)) {
                continue;
            }

            table[i][j] = (i == 0 || j == 0) ? 1
                                             : 1 + table[i - 1][j - 1];
            if (table[i][j] > longest) {
                longest = table[i][j];
                result.clear();
            }
            if (table[i][j] == longest) {
                result.add(s.substring(i - longest + 1, i + 1));
            }
        }
    }
    return result;
}

public static void main(String[] args) {
	
	//Report and MedicalIndication
	
	Set<String> lcs = longestCommonSubstrings("Report", "TechnicalReport");
	
	for (String s : lcs) {
		if (s.length() > 3) {
		System.out.println(s);
		}
	}
}
}

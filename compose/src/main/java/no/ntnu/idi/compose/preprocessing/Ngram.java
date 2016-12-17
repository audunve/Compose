package no.ntnu.idi.compose.preprocessing;

import java.util.*;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.*;

public class Ngram {

public static List<String> ngrams(int n, String str) {
    List<String> ngrams = new ArrayList<String>();
    String[] words = str.split(" ");
    for (int i = 0; i < words.length - n + 1; i++)
        ngrams.add(concat(words, i, i+n));
    return ngrams;
}

public static String concat(String[] words, int start, int end) {
    StringBuilder sb = new StringBuilder();
    for (int i = start; i < end; i++)
        sb.append((i > start ? " " : "") + words[i]);
    return sb.toString();
}

public static void main(String[] args) throws IOException {
String token1 = "Chair_PCC";

List<String> abbList = ngrams(2,token1);

System.out.println("Number of abbreviations = " + abbList.size());

}
}
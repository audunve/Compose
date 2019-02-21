package test;

public class RegEx {
	
	public static void main(String[] args) {
		String test = "This is a test (test)";
		
		System.out.println(test.replaceAll("[^A-Za-z]+", ""));
	}

}

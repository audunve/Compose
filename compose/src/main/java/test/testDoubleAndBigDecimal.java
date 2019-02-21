package test;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

public class testDoubleAndBigDecimal {
	
	public static void main(String[] args) {
		
		double d1 = 1.175372254031583E-4;
		
		ArrayList<Double> scores = new ArrayList<Double>();
		scores.add(7.455674295771417E-5);
		scores.add(6.991401537426676E-7);
		scores.add(1.175372254031583E-4);
		
		
		
		
		double d2 = 6.991401537426676E-7;
		
		float l = (float) d2;
		
		System.out.println(Double.parseDouble("6.991401537426676E-7"));
		
		System.out.println("Printing float: " + l);
		
		BigDecimal b = new BigDecimal(d2);
		b.setScale(2, RoundingMode.HALF_UP);
		
		
		System.out.println("b is " + b);
		
		System.out.println("The relation has an initial strength of " + 0.3 + " , and after the Harmony value is considered it has a strength of " + 0.3*d1);
				
		
		
		
		
	}

}

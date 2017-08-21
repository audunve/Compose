package compose.misc;

/**
 * @author audunvennesland
 * 19. aug. 2017 
 */
public class AlignmentOperations {
	
	/**
	 * Increases an input value by 12 percent
	 * @param inputStrength the strength/confidence for a correspondence to be increased
	 * @return a value 12 percent higher than its input value
	 */
	public static double increaseCellStrength(double inputStrength, double addition) {

		double newStrength = inputStrength + (inputStrength * (addition/100));

		if (newStrength > 1.0) {
			newStrength = 1.0;
		}

		return newStrength;
	}
	
	/**
	 * Decreases an input value by 12 percent
	 * @param inputStrength the strength/confidence for a correspondence to be decreased
	 * @return a value 12 percent lower than its input value
	 */
	public static double reduceCellStrength(double inputStrength, double reduction) {

		double newStrength = inputStrength - (inputStrength * (reduction/100));

		if (newStrength > 1.0) {
			newStrength = 1.0;
		}

		return newStrength;
	}

}

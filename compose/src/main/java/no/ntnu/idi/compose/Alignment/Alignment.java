package no.ntnu.idi.compose.Alignment;

import java.util.Map;

/**
 * @author audunvennesland
 * @version 1.0
 * @created 21-apr-2016 10:34:50
 */
public interface Alignment {

	/**
	 * 
	 * @param mappings
	 */
	public Alignment formatAlignment(Map mappings);

	/**
	 * 
	 * @param alignment
	 */
	public void storeAlignment(Alignment alignment);

}
package no.ntnu.idi.compose.Alignment;

import java.util.Map;

/**
 * @author audunvennesland
 * @version 1.0
 * @created 21-apr-2016 10:34:51
 */
public abstract class EdoalAligner implements Alignment {

	public EdoalAligner(){

	}

	@Override
	public void finalize() throws Throwable {

	}

	/**
	 * 
	 * @param mappings
	 */
	public Alignment formatAlignment(Map mappings){
		return null;
	}

	/**
	 * 
	 * @param alignment
	 */
	public void storeAlignment(Alignment alignment){

	}

}
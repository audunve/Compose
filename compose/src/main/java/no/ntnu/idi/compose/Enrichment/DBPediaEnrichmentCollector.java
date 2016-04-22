package no.ntnu.idi.compose.Enrichment;

import java.util.HashMap;
import java.util.Map;

/**
 * @author audunvennesland
 * @version 1.0
 * @created 21-apr-2016 10:34:51
 */
public class DBPediaEnrichmentCollector implements EnrichmentCollector {

	public DBPediaEnrichmentCollector(){

	}

	@Override
	public void finalize() throws Throwable {

	}

	/**
	 * Using an ontology concept id as key and possible search keywords as value for
	 * the Map keywords in the input parameter. Further the URI to the web service (e.
	 * g. DBPedia Spotlight) and a confidence value stating the confidence for the KB
	 * annotation.
	 * 
	 * The return value is a map where the key is ontology concept id and the return
	 * value is the instances as value in the map.
	 * 
	 * @param keywords
	 * @param URI
	 * @param confidence
	 */
	public Map FindEnrichment(Map keywords, String URI, double confidence){

		Map enrichments = new HashMap();
		return enrichments;
		
	}

}
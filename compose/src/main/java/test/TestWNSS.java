package test;

import WordNetSemanticSimilarity.DataBaseConnection.DataBaseConnection;
import WordNetSemanticSimilarity.ICApproach.ICApproachException;
import WordNetSemanticSimilarity.ICApproach.ICComputingMethod;
import WordNetSemanticSimilarity.ICApproach.ICMeasure;
import WordNetSemanticSimilarity.ICApproach.WordNetICApproaches;
import WordNetSemanticSimilarity.TaxonomicApproach.TaxonomicMeasures;
import WordNetSemanticSimilarity.WordNetTreatment.BaseName;
import WordNetSemanticSimilarity.WordNetTreatment.Depth;
import WordNetSemanticSimilarity.WordNetTreatment.Hyponyms;
import WordNetSemanticSimilarity.WordNetTreatment.WordNetProcessing;
import WordNetSemanticSimilarity.WordNetTreatment.WordNetVersion;
import WordNetSemanticSimilarity.WordNetTreatment.WordnetTreatmentException;
import net.didion.jwnl.JWNL;
import net.didion.jwnl.dictionary.Dictionary;
import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.POS;
import rita.RiWordNet;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

public class TestWNSS {
	
    public static void main(String[] args) throws JWNLException, SQLException, WordnetTreatmentException, FileNotFoundException, TransformerConfigurationException, TransformerException, ParserConfigurationException, SAXException, IOException, net.didion.jwnl.JWNLException, ICApproachException
    {
    	
        String pathFilePropertiesWordNet="file_properties.xml";
        String pathWordNetDictFolder="/Users/audunvennesland/Documents/PhD/Development/WordNet/WordNet-3.0/dict";
        WordNetVersion version=WordNetVersion.WordNet2dot1;        
        DataBaseConnection dbc0=new DataBaseConnection("localhost",3306,"root","fo2Aprat") ;
        WordNetProcessing.initialize(dbc0);              
        DataBaseConnection dbc=new DataBaseConnection("localhost",3306,BaseName.name,"root","fo2Aprat") ;
        dbc.openConnection(); 
        WordNetProcessing WP=new WordNetProcessing(dbc,pathFilePropertiesWordNet,pathWordNetDictFolder,version);                    
        WP.LoadWordNetInDataBase();                   
        dbc.closeConnection();   
//        ////////////////////////////////////////////////////////////////////////
//        //              Wordnet Semantic Similarity Taxonomic Measures
//        //////////////////////////////////////////////////////////////////////// 
//        String pathFilePropertiesWordNet="file_properties.xml";
//        String pathWordNetDictFolder="/Users/audunvennesland/Documents/PhD/Development/WordNet/WordNet-3.0/dict";
//		
//        DataBaseConnection dbc=new DataBaseConnection("localhost",3306,BaseName.name,"root","fo2Aprat") ;
//        dbc.openConnection();             
//        WordNetVersion version=WordNetVersion.WordNet3dot0;
//        POS pos=POS.NOUN;
//        WordNetProcessing WP=new WordNetProcessing(dbc,pathFilePropertiesWordNet,pathWordNetDictFolder,version,pos);                    
//        WordNetICApproaches  wic= new WordNetICApproaches(WP);
//        ICComputingMethod.Zhou2008.setHyponyms(Hyponyms.HypDepthWH);
//        ICComputingMethod.Zhou2008.setDepth(Depth.depthWH);
//        double ss=wic.SemanticSimilarityICComputing("car","wheel", ICComputingMethod.Zhou2008, ICMeasure.JiangConrath1997);              
//        System.out.println("ss= "+ss);
//       
//   
//        System.out.println("ss="+ss);        
    }

}

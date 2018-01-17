package compose.ui;


import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.RefineryUtilities;



public class BarChart extends JFrame {

    public BarChart( String applicationTitle , String chartTitle , double precisionValue, double recallValue, double fMeasureValue) {
        super( applicationTitle );        
        JFreeChart barChart = ChartFactory.createBarChart(
           chartTitle,           
           "Category",            
           "Score",            
           createDataset(precisionValue, recallValue, fMeasureValue),          
           PlotOrientation.VERTICAL,           
           true, true, false);
           
        ChartPanel chartPanel = new ChartPanel( barChart );        
        chartPanel.setPreferredSize(new java.awt.Dimension( 560 , 367 ) );        
        setContentPane( chartPanel ); 
     }

    
    private CategoryDataset createDataset(double precisionValue, double recallValue, double fMeasureValue) {
        final String precision = "Precision";        
        final String recall = "Recall";        
        final String fMeasure = "F-Measure";        

        final DefaultCategoryDataset dataset = 
        new DefaultCategoryDataset( );  

        dataset.addValue( precisionValue , "Precision" , precision );        
        dataset.addValue( recallValue , "Recall" , recall );        
        dataset.addValue( fMeasureValue , "F-Mmeasure" , fMeasure );           

        return dataset; 
     }

   /* public static void main( String[ ] args ) {
    	
    	double prec = 88.0;
    	double rec = 65.0;
    	double f = 72.0;
    	
        BarChart chart = new BarChart("Evaluation", 
           "", prec, rec, f);
        chart.pack( );        
        RefineryUtilities.centerFrameOnScreen( chart );        
        chart.setVisible( true ); 
     }*/
}
package no.ntnu.idi.compose.Loading.demo;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JApplet;
import javax.swing.JFrame;

import org.jgraph.JGraph;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphConstants;
import org.jgrapht.DirectedGraph;
import org.jgrapht.ListenableGraph;
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.graph.ListenableDirectedGraph;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import no.ntnu.idi.compose.Loading.GraphLoader;

import org.jgrapht.graph.DefaultEdge;

/**
 * A demo applet that shows how to use JGraph to visualize JGraphT graphs.
 *
 * @author Barak Naveh
 *
 * @since Aug 3, 2003
 */
public class JGraphAdapterDemo extends JApplet {
    private static final Color     DEFAULT_BG_COLOR = Color.decode( "#FAFBFF" );
    private static final Dimension DEFAULT_SIZE = new Dimension( 530, 320 );

    // 
    private JGraphModelAdapter m_jgAdapter;

    /**
     * @see java.applet.Applet#init().
     */
    public void init(  ) {
        // create a JGraphT graph
        ListenableGraph g = new ListenableDirectedGraph( DefaultEdge.class );
        
        File file1 = new File("/Users/audunvennesland/Documents/PhD/Ontologies/TestOntologiesTransport/TestTransport2.owl");
		//GraphLoader gl = new GraphLoader();
		try {
			
			DirectedGraph<String, DefaultEdge> owlGraph = GraphLoader.createOWLGraph(file1);
			// create a visualization using JGraph, via an adapter
	        m_jgAdapter = new JGraphModelAdapter( owlGraph );

	        JGraph jgraph = new JGraph( m_jgAdapter );

	        adjustDisplaySettings( jgraph );
	        getContentPane(  ).add( jgraph );
	        resize( DEFAULT_SIZE );
	        
		} catch (OWLOntologyCreationException e) {
			// FIXME Auto-generated catch block
			e.printStackTrace();
		}


/*        // position vertices nicely within JGraph component
        positionVertexAt( "v1", 130, 40 );
        positionVertexAt( "v2", 60, 200 );
        positionVertexAt( "v3", 310, 230 );
        positionVertexAt( "v4", 380, 70 );*/

    }


    private void adjustDisplaySettings( JGraph jg ) {
        jg.setPreferredSize( DEFAULT_SIZE );

        Color  c        = DEFAULT_BG_COLOR;
        String colorStr = null;

        try {
            colorStr = getParameter( "bgcolor" );
        }
         catch( Exception e ) {}

        if( colorStr != null ) {
            c = Color.decode( colorStr );
        }

        jg.setBackground( c );
    }


    private void positionVertexAt( Object vertex, int x, int y ) {
        DefaultGraphCell cell = m_jgAdapter.getVertexCell( vertex );
        Map              attr = cell.getAttributes(  );
       // Rectangle        b    = GraphConstants.getBounds( attr );

        //GraphConstants.setBounds( attr, new Rectangle( x, y, b.width, b.height ) );

        Map cellAttr = new HashMap(  );
        cellAttr.put( cell, attr );
       // m_jgAdapter.edit( cellAttr, null, null, null, null );
    }
}



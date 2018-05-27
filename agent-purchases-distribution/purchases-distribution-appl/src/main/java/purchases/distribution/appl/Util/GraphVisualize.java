package purchases.distribution.appl.Util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Rectangle2D;

import java.util.HashMap;
import java.util.Map;
import javax.swing.JApplet;
import javax.swing.undo.UndoableEdit;

import org.jgraph.JGraph;
import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.ConnectionSet;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.ParentMap;

import org.jgrapht.ListenableGraph;
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.graph.ListenableDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import purchases.distribution.appl.GraphImplement.City;
import purchases.distribution.appl.GraphImplement.MyWeightedEdge;

@SuppressWarnings("serial")
public class GraphVisualize extends JApplet {

    private static final Color     DEFAULT_BG_COLOR = Color.decode( "#FAFBFF" );
    private static final Dimension DEFAULT_SIZE = new Dimension( 530, 320 );

    @SuppressWarnings("rawtypes")
    private JGraphModelAdapter jgAdapter;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    /**
     * Initialize the applet @see java.applet.Applet#init().
     */
    public void init(  ) {
        // create a JGraphT graph
        City<String,MyWeightedEdge> g = new City<>( MyWeightedEdge.class );

        // create a visualization using JGraph, via an adapter
        jgAdapter = new JGraphModelAdapter( g );

        JGraph jgraph = new JGraph( jgAdapter );

        // add some hard-coded sample data (graph manipulated via JGraphT)
        //First the vertices
        g.addVertex( "v1" );
        g.addVertex( "v2" );
        g.addVertex( "v3" );
        g.addVertex( "v4" );

        //...then the edges
        g.addEdge( "v1", "v2" );
        g.addEdge( "v2", "v3" );
        g.addEdge( "v3", "v1" );
        g.addEdge( "v4", "v3" );

        // a ConnectionSet is another way of storing edges. Each connection stores information about the edge's nodes and its origin/source
        // This ConnectionSet is necessary for the visualization
        ConnectionSet cs=new ConnectionSet();
        cs.connect("v1", "v2", true);
        cs.connect("v2", "v3", true);
        cs.connect("v3", "v1", true);
        cs.connect("v4", "v3", true);

        adjustDisplaySettings( jgraph );
        getContentPane(  ).add( jgraph );
        resize( DEFAULT_SIZE );

        // position vertices nicely within JGraph component
        positionVertexAt( "v1", 130, 40, cs );
        positionVertexAt( "v2", 60, 200, cs );
        positionVertexAt( "v3", 310, 230, cs );
        positionVertexAt( "v4", 380, 70, cs );
    }

    /**
     * a method for general settings of the applet
     * @param jg
     */
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


    @SuppressWarnings({ "rawtypes", "unchecked" })
    /**
     * Set a position for the vertices
     */
    private void positionVertexAt( Object vertex, int x, int y , ConnectionSet cs) {
        DefaultGraphCell cell = jgAdapter.getVertexCell( vertex );
        AttributeMap vertexAttributes=cell.getAttributes();
        Rectangle2D rect=vertexAttributes.createRect(new Point(x,y),50);

        Map attr = cell.getAttributes();

        GraphConstants.setBounds( attr,rect);

        Map cellAttr = new HashMap(  );
        cellAttr.put( cell, attr );
        UndoableEdit[] e=null;

        ParentMap m=new ParentMap();
        jgAdapter.edit(cellAttr, cs, m, e);
    }
}
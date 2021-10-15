package gr.csd.uoc.hy463.themis.linkAnalysis.graph;


import org.jgrapht.*;
import org.jgrapht.graph.*;

import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.swing.mxGraphComponent;

import org.jgrapht.ext.JGraphXAdapter;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.Map.Entry;

public class VisualGraph
    extends
    JApplet
{
    private static final long serialVersionUID = 2202072534703043194L;

    private static final Dimension DEFAULT_SIZE = new Dimension(1920,1080);

    private JGraphXAdapter<String, DefaultEdge> jgxAdapter;
    Graph graph; 
    
    public VisualGraph(Graph graph) {
    	this.graph = graph; 
    	
    }

    public void init()
    {
        // create a JGraphT graph
        ListenableGraph<String, DefaultEdge> g =
            new DefaultListenableGraph<>(new DefaultDirectedGraph<>(DefaultEdge.class));

        // create a visualization using JGraph, via an adapter
        jgxAdapter = new JGraphXAdapter<>(g);

        setPreferredSize(DEFAULT_SIZE);
        mxGraphComponent component = new mxGraphComponent(jgxAdapter);
        component.setConnectable(false);
        component.getGraph().setAllowDanglingEdges(false);
        getContentPane().add(component);
        resize(DEFAULT_SIZE);
        
        Map<Node, Map<Node, Integer>> map = graph.getAdjacencyList(); 
        
        for (Node key : map.keySet()) 
        {
        	g.addVertex(key.getId()); 
        	for(Entry<Node, Integer> edge : map.get(key).entrySet()) {
        		g.addVertex(edge.getKey().getId()); 
        		g.addEdge(key.getId(), edge.getKey().getId()); 
        	}
           	
        }


        // positioning via jgraphx layouts
        mxCircleLayout layout = new mxCircleLayout(jgxAdapter);

        // center the circle
        int radius = 100;
        layout.setX0((DEFAULT_SIZE.width / 2.0) - radius);
        layout.setY0((DEFAULT_SIZE.height / 2.0) - radius);
        layout.setRadius(radius);
        layout.setMoveCircle(true);

        layout.execute(jgxAdapter.getDefaultParent());
        // that's all there is to it!...
    }
}
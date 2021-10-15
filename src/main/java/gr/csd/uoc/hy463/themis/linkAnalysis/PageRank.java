package gr.csd.uoc.hy463.themis.linkAnalysis;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import gr.csd.uoc.hy463.themis.config.Config;
import gr.csd.uoc.hy463.themis.linkAnalysis.graph.Graph;
import gr.csd.uoc.hy463.themis.linkAnalysis.graph.Node;

public class PageRank {
	HashMap<Node, Double> realRanking = new HashMap<Node, Double>();

	private Graph graph; 


	public PageRank(Graph graph) {
		this.graph = graph; 
	}

	public HashMap<Node, Double> rank(int iterations, double dampingFactor){	
		Map<Node, Map<Node, Integer>> map = graph.getAdjacencyList(); 
		
		HashMap<Node, Double> lastRanking = new HashMap<Node, Double>();
		
		Double startRank = 1.0/map.size(); 
		
		for (Entry<Node, Map<Node, Integer>> key : map.entrySet()) {
			lastRanking.put(key.getKey(), startRank);
			Map<Node, Integer> nodes = key.getValue(); 
			
			for(Entry<Node, Integer> entry : nodes.entrySet()) {
				
				lastRanking.put(entry.getKey(), startRank); 
				
			}
		}
		double dampingFactorComplement = 1.0 - dampingFactor;

		for (int times = 0; times < iterations; times++) {
			for (Entry<Node, Map<Node, Integer>> key : map.entrySet()) {
				//				System.out.println(key.getId()+" "+startRank+" "+key.getNumberOfOutEdges()); 
				Map<Node, Integer> nodes = key.getValue(); 
				double totalWeight = 0;
				for(Entry<Node, Integer> entry : nodes.entrySet()) { 
					totalWeight += lastRanking.get(entry.getKey())/entry.getValue(); 
					realRanking.put(entry.getKey(), startRank);
				}
				Double nextRank = dampingFactorComplement + (dampingFactor * totalWeight);
				lastRanking.replace(key.getKey(), nextRank);
				realRanking.replace(key.getKey(), nextRank); 
				
			}
			
			
		}

		return realRanking; 

	}
	
	
	public void writeScores(Config config) throws IOException {
		Properties properties = new Properties();

		for (Entry<Node, Double> entry : realRanking.entrySet()) {
		    properties.put(entry.getKey().getId(), String.valueOf(entry.getValue()));
		}

		properties.store(new FileOutputStream(config.getPageRankScoreFile()), null);
	}




}
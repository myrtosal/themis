/*
 * themis - A fair search engine for scientific articles
 *
 * Currently over the Semantic Scholar Open Research Corpus
 * http://s2-public-api-prod.us-west-2.elasticbeanstalk.com/corpus/
 *
 * Collaborative work with the undergraduate/graduate students of
 * Information Retrieval Systems (hy463) course
 * Spring Semester 2020
 *
 * -- Writing code during COVID-19 pandemic times :-( --
 *
 * Aiming to participate in TREC 2020 Fair Ranking Track
 * https://fair-trec.github.io/
 *
 * Computer Science Department http://www.csd.uoc.gr
 * University of Crete
 * Greece
 *
 * LICENCE: TO BE ADDED
 *
 * Copyright 2020
 *
 */
package gr.csd.uoc.hy463.themis.retrieval.models;

import gr.csd.uoc.hy463.themis.config.Config;
import gr.csd.uoc.hy463.themis.indexer.Indexer;
import gr.csd.uoc.hy463.themis.indexer.model.DocInfoEssential;
import gr.csd.uoc.hy463.themis.indexer.model.DocInfoFull;
import gr.csd.uoc.hy463.themis.indexer.model.DocInfoEssential.PROPERTY;
import gr.csd.uoc.hy463.themis.retrieval.QueryTerm;
import gr.csd.uoc.hy463.themis.retrieval.models.ARetrievalModel.RESULT_TYPE;
import gr.csd.uoc.hy463.themis.utils.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Map.Entry;

/**
 * Implementation of the OkapiBM25 retrieval model
 *
 * @author Panagiotis Papadakos <papadako at ics.forth.gr>
 */
public class OkapiBM25 extends ARetrievalModel {
	private Indexer index; 
	private double k;
	private double b; 
	private double delta; 
	private Config __CONFIG__; 
	public OkapiBM25(Indexer index, double k, double b, double delta) {
		super(index);
		try {
			__CONFIG__ = new Config();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		this.index = index; 
		if (k < 0) {
			throw new IllegalArgumentException("Negative k1 = " + k);
		}

		if (b < 0 || b > 1) {
			throw new IllegalArgumentException("Invalid b = " + b);
		}

		if (delta < 0) {
			throw new IllegalArgumentException("Invalid delta = " + delta);
		}
		this.delta = delta; 
		this.k = k; 
		this.b = b; 
	}

	@Override
	public List<?> getRankedResults(List<QueryTerm> query, RESULT_TYPE type) throws IOException {
		LinkedHashMap<Object, Double> score = new LinkedHashMap<Object, Double>();
		if(type == RESULT_TYPE.ESSENTIAL) {
			List<List<DocInfoEssential>> results = index.getDocInfoEssentialForTerms(query); 
			for(List<DocInfoEssential> ess : results) {
				for(DocInfoEssential doc : ess) {
					double tf = (double) Double.parseDouble(doc.getProperty(PROPERTY.TF).toString()); 
					double df = (double) Double.parseDouble(doc.getProperty(PROPERTY.DF).toString());
					double rank = rank(doc, tf, df); 
					
					if(score.containsKey(doc)) {
						score.replace(doc, score.get(doc)+rank); 
					}else {
						
						score.put(doc, rank); 
					}	
					doc.setProperty(PROPERTY.SCORE, (double) rank);
				}
			}

			List<?> sortedResults = sort(score); 

			return sortedResults; 
		}else if(type == RESULT_TYPE.FULL) {
			List<List<DocInfoFull>> results = index.getDocInfoFullTerms(query); 
			for(List<DocInfoFull> ess : results) {
				for(DocInfoFull doc : ess) {
					double tf = (double) Double.parseDouble(doc.getProperty(PROPERTY.TF).toString()); 
					double df = (double) Double.parseDouble(doc.getProperty(PROPERTY.DF).toString());
					double rank = rank(doc, tf, df); 
					if(score.containsKey(doc)) {
						score.replace(doc, score.get(doc)+rank); 
					}else {
						score.put(doc, rank); 
					}
					doc.setProperty(PROPERTY.SCORE, (double) rank);
				}
			}

			List<?> sortedResults = sort(score); 
			return sortedResults; 
		}else if(type == RESULT_TYPE.PLAIN) {
			List<List<DocInfoEssential>> results = index.getDocInfoEssentialForTerms(query); 
			List<DocInfoFull> plain = new ArrayList<DocInfoFull>(); 
			for(List<DocInfoEssential> docs : results) {
				if(!plain.containsAll(docs))
					plain.addAll(index.getPlain(docs)); 
			}
			return plain;
		}

		return null; 
	}

	@Override
	public List<?> getRankedResults(List<QueryTerm> query, RESULT_TYPE type, int topk) throws IOException {
		List<?> results =  getRankedResults(query, type); 
		if(topk>results.size() || topk<=0) return results; 
		return results.subList(0, topk); 
	}


	public double score(double freq, double docSize, double avgDocSize, int N, double n2) {
		if (freq <= 0) return 0.0;
	
		double tf = freq * (k + 1) / (freq + k * (1 - b + b * docSize / avgDocSize));
		double idf = Math.log((N - n2 + 0.5) / (n2 + 0.5));

		return (tf + delta) * idf;
	}
	public double rank(DocInfoEssential doc, double tf, double n) {
		if (tf <= 0) return 0.0;

		int N = index.getCollectionLength(); 
		double docSize = (double) Double.parseDouble(doc.getProperty(PROPERTY.LENGTH).toString()); 
		double avgDocSize = index.getAverageDocumentSize(); 
		double pageRank = Double.parseDouble(doc.getProperty(PROPERTY.PAGERANK).toString()); 
		return score(tf, docSize, avgDocSize, N, n)*__CONFIG__.getRetrievalModelWeight() + pageRank*__CONFIG__.getPagerankPublicationsWeight();
	}
	private List<Object> sort(LinkedHashMap<?, Double> docs) {
		List<Object> sorted = new ArrayList<>(); 
		List<Double> mapValues = new ArrayList<>(docs.values());
		Collections.sort(mapValues, Collections.reverseOrder());
		Iterator<Double> valueIt = mapValues.iterator();

		while (valueIt.hasNext()) {
			Double key = valueIt.next();
			for (Entry<?, Double> entry : docs.entrySet()) {
				if (Objects.equals(key, entry.getValue())) {

					if(!sorted.contains(entry.getKey())) {
						sorted.add( entry.getKey()); 
					}
				}
			}
		}
		return sorted;
	}    

}

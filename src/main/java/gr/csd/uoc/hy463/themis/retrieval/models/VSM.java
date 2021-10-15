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
import gr.csd.uoc.hy463.themis.indexer.model.DocInfoEssential.PROPERTY;
import gr.csd.uoc.hy463.themis.indexer.model.DocInfoFull;
import gr.csd.uoc.hy463.themis.retrieval.QueryTerm;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

/**
 * Implementation of the OkapiBM25 retrieval model
 *
 * @author Panagiotis Papadakos <papadako at ics.forth.gr>
 */
public class VSM extends ARetrievalModel {

	protected LinkedHashMap<Object, Double> weights;
	private Indexer index; 
	private Config __CONFIG__; 
	public VSM(Indexer index) {
		super(index);
		try {
			__CONFIG__ = new Config();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		this.index = index; 
		weights =  new LinkedHashMap<Object, Double>();
	}

	@Override
	public List<?> getRankedResults(List<QueryTerm> query, RESULT_TYPE type) throws IOException {
		
		if(type == RESULT_TYPE.ESSENTIAL) {
			List<List<DocInfoEssential>> results = index.getDocInfoEssentialForTerms(query); 
			for(List<DocInfoEssential> ess : results) {
				for(DocInfoEssential doc : ess) {
					double tf = (double) Double.parseDouble(doc.getProperty(PROPERTY.TF).toString()); 
					double idf = Math.log(index.getCollectionLength()/ (double) Double.parseDouble(doc.getProperty(PROPERTY.DF).toString()));
					double querytf = (double) Double.parseDouble(doc.getProperty(PROPERTY.QUERYTF).toString());
					double cosineSimilarity = (tf*idf)*(idf*querytf) / (Math.sqrt(Math.pow(idf*tf, 2))*Math.sqrt(Math.pow(idf*querytf, 2)));
					double pageRank = Double.parseDouble(doc.getProperty(PROPERTY.PAGERANK).toString()); 
					double score = cosineSimilarity * __CONFIG__.getRetrievalModelWeight() + __CONFIG__.getPagerankPublicationsWeight() * pageRank; 
					if(weights.containsKey(doc)) {
						weights.replace(doc, weights.get(doc)+(score)); 
					}else {
						weights.put(doc, score); 
					}	
					doc.setProperty(PROPERTY.SCORE, (double) weights.get(doc));

				}
			}

			List<?> sortedResults = sort(weights); 

			return sortedResults; 
		}
		else if(type == RESULT_TYPE.FULL) {
			List<List<DocInfoFull>> results = index.getDocInfoFullTerms(query); 
			for(List<DocInfoFull> ess : results) {
				for(DocInfoFull doc : ess) {
					double tf = (double) Double.parseDouble(doc.getProperty(PROPERTY.TF).toString()); 
					double idf = Math.log(index.getCollectionLength()/ (double) Double.parseDouble(doc.getProperty(PROPERTY.DF).toString()));
					double querytf = (double) Double.parseDouble(doc.getProperty(PROPERTY.QUERYTF).toString());
					double cosineSimilarity = (tf*idf)*(idf*querytf) / (Math.sqrt(Math.pow(idf*tf, 2))*Math.sqrt(Math.pow(idf*querytf, 2)));
					double pageRank = Double.parseDouble(doc.getProperty(PROPERTY.PAGERANK).toString()); 
					double score = cosineSimilarity * __CONFIG__.getRetrievalModelWeight() + __CONFIG__.getPagerankPublicationsWeight() * pageRank; 
					if(weights.containsKey(doc)) {
						weights.replace(doc, weights.get(doc)+(score)); 
					}else {
						weights.put(doc, score); 
					}	
					doc.setProperty(PROPERTY.SCORE, (double) weights.get(doc));
				}
			}

			List<?> sortedResults = sort(weights); 

			return sortedResults; 
		}
		else if(type == RESULT_TYPE.PLAIN) {
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

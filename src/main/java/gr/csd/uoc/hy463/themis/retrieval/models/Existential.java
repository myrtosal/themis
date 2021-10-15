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

import gr.csd.uoc.hy463.themis.indexer.Indexer;
import gr.csd.uoc.hy463.themis.indexer.model.DocInfoEssential;
import gr.csd.uoc.hy463.themis.indexer.model.DocInfoFull;
import gr.csd.uoc.hy463.themis.retrieval.QueryTerm;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;  
/**
 * Implementation of the Existential retrieval model. Returns the documents that
 * contain any of the terms of the query. For this model, there is no ranking of
 * documents, since all documents that have at least one term of the query, are
 * relevant and have a score 1.0
 *
 * @author Panagiotis Papadakos <papadako at ics.forth.gr>
 */
public class Existential extends ARetrievalModel {
	Indexer index = null; 
	public Existential(Indexer index) {
		super(index);
		this.index = index; 

	}

	@Override
	public List<?> getRankedResults(List<QueryTerm> query, RESULT_TYPE type) throws IOException {
		LinkedHashMap<Object, String> listdocs = new LinkedHashMap<Object, String>();
		if(type == RESULT_TYPE.ESSENTIAL) {
			System.out.println(index.getAverageDocumentSize() + " "+index.getCollectionLength()); 
			List<List<DocInfoEssential>> results = index.getDocInfoEssentialForTerms(query); 
			for(List<DocInfoEssential> ess : results) {
				for(DocInfoEssential doc : ess) {
					listdocs.put(doc, doc.getId()); 
				}
			}
			
			return sort(listdocs); 
		}
		else if(type == RESULT_TYPE.FULL) {
			List<List<DocInfoFull>> results = index.getDocInfoFullTerms(query); 
			if(results != null) {
				for(List<DocInfoFull> ess : results) {
					for(DocInfoFull doc : ess) {
						listdocs.put(doc, doc.getId()); 
					}
				}
				
				return sort(listdocs); 
			}
			else System.out.println("There is no results for this word"); 
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
		if(topk>results.size() || topk<= 0) return results; 
		return results.subList(0, topk); 
		
	}
	
	private List<Object> sort(LinkedHashMap<?, String> docs) {
		List<Object> sorted = new ArrayList<>(); 
		List<String> mapValues = new ArrayList<>(docs.values());
		Collections.sort(mapValues, Collections.reverseOrder());
		Iterator<String> valueIt = mapValues.iterator();

		while (valueIt.hasNext()) {
			String key = valueIt.next();
			for (Entry<?, String> entry : docs.entrySet()) {
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

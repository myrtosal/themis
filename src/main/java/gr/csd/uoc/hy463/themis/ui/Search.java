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
package gr.csd.uoc.hy463.themis.ui;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gr.csd.uoc.hy463.themis.indexer.Indexer;
import gr.csd.uoc.hy463.themis.indexer.model.DocInfoEssential;
import gr.csd.uoc.hy463.themis.indexer.model.DocInfoFull;
import gr.csd.uoc.hy463.themis.retrieval.QueryTerm;
import gr.csd.uoc.hy463.themis.retrieval.models.VSM;
import gr.csd.uoc.hy463.themis.retrieval.models.ARetrievalModel.RESULT_TYPE;
import gr.csd.uoc.hy463.themis.retrieval.models.Existential;
import gr.csd.uoc.hy463.themis.retrieval.models.OkapiBM25;

/**
 * Some kind of simple ui to search the indexes. Some kind of GUI will be a
 * bonus!
 *
 * @author Panagiotis Papadakos <papadako at ics.forth.gr>
 */
public class Search {
	private static final Logger __LOGGER__ = LogManager.getLogger(Search.class);
	private Indexer index; 
	
	
	public Search(Indexer index) {
		this.index = index;
	}
	


	@SuppressWarnings("unchecked")
	public void VSMsearch(List<QueryTerm> query, int i, RESULT_TYPE essential) {
		if(index.hasIndex() && index.loaded()) {
			VSM vsm = new VSM(index); 
			
			if(essential == RESULT_TYPE.ESSENTIAL) {
				List<DocInfoEssential> list;
				try {
					list = (List<DocInfoEssential>) vsm.getRankedResults(query, essential, i);
					if(list !=null){
						__LOGGER__.info("Number of result "+list.size());
						
						for(DocInfoEssential ess : list) {
							System.out.println(ess.toString()); 
						}
					}
					else System.out.println("There no results for this query"); 
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else {
				List<DocInfoFull> list;
				try {
					list = (List<DocInfoFull>) vsm.getRankedResults(query, essential, i);
					if(list !=null){
						__LOGGER__.info("Number of result "+list.size());
						for(DocInfoFull ess : list) {
							System.out.println(ess.toString()); 
						}
					}else System.out.println("There no results for this query");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			
		}
		
	}
	
	@SuppressWarnings("unchecked")
	public void okapiSearch(List<QueryTerm> query, int i, RESULT_TYPE type, double k, double b, double delta) {
		if(index.hasIndex() && index.loaded()) {
			OkapiBM25 okapi = new OkapiBM25(index, k, b, delta); 


			if(type == RESULT_TYPE.ESSENTIAL) {
				List<DocInfoEssential> list;
				try {
					list = (List<DocInfoEssential>) okapi.getRankedResults(query, type, i);
					if(list !=null){
						__LOGGER__.info("Number of result "+list.size()); 
						for(DocInfoEssential ess : list) {
							System.out.println(ess.toString()); 
						}
					}
				else System.out.println("There no results for this query");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else {
				List<DocInfoFull> list;
				try {
					list = (List<DocInfoFull>) okapi.getRankedResults(query, type, i);
					if(list !=null){
						__LOGGER__.info("Number of result "+list.size());
						for(DocInfoFull ess : list) {
							System.out.println(ess.toString()); 
						}
					}
					else System.out.println("There no results for this query");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void existancialSearch(List<QueryTerm> query, int i, RESULT_TYPE essential) {
		if(index.hasIndex() && index.loaded()) {
			Existential existancial = new Existential(index); 
			
			if(essential == RESULT_TYPE.ESSENTIAL) {
				List<DocInfoEssential> list;
				try {
					list = (List<DocInfoEssential>) existancial.getRankedResults(query, essential, i);
					if(list !=null){
						__LOGGER__.info("Number of result "+list.size());
						for(DocInfoEssential ess : list) {
							System.out.println(ess.toString()); 
						}
					}else System.out.println("There no results for this query");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else {
				List<DocInfoFull> list;
				try {
					
					list = (List<DocInfoFull>) existancial.getRankedResults(query, essential, i);
					if(list !=null){
						__LOGGER__.info("Number of result "+list.size());
						for(DocInfoFull ess : list) {
							System.out.println(ess.toString()); 
						}
					}else System.out.println("There no results for this query");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			 
			
			
		}
		
	}
	
	
	

}

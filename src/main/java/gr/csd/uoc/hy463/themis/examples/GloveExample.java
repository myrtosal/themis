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
package gr.csd.uoc.hy463.themis.examples;

import gr.csd.uoc.hy463.themis.config.Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;

import java.io.File;
import java.util.Collection;
import java.util.List;

/**
 * Class that showcases example of Glove usage
 *
 * @author Panagiotis Papadakos (papadako@ics.forth.gr)
 */
public class GloveExample {
	Config __CONFIG__ ;
	File gloveModel; 
	
	
	public GloveExample(Config config) {
		__CONFIG__ = config;
		gloveModel = new File(__CONFIG__.getGloveModelFileName());
	}
	
	
	public Collection<String> getAllNearest(String word, int n){
		WordVectors model = WordVectorSerializer.readWord2VecModel(gloveModel);
		return model.wordsNearest(word, n);
	}
	
	public static void main(String[] args) throws Exception {
		GloveExample glove = new GloveExample(new Config()); 
		
		String[] nouns = { "female", "tail", "person", "book", "actor", "information", "retrieval" };

		for (String noun : nouns) {
			System.out.println(noun + ":");

			Collection<String> lst = glove.getAllNearest(noun, 10);
			
			for (String s : lst) {
				System.out.println(s);
			}
		}

	}
}

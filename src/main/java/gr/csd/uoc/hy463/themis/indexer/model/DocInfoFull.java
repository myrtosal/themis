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
package gr.csd.uoc.hy463.themis.indexer.model;

import java.util.Arrays;
import java.util.LinkedList;
/**
 * This class could be used when we want to get all information of a specific
 * document, etc. title, authors, etc. by reading the appropriate entry in the
 * documents file
 *
 * @author Panagiotis Papadakos <papadako at ics.forth.gr>
 */
public class DocInfoFull extends DocInfoEssential {
	
	private String[] authors; 
	private String title; 
	private String description; 
	private String year; 

    public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public DocInfoFull(String id, long offset) {
        super(id, offset);
    }

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String[] getAuthors() {
		return authors;
	}

	public void setAuthors(String[] authors) {
		this.authors = authors;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return "DocInfoFull [authors=" + Arrays.toString(authors) + ", title=" + title + ", description=" + description
				+ ", year=" + year + ", id=" + id + ", offset=" + offset + ", props=" + props + "]";
	}
	
	
 

}

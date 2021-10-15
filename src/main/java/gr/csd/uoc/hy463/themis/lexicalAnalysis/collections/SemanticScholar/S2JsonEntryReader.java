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
package gr.csd.uoc.hy463.themis.lexicalAnalysis.collections.SemanticScholar;

import gr.csd.uoc.hy463.themis.config.Config;
import gr.csd.uoc.hy463.themis.utils.Pair;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Class responsible for reading textual entries from the json description of
 * entries
 *
 * @author Panagiotis Papadakos (papadako@ics.forth.gr)
 */
public class S2JsonEntryReader {

    private static final Logger __LOGGER__ = LogManager.getLogger(S2JsonEntryReader.class);
     
    // Method that reads all textual information from an entry
    @SuppressWarnings("unchecked")
	public S2TextualEntry readTextualEntry(String jsonToRead) {
        S2TextualEntry entry = new S2TextualEntry();
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(jsonToRead);

            // This should be a JSON object.
            JSONObject jsonObject = (JSONObject) obj;

            // Get the id for example
            String id = (String) jsonObject.get("id");
            entry.setId(id);

            // Get the title for example
            String titleCheck = (String) jsonObject.get("title");
            String title = titleCheck != null ? titleCheck : "";
            entry.setTitle(title);

            // Get abstract for example
            String paperAbstractCheck = (String) jsonObject.get("paperAbstract");
            String paperAbstract = paperAbstractCheck != null ? paperAbstractCheck : "";
            entry.setPaperAbstract(paperAbstract); 

            // Read entities. A JSONArray
            JSONArray entitiesArray = (JSONArray) jsonObject.get("entities");
            List<String> entities = new ArrayList<>();
            entitiesArray.forEach(entity -> {
                entities.add(entity.toString());
            });
            entry.setEntities(entities);

//            // Read fieldsOfStudy. A JSONArray
//            JSONArray fieldsArray = (JSONArray) jsonObject.get("fieldsOfStudy");
//            List<String> fields = new ArrayList<>();
//            fieldsArray.forEach(field -> {
//                fields.add(field.toString());
//            });
            entry.setFieldsOfStudy(null);

            // Read authors. A JSONArray
            JSONArray authorsList = (JSONArray) jsonObject.get("authors");
            List<Pair<String, List<String>>> authors = new ArrayList<>();
            for (int i = 0; i < authorsList.size(); i++) {
                JSONObject authorInfo = (JSONObject) authorsList.get(i);
                String authorName = (String) authorInfo.get("name");
                // Now get all the ids
                JSONArray idsList = (JSONArray) authorInfo.get("ids");
                List<String> ids = new ArrayList<>();
                for (int j = 0; j < idsList.size(); j++) {
                    String ID = (String) idsList.get(j);
                    ids.add(ID);
                }
                Pair<String, List<String>> author = new Pair<String, List<String>>(authorName, ids);
                authors.add(author);
            }
            entry.setAuthors(authors);

            JSONArray citationsArray = (JSONArray) jsonObject.get("outCitations");
            List<String> citations = new ArrayList<>();
            if(citationsArray !=null ) {
                citationsArray.forEach(citation -> {
                    citations.add(citation.toString());
                });
            }
            entry.setCitations(citations);

            // Get journal for example
            String journalCheck = (String) jsonObject.get("journalName");
            String journal = journalCheck != null ? journalCheck : "";
            entry.setJournalName(journal);

            // Read sources. A JSONArray
            JSONArray sourcesArray = (JSONArray) jsonObject.get("sources");
            List<String> sources = new ArrayList<>();
            if(sourcesArray !=null ) {
                sourcesArray.forEach(source -> {
                    sources.add(source.toString());
                });
            }
            entry.setSources(sources);

            // Get year for example
            Long yearLong = (Long) jsonObject.get("year");
            int year = yearLong != null ? yearLong.intValue() : 0;
            entry.setYear(year);

            // Get venue for example
            String venueCheck = (String) jsonObject.get("venue");
            String venue = venueCheck != null ? venueCheck : "";
            entry.setVenue(venue);

        } catch (ParseException e) {
            __LOGGER__.error(e.getMessage());
        }

        return entry;
    }
    
    public  LinkedList<String> readFile(String path) {
    	LinkedList<String> list = new LinkedList<>(); 
        try {
            final BufferedReader in = new BufferedReader(
                new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8));
            String line;
            while ((line = in.readLine()) != null) {
            	
                list.add(line); 
            }
            in.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return list;
    }
    
 // Method that reads all textual information from an entry
    public S2GraphEntry readGraphEntry(String jsonToRead) {
        S2GraphEntry entry = new S2GraphEntry();
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(jsonToRead);

            // This should be a JSON object.
            JSONObject jsonObject = (JSONObject) obj;

            // Get the id for example
            String id = (String) jsonObject.get("id");
            entry.setId(id);

            // Read authors. A JSONArray
            JSONArray authorsList = (JSONArray) jsonObject.get("authors");
            List<String> authors = new ArrayList<>();
            if (authorsList != null) {
                for (int i = 0; i < authorsList.size(); i++) {
                    JSONObject authorInfo = (JSONObject) authorsList.get(i);
                    // Now get all the ids
                    JSONArray idsList = (JSONArray) authorInfo.get("ids");
                    new ArrayList<>();
                    if(idsList != null) {
                        for (int j = 0; j < idsList.size(); j++) {
                            String ID = (String) idsList.get(j);
                            authors.add(ID);
                        }
                    }
                }
            }
            entry.setAuthors(authors);

            // Read sources. A JSONArray
            JSONArray citationsArray = (JSONArray) jsonObject.get("outCitations");
            List<String> citations = new ArrayList<>();
            if(citationsArray !=null ) {
                citationsArray.forEach(citation -> {
                    citations.add(citation.toString());
                });
            }
            entry.setCitations(citations);

        } catch (ParseException e) {
            __LOGGER__.error(e.getMessage());
        }

        return entry;
    }

    
    public static void main(String[] args) throws IOException {
    	Config config = new Config(); 
    	S2JsonEntryReader jsonEntry = new S2JsonEntryReader(); 
    	LinkedList<String> articles = jsonEntry.readFile(config.getDatasetPath()+"test");
    	java.util.Iterator<String> it = articles.iterator();
    	
    	while(it.hasNext()) {
    		S2GraphEntry entry = jsonEntry.readGraphEntry(it.next());
    		
    		System.out.println(entry.getId() + " "+entry.getCitations()); 
    	}
    }





}
package gr.csd.uoc.hy463.themis.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import gr.csd.uoc.hy463.themis.config.Config;
import gr.csd.uoc.hy463.themis.indexer.Indexer;
import gr.csd.uoc.hy463.themis.lexicalAnalysis.stemmer.StopWords;
import gr.csd.uoc.hy463.themis.retrieval.QueryTerm;
import gr.csd.uoc.hy463.themis.retrieval.models.ARetrievalModel.RESULT_TYPE;

public class BenchMark {
	private static final Logger __LOGGER__ = LogManager.getLogger(Main.class);
	static Config __CONFIG__;
	public static void main(String[] args) {
	try {
		
		Indexer index;
		__CONFIG__ = new Config();
		index = new Indexer();
		CreateIndex createIndex = new CreateIndex(index, __CONFIG__);
	      
	      createIndex.createIndex(true);
	      
	      if(index.load()) {
	    	  Search search = new Search(index);
	    	  int topk = 10000000; 
	    	  LinkedList<String> queries = readQueryFile(); 
	    	  
	    	  __LOGGER__.info("VSM SEARCHING ");
	    	  for(String query : queries) {
	    		  __LOGGER__.info("Searching "+query+"\t");
	    		  long startTime = System.currentTimeMillis();
	    		  List<QueryTerm> termquery = new ArrayList<>(); 
	    		  
                  String[] words = query.split(" "); 
                  StopWords stop = new StopWords();
                  for(String word : words) {
                      
                      if(!stop.isStopWord(word.toLowerCase())){
                          
                          termquery.add(new QueryTerm(word.toLowerCase().trim()));
                          
                      }
                      
                  }
                  search.VSMsearch(termquery, topk, RESULT_TYPE.ESSENTIAL);
                  long endTime = System.currentTimeMillis();
	              
	              long timeElapsed = endTime - startTime;
	              
	              __LOGGER__.info(" : " + timeElapsed / 1000+ " Seconds");
	    	  }
	    	  
	    
	    	  __LOGGER__.info("OKAPI SEARCHING "); 
	    	  for(String query : queries) {
	    		  __LOGGER__.info("Searching "+query+"\t");
	    
	    		  long startTime = System.currentTimeMillis();
	              
	    		  List<QueryTerm> termquery = new ArrayList<>(); 
	    		  
                  String[] words = query.split(" "); 
                  StopWords stop = new StopWords();
                  for(String word : words) {
                      
                      if(!stop.isStopWord(word.toLowerCase())){
                          
                          termquery.add(new QueryTerm(word.toLowerCase().trim()));
                          
                      }
                      
                  }
                  search.okapiSearch(termquery, topk, RESULT_TYPE.ESSENTIAL, 1.2, 0.75, 8);	
                  long endTime = System.currentTimeMillis();
	              
	              long timeElapsed = endTime - startTime;
	              
	              __LOGGER__.info(" : " + timeElapsed / 1000+ " Seconds");
	    	  }
	    	  
	      }
	} catch (ClassNotFoundException e) {
		
		e.printStackTrace();
	} catch (IOException e) {
		
		e.printStackTrace();
	}
      
     	__LOGGER__.info(getFileSizGigaBytes(new File(__CONFIG__.getIndexPath()+__CONFIG__.getDocumentsFileName())));
     	__LOGGER__.info(getFileSizGigaBytes(new File(__CONFIG__.getIndexPath()+__CONFIG__.getPostingsFileName())));
     	__LOGGER__.info(getFileSizGigaBytes(new File(__CONFIG__.getIndexPath()+__CONFIG__.getVocabularyFileName())));
		
	}
	
	
	private static String getFileSizGigaBytes(File file) {
		if(file.exists()) {
			return  file.getName()+" : "+(double)file.length() / (1024 * 1024*1024) + " Gb";
		}
		else return "File "+file+" does not exist"; 
	}
	
	
	public static LinkedList<String> readQueryFile(){
		LinkedList<String> queries = new LinkedList<>(); 
		try {
		      File myObj = new File("src/main/resources/QueryList.lst");
		      Scanner myReader = new Scanner(myObj);
		      while (myReader.hasNextLine()) {
		        String data = myReader.nextLine();
		        queries.add(data); 
		      }
		      myReader.close();
		    } catch (FileNotFoundException e) {
		      System.out.println("An error occurred.");
		      e.printStackTrace();
		    }
		return queries; 
	}
	

}

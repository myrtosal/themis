package gr.csd.uoc.hy463.themis.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import gr.csd.uoc.hy463.themis.config.Config;
import gr.csd.uoc.hy463.themis.indexer.Indexer;
import gr.csd.uoc.hy463.themis.lexicalAnalysis.stemmer.StopWords;
import gr.csd.uoc.hy463.themis.queryExpansion.GloveExpansion;
import gr.csd.uoc.hy463.themis.queryExpansion.WordNetExpansion;
import gr.csd.uoc.hy463.themis.retrieval.QueryTerm;
import gr.csd.uoc.hy463.themis.retrieval.models.ARetrievalModel.RESULT_TYPE;
import net.sf.extjwnl.JWNLException;

public class Main {
	private static final Logger __LOGGER__ = LogManager.getLogger(Main.class);
	static Config __CONFIG__;


	public static void main(String[] args) {

		try {

			Boolean bool;
			__CONFIG__ = new Config();
			Indexer index = new Indexer();
			CreateIndex createIndex = new CreateIndex(index, __CONFIG__); 

			System.out.println("Do you want to implement partial or regular indexing?");
			System.out.println("For partial indexing type PI. For regular indexing type RI");

			Scanner indexing = new Scanner(System.in);
			String in = indexing.nextLine();

			while(true){

				if(in.equalsIgnoreCase("PI")){

					bool = true;
					break;

				}
				else if(in.equalsIgnoreCase("RI")){

					bool = false;
					break;

				}
				else{

					System.out.println("Invalid input, please try again.");
					System.out.println("For partial indexing type PI. For regular indexing type RI");

				}

				in = indexing.nextLine();

			}

			System.out.println("Begin indexing process"); 

			createIndex.createIndex(bool);

			if(index.load()) {

				System.out.println("Vocabulary is loaded in memory "); 

			}
			else {

				System.out.println("Trying to load the vocabulary in memory"); 

				if(!index.load()) {

					System.exit(0);

				}

			}

			System.out.println("\tPlease type : \nv : to search with VSM\n" + "o : to search with OkapiBM25\n" + "q to quit"); 

			Scanner scanIn = new Scanner(System.in);
			String input = ""; 


			while(!"q".equals(input.toLowerCase())) {

				input = scanIn.nextLine(); 

				Search search = new Search(index); 

				System.out.println("Please type your query as :"
						+ " some random words"); 
				String query = scanIn.nextLine(); 


				System.out.println("Do you want to use the query expansion to improve the results"
						+ " (It may increase the time of searching) [YES or No]"); 

				String expansion = scanIn.nextLine(); 

				System.out.println("Now you can type the maximum number of results to display"); 

				String topk = scanIn.nextLine(); 

				System.out.println("Finally, you have to choose the result type :  " + "plain for RESULT_TYPE.PLAIN" + "full for RESULT_TYPE.FULL" + "and essential for RESULT_TYPE.ESSENTIAL");

				String result_type = scanIn.nextLine(); 

				RESULT_TYPE type; 

				if(result_type.toLowerCase().contentEquals("full")){

					type = RESULT_TYPE.FULL;

				} 
				else if(result_type.toLowerCase().contentEquals("plain")){

					type = RESULT_TYPE.PLAIN;

				} 
				else{

					type = RESULT_TYPE.ESSENTIAL;

				} 

				List<QueryTerm> termquery = new ArrayList<>(); 

				String[] words = query.split(" "); 
				StopWords stop = new StopWords();
				WordNetExpansion wordnet = new WordNetExpansion(); 
				GloveExpansion glove = new GloveExpansion(__CONFIG__); 
				if(expansion.toLowerCase().contentEquals("yes")) {
					for(String word : words) {

						if(!stop.isStopWord(word)){
							Collection<String> lst = glove.getAllNearest(word, 5);
							termquery.add(new QueryTerm(word.trim()));
							
							for (String noun : wordnet.getAllSynsets(word)) {
								QueryTerm newTerm = new QueryTerm(noun.trim()); 
								if(!termquery.contains(newTerm))
									termquery.add(newTerm);
								
							}

							for (String s : lst) {
								QueryTerm newTerm = new QueryTerm(s.trim()); 
								if(!termquery.contains(newTerm))
									termquery.add(newTerm);
							}


						}
						
					}
				
				}
				else {
					for(String word : words) {

						if(!stop.isStopWord(word)){
							
							termquery.add(new QueryTerm(word.trim()));
						}

					}
					System.out.println(termquery.size()); 
				}

				if(input.toLowerCase().contentEquals("v")) {

					search.VSMsearch(termquery, Integer.parseInt(topk),type );

				}
				else if(input.toLowerCase().contentEquals("o")) {

					search.okapiSearch(termquery, Integer.parseInt(topk), type, 1.2, 0.75, 8);			

				}
				else if(input.toLowerCase().contentEquals("e")) {

					search.existancialSearch(termquery, Integer.parseInt(topk), type);

				}
				System.out.println(" Please type : \n \t v : to search with VSM\n" + "o : to search with OkapiBM25\n" + "e : to search with Existential model\n" + "q to quit"); 

			}

		} 
		catch (ClassNotFoundException | IOException | JWNLException e) {

			// TODO Auto-generated catch block
			e.printStackTrace();

		}

		// TODO Auto-generated catch block

	}

}

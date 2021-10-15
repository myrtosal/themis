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
package gr.csd.uoc.hy463.themis.indexer;

import gr.csd.uoc.hy463.themis.config.Config;
import gr.csd.uoc.hy463.themis.indexer.indexes.Index;
import gr.csd.uoc.hy463.themis.indexer.indexes.InvertedIndex;
import gr.csd.uoc.hy463.themis.indexer.model.DocInfoEssential;
import gr.csd.uoc.hy463.themis.indexer.model.DocInfoFull;
import gr.csd.uoc.hy463.themis.lexicalAnalysis.collections.SemanticScholar.S2GraphEntry;
import gr.csd.uoc.hy463.themis.lexicalAnalysis.collections.SemanticScholar.S2JsonEntryReader;
import gr.csd.uoc.hy463.themis.lexicalAnalysis.collections.SemanticScholar.S2TextualEntry;
import gr.csd.uoc.hy463.themis.lexicalAnalysis.stemmer.StopWords;
import gr.csd.uoc.hy463.themis.linkAnalysis.PageRank;
import gr.csd.uoc.hy463.themis.linkAnalysis.graph.Graph;
import gr.csd.uoc.hy463.themis.linkAnalysis.graph.Node;
import gr.csd.uoc.hy463.themis.linkAnalysis.graph.VisualGraph;
import gr.csd.uoc.hy463.themis.retrieval.QueryTerm;
import gr.csd.uoc.hy463.themis.utils.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;

import javax.swing.JFrame;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Paths;

/**
 * Our basic indexer class. This class is responsible for two tasks:
 *
 * a) Create the appropriate indexes given a specific directory with files (in
 * our case the Semantic Scholar collection)
 *
 * b) Given a path load the indexes (if they exist) and provide information
 * about the indexed data, that can be used for implementing any kind of
 * retrieval models
 *
 * When the indexes have been created we should have three files, as documented
 * in Index.java
 *
 * @author Panagiotis Papadakos (papadako@ics.forth.gr)
 */
public class Indexer {
	Queue<Integer> queue; 
	private static final Logger __LOGGER__ = LogManager.getLogger(Indexer.class);
	private Config __CONFIG__;  // configuration options
	// The file path of indexes
	private String __INDEX_PATH__ = null;
	// Filenames of indexes
	private String __VOCABULARY_FILENAME__ = null;
	private String __POSTINGS_FILENAME__ = null;
	private String __DOCUMENTS_FILENAME__ = null;
	// Vocabulary should be stored in memory for querying! This is crucial
	// since we want to keep things fast! This is done through load().
	// For this project use a HashMap instead of a trie
	private HashMap<String, InvertedIndex>  tempvocabulary; 
	private HashMap<String, Pair<Integer, Long>> __VOCABULARY__ = null;
	private RandomAccessFile __POSTINGS__ = null;
	private RandomAccessFile __DOCUMENTS__ = null;
	S2JsonEntryReader jsonEntry = new S2JsonEntryReader();
	private int collectionLength  = 10000 ; 
	private double averageDocumentSize;

	public int getCollectionLength() {
		return this.collectionLength;
	}



	/**
	 * Default constructor. Creates also a config instance
	 *
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public Indexer() throws IOException, ClassNotFoundException {
		__CONFIG__ = new Config();  // reads info from themis.config file
		init();
	}



	public Config get__CONFIG__() {
		return __CONFIG__;
	}


	public void set__CONFIG__(Config __CONFIG__) {
		this.__CONFIG__ = __CONFIG__;
	}

	/**
	 * Constructor that gets a current Config instance
	 *
	 * @param config
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public Indexer(Config config) throws IOException, ClassNotFoundException {
		this.__CONFIG__ = config;  // reads info from themis.config file
		init();
	}

	/**
	 * Initialize things
	 */
	private void init() { 
		__VOCABULARY_FILENAME__ = __CONFIG__.getVocabularyFileName();
		__POSTINGS_FILENAME__ = __CONFIG__.getPostingsFileName();
		__DOCUMENTS_FILENAME__ = __CONFIG__.getDocumentsFileName();
		__INDEX_PATH__ = __CONFIG__.getIndexPath();
		__VOCABULARY__ = new HashMap<String, Pair<Integer, Long>> (); 
		queue = new LinkedList<>();
		tempvocabulary = new HashMap<String, InvertedIndex>(); 
	}

	/**
	 * Checks that the index path + all *.idx files exist
	 *
	 * Method that checks if we have all appropriate files
	 *
	 * @return
	 */
	public boolean hasIndex() {
		// Check if path exists
		File file = new File(__INDEX_PATH__);
		if (!file.exists() || !file.isDirectory()) {
			__LOGGER__.error(__INDEX_PATH__ + "directory does not exist!");
			return false;
		}
		// Check if index files exist
		file = new File(__INDEX_PATH__ + __VOCABULARY_FILENAME__);
		if (!file.exists() || file.isDirectory()) {
			__LOGGER__.error(__VOCABULARY_FILENAME__ + "vocabulary file does not exist in " + __INDEX_PATH__);
			return false;
		}
		file = new File(__INDEX_PATH__ + __POSTINGS_FILENAME__);
		if (!file.exists() || file.isDirectory()) {
			__LOGGER__.error(__POSTINGS_FILENAME__ + " posting binary file does not exist in " + __INDEX_PATH__);
			return false;
		}
		file = new File(__INDEX_PATH__ + __DOCUMENTS_FILENAME__);
		if (!file.exists() || file.isDirectory()) {
			__LOGGER__.error(__DOCUMENTS_FILENAME__ + "documents binary file does not exist in " + __INDEX_PATH__);
			return false;
		}
		return true;
	}

	/**
	 * Method responsible for indexing a directory of files
	 *
	 * If the number of files is larger than the PARTIAL_INDEX_MAX_DOCS_SIZE set
	 * to the themis.config file then we have to dump all data read up to now to
	 * a partial index and continue with a new index. After creating all partial
	 * indexes then we have to merge them to create the final index that will be
	 * stored in the file path.
	 *
	 * Can also be modified to use the MAX_MEMORY usage parameter given in
	 * themis.conf for brave hearts!
	 *
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public boolean index(String path) throws IOException {
		Index index = new Index(__CONFIG__);
		Long previousSeek = 0L; 
		Long seekDoc = 0L;
		int id = 0;
		StopWords stop = new StopWords();
		stop.Initialize();
		double aveDocSize = 0; 
		// set id of index
		index.setID(id);

		// We use a linked list as a queuue for our partial indexes
		Queue<Integer> partialIndexesQueue = new LinkedList<>();

		// Add id to queue
		partialIndexesQueue.add(id);

		// for each file in path  
		S2JsonEntryReader jsonEntry = new S2JsonEntryReader(); 
		DirectoryStream<Path> stream = Files.newDirectoryStream( Paths.get(path)); 
		for (Path entry : stream) {
			LinkedList<String> articles = jsonEntry.readFile(entry.toString());
			long startTime = System.currentTimeMillis();	
			System.out.println("Indexing file : "+entry.getFileName()); 
			for (java.util.Iterator<String> article = articles.iterator(); article.hasNext();) {
				collectionLength ++; 
				S2TextualEntry textEntry = jsonEntry.readTextualEntry(article.next());

				String[] split = textEntry.getUtileInformation().replaceAll(System.lineSeparator(), " ").split(" "); 
				double weight = 0; 
				System.out.println(textEntry.getTitle());
				aveDocSize += split.length; 
				for(String word : split) {
					List<Long> positions = new ArrayList<Long>();
					String cleaned = index.cleanTextContent(word); 
					if(cleaned != null && cleaned.length()>2) {
						float tf = index.calculateTF(split, cleaned);				
						if (!tempvocabulary.containsKey(cleaned)) {
							weight =1; 
							positions.add(previousSeek); 

							tempvocabulary.put(cleaned, new InvertedIndex(1, tf, positions) );
						} else {
							weight +=1; 
							// We want to avoid duplicate pointers
							if(tempvocabulary.get(cleaned).getPostions().contains(previousSeek)) {
								tempvocabulary.put(cleaned, new InvertedIndex( tempvocabulary.get(cleaned).getDf() + 1,  tf,  tempvocabulary.get(cleaned).getPostions()));
							}
							else {
								tempvocabulary.get(cleaned).getPostions().add(previousSeek); 
								tempvocabulary.put(cleaned, new InvertedIndex( tempvocabulary.get(cleaned).getDf() + 1,  tf,  tempvocabulary.get(cleaned).getPostions()));
							}
						}

					}

				}
				seekDoc = index.writeDocument(textEntry, seekDoc, collectionLength, weight); 
				weight = 0; 
				previousSeek = seekDoc; 
			}
			long endTime = System.currentTimeMillis();

			long timeElapsed = endTime - startTime;
			System.out.println("\ttime in secondes : " + 
					timeElapsed / 1000); 
		}



		//		setCollectionLength(collectionLength);
		setAverageDocumentSize(aveDocSize/collectionLength); 
		indexing(index);


		// Now we have finished creating the partial indexes
		// So we have to merge them (call merge)


		return true;
	}


	public Graph graphPublications(String path) throws IOException {
		Graph graph = new Graph(); 


		DirectoryStream<Path> stream = Files.newDirectoryStream( Paths.get(path)); 
		for (Path entry : stream) {
			System.out.println(entry.getFileName()); 
			LinkedList<String> articles = jsonEntry.readFile(entry.toString());
			for (java.util.Iterator<String> article = articles.iterator(); article.hasNext();) {
				S2GraphEntry item = jsonEntry.readGraphEntry(article.next());
				Node node = new Node(item.getId()); 
				graph.addNode(node);
				for(String edges : item.getCitations()) {
					Node edge = new Node(edges); 
					graph.addEdge(node, edge);
				}	
			}
		}
		return graph; 
	}


	public boolean index(Boolean bool) throws IOException {

		String collectionPath = __CONFIG__.getDatasetPath();

		if (collectionPath != null) {
			if(bool == true){
				return indexPartial(collectionPath);
			}
			else{
				return index(collectionPath);
			}
		} 
		else {

			__LOGGER__.error("DATASET_PATH not set in themis.config!");
			return false;
		}

	}

	public void setCollectionLength(int collectionLength) {
		this.collectionLength = collectionLength;
	}



	public void indexing(Index index) throws IOException{

		Iterator<?> hmIterator = tempvocabulary.entrySet().iterator();
		Long seekPos = 0L;
		ArrayList<String> tempVoc = new ArrayList<>();
		ArrayList<Pair<InvertedIndex, Long>> tempPost = new ArrayList<>();

		while(hmIterator.hasNext()){

			Map.Entry mapElem = (Map.Entry)hmIterator.next();

			InvertedIndex inverted = (InvertedIndex) mapElem.getValue();
			String word = mapElem.getKey().toString();

			double tf = inverted.getTf();
			double df = inverted.getDf();

			String voctext = word + "##" + df + "##" + seekPos + "\r\n";
			String postext = tf + "##" + inverted.getPostions() + "\r\n";

			tempPost.add(new Pair<InvertedIndex, Long>(inverted, seekPos));            
			tempVoc.add(voctext);

			seekPos = seekPos + postext.length(); 
		}

		System.currentTimeMillis();



		//System.out.println("Making arrays time: " + elapsedArrTime/1000);

		index.writePosting(tempPost);

		Collections.sort(tempVoc);
		index.writeVocabulary(tempVoc);

	}
	
	/**
	 * Method that merges the partial indexes and creates a new index with new
	 * ID which is either a new partial index or the final index if the queue is
	 * empty. If it is a partial index it adds it to the queue at the tail using
	 * add
	 *
	 * @param partialIndexesQueue
	 * @return
	 */
	private void merge(Index index) throws IOException {
		

		long startTime = System.nanoTime();

		__LOGGER__.info("Start merging - queue size : "+queue.size());
		
		int id = queue.size(); 
		int last = 0; 
		MergeFiles w1 = new MergeFiles(__CONFIG__, queue);
		Iterator<Integer> iterator = queue.iterator();
		while(iterator.hasNext()){
			int element1 = queue.poll();
		
			if(iterator.hasNext()) {
				int element2 = queue.poll(); 
				
				int next = id++; 
				
				String tempFile1 = __INDEX_PATH__+element1+".idx";
				String tempFile2 = __INDEX_PATH__+element2+".idx";
				String opFile = __INDEX_PATH__+next+".idx"; 
				
				w1.merge(tempFile1,tempFile2,opFile);
				queue.add(next); 
	
				iterator = queue.iterator();

			}
			last = element1;  
		}

		queue.add(last); 
		
		long endTime = System.nanoTime();
		double timeTaken = (endTime - startTime)/1e9;
		__LOGGER__.info("End merging taking time "+timeTaken);

		index.writePartial(queue);
	}


	/**
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public boolean indexPartial(String path) throws IOException {
		Index index = new Index(__CONFIG__);
		StopWords stop = new StopWords();
		stop.Initialize();
		HashMap<Node, Double> rank = new HashMap<>();
		
		Long seekDoc = 0L, previousSeek = 0L;
		int id = 0;
		File pageRankScore = new File(__CONFIG__.getPageRankScoreFile()); 
		if(pageRankScore.exists()) {

			Properties properties = new Properties();
			properties.load(new FileInputStream(__CONFIG__.getPageRankScoreFile()));

			for (String key : properties.stringPropertyNames()) {
				Node node = new Node(key); 
				rank.put(node, Double.parseDouble(properties.get(key).toString()));
			}
		}
		else {
			Graph publicGraph = graphPublications(path); 

			PageRank pagerank = new PageRank(publicGraph); 

			rank = pagerank.rank(50, 0.85); 

			pagerank.writeScores(__CONFIG__); 

			System.out.println(rank.size()); 
		}



		Paths.get(__CONFIG__.getDatasetPath());
		// set id of index
		index.setID(id);
		Files.newDirectoryStream(Paths.get(path));
		// We use a linked list as a queue for our partial indexes
		queue = new LinkedList<>();
		S2JsonEntryReader jsonEntry = new S2JsonEntryReader(); 
		DirectoryStream<Path> stream = Files.newDirectoryStream( Paths.get(path)); 


		for(Path entry : stream){
			long startProTime = System.currentTimeMillis();
			__LOGGER__.info("Indexing "+entry.getFileName().toString());
			final BufferedReader in = new BufferedReader(
					new InputStreamReader(new FileInputStream(entry.toString()), StandardCharsets.UTF_8));
			String article;
			while ((article = in.readLine()) != null) {

				collectionLength++;

				InvertedIndex inv;
				S2TextualEntry textEntry = jsonEntry.readTextualEntry(article); 
				String info = textEntry.getUtileInformation(); 
				String cleanLine = index.cleanTextContent(info); 

				String[] split = cleanLine.split(" ");
				Node node  = new Node(textEntry.getId()); 
				// for each article of file
				for(int blah = 0; blah < split.length; blah++){

					String cleaned = split[blah];


					if((!stop.isStopWord(cleaned) && index.isLatinWords(cleaned)) &&  cleaned.length()>3){

						float tf = index.calculateTF(split, cleaned, blah);

						if(tempvocabulary.containsKey(cleaned)){

							tempvocabulary.get(cleaned).getPostions().add(previousSeek);
							InvertedIndex invNew = new InvertedIndex(tempvocabulary.get(cleaned).getDf() + 1, tf, tempvocabulary.get(cleaned).getPostions());
							tempvocabulary.replace(cleaned, invNew);

						}
						else{
							List<Long> positions = new ArrayList<>();
							positions.add(previousSeek);                                
							inv = new InvertedIndex(1, tf, positions);
							tempvocabulary.put(cleaned, inv);

						}

					}

					// if indexed articles for this index less than
					// config.getPartialIndexSize store all information to
					// approapriate structures in memory to Index class else dump
					// to files in appropriate directory id and increase partialIndexes
					if (tempvocabulary.size() >= __CONFIG__.getPartialIndexSize()) {

						index.dump(tempvocabulary);   // dump partial index to appropriate subdirectory
						// Create a new index
						// Increase partial indexes and dump files to appropriate directory

						int next = id++;
						index.setID(next);

						// Add id to queue
						queue.add(next);

						tempvocabulary.clear();
					}

				}
				if(rank.get(node) == null) {
					System.out.println("null" + rank.size()); 
					seekDoc = index.writeDocument(textEntry, seekDoc, collectionLength,  1/rank.size()); 
				}
					 
				else {
					System.out.println("else" + rank.get(node)); 
					seekDoc = index.writeDocument(textEntry, seekDoc, collectionLength,  rank.get(node)); 
				}
				previousSeek = seekDoc; 
				
			}

			index.dump(tempvocabulary);


			//System.out.println(partialIndexesQueue);

			long endProTime = System.currentTimeMillis();

			long timeProElapsed = endProTime - startProTime;


			in.close();

			//			System.out.println("Indexing time: " + timeProElapsed/1000);
			__LOGGER__.info("Indexing time: " + timeProElapsed/1000); 
		}

		setCollectionLength(collectionLength);
		setAverageDocumentSize(averageDocumentSize/collectionLength);

		
		System.out.println(collectionLength); 
		// Now we have finished creating the partial indexes
		// So we have to merge them (call merge)
		merge(index);

		return false;
	}

	/**
	 * Method that indexes the collection that is given in the themis.config
	 * file
	 *
	 * Used for the task of indexing!
	 *
	 * @return
	 * @throws IOException
	 */
	public boolean index() throws IOException {
		String collectionPath = __CONFIG__.getDatasetPath();
		if (collectionPath != null) {
			return index(collectionPath);
		} else {
			__LOGGER__.error("DATASET_PATH not set in themis.config!");
			return false;
		}
	}

	/**
	 * Method responsible for loading vocabulary file to memory and also opening
	 * RAF files to postings and documents, ready to seek
	 *
	 * Used for the task of querying!
	 *
	 * @return
	 * @throws IOException
	 */
	public boolean load() throws IOException {
		if (!hasIndex()) {
			__LOGGER__.error("Index is not constructed correctly!");
			return false;
		}
		else {
			try {
				final BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(__INDEX_PATH__+__VOCABULARY_FILENAME__), StandardCharsets.UTF_8));
				in.readLine(); 
				String line;

				while ((line = in.readLine()) != null) {

					if(line != "" || line !=" ") {
						String spliter[] = line.split("##"); 
						if(spliter.length > 2)
							__VOCABULARY__.put(spliter[0], new Pair<Integer, Long>((int)Double.parseDouble(spliter[1]), Long.parseLong(spliter[2])));
					}

				}
				in.close();
			} catch (final IOException e) {
				e.printStackTrace();
			}
			return true; 
		}
	}

	/**
	 * Basic method for querying functionality. Given the list of terms in the
	 * query, returns a List of Lists of DocInfoEssential objects, where each
	 * list of DocInfoEssential objects holds where each list of
	 * DocInfoEssential objects holds the DocInfoEssential representation of the
	 * docs that the corresponding term of the query appears in. A
	 * DocInfoEssential, should hold all needed information for implementing a
	 * retrieval model, like VSM, Okapi-BM25, etc. This is more memory efficient
	 * than holding getDocInfoFullTerms objects
	 *
	 * @param terms
	 * @return
	 * @throws IOException 
	 */
	@SuppressWarnings("unchecked")
	public List<List<DocInfoEssential>> getDocInfoEssentialForTerms(List<QueryTerm> terms) throws IOException {
		List<List<DocInfoEssential>> docinfo = new ArrayList<List<DocInfoEssential>>(); 

		if (!loaded()) {
			return null;
		} else {

			for(QueryTerm qr : terms) {
				double querytf = computeTF(qr.getTerm(), terms);
				Pair<Integer, Long> entry = __VOCABULARY__.get(qr.getTerm());
				
				if(entry != null ) {
					Long seek = entry.getR();
					
					
					Pair<String, Double> docseeks = getDocSeeks(seek); 
					

					if(docseeks == null) return null; 

					docinfo.add((List<DocInfoEssential>) getEssentials(docseeks.getL(), entry.getL(), docseeks.getR(), querytf)); 
				}
			
			
			}
		}
		return docinfo; 
	}

	public float computeTF(String term, List<QueryTerm> terms) {
		float result = 0;
		for (QueryTerm word : terms) {
			if (term.equalsIgnoreCase(word.getTerm()))
				result++;
		}
		return result/terms.size(); 
	}

	public Pair<String, Double> getDocSeeks(Long seek) throws IOException{
		__POSTINGS__ = new RandomAccessFile(__INDEX_PATH__+__POSTINGS_FILENAME__, "rw");
		Pair<String, Double> docseek; 
		__POSTINGS__.seek(seek);
		String read = __POSTINGS__.readLine();
		docseek = new Pair<String, Double>(read.split("##")[1] , Double.parseDouble(read.split("##")[0])); 
		__POSTINGS__.close();
		return docseek; 
	}


	public List<?> getEssentials(String docseek, int df, double tf, double querytf) throws IOException {
		List<DocInfoEssential> essentials = new ArrayList<DocInfoEssential>(); 
		__DOCUMENTS__ = new RandomAccessFile(__INDEX_PATH__+__DOCUMENTS_FILENAME__, "rw");
		String[]seeks = docseek.replace("[", "").replace("]", "").trim().split(","); 
		for(String s : seeks) {
			if(s != "") {
				__DOCUMENTS__.seek(Long.parseUnsignedLong(s.trim()));
				String infoDoc = __DOCUMENTS__.readLine(); 
				String split[] = infoDoc.split("##"); 
				DocInfoEssential essential = new DocInfoEssential(split[0], Long.parseLong(s.trim())); 
				essential.setProperty(DocInfoEssential.PROPERTY.LENGTH, Double.parseDouble(split[3]));
				//				essential.setProperty(DocInfoEssential.PROPERTY.WEIGHT, Double.parseDouble(split[4]));
				essential.setProperty(DocInfoEssential.PROPERTY.PAGERANK, Double.parseDouble(split[4]));
				essential.setProperty(DocInfoEssential.PROPERTY.QUERYTF, querytf);
				essential.setProperty(DocInfoEssential.PROPERTY.TF,tf);
				essential.setProperty(DocInfoEssential.PROPERTY.DF, df);
				essentials.add(essential); 
			}
		}
		__DOCUMENTS__.close(); 

		return essentials; 

	}




	/**
	 * Basic method for querying functionality. Given the list of terms in the
	 * query, returns a List of Lists of DocInfoFull objects, where each list of
	 * DocInfoFull objects holds the DocInfoFull representation of the docs that
	 * the corresponding term of the query appears in (i.e., the whole
	 * information). Not memory efficient though...
	 *
	 * Useful when we want to return the title, authors, etc.
	 *
	 * @param terms
	 * @return
	 * @throws IOException 
	 */
	public List<List<DocInfoFull>> getDocInfoFullTerms(List<QueryTerm> terms) throws IOException {
		// If indexes are not oaded
		if (!loaded()) {
			return null;
		} else {
			List<List<DocInfoFull>> full = new LinkedList<List<DocInfoFull>>(); 
			for(QueryTerm term : terms) {
				Pair<Integer, Long> posseek = __VOCABULARY__.get(term.getTerm()); 
				if(posseek != null) {
					Pair<String, Double> docseeks = getDocSeeks(posseek.getR()); 
					double querytf = computeTF(term.getTerm(), terms); 
					full.add(getFull(docseeks.getL(), posseek.getL(), docseeks.getR(), querytf)); 
				}

			}
			return full; 
		} 
	}

	public List<DocInfoFull> getFull(String docseek, double tf, double df, double querytf) throws IOException{
		List<DocInfoFull> full = new LinkedList<DocInfoFull>(); 
		__DOCUMENTS__ = new RandomAccessFile(__INDEX_PATH__+__DOCUMENTS_FILENAME__, "rw");
		String[]seeks = docseek.replace("[", "").replace("]", "").split(","); 
		for(String s : seeks) {
			if(s != "") {
				__DOCUMENTS__.seek(Long.parseUnsignedLong(s.trim()));
				String infoDoc = __DOCUMENTS__.readLine(); 

				String split[] = infoDoc.split("##");
				DocInfoFull fulldoc = new DocInfoFull(split[0], Long.parseLong(s.trim())); 
				fulldoc.setProperty(DocInfoEssential.PROPERTY.LENGTH, Double.parseDouble(split[4]));
				//				fulldoc.setProperty(DocInfoEssential.PROPERTY.WEIGHT, Double.parseDouble(split[4]));
				fulldoc.setProperty(DocInfoEssential.PROPERTY.PAGERANK, Double.parseDouble(split[4]));
				fulldoc.setProperty(DocInfoEssential.PROPERTY.TF,tf);
				fulldoc.setProperty(DocInfoEssential.PROPERTY.DF, df);
				fulldoc.setProperty(DocInfoEssential.PROPERTY.QUERYTF, querytf);
				fulldoc.setYear(split[2]);
				fulldoc.setTitle(split[1]);

				//				String authors[] = split[2].split(","); 
				//
				//				fulldoc.setAuthors(authors);
				full.add(fulldoc); 
			}
		}
		__DOCUMENTS__.close(); 


		return full; 
	}

	/**
	 * This is a method that given a list of docs in the essential
	 * representation, returns a list with the full description of docs stored
	 * in the Documents File. This method is needed when we want to return the
	 * full information of a list of documents. Could be useful if we support
	 * pagination to the results (i.e. provide the full results of ten
	 * documents)
	 *
	 * @param docs
	 * @return
	 * @throws IOException 
	 */
	public List<DocInfoFull> getPlain(List<DocInfoEssential> docs) throws IOException {
		// If indexes are not loaded
		if (!loaded()) {
			return null;
		} else {
			__DOCUMENTS__ = new RandomAccessFile(__INDEX_PATH__+__DOCUMENTS_FILENAME__, "rw");
			List<DocInfoFull> description = new ArrayList<DocInfoFull>(); 
			for(DocInfoEssential doc : docs) {
				__DOCUMENTS__.seek(doc.getOffset());
				String infoDoc = __DOCUMENTS__.readLine(); 
				String split[] = infoDoc.split("##"); 
				DocInfoFull fulldoc = new DocInfoFull(split[0], doc.getOffset()); 
				fulldoc.setProperty(DocInfoEssential.PROPERTY.LENGTH, Double.parseDouble(split[3]));
				//				fulldoc.setProperty(DocInfoEssential.PROPERTY.WEIGHT, Double.parseDouble(split[4]));
				fulldoc.setProperty(DocInfoEssential.PROPERTY.PAGERANK, Double.parseDouble(split[4]));

				//				fulldoc.setDescription(split[7]);

				fulldoc.setTitle(split[1]);
				fulldoc.setYear(split[2]);
				//				String authors[] = split[2].split(","); 
				//
				//				fulldoc.setAuthors(authors);
				description.add(fulldoc); 
			}
			return description; 
		}
	}

	/**
	 * Method that checks if indexes have been loaded/opened
	 *
	 * @return
	 */
	public boolean loaded() {
		return __VOCABULARY__ != null; 
	}

	/**
	 * Get the path of index as set in themis.config file
	 *
	 * @return
	 */
	public String getIndexDirectory() {
		if (__CONFIG__ != null) {
			return __INDEX_PATH__;
		} else {
			__LOGGER__.error("Index has not been initialized correctly");
			return "";
		}
	}



	public double getAverageDocumentSize() {
		return this.averageDocumentSize;
	}



	public void setAverageDocumentSize(double averageDocumentSize) {
		this.averageDocumentSize = averageDocumentSize;
	}



	public void createGraph(Graph graph) throws IOException {
		VisualGraph applet = new VisualGraph(graph);
		applet.init();

		JFrame frame = new JFrame();
		frame.getContentPane().add(applet);
		frame.setTitle("JGraphT Adapter to JGraphX Demo");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}

}
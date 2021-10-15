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
package gr.csd.uoc.hy463.themis.indexer.indexes;

import gr.csd.uoc.hy463.themis.config.Config;
import gr.csd.uoc.hy463.themis.lexicalAnalysis.collections.SemanticScholar.S2TextualEntry;
import gr.csd.uoc.hy463.themis.lexicalAnalysis.stemmer.StopWords;
import gr.csd.uoc.hy463.themis.utils.Pair;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class holds all information related to a specific (partial or not) index
 * in memory. It also knows how to store this information to files
 *
 * @author Panagiotis Papadakos (papadako@ics.forth.gr)
 */
public class Index {

	// Partial indexes have an id > 0 and corresponding idx files are stored in
	// INDEX_PATH/id while for a full index, idx files are stored in INDEX_PATH
	// e.g., the first partial index files are saved to INDEX_PATH/1/
	private static int id = 0; // the id of the index that is used for partial indexes

	private static final Logger __LOGGER__ = LogManager.getLogger(Index.class);
	private static Config __CONFIG__;  // configuration options

	// The path of index
	private static String __INDEX_PATH__ = null;
	// Filenames of indexes
	private static String __VOCABULARY_FILENAME__ = null;
	private static String __POSTINGS_FILENAME__ = null;
	private static String __DOCUMENTS_FILENAME__ = null;




	// We also need to store any information about the vocabulary,
	// posting and document file in memory
	// For example a TreeMap holds entries sorted which helps with storing the
	// vocabulary file


	// We have to hold also other appropriate data structures for postings / documents
	public Index(Config config) {
		__CONFIG__ = config;
		init();
	}

	/**
	 * Initialize things
	 */
	private void init() {
		__INDEX_PATH__ = __CONFIG__.getIndexPath();
		__VOCABULARY_FILENAME__ = __INDEX_PATH__+__CONFIG__.getVocabularyFileName();
		__POSTINGS_FILENAME__ = __INDEX_PATH__+__CONFIG__.getPostingsFileName();
		__DOCUMENTS_FILENAME__ = __INDEX_PATH__+__CONFIG__.getDocumentsFileName();
	}

	/**
	 * This method is responsible for dumping all information held by this index
	 * to the filesystem in the directory INDEX_PATH/id. If id = 0 then it dumps
	 * every idx files to the INDEX_PATH
	 *
	 * Specifically, it creates:
	 *
	 * =========================================================================
	 * 1) VOCABULARY FILE => vocabulary.idx (Normal Sequential file)
	 *
	 * This is a normal sequential file where we write in lexicographic order
	 * the following entries separated by space: | TERM (a term of the
	 * vocabulary) | DF document frequency of this term | POINTER_TO_POSTING
	 * (the offset in the posting.idx, this is a long number) |
	 * @throws IOException 
	 *
	 */

	public void writeVocabulary(ArrayList<String> vocab){

		try {

			long startTime = System.currentTimeMillis();

			BufferedWriter writer = new BufferedWriter(new FileWriter(__VOCABULARY_FILENAME__,true));
			writer.newLine();

			for(String word : vocab){

				//System.out.println("word: "+word);
				writer.write(word);                

			}

			writer.close();

			long endTime = System.currentTimeMillis();

			long elapsedTime = endTime - startTime;

			System.out.println("WRITE VOCAB TIME: " + elapsedTime/1000);

		}
		catch (IOException e) {

			e.printStackTrace();

		}


	}

	// Method to calculate the tf
	public float calculateTF(String[] split, String term, int i) {

		float result = 0;

		for(int blah=i; blah<split.length;blah++) {

			if (term.equalsIgnoreCase(split[blah])){

				result++;

			}

		}

		return result/split.length; 
	} 


	public boolean isLatinWords(String v) {
		return Charset.forName("ISO-8859-1").newEncoder().canEncode(v);
	}


	public String cleanTextContent(String text) {

		 
		text = text.trim().toLowerCase().replaceAll(System.lineSeparator(), " ").replaceAll("\\W"," ").replaceAll("(?U)[^\\p{Alnum}\\p{Space}]+", " ");
		return text;

	}

	public float calculateTF(String[] split, String term) {
		float result = 0;
		for (String word : split) {
			if (term.equalsIgnoreCase(word))
				result++;
		}

		return result/split.length; 
	}

	/**
	 * =========================================================================
	 * 2) POSTING FILE => posting.idx (Random Access File)
	 *
	 * For each entry it stores: | DOCUMENT_ID (40 ASCII chars => 40 bytes) | TF
	 * (int => 4 bytes) | POINTER_TO_DOCUMENT_FILE (long => 4 bytes)
	 * @throws IOException 
	 */

	//	public  Long writePosting(Long seekPos,double tf, InvertedIndex positions) throws IOException {
	//		File file = new File(__POSTINGS_FILENAME__);
	//		RandomAccessFile raf = new RandomAccessFile(file, "rw");;
	//		String data = null; 
	//
	//		data = tf+"##"+positions.getPostions(); 
	//		data += "\r\n"; 
	//		raf.seek(seekPos);
	//		raf.writeBytes(data);
	//		raf.close();
	//		return seekPos+data.length(); 
	//	}

	public void writePosting(ArrayList<Pair<InvertedIndex, Long>> entries) throws FileNotFoundException, IOException{

		File file = new File(__POSTINGS_FILENAME__);
		RandomAccessFile raf = new RandomAccessFile(file, "rw");
		String data = null;        

		long startTime = System.currentTimeMillis();

		for(Pair<InvertedIndex, Long> in : entries){

			data = in.getL().getTf()+"##"+in.getL().getPostions()+"\r\n";
			raf.seek(in.getR());
			raf.writeBytes(data);

		}

		long endTime = System.currentTimeMillis();

		long elapsedTime = endTime - startTime;

		System.out.println("WRITE POST TIME: " + elapsedTime/1000);

		raf.close();

	}




	/**
	 * =========================================================================
	 * 3) DOCUMENTS FILE => documents.idx (Random Access File)
	 *
	 * For each entry it stores: | DOCUMENT_ID (40 ASCII chars => 40 bytes) |
	 * Title (variable bytes / UTF-8) | Author_1,Author_2, ...,Author_k
	 * (variable bytes / UTF-8) | AuthorID_1, AuthorID_2, ...,Author_ID_k
	 * (variable size /ASCII) | Year (short => 2 bytes)| Journal Name (variable
	 * bytes / UTF-8) | The weight (norm) of Document (double => 8 bytes)|
	 * Length of Document (int => 4 bytes) | PageRank Score (double => 8 bytes
	 * => this will be used in the second phase of the project)
	 *
	 * ==> IMPORTANT NOTES
	 *
	 * For strings that have a variable size, just add as an int (4 bytes)
	 * prefix storing the size in bytes of the string. Also make sure that you
	 * use the correct representation ASCII (1 byte) or UTF-8 (2 bytes). For
	 * example the doc id is a hexadecimal hash so there is no need for UTF
	 * encoding
	 *
	 * Authors are separated by a comma
	 *
	 * Author ids are also separated with a comma
	 *
	 * The weight of the document will be computed after indexing the whole
	 * collection by scanning the whole postings list
	 *
	 * For now add 0.0 for PageRank score (a team will be responsible for
	 * computing it in the second phase of the project)
	 * @param totalDocuments 
	 * @return
	 */
	public Long writeDocument(S2TextualEntry entry, Long pos, int id, double pagerank) {

		File file = new File(__DOCUMENTS_FILENAME__);
		file.length();
		RandomAccessFile raf;
		String data = null; 
		try {
			raf = new RandomAccessFile(file, "rw");
			if(entry != null) {

				String title = entry.getTitle(); 
				//				List<Pair<String, List<String>>> authors = entry.getAuthors(); 
				int year = entry.getYear(); 
				//				
				int length = (entry.getPaperAbstract()+ " "+entry.getTitle()).split(" ").length; 
				data = id+"##"+title+"##"; 
				//
				//				if(!authors.isEmpty() || authors != null) {
				//					for (Pair<String, List<String>> author : authors) {
				//						data += author.getL()+","; 
				//						author.getR();
				//					}
				//				} 
				data += Integer.toString(year)+"##"; 
				data += Integer.toString(length)+"##"; 
				data += Double.toString(pagerank)+System.lineSeparator();
				//				if(entry.getPaperAbstract().isBlank() || entry.getPaperAbstract().isEmpty() ) data +="empty"+"##";
				//				else data += entry.getPaperAbstract()+"##";

				data = data.replaceAll("[^\\x00-\\x7F]", "").replaceAll(System.lineSeparator(), "")+System.lineSeparator();
				raf.seek(pos);
				raf.writeBytes(data);

			}

			raf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return pos+data.length(); 
	}


	// Method dumping
	public boolean dump(HashMap<String, InvertedIndex> data) {

		LinkedList<String> partial = new LinkedList<>();

		SortedSet<String> keys = new TreeSet<>(data.keySet());

		File file;

		if (id == 0) {

			// dump to INDEX_PATH
			file = new File(__INDEX_PATH__+id+".idx");

		} 
		else {

			// dump to INDEX_PATH/id
			file = new File(__INDEX_PATH__+id+".idx");

		}



		try {
			BufferedWriter bufferW = new BufferedWriter(new FileWriter(file));

			for (String key : keys) { 

				InvertedIndex value = data.get(key);
				double tf = value.getTf(); 
				double df = value.getDf();



				String towrite = key+"#"+tf+"#"+df+"#"+value.getPostions();




				partial.add(towrite);
			}

			for(String word : partial){

				bufferW.write(word);
				bufferW.newLine();

			}

			bufferW.flush();
			bufferW.close();

		} catch (IOException ex) {

			java.util.logging.Logger.getLogger(Index.class.getName()).log(Level.SEVERE, null, ex);

		}

		return false;
	}

	public void setID(int id) {
		Index.id = id;
	}

	public void writePartial(Queue<Integer> queue) throws IOException{
		__LOGGER__.info("Begin writing vocabulary and posting files ");
		Integer file = queue.element();
		long startTime = System.currentTimeMillis();
		String path = __INDEX_PATH__+ file + ".idx";

		File partVocab = new File(__INDEX_PATH__ + "vocabulary.idx");
		File partPost = new File(__INDEX_PATH__ + "postings.idx");

		FileWriter fstreamVoc = new FileWriter(partVocab, true);
		FileWriter fstreamPos = new FileWriter(partPost, true);
		BufferedWriter outVoc = new BufferedWriter(fstreamVoc);
		BufferedWriter outPos = new BufferedWriter(fstreamPos);

		FileInputStream partial = new FileInputStream(path);
		InputStreamReader reader = new InputStreamReader(partial);
		BufferedReader read = new BufferedReader(reader);



		String line, textVoc, textPost;

		Long pos = 0L;

		line = read.readLine();

		while(line != null){

			String[] split = line.split("#");

			textPost = split[1]+"##"+split[3]+"\n";
			textVoc = split[0]+"##"+split[2]+"##"+pos+"\n";
			pos += textPost.length();


			outVoc.write(textVoc); 
			outPos.write(textPost);

			line = read.readLine();

		}


		read.close();

		outVoc.flush();
		outVoc.close();

		outPos.flush();
		outPos.close();

		Files.delete(Paths.get(path));

		long endTime = System.currentTimeMillis();

		long timeElapsed = endTime - startTime;
		__LOGGER__.info(" : " + timeElapsed / 1000+ "Seconds");

	}



	/**
	 * Returns if index is partial
	 *
	 * @return
	 */
	public boolean isPartial() {
		return id != 0;
	}




}

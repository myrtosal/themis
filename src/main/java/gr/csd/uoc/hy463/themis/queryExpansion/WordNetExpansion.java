package gr.csd.uoc.hy463.themis.queryExpansion;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.Pointer;
import net.sf.extjwnl.data.PointerType;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.data.Word;
import net.sf.extjwnl.dictionary.Dictionary;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import gr.csd.uoc.hy463.themis.examples.EXTJWNLExample;


/**
 * Implementation of query expansion using an external java wordnet library usage
 *
 * @author Panagiotis Papadakos (papadako@ics.forth.gr)
 */

public class WordNetExpansion {
	// Get the POS represenation for wordnet

		private static final Logger LOGGER = LoggerFactory.getLogger(EXTJWNLExample.class);

		private Dictionary dictionary;

		public WordNetExpansion() {
			try {
				dictionary = Dictionary.getDefaultResourceInstance();
			} catch (JWNLException e) {
				e.printStackTrace();
			}
		}
		private static POS getPos(String taggedAs) {
			switch(taggedAs) {
			case "NN" :
			case "NNS" :
			case "NNP" :
			case "NNPS" :
				return POS.NOUN;
			case "VB" :
			case "VBD" :
			case "VBG" :
			case "VBN" :
			case "VBP" :
			case "VBZ" :
				return POS.VERB;
			case "JJ" :
			case "JJR" :
			case "JJS" :
				return POS.ADJECTIVE;
			case "RB" :
			case "RBR" :
			case "RBS" :
				return POS.ADVERB;
			default:
				return null;
			}
		}

		public String getDerivedAdjective(String noun) {
			try {
				IndexWord nounIW = dictionary.lookupIndexWord(POS.ADJECTIVE, noun);

				List<Synset> senses; 
				
				if(nounIW != null) {
					senses = nounIW.getSenses();

					Synset mainSense = senses.get(0);

					List<Pointer> pointers = mainSense.getPointers(PointerType.DERIVATION);

					for (Pointer pointer : pointers) {
						Synset derivedSynset = pointer.getTargetSynset();
						return derivedSynset.getWords().get(0).getLemma(); 
					}
				}
					
			} catch (JWNLException e) {
				e.printStackTrace();
			}
			return null;
		}

		/**
		 * Gets all synsets for the given word as VERB and NOUN.
		 *
		 * @param word
		 *            the word
		 * @return a representative word for each synset
		 * @throws JWNLException 
		 */
		public List<String> getAllSynsets(String query) throws JWNLException {
			List<String> synsets = new ArrayList<>();
			MaxentTagger maxentTagger = new MaxentTagger("edu/stanford/nlp/models/pos-tagger/english-left3words-distsim.tagger");
			String taggedQuery = maxentTagger.tagString(query);
			String[] eachTag = taggedQuery.split("\\s+");
			for (int i = 0; i < eachTag.length; i++) {
				String term = eachTag[i].split("_")[0];
				String tag = eachTag[i].split("_")[1];
				POS pos = getPos(tag);
				if(pos != null) {
					IndexWord iWord;
					iWord = dictionary.getIndexWord(pos, term);
					if(iWord != null) {
						for (Synset synset : iWord.getSenses()) {
							List<Word> words = synset.getWords();
							for (Word word : words) {
								synsets.add(word.getLemma());

							}
						}
					}
				}
			}

			return synsets;
		}
}

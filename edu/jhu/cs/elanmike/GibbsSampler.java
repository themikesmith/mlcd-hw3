package edu.jhu.cs.elanmike;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.StringTokenizer;

public class GibbsSampler {
	/**
	 * 1D array, indexed by document number, holding the number of words in each document
	 */
	private ArrayList<Integer> ndStar;
	
	/**
	 * 1D array indexed by document num, holds which collection each document is in
	 */
	private ArrayList<Integer> collections_d;
	/**
	 * 2D array of counts of words of each topic in each document
	 * first index is document, second index is topic
	 */
	private ArrayList< ArrayList<Integer> > ndk;
	/**
	 * 1D array counting the number of tokens that are assigned to each topic
	 * First index is topic
	 */
	private ArrayList<Integer> nkStar;
	/**
	 * 2D array counting the number of word tokens labeled with each topic  ( Corpus independent x = 0 )
	 * First index is topic, second is word index
	 */
	private ArrayList< ArrayList<Integer> > nkw;
	/**
	 * 3D array, number of collections, number of topics, number of word types of each topic in each collection   ( Corpus dependent x = 1 )
	 * First index is collection, second index is topic, third is word type
	 */
	private ArrayList< ArrayList< ArrayList<Integer> > > nckw;
	/**
	 * 2D array, number of times topic is mentioned in each collection
	 * First index is collection, second is topic
	 */
	private ArrayList< ArrayList<Integer> > nckStar;
	
	/**
	 * Stores the topic index of each word in each document.
	 * First index is document, second word index
	 */
	private ArrayList< ArrayList<Integer> > zdi;
	/**
	 * Stores the flag of global vs collection-specific for each word in each document
	 * First index is document, second word index
	 */
	private ArrayList< ArrayList<Integer> > xdi;
	/**
	 * Stores the wordIntValue for each word in each document
	 * First index is document, second word index
	 */
	private ArrayList< ArrayList<Integer> > wdi;
	
	/**
	 * 1D array indexed by document num, holds which collection each document is in
	 * for test data counts
	 */
	private ArrayList<Integer> collections_dTest;
	/**
	 * 1D array, indexed by document number, holding the number of words in each document
	 * for test data counts
	 */
	private ArrayList<Integer> ndStarTest;
	/**
	 * 2D array of counts of words of each topic in each document
	 * first index is document, second index is topic
	 * for test data counts
	 */
	private ArrayList< ArrayList<Integer> > ndkTest;
	/**
	 * Stores the topic index of each word in each document.
	 * First index is document, second word index
	 * for test data counts
	 */
	private ArrayList< ArrayList<Integer> > zdiTest;
	/**
	 * Stores the flag of global vs collection-specific for each word in each document
	 * First index is document, second word index
	 * for test data counts
	 */
	private ArrayList< ArrayList<Integer> > xdiTest;
	/**
	 * Stores the wordIntValue for each word in each document
	 * First index is document, second word index
	 * for test data counts
	 */
	private ArrayList< ArrayList<Integer> > wdiTest;
	
	
	private ArrayList<ArrayList<Probability>> theta_dk;
	private ArrayList<ArrayList<Probability>> phi_kw;
	private ArrayList< ArrayList< ArrayList<Probability> > >  phi_ckw;

	private ArrayList<ArrayList<Probability>> theta_dkTest;
	
	private ArrayList<ArrayList<Probability>> theta_dkMean;
	private ArrayList<ArrayList<Probability>> phi_kwMean;
	private ArrayList< ArrayList< ArrayList<Probability> > >  phi_ckwMean;
	
	/**
	 * The number of topics.
	 */
	private int numTopics;
	/**
	 * The parameter lambda
	 */
	private double lambda;
	/**
	 * The parameter alpha
	 */
	private double alpha;
	/**
	 * The parameter beta.
	 */
	private double beta;
	/**
	 * Number of collections
	 */
	private int numCollections = 2;
	private enum SamplerType {
		COLLAPSED, BLOCKED
	}
	private SamplerType type;
	private static final boolean COUNTS_ARE_SEPARATED = false;
	
	/**
	 * Map from word string to index
	 */
	private HashMap<String,Integer> WordToIndex;
	
	private Random rand;
	
	GibbsSampler(SamplerType type, int numCollections, int numTopics, 
			double lambda, double alpha, double beta) {
//		System.err.println("\n===[ New GibbsSampler ]===");
//		System.out.printf("type = %d \nnumCollections = %d  \nnumTopics = %d \nlambda = %f \nalpha = %f \nbeta = %f",
//				type.ordinal(),numCollections,numTopics,lambda,alpha,beta);
		
		WordToIndex = new HashMap<String,Integer>();
		ndStar = new ArrayList<Integer>();
		collections_d = new ArrayList<Integer>();
		ndk = new ArrayList<ArrayList<Integer> >();
		nkStar = new ArrayList<Integer>();
		nkw = new ArrayList<ArrayList<Integer> >();
		nckw = new ArrayList< ArrayList< ArrayList<Integer> > >();
		nckStar = new ArrayList<ArrayList<Integer> >();
		
		collections_dTest = new ArrayList<Integer>();
		ndStarTest = new ArrayList<Integer>();
		ndkTest = new ArrayList<ArrayList<Integer> >();
		
		xdi = new ArrayList<ArrayList<Integer> >();
		zdi = new ArrayList<ArrayList<Integer> >();
		wdi = new ArrayList<ArrayList<Integer> >();
		
		xdiTest = new ArrayList<ArrayList<Integer> >();
		zdiTest = new ArrayList<ArrayList<Integer> >();
		wdiTest = new ArrayList<ArrayList<Integer> >();
		
		theta_dk = new ArrayList<ArrayList<Probability>>();
		phi_kw = new ArrayList<ArrayList<Probability>>();
		phi_ckw = new ArrayList< ArrayList< ArrayList<Probability> > >();
		theta_dkTest = new ArrayList<ArrayList<Probability>>();
		
		theta_dkMean = new ArrayList<ArrayList<Probability>>();
		phi_kwMean = new ArrayList<ArrayList<Probability>>();
		phi_ckwMean = new ArrayList< ArrayList< ArrayList<Probability> > >();
		
		this.type = type;
		this.numCollections = numCollections;
		this.numTopics = numTopics;
		this.lambda = lambda;
		this.alpha = alpha;
		this.beta = beta;
		
		// init all things collection , topic
		for(int c = 0; c < numCollections; c++) {
			nckStar.add(new ArrayList<Integer>());
			nckw.add(new ArrayList< ArrayList<Integer>>());
			
			phi_ckw.add(new ArrayList<ArrayList<Probability>>());
			phi_ckwMean.add(new ArrayList<ArrayList<Probability>>());
			
			for(int k = 0; k < numTopics; k++) {
				nckStar.get(c).add(0);
				nckw.get(c).add(new ArrayList<Integer>());
				
				phi_ckw.get(c).add(new ArrayList<Probability>());
				phi_ckwMean.get(c).add(new ArrayList<Probability>());
			}
		}
		
		// init everything with index topic
		for(int k = 0; k < numTopics; k++) {
			nkStar.add(0);
			nkw.add(new ArrayList<Integer>());
			
			phi_kw.add(new ArrayList<Probability>());
			phi_kwMean.add(new ArrayList<Probability>());
		}
		
		rand = new Random();
	}
	
	
	private void processWord(String word, int collectionIdx, int docIdx, int wordIdx){
		//System.out.printf("Processing: %s, c=%d d=%d i=%d \n",word,collectionIdx,docIdx,wordIdx);
		
		
		//NEW DOCUMENT
		if(docIdx >= collections_d.size()){
			//System.out.printf("New document!\n");

			ndStar.add(0);
			collections_d.add(collectionIdx);
			
			ndk.add(new ArrayList<Integer>());
			for(int k = 0; k<numTopics; k++){
				ndk.get(docIdx).add(0);
			}
			
			zdi.add(new ArrayList<Integer>());
			xdi.add(new ArrayList<Integer>());
			wdi.add(new ArrayList<Integer>());
			
			theta_dk.add(new ArrayList<Probability>());
			theta_dkTest.add(new ArrayList<Probability>());
			theta_dkMean.add(new ArrayList<Probability>());
			for(int k = 0; k< numTopics; k++) {
				theta_dk.get(docIdx).add(new Probability(0.0));
				theta_dkTest.get(docIdx).add(new Probability(0.0));
				theta_dkMean.get(docIdx).add(new Probability(0.0));
			}
		}
		
		//NEW WORD
		if(!WordToIndex.containsKey(word)){
			//System.out.printf("New word!\n");
			WordToIndex.put(word, WordToIndex.size());
			//add a new word row to n^{k}_{w} and n^{(c),k}_{w}
			for(int k = 0; k<numTopics; k++){
				nkw.get(k).add(0);
				
				phi_kw.get(k).add(new Probability(0));
				phi_kwMean.get(k).add(new Probability(0));
				for(int c = 0; c< numCollections; c++){
					nckw.get(c).get(k).add(0);
					
					phi_ckw.get(c).get(k).add(new Probability(0));
					phi_ckwMean.get(c).get(k).add(new Probability(0));
				}
			}
		}
		
		int wordIntValue = WordToIndex.get(word);
		int x = rand.nextInt(2);
		int z = rand.nextInt(numTopics);
		
		xdi.get(docIdx).add(x);
		zdi.get(docIdx).add(z);
		wdi.get(docIdx).add(wordIntValue);
		
		if(COUNTS_ARE_SEPARATED){
			if(x == 0){ // using collection-independent counts
				increment(nkw,z,wordIntValue);
				increment(nkStar,z);
			}else{ // using collection-dependent counts
				increment(nckw,collectionIdx,z,wordIntValue);
				increment(nckStar, collectionIdx, z);
			}
		}else{
			increment(nkw,z,wordIntValue);
			increment(nkStar,z);
			increment(nckw,collectionIdx,z,wordIntValue);
			increment(nckStar, collectionIdx, z);
		}
		increment(ndStar,docIdx);
		//System.out.printf("d = %d  z = %d\n",docIdx,z);
		increment(ndk,docIdx,z);
		
		
	}
	
	/**
	 * Process a word that we see when reading data.
	 * increment the appropriate counts.
	 * @param word
	 * @param collectionIdx
	 * @param docIdx
	 * @param wordIdx
	 */
	private void processWordTest(String word, int collectionIdx, int docIdx, int wordIdx){
		//System.out.printf("Processing: %s, c=%d d=%d i=%d \n",word,collectionIdx,docIdx,wordIdx);
		
		//NEW DOCUMENT
		if(docIdx >= collections_dTest.size()){
			//System.out.printf("New document!\n");
			ndStarTest.add(0);
			collections_dTest.add(collectionIdx);
			
			ndkTest.add(new ArrayList<Integer>());
			for(int k = 0; k<numTopics; k++){
				ndkTest.get(docIdx).add(0);
			}
			
			zdiTest.add(new ArrayList<Integer>());
			xdiTest.add(new ArrayList<Integer>());
			wdiTest.add(new ArrayList<Integer>());
			
			theta_dkTest.add(new ArrayList<Probability>());
			for(int k = 0; k< numTopics; k++) {
				theta_dkTest.get(docIdx).add(new Probability(0.0));
			}
		}
		
		//NEW WORD
		if (!WordToIndex.containsKey(word)) {
			// System.out.printf("New word!\n");
			WordToIndex.put(word, WordToIndex.size());
			// add a new word row to n^{k}_{w} and n^{(c),k}_{w}
			for (int k = 0; k < numTopics; k++) {
				nkw.get(k).add(0);
				
				phi_kw.get(k).add(new Probability(0));
				phi_kwMean.get(k).add(new Probability(0));
				for (int c = 0; c < numCollections; c++) {
					nckw.get(c).get(k).add(0);
					phi_ckw.get(c).get(k).add(new Probability(0));
					phi_ckwMean.get(c).get(k).add(new Probability(0));
				}
			}
		}
		
		int wordIntValue = WordToIndex.get(word);
		int x = rand.nextInt(2);
		int z = rand.nextInt(numTopics);
		
		xdiTest.get(docIdx).add(x);
		zdiTest.get(docIdx).add(z);
		wdiTest.get(docIdx).add(wordIntValue);
		
		increment(ndStarTest,docIdx);
		//System.out.printf("d = %d  z = %d\n",docIdx,z);
		increment(ndkTest,docIdx,z);
	}
	
	private Integer getValue(ArrayList a, int... indicies){
		int depth = 0;
		ArrayList curArray = a;
		
		while(depth <indicies.length - 1){
			//System.out.printf("Depth(%d) = %d\n",indicies[depth], depth);
			curArray = (ArrayList) curArray.get(indicies[depth]);
			depth++;
		}
		
		//System.out.printf("LastDepth(%d) = %d\n",indicies[depth], depth);
		return (Integer) curArray.get(indicies[depth]);
	}
	
	private void setValue(ArrayList a, Integer val, int... indicies){
		int depth = 0;
		ArrayList curArray = a;
		
		while(depth <indicies.length - 1){
			curArray = (ArrayList) curArray.get(indicies[depth]);
			depth++;
		}
		curArray.set(indicies[depth],val);
	}
	
	private Probability getProbability(ArrayList a, int... indicies){
		int depth = 0;
		ArrayList curArray = a;
		
		while(depth <indicies.length - 1){
			curArray = (ArrayList) curArray.get(indicies[depth]);
			depth++;
		}
		return (Probability) curArray.get(indicies[depth]);
	}
	
	private void setProbability(ArrayList a, Probability val, int... indicies){
		int depth = 0;
		ArrayList curArray = a;
		
		while(depth <indicies.length - 1){
			curArray = (ArrayList) curArray.get(indicies[depth]);
			depth++;
		}
		curArray.set(indicies[depth],val);
	}
	
	private void increment(ArrayList a, int... indicies){
		int depth = 0;
		ArrayList curArray = a;
		
		while(depth <indicies.length - 1){
			//System.out.printf("Getting %d of %d\n",indicies[depth],curArray.size());
			curArray = (ArrayList) curArray.get(indicies[depth]);
			depth++;
		}
		//System.out.printf("(Last)Getting %d of %d\n",indicies[depth],curArray.size());
		((ArrayList<Integer>)curArray).set(indicies[depth],(Integer)curArray.get(indicies[depth])+1);
	}
	
	private void decrement(ArrayList a, int... indicies){
		int depth = 0;
		ArrayList curArray = a;
		
		while(depth <indicies.length - 1){
			curArray = (ArrayList) curArray.get(indicies[depth]);
			depth++;
		}
		((ArrayList<Integer>)curArray).set(indicies[depth],(Integer)curArray.get(indicies[depth])-1);
	}
	
	/**
	 * Reads in our training file.
	 * Each line represents a document.
	 * The first token in a document is the collection from which it came
	 * The rest of the tokens are words to be processed.
	 * Processes all the words.
	 * @param filename
	 * @throws IOException
	 */
	private void readTrainingFile(String filename) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(filename));
		String line;
		int d = 0;
		while((line = in.readLine()) != null) {
			int space = line.indexOf(' ');
			String collection = line.substring(0, space);
			int collectionIndex = Integer.parseInt(collection);
			String document = line.substring(space);
			StringTokenizer st = new StringTokenizer(document);
			int i = 0;
			while(st.hasMoreElements()) {
				processWord(st.nextToken(), collectionIndex, d, i);
				i++;
			}
			d++;
		}
		in.close();
	}
	/**
	 * Reads in our test file.
	 * Each line represents a document.
	 * The first token in a document is the collection from which it came
	 * The rest of the tokens are words to be processed.
	 * Processes all the words.
	 * @param filename
	 * @throws IOException
	 */
	private void readTestFile(String filename) throws IOException {
		
		BufferedReader in = new BufferedReader(new FileReader(filename));
		String line;
		int d = 0;
		while((line = in.readLine()) != null) {
			int space = line.indexOf(' ');
			String collection = line.substring(0, space);
			int collectionIndex = Integer.parseInt(collection);
			String document = line.substring(space);
			StringTokenizer st = new StringTokenizer(document);
			int i = 0;
			while(st.hasMoreElements()) {
				processWordTest(st.nextToken(), collectionIndex, d, i);
				i++;
			}
			d++;
		}
		in.close();
	}
	/**
	 * Updates the counts to exclude the current assignment of the given word 
	 * in the given document.
	 * @param docIdx
	 * @param wordIdx
	 */
	private void updateCountsExcludeCurrentAssignment(int docIdx, int wordIdx) {
//		System.out.printf("update counts exclude. docid:%d wordid:%d\n", docIdx, wordIdx);
		// query zdi and xdi, and get word value and doc collection
		int z = getValue(zdi, docIdx, wordIdx), // topic index
			x = getValue(xdi, docIdx, wordIdx), // global flag
			w = getValue(wdi, docIdx, wordIdx), // word value
			c = getValue(collections_d, docIdx); // collection id
		
		
		// decrement topic count per doc, ndk
		decrement(ndk, docIdx, z);
		decrement(ndStar, docIdx);
		
		if(COUNTS_ARE_SEPARATED){
			if(x == 0) { // decrement global
				// and decrement topic count per word, nkstar
				decrement(nkw, z, w);
				decrement(nkStar, z);
			}else { // collection-specific
				// and decrement nckstar
				decrement(nckw, c, z, w);
				decrement(nckStar, c, z);
			}
		}else{
			// and decrement topic count per word, nkstar
			decrement(nkw, z, w);
			decrement(nkStar, z);
			// and decrement nckstar
			decrement(nckw, c, z, w);
			decrement(nckStar, c, z);			
		}
	}
	/**
	 * Updates the counts to include the newly sampled assignment of the
	 * given word in the given document.
	 * @param docIdx
	 * @param wordIdx
	 */
	private void updateCountsNewlySampledAssignment(int docIdx, int wordIdx) {
//		System.out.printf("update counts new sample. docid:%d wordid:%d\n", docIdx, wordIdx);
		// query zdi and xdi, and get word value and doc collection
		int z = getValue(zdi, docIdx, wordIdx), // topic index
		x = getValue(xdi, docIdx, wordIdx), // global flag
		w = getValue(wdi, docIdx, wordIdx), // word value
		c = getValue(collections_d, docIdx); // collection id
		
		
		// decrement topic count per doc, ndk
		increment(ndk, docIdx, z);
		increment(ndStar, docIdx);
		
		if(COUNTS_ARE_SEPARATED){
			if(x == 0) { // decrement global
				// and decrement topic count per word, nkstar
				increment(nkw, z, w);
				increment(nkStar, z);
			}else { // collection-specific
				// and decrement nckstar
				increment(nckw, c, z, w);
				increment(nckStar, c, z);
			}
		}else{
			// and decrement topic count per word, nkstar
			increment(nkw, z, w);
			increment(nkStar, z);
			// and decrement nckstar
			increment(nckw, c, z, w);
			increment(nckStar, c, z);			
		}
	}
	
	/**
	 * Updates the counts to exclude the current assignment of the given word 
	 * in the given document.
	 * @param docIdx
	 * @param wordIdx
	 */
	private void updateTestCountsExcludeCurrentAssignment(int docIdx, int wordIdx) {
//		System.out.printf("update counts exclude. docid:%d wordid:%d\n", docIdx, wordIdx);
		// query zdi and xdi, and get word value and doc collection
		int z = getValue(zdiTest, docIdx, wordIdx); // collection id
		// decrement topic count per doc, ndk
		decrement(ndkTest, docIdx, z);
		decrement(ndStarTest, docIdx);
	}
	/**
	 * Updates the counts to include the newly sampled assignment of the
	 * given word in the given document.
	 * @param docIdx
	 * @param wordIdx
	 */
	private void updateTestCountsNewlySampledAssignment(int docIdx, int wordIdx) {
//		System.out.printf("update counts new sample. docid:%d wordid:%d\n", docIdx, wordIdx);
		// query zdi and xdi, and get word value and doc collection
		int z = getValue(zdiTest, docIdx, wordIdx); // collection id
		// increment topic count per doc, ndk
		increment(ndkTest, docIdx, z);
		increment(ndStarTest, docIdx);
	}
	
	/**
	 * the vocab size is the size of the set of words we know, + 1 for OOV
	 * @return the vocab size.
	 */
	private int getVocabSize() {
//		return WordToIndex.keySet().size() + 1;
		return WordToIndex.keySet().size();
	}
	/**
	 * Get the probability that z_{d,i} = k
	 * if xdi = 0
	 * (ndk + alpha) / (ndstar + K alpha) *
	 * 	(nkw + beta) / (nkstar + V beta)
	 * 
	 * or if xdi = 1
	 * 
	 * (ndk + alpha) / (ndstar + K alpha) *
	 * 	(nckw + beta) / (nckstar + V beta)
	 * @param d document index
	 * @param i index of word in doc
	 * @param w word index
	 * @param xdi xdi value
	 * @param k topic index
	 * @return the probability
	 */
	private Probability getPZdiEqualsK(int d, int i, int w, int xdi, int k) {
//		System.out.printf("get p zdi = k: d:%d i:%d w:%d xdi:%d k:%d\n",
//			d, i, w, xdi, k);
		// ndk + alpha
//		System.out.println("alpha:"+alpha);
		Probability a = new Probability(alpha);
		a = a.add(new Probability(getValue(ndk, d, k)));
//		System.out.println("ndk:"+getValue(ndk, d, k));
//		System.out.println("alpha + ndk:"+a);
		// ndstar + K * alpha
		Probability b = new Probability(numTopics);
//		System.out.println("K:"+b);
		b = b.product(new Probability(alpha));
//		System.out.println("alpha:"+alpha);
//		System.out.println("ndstar:"+getValue(ndStar, d));
		b = b.add(new Probability(getValue(ndStar, d)));
//		System.out.println("K * alpha + ndstar:"+b);
		// a / b
		a = a.divide(b);
		if(a.getLogProb() == Double.NaN) {
			a = Probability.ZERO;
		}
//		System.out.println("(alpha + ndk) / (K * alpha + ndstar):"+a);
		if(xdi == 0) {
			// nkw + beta
			Probability c = new Probability(beta);
			c = c.add(new Probability(getValue(nkw, k, w)));
			// nkstar + V * beta
			Probability f = new Probability(getVocabSize());
			f = f.product(new Probability(beta));
			f = f.add(new Probability(getValue(nkStar, k)));
			// a / b
			c = c.divide(f);
			if(c.getLogProb() == Double.NaN) {
				c = Probability.ZERO;
			}
			return a.product(c);
		}
		else {
			int coll = getValue(collections_d, d);
			// nckw + beta
			Probability c = new Probability(beta);
			c = c.add(new Probability(getValue(nckw, coll, k, w)));
			// nckstar + V * beta
			Probability f = new Probability(getVocabSize());
			f = f.product(new Probability(beta));
			f = f.add(new Probability(getValue(nckStar, coll, k)));
			// c / f
			c = c.divide(f);
			if(c.getLogProb() == Double.NaN) {
				c = Probability.ZERO;
			}
			return a.product(c);
		}
	}
	/**
	 * Get the probability that x_{d,i} = v
	 * if v = 0
	 * (nkw + beta) / (nkstar + V beta) * (1 - lambda)
	 * 
	 * or if v = 1
	 * 
	 * (nckw + beta) / (nckstar + V beta) * lambda
	 * 
	 * @param d document index
	 * @param i index of word in doc
	 * @param w word index
	 * @param zdi zdi value = k
	 * @param v 0 or 1
	 * @return the probability
	 */
	private Probability getPXdiEqualsV(int d, int i, int w, int zdi, int v) {
//		System.out.printf("get p xdi = k: d:%d i:%d w:%d zdi:%d v:%d\n",
//				d, i, w, zdi, v);
		int k = zdi;
		Probability multiplier;
		if(v == 0) {
			multiplier = new Probability((1-lambda));
//			System.out.println("multiplier:"+multiplier);
//			System.out.println("beta:"+beta);
			// nkw + beta
			Probability c = new Probability(beta);
//			System.out.println("beta:"+c);
			c = c.add(new Probability(getValue(nkw, k, w)));
//			System.out.println("nkw:"+getValue(nkw, k, w));
//			System.out.println("beta + nkw:"+c);
			// nkstar + V * beta
			Probability f = new Probability(getVocabSize());
			f = f.product(new Probability(beta));
			f = f.add(new Probability(getValue(nkStar, k)));
			// a / b
//			System.out.println("(beta + nkw) / (V * beta + nkstar):"+f);
			c = c.divide(f);
			if(c.getLogProb() == Double.NaN) {
				c = Probability.ZERO;
			}
			return multiplier.product(c);
		}
		else if(v == 1) {
			multiplier = new Probability(lambda);
//			System.out.println("multiplier:"+multiplier);
			int coll = getValue(collections_d, d);
//			System.out.println("c:"+coll);
			// nckw + beta
			Probability c = new Probability(beta);
//			System.out.println("beta:"+beta);
//			System.out.println("nckw:"+getValue(nckw, coll, k, w));
			c = c.add(new Probability(getValue(nckw, coll, k, w)));
//			System.out.println("beta + nckw:"+c);
			// nckstar + V * beta
			Probability f = new Probability(getVocabSize());
//			System.out.println("V:"+f);
//			System.out.println("beta:"+beta);
			f = f.product(new Probability(beta));
//			System.out.println("V * beta:"+f);
//			System.out.println("nckstar:"+getValue(nckStar, coll, k));
			f = f.add(new Probability(getValue(nckStar, coll, k)));
//			System.out.println("V * beta + nckstar:"+f);
			// c / f
			c = c.divide(f);
			if(c.getLogProb() == Double.NaN) {
				c = Probability.ZERO;
			}
//			System.out.println("(beta + nckw) / (V * beta + nckstar):"+f);
			return multiplier.product(c);
		}
		else {
			System.err.println("x can only be 0 or 1: specified:"+v);
			return null; // throw error, x can only be 0 or 1
		}
	}

	/**
	 * Get the probability that z_{d,i} = k
	 * if xdi = 0
	 * (ndk + alpha) / (ndstar + K alpha) *
	 * 	(nkw + beta) / (nkstar + V beta)
	 * 
	 * or if xdi = 1
	 * 
	 * (ndk + alpha) / (ndstar + K alpha) *
	 * 	(nckw + beta) / (nckstar + V beta)
	 * @param d document index
	 * @param i index of word in doc
	 * @param w word index
	 * @param xdi xdi value
	 * @param k topic index
	 * @return the probability
	 */
	private Probability getTestPZdiEqualsK(int d, int i, int w, int xdi, int k) {
//		System.out.printf("get p zdi = k: d:%d i:%d w:%d xdi:%d k:%d\n",
//			d, i, w, xdi, k);
		// ndk + alpha
		Probability a = new Probability(alpha);
//		System.out.println("alpha:"+a);
//		System.out.printf("ndk d:%d k:%d = %d\n", d, k, getValue(ndkTest, d, k));
		a = a.add(new Probability(getValue(ndkTest, d, k)));
//		System.out.println("alpha + ndk:"+a);
		// ndstar + K * alpha
		Probability b = new Probability(numTopics);
//		System.out.println("K:"+b);
		b = b.product(new Probability(alpha));
//		System.out.println("alpha:"+alpha);
//		System.out.println("ndstar:"+getValue(ndStarTest, d));
		b = b.add(new Probability(getValue(ndStarTest, d)));
//		System.out.println("K * alpha + ndstar:"+b);
		// a / b
		a = a.divide(b);
		if(a.getLogProb() == Double.NaN) {
			a = Probability.ZERO;
		}
//		System.out.println("(alpha + ndk) / (K * alpha + ndstar):"+a);
		if(xdi == 0) {
			return a.product(getProbability(phi_kw, k, w));
		}
		else {
			int c = getValue(collections_dTest, d);
			return a.product(getProbability(phi_ckw, c, k, w));
		}
	}
	/**
	 * Get the probability that x_{d,i} = v
	 * if v = 0
	 * (nkw + beta) / (nkstar + V beta) * (1 - lambda)
	 * 
	 * or if v = 1
	 * 
	 * (nckw + beta) / (nckstar + V beta) * lambda
	 * 
	 * @param d document index
	 * @param i index of word in doc
	 * @param w word index
	 * @param zdi zdi value = k
	 * @param v 0 or 1
	 * @return the probability
	 */
	private Probability getTestPXdiEqualsV(int d, int i, int w, int zdi, int v) {
//		System.out.printf("get p xdi = k: d:%d i:%d w:%d zdi:%d v:%d\n",
//				d, i, w, zdi, v);
		int k = zdi;
		Probability multiplier;
		if(v == 0) {
			multiplier = new Probability((1-lambda));
			return multiplier.product(getProbability(phi_kw, k, w));
		}
		else if(v == 1) {
			multiplier = new Probability(lambda);
			int c = getValue(collections_dTest, d);
			return multiplier.product(getProbability(phi_ckw, c, k, w));
		}
		else {
			System.err.println("x can only be 0 or 1: specified:"+v);
			return null; // throw error, x can only be 0 or 1
		}
	}
	
	/**
	 * Computes and returns our estimated theta_{d,k}
	 * @param d
	 * @param k
	 * @return
	 */
	private Probability getThetadk(int d, int k) {
		// ndk + alpha
		Probability a = new Probability(alpha);
		a = a.add(new Probability(getValue(ndk, d, k)));
		// ndstar + K * alpha
		Probability b = new Probability(numTopics);
		b = b.product(new Probability(alpha));
		b = b.add(new Probability(getValue(ndStar, d)));
		// a / b
		a = a.divide(b);
		return a;
	}
	/**
	 * Computes and returns our estimated theta_{d,k}
	 * @param d
	 * @param k
	 * @return
	 */
	private Probability getThetadkTest(int d, int k) {
		// ndk + alpha
		Probability a = new Probability(alpha);
		a = a.add(new Probability(getValue(ndkTest, d, k)));
		// ndstar + K * alpha
		Probability b = new Probability(numTopics);
		b = b.product(new Probability(alpha));
		b = b.add(new Probability(getValue(ndStarTest, d)));
		// a / b
		a = a.divide(b);
		return a;
	}
	/**
	 * Computes and returns our estimated phi_{k,w}
	 * @param k
	 * @param w
	 * @return
	 */
	private Probability getPhikw(int k, int w) {
		// nkw + beta
		Probability c = new Probability(beta);
		c = c.add(new Probability(getValue(nkw, k, w)));
		// nkstar + V * beta
		Probability f = new Probability(getVocabSize());
		f = f.product(new Probability(beta));
		f = f.add(new Probability(getValue(nkStar, k)));
		// a / b
		c = c.divide(f);
		return c;
	}
	/**
	 * Computes and returns our estimated phi_{k,w}
	 * @param c
	 * @param k
	 * @param w
	 * @return
	 */
	private Probability getPhickw(int c, int k, int w) {
		int coll = c;
		// nckw + beta
		Probability p = new Probability(beta);
		p = p.add(new Probability(getValue(nckw, coll, k, w)));
		// nckstar + V * beta
		Probability f = new Probability(getVocabSize());
		f = f.product(new Probability(beta));
		f = f.add(new Probability(getValue(nckStar, coll, k)));
		// c / f
		p = p.divide(f);
		return p;
	}
	/**
	 * Runs an iteration of test sampling
	 */
	private void sampleTestIter() {
		
		for(int d = 0; d < ndkTest.size(); d++) {
			int numWordsInD = ndStarTest.get(d);
//			System.out.printf("\nd:%d numwords:%d\n",d, numWordsInD);
			for(int i = 0; i < numWordsInD; i++) {
				int w = getValue(wdiTest, d, i),
					v = getValue(xdiTest, d, i);
//				System.out.printf("\ni:%d w:%d\n",i,w);
				// exclude current assignment
				updateTestCountsExcludeCurrentAssignment(d, i);
				// randomly sample a new value for zdi
				Probability[] totalProbs = new Probability[numTopics];
				// find total
				Probability totalProb = new Probability(0);
				int sampledZdi = -1;
				for(int k = 0; k < numTopics; k++) {
//					System.out.printf("\nk:%d\ntotal before:%s\n", k, totalProb);
					Probability curr = getTestPZdiEqualsK(d, i, w, v, k);
//					System.out.println("curr:"+curr);
					totalProb = totalProb.add(curr);
//					System.out.println("total after:"+totalProb);
					totalProbs[k] = totalProb;
				}
				// get random num
				double p = rand.nextDouble();
				Probability marker = new Probability(p);
				marker = marker.product(totalProb);
//				System.out.println("marker:"+marker);
				for(int k = 0; k < numTopics; k++) {
//					System.out.println("kth total:"+totalProbs[k]);
					if(totalProbs[k].getLogProb() > marker.getLogProb()) {
						// stop. we have sampled this value of k
						sampledZdi = k;
						break;
					}
				}
//				System.out.println("sampled k = "+sampledZdi);
				// randomly sample a new value for xdi, using newly sampled zdi
				p = rand.nextDouble();
				marker = new Probability(p);
				Probability xzero = getTestPXdiEqualsV(d, i, w, sampledZdi, 0);
				Probability xone = getTestPXdiEqualsV(d, i, w, sampledZdi, 1);
				marker = marker.product(xzero.add(xone));
				int sampledXdi = -1;
				if(xzero.getLogProb() > marker.getLogProb()) {
					// we have sampled xdi = 0
					sampledXdi = 0;
				}
				else {
					sampledXdi = 1;
				}
//				System.out.println("sampled x = "+sampledXdi);
				// set zdi and xdi
				setValue(zdiTest, sampledZdi, d, i);
				setValue(xdiTest, sampledXdi, d, i);
				// and update counts
				updateTestCountsNewlySampledAssignment(d, i);
			}
		}
	}
	/**
	 * Runs an iteration of training asmpling
	 */
	private void sampleTrainingIter() {
		
		// Loop through all documents
		for(int d = 0; d < ndk.size(); d++) {
//			System.out.printf("d:%d ",d);
			int numWordsInD = ndStar.get(d);
			
			// Loop through all words
			for(int i = 0; i < numWordsInD; i++) {
				int w = getValue(wdi, d, i),
					v = getValue(xdi, d, i);
//				System.out.printf("i:%d w:%d\n",i,w);
				// exclude current assignment
				updateCountsExcludeCurrentAssignment(d, i);
				// randomly sample a new value for zdi
				Probability[] totalProbs = new Probability[numTopics];
				// find total
				Probability totalProb = new Probability(0);
				int sampledZdi = -1;
				for(int k = 0; k < numTopics; k++) {
//					System.out.printf("\nk:%d\ntotal before:%s\n", k, totalProb);
					Probability curr = getPZdiEqualsK(d, i, w, v, k);
//					System.out.println("curr:"+curr);
					totalProb = totalProb.add(curr);
//					System.out.println("total after:"+totalProb);
					totalProbs[k] = totalProb;
				}
				// get random num
				double p = rand.nextDouble();
				Probability marker = new Probability(p);
				marker = marker.product(totalProb);
//				System.out.println("marker:"+marker);
				for(int k = 0; k < numTopics; k++) {
//					System.out.println("kth total:"+totalProbs[k]);
					if(totalProbs[k].getLogProb() > marker.getLogProb()) {
						// stop. we have sampled this value of k
						sampledZdi = k;
						break;
					}
				}
//				System.out.println("sampled k = "+sampledZdi);
				// randomly sample a new value for xdi, using newly sampled zdi
				p = rand.nextDouble();
				marker = new Probability(p);
				Probability xzero = getPXdiEqualsV(d, i, w, sampledZdi, 0);
				Probability xone = getPXdiEqualsV(d, i, w, sampledZdi, 1);
				marker = marker.product(xzero.add(xone));
				int sampledXdi = -1;
				if(xzero.getLogProb() > marker.getLogProb()) {
					// we have sampled xdi = 0
					sampledXdi = 0;
				}
				else {
					sampledXdi = 1;
				}
//				System.out.println("sampled x = "+sampledXdi);
				// set zdi and xdi
				setValue(zdi, sampledZdi, d, i);
				setValue(xdi, sampledXdi, d, i);
				// and update counts
				updateCountsNewlySampledAssignment(d, i);
			}
		}
	}
	/**
	 * Run sampling algorithm.
	 * Each iteration runs on training data,
	 * then on test data, then computes likelihoods.
	 * prints out likelihoods of training and test data
	 * to output files, naming them appropriately
	 * @param totalIters
	 * @param totalBurnin
	 * @param outfilename the base output file name
	 * @throws IOException 
	 */
	private void runSampling(int totalIters, int totalBurnin, String outfilename) throws IOException {
//		System.out.printf("run sampling! totaliters:%d burn in:%d\n", totalIters, totalBurnin);
		File trainLL = new File(outfilename+"-trainll"),
				testLL = new File(outfilename+"-testll");
		if(trainLL.exists()) trainLL.delete();
		if(testLL.exists()) testLL.delete();
		PrintWriter pwTrainLL = new PrintWriter(trainLL),
			pwTestLL = new PrintWriter(testLL);
		// and now run sampling
		for (int t = 0; t < totalIters; t++) {
//			System.out.printf("t:%d ",t);
			// sample training
			sampleTrainingIter();
			// estimate map theta_dk
			for(int d = 0; d < collections_d.size(); d++) {
				for(int k = 0; k < numTopics; k++) {
					Probability thetadk = getThetadk(d, k);
					setProbability(theta_dk, thetadk, d, k);
				}
			}
			// estimate map phi_dk
			for(int k = 0; k < numTopics; k++) {
				for(int w = 0; w < WordToIndex.size(); w++) {
					Probability phikw = getPhikw(k, w);
					setProbability(phi_kw, phikw, k, w);
					for(int c = 0; c < numCollections; c++) {
						Probability phickw = getPhickw(c, k, w);
						setProbability(phi_ckw, phickw, c, k, w);
					}
				}
			}
			//SANITY CHECKS
			
			double epsilon = 1.0 - 0.99999999999999;
			
			//Theta_dk should sum to 1 over topics
			for(int d = 0; d < collections_d.size(); d++) {
				Probability curDocSum = Probability.ZERO;
				for(int k = 0; k < numTopics; k++) {
					Probability curProb = getProbability(theta_dk, d,k);
					curDocSum = curDocSum.add(curProb);
				}
				if(Math.abs( 1.0 - Math.exp(curDocSum.getLogProb()) ) > epsilon){
					System.err.println("Error: Theta_dk check (should be near one):\t" + curDocSum);
				}
			}
			
			
			//Phi_kw should sum to 1 over words
			for(int k = 0; k < numTopics; k++) {
				Probability curTopicSum = Probability.ZERO;
				for(int w = 0; w < WordToIndex.keySet().size(); w++) {
					Probability curProb = getProbability(phi_kw,k,w);
					curTopicSum = curTopicSum.add(curProb);
				}
				if(Math.abs( 1.0 - Math.exp(curTopicSum.getLogProb()) ) > epsilon){
					System.out.println("Error: Phi_kw check (should be near one):\t" + curTopicSum);
				}
			}
			
			//Phi_ckw should sum to 1 over words
			for(int c = 0; c < numCollections; c++) {
				for(int k = 0; k < numTopics; k++) {
					Probability curTopicSum = Probability.ZERO;
					for(int w = 0; w < WordToIndex.keySet().size(); w++) {
						Probability curProb = getProbability(phi_ckw,c,k,w);
						curTopicSum = curTopicSum.add(curProb);
					}
					if(Math.abs( 1.0 - Math.exp(curTopicSum.getLogProb()) ) > epsilon){
						System.out.println("Error: Phi_ckw check (should be near one):\t" + curTopicSum);
					}
				}
			}
			
			// Counts check;
			// Does ndk over k = ndstar?
			for(int d = 0; d < collections_d.size(); d++) {
				int sum = 0; 
				for(int k = 0; k < numTopics; k++) {
					sum += getValue(ndk,d,k); 
				}
				if(sum !=  getValue(ndStar,d)){
					System.err.printf("Error (ndk over k != ndstar): %d != %d\n",
							sum,getValue(ndStar,d));
				}
			}
			// Does nkw over w = nkstar?
			for(int k = 0; k < numTopics; k++) {
				int sum = 0; 
				for(int w = 0; w < WordToIndex.size(); w++) {
					sum += getValue(nkw,k,w); 
				}
				if(sum !=  getValue(nkStar,k)){
					System.err.printf("Error (nkw over w != nkstar): %d != %d\n",
							sum,getValue(nkStar,k,k));
				}
			}
			// Does nckw over w = nckstar?
			for(int c = 0; c< numCollections; c++){
				for(int k = 0; k < numTopics; k++) {
					int sum = 0; 
					for(int w = 0; w < WordToIndex.size(); w++) {
						sum += getValue(nckw,c,k,w); 
					}
					if(sum !=  getValue(nckStar,c,k)){
						System.err.printf("Error (nckw over w != nckstar): %d != %d\n",
								sum,getValue(nckStar,c,k));
					}
				}
			}
			
			if (t >= totalBurnin) {
				// save sample, add estimate to our expected value
//				if(!meansInitialized) {
//					// init mean values
//					for (int d = 0; d < collections_d.size(); d++) {
//						for (int k = 0; k < numTopics; k++) {
//							Probability thetadk = new Probability(0);
//							setProbability(theta_dkMean, thetadk, d, k);
//						}
//					}
//					// estimate map phi_dk
//					for (int k = 0; k < numTopics; k++) {
//						for (int w = 0; w < WordToIndex.size(); w++) {
//							Probability phikw = new Probability(0);
//							setProbability(phi_kwMean, phikw, k, w);
//							for (int c = 0; c < numCollections; c++) {
//								Probability phickw = new Probability(0);
//								setProbability(phi_ckwMean, phickw, c, k, w);
//							}
//						}
//					}
//					meansInitialized = true;
//				}
				// add to sums
				for (int d = 0; d < collections_d.size(); d++) {
					for (int k = 0; k < numTopics; k++) {
						Probability old = getProbability(theta_dkMean, d,k);
						setProbability(theta_dkMean, 
								old.add(getProbability(theta_dk, d,k)), d, k);
					}
				}
				for (int k = 0; k < numTopics; k++) {
					for (int w = 0; w < WordToIndex.size(); w++) {
						Probability old = getProbability(phi_kwMean, k,w);
						setProbability(phi_kwMean,
								old.add(getProbability(phi_kw, k,w)), k, w);
						for (int c = 0; c < numCollections; c++) {
							old = getProbability(phi_ckwMean, c, k,w);
							setProbability(phi_ckwMean,
									old.add(getProbability(phi_ckw, c,k,w)), c,k, w);
						}
					}
				}
			}
			// sample z of the test set, directly use the current iteration's
			// estimates of phis
			sampleTestIter();
			// estimate map theta_dk test
			for (int d = 0; d < collections_dTest.size(); d++) {
				for (int k = 0; k < numTopics; k++) {
					Probability thetadkTest = getThetadkTest(d, k);
					setProbability(theta_dkTest, thetadkTest, d, k);
				}
			}
			
			// compute log likelihood of train
//			Probability logLike_train = new Probability(0.0);
			double logLike_train = 0;
			for (int d = 0; d < collections_d.size(); d++) {
				for (int i = 0; i < ndStar.get(d); i++) {
					double topicTotal = 0;
					for (int k = 0; k < numTopics; k++) {
						Probability oneMinusLambda_times_PhiKW = (new Probability(1-lambda)).product(
								getProbability(phi_kw,k,getValue(wdi,d,i)));

						Probability lambda_times_PhiCKW = (new Probability(lambda)).product(
								getProbability(
										phi_ckw,getValue(collections_d,d),k,getValue(wdi,d,i)));
						
//						System.out.printf("oneMinusTheta_times_PhiKW = %s  \n",oneMinusTheta_times_PhiKW);
//						System.out.printf("lambda_times_PhiCKW = %s  \n",lambda_times_PhiCKW);

						Probability sum = oneMinusLambda_times_PhiKW.add(lambda_times_PhiCKW);
						Probability thetadk = getProbability(theta_dk,d,k);
						topicTotal = topicTotal + Math.exp(thetadk.product(sum).getLogProb());
//						double thetadk = Math.exp(getProbability(theta_dk,d,k).getLogProb());
//						double one = Math.exp(oneMinusLambda_times_PhiKW.getLogProb()),
//							two = Math.exp(lambda_times_PhiCKW.getLogProb());
//						logLike_train = logLike_train + Math.log(thetadk * (one + two));
					}
					logLike_train += Math.log(topicTotal);
				}
			}
//			System.out.printf("ll(train) = %s\n", logLike_train.getLogProb());
			System.out.printf("t:%d ll(train) = %s\n",t, logLike_train);
			printDoubleToFile(logLike_train, pwTrainLL, true);
			
			// compute log likelihood of test
//			Probability logLike_test = new Probability(0.0);
			double logLike_test = 0;
			for (int d = 0; d < collections_dTest.size(); d++) {
				for (int i = 0; i < ndStarTest.get(d); i++) {
					double topicTotal = 0;
					for (int k = 0; k < numTopics; k++) {
						Probability oneMinusLambda_times_PhiKW = (new Probability(1-lambda)).product(
								getProbability(phi_kw,k,getValue(wdiTest,d,i).intValue()));
						
//						System.out.printf("oneMinusTheta_times_PhiKW = %s  \n",oneMinusTheta_times_PhiKW);
						//System.out.printf("ll(test1, %d) = %s  \n",d,k,term1);
						
						Probability lambda_times_PhiCKW = (new Probability(lambda)).product(
								getProbability(phi_ckw,getValue(collections_dTest,d),k,getValue(wdiTest,d,i).intValue()));

//						System.out.printf("lambda_times_PhiCKW = %s  \n",lambda_times_PhiCKW);
				
						Probability sum = oneMinusLambda_times_PhiKW.add(lambda_times_PhiCKW);
						Probability thetadk = getProbability(theta_dk,d,k);
						topicTotal = topicTotal + Math.exp(thetadk.product(sum).getLogProb());
						
						//System.out.printf("ll(test, %d) = %s  \n",d, logLike_test);
					}
					logLike_test += Math.log(topicTotal);
				}
			}
			System.out.printf("T:%d ll(test) = %s\n",t, logLike_test);
			printDoubleToFile(logLike_test, pwTestLL, true);
		}
		int numSamples = totalIters - totalBurnin;
		// divide means by N to actaully get means
		for (int d = 0; d < collections_d.size(); d++) {
			for (int k = 0; k < numTopics; k++) {
				Probability old = getProbability(theta_dkMean, d,k);
				setProbability(theta_dkMean, 
						old.divide(new Probability(numSamples)), d, k);
			}
		}
		for (int k = 0; k < numTopics; k++) {
			for (int w = 0; w < WordToIndex.size(); w++) {
				Probability old = getProbability(phi_kwMean, k,w);
				setProbability(phi_kwMean,
						old.divide(new Probability(numSamples)), k, w);
				for (int c = 0; c < numCollections; c++) {
					old = getProbability(phi_ckwMean, c, k,w);
					setProbability(phi_ckwMean,
						old.divide(new Probability(numSamples)), c,k, w);
				}
			}
		}
		pwTrainLL.close();
		pwTestLL.close();
	}

	private void printDebug(){
		System.err.println("\n\n===[ DEBUG ]===");
		System.out.println("\n=== ndStar ===");
		for(int d = 0; d<ndStar.size(); d++){
			System.out.printf("d %d:  %d\n", d,ndStar.get(d));
		}
		
		System.out.println("\n=== collections_d ===");
		for(int d = 0; d<collections_d.size(); d++){
			System.out.printf("d %d:  %d\n", d,collections_d.get(d));
		}
		
		System.out.println("\n=== ndk ===");
		for(int d = 0; d<ndk.size(); d++){
			System.out.printf("\nd %d:\t", d);
			for(int k = 0; k<numTopics; k++){
				System.out.printf("%d\t", getValue(ndk,d,k));
			}
		}
		
		
		System.out.println("\n\n=== w/x/z_di ===");
		for(int d = 0; d<ndk.size(); d++){
			System.out.printf("\nd %d:", d);
			System.out.printf("\tw\t");
			for(int i = 0; i<xdi.get(d).size(); i++){
				System.out.printf("%d\t", getValue(wdi,d,i));
			}
			System.out.printf("\n\tx\t");
			for(int i = 0; i<xdi.get(d).size(); i++){
				System.out.printf("%d\t", getValue(xdi,d,i));
			}
			System.out.printf("\n\tz\t");
			for(int i = 0; i<xdi.get(d).size(); i++){
				System.out.printf("%d\t", getValue(zdi,d,i));
			}
			
			System.out.printf("\n");
		}
		
		System.out.println("\n\n=== nkStar ===");
		for(int k = 0; k <numTopics; k++){
			System.out.printf("k %d:  %d\n", k,nkStar.get(k));
		}
		
		System.out.println("\n\n=== nkw ===");
		for(int k = 0; k <numTopics; k++){
			System.out.printf("\nk %d:\t", k);
			for(int w = 0; w < nkw.get(k).size(); w++)
				System.out.printf("%d\t", getValue(nkw,k,w));
		}
		
		System.out.println("\n\n=== nckw ===");
		for(int c = 0; c <numCollections; c++){
			System.out.printf("\nc %d:", c);
			for(int k = 0; k <numTopics; k++){
				System.out.printf("\n\tk %d:\t", k);
				for(int w = 0; w < nckw.get(c).get(k).size(); w++)
					System.out.printf("%d\t\t", getValue(nckw,c,k,w));
			}
		}
		
		System.out.println("\n\n=== nckStar ===");
		for(int c = 0; c <numCollections; c++){
			System.out.printf("\nc %d:", c);
			for(int k = 0; k <numTopics; k++){
				System.out.printf("\n\tk %d: %d", k,getValue(nckStar,c,k));
			}
		}
		
		//
		
		System.out.println();
		
	}
	
	
	
	
	/**
	 * @param args
	 */
 	public static void main(String[] args) {
 		//GibbsSampler g = new GibbsSampler(null, 2, 1, 1.0, 1.0, 1.0);
// 		ArrayList<ArrayList<Integer>> a = new ArrayList<ArrayList<Integer>>();
// 		a.add(new ArrayList<Integer>());
// 		a.get(0).add(7);
// 		System.out.printf("a (0,0) = %d ",g.getValue(a,0,0));
 		
		SamplerType type = SamplerType.COLLAPSED;
		if(args.length < 9 || args.length > 10) {
			System.err.printf("Error: You passed %d arguments.\n",args.length);
			usage();
			return;
		}
		else if(args.length == 10) {
			if(args[9].equals("-b")) {
				type = SamplerType.BLOCKED;
			}
			else {
				usage();
				return;
			}
		}
		// read arguments
		String trainingFile = args[0], testFile = args[1], outFile = args[2];
		int numTopics = Integer.parseInt(args[3]), totalIters = Integer.parseInt(args[7]), 
				totalBurnin = Integer.parseInt(args[8]), numCollections = 2;
		double lambda = Double.parseDouble(args[4]),
				alpha = Double.parseDouble(args[5]), 
				beta = Double.parseDouble(args[6]);
		System.out.printf("trainfile:%s testfile:%s output:%s \nk:%d lambda:%f alpha:%f beta:%f \ntotaliters:%d totalburnin:%d\n",
				trainingFile, testFile, outFile, numTopics, lambda, alpha, beta, totalIters, totalBurnin);
		GibbsSampler g = new GibbsSampler(type, numCollections, numTopics, lambda, alpha, beta);
		try {
			g.readTrainingFile(trainingFile);
		} catch (IOException e) {
			System.err.println("error getting training data!");
			e.printStackTrace();
			return;
		}
		
		
//		//START DEBUG
//		g.printDebug();
//		System.err.println("\n\nEXCLUDING d= 0 w = 0");
//		g.updateCountsExcludeCurrentAssignment(0, 0);
//		System.err.println();
//		g.printDebug();
//		
//		System.err.println("\n\nINCLUDING d= 0 w = 0");
//		g.updateCountsNewlySampledAssignment(0, 0);
//		System.err.println();
//		g.printDebug();
//		
//		
//		System.err.println("\n\nEXCLUDING d= 0 w = 1");
//		g.updateCountsExcludeCurrentAssignment(0, 1);
//		System.err.println();
//		g.printDebug();
//		
//		System.err.println("\n\nINCLUDING d= 0 w = 1");
//		g.updateCountsNewlySampledAssignment(0, 1);
//		System.err.println();
//		g.printDebug();
//		
//		
//		System.err.println("\n\nEXCLUDING d= 0 w = 2");
//		g.updateCountsExcludeCurrentAssignment(0, 2);
//		System.err.println();
//		g.printDebug();
//		
//		System.err.println("\n\nINCLUDING d= 0 w = 2");
//		g.updateCountsNewlySampledAssignment(0, 2);
//		System.err.println();
//		g.printDebug();
//		//END DEBUG
		
		try {
			g.readTestFile(testFile);
		} catch (IOException e) {
			System.err.println("error getting test data!");
			e.printStackTrace();
			return;
		}
		try {
			g.runSampling(totalIters, totalBurnin, outFile);
		} catch (IOException e) {
			System.err.println("error writing likelood files while sampling");
			e.printStackTrace();
			return;
		}
		// after this we should have iters - burnin # of data points
		// compute the sample means? - computed
		// print out data::
		// print out theta dk
		try {
			File f = new File(outFile+"theta");
			if(f.exists()) f.delete();
			PrintWriter pw = new PrintWriter(f);
			for(int d = 0; d < g.collections_d.size(); d++) {
				for(int k = 0; k < numTopics; k++) {
					//TODO verify that we print out means
					printFloatToFile(g.getProbability(g.theta_dkMean, d, k), pw, false);
				}
				pw.println();
			}
		}
		catch(IOException ex) {
			System.err.println("error writing to theta file");
			ex.printStackTrace();
		}
		// print out phi kw
		try {
			File f = new File(outFile+"phi");
			if(f.exists()) f.delete();
			PrintWriter pw = new PrintWriter(f);
			for(String word : g.WordToIndex.keySet()) {
				pw.printf("%s", word);
				for(int k = 0; k < numTopics; k++) {
					//TODO verify that we print out means
					pw.printf(" ");
					printFloatToFile(g.getProbability(g.phi_kwMean, k, g.WordToIndex.get(word)), pw, false);
				}
				pw.println();
			}
		}
		catch(IOException ex) {
			System.err.println("error writing to phikw file");
			ex.printStackTrace();
		}
		// for each collection, print out phi ckw
		for(int c = 0; c < numCollections; c++) {
			try {
				File f = new File(outFile+"phi"+c);
				if(f.exists()) f.delete();
				PrintWriter pw = new PrintWriter(f);
				for(String word : g.WordToIndex.keySet()) {
					pw.printf("%s", word);
					for(int k = 0; k < numTopics; k++) {
						//TODO verify that we print out means
						pw.printf(" ");
						printFloatToFile(g.getProbability(g.phi_ckwMean, c, k, g.WordToIndex.get(word)), pw, false);
					}
					pw.println();
				}
			}
			catch(IOException ex) {
				System.err.println("error writing to phikw file");
				ex.printStackTrace();
			}
		}

	}
	private static void usage() {
		System.out.println("Usage:./collapsed-sampler trainFile testFile outputFile K lambda alpha beta totalNumSamples totalBurnIn\n" +
				"Example usage:\n./collapsed-sampler input-train.txt " +
				"input-test.txt output.txt 10 0.5 0.1 0.01 1100 1000\n" +
				"This runs the sampler with K = 10 topics and would " +
				"collect 100 samples after a 1000-iteration burn-in.\n" +
				"An optional 10th argument may be passed '-b' to run blocked collapsed gibbs sampling" +
				" instead of the default collapsed gibbs");
	}
	private static void printFloatToFile(Probability p, PrintWriter pw, boolean newline) {
		if(newline) pw.printf("%.13f\n", p.getLogProb());
		else pw.printf("%.13f", p.getLogProb());
	}
	private static void printDoubleToFile(double p, PrintWriter pw, boolean newline) {
		if(newline) pw.printf("%.13f\n", p);
		else pw.printf("%.13f", p);
	}
}

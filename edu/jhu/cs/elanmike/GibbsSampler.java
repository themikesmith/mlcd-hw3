package edu.jhu.cs.elanmike;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
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
	 * 2D array counting the number of word tokens labeled with each topic
	 * First index is topic, second is word index
	 */
	private ArrayList< ArrayList<Integer> > nkw;
	/**
	 * 3D array, number of collections, number of topics, number of word types of each topic in each collection
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
	 * 1D array counting the number of tokens that are assigned to each topic
	 * First index is topic
	 * for test data counts
	 */
	private ArrayList<Integer> nkStarTest;
	/**
	 * 2D array counting the number of word tokens labeled with each topic
	 * First index is topic, second is word index
	 * for test data counts
	 */
	private ArrayList< ArrayList<Integer> > nkwTest;
	/**
	 * 3D array, number of collections, number of topics, number of word types of each topic in each collection
	 * First index is collection, second index is topic, third is word type
	 * for test data counts
	 */
	private ArrayList< ArrayList< ArrayList<Integer> > > nckwTest;
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
	 * 2D array, number of times topic is mentioned in each collection
	 * First index is collection, second is topic
	 * for test data counts
	 */
	private ArrayList< ArrayList<Integer> > nckStarTest;

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
	
	/**
	 * Map from word string to index
	 */
	private HashMap<String,Integer> WordToIndex;
	
	private Random rand;
	
	GibbsSampler(SamplerType type, int numCollections, int numTopics, 
			double lambda, double alpha, double beta) {
		System.err.println("\n===[ New GibbsSampler ]===");
		System.out.printf("type = %d \nnumCollections = %d  \nnumTopics = %d \nlambda = %f \nalpha = %f \nbeta = %f",
				type.ordinal(),numCollections,numTopics,lambda,alpha,beta);
		
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
		nkStarTest = new ArrayList<Integer>();
		nkwTest = new ArrayList<ArrayList<Integer> >();
		nckwTest = new ArrayList< ArrayList< ArrayList<Integer> > >();
		nckStarTest = new ArrayList<ArrayList<Integer> >();
		
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
		
		// init nckstar, nckstartest, nckw, nckwtest
		for(int c = 0; c < numCollections; c++) {
			nckStar.add(new ArrayList<Integer>());
			nckStarTest.add(new ArrayList<Integer>());
			
			nckw.add(new ArrayList< ArrayList<Integer>>());
			nckwTest.add(new ArrayList< ArrayList<Integer>>());
			
			phi_ckw.add(new ArrayList<ArrayList<Probability>>());
			phi_ckwMean.add(new ArrayList<ArrayList<Probability>>());
			
			for(int k = 0; k < numTopics; k++) {
				nckStar.get(c).add(0);
				nckStarTest.get(c).add(0);
				
				nckw.get(c).add(new ArrayList<Integer>());
				nckwTest.get(c).add(new ArrayList<Integer>());
				
				phi_ckw.get(c).add(new ArrayList<Probability>());
				phi_ckwMean.get(c).add(new ArrayList<Probability>());
			}
		}
		
		// init count of topics
		for(int k = 0; k < numTopics; k++) {
			nkStar.add(0);
			nkStarTest.add(0);
			nkw.add(new ArrayList<Integer>());
			nkwTest.add(new ArrayList<Integer>());
			
			phi_kw.add(new ArrayList<Probability>());
			phi_kwMean.add(new ArrayList<Probability>());
		}
		
		
		
		
		rand = new Random();
	}
	
	
	private void processWord(String word, int collectionIdx, int docIdx, int wordIdx){
		//System.out.printf("Processing: %s, c=%d d=%d i=%d \n",word,collectionIdx,docIdx,wordIdx);
		//if new document
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
		}
		
		//if new word
		if(!WordToIndex.containsKey(word)){
			//System.out.printf("New word!\n");
			WordToIndex.put(word, WordToIndex.size());
			//add a new word row to n^{k}_{w} and n^{(c),k}_{w}
			for(int k = 0; k<numTopics; k++){
				nkw.get(k).add(0);
				nkwTest.get(k).add(0);
				
				phi_kw.get(k).add(new Probability(0));
				phi_kwMean.get(k).add(new Probability(0));
				for(int c = 0; c< numCollections; c++){
					nckw.get(c).get(k).add(0);
					nckwTest.get(c).get(k).add(0);
					
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
		
		if(x == 0){ // using collection-independent counts
			increment(nkw,z,wordIntValue);
		}else{ // using collection-dependent counts
			increment(nckw,collectionIdx,z,wordIntValue);	
		}
		increment(ndStar,docIdx);
		//System.out.printf("d = %d  z = %d\n",docIdx,z);
		increment(ndk,docIdx,z);
		increment(nkStar,z);
		increment(nckStar, collectionIdx, z);
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
		//if new document
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
			
			theta_dk.add(new ArrayList<Probability>());
			theta_dkTest.add(new ArrayList<Probability>());
			theta_dkMean.add(new ArrayList<Probability>());
			for(int k = 0; k< numTopics; k++) {
				theta_dk.get(docIdx).add(new Probability(0.0));
				theta_dkTest.get(docIdx).add(new Probability(0.0));
				theta_dkMean.get(docIdx).add(new Probability(0.0));
			}
		}
		
		//if new word
		if(!WordToIndex.containsKey(word)){
			//System.out.printf("New word!\n");
			WordToIndex.put(word, WordToIndex.size());
			//add a new word row to n^{k}_{w} and n^{(c),k}_{w}
			for(int k = 0; k<numTopics; k++){
				nkw.get(k).add(0);
				nkwTest.get(k).add(0);
				for(int c = 0; c< numCollections; c++){
					nckw.get(c).get(k).add(0);
					nckwTest.get(c).get(k).add(0);
				}
			}
		}
		
		
		int wordIntValue = WordToIndex.get(word);
		int x = rand.nextInt(2);
		int z = rand.nextInt(numTopics);
		
		xdiTest.get(docIdx).add(x);
		zdiTest.get(docIdx).add(z);
		wdiTest.get(docIdx).add(wordIntValue);
		
		if(x == 0){ // using collection-independent counts
			increment(nkwTest,z,wordIntValue);
		}else{ // using collection-dependent counts
			increment(nckwTest,collectionIdx,z,wordIntValue);	
		}
		increment(ndStarTest,docIdx);
		//System.out.printf("d = %d  z = %d\n",docIdx,z);
		increment(ndkTest,docIdx,z);
		increment(nkStarTest,z);
		increment(nckStarTest, collectionIdx, z);
	}
	
	private Integer getValue(ArrayList a, int... indicies){
		int depth = 0;
		ArrayList curArray = a;
		
		while(depth <indicies.length - 1){
			curArray = (ArrayList) curArray.get(indicies[depth]);
			depth++;
		}
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
		// and decrement topic count per word, nkstar
		decrement(nkStar, z);
		// and decrement nckstar
		decrement(nckStar, c, z);
		if(x == 0) { // decrement global
			decrement(nkw, z, w);
		}
		else { // collection-specific
			decrement(nckw, c, z, w);
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
		// and decrement topic count per word, nkstar
		increment(nkStar, z);
		// and decrement nckstar
		increment(nckStar, c, z);
		if (x == 0) { // decrement global
			increment(nkw, z, w);
		} else { // collection-specific
			increment(nckw, c, z, w);
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
		int z = getValue(zdiTest, docIdx, wordIdx), // topic index
			c = getValue(collections_dTest, docIdx); // collection id
		// decrement topic count per doc, ndk
		decrement(ndkTest, docIdx, z);
		// and decrement topic count per word, nkstar
		decrement(nkStarTest, z);
		// and decrement nckstar
		decrement(nckStarTest, c, z);
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
		int z = getValue(zdiTest, docIdx, wordIdx), // topic index
		c = getValue(collections_dTest, docIdx); // collection id
		// decrement topic count per doc, ndk
		increment(ndkTest, docIdx, z);
		// and decrement topic count per word, nkstar
		increment(nkStarTest, z);
		// and decrement nckstar
		increment(nckStarTest, c, z);
	}
	
	/**
	 * the vocab size is the size of the set of words we know, + 1 for OOV
	 * @return the vocab size.
	 */
	private int getVocabSize() {
		return WordToIndex.keySet().size() + 1;
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
//		System.out.println("(alpha + ndk) / (K * alpha + ndstar):"+a);
		if(xdi == 0) {
			return a.product(getProbability(phi_kw, k, w));
		}
		else {
			int c = getValue(collections_d, d);
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
			int c = getValue(collections_d, d);
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
//			System.out.printf("d:%d ",d);
			int numWordsInD = ndStarTest.get(d);
			for(int i = 0; i < numWordsInD; i++) {
				int w = getValue(wdiTest, d, i),
					v = getValue(xdiTest, d, i);
//				System.out.printf("i:%d w:%d\n",i,w);
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
		for(int d = 0; d < ndk.size(); d++) {
//			System.out.printf("d:%d ",d);
			int numWordsInD = ndStar.get(d);
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
	 * @param totalIters
	 * @param totalBurnin
	 */
	private void runSampling(int totalIters, int totalBurnin) {
//		System.out.printf("run sampling! totaliters:%d burn in:%d\n", totalIters, totalBurnin);
		boolean meansInitialized = false;
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
			Probability logLike_train = new Probability(0.0);
			for (int d = 0; d < collections_dTest.size(); d++) {
				for (int i = 0; i < ndStar.get(d); i++) {
					for (int k = 0; k < numTopics; k++) {
						Probability term1 = new Probability(1-lambda);
						term1 = term1.product(
								getProbability(phi_kw,k,getValue(wdi,d,i).intValue()));
						Probability term2 = new Probability(lambda);
						term2 = term2.product(
								getProbability(phi_ckw,getValue(collections_d,d),k,getValue(wdi,d,i).intValue()));
						
						logLike_train.add(getProbability(theta_dk,d,k).product(term1.add(term2)));
					}
				}
			}
			System.out.printf("ll(train) = %s\n", logLike_train);
			// compute log likelihood of test
			Probability logLike_test = new Probability(0.0);
			for (int d = 0; d < collections_dTest.size(); d++) {
				for (int i = 0; i < ndStar.get(d); i++) {
					for (int k = 0; k < numTopics; k++) {
						Probability term1 = new Probability(1-lambda);
						term1 = term1.product(
								getProbability(phi_kw,k,getValue(wdi,d,i).intValue()));
						Probability term2 = new Probability(lambda);
						term2 = term2.product(
								getProbability(phi_ckw,getValue(collections_d,d),k,getValue(wdi,d,i).intValue()));
						
						logLike_test.add(getProbability(theta_dkTest,d,k).product(term1.add(term2)));
					}
				}
			}
			System.out.printf("ll(test) = %s\n", logLike_test);
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
			System.out.printf("\nd: %d\t", d);
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
		}
		
		System.out.println("\n\n=== nkStar ===");
		for(int k = 0; k <numTopics; k++){
			System.out.printf("k %d:  %d\n", k,nkStar.get(k));
		}
		
		System.out.println("\n\n=== nkw ===");
		for(int k = 0; k <numTopics; k++){
			System.out.printf("\nk %d:", k);
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
		
		
		
	}
	
	/**
	 * @param args
	 */
 	public static void main(String[] args) {
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
				totalBurnin = Integer.parseInt(args[8]);
		double lambda = Double.parseDouble(args[4]),
				alpha = Double.parseDouble(args[5]), 
				beta = Double.parseDouble(args[6]);
		GibbsSampler g = new GibbsSampler(type, 2, numTopics, lambda, alpha, beta);
		try {
			g.readTrainingFile(trainingFile);
		} catch (IOException e) {
			System.err.println("error getting training data!");
			e.printStackTrace();
			return;
		}
		g.printDebug();
		try {
			g.readTestFile(testFile);
		} catch (IOException e) {
			System.err.println("error getting test data!");
			e.printStackTrace();
			return;
		}
		g.runSampling(totalIters, totalBurnin);
		// after this we should have iters - burnin # of data points
		// compute the sample means?
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
}

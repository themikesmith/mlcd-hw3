package edu.jhu.cs.elanmike;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
	 * first index is topic, second index is document
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
	 * 1D array, indexed by document number, holding the number of words in each document
	 * for test data counts
	 */
	private ArrayList<Integer> ndStarTest;
	/**
	 * 2D array of counts of words of each topic in each document
	 * first index is topic, second index is document
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
		WordToIndex = new HashMap<String,Integer>();
		ndStar = new ArrayList<Integer>();
		collections_d = new ArrayList<Integer>();
		ndk = new ArrayList<ArrayList<Integer> >();
		nkStar = new ArrayList<Integer>();
		nkw = new ArrayList<ArrayList<Integer> >();
		nckw = new ArrayList< ArrayList< ArrayList<Integer> > >();
		nckStar = new ArrayList<ArrayList<Integer> >();
		
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
			
			for(int k = 0; k < numTopics; k++) {
				nckStar.get(c).add(0);
				nckStarTest.get(c).add(0);
				
				nckw.get(c).add(new ArrayList<Integer>());
				nckwTest.get(c).add(new ArrayList<Integer>());
			}
		}
		
		// init count of topics
		for(int k = 0; k < numTopics; k++) {
			nkStar.add(0);
			nkw.add(new ArrayList<Integer>());
			
		}
		
		rand = new Random();
	}
	
	
	private void processWord(String word, int collectionIdx, int docIdx, int wordIdx){
		System.out.printf("Processing: %s, c=%d d=%d i=%d \n",word,collectionIdx,docIdx,wordIdx);
		//if new document
		if(docIdx >= collections_d.size()){
			System.out.printf("New document!\n");

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
			System.out.printf("New word!\n");
			WordToIndex.put(word, WordToIndex.size());
			//add a new word row to n^{k}_{w} and n^{(c),k}_{w}
			for(int k = 0; k<numTopics; k++){
				nkw.get(k).add(0);
				for(int c = 0; c< numCollections; c++){
					nckw.get(c).get(k).add(0);
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
	 * Process a word taht we see when reading data.
	 * increment the appropriate counts.
	 * @param word
	 * @param collectionIdx
	 * @param docIdx
	 * @param wordIdx
	 */
	private void processWordTest(String word, int collectionIdx, int docIdx, int wordIdx){
		System.out.printf("Processing: %s, c=%d d=%d i=%d \n",word,collectionIdx,docIdx,wordIdx);
		//if new document
		if(docIdx >= collections_d.size()){
			System.out.printf("New document!\n");

			ndStarTest.add(0);
			collections_d.add(collectionIdx);
			
			ndkTest.add(new ArrayList<Integer>());
			for(int k = 0; k<numTopics; k++){
				ndkTest.get(docIdx).add(0);
			}
			
			zdiTest.add(new ArrayList<Integer>());
			xdiTest.add(new ArrayList<Integer>());
			wdiTest.add(new ArrayList<Integer>());
		}
		
		//if new word
		if(!WordToIndex.containsKey(word)){
			System.out.printf("New word!\n");
			WordToIndex.put(word, WordToIndex.size());
			//add a new word row to n^{k}_{w} and n^{(c),k}_{w}
			for(int k = 0; k<numTopics; k++){
				nkwTest.get(k).add(0);
				for(int c = 0; c< numCollections; c++){
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
		// query zdi and xdi, and get word value and doc collection
		int z = getValue(zdi, docIdx, wordIdx), // topic index
			x = getValue(xdi, docIdx, wordIdx), // global flag
			w = getValue(wdi, docIdx, wordIdx), // word value
			c = getValue(collections_d, docIdx); // collection id
		// decrement topic count per doc, ndk
		decrement(ndk, z, docIdx);
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
		// query zdi and xdi, and get word value and doc collection
		int z = getValue(zdi, docIdx, wordIdx), // topic index
		x = getValue(xdi, docIdx, wordIdx), // global flag
		w = getValue(wdi, docIdx, wordIdx), // word value
		c = getValue(collections_d, docIdx); // collection id
		// decrement topic count per doc, ndk
		increment(ndk, z, docIdx);
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
		// ndk + alpha
		Probability a = new Probability(alpha);
		a = a.add(new Probability(getValue(ndk, k, d)));
		// ndstar + K * alpha
		Probability b = new Probability(numTopics);
		b = b.product(new Probability(alpha));
		b = b.add(new Probability(getValue(ndStar, d)));
		// a / b
		a = a.divide(b);
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
		int k = zdi;
		Probability multiplier;
		if(v == 0) {
			multiplier = new Probability(-1);
			multiplier = multiplier.product(new Probability(lambda));
			multiplier = multiplier.add(Probability.ONE);
			// nkw + beta
			Probability c = new Probability(beta);
			c = c.add(new Probability(getValue(nkw, k, w)));
			// nkstar + V * beta
			Probability f = new Probability(getVocabSize());
			f = f.product(new Probability(beta));
			f = f.add(new Probability(getValue(nkStar, k)));
			// a / b
			c = c.divide(f);
			return multiplier.product(c);
		}
		else if(v == 1) {
			multiplier = new Probability(lambda);
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
			return multiplier.product(c);
		}
		else {
			System.err.println("x can only be 0 or 1: specified:"+v);
			return null; // throw error, x can only be 0 or 1
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
		// and now run sampling
		for (int t = 0; t < totalIters; t++) {
			// sample
			for(int d = 0; d < ndk.size(); d++) {
				int numWordsInD = ndStar.get(d);
				for(int i = 0; i < numWordsInD; i++) {
					int w = getValue(wdi, d, i),
						v = getValue(xdi, d, i);
					// exclude current assignment
					updateCountsExcludeCurrentAssignment(d, i);
					// randomly sample a new value for zdi
					double p = rand.nextDouble();
					Probability marker = new Probability(p);
					Probability totalProb = new Probability(0);
					int sampledZdi = -1;
					for(int k = 0; k < numTopics; k++) {
						Probability curr = getPZdiEqualsK(d, totalBurnin, w, v, k);
						totalProb = totalProb.add(curr);
						if(totalProb.getLogProb() > marker.getLogProb()) {
							// stop. we have sampled this value of k
							sampledZdi = k;
							break;
						}
					}
					// randomly sample a new value for xdi, using newly sampled zdi
					p = rand.nextDouble();
					marker = new Probability(p);
					totalProb = getPXdiEqualsV(numWordsInD, i, w, sampledZdi, 0);
					int sampledXdi = -1;
					if(totalProb.getLogProb() > marker.getLogProb()) {
						// we have sampled xdi = 0
						sampledXdi = 0;
					}
					else {
						sampledXdi = 1;
					}
					// set zdi and xdi
					setValue(zdi, sampledZdi, d, i);
					setValue(xdi, sampledXdi, d, i);
					// and update counts
					updateCountsNewlySampledAssignment(d, i);
				}
			}
			// estimate map theta_dk
			// estimate map phi_dk
			// for each collection c
			// estimate map phi_cdk
			if (t > totalBurnin) {
				// save sample, add estimate to our expected value
			}
			// sample z of the test set, directly use the current iteration's
			// estimates of phis
			// for each token (d,i) in each document d in the test set:
			// 	update the counts to exclude the assignment of the current token
			// 	randomly sample a new value for zdi
			// 	randomly sample a new value for xdi, using newly sampled zdi
			// 	update the counts to include the newly sampled assignments of the
			// 	current token
			// compute log likelihood of train
			// compute log likelihood of test
		}
	}

	private void printDebug(){
		
		System.out.println("\n=== ndStar ===");
		for(int d = 0; d<ndStar.size(); d++){
			System.out.printf("d: %d - %d", d,ndStar.get(d));
		}
		
		System.out.println("\n=== collections_d ===");
		for(int d = 0; d<collections_d.size(); d++){
			System.out.printf("d: %d - %d", d,collections_d.get(d));
		}
		
		System.out.println("\n=== ndk ===");
		for(int d = 0; d<ndk.size(); d++){
			System.out.printf("\nd: %d\t", d);
			for(int k = 0; k<ndk.get(d).size(); k++){
				System.out.printf("%d\t", ndk.get(d).get(k));
			}
		}
		
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

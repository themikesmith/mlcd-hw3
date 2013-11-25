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
		
		this.type = type;
		this.numCollections = numCollections;
		this.numTopics = numTopics;
		this.lambda = lambda;
		this.alpha = alpha;
		this.beta = beta;
		
		// init num collections per topic
		for(int c = 0; c < numCollections; c++) {
			nckStar.add(new ArrayList<Integer>());
			nckStarTest.add(new ArrayList<Integer>());
			ArrayList<Integer> num = nckStar.get(c);
			ArrayList<Integer> numTest = nckStarTest.get(c);
			for(int k = 0; k < numTopics; k++) {
				num.add(0);
				numTest.add(0);
			}
		}
		// init count of topics
		for(int k = 0; k < numTopics; k++) {
			nkStar.add(0);
		}
		
		rand = new Random();
	}
	
	
	private void processWord(String word, int collectionIdx, int docIdx, int wordIdx){
		
		//if new document
		if(docIdx >= collections_d.size()){
			ndStar.add(0);
			collections_d.add(collectionIdx);
			Integer[] topicArray = new Integer[numTopics];
			ndk.add(new ArrayList<Integer>(Arrays.asList(topicArray)));
			zdi.add(new ArrayList<Integer>());
			xdi.add(new ArrayList<Integer>());
			wdi.add(new ArrayList<Integer>());
		}
		
		//if new word
		if(!WordToIndex.containsKey(word)){
			WordToIndex.put(word, WordToIndex.size());
			//add a new word row to n^{k}_{w}
			Integer[] topicArray = new Integer[numTopics];
			nkw.add(new ArrayList<Integer>(Arrays.asList(topicArray)));
			topicArray = new Integer[numTopics];
			nckw.get(collectionIdx).add(new ArrayList<Integer>(Arrays.asList(topicArray)));
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
		increment(ndk,z,docIdx);
		increment(nkStar,z);
		increment(nckStar, collectionIdx, z);
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
			curArray = (ArrayList) curArray.get(indicies[depth]);
			depth++;
		}
		curArray.set(indicies[depth],curArray.get(indicies[depth]+1));
	}
	
	private void decrement(ArrayList a, int... indicies){
		int depth = 0;
		ArrayList curArray = a;
		
		while(depth <indicies.length - 1){
			curArray = (ArrayList) curArray.get(indicies[depth]);
			depth++;
		}
		curArray.set(indicies[depth],curArray.get(indicies[depth]-1));
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
//				processWordTest(st.nextToken(), collectionIndex, d, i);
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
					updateCountsExcludeCurrentAssignment(d, i);
					// 	randomly sample a new value for zdi
					// 	randomly sample a new value for xdi, using newly sampled zdi
					updateCountsNewlySampledAssignment(d, i);
				}
			}
			// estimate params
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

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SamplerType type = SamplerType.COLLAPSED;
		if(args.length < 9 || args.length > 10) {
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

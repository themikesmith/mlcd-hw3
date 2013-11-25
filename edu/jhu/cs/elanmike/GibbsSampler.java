package edu.jhu.cs.elanmike;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class GibbsSampler {
	/**
	 * 1D array, indexed by document number, holding the number of words in each document
	 */
	private ArrayList<Integer> ndStar;
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
	 * Number of collections
	 */
	private int NUM_COLLECTIONS = 2;
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
	 * Map from word string to index
	 */
	private HashMap<String,Integer> WordToIndex;
	
	GibbsSampler(){
		WordToIndex = new HashMap<String,Integer>();
		ndStar = new ArrayList<Integer>();
		ndk = new ArrayList<ArrayList<Integer> >();
		nkStar = new ArrayList<Integer>();
		nkw = new ArrayList<ArrayList<Integer> >();
		nckw = new ArrayList< ArrayList< ArrayList<Integer> > >();
	}
	
	
	private void processWord(String word,int docIdx, int wordIdx){
		if(WordToIndex.containsKey(word)){
			
		}else{
			WordToIndex.put(word, WordToIndex.size());
			//add a new word row to n^{k}_{w}
			Integer[] topicArray = new Integer[numTopics];
			nkw.add(new ArrayList<Integer>(Arrays.asList(topicArray)));
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length != 9) {
			usage();
			return;
		}
		// read arguments
		String trainingFile = args[0], testFile = args[1], outFile = args[2];
		int k = Integer.parseInt(args[3]), totalIters = Integer.parseInt(args[7]), 
				totalBurnin = Integer.parseInt(args[8]);
		double lambda = Double.parseDouble(args[4]),
				alpha = Double.parseDouble(args[5]), 
				beta = Double.parseDouble(args[6]);
		
		
	}
	private static void usage() {
		System.out.println("Usage:./collapsed-sampler trainFile testFile outputFile K lambda alpha beta totalNumSamples totalBurnIn\n" +
				"Eample usage:\n./collapsed-sampler input-train.txt " +
				"input-test.txt output.txt 10 0.5 0.1 0.01 1100 1000\n" +
				"This runs the sampler with K = 10 topics and would" +
				"collect 100 samples after a 1000-iteration burn-in.");
	}
}

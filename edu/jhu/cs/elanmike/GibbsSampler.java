package edu.jhu.cs.elanmike;

import java.util.HashMap;

public class GibbsSampler {

	private HashMap<String,Integer> WordToIndex;
	
	GibbsSampler(){
		WordToIndex = new HashMap<String,Integer>();
	}
	
	
	private void processWord(String word){
		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// parse args
		// ./collapsed-sampler input-train.txt input-test.txt output.txt 10 0.5 0.1 0.01 1100 1000
		if(args.length != 9) {
			usage();
			return;
		}
		String trainingFile = args[0], testFile = args[1], outFile = args[2];
		int k = Integer.parseInt(args[3]), totalIters = Integer.parseInt(args[7]), 
				totalBurnin = Integer.parseInt(args[8]);
		double lambda = Double.parseDouble(args[4]), alpha = Double.parseDouble(args[5]), 
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

package minions.forceMult;

import java.util.*;

import models.encoder.CodeVector;
import models.math.LogRegression;

import org.ejml.simple.SimpleMatrix;

import util.PQueue;
import util.Warnings;

public class FMBatchFast {

	private static final double DELTA = 1e-8;
	private int batchSize = 50;

	private List<String> chosen = null;
	private LinkedList<String> gradeStack = null;

	private Set<String> notChosen = null;
	private Map<String, List<Boolean>> gradedMap = null;
	private CodeVectorLogistic minion = null;

	private Map<String, Double> piProductCache = null;
	private Map<String, Double> chosenScoreCache = null;
	//private Map<String, Double> fSCache = null;
	private Map<String, Double> fSumCache = null;
	private double fSumS = 0;

	private PQueue<String> contendersQueue = null;
	private Set<String> validSet = null;

	private Map<String, SimpleMatrix> encodingMap = null;

	public FMBatchFast(Map<String, CodeVector> codeVectorMap, int batchSize) {
		this.batchSize = batchSize;
		Warnings.msg("code vectors not normalized.");
		createNormalizedEncodingMap(codeVectorMap);
		this.notChosen = new HashSet<String>();
		this.gradedMap = new HashMap<String, List<Boolean>>();
		this.gradeStack = new LinkedList<String>();
		notChosen.addAll(encodingMap.keySet());

		fSumCache = new HashMap<String, Double>();
		Warnings.msg("not updating fSum");
		//throw new RuntimeException("This cache seems to have made things bad...");
	}
	
	private void createNormalizedEncodingMap(Map<String, CodeVector> codeVectorMap) {
		encodingMap = new HashMap<String, SimpleMatrix>();
		for(String key : codeVectorMap.keySet()) {
			SimpleMatrix v = codeVectorMap.get(key).getVector();
			double norm = v.normF();
			SimpleMatrix vNorm = v.scale(1.0 / norm);
			encodingMap.put(key, vNorm);
		}
	}

	public void update(String id, List<Boolean> feedback) {
		gradedMap.put(id, feedback);
	}

	public String choseNext(CodeVectorLogistic encoder) {
		this.minion = encoder;
		if(gradeStack.isEmpty()) {
			encoder.train(gradedMap);

			selectBatch(batchSize);
		} 
		return gradeStack.removeFirst();
	}

	private void selectBatch(int batchSize) {
		piProductCache = new HashMap<String, Double>();
		chosenScoreCache = new HashMap<String, Double>();

		this.chosen = new ArrayList<String>();
		this.contendersQueue = new PQueue<String>();
		for(int i = 0; i < batchSize; i++) {
			this.validSet = new HashSet<String>();
			System.out.println(i);
			String next = greedyNext();
			chosen.add(next);
			gradeStack.add(next);
			notChosen.remove(next);
		}
	}

	/**
	 * Method: Greedy Next
	 * -------------------
	 * Select the next program to add to the batch grading set.
	 */
	private String greedyNext() {

		if(contendersQueue.isEmpty()) {
			return baseCase();
		}

		int done = 0;
		while(true) {
			String next = contendersQueue.dequeue();
			if(validSet.contains(next)) {
				fSumS = fSumCache.get(next);
				return next; // doesn't get added back to the queue...
			}
			double score = multRegressionDelta(next);
			contendersQueue.add(next, score);
			validSet.add(next);
			done++;
		}
	}

	private String baseCase() {
		System.out.println("base-case");
		if(!chosen.isEmpty()) {
			throw new RuntimeException("mis-use");
		}

		// precompute f(S)
		System.out.println("bc: precompute f(S)");
		fSumS = 0;
		for(int i = 0; i < minion.getNumPredictions(); i++) {
			LogRegression classifier = minion.getClassifier(i);
			Set<String> S = new HashSet<String>(chosen);
			fSumS += f(classifier, S);
		}

		System.out.println("bc: calculate all...");
		for(String x : notChosen) {
			double score = multRegressionDelta(x);
			contendersQueue.add(x, score);
		}
		return contendersQueue.dequeue();
	}

	private double multRegressionDelta(String x) {
		Set<String> SUnionX = new HashSet<String>(chosen);
		SUnionX.add(x);

		double fSumSprime = 0;
		for(int i = 0; i < minion.getNumPredictions(); i++) {
			LogRegression classifier = minion.getClassifier(i);
			fSumSprime += f(classifier, SUnionX);
		}

		fSumCache.put(x, fSumSprime);
		//System.out.println(fSumSprime);
		return fSumSprime - fSumS;
	}

	private double f(LogRegression classifier, Set<String> S) {
		double secondTerm = 0;
		// that first term is a constant...
		for(String x : notChosen) {
			// We add things from notChosen into S
			if(S.contains(x)) continue; 
			double n = piProduct(classifier, x);
			double d = DELTA + chosenScore(classifier, S, x);
			secondTerm += n / d;
		}
		return -secondTerm;
	}

	private double chosenScore(LogRegression classifier, Set<String> S, String x) {
		double score = 0;
		for(String xPrime : S) {
			score += piProduct(classifier, xPrime) * dotSquared(x, xPrime);
		}
		return score;
	}

	/**
	 * Method: Pi Product
	 * -------
	 * pi(x)(1 - pi(x))
	 * @param classifier 
	 */
	private double piProduct(LogRegression classifier, String s) {
		String key = classifier.getName() + "_" + s;
		if(piProductCache.containsKey(key)) {
			return piProductCache.get(key);
		}
		double p = pi(classifier, s);
		double result = (p) * (1 - p);
		piProductCache.put(key, result);
		return result;
	}

	private double pi(LogRegression classifier, String s) {
		SimpleMatrix x = encodingMap.get(s);
		double dot = classifier.thetaDot(x);
		return 1.0 / (1.0 + Math.exp(dot));
	}


	/**
	 * Method:
	 * --------
	 * (xTx')^2
	 */
	private double dotSquared(String s, String x) {
		double dot = codeVector(s).dot(codeVector(x));
		return dot * dot;
	}

	private SimpleMatrix codeVector(String s) {
		return encodingMap.get(s);
	}

}

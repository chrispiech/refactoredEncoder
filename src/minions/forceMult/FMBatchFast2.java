package minions.forceMult;

import java.util.*;

import models.encoder.CodeVector;
import models.math.LogRegression;

import org.ejml.simple.SimpleMatrix;

import util.PQueue;
import edu.stanford.nlp.neural.NeuralUtils;

public class FMBatchFast2 {

	private static final double DELTA = 1e-8;
	private static final int DEFAULT_BATCH_SIZE = 50;
	
	private int batchSize;

	private List<String> chosen = null;
	private Map<String, SimpleMatrix> encodingMap = null;
	private LinkedList<String> gradeStack = null;

	private Set<String> notChosen = null;
	private Map<String, List<Boolean>> gradedMap = null;
	private CodeVectorLogistic minion = null;
	
	private Map<String, SimpleMatrix> notChosenCache = new HashMap<String, SimpleMatrix>();

	public FMBatchFast2(Map<String, CodeVector> codeVectorMap) {
		this(codeVectorMap, DEFAULT_BATCH_SIZE);
	}
	
	public FMBatchFast2(Map<String, CodeVector> codeVectorMap, int batchSize) {
		this.batchSize = batchSize;
		createNormalizedEncodingMap(codeVectorMap);
		this.notChosen = new HashSet<String>();
		this.gradedMap = new HashMap<String, List<Boolean>>();
		this.gradeStack = new LinkedList<String>();
		notChosen.addAll(encodingMap.keySet());
		this.chosen = new ArrayList<String>();
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
		
		//System.out.println("update: " + id);
		gradedMap.put(id, feedback);
	}

	public String choseNext(CodeVectorLogistic encoder) {
		this.minion = encoder;
		if(gradeStack.isEmpty()) {
			encoder.train(gradedMap);
			selectBatch(batchSize);
		} 
		String chosen = gradeStack.removeFirst();
		//System.out.println("chosen: " + chosen);
		return chosen;
	}

	private void selectBatch(int batchSize) {
		if(chosen.isEmpty()) {
			for(int i = 0; i < batchSize; i++) {
				System.out.println("selecting active: " + i);
				String next = choseRandom();
				chosen.add(next);
				gradeStack.add(next);
				notChosen.remove(next);
			}
		}
		
		
		for(int i = 0; i < batchSize; i++) {
			System.out.println("selecting active: " + i);
			String next = greedyNext();
			chosen.add(next);
			gradeStack.add(next);
			notChosen.remove(next);
		}
	}

	public void setBudget(int budget) {
		// do nothing..
	}
	
	private String choseRandom() {
		List<String> notChosenList = new ArrayList<String>(notChosen);
		Collections.shuffle(notChosenList);
		return notChosenList.get(0);
	}

	/**
	 * Method: Greedy Next
	 * -------------------
	 * Select the next program to add to the batch grading set.
	 */
	private String greedyNext() {

		// 1. compute the not chosen score...
		for(int i = 0; i < minion.getNumPredictions(); i++) {
			LogRegression classifier = minion.getClassifier(i);
			notChosenCache.put(classifier.getName(), notChosenVector(classifier));
		}

		// 2. chose the argmax
		String argMax = null;
		double max = 0;
		for(String id : notChosen){
			double f = multRegressionScore(id);
			if(argMax == null || f > max) {
				argMax = id;
				max = f;
			}
		}
		return argMax;
	}

	private SimpleMatrix notChosenVector(LogRegression classifier) {
		Set<String> S = new HashSet<String>(chosen);
		SimpleMatrix notChosenVector = null;
		for(String xPrime : notChosen) {
			double w = g(classifier, xPrime, S);
			SimpleMatrix v = codeVector(xPrime);
			SimpleMatrix xPrimeKron = v.kron(v);
			SimpleMatrix update = xPrimeKron.scale(w);
			if(notChosenVector == null) {
				notChosenVector = update;
			} else {
				notChosenVector = notChosenVector.plus(update);
			}
		}
		return notChosenVector;
	}

	private double multRegressionScore(String x) {
		double score = 0;
		for(int i = 0; i < minion.getNumPredictions(); i++) {
			LogRegression classifier = minion.getClassifier(i);
			score += delta(classifier, x);
		}
		return score;
	}

	private double delta(LogRegression classifier, String x) {
		Set<String> S = new HashSet<String>(chosen);
		Set<String> Sx = new HashSet<String>(chosen);
		Sx.add(x);
		
		double gBase = g(classifier, x, S);
		double gWithX = g(classifier, x, Sx);
		
		SimpleMatrix xTrans = codeVector(x).transpose();
		SimpleMatrix xTransKron = xTrans.kron(xTrans);
		
		SimpleMatrix notChosenVector = notChosenCache.get(classifier.getName());
		
		return gBase + gWithX * xTransKron.mult(notChosenVector).get(0);
	}

	private double g(LogRegression classifier, String x, Set<String> S) {
		double n = piProduct(classifier, x);
		double d = DELTA;
		for(String xPrime : S) {
			d += piProduct(classifier, xPrime) * dotSquared(x, xPrime);
		}
		return n / d;
	}
	

	/**
	 * Method: Pi Product
	 * -------
	 * pi(x)(1 - pi(x))
	 * @param classifier 
	 */
	private double piProduct(LogRegression classifier, String x) {
		double p = pi(classifier, x);
		double result = (p) * (1 - p);
		return result;
	}

	/**
	 * Method: Pi
	 * ----------
	 * 1 / (1 + exp(theta T x))
	 */
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

package minions.forceMult;

import java.util.*;

import models.encoder.ClusterableMatrix;
import models.math.LogRegression;

import org.ejml.simple.SimpleMatrix;
public class FMBatchActiveLearner {

	private static final double DELTA = 1e-8;
	private int batchSize = 50;

	private List<String> chosen = null;
	private LinkedList<String> gradeStack = null;

	private Set<String> notChosen = null;
	private Map<String, List<Boolean>> gradedMap = null;
	private CodeVectorLogistic minion = null;
	
	private Map<String, Double> piProductCache = null;
	private Map<String, Double> chosenScoreCache = null;
	
	private Map<String, SimpleMatrix> encodingMap = null;

	public FMBatchActiveLearner(Map<String, ClusterableMatrix> codeVectorMap, int batchSize) {
		this.batchSize = batchSize;
		createNormalizedEncodingMap(codeVectorMap);
		this.notChosen = new HashSet<String>();
		this.gradedMap = new HashMap<String, List<Boolean>>();
		this.gradeStack = new LinkedList<String>();
		notChosen.addAll(encodingMap.keySet());

	}
	
	private void createNormalizedEncodingMap(Map<String, ClusterableMatrix> codeVectorMap) {
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
		minion.train(gradedMap);
		minion.test();
	}

	public String choseNext(CodeVectorLogistic encoder) {
		this.minion = encoder;
		/*if(gradeStack.isEmpty()) {
			encoder.train(gradedMap);
			encoder.test();
			piProductCache = new HashMap<String, Double>();
			chosenScoreCache = new HashMap<String, Double>();
			selectBatch(batchSize);
		} 
		return gradeStack.removeFirst();*/
		return choseRandom();
	}
	
	private String choseRandom() {
		List<String> notChosenList = new ArrayList<String>(notChosen);
		Collections.shuffle(notChosenList);
		return notChosenList.get(0);
	}

	private void selectBatch(int batchSize) {
		this.chosen = new ArrayList<String>();
		for(int i = 0; i < batchSize; i++) {
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
		String argMax = null;
		double max = 0;
		int done = 0;
		for(String id : notChosen){
			double f = multRegressionScore(id);
			if(argMax == null || f > max) {
				argMax = id;
				max = f;
			}
			if(++done % 100 == 0) System.out.println(done);
		}
		return argMax;
	}

	/**
	 * Method: Mult Regression Score
	 * -----------------------------
	 * Computes the f function, which is equation (6) in the Hoi et all
	 * paper: Batch Mode Active Learning and Its Application...
	 * Note that the set S = "chosen" union with the "id" parameter.
	 */
	private double multRegressionScore(String id) {
		if(minion.getNumPredictions() != 1) throw new RuntimeException("not ready");
		double score = 0;
		for(int i = 0; i < minion.getNumPredictions(); i++) {
			LogRegression classifier = minion.getClassifier(i);
			score += getClassifierScore(classifier, id);
		}
		return score;
	}

	/**
	 * Method: Get Classifier Score
	 * ----------------------------
	 * Computes the f function, which is equation (6) in the Hoi et all
	 * paper: Batch Mode Active Learning and Its Application...
	 * Note that the set S = "chosen" union with the "id" parameter.
	 */
	private double getClassifierScore(LogRegression classifier, String id) {
		double sum = 0;
		for(String s : notChosen) {
			// we pretend id is in chosen
			if(s.equals(id)) continue;
			double n = piProduct(classifier, s);
			double d = DELTA + chosenScore(classifier, id, s);
			sum += n / d;
		}
		return -sum;
	}

	/**
	 * Method: Chosen Score
	 * --------------------
	 * The inner sum in equation (6).
	 * @param classifier 
	 */
	private double chosenScore(LogRegression classifier, String id, String x) {
		double score = 0;
		for(String xPrime : chosen) {
			score += piProduct(classifier, xPrime) * dotSquared(x, xPrime);
		}
		// we pretend that id is in chosen
		score += piProduct(classifier, id) * dotSquared(id, x);
		return score;
	}

	/**
	 * Method: Pi Product
	 * -------
	 * pi(x)(1 - pi(x))
	 * @param classifier 
	 */
	private double piProduct(LogRegression classifier, String s) {
		/*String key = classifier.getName() + "_" + s;
		if(piProductCache.containsKey(key)) {
			return piProductCache.get(key);
		}*/
		double p = pi(classifier, s);
		double result = (p) * (1 - p);
		//piProductCache.put(key, result);
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
	 * (aTb)^2
	 */
	private double dotSquared(String a, String b) {
		double dot = codeVector(a).transpose().mult(codeVector(b)).get(0);
		//double dot = codeVector(a).dot(codeVector(b));
		return Math.pow(dot, 2);
	}

	private SimpleMatrix codeVector(String s) {
		return encodingMap.get(s);
	}

}

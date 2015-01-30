package models.math;

import org.ejml.simple.SimpleMatrix;

import util.MatrixUtil;
import util.RandomUtil;
import util.Warnings;
import cc.mallet.classify.Classification;
import cc.mallet.classify.ClassifierTrainer;
import cc.mallet.classify.MaxEnt;
import cc.mallet.classify.MaxEntGETrainer;
import cc.mallet.pipe.Array2FeatureVector;
import cc.mallet.pipe.Pipe;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.Label;

/**
 * Class: Vanilla Logisitc Regression
 * ----------------------------------
 * 1 is true
 * 0 is false
 * Adds an intercept term.
 * @author mike + chris
 */
public class LogRegression2 {

	private static final boolean ADD_INTERCEPT = true;
	private SimpleMatrix theta;
	private String name = "";

	public LogRegression2() {}

	public LogRegression2(String name) {
		this.name = name;
	}

	public void train(SimpleMatrix f, SimpleMatrix labels) {
		Pipe pipe = new Array2FeatureVector();
		InstanceList iList = new InstanceList(pipe);
		double[] data = new double[4];
		Instance sampleInstance = new Instance(data, null, null, null);
		iList.add(pipe.instanceFrom(sampleInstance));
		ClassifierTrainer trainer = new MaxEntGETrainer();
		MaxEnt classifier = (MaxEnt) trainer.train(iList);
		Classification c = classifier.classify(pipe.instanceFrom(sampleInstance));
		System.out.println(c);
		Warnings.error("wtf");
	}

	/**
	 * Assumes the intercept has not been added.
	 * Takes in a horizontal vector
	 */
	public int predict(SimpleMatrix feature) {
		return predict(feature, 0.5);
	}

	/**
	 * Assumes the intercept has not been added.
	 * Takes in a horizontal vector
	 */
	public int predict(SimpleMatrix feature, double threshold) {
		if(feature.numRows() > 1) {
			throw new RuntimeException("feature input should be a horizontal vector");
		}
		SimpleMatrix fPrime = addInterceptFeature(feature);
		return (logistic(fPrime) >= threshold) ? 1 : 0; 
	}
	
	/**
	 * Assumes the intercept has not been added.
	 * Takes in a horizontal vector
	 */
	public double probability(SimpleMatrix x) {
		if(x.numRows() > 1) {
			throw new RuntimeException("feature input should be a horizontal vector");
		}
		SimpleMatrix xPrime = addInterceptFeature(x);
		return logistic(xPrime);
	}
	
	public String getName() {
		return name;
	}
	
	private SimpleMatrix addInterceptFeature(SimpleMatrix f) {
		if(ADD_INTERCEPT) {
			int numFeatures = f.numCols();
			int numSamples = f.numRows();
			SimpleMatrix fPrime = f.combine(0, numFeatures, MatrixUtil.ones(numSamples, 1));
			return fPrime;
		} else {
			return f;
		}
	}
	
	public double thetaDot(SimpleMatrix x) {
		SimpleMatrix fPrime = addInterceptFeature(x);
		return theta.transpose().mult(fPrime).get(0);
	}

	/**
	 * Assumes the intercept has already been added.
	 * Takes in a horizontal vector
	 */
	private double logistic(SimpleMatrix x) {
		double z = theta.mult(x.transpose()).get(0, 0);
		return 1.0 / (1.0 +Math.exp(-z));
	}
	
	public static void main(String[] args) {
		int n = 20;
		SimpleMatrix m = new SimpleMatrix(n, 2);
		SimpleMatrix l = new SimpleMatrix(n, 1);
		for(int i = 0; i < n; i++) {
			double v = Math.random() - 0.5;
			m.set(i, 0, v);
			m.set(i, 1, RandomUtil.gauss(0, 0.1));
			l.set(i, v > 0 ? 1 : 0);
		}
		LogRegression2 r = new LogRegression2();
		r.train(m, l);
		int correct = 0;
		for(int i = 0; i < n; i++) {
			SimpleMatrix f = m.extractVector(true, i);
			int prediction = r.predict(f);
			if(prediction != l.get(i)) {
				System.out.println("no");
				System.out.println(f);
			} else {
				correct++;
			}
		}
		System.out.println(100.0 * correct / n);
	}
}

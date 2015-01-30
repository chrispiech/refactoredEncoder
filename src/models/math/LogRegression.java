package models.math;

import org.ejml.simple.SimpleMatrix;

import util.MatrixUtil;
import util.RandomUtil;
import edu.stanford.nlp.classify.LogisticObjectiveFunction;
import edu.stanford.nlp.optimization.DiffFunction;
import edu.stanford.nlp.optimization.QNMinimizer;

/**
 * Class: Vanilla Logisitc Regression
 * ----------------------------------
 * 1 is true
 * 0 is false
 * Adds an intercept term.
 * @author mike + chris
 */
public class LogRegression {

	private static final boolean ADD_INTERCEPT = true;
	private static final double LEARNING_RATE = 0.1;
	private static final double THRESHOLD = 1E-20;
	private static final int MAX_ITERATIONS = 10000;
	private static final double LAMBDA = 0.0;
	private SimpleMatrix theta;
	private String name = "";

	public LogRegression() {}

	public LogRegression(String name) {
		this.name = name;
	}
	
	public void train(SimpleMatrix f, SimpleMatrix labels) {
		SimpleMatrix fPrime = addInterceptFeature(f);
		theta = MatrixUtil.randomVector(fPrime.numCols(), 1).transpose();

		for(int i = 0; i < MAX_ITERATIONS; i++) {
			int row = (i % fPrime.numRows());
			SimpleMatrix x = fPrime.extractMatrix(row, row+1, 0, fPrime.numCols());
			double label = labels.get(row); 
			SimpleMatrix thetaTemp = new SimpleMatrix(theta);
			double gradSize = 0;
			for(int col = 0; col < fPrime.numCols(); col++) {
				double activation = logistic(x);
				double loss = -(label - activation);
				double grad = loss * x.get(col);
				if(col != fPrime.numCols() - 1) {
					grad += LAMBDA * theta.get(col);
				}
				double update = theta.get(col) - LEARNING_RATE * grad;
				gradSize += Math.pow(grad, 2);
				thetaTemp.set(col, update);
			}
			theta = thetaTemp;
			gradSize = Math.sqrt(gradSize);
			if(gradSize < THRESHOLD) {
				System.out.println("finished in " + i + " iterataion");
				break;
			}
			if(i == MAX_ITERATIONS - 1) {
				System.out.println("finished in " + i + " iterataion");
			}
			//if(i % 1000 == 0) System.out.println(i + ": " + gradSize);
		}
	}

	public void oldTrain(SimpleMatrix f, SimpleMatrix labels) {
		
		SimpleMatrix fPrime = addInterceptFeature(f);
		theta = MatrixUtil.randomVector(fPrime.numCols(), 1).transpose();

		double inverseM = 1.0 / f.numRows();
		for(int i = 0; i < MAX_ITERATIONS; i++) {
			int row = (i % fPrime.numRows());
			SimpleMatrix x = fPrime.extractMatrix(row, row+1, 0, fPrime.numCols());
			double label = labels.get(row); 
			SimpleMatrix thetaTemp = new SimpleMatrix(theta);
			double gradSize = 0;
			for(int col = 0; col < fPrime.numCols(); col++) {
				double activation = logistic(x);
				double loss = label - activation;
				double grad = loss * x.get(col);
				if(col != fPrime.numCols() - 1) {
					grad += LAMBDA * theta.get(col);
				}
				double update = theta.get(col) + LEARNING_RATE * grad;
				gradSize += Math.pow(grad, 2);
				thetaTemp.set(col, update);
			}
			theta = thetaTemp;
			gradSize = Math.sqrt(gradSize);
			if(gradSize < THRESHOLD) {
				System.out.println("finished in " + i + " iterataion");
				break;
			}
			if(i == MAX_ITERATIONS - 1) {
				System.out.println("finished in " + i + " iterataion");
			}
			//if(i % 1000 == 0) System.out.println(i + ": " + gradSize);
		}
		
	}
	
	public void trainBFGS(SimpleMatrix f, SimpleMatrix labels) {
		
		SimpleMatrix fPrime = addInterceptFeature(f);
		int n = fPrime.numCols();
		
		int[][] features = new int[fPrime.numRows()][];
		for(int i = 0; i < fPrime.numRows(); i++) {
			features[i] = new int[fPrime.numCols()];
			for(int j = 0; j < fPrime.numCols(); j++) {
				features[i][j] = j;
			}
		}
		
		double[][] x = MatrixUtil.asMatrix(fPrime);
		
		int[] y = new int[labels.getNumElements()];
		for(int i = 0; i < y.length; i++) {
			y[i] = (int) labels.get(i);
		}
		
		double[] init = new double[n];
		for(int i = 0; i < init.length; i++) {
			init[i] = Math.random();
		}
		
		DiffFunction fn = new LogisticObjectiveFunction(n, features, x, y);
		QNMinimizer minimizer = new QNMinimizer();
		double[] min = minimizer.minimize(fn, 1E-50, init);
		
		theta = new SimpleMatrix(1, n);
		for(int i = 0; i < min.length; i++) {
			theta.set(i, min[i]);
		}
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

	private double getError(SimpleMatrix data, SimpleMatrix labels) {
		SimpleMatrix fPrime = addInterceptFeature(data);
		// implement this to be actual loss
		SimpleMatrix y = fPrime.mult(theta.transpose());
		SimpleMatrix predict = MatrixUtil.elementwiseApplyLogistic(y);
		int loss = 0;
		for(int i = 0; i < predict.getNumElements(); i++) {
			int label = predict.get(i) > 0 ? 1 : 0;
			if(labels.get(i) != label) loss++;
		}
		return loss;
	}
	
	public static void main(String[] args) {
		int n = 20;
		SimpleMatrix m = new SimpleMatrix(n, 2);
		SimpleMatrix l = new SimpleMatrix(n, 1);
		for(int i = 0; i < n; i++) {
			double v = Math.random() - 0.5;
			m.set(i, 0, v + RandomUtil.gauss(0, 0.00001));
			m.set(i, 1, RandomUtil.gauss(0, 1));
			l.set(i, v > 0.4 ? 1 : 0);
		}
		LogRegression r = new LogRegression();
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

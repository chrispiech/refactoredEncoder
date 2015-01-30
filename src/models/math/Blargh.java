package models.math;

import java.util.ArrayList;
import java.util.Collections;

import org.ejml.simple.SimpleMatrix;

public class Blargh {
	private double[] alpha;
	private double lambda;
	private double logLikelihood;
	private double prevLogLikelihood;
	private SimpleMatrix allData;
	private int epochs;
	private int maxDatasetIterations;
	private double tolerance;

	/**
	 *
	 * @param theta
	 *            : weights
	 * @param lambda
	 *            : learning rate
	 * @param data
	 *            : dataset (instances to vector of values)
	 * @param maxDatasetIterations
	 * @param tolerance
	 */
	public Blargh(double[] theta, double lambda, SimpleMatrix data,
			int maxDatasetIterations, double tolerance) {
		this.alpha = theta;
		this.lambda = lambda;
		prevLogLikelihood = Double.POSITIVE_INFINITY;
		logLikelihood = 0;
		allData = new ArrayList<Binaryinstance>();
		u = new Utils();
		for (String row : data) {
			BinaryInstance instance = u.rowDataToInstance(row);
			allData.add(instance);
		}
		epochs = 0;
		this.maxDatasetIterations = maxDatasetIterations;
		this.tolerance = tolerance;

	}

	public void classifyByInstance() {

		while (evaluateCondition()) {
			prevLogLikelihood = logLikelihood;
			epochs++;
			Collections.shuffle(allData);
			for (BinaryInstance instance : allData) {
				double probPositive = estimateProbs(instance.getX());
				double label = (instance.getLabel() == true) ? 1 : 0;
				adjustWeights(instance.getX(), probPositive, label);

			}
			logLikelihood = calculateLogL(allData);

		}

	}

	private boolean evaluateCondition() {
		return (Math.abs(logLikelihood - prevLogLikelihood) > tolerance && epochs < maxDatasetIterations) ? true     : false;  }    private double estimateProbs(double[] x) {    double sum = alpha[0];   for (int i = 1; i < this.alpha.length; i++)    sum += this.alpha[i] * x[i - 1];   double exponent = Math.exp(-sum);   double probPositive = 1 / (1 + exponent);   if (probPositive == 0)    probPositive = 0.00001;   else if (probPositive == 1)    probPositive = 0.9999;    return probPositive;  }   private void adjustWeights(double[] x, double probPositive, double label) {                 //for the intercept   this.alpha[0] += this.lambda * (label - probPositive);    for (int i = 1; i < this.alpha.length; i++) {    this.alpha[i] += this.lambda * x[i - 1] * (label - probPositive);   }  }    private double calculateLogL(ArrayList<Binaryinstance> allData) {

			double logl = 0;
			for (BinaryInstance instance : allData) {

				double probPositive = estimateProbs(instance.getX());
				double label = (instance.getLabel() == true) ? 1 : 0;
				double probNegative = 1 - probPositive;
				double tmp = label * Math.log(probPositive) + (1 - label)
						* Math.log(probNegative);
				logl += tmp;
			}
			return logl;

		}
}

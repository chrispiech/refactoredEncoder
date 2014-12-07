package models.encoder;

import org.apache.commons.math3.util.Pair;

import util.Warnings;

public class EncoderParams{

	private static double weightDecay = 0.0;
	private static double learningRate = 0.01;
	private static int codeVectorSize = 0;
	private static int miniBatchSize = 1000;
	private static double initStd = 0.01;
	private static int stateVectorSize = 0;
	
	public static int getMiniBatchSize() {
		return miniBatchSize;
	}
	public static void setMiniBatchSize(int miniBatchSize) {
		EncoderParams.miniBatchSize = miniBatchSize;
	}
	public static int getStateVectorSize() {
		Warnings.check(stateVectorSize != 0);
		return stateVectorSize;
	}
	public static void setStateVectorSize(int stateVectorSize) {
		EncoderParams.stateVectorSize = stateVectorSize;
	}
	public static double getWeightDecay() {
		return weightDecay;
	}
	public static void setWeightDecay(double weightDecay) {
		EncoderParams.weightDecay = weightDecay;
	}
	public static double getLearningRate() {
		return learningRate;
	}
	public static void setLearningRate(double learningRate) {
		EncoderParams.learningRate = learningRate;
	}
	public static int getCodeVectorSize() {
		Warnings.check(codeVectorSize != 0);
		return codeVectorSize;
	}
	public static void setCodeVectorSize(int codeVectorSize) {
		EncoderParams.codeVectorSize = codeVectorSize;
	}
	public static double getInitStd() {
		return initStd;
	}
	public static void setInitStd(double initStd) {
		EncoderParams.initStd = initStd;
	}
	public static int getN() {
		return getCodeVectorSize();
	}
	public static int getSqrtN() {
		int sqrt = (int)Math.sqrt(getN());
		Warnings.check(sqrt * sqrt == getN());
		return sqrt;
	}
	public static int getM() {
		return getStateVectorSize();
	}
	public static boolean hasStateVectorSize() {
		return stateVectorSize != 0;
	}
	
}
	

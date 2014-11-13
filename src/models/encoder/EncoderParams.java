package models.encoder;

import org.apache.commons.math3.util.Pair;

import util.Warnings;

public class EncoderParams{

	private static double weightDecay = 0.0;
	private static double stateWeightDecay = 0.0;
	private static double learningRate = 0.01;
	private static int codeVectorSize = 0;
	private static double initStd = 0.01;
	private static String language = null;
	private static int stateVectorSize = 0;
	private static int depthLimit = -1; // negative means no limit
	private static int worldRows = 0;
	private static int worldCols = 0;
	
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
	public static double getStateWeightDecay() {
		return stateWeightDecay;
	}
	public static void setStateWeightDecay(double stateWeightDecay) {
		EncoderParams.stateWeightDecay = stateWeightDecay;
	}
	public static int getDepthLimit() {
		return depthLimit;
	}
	public static void setDepthLimit(int depthLimit) {
		EncoderParams.depthLimit = depthLimit;
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
	public static void setLanguage(String string) {
		EncoderParams.language = string;
	}
	public static String getLanguage() {
		return EncoderParams.language;
	}
	public static void setWorldDim(int r, int c) {
		EncoderParams.worldRows = r;
		EncoderParams.worldCols = c;
	}
	public static Pair<Integer, Integer> getWorldDim() {
		int r = EncoderParams.worldRows;
		int c = EncoderParams.worldCols;
		return new Pair<Integer, Integer>(r, c);
	}
	public static int getM() {
		return getStateVectorSize();
	}
	
}
	

package run.clusterExp;

import java.util.List;

import minions.encoder.EncoderSaver;
import minions.minimizer.AdaGradCluster;
import minions.program.PostExperimentLoader;
import minions.program.PrePostExperimentLoader;
import models.code.TestTriplet;
import models.encoder.EncoderParams;
import models.encoder.ModelFormat;
import models.encoder.encoders.Encoder;
import util.FileSystem;
import util.RandomUtil;
import util.Warnings;

public class ClusterNewspaperMonkey {
	private static final String LANGUAGE = "karel";
	private static final String MODEL_TYPE = "monkey";
	private static final String NAME_BASE = "monkeypaper";
	
	List<TestTriplet> trainSet = null;
	List<TestTriplet> testSet = null;
	ModelFormat format = null;

	private void run(int n, String nameExt) {
		setParameters(n);
		
		format = new ModelFormat(LANGUAGE, MODEL_TYPE);
		FileSystem.setAssnId("Newspaper");
		
		FileSystem.setExpId("prePostExp");
		System.out.println("load test...");
		testSet = PrePostExperimentLoader.loadTriplets("test", -1,format.getLanguage());
		
		FileSystem.setExpId("prePostExp");
		System.out.println("load train...");
		trainSet = PrePostExperimentLoader.loadTriplets("train", -1, format.getLanguage());
		System.out.println("train set size: " + trainSet.size());
		
		train(n, nameExt);
	}
	
	private void setParameters(int n) {
		EncoderParams.setCodeVectorSize(n);
		EncoderParams.setStateVectorSize(EncoderParams.getSqrtN());
		EncoderParams.setWorldDim(5, 7);
		
		double weightDecay = getParamLogScale(0.00001, 1);
		double learningRate = getParamLogScale(0.001, 0.01);
		int miniBatchSize = (int) getParamLogScale(1000, 10000);
		
		EncoderParams.setWeightDecay(weightDecay);
		EncoderParams.setLearningRate(learningRate);
		EncoderParams.setMiniBatchSize(miniBatchSize);
	}
	
	private double getParamLogScale(double min, double max) {
		double x = Math.random();
		return min * Math.pow(Math.E, Math.log(max/min) * x);
	}

	private void train(int n, String nameExt) {
		int maxHours = 24 * 2;
		String name = NAME_BASE + "-" + nameExt;
		AdaGradCluster.train(format, trainSet, testSet, maxHours, name);
	}
	
	public static void main(String[] args) {
		Warnings.check(args.length == 3, "wrong num args");
		String argDataPath = args[0];
		String argN = args[1];
		String argName = args[2];
		
		FileSystem.setDataDir(argDataPath);
		int n = Integer.parseInt(argN);
		int sqrt = (int) Math.sqrt(n);
		Warnings.check(sqrt * sqrt == n, "must be square number");
		new ClusterNewspaperMonkey().run(n, argName);
	}
}

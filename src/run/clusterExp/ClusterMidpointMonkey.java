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

public class ClusterMidpointMonkey {
	private static final String LANGUAGE = "karel";
	private static final String MODEL_TYPE = "monkey";
	private static final String NAME_BASE = "middlemonkey";
	
	List<TestTriplet> testSet = null;
	ModelFormat format = null;

	private void run(int n, String nameExt) {
		setParameters(n);
		
		format = new ModelFormat(LANGUAGE, MODEL_TYPE);
		FileSystem.setAssnId("Midpoint");
		
		FileSystem.setExpId("prePostExp");
		System.out.println("load test...");
		testSet = PrePostExperimentLoader.loadTestSet(1000, format.getLanguage());
		
		FileSystem.setExpId("prePostExp");
		System.out.println("load train...");
		PrePostExperimentLoader.loadTrainPrograms(format.getLanguage());
		//trainSet = PrePostExperimentLoader.loadTriplets("train", -1, format.getLanguage());
		
		train(n, nameExt);
	}
	
	private void setParameters(int n) {
		EncoderParams.setCodeVectorSize(n);
		EncoderParams.setStateVectorSize(EncoderParams.getSqrtN());
		
		double weightDecay = getParamLogScale(0.00001, 0.01);
		double learningRate = getParamLogScale(0.001, 0.05);
		int miniBatchSize = (int) getParamLogScale(1000, 20000);
		
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
		AdaGradCluster.trainFromFile(format, testSet, maxHours, name);
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
		new ClusterMidpointMonkey().run(n, argName);
	}
}

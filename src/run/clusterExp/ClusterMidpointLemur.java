package run.clusterExp;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import minions.encoder.EncoderSaver;
import minions.minimizer.AdaGradCluster;
import minions.minimizer.lemur.AdaGradThreadedLemur;
import minions.program.PostExperimentLoader;
import minions.program.PrePostExperimentLoader;
import models.code.TestTriplet;
import models.encoder.EncoderParams;
import models.encoder.ModelFormat;
import models.encoder.encoders.Encoder;
import util.FileSystem;
import util.RandomUtil;
import util.Warnings;

public class ClusterMidpointLemur {
	private static final String LANGUAGE = "karel";
	private static final String MODEL_TYPE = "lemur";
	private static final String NAME_BASE = "lemur";

	List<TestTriplet> trainSet = null;
	ModelFormat format = null;

	private void run(String nameExt) {
		setParameters();

		format = new ModelFormat(LANGUAGE, MODEL_TYPE);
		FileSystem.setAssnId("Midpoint");

		FileSystem.setExpId("prePostExp3");
		System.out.println("load test...");
		trainSet = PrePostExperimentLoader.loadTrainSet(format.getLanguage());
		
		train(nameExt);
		System.out.println("DONE!");
	}

	private void setParameters() {

		int m = RandomUtil.nextInt(10, 100);
		
		EncoderParams.setCodeVectorSize(m * m);
		EncoderParams.setStateVectorSize(m);

		double weightDecay = getParamLogScale(0.00001, 0.01);
		double learningRate = getParamLogScale(0.0001, 0.5);
		int miniBatchSize = (int) getParamLogScale(100, 100000);

		EncoderParams.setMultiThreaded(Math.random() > 1.0);
		EncoderParams.setWeightDecay(weightDecay);
		EncoderParams.setLearningRate(learningRate);
		EncoderParams.setMiniBatchSize(miniBatchSize);
	}

	private double getParamLogScale(double min, double max) {
		double x = Math.random();
		return min * Math.pow(Math.E, Math.log(max/min) * x);
	}

	private void train(String nameExt) {
		String name = NAME_BASE + "-" + nameExt;
		AdaGradThreadedLemur.train(format, trainSet, 24, name);
	}

	public static void main(String[] args) {
		Warnings.check(args.length == 3, "wrong num args");
		String argDataPath = args[0];
		String argName = args[1];
		int numThreads = Integer.parseInt(args[2]);
		EncoderParams.setNumThreads(numThreads);
		FileSystem.setDataDir(argDataPath);
		//int n = Integer.parseInt(argN);
		//int sqrt = (int) Math.sqrt(n);
		//Warnings.check(sqrt * sqrt == n, "must be square number");
		new ClusterMidpointLemur().run(argName);
	}
}

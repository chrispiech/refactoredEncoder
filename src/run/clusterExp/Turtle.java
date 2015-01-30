package run.clusterExp;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import minions.encoder.EncoderSaver;
import minions.minimizer.AdaGradCluster;
import minions.minimizer.turtle.AdaGradThreadedTurtle;
import minions.minimizer.turtle.AdaGradTurtle;
import minions.program.PostExperimentLoader;
import minions.program.PrePostExperimentLoader;
import models.code.TestTriplet;
import models.encoder.EncoderParams;
import models.encoder.ModelFormat;
import models.encoder.encoders.Encoder;
import util.FileSystem;
import util.RandomUtil;
import util.Warnings;

public class Turtle {
	private static final String LANGUAGE = "karel";
	private static final String MODEL_TYPE = "turtle";
	private static final String NAME_BASE = "terapin";
	
	List<TestTriplet> trainSet = null;
	List<TestTriplet> testSet = null;
	ModelFormat format = null;

	private void run(String nameExt) {
		setParameters();
		format = new ModelFormat(LANGUAGE, MODEL_TYPE);
		FileSystem.setAssnId("Midpoint");
		FileSystem.setExpId("runExp");
		train(nameExt);
	}

	private void setParameters() {
		ArrayList<Integer> nOptions = new ArrayList<Integer>();
		for(int i = 15; i < 40; i++) {
			nOptions.add(i * i);
		}
		Random randomGenerator = new Random();
		int index = randomGenerator.nextInt(nOptions.size());
		int n = nOptions.get(index);

		EncoderParams.setCodeVectorSize(n);
		EncoderParams.setStateVectorSize(EncoderParams.getSqrtN());
		
		double weightDecay = getParamLogScale(0.0000001, 1);
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

	private void train(String nameExt) {
		int maxHours = 24 * 2;
		String name = NAME_BASE + "-" + nameExt;
		AdaGradThreadedTurtle.train(format, "runExp", maxHours, name);
	}
	
	public static void main(String[] args) {
		String argDataPath = args[0];
		String argName = args[1];

		FileSystem.setDataDir(argDataPath);
		new Turtle().run(argName);
	}
}

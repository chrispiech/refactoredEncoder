package run.clusterExp;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import minions.encoder.EncoderSaver;
import minions.encoder.factory.EncoderFactory;
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

public class RandomLemur {
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
		
		String notes = EncoderSaver.makeNotes();
		double[] x = EncoderFactory.makeRandomVec(format);
		EncoderSaver.save(x, format, nameExt, "model", notes);
		
		String log = "";
		log = "log start " + new SimpleDateFormat("dd-MM-yyyy").format(new Date()) + "\n";
		log += EncoderSaver.makeNotes() + "\n";
		log += "-------------\n";
		System.out.println(log);
		
		File expDir = FileSystem.getExpDir();
		File savedModelsDir = new File(expDir, "savedModels");
		File modelDir = new File(savedModelsDir, nameExt);
		FileSystem.createFile(modelDir, "log.txt", log);
	}

	private void setParameters() {

		int m = RandomUtil.nextInt(10, 150);
		
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
		Warnings.check(args.length == 2, "wrong num args");
		String argDataPath = args[0];
		String argName = args[1];
		FileSystem.setDataDir(argDataPath);
		//int n = Integer.parseInt(argN);
		//int sqrt = (int) Math.sqrt(n);
		//Warnings.check(sqrt * sqrt == n, "must be square number");
		new RandomLemur().run(argName);
	}
}

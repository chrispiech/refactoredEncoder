package run.clusterExp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import minions.minimizer.AdaGradCluster;
import minions.program.PrePostExperimentLoader;
import models.code.State;
import models.code.TestTriplet;
import models.encoder.EncoderParams;
import models.encoder.ModelFormat;
import util.FileSystem;

public class MidpointBee2 {
	private static final String LANGUAGE = "karel";
	private static final String MODEL_TYPE = "bee";
	private static final String NAME_BASE = "yellow";
	
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
		ArrayList<Integer> mOptions = new ArrayList<Integer>();
		for(int i = 12; i < 20; i++) {
			mOptions.add(i);
		}
		Random randomGenerator = new Random();
		int index = randomGenerator.nextInt(mOptions.size());
		int m = mOptions.get(index);

		EncoderParams.setCodeVectorSize(m*m);
		EncoderParams.setStateVectorSize(m);
		
		double weightDecay = getParamLogScale(0.0001, .01);
		double learningRate = getParamLogScale(0.01, 0.1);
		int miniBatchSize = 5000;//(int) getParamLogScale(3000, 9000);
		
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
		List<TestTriplet> trainSet = loadTrainSet();
		Collections.shuffle(trainSet);
		AdaGradCluster.train(format, trainSet, trainSet.subList(0, 10), maxHours, name);
	}
	
	private List<TestTriplet> loadTrainSet() {
		File expDir = new File(FileSystem.getAssnDir(), "stateExp");
		File trainFile = new File(expDir, "states30_2.csv");
		Scanner codeIn = null;
		List<TestTriplet> trainSet = new ArrayList<TestTriplet>();
		System.out.println("loading...");
		int done = 0;
		try {
			codeIn = new Scanner(trainFile);
			while (codeIn.hasNextLine()) {
				String line = codeIn.nextLine();
				trainSet.add(loadTestTriplet(line));
				if(++done % 100 == 0) System.out.println(done);
			}
			codeIn.close();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
		return trainSet;
	}

	private TestTriplet loadTestTriplet(String line) {
		String[] cols = line.split(",");
		int count = Integer.parseInt(cols[0]);

		List<String> stateList = new ArrayList<String>();
		for(int j = 1; j < cols.length; j++) {
			stateList.add(cols[j]);
		}

		State post = PrePostExperimentLoader.loadState(format.getLanguage(), stateList);

		return new TestTriplet(null, post, null, null, count);
	}

	public static void main(String[] args) {
		String argDataPath = args[0];
		String argName = args[1];

		FileSystem.setDataDir(argDataPath);
		new MidpointBee2().run(argName);
	}
}

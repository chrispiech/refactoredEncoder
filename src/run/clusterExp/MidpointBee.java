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

public class MidpointBee {
	private static final String LANGUAGE = "karel2";
	private static final String MODEL_TYPE = "bee";
	private static final String NAME_BASE = "bumble";
	
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
		double learningRate = getParamLogScale(0.001, 0.1);
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
		List<TestTriplet> trainSet = loadTrainSet();
		Collections.shuffle(trainSet);
		AdaGradCluster.train(format, trainSet, trainSet.subList(0, 10), maxHours, name);
	}
	
	private List<TestTriplet> loadTrainSet() {
		File expDir = new File(FileSystem.getAssnDir(), "runExp");
		File trainFile = new File(expDir, "prePost.csv");
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
		String astId = cols[0];
		int stateSize = (cols.length - 2) / 2;

		int start1 = 2;
		int start2 = 2 + stateSize;
		List<String> preList = new ArrayList<String>();
		for(int j = start1; j < start2; j++) {
			preList.add(cols[j]);
		}
		List<String> postList = new ArrayList<String>();
		for(int j = start2; j < cols.length; j++) {
			postList.add(cols[j]);
		}

		State pre =  PrePostExperimentLoader.loadState(format.getLanguage(), preList);
		State post = PrePostExperimentLoader.loadState(format.getLanguage(), postList);

		return new TestTriplet(pre, post, null, astId, 1);
	}

	public static void main(String[] args) {
		String argDataPath = args[0];
		String argName = args[1];

		FileSystem.setDataDir(argDataPath);
		new MidpointBee().run(argName);
	}
}

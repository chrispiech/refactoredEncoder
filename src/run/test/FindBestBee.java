package run.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import minions.encoder.EncoderSaver;
import minions.encoder.ModelTester;
import minions.program.PrePostExperimentLoader;
import models.code.State;
import models.code.TestTriplet;
import models.encoder.EncoderParams;
import models.encoder.encoders.models.StateEncodable;
import models.language.KarelLanguage;
import util.FileSystem;

public class FindBestBee {

	public static void main(String[] args) {
		new FindBestBee().run();
	}

	private void run() {
		FileSystem.setAssnId("Midpoint");
		FileSystem.setExpId("runExp");

		List<TestTriplet> test = loadTrainSet();


		Map<String, Double> accMap = new HashMap<String, Double>();
		File savedModels = new File(FileSystem.getExpDir(), "savedModels");
		for(File f : FileSystem.listFiles(savedModels)) {
			if(!f.isDirectory()) {
				continue;

			}
			File modelDir = new File(f, "model");
			String key = f.getName();
			StateEncodable m = (StateEncodable) EncoderSaver.load(modelDir);
			double accSum = 0;
			for(TestTriplet t : test) {
				State post = t.getPostcondition();
				State postHat = m.getAutoEncode(post);
				accSum += ModelTester.calcAccuracyPartialCredit(m.getFormat(), post, postHat);
			}
			double acc = accSum / test.size();
			System.out.println(key + "\t" + acc + "\t" + EncoderParams.getM());
		}
	}

	private Map<String, StateEncodable> loadModels() {
		Map<String, StateEncodable> models = new HashMap<String, StateEncodable>();

		File savedModels = new File(FileSystem.getExpDir(), "savedModels");
		for(File f : FileSystem.listFiles(savedModels)) {
			if(f.isDirectory()) {
				File modelDir = new File(f, "model");
				models.put(f.getName(), (StateEncodable) EncoderSaver.load(modelDir));
			}
		}
		return models;
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

		State pre =  PrePostExperimentLoader.loadState(new KarelLanguage(), preList);
		State post = PrePostExperimentLoader.loadState(new KarelLanguage(), postList);

		return new TestTriplet(pre, post, null, astId, 1);
	}
}

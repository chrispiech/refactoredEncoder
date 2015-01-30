package run.forceMultiply.midpoint;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

import minions.forceMult.FMEncoderRandom;
import minions.forceMult.FMMinion;
import minions.forceMult.ForceMultiplier;
import minions.program.PrePostExperimentLoader;
import models.code.State;
import models.code.TestTriplet;
import models.encoder.ModelFormat;
import models.language.KarelLanguage;

import org.ejml.simple.SimpleMatrix;

import run.forceMultiply.ForceMultUtil;
import util.FileSystem;
import util.MatrixUtil;
import util.RandomUtil;

public class SanityFeedback2 {

	private static final int NUM_PROGRAMS = -1;
	private static final int BUDGET = 500;

	public static void main(String[] args) {
		new SanityFeedback2().run();
	}

	Map<String, List<Integer>>  feedbackMap;

	private void run() {

		FileSystem.setAssnId("Midpoint");

		// but the feedback lives in the feedbackExp :)
		System.out.println("loading feedback...");
		FileSystem.setExpId("feedbackExp");
		feedbackMap = ForceMultUtil.loadFeedbackZip();

		FileSystem.setExpId("prePostExp");

		TreeMap<String, SimpleMatrix> encodingMap = makeEncodingMap();


		Set<String> toGrade = encodingMap.keySet();
		System.out.println("num programs: " + toGrade.size());

		System.out.println("running active learning!");
		FMMinion minion = new FMEncoderRandom(encodingMap, 0);
		ForceMultiplier force = new ForceMultiplier(minion, feedbackMap, toGrade);
		force.run(BUDGET);
	}

	private class Unit {
		Map<State, SimpleMatrix> stateMap = new HashMap<State, SimpleMatrix>();
	}



	private TreeMap<String, SimpleMatrix> makeEncodingMap() {
		Map<String, Unit> programTests = new HashMap<String, Unit>();

		Set<State> preconditions = new HashSet<State>();

		int m = 0;
		for(TestTriplet t : loadTrainSet()) {
			String key = t.getAstId();
			State pre = t.getPrecondition();
			State post = t.getPostcondition();


			preconditions.add(pre);
			if(!programTests.containsKey(key)) {
				programTests.put(key, new Unit());
			}

			SimpleMatrix v = getMatrix(post);
			m = v.getNumElements();
			programTests.get(key).stateMap.put(pre, v);

		}

		List<State> preList = new ArrayList<State>(preconditions);

		TreeMap<String, SimpleMatrix> encodingMap = new TreeMap<String, SimpleMatrix>();
		for(String k : programTests.keySet()) {
			ArrayList<Double> embedding = new ArrayList<Double>();
			for(State s : preList) {
				SimpleMatrix v = programTests.get(k).stateMap.get(s);
				embedding.addAll(MatrixUtil.matrixToList(v));
			}
			SimpleMatrix codeVec = MatrixUtil.listToMatrix(embedding, m * 3, 1);

			encodingMap.put(k, codeVec);
		}

		return encodingMap;

	}

	private SimpleMatrix getMatrix(State post) {
		ModelFormat f = new ModelFormat("karel", "bee");
		SimpleMatrix e = new SimpleMatrix(5 + 7, 1);
		for(int i = 0 ; i < 5 + 7; i++) {
			e.set(i, getNoise());
		}
		int r = post.getNumber("row");
		int c = post.getNumber("col");
		SimpleMatrix statusVec = post.getActivation(f, "status");
		for(int i = 0; i < 5; i++) {
			e.set(i, e.get(i) + statusVec.get(i));
		}
		e.set(c + 5, 1);
		return e;
	}

	private double getNoise() {
		return RandomUtil.gauss(0, 0.1);
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

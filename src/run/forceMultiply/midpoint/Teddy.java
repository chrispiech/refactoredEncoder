package run.forceMultiply.midpoint;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

import minions.encoder.EncoderSaver;
import minions.forceMult.FMEncoderRandom;
import minions.forceMult.FMMinion;
import minions.forceMult.ForceMultiplier;
import minions.program.PrePostExperimentLoader;
import models.code.TestTriplet;
import models.encoder.EncoderParams;
import models.encoder.encoders.models.StateEncodable;
import models.language.KarelLanguage2;

import org.apache.commons.math3.util.Pair;
import org.ejml.simple.SimpleMatrix;

import run.forceMultiply.ForceMultUtil;
import util.FileSystem;
import util.MatrixUtil;

public class Teddy {
	
	private static double LAMBDA = 0.9; 

	public static void main(String[] args) {
		new Teddy().run();
	}

	private void run() {
		FileSystem.setAssnId("Midpoint");
		System.out.println("loading model...");
		FileSystem.setExpId("runExp");
		StateEncodable model = (StateEncodable) EncoderSaver.load("yellow-1426057_5/model");
		
		FileSystem.setExpId("feedbackExp");
		Map<String, List<Integer>> feedbackMap = ForceMultUtil.loadFeedbackZip();
		
		// we need to load the pre-post conditions...
		Map<String, List<TestTriplet>> runMap = loadRunMap();
		
		// then calculate the 
		TreeMap<String, SimpleMatrix> encodingMap = getEncodingMap(model, runMap);
		
		System.out.println("lambda: " + LAMBDA);
		// then run force multiplication! 
		Set<String> toGrade = encodingMap.keySet();
		FMMinion minion = new FMEncoderRandom(encodingMap, 0);
		ForceMultiplier force = new ForceMultiplier(minion, feedbackMap, toGrade);
		force.run(500);
	}

	private TreeMap<String, SimpleMatrix> getEncodingMap(
			StateEncodable model, 
			Map<String, List<TestTriplet>> runMap) {
		TreeMap<String, SimpleMatrix> encodingMap = new TreeMap<String, SimpleMatrix>();
		int done = 0;
		for(String s : runMap.keySet()) {
			SimpleMatrix bestFit = getBestFit(model, runMap.get(s));
			encodingMap.put(s, bestFit);
			if(++done % 100 == 0) System.out.println(done);
		}
		return encodingMap;
	}

	private SimpleMatrix getBestFit(StateEncodable model, List<TestTriplet> list) {
		List<Pair<SimpleMatrix, SimpleMatrix>> pairs = getPrePost(model, list);
		int m = EncoderParams.getM();
		SimpleMatrix X = makeX(pairs);
		SimpleMatrix P = new SimpleMatrix(m, m);
		for(int i = 0; i < m; i++) {
			SimpleMatrix y = makeY(pairs, i);
			SimpleMatrix theta = fit(X, y);
			MatrixUtil.setRow(P, i, theta);
		}
		
		P.reshape(m * m, 1);
		
		return P;
	}

	private SimpleMatrix fit(SimpleMatrix X, SimpleMatrix y) {
		int m = EncoderParams.getM();
		SimpleMatrix I = SimpleMatrix.identity(m);
		SimpleMatrix term1 = (X.transpose().mult(X).plus(I.scale(LAMBDA)));
		return term1.invert().mult(X.transpose().mult(y));

	}

	private SimpleMatrix makeX(List<Pair<SimpleMatrix, SimpleMatrix>> pairs) {
		int m = EncoderParams.getM();
		SimpleMatrix X = new SimpleMatrix(pairs.size(), m);
		for(int i = 0; i < pairs.size(); i++) {
			Pair<SimpleMatrix, SimpleMatrix> prePost = pairs.get(i);
			SimpleMatrix pre = prePost.getFirst();
			for(int j = 0; j < m; j++) {
				X.set(i, j, pre.get(j));
			}
		}
		return X;
	}
	
	private SimpleMatrix makeY(List<Pair<SimpleMatrix, SimpleMatrix>> pairs, int i) {
		SimpleMatrix y = new SimpleMatrix(pairs.size(), 1);
		for(int j = 0; j < pairs.size(); j++) {
			double v = pairs.get(j).getSecond().get(i);
			y.set(j, v);
		}
		return y;
	}
	
	private List<Pair<SimpleMatrix, SimpleMatrix>> getPrePost(StateEncodable model, 
			List<TestTriplet> list) {
		List<Pair<SimpleMatrix, SimpleMatrix>> pairs = new
				ArrayList<Pair<SimpleMatrix, SimpleMatrix>>();
		for(TestTriplet t : list) {
			SimpleMatrix pre = model.getVector(t.getPrecondition());
			SimpleMatrix post = model.getVector(t.getPostcondition());
			pairs.add(new Pair<SimpleMatrix, SimpleMatrix>(pre,post));
		}
		return pairs;
	}

	private Map<String, List<TestTriplet>> loadRunMap() {
		FileSystem.setExpId("runExp");
		int done = 0;
		File prePostCsv = new File(FileSystem.getExpDir(), "prePost.csv");
		Map<String, List<TestTriplet>> runMap = 
				new TreeMap<String, List<TestTriplet>>();
		try {
			Scanner sc = new Scanner(prePostCsv);
			while(sc.hasNextLine()) {
				String line = sc.nextLine();
				TestTriplet t = lineToTest(line);
				if(!runMap.containsKey(t.getAstId())) {
					runMap.put(t.getAstId(), new ArrayList<TestTriplet>());
				}
				runMap.get(t.getAstId()).add(t);
				if(++done % 100 == 0) System.out.println(done);
			}
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
		
		return runMap;
	}

	private TestTriplet lineToTest(String line) {
		line = "1," + line;
		return PrePostExperimentLoader.lineToTest(
				new KarelLanguage2(), line.split(","),
				null, "0");
	}
}

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

import minions.encoder.EncoderSaver;
import minions.forceMult.FMEncoderRandom;
import minions.forceMult.FMMinion;
import minions.forceMult.ForceMultiplier;
import minions.program.PrePostExperimentLoader;
import models.code.State;
import models.code.TestTriplet;
import models.encoder.EncoderParams;
import models.encoder.encoders.Encoder;
import models.encoder.encoders.models.StateEncodable;
import models.language.KarelLanguage;

import org.ejml.simple.SimpleMatrix;

import run.forceMultiply.ForceMultUtil;
import util.FileSystem;
import util.MatrixUtil;
import util.RandomUtil;

public class SanityFeedback {

	private static final int NUM_PROGRAMS = -1;
	private static final int BUDGET = 500;

	public static void main(String[] args) {
		new SanityFeedback().run();
	}
	
	Map<String, List<Integer>>  feedbackMap;
	
	private void run() {

		FileSystem.setAssnId("Midpoint");

		// but the feedback lives in the feedbackExp :)
		System.out.println("loading feedback...");
		FileSystem.setExpId("feedbackExp");
		feedbackMap = ForceMultUtil.loadFeedbackZip();

		// the programs came from the start experiment!
		System.out.println("loading model...");

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
		TreeMap<String, SimpleMatrix> encodingMap = new TreeMap<String, SimpleMatrix>();
		
		for(TestTriplet t : loadTrainSet()) {
			String key = t.getAstId();
			State pre = t.getPrecondition();
			State post = t.getPostcondition();
			if(pre.getNumber("worldRows") == 6) {
				String f = "";
				SimpleMatrix e = new SimpleMatrix(1, 1);
				e.set(0, 0);
				for(Integer s : feedbackMap.get(key)) {
					f += s + ",";
					if(s == 4) {
						e.set(0, post.getNumber("col"));
					}
				}
				encodingMap.put(key, e);
			}
			
			
		}
		
		
		
		return encodingMap;
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

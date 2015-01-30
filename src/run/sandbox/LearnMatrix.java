package run.sandbox;

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

import org.ejml.simple.SimpleMatrix;

import minions.encoder.EncoderSaver;
import minions.program.PrePostExperimentLoader;
import models.code.State;
import models.code.TestTriplet;
import models.encoder.EncoderParams;
import models.encoder.encoders.Encoder;
import models.encoder.encoders.models.StateEncodable;
import models.language.KarelLanguage;
import run.forceMultiply.ForceMultUtil;
import run.forceMultiply.midpoint.BeeFeedback.Unit;
import util.FileSystem;
import util.MatrixUtil;

public class LearnMatrix {
	private static  void main(String[] args) {
		new LearnMatrix().run();
	}

	private void run() {
		FileSystem.setAssnId("Midpoint");

		// but the feedback lives in the feedbackExp :)
		System.out.println("loading feedback...");
		FileSystem.setExpId("feedbackExp");
		Map<String, List<Integer>> feedbackMap = ForceMultUtil.loadFeedbackZip();

		// the programs came from the start experiment!
		System.out.println("loading model...");
		FileSystem.setExpId("runExp");
		Encoder model = EncoderSaver.load("yellow-1424207_3/model");

		FileSystem.setExpId("prePostExp");

		TreeMap<String, SimpleMatrix> encodingMap = makeEncodingMap((StateEncodable) model);

	}
	
	private class Unit {
		Map<State, SimpleMatrix> stateMap = new HashMap<State, SimpleMatrix>();
	}
	
	private TreeMap<String, SimpleMatrix> makeEncodingMap(StateEncodable model) {
		Map<String, Unit> programTests = new HashMap<String, Unit>();
		
		Set<State> preconditions = new HashSet<State>();
		for(TestTriplet t : loadTrainSet()) {
			String key = t.getAstId();
			State pre = t.getPrecondition();
			if(pre.getNumber("worldRows") != 6) continue;
			State post = t.getPostcondition();
			preconditions.add(pre);
			if(!programTests.containsKey(key)) {
				programTests.put(key, new Unit());
			}
			SimpleMatrix v = model.getVector(post);
			programTests.get(key).stateMap.put(pre, v);
			
		}
		
		TreeMap<String, SimpleMatrix> encodingMap = new TreeMap<String, SimpleMatrix>();
		for(String p : programTests.keySet()) {
			SimpleMatrix embedding = loadEmbedding(model, programTests.get(p));
			encodingMap.put(p, embedding);
		}
		
		return encodingMap;
	}

	private SimpleMatrix loadEmbedding(StateEncodable model, Unit unit) {
		// TODO Auto-generated method stub
		return null;
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

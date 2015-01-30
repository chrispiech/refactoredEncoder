package run.forceMultiply.blocky;

import java.io.File;
import java.util.*;

import minions.encoder.EncoderSaver;
import minions.forceMult.FMEncoderActive;
import minions.forceMult.FMEncoderRandom;
import minions.forceMult.FMMinion;
import minions.forceMult.ForceMultiplier;
import minions.parser.EncodeGraphParser;
import minions.program.PostExperimentLoader;
import models.ast.Tree;
import models.code.State;
import models.code.TestTriplet;
import models.encoder.ClusterableMatrix;
import models.encoder.EncodeGraph;
import models.encoder.EncoderParams;
import models.encoder.encoders.Encoder;
import models.encoder.encoders.models.StateEncodable;
import models.encoder.neurons.TreeNeuron;
import models.language.Language;

import org.ejml.simple.SimpleMatrix;
import org.json.JSONArray;
import org.json.JSONObject;

import util.FileSystem;
import util.RandomUtil;

public class PostRandomMany {

	private static final int NUM_PROGRAMS = -1;
	private static final int BUDGET = 500;

	private static final int ITERATIONS = 20;


	private void run() {
		FileSystem.setAssnId("Hoc18");

		String modelExp = "prePostExp";
		String modelName = "bumbleBee";

		// but the feedback lives in the feedbackExp :)
		System.out.println("loading...");
		FileSystem.setExpId("feedbackExp");
		Map<String, List<Integer>> feedbackMap = loadFeedback();

		// the programs came from the start experiment!
		FileSystem.setExpId(modelExp);
		StateEncodable model = (StateEncodable) EncoderSaver.load(modelName);

		FileSystem.setExpId("postExp");
		Map<String, TestTriplet> programMap = loadPrograms(model.getFormat().getLanguage());
		TreeMap<String, SimpleMatrix> encodingMap = makeEncodingMap(programMap, model);

		Set<String> toGrade = encodingMap.keySet();
		System.out.println("num programs: " + toGrade.size());

		FileSystem.setExpId("feedbackExp");
		File resultDir = new File(FileSystem.getExpDir(), "results");
		File resultModelDir = new File(resultDir, "random-" + modelName);
		
		System.out.println("running active learning!");
		for(int i = 0; i < ITERATIONS; i++) {
			FMMinion minion = new FMEncoderRandom(encodingMap, i);
			ForceMultiplier force = new ForceMultiplier(minion, feedbackMap, toGrade);
			String result = force.run(BUDGET);
			
			String resultFileName = "random-" + modelName + "-" + i + ".txt";
			FileSystem.createFile(resultModelDir, resultFileName, result);
		}
	}

	private TreeMap<String, SimpleMatrix> makeEncodingMap(
			Map<String, TestTriplet> programMap, StateEncodable model) {
		TreeMap<String, SimpleMatrix> encodingMap = new TreeMap<String, SimpleMatrix>();
		for(String id : programMap.keySet()) {
			TestTriplet test = programMap.get(id);
			try{
				State post = test.getPostcondition();
				SimpleMatrix pv = model.getStateEncoder().getVector(post);
				encodingMap.put(id, pv);
			} catch(RuntimeException e) {
				System.out.println("ERROR PARSING: " + id);
			}

		}
		return encodingMap;
	}

	private Map<String, TestTriplet> loadPrograms(Language lang) {
		List<TestTriplet> tests = PostExperimentLoader.load(NUM_PROGRAMS, lang);
		Map<String, TestTriplet> programMap = new TreeMap<String, TestTriplet>();
		for(TestTriplet t : tests) {
			programMap.put(t.getAstId(), t);
		}
		return programMap;
	}

	private Map<String, List<Integer>> loadFeedback() {
		File expDir = FileSystem.getExpDir();
		File feedbackFile = new File(expDir, "feedback.json");
		String feedbackStr = FileSystem.getFileContents(feedbackFile);
		JSONObject feedback = new JSONObject(feedbackStr);
		Map<String, List<Integer>> feedbackMap = new HashMap<String, List<Integer>>();
		for(Object key : feedback.keySet()) {
			String astId = (String)key;
			JSONArray feedbackListJson = feedback.getJSONArray(astId);
			List<Integer> feedbackList = new ArrayList<Integer>();
			for(int i = 0; i < feedbackListJson.length(); i++) {
				int feedbackId = feedbackListJson.getInt(i);
				feedbackList.add(feedbackId);
			}
			feedbackMap.put(astId, feedbackList);
		}
		return feedbackMap;
	}

	public static void main(String[] args) {
		new PostRandomMany().run();
	}
}

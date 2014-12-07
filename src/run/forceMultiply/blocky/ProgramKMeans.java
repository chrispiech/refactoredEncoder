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
import models.code.TestTriplet;
import models.encoder.CodeVector;
import models.encoder.EncodeGraph;
import models.encoder.EncoderParams;
import models.encoder.encoders.Encoder;
import models.encoder.neurons.TreeNeuron;
import models.language.Language;

import org.ejml.simple.SimpleMatrix;
import org.json.JSONArray;
import org.json.JSONObject;

import util.FileSystem;
import util.RandomUtil;

public class ProgramKMeans {

	private static final int NUM_PROGRAMS = 2000;
	private static final int BUDGET = -1;


	private void run() {
		EncoderParams.setCodeVectorSize(100);
		FileSystem.setAssnId("Hoc18");

		// but the feedback lives in the feedbackExp :)
		System.out.println("loading...");
		FileSystem.setExpId("feedbackExp");
		Map<String, List<Integer>> feedbackMap = loadFeedback();

		// the programs came from the start experiment!
		FileSystem.setExpId("prePostExp");
		Encoder model = EncoderSaver.load("gorilla-epoch10");

		FileSystem.setExpId("postExp");
		Map<String, TestTriplet> programMap = loadPrograms(model);
		TreeMap<String, SimpleMatrix> encodingMap = makeEncodingMap(programMap, model);


		Set<String> toGrade = encodingMap.keySet();
		System.out.println("num programs: " + toGrade.size());

		System.out.println("running active learning!");
		FMMinion minion = new FMEncoderActive(encodingMap);
		ForceMultiplier force = new ForceMultiplier(minion, feedbackMap, toGrade);
		force.run(BUDGET);
	}

	private TreeMap<String, SimpleMatrix> makeEncodingMap(
			Map<String, TestTriplet> programMap, Encoder model) {
		TreeMap<String, SimpleMatrix> encodingMap = new TreeMap<String, SimpleMatrix>();
		for(String id : programMap.keySet()) {
			TestTriplet test = programMap.get(id);
			try{
				CodeVector cv = model.getCodeVector(test);
				encodingMap.put(id, cv.getVector());
			} catch(RuntimeException e) {
				System.out.println("ERROR PARSING: " + id);
			}

		}
		return encodingMap;
	}

	private Map<String, TestTriplet> loadPrograms(Encoder model) {
		Language lang = model.getFormat().getLanguage();
		List<TestTriplet> tests = PostExperimentLoader.load(NUM_PROGRAMS, lang);
		Map<String, TestTriplet> programMap = new TreeMap<String, TestTriplet>();
		for(TestTriplet t : tests) {
			programMap.put(t.getId(), t);
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
		new ProgramKMeans().run();
	}
}

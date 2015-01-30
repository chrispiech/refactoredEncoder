package run.forceMultiply.newspaper;

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
import models.encoder.ClusterableMatrix;
import models.encoder.EncodeGraph;
import models.encoder.EncoderParams;
import models.encoder.encoders.Encoder;
import models.encoder.neurons.TreeNeuron;
import models.language.Language;

import org.ejml.simple.SimpleMatrix;
import org.json.JSONArray;
import org.json.JSONObject;

import run.forceMultiply.ForceMultUtil;
import util.FileSystem;
import util.RandomUtil;

public class ProgramRandom {

	private static final int NUM_PROGRAMS = -1;
	private static final int BUDGET = 500;


	private void run() {

		FileSystem.setAssnId("Newspaper");

		// but the feedback lives in the feedbackExp :)
		System.out.println("loading feedback...");
		FileSystem.setExpId("feedbackExp");
		Map<String, List<Integer>> feedbackMap = loadFeedback();

		// the programs came from the start experiment!
		System.out.println("loading model...");
		EncoderParams.stateHasSize = false;
		EncoderParams.worldRows = 5;
		
		FileSystem.setExpId("prePostExp");
		Encoder model = EncoderSaver.load("simpleMonkey100");
	
		Map<String, TreeNeuron> programMap = ForceMultUtil.loadProgramsZip();
		TreeMap<String, SimpleMatrix> encodingMap = ForceMultUtil.makeEncodingMap(programMap, model);


		Set<String> toGrade = encodingMap.keySet();
		System.out.println("num programs: " + toGrade.size());

		System.out.println("running active learning!");
		FMMinion minion = new FMEncoderRandom(encodingMap, 0);
		ForceMultiplier force = new ForceMultiplier(minion, feedbackMap, toGrade);
		force.run(BUDGET);
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
		new ProgramRandom().run();
	}
}

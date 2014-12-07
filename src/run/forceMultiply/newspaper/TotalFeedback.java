package run.forceMultiply.newspaper;

import java.io.File;
import java.util.*;

import minions.encoder.EncoderSaver;
import minions.forceMult.FMEncoderActive;
import minions.forceMult.FMEncoderRandom;
import minions.forceMult.FMMinion;
import minions.forceMult.FMRandomChoser;
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
import models.language.KarelLanguage;
import models.language.Language;

import org.ejml.simple.SimpleMatrix;
import org.json.JSONArray;
import org.json.JSONObject;

import util.FileSystem;
import util.RandomUtil;

public class TotalFeedback {

	private void run() {
		FileSystem.setAssnId("Newspaper");
		EncoderParams.setWorldDim(5, 7);

		// but the feedback lives in the feedbackExp :)
		System.out.println("loading feedback...");
		FileSystem.setExpId("feedbackExp");
		Map<String, List<Integer>> feedbackMap = loadFeedback();


		FileSystem.setExpId("postExp");
		Map<String, TestTriplet> programMap = loadPrograms(new KarelLanguage());
		
		Set<String> toGrade = programMap.keySet();
		System.out.println("num programs: " + toGrade.size());

		int total = 0;
		for(String id : toGrade) {
			total += feedbackMap.get(id).size();
		}
		System.out.println("total: " + total);
	}


	private Map<String, TestTriplet> loadPrograms(Language lang) {
		List<TestTriplet> tests = PostExperimentLoader.load(-1, lang);
		Map<String, TestTriplet> programMap = new TreeMap<String, TestTriplet>();
		for(TestTriplet t : tests) {
			if(!t.getEncodeGraph().hasCycles()) {
				programMap.put(t.getId(), t);
			}
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
		new TotalFeedback().run();
	}
}

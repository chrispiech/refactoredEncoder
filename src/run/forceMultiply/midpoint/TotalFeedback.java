package run.forceMultiply.midpoint;

import java.io.File;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import minions.encoder.EncoderSaver;
import minions.forceMult.FMEncoderActive;
import minions.forceMult.FMEncoderRandom;
import minions.forceMult.FMMinion;
import minions.forceMult.FMRandomChoser;
import minions.forceMult.ForceMultiplier;
import minions.parser.EncodeGraphParser;
import minions.program.EncodeGraphsLoader;
import minions.program.PostExperimentLoader;
import models.ast.Tree;
import models.code.TestTriplet;
import models.encoder.ClusterableMatrix;
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
		FileSystem.setAssnId("Midpoint");

		// but the feedback lives in the feedbackExp :)
		System.out.println("loading feedback...");
		FileSystem.setExpId("feedbackExp");
		Map<String, List<Integer>> feedbackMap = loadFeedbackZip();

		FileSystem.setExpId("prePostExp");
		Map<String, TreeNeuron> programMap = loadPrograms();

		Set<String> toGrade = programMap.keySet();
		System.out.println("num programs: " + toGrade.size());

		int total = 0;
		for(String id : toGrade) {
			if(feedbackMap.containsKey(id)) {
				total += feedbackMap.get(id).size();
			}

		}
		System.out.println("total: " + total);
	}

	private Map<String, TreeNeuron> loadPrograms() {
		System.out.println("loading programs...");
		Language lang = new KarelLanguage();
		File expDir = FileSystem.getExpDir();
		File trainDir = new File(expDir, "train");
		File testDir = new File(expDir, "test");
		try {
			File trainFile = new File(trainDir, "encode.zip");
			System.out.println(trainFile.getAbsolutePath());
			ZipFile trainZip = new ZipFile(trainFile);
			ZipFile testZip = new ZipFile(new File(testDir, "encode.zip"));
			Map<String, TreeNeuron> map = new HashMap<String, TreeNeuron>();
			map.putAll(EncodeGraphsLoader.loadRunTreeClones(lang, trainZip));
			map.putAll(EncodeGraphsLoader.loadRunTreeClones(lang, testZip));
			return map;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private Map<String, List<Integer>> loadFeedbackZip() {
		File expDir = FileSystem.getExpDir();
		File feedbackFile = new File(expDir, "feedback.json.zip");
		String feedbackStr = null;
		try {
			ZipFile zip = new ZipFile(feedbackFile);
			ZipEntry entry = zip.entries().nextElement();
			feedbackStr = FileSystem.getZipEntryString(zip, entry);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		JSONObject feedback = new JSONObject(feedbackStr);
		Map<String, List<Integer>> feedbackMap = new HashMap<String, List<Integer>>();
		System.out.println(feedback.keySet().size());
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

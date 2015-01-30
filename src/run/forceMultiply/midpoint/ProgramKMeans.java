package run.forceMultiply.midpoint;

import java.io.File;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import minions.encoder.EncoderSaver;
import minions.forceMult.FMEncoderActive;
import minions.forceMult.FMEncoderRandom;
import minions.forceMult.FMMinion;
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
import util.Warnings;

public class ProgramKMeans {

	private static final int NUM_PROGRAMS = -1;
	private static final int BUDGET = 500;

	private static final int ITERATIONS = 20;


	private void run() {

		FileSystem.setAssnId("Midpoint");


		// but the feedback lives in the feedbackExp :)
		System.out.println("loading feedback...");
		FileSystem.setExpId("feedbackExp");
		Map<String, List<Integer>> feedbackMap = loadFeedbackZip();

		// the programs came from the start experiment!
		FileSystem.setExpId("prePostExp");
		System.out.println("loading model...");
		Encoder model = EncoderSaver.load("fixed100");

		FileSystem.setExpId("prePostExp");
		Map<String, TreeNeuron> programMap = loadPrograms();
		TreeMap<String, SimpleMatrix> encodingMap = makeEncodingMap(programMap, model);

		Set<String> toGrade = encodingMap.keySet();
		System.out.println("num programs: " + toGrade.size());


		System.out.println("running active learning!");

		FMMinion minion = new FMEncoderActive(encodingMap);
		ForceMultiplier force = new ForceMultiplier(minion, feedbackMap, toGrade);
		force.run(BUDGET);

	}

	private TreeMap<String, SimpleMatrix> makeEncodingMap(
			Map<String, TreeNeuron> programMap, Encoder model) {
		System.out.println("making encoding map...");
		TreeMap<String, SimpleMatrix> encodingMap = new TreeMap<String, SimpleMatrix>();
		int done = 0;
		for(String id : programMap.keySet()) {

			try{
				TreeNeuron neuron = programMap.get(id);
				model.getProgramEncoder().activateTree(neuron);
				SimpleMatrix m = neuron.getActivation();
				Warnings.check(m.isVector());
				encodingMap.put(id, m);
			} catch(RuntimeException e) {
				System.out.println("ERROR PARSING: " + id);
			}
			if(++done % 100 == 0) System.out.println(done);
		}
		return encodingMap;
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
		new ProgramKMeans().run();
	}
}

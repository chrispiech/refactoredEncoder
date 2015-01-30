package run.forceMultiply;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import minions.program.EncodeGraphsLoader;
import models.encoder.encoders.Encoder;
import models.encoder.neurons.TreeNeuron;
import models.language.KarelLanguage;
import models.language.Language;

import org.ejml.simple.SimpleMatrix;
import org.json.JSONArray;
import org.json.JSONObject;

import util.FileSystem;
import util.Warnings;

public class ForceMultUtil {
	public static Map<String, TreeNeuron> loadProgramsZip() {
		System.out.println("loading programs...");
		Language lang = new KarelLanguage();
		File expDir = FileSystem.getExpDir();
		File trainDir = new File(expDir, "train");
		try {
			ZipFile trainZip = new ZipFile(new File(trainDir, "encode.zip"));
			Map<String, TreeNeuron> map = new HashMap<String, TreeNeuron>();
			map.putAll(EncodeGraphsLoader.loadRunTreeClones(lang, trainZip));
			return map;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static TreeMap<String, SimpleMatrix> makeEncodingMap(
			Map<String, TreeNeuron> programMap, Encoder model) {
		System.out.println("making encoding map...");
		TreeMap<String, SimpleMatrix> encodingMap = new TreeMap<String, SimpleMatrix>();
		int done = 0;
		for(String id : programMap.keySet()) {
			try{
				TreeNeuron neuron = programMap.get(id);
				model.getProgramEncoder().activateTree(neuron);
				SimpleMatrix m = neuron.getActivation();
				SimpleMatrix mCopy = new SimpleMatrix(m);
				int numRows = m.getNumElements();
				mCopy.reshape(numRows, 1);
				encodingMap.put(id, mCopy);
			} catch (RuntimeException e) {
				System.out.println("error encoding: " + id);
			}
			if(++done % 100 == 0) System.out.println(done);
		}
		return encodingMap;
	}
	
	public static Map<String, List<Integer>> loadFeedbackZip() {
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
}

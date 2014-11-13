package minions.program;

import java.io.File;
import java.util.*;

import org.ejml.simple.SimpleMatrix;
import org.json.JSONArray;
import org.json.JSONObject;

import models.code.State;
import models.code.TestTriplet;
import models.encoder.EncodeGraph;
import models.encoder.neurons.TreeNeuron;
import models.language.Language;
import util.FileSystem;

public class PrePostExperimentLoader {

	public static List<TestTriplet> loadTests(String dirName, int max, Language language) {
		File expDir = FileSystem.getExpDir();
		File testDir = new File(expDir, dirName);
		return loadTests(testDir, max, language);
	}

	private static List<TestTriplet> loadTests(File testDir, int max, Language language) {
		System.out.println(testDir);
		Map<String, TreeNeuron> runTreeMap = loadTrees(testDir, max, language);
		return getPrePostTriplets(runTreeMap, testDir, max, language);
	}

	private static List<TestTriplet> getPrePostTriplets(
			Map<String, TreeNeuron> runTreeMap, File testDir, int max,
			Language language) {
		System.out.println("loading...");
		File f = new File(testDir, "prePost.csv");
		List<String> lines = FileSystem.getFileLines(f, max+1);
		List<TestTriplet> tests = new ArrayList<TestTriplet>();
		//astId,nodeId,preRow,preCol,preDir,preStat,postRow,postCol,postDir,postStat
		for(int i = 1; i < lines.size(); i++) {
			String line = lines.get(i);
			String[] cols = line.split(",");
			String astId = cols[0];
			String nodeId = cols[1];
			TreeNeuron t = runTreeMap.get(astId);
			TreeNeuron subtree = t.getDescendant(nodeId);
			State pre =  loadBlockyState(cols[2], cols[3], cols[4], cols[5]);
			State post = loadBlockyState(cols[6], cols[7], cols[8], cols[9]);
			
			Map<String, TreeNeuron> methodMap = new HashMap<String, TreeNeuron>();
			methodMap.put("run", subtree);
			EncodeGraph graph = new EncodeGraph(methodMap);
			TestTriplet test = new TestTriplet(pre, post, graph, astId, 1);
			tests.add(test);
		}
		return tests;
	}

	private static Map<String, TreeNeuron> loadTrees(File testDir, int max,
			Language language) {
		Map<String, TreeNeuron> runTreeMap = new HashMap<String, TreeNeuron>();
		File encodeTreeDir = new File(testDir, "encodeTrees");
		int done = 0;
		for(File f : FileSystem.listNumericalFiles(encodeTreeDir)) {
			JSONObject prePostJson = FileSystem.getFileJson(f);
			// ast is a misleading name for the encodeTree field
			JSONObject encodeTreeJson = prePostJson.getJSONObject("ast"); 
			TreeNeuron tree = EncodeTreeLoader.parseJson(encodeTreeJson);
			String name = FileSystem.getNameWithoutExtension(f.getName());
			runTreeMap.put(name, tree);
			
			done++;
			if(done % 100 == 0) {
				System.out.println("asts loaded: " + done);
			}
			if(max > 0 && done >= max) break;
		}
		return runTreeMap;
	}
	
	private static State loadBlockyState(String row, String col, String dir, String stat) {
		Map<String, String> choiceMap = new HashMap<String, String>();
		choiceMap.put("row", row);
		choiceMap.put("col", col);
		choiceMap.put("direction", dir);
		choiceMap.put("status", stat);
		Map<String, Integer> numMap = new HashMap<String, Integer>();
		Map<String, SimpleMatrix> matMap = new HashMap<String, SimpleMatrix>();
		return new State(choiceMap, numMap, matMap);
	}
}

package minions.program;

import java.io.File;
import java.util.*;

import minions.parser.EncodeGraphParser;
import models.ast.Tree;
import models.code.State;
import models.code.TestTriplet;
import models.encoder.EncodeGraph;
import models.encoder.EncoderParams;
import models.language.Language;

import org.ejml.simple.SimpleMatrix;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import util.FileSystem;
import util.Warnings;



public class PostExperimentLoader {
	
	public static List<TestTriplet> load(Language language) {
		return load(-1, language);
	}

	public static List<TestTriplet> load(int maxId, Language language) {
		List<TestTriplet> testSet = PostExperimentLoader.loadTests("test", maxId, language);
		List<TestTriplet> trainSet = PostExperimentLoader.loadTests("train", maxId, language);
		List<TestTriplet> holdSet = PostExperimentLoader.loadTests("holdout", maxId, language);
		List<TestTriplet> all = new ArrayList<TestTriplet>();
		addToList(all, testSet, maxId);
		addToList(all, trainSet, maxId);
		addToList(all, holdSet, maxId);
		return all;
	}
	
	public static List<TestTriplet> loadFolds(String unitTest, int numFolds, int max, Language language) {
		Warnings.check(max == -1);
		List<TestTriplet> tests = new ArrayList<TestTriplet>();
		for(int i = 0; i < numFolds; i++) {
			tests.addAll(loadFold(unitTest, i, language));
		}
		return tests;
	}
	
	public static List<TestTriplet> loadFold(String unitTest, int i, Language language) {
		File expDir = FileSystem.getExpDir();
		File unitTestDir = new File(expDir, unitTest);
		File foldDir = new File(unitTestDir, "fold" + i);
		return PostExperimentLoader.loadTests(foldDir, -1, language);
	}
	
	public static List<TestTriplet> loadFolds(String unitTest, int numFolds, Language language) {
		return loadFolds(unitTest, numFolds, -1, language);
	}

	private static void addToList(List<TestTriplet> all, List<TestTriplet> testSet,int maxId) {
		for(TestTriplet t : testSet) {
			int id = Integer.parseInt(t.getId());
			if(maxId < 0 || id < maxId) {
				all.add(t);
			}
		}	
	}

	public static List<TestTriplet> loadTests(String dirName, int max, Language language) {
		File expDir = FileSystem.getExpDir();
		File testDir = new File(expDir, dirName);
		return loadTests(testDir, max, language);
	}

	public static List<TestTriplet> loadTests(String dirName, Language language) {
		return loadTests(dirName, -1, language);
	}
	

	public static List<TestTriplet> removeRecursive(List<TestTriplet> tests, Language language) {
		List<TestTriplet> culledTests = new ArrayList<TestTriplet>();
		for(TestTriplet t : tests) {
			EncodeGraph g = t.getEncodeGraph();
			if(!g.hasCycles()) {
				culledTests.add(t);
			}
		}
		return culledTests;
	}
	
	private static List<TestTriplet> loadTests(File programDir, int max, Language language) {
		List<File> xmlDirFiles = FileSystem.listNumericalFiles(programDir);
		
		List<TestTriplet> tests = new ArrayList<TestTriplet>();
		for(File f : xmlDirFiles) {
			
			String testJsonStr = FileSystem.getFileContents(f);
			JSONObject json = new JSONObject(testJsonStr);
			TestTriplet test = loadJson(json, language);
			if(test == null) continue;
			tests.add(test);
			
			int numLoaded = tests.size();
			if(numLoaded % 100 == 0) {
				System.out.println("loaded: " + numLoaded);
			}
			if(max > 0 && numLoaded >= max) {
				break;
			}
		}
		
		return tests;
	}
	
	private static TestTriplet loadJson(JSONObject json, Language language) {

		String astId = getAstId(json);
		int count = json.getInt("count");

		Tree ast = TreeLoader.loadJsonTree(json.getJSONObject("ast"));
		EncodeGraph graph = EncodeGraphParser.parse(ast, language);

		State pre = null;
		State post = null;
		if(language.getName().equals("blocky")) {
			pre = loadBlockyState(json.getJSONArray("pre"));
			post = loadBlockyState(json.getJSONArray("post"));
		} else {
			pre = loadKarelState(json.getJSONObject("pre"));
			post = loadKarelState(json.getJSONObject("post"));
		}
		return new TestTriplet(pre, post, graph, astId, count);
	}

	private static String getAstId(JSONObject json) {
		try{
			return json.getString("astId");
		} catch(JSONException e) {
			return json.getInt("astId") + "";
		}
	}


	private static State loadBlockyState(JSONArray jsonArray) {
		int size = jsonArray.length();
		int[] nums = new int[size];
		for(int i = 0; i < size; i++) {
			nums[i] = jsonArray.getInt(i);
		}
		Map<String, String> choiceMap = new HashMap<String, String>();
		choiceMap.put("row", nums[0] +"");
		choiceMap.put("col", nums[1] +"");
		choiceMap.put("direction", nums[2] +"");
		choiceMap.put("status", nums[3] +"");
		Map<String, Integer> numMap = new HashMap<String, Integer>();
		Map<String, SimpleMatrix> matMap = new HashMap<String, SimpleMatrix>();
		return new State(choiceMap, numMap, matMap);
	}

	private static State loadKarelState(JSONObject stateJson) {
		Map<String, String> choiceMap = new HashMap<String, String>();
		choiceMap.put("direction", stateJson.getInt("direction") +"");
		choiceMap.put("status", stateJson.getInt("status") +"");
		
		Map<String, Integer> numMap = new HashMap<String, Integer>();
		numMap.put("row", stateJson.getInt("row"));
		numMap.put("col", stateJson.getInt("col"));
		
		Map<String, SimpleMatrix> matMap = new HashMap<String, SimpleMatrix>();
		JSONObject world = stateJson.getJSONObject("world");
		int rows = world.getInt("numRows");
		int cols = world.getInt("numCols");
	
		
		SimpleMatrix beepers = new SimpleMatrix(rows, cols);
		JSONObject squares = world.getJSONObject("squares");
		
		for(int r = 0; r < rows; r++) {
			for(int c = 0; c < cols; c++) {
				String squareKey = getSquareKey(r, c);
				if(squares.has(squareKey)) {
					JSONObject squareJson = squares.getJSONObject(squareKey);
					beepers.set(r, c, squareJson.getInt("nBeepers"));
				} 
			}
		}
		matMap.put("beepers", beepers);
		
		return new State(choiceMap, numMap, matMap);
	}
	
	private static String getSquareKey(int r, int c) {
		return c + "," + r;
	}
	
}

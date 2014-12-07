package minions.program;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.ejml.simple.SimpleMatrix;
import org.json.JSONArray;
import org.json.JSONObject;

import minions.parser.EncodeGraphParser;
import minions.parser.EncodeTreeParser;
import models.ast.Tree;
import models.code.State;
import models.code.TestTriplet;
import models.encoder.EncodeGraph;
import models.encoder.neurons.TreeNeuron;
import models.language.Language;
import util.FileSystem;
import util.MatrixUtil;
import util.RandomUtil;
import util.Warnings;

public class PrePostExperimentLoader {

	private static Map<String, TreeNeuron> trainPrograms = null;


	public static List<TestTriplet> loadTrainSetSample(int num, Language lang) {
		if(trainPrograms == null) {
			loadTrainPrograms(lang);
		}
		File expDir = FileSystem.getExpDir();
		File trainDir = new File(expDir, "train");
		File csv = new File(trainDir, "prePost.csv");
		return loadSubset(csv, trainPrograms, num, lang);
	}

	public static void loadTrainPrograms(Language lang) {
		File expDir = FileSystem.getExpDir();
		File trainDir = new File(expDir, "train");
		ZipFile zip = null;
		try {
			zip = new ZipFile(new File(trainDir, "encode.zip"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		trainPrograms = EncodeGraphsLoader.loadGraphs(lang, zip);
	}

	public static List<TestTriplet> loadTrainSet(int num, Language lang) {
		throw new RuntimeException("must implement...");
	}

	public static List<TestTriplet> loadTestSet(int num, Language lang) {
		File expDir = FileSystem.getExpDir();
		File testDir = new File(expDir, "test");
		File zipFile = new File(testDir, "encode.zip");
		ZipFile zip;
		try {
			zip = new ZipFile(zipFile);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		Map<String, TreeNeuron> m = EncodeGraphsLoader.loadGraphs(lang, zip);

		File csv = new File(testDir, "prePost.csv");
		return loadSubset(csv, m, num, lang);
	}

	public static List<TestTriplet> loadTriplets(String dirName, int max, Language language) {
		File expDir = FileSystem.getExpDir();
		File testDir = new File(expDir, dirName);
		return loadTests(testDir, max, language);
	}

	public static List<TestTriplet> loadTripletsRandom(String dirName, int numAsts, Language language) {
		File expDir = FileSystem.getExpDir();
		File testDir = new File(expDir, dirName);
		return loadTestsRandom(testDir, numAsts, language);
	}

	public static List<TestTriplet> loadSubset(String dirName, int num, Language l) {
		/*File expDir = FileSystem.getExpDir();
		File testDir = new File(expDir, dirName);
		Map<String, TreeNeuron> graphMap = loadPrograms(testDir, -1, l);

		File csv = new File(testDir, "prePost.csv");
		RandomAccessFile raf = getRandomAccessFile(csv);
		List<TestTriplet> tests = new ArrayList<TestTriplet>();
		while(true) {
			String line = getRandomLine(raf);
			if(line == null) continue;

			TestTriplet test = lineToTest(graphMap, line, l);
			if(test != null) {
				tests.add(test);
				if(tests.size() % 100 == 0) System.out.println(tests.size());
				if(tests.size() == num) return tests;
			}
		}*/
		throw new RuntimeException("need to rethink");
	}

	public static List<TestTriplet> loadSubset(File csv, Map<String, TreeNeuron> m, int num, Language l) {

		RandomAccessFile raf = getRandomAccessFile(csv);
		List<TestTriplet> tests = new ArrayList<TestTriplet>();
		while(true) {
			String line = getRandomLine(raf);
			if(line == null) continue;

			TestTriplet test = lineToTest(m, line, l);
			if(test != null) {
				tests.add(test);
				//if(tests.size() % 100 == 0) System.out.println(tests.size());
				if(tests.size() == num) return tests;
			}
		}
	}

	private static RandomAccessFile getRandomAccessFile(File csv) {
		try {
			return new RandomAccessFile(csv, "r");
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private static String getRandomLine(RandomAccessFile raf) {
		try {
			long length = raf.length();
			raf.seek(RandomUtil.nextLong(length));
			//get start
			while(true) {
				char ch = (char)raf.read();
				if(ch == '\n') { break; }
				if(raf.getFilePointer() >= length) return null;
			}
			// read until newline
			String line = "";
			while(true) {
				char ch = (char)raf.read();
				line += ch;
				if(ch == '\n') { break; }
				if(raf.getFilePointer() >= length) return null;
			}
			return line;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static List<TestTriplet> loadTestsRandom(File testDir, int numAsts, Language language) {
		/*Map<String, EncodeGraph> graphMap = loadPrograms(testDir, -1, language);
		List<String> ids = new ArrayList<String>();
		ids.addAll(graphMap.keySet());
		Collections.shuffle(ids);

		Map<String, EncodeGraph> reduced = new HashMap<String, EncodeGraph>();
		for(int i = 0; i < numAsts; i++) {
			String key = ids.get(i);
			reduced.put(key, graphMap.get(key));
		}

		return getPrePostTriplets(graphMap, testDir, -1, language);*/
		throw new RuntimeException("need to rethink");
	}

	private static List<TestTriplet> loadTests(File testDir, int max, Language language) {
		/*System.out.println(testDir);
		Map<String, TreeNeuron> graphMap = loadPrograms(testDir, max, language);

		return getPrePostTriplets(graphMap, testDir, max, language);*/
		throw new RuntimeException("need to rethink");
	}

	private static List<TestTriplet> getPrePostTriplets(
			Map<String, TreeNeuron> graphMap, File testDir, int max,
			Language language) {
		System.out.println("loading prePost...");
		File f = new File(testDir, "prePost.csv");
		List<String> lines = FileSystem.getFileLines(f, max+1);
		Set<TestTriplet> tests = new HashSet<TestTriplet>();
		//astId,nodeId,preRow,preCol,preDir,preStat,postRow,postCol,postDir,postStat
		int count = 0;
		for(int i = 1; i < lines.size(); i++) {
			String line = lines.get(i);
			TestTriplet test = lineToTest(graphMap, line, language);
			tests.add(test);
			count++;
			if(count % 100 == 0) System.out.println(count);
		}
		System.out.println(tests.size());
		Warnings.error("work in progress");
		return null;//tests;
	}

	private static TestTriplet lineToTest(ZipFile trainZip, String line,
			Language lang) {
		String[] cols = line.split(",");
		String astId = cols[0];
		String nodeId = cols[1];
		TreeNeuron effectiveTree = getEffectiveTree(trainZip, astId, nodeId, lang);
		if(effectiveTree == null) {
			return null;
		}
		return lineToTest(lang, cols, effectiveTree);
	}

	private static TestTriplet lineToTest(Map<String, TreeNeuron> graphMap,
			String line, Language lang) {
		String[] cols = line.split(",");
		String astId = cols[0];
		String nodeId = cols[1];
		TreeNeuron effectiveTree = getEffectiveTree(graphMap, astId, nodeId);
		if(effectiveTree == null) {
			//String params = "astId = " + astId;// + ", nodeId = " + nodeId;
			//Warnings.msg("can't make effective tree for " + params);
			return null;
		}
		return lineToTest(lang, cols, effectiveTree);
	}

	private static TestTriplet lineToTest(Language lang, String[] cols,
			TreeNeuron effectiveTree) {

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

		State pre =  loadState(lang, preList);
		State post = loadState(lang, postList);

		Map<String, TreeNeuron> methodMap = new HashMap<String, TreeNeuron>();
		methodMap.put("run", effectiveTree);
		EncodeGraph graph = new EncodeGraph(methodMap);
		return new TestTriplet(pre, post, graph, astId, 1);
	}

	private static State loadState(Language language, List<String> preList) {
		if(language.isBlocky()) {
			return loadBlockyState(preList);
		}
		if(language.isKarel()) {
			return loadKarelState(preList);
		}
		throw new RuntimeException("wot");
	}

	private static TreeNeuron getEffectiveTree(
			Map<String, TreeNeuron> graphMap, String astId, String nodeId) {
		TreeNeuron t = graphMap.get(astId);
		if(t == null) return null;
		return t.getDescendant(nodeId);//t.getEffectiveTree(nodeId);//
	}

	private static TreeNeuron getEffectiveTree(ZipFile trainZip, String astId,
			String nodeId, Language lang) {
		ZipEntry entry = trainZip.getEntry("encode/" + astId + ".json");
		if(entry == null) return null;
		EncodeGraph g =  EncodeGraphsLoader.loadGraph(lang, trainZip, entry);
		if(g == null || g.hasCycles()) return null;
		return g.getEffectiveTree(nodeId);
	}

	private static Map<String, TreeNeuron> loadPrograms(File testDir, int max,
			Language language) {


		Map<String, TreeNeuron> runTreeMap = new HashMap<String, TreeNeuron>();
		File encodeTreeDir = new File(testDir, "encode");
		int done = 0;
		for(File f : FileSystem.listNumericalFiles(encodeTreeDir)) {

			JSONObject fileJson = FileSystem.getFileJson(f);

			EncodeGraph g = getEncodeGraph(fileJson, language);
			if(g == null || g.hasCycles()) {
				continue;
			}
			String name = FileSystem.getNameWithoutExtension(f.getName());
			TreeNeuron runTree = g.getRunEncodeTreeClone();
			runTreeMap.put(name, runTree);

			done++;
			if(done % 100 == 0) {
				System.out.println("asts loaded: " + done);
			}
			if(max > 0 && done >= max) break;
		}
		return runTreeMap;
	}



	private static EncodeGraph getEncodeGraph(JSONObject fileJson, Language language) {
		if(language.isBlocky()) {
			Map<String, TreeNeuron> methodMap = new HashMap<String, TreeNeuron>();
			JSONObject encodeTreeJson = fileJson.getJSONObject("ast"); 
			TreeNeuron tree = EncodeTreeLoader.parseJson(encodeTreeJson);
			methodMap.put("run", tree);
			return new EncodeGraph(methodMap);
		}
		if(language.isKarel()) {
			Map<String, TreeNeuron> methodMap = new HashMap<String, TreeNeuron>();
			JSONObject encodeTreeJson = fileJson;
			if(encodeTreeJson.toString().equals("{}")) return null;
			for(Object key : encodeTreeJson.keySet()) {
				String name = (String)key;
				JSONObject methodJson = encodeTreeJson.getJSONObject(name);
				TreeNeuron body = EncodeTreeLoader.parseJson(methodJson);
				methodMap.put(name, body);
			}
			return new EncodeGraph(methodMap);
		}
		throw new RuntimeException("unknown language");
	}

	private static State loadBlockyState(List<String> list) {
		Warnings.check(list.size() == 4);
		Map<String, String> choiceMap = new HashMap<String, String>();
		choiceMap.put("row", list.get(0));
		choiceMap.put("col", list.get(1));
		choiceMap.put("direction", list.get(2));
		choiceMap.put("status", list.get(3));
		Map<String, Integer> numMap = new HashMap<String, Integer>();
		Map<String, SimpleMatrix> matMap = new HashMap<String, SimpleMatrix>();
		return new State(choiceMap, numMap, matMap);
	}

	private static State loadKarelState(List<String> list) {
		int worldRows = Integer.parseInt(list.get(4)); 
		int worldCols = Integer.parseInt(list.get(5));
		int worldSize = worldRows * worldCols;
		int expectedSize = 6 + worldRows * worldCols;
		Warnings.check(list.size() == expectedSize);

		Map<String, Integer> numMap = new HashMap<String, Integer>();
		numMap.put("row", Integer.parseInt(list.get(0)));
		numMap.put("col", Integer.parseInt(list.get(1)));
		numMap.put("worldRows", worldRows);
		numMap.put("worldCols", worldCols);

		Map<String, String> choiceMap = new HashMap<String, String>();
		choiceMap.put("status", list.get(2));
		choiceMap.put("direction", list.get(3));

		Map<String, SimpleMatrix> matMap = new HashMap<String, SimpleMatrix>();
		List<String> beeperList = list.subList(6, list.size());
		Warnings.check(beeperList.size() == worldSize);
		double[] beeperVector = new double[worldCols * worldRows];
		for(int i = 0; i < beeperList.size(); i++) {
			beeperVector[i] = Double.parseDouble(beeperList.get(i));
		}
		Warnings.check(worldRows <= 7);
		Warnings.check(worldCols <= 7);
		SimpleMatrix beeperMap = new SimpleMatrix(7, 7);
		SimpleMatrix beeperMapSmall = MatrixUtil.asSimpleMatrix(beeperVector);
		beeperMapSmall.reshape(worldRows, worldCols);
		for(int r = 0; r < beeperMapSmall.numRows(); r++) {
			for(int c = 0; c < beeperMapSmall.numCols(); c++) {
				beeperMap.set(r, c, beeperMapSmall.get(r, c));
			}
		}
		// this is optional. We could keep the matrix in vector form...
		
		matMap.put("beepers", beeperMap);
		return new State(choiceMap, numMap, matMap);
	}







}

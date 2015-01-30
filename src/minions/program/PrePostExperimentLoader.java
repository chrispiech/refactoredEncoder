package minions.program;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import models.code.GeneralState;
import models.code.KarelState;
import models.code.State;
import models.code.TestTriplet;
import models.encoder.EncodeGraph;
import models.encoder.EncoderParams;
import models.encoder.neurons.TreeNeuron;
import models.language.Language;

import org.ejml.simple.SimpleMatrix;
import org.json.JSONObject;

import util.FileSystem;
import util.MatrixUtil;
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
		trainPrograms = EncodeGraphsLoader.loadRunTreeClones(lang, zip);
	}

	public static List<TestTriplet> loadTrainSet(int num, Language lang) {
		throw new RuntimeException("must implement...");
	}

	public static List<TestTriplet> loadTrainSet(Language lang) {
		return loadTrainSet(lang, -1);
	}
	
	public static List<TestTriplet> loadTrainSet(Language lang, int i) {
		File expDir = FileSystem.getExpDir();
		File trainDir = new File(expDir, "train");
		ZipFile zip = null;
		try {
			zip = new ZipFile(new File(trainDir, "encode.zip"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		Map<String, EncodeGraph> map =  EncodeGraphsLoader.loadGraphs(lang, zip);

		return getPrePostTriplets(map, trainDir, i, lang);
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
		Map<String, TreeNeuron> m = EncodeGraphsLoader.loadRunTreeClones(lang, zip);

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
			/*String line = getRandomLine(raf);
			if(line == null) continue;

			TestTriplet test = lineToTest(m, line, l);
			if(test != null) {
				tests.add(test);
				//if(tests.size() % 100 == 0) System.out.println(tests.size());
				if(tests.size() == num) return tests;
			}*/
			throw new RuntimeException("depricated");
		}
	}

	private static RandomAccessFile getRandomAccessFile(File csv) {
		try {
			return new RandomAccessFile(csv, "r");
		} catch (FileNotFoundException e) {
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
		System.out.println(testDir);
		Map<String, TreeNeuron> graphMap = loadPrograms(testDir, max, language);

		return getPrePostTripletsDepricated(graphMap, testDir, max, language);
	}

	private static List<TestTriplet> getPrePostTriplets(
			Map<String, EncodeGraph> map, File dir, int max, Language lang) {
		List<TestTriplet> tests = new ArrayList<TestTriplet>();
		int done = 0;
		try {
			File tripsFile = new File(dir, "prePost.csv");
			Scanner sc = new Scanner(tripsFile); 
			while(sc.hasNextLine()) {
				String line = sc.nextLine();
				TestTriplet test = lineToTest(map, line, lang);
				if(test != null) {
					tests.add(test);
				}
				if(++done % 1000 == 0) System.out.println(done);
				if(max > 0 && done > max) break;
			}
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
		return tests;
	}

	private static List<TestTriplet> getPrePostTripletsDepricated(
			Map<String, TreeNeuron> graphMap, File testDir, int max,
			Language language) {
		/*System.out.println("loading prePost...");
		File f = new File(testDir, "prePost.csv");
		List<String> lines = null;
		if(max < 0) {
			lines = FileSystem.getFileLines(f);
		} else {
			lines = FileSystem.getFileLines(f, max+1);
		}

		Set<TestTriplet> tests = new HashSet<TestTriplet>();
		//astId,nodeId,preRow,preCol,preDir,preStat,postRow,postCol,postDir,postStat
		int count = 0;
		for(int i = 1; i < lines.size(); i++) {
			String line = lines.get(i);
			TestTriplet test = lineToTest(graphMap, line, language);
			if(test == null) continue;

			tests.add(test);
			count++;
			if(count % 100 == 0) System.out.println(count);
		}
		return new ArrayList<TestTriplet>(tests);*/
		throw new RuntimeException("depricated");
	}

	private static TestTriplet lineToTest(ZipFile trainZip, String line,
			Language lang) {
		/*String[] cols = line.split(",");
		String astId = cols[0];
		String nodeId = cols[1];
		TreeNeuron effectiveTree = getEffectiveTree(trainZip, astId, nodeId, lang);
		if(effectiveTree == null) {
			return null;
		}
		return lineToTest(lang, cols, effectiveTree);*/
		throw new RuntimeException("depricated");
	}

	private static TestTriplet lineToTest(Map<String, EncodeGraph> graphMap,
			String line, Language lang) {
		String[] cols = line.split(",");
		String astId = cols[1];
		if(!graphMap.containsKey(astId)) {
			return null;
		}
		EncodeGraph graph = graphMap.get(astId);
		if(graph == null) return null;
		String nodeId = cols[2];
		return lineToTest(lang, cols, graph, nodeId);
	}

	public static TestTriplet lineToTest(Language lang, String[] cols,
			EncodeGraph graph, String nodeId) {

		int count = Integer.parseInt(cols[0]);
		String astId = cols[1];
		int stateSize = (cols.length - 3) / 2;

		int start1 = 3;
		int start2 = 3 + stateSize;
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

		
		return new TestTriplet(pre, post, graph, astId, nodeId, count);
	}

	public static State loadState(Language language, List<String> preList) {
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
		return new GeneralState(choiceMap, numMap, matMap);
	}

	public static KarelState loadKarelState(List<String> list) {
		int worldRows = Integer.parseInt(list.get(4)); 
		int worldCols = Integer.parseInt(list.get(5));
		int worldSize = worldRows * worldCols;
		int expectedSize = 6 + worldRows * worldCols;
		Warnings.check(list.size() == expectedSize);

		Map<String, Integer> numMap = new HashMap<String, Integer>();


		numMap.put("worldRows", worldRows);
		numMap.put("worldCols", worldCols);

		Map<String, String> choiceMap = new HashMap<String, String>();
		choiceMap.put("status", list.get(2));
		choiceMap.put("direction", list.get(3));
		choiceMap.put("col", list.get(1));
		choiceMap.put("row", list.get(0));
		
		int row = Integer.parseInt(list.get(0));
		int col = Integer.parseInt(list.get(1));
		int status = Integer.parseInt(list.get(2));
		int dir = Integer.parseInt(list.get(3));

		List<String> beeperList = list.subList(6, list.size());
		Warnings.check(beeperList.size() == worldSize);
		double[] beeperVector = new double[worldCols * worldRows];
		for(int i = 0; i < beeperList.size(); i++) {
			beeperVector[i] = Double.parseDouble(beeperList.get(i));
		}

		SimpleMatrix beepers = new SimpleMatrix(EncoderParams.worldRows, EncoderParams.worldCols);
		SimpleMatrix beeperMapSmall = MatrixUtil.asSimpleMatrix(beeperVector);
		beeperMapSmall.reshape(worldRows, worldCols);
		for(int r = 0; r < beeperMapSmall.numRows(); r++) {
			for(int c = 0; c < beeperMapSmall.numCols(); c++) {
				beepers.set(r, c, beeperMapSmall.get(r, c));
			}
		}
		// this is optional. We could keep the matrix in vector form...

		return new KarelState(row, col,dir, status, worldRows, worldCols, beepers);
	}

	







}

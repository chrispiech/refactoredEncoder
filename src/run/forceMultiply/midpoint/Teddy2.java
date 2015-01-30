package run.forceMultiply.midpoint;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.zip.ZipFile;

import minions.encoder.EncoderSaver;
import minions.program.EncodeGraphsLoader;
import minions.program.PrePostExperimentLoader;
import models.code.TestTriplet;
import models.encoder.EncodeGraph;
import models.encoder.EncoderParams;
import models.encoder.encoders.models.StateEncodable;
import models.encoder.neurons.TreeNeuron;
import models.language.KarelLanguage;
import models.language.KarelLanguage2;

import org.apache.commons.math3.util.Pair;
import org.ejml.simple.SimpleMatrix;

import util.FileSystem;
import util.MatrixUtil;

public class Teddy2 {

	private static double LAMBDA = 0.9; 

	private Map<String, EncodeGraph> asts = 
			new HashMap<String, EncodeGraph>();

	public static void main(String[] args) {
		new Teddy2().run();
	}

	private void run() {
		FileSystem.setAssnId("Midpoint");
		System.out.println("loading model...");
		FileSystem.setExpId("runExp");
		StateEncodable model = (StateEncodable) EncoderSaver.load("yellow-1426057_5/model");

		System.out.println("load asts...");
		collectEncodeTrees();

		//FileSystem.setExpId("feedbackExp");
		//Map<String, List<Integer>> feedbackMap = ForceMultUtil.loadFeedbackZip();

		// we need to load the pre-post conditions...
		System.out.println("make tree map...");
		Map<TreeNeuron, List<TestTriplet>> runMap = loadRunMap();

		// then calculate the encoding maps
		System.out.println("compute tree encodings...");
		Map<TreeNeuron, SimpleMatrix> encodingMap = getEncodingMap(model, runMap);
		saveEncodingMap(encodingMap);

		System.out.println("num trees: " + runMap.size());
		System.out.println("lambda: " + LAMBDA);
		System.out.println("got here!");
		// then run force multiplication! 
		/*Set<String> toGrade = asts.keySet();
		FMMinion minion = new FMEncoderTeddy(encodingMap, 0);
		ForceMultiplier force = new ForceMultiplier(minion, feedbackMap, toGrade);
		force.run(500);*/
	}

	private void saveEncodingMap(Map<TreeNeuron, SimpleMatrix> encodingMap) {
		try {
			FileSystem.setAssnId("Results");
			File root = new File( FileSystem.getAssnDir(), "teddy2.ser");
			FileOutputStream fileOut = new FileOutputStream(root);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(encodingMap);
			out.close();
			fileOut.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private void collectEncodeTrees() {
		File expDir = new File(FileSystem.getAssnDir(), "prePostExp");
		File trainDir = new File(expDir, "train");
		File testDir = new File(expDir, "test");
		collectEncodeTreesFromDir(trainDir);
		collectEncodeTreesFromDir(testDir);
		System.out.println(asts.size());
	}

	private void collectEncodeTreesFromDir(File dir) {

		ZipFile zip = null;
		try {
			zip = new ZipFile(new File(dir, "encode.zip"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		Map<String, EncodeGraph> map =  EncodeGraphsLoader.loadGraphs(new KarelLanguage(), zip);
		for(String key : map.keySet()) {
			if(map.get(key) != null) {
				asts.put(key, map.get(key));
			}
		}
	}

	private Map<TreeNeuron, SimpleMatrix> getEncodingMap(
			StateEncodable model, 
			Map<TreeNeuron, List<TestTriplet>> runMap) {
		Map<TreeNeuron, SimpleMatrix> encodingMap = new HashMap<TreeNeuron, SimpleMatrix>();
		int done = 0;
		for(TreeNeuron tree : runMap.keySet()) {
			SimpleMatrix bestFit = getBestFit(model, runMap.get(tree));
			encodingMap.put(tree, bestFit);
			if(++done % 100 == 0) System.out.println(done);
		}
		return encodingMap;
	}

	private SimpleMatrix getBestFit(StateEncodable model, List<TestTriplet> list) {
		List<Pair<SimpleMatrix, SimpleMatrix>> pairs = getPrePost(model, list);
		int m = EncoderParams.getM();
		SimpleMatrix X = makeX(pairs);
		SimpleMatrix P = new SimpleMatrix(m, m);
		for(int i = 0; i < m; i++) {
			SimpleMatrix y = makeY(pairs, i);
			SimpleMatrix theta = fit(X, y);
			MatrixUtil.setRow(P, i, theta);
		}

		P.reshape(m * m, 1);

		return P;
	}

	private SimpleMatrix fit(SimpleMatrix X, SimpleMatrix y) {
		int m = EncoderParams.getM();
		SimpleMatrix I = SimpleMatrix.identity(m);
		SimpleMatrix term1 = (X.transpose().mult(X).plus(I.scale(LAMBDA)));
		return term1.invert().mult(X.transpose().mult(y));

	}

	private SimpleMatrix makeX(List<Pair<SimpleMatrix, SimpleMatrix>> pairs) {
		int m = EncoderParams.getM();
		SimpleMatrix X = new SimpleMatrix(pairs.size(), m);
		for(int i = 0; i < pairs.size(); i++) {
			Pair<SimpleMatrix, SimpleMatrix> prePost = pairs.get(i);
			SimpleMatrix pre = prePost.getFirst();
			for(int j = 0; j < m; j++) {
				X.set(i, j, pre.get(j));
			}
		}
		return X;
	}

	private SimpleMatrix makeY(List<Pair<SimpleMatrix, SimpleMatrix>> pairs, int i) {
		SimpleMatrix y = new SimpleMatrix(pairs.size(), 1);
		for(int j = 0; j < pairs.size(); j++) {
			double v = pairs.get(j).getSecond().get(i);
			y.set(j, v);
		}
		return y;
	}

	private List<Pair<SimpleMatrix, SimpleMatrix>> getPrePost(StateEncodable model, 
			List<TestTriplet> list) {
		List<Pair<SimpleMatrix, SimpleMatrix>> pairs = new
				ArrayList<Pair<SimpleMatrix, SimpleMatrix>>();
		for(TestTriplet t : list) {
			SimpleMatrix pre = model.getVector(t.getPrecondition());
			SimpleMatrix post = model.getVector(t.getPostcondition());
			pairs.add(new Pair<SimpleMatrix, SimpleMatrix>(pre,post));
		}
		return pairs;
	}

	private Map<TreeNeuron, List<TestTriplet>> loadRunMap() {
		FileSystem.setExpId("teddyExp");
		int done = 0;
		File prePostCsv = new File(FileSystem.getExpDir(), "prePost.csv");
		Map<TreeNeuron, List<TestTriplet>> runMap = 
				new HashMap<TreeNeuron, List<TestTriplet>>();
		try {
			Scanner sc = new Scanner(prePostCsv);
			while(sc.hasNextLine()) {
				String line = sc.nextLine();
				TestTriplet t = lineToTest(line);
				if(t == null) continue;
				TreeNeuron tree = t.getEncodeGraph().getEffectiveTree(t.getNodeId());
				if(!runMap.containsKey(tree)) {
					runMap.put(tree, new ArrayList<TestTriplet>());
				}
				runMap.get(tree).add(t);
				if(++done % 100 == 0) System.out.println(done);
			}
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}

		return runMap;
	}

	private TestTriplet lineToTest(String line) {
		String[] cols = line.split(",");
		String astId = cols[1];
		String nodeId = cols[2];
		EncodeGraph g = asts.get(astId);
		if(g == null) return null;
		return PrePostExperimentLoader.lineToTest(
				new KarelLanguage2(), cols,
				g, nodeId);
	}
}

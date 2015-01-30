package run.explore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.zip.ZipFile;

import minions.program.EncodeGraphsLoader;
import minions.program.PrePostExperimentLoader;
import models.code.State;
import models.code.TestTriplet;
import models.encoder.EncodeGraph;
import models.encoder.neurons.TreeNeuron;
import models.language.KarelLanguage;
import models.language.Language;
import util.FileSystem;
import util.Histogram;
import util.MapSorter;
import util.Warnings;

public class CollectAstPrePost {
	private static final int MAX_TRIPLES = 30;

	public static void main(String[] args) {
		new CollectAstPrePost().run();
	}

	private Map<String, EncodeGraph> asts = 
			new HashMap<String, EncodeGraph>();
	
	private Map<SimpleTree, List<SimpleTripplet>> trips = 
			new HashMap<SimpleTree, List<SimpleTripplet>>();

	class SimpleTree {
		String astId;
		String nodeId;
		TreeNeuron ast;

		@Override
		public boolean equals(Object o) {
			SimpleTree other = (SimpleTree)o;
			return ast.equals(other.ast);
		}

		@Override
		public int hashCode(){
			return ast.hashCode();
		}
	}
	
	class SimpleTripplet {
		SimpleTree ast;
		String prePost;
	}

	private void run() {
		FileSystem.setAssnId("Midpoint");
		FileSystem.setExpId("prePostExp");
		File assnDir = FileSystem.getAssnDir();
		
		collectEncodeTrees();

		File test = new File(assnDir, "prePostExp2/test/prePost.csv");
		File train = new File(assnDir, "prePostExp2/train/prePost.csv");
		File run = new File(assnDir, "runExp/prePost.csv");
		loadTriplesFromFile(test); 
		loadTriplesFromFile(train);
		loadTriplesFromRunFile(run);
		System.out.println("\nchosen states: " + trips.size());

		FileSystem.setAssnId("Results");
		File results = FileSystem.getAssnDir();
		File states = new File(results, "triples.csv");

		try {
			FileWriter writer = new FileWriter(states);
			for(SimpleTree tree : trips.keySet()) {
				for(SimpleTripplet trip : trips.get(tree)) {
					writer.write(getLine(tree, trip) + "\n");
				}
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}	

	}

	private String getLine(SimpleTree tree, SimpleTripplet trip) {
		return tree.astId + "," + tree.nodeId + "," + trip.prePost;
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

	private void loadTriplesFromFile(File csv) {
		String currentAst = "0";
		List<String> programStates = new ArrayList<String>();
		int done = 0;
		try {
			Scanner codeIn = new Scanner(csv);
			while (codeIn.hasNextLine()) {
				String line = codeIn.nextLine();
				String[] cols = line.split(",");
				if(cols.length < 1) continue;
				String ast = cols[0];
				if(!ast.equals(currentAst)) {
					processAst(programStates);
					programStates = new LinkedList<String>();
					currentAst = ast;
				}
				programStates.add(line);
				if(++done %100 == 0) System.out.println("DONE: " + done);
			}
			codeIn.close();
		} catch(FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void loadTriplesFromRunFile(File csv) {
		int done = 0;
		try {
			Scanner codeIn = new Scanner(csv);
			while (codeIn.hasNextLine()) {
				String line = codeIn.nextLine();
				String[] cols = line.split(",");
				if(cols.length < 1) continue;
				addToCollectionFromRun(line);
				if(++done %100 == 0) System.out.println("DONE: " + done);
			}
			codeIn.close();
		} catch(FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private void processAst(List<String> programStates) {
		Collections.shuffle(programStates);
		for(int i = 0; i < Math.min(programStates.size(), MAX_TRIPLES); i++) {
			addToCollection(programStates.get(i));
		}	
	}
	
	private void addToCollectionFromRun(String line) {
		String[] cols = line.split(",");
		String astId = cols[0];
		EncodeGraph ast = asts.get(astId);
		if(ast == null) return;
		String nodeId = ast.getRunNodeId();
		if(nodeId == null) return;
		cols[1] = nodeId;
		String linePrime = "";
		for(int i = 0; i < cols.length; i++) {
			linePrime += cols[i];
			if(i != cols.length - 1) {
				linePrime += ",";
			}
		}
		addToCollection(linePrime);
	}

	private void addToCollection(String line) {
		String[] cols = line.split(",");
		String astId = cols[0];
		String nodeId = cols[1];

		EncodeGraph ast = asts.get(astId);
		if(ast == null) return;
		TreeNeuron node = ast.getEffectiveTree(nodeId);
		if(node == null) return;

		List<String> prePostList = new ArrayList<String>();
		for(int j = 2; j < cols.length; j++) {
			prePostList.add(cols[j]);
		}
		String prePost = listToString(prePostList);
		
		SimpleTree tree = new SimpleTree();
		tree.astId = cols[0];
		tree.nodeId = cols[1];
		tree.ast = node;
		
		SimpleTripplet trip = new SimpleTripplet();
		trip.ast = tree;
		trip.prePost = prePost;
		
		if(!trips.containsKey(tree)) {
			trips.put(tree, new ArrayList<SimpleTripplet>());
		}
		trips.get(tree).add(trip);
	}

	private String listToString(List<String> state) {
		String str = "";
		for(String s : state) {
			str += s + ",";
		}
		return str;
	}
}

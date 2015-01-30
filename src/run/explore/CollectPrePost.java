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

public class CollectPrePost {
	private static final int MAX_TRIPLES = 200;

	public static void main(String[] args) {
		new CollectPrePost().run();
	}

	private Map<String, EncodeGraph> asts = 
			new HashMap<String, EncodeGraph>();

	private Map<SimpleTriplet, Integer> triples = 
			new HashMap<SimpleTriplet, Integer>();

	class SimpleTriplet {
		String line;
		TreeNeuron ast;
		String precondition;

		@Override
		public boolean equals(Object o) {
			SimpleTriplet other = (SimpleTriplet)o;
			if(!precondition.equals(other.precondition)) {
				return false;
			}
			if(!ast.equals(other.ast)) {
				return false;
			}
			return true;
		}

		@Override
		public int hashCode(){
			List<Object> objList = new ArrayList<Object>();
			objList.add(ast);
			objList.add(precondition);
			return objList.hashCode();
		}
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
		System.out.println("\nchosen states: " + triples.size());

		FileSystem.setAssnId("Results");
		File results = FileSystem.getAssnDir();
		File states = new File(results, "triples.csv");

		try {
			FileWriter writer = new FileWriter(states);
			for(SimpleTriplet s : triples.keySet()) {
				writer.write(triples.get(s) + "," + s.line + "\n");
			}
			writer.close();
		} catch (IOException e) {
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

		int stateSize = (cols.length - 2) / 2;

		List<String> preList = new ArrayList<String>();
		for(int j = 2; j < stateSize + 2; j++) {
			preList.add(cols[j]);
		}

		EncodeGraph ast = asts.get(astId);
		if(ast == null) return;
		TreeNeuron node = ast.getEffectiveTree(nodeId);
		if(node == null) return;

		String pre = listToString(preList);
		SimpleTriplet trip = new SimpleTriplet();
		trip.ast = node;
		trip.precondition = pre;
		trip.line = line;

		if(!triples.containsKey(trip)) {
			triples.put(trip, 0);
		}
		triples.put(trip, triples.get(trip) + 1);
	}

	private String listToString(List<String> state) {
		String str = "";
		for(String s : state) {
			str += s + ",";
		}
		return str;
	}
}

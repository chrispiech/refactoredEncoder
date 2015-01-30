package run.explore;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import minions.program.PrePostExperimentLoader;
import models.code.State;
import util.FileSystem;
import util.Histogram;
import util.MapSorter;

public class CollectStates {
	private static final int MAX_STATES = 30;

	public static void main(String[] args) {
		new CollectStates().run();
	}
	
	private Map<String, Integer> stateCounts = 
			new HashMap<String, Integer>();
	private Histogram histogram = new Histogram(0, 20, 1);

	private void run() {
		FileSystem.setAssnId("Midpoint");
		FileSystem.setExpId("prePostExp");
		File assnDir = FileSystem.getAssnDir();
		File test = new File(assnDir, "prePostExp2/test/prePost.csv");
		File train = new File(assnDir, "prePostExp2/train/prePost.csv");
		File run = new File(assnDir, "runExp/prePost.csv");
		loadStatesFromFile(test); 
		loadStatesFromFile(train);
		loadStatesFromFile(run);
		System.out.println("states histogram:");
		System.out.println(histogram);
		System.out.println("\nchosen states: " + stateCounts.size());
		
		FileSystem.setAssnId("Results");
		File results = FileSystem.getAssnDir();
		FileSystem.createFile(results, "hist.csv", histogram.toString());
		File states = new File(results, "states30_2.csv");
		
		try {
			FileWriter writer = new FileWriter(states);
			for(String s : stateCounts.keySet()) {
				writer.write(stateCounts.get(s) + "," + s + "\n");
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}

	private void loadStatesFromFile(File csv) {
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
					if(!programStates.isEmpty()) {
						double z = Math.log(programStates.size());
						histogram.addPoint(z / Math.log(2));
					}
					programStates = new LinkedList<String>();
					currentAst = ast;
				}
				programStates.add(line);
				if(++done %100 == 0) System.out.println("DONE: " + done);
			}
			codeIn.close();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void processAst(List<String> programStates) {
		Collections.shuffle(programStates);
		for(int i = 0; i < Math.min(programStates.size(), MAX_STATES); i++) {
			addToCollection(programStates.get(i));
		}	
	}

	private void addToCollection(String line) {
		String[] cols = line.split(",");
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

		String stateStr = listToString(postList);
		if(!stateCounts.containsKey(stateStr)) {
			stateCounts.put(stateStr,0);
		}
		stateCounts.put(stateStr, stateCounts.get(stateStr) + 1);
		
	}
	
	private String listToString(List<String> state) {
		String str = "";
		for(String s : state) {
			str += s + ",";
		}
		return str;
	}
}

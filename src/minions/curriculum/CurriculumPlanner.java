package minions.curriculum;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.Scanner;
import java.util.TreeMap;

import util.FileSystem;
import minions.program.PostExperimentLoader;
import minions.program.TreeLoader;
import models.ast.Tree;
import models.code.TestTriplet;
import models.encoder.ModelFormat;

public class CurriculumPlanner {
	
	//!!IMPORTANT -> only work for *BLOCKY* model now. 
	
	private static final String LANGUAGE = "blocky";
	private static final String MODEL_TYPE = "bear";
	
	private static final int NUM_TRAIN = -1;
	
	List<TestTriplet> trainSet = null;
	ModelFormat format = null;
	
	TreeMap<Integer, Tree> treeMap; 
	HashMap<Integer, List<Integer>> complexity = new HashMap<Integer, List<Integer>>(); 
	
	//*-------------------------------SOME HELPER FUNCTION-------------------------------*//
	
	private void loadTrainSet() {
		FileSystem.setAssnId("Hoc18");
		FileSystem.setExpId("postExp");
		format = new ModelFormat(LANGUAGE, MODEL_TYPE);
		trainSet = PostExperimentLoader.loadTests("train", NUM_TRAIN, format.getLanguage());
	}
	
	private void loadTreeMap() {
		FileSystem.setAssnId("Hoc18");
		FileSystem.setExpId("dropoutExp");
		File dataDir = new File(FileSystem.getDataDir(), FileSystem.getAssnId());
		File expDir = new File(dataDir, FileSystem.getExpId());
		File astsDir = new File(expDir, "asts");
		treeMap = TreeLoader.loadTrees(astsDir, -1);
	}
	
	private void saveOrder(List<Integer> order, String name) {
		String str = "";
		for (int val : order) {
			str += val + " ";
		}
		try (PrintStream out = new PrintStream(new FileOutputStream(name))) {
		    out.print(str);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private List<Integer> loadOrder(String name) {
		List<Integer> order = new ArrayList<Integer>();
		Scanner read = null;
		try {
			read = new Scanner(new File(name));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		read.useDelimiter(" ");
		while(read.hasNext()) {
			order.add(Integer.parseInt(read.next()));
		}
		read.close();
		return order;
	}
	
	//*-------------------------------CURRICULUM PLANNER-------------------------------*//
	
	//ORDER: Get order of ast from low complexity to high complexity and save the order to text file.
	private void getNaiveOrder() {
		saveOrder(createNaiveOrder(), "naiveOrder.txt");
	}
	
	private List<Integer> createNaiveOrder() {
		loadTreeMap();
		for (int id = 0; id < treeMap.size(); id++) {
			Tree tree = treeMap.get(id);
			int size = tree.size()-1;
			if (complexity.containsKey(size)) {
				List<Integer> idList = complexity.get(size);
				idList.add(id);
				complexity.put(size, idList);
			} else {
				List<Integer> idList = new ArrayList<Integer>();
				idList.add(id);
				complexity.put(size, idList);
			}
		}
		
		List<Integer> naiveOrder = new ArrayList<Integer>();
		for (int key : complexity.keySet()) {
			for (int val : complexity.get(key)) {
				naiveOrder.add(val);
			}
		}
		
		return naiveOrder; 	
	}
	
	//CURRICULUM: Load order and create curriculum. Curriculum is just order of trainSet that 
	//we will train on.
	private void getNaiveCurriculum() {
		saveOrder(createNaiveCurriculum(), "naiveCurriculum.txt");
	}
	
	private List<Integer> createNaiveCurriculum() {
		List<Integer> order = loadOrder("naiveOrder.txt");
		loadTrainSet();
		
		//Get trainSet astId list
		List<Integer> astIdList = new ArrayList<Integer>();
		int size = trainSet.size();
		for (int i = 0; i < size; i++) {
			astIdList.add(Integer.parseInt(trainSet.get(i).getId()));
		}
		
		//Get index of trainSet according to curriculum order
		List<Integer> indexTrack = new ArrayList<Integer>();
		for (int id : order) {
			ListIterator<Integer> iter = astIdList.listIterator();
			while (iter.hasNext()) {
				int astId = iter.next();
				if (astId == id) {
					indexTrack.add(iter.nextIndex()-1);
					break;
				} else if (astId > id) {
					break;
				}
			}
		}
		
		return indexTrack;
	}
	
	//*-------------------------------PUBLIC CALL for CURRICULUM-------------------------------*//
	//Usage: testSet = CurriculumPlanner.loadNaiveCurriculum();
	
	//Naive
	public List<TestTriplet> loadNaiveCurriculum() {
		loadTrainSet();
		List<TestTriplet> curriculum = new ArrayList<TestTriplet>();
		List<Integer> order = loadOrder("naiveCurriculum.txt");
		for (int index : order){
			curriculum.add(trainSet.get(index));
		}
		return curriculum;
	}
	
	//Mixed
	public List<TestTriplet> loadMixedCurriculum() {
		loadTrainSet();
		long seed = System.nanoTime();
		Collections.shuffle(trainSet, new Random(seed));
		return trainSet;
	}
	
	//Combined: simply pick ratio of mixed curriculum wanted 
	//We will randomly swap element in the naive curriculum according to the ratio
	public List<TestTriplet> loadCombinedCurriculum(double mixedRatio) {
		List<TestTriplet> naive = loadNaiveCurriculum();
		int size = naive.size();
		int numSwap = (int) mixedRatio*size/2;
		
		List<Integer> index = new ArrayList<Integer>(); 
		for (int i = 0; i < size; i++) {
			index.add(i);
		}
		
		Collections.shuffle(index);
		int end = size - 1;
		for (int count = 0; count < numSwap; count++) {
			Collections.swap(naive, index.get(count), index.get(end-count));
		}
		
		return naive;
	}
	
	//*------------------------------- MAIN -------------------------------*//
	//Run to get the pre-computed naive curriculum.
	
	public static void main(String[] args) {
		new CurriculumPlanner().getNaiveOrder();
		new CurriculumPlanner().getNaiveCurriculum();
	}
	
}

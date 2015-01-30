package run.sandbox;

import java.io.File;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import minions.encoder.EncoderSaver;
import minions.forceMult.FMEncoderActive;
import minions.forceMult.FMEncoderRandom;
import minions.forceMult.FMMinion;
import minions.forceMult.FMRandomChoser;
import minions.forceMult.ForceMultiplier;
import minions.parser.EncodeGraphParser;
import minions.program.EncodeGraphsLoader;
import minions.program.PostExperimentLoader;
import models.ast.Tree;
import models.code.TestTriplet;
import models.encoder.ClusterableMatrix;
import models.encoder.EncodeGraph;
import models.encoder.EncoderParams;
import models.encoder.encoders.Encoder;
import models.encoder.neurons.TreeNeuron;
import models.language.KarelLanguage;
import models.language.Language;

import org.ejml.simple.SimpleMatrix;
import org.json.JSONArray;
import org.json.JSONObject;

import util.FileSystem;
import util.MapSorter;
import util.RandomUtil;

public class ManyModelTest {

	private static final int BUDGET = 500;
	
	private void run() {
		System.out.println("finds the relationship between model size and accuracy");
		FileSystem.setAssnId("Midpoint");
		File prePostDir = new File(FileSystem.getAssnDir(), "prePostExp");
		File savedModels = new File(prePostDir, "savedModels");
		
		Map<String, Double> scoreMap = new HashMap<String, Double>();
		
		
		for(File f : FileSystem.listFiles(savedModels)) {
			if(f.isDirectory()) {
				String baseName = f.getName();
				
				// check its score and add to map
				File log = new File(f, "log.txt");
				double best = getBest(log);
				int maxIndex = getMaxIndex(log);
				//System.out.println(best);
				scoreMap.put(baseName, best);
				
				// find its last model
				File bestModel = new File(f, baseName + "-epoch" + maxIndex);
				int stateVectorSize = getStateVectorSize(bestModel);
				System.out.println(stateVectorSize + "\t" + best);
			}
		}
		/*MapSorter<String> s = new MapSorter<String>();
		for(String name : s.sortDouble(scoreMap)) {
			System.out.println(name + "\t" + scoreMap.get(name));
		}*/
	}

	
	private int getStateVectorSize(File bestModel) {
		File notes = new File(bestModel, "notes.txt");
		Map<String, String> map = FileSystem.getFileMapString(notes, ":");
		return Integer.parseInt(map.get("stateVec size"));
	}


	private int getMaxIndex(File log) {
		List<String> lines = FileSystem.getFileLines(log);
		String lastLine = lines.get(lines.size() - 1);
		String[] cols = lastLine.split(",");
		return Integer.parseInt(cols[0]);
	}


	private double getBest(File log) {
		List<String> lines = FileSystem.getFileLines(log);
		String lastLine = lines.get(lines.size() - 1);
		String[] cols = lastLine.split(",");
		return Double.parseDouble(cols[2]);
	}


	public static void main(String[] args) {
		new ManyModelTest().run();
	}
}

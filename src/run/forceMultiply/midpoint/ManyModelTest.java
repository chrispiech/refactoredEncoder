package run.forceMultiply.midpoint;

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

import run.forceMultiply.ForceMultUtil;
import util.FileSystem;
import util.MapSorter;
import util.RandomUtil;

public class ManyModelTest {

	private static final int BUDGET = 500;
	
	private void run() {
		System.out.println("finds the relationship between model size and forcemult");
		FileSystem.setAssnId("Midpoint");
		File prePostDir = new File(FileSystem.getAssnDir(), "prePostExp");
		File savedModels = new File(prePostDir, "savedModels");
		
		FileSystem.setExpId("feedbackExp");
		Map<String, List<Integer>> feedbackMap = ForceMultUtil.loadFeedbackZip();
		
		FileSystem.setExpId("prePostExp");
		Map<String, TreeNeuron> programMap = ForceMultUtil.loadProgramsZip();
		
		Map<String, Double> propMap = new HashMap<String, Double>();
		
		for(File f : FileSystem.listFiles(savedModels)) {
			if(f.isDirectory()) {
				String baseName = f.getName();
				
				// check its force mult curve
				File log = new File(f, "log.txt");
				int maxIndex = getMaxIndex(log);
				
				
				// find its last model
				File bestModel = new File(f, baseName + "-epoch" + maxIndex);
				Encoder model = EncoderSaver.load(bestModel);
				TreeMap<String, SimpleMatrix> encodingMap = ForceMultUtil.makeEncodingMap(programMap, model);
				Set<String> toGrade = encodingMap.keySet();
				
				FMMinion minion = new FMEncoderRandom(encodingMap, 0);
				ForceMultiplier force = new ForceMultiplier(minion, feedbackMap, toGrade);
				force.run(BUDGET);
				double numProp = force.getNumProp();
				propMap.put(baseName, numProp);
				
				System.out.println(baseName + ": " + numProp);
				
			}
		}
		
		MapSorter<String> s = new MapSorter<String>();
		for(String name : s.sortDouble(propMap)) {
			System.out.println(name + "\t" + propMap.get(name));
		}
	}
	

	private int getMaxIndex(File log) {
		List<String> lines = FileSystem.getFileLines(log);
		String lastLine = lines.get(lines.size() - 1);
		String[] cols = lastLine.split(",");
		return Integer.parseInt(cols[0]);
	}

	public static void main(String[] args) {
		new ManyModelTest().run();
	}
}

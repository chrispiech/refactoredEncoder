package run.forceMultiply.midpoint;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.zip.ZipFile;

import minions.encoder.EncoderSaver;
import minions.encoder.modelVector.ModelVector;
import minions.forceMult.FMEncoderRandom;
import minions.forceMult.FMMinion;
import minions.forceMult.ForceMultiplier;
import minions.minimizer.teddy.TeddyDimension;
import minions.minimizer.teddy.TeddyModel;
import minions.minimizer.teddy.TeddyVec;
import minions.program.EncodeGraphsLoader;
import minions.program.PrePostExperimentLoader;
import models.code.TestTriplet;
import models.encoder.EncodeGraph;
import models.encoder.EncoderParams;
import models.encoder.ModelFormat;
import models.encoder.encoders.Encoder;
import models.encoder.encoders.models.StateEncodable;
import models.encoder.neurons.TreeNeuron;
import models.language.KarelLanguage;
import models.language.KarelLanguage2;

import org.apache.commons.math3.util.Pair;
import org.ejml.simple.SimpleMatrix;

import run.forceMultiply.ForceMultUtil;
import util.FileSystem;
import util.MatrixUtil;
import util.RandomUtil;
import util.Warnings;

public class Teddy4 {


	private Map<String, EncodeGraph> asts = 
			new HashMap<String, EncodeGraph>();

	public static void main(String[] args) {
		new Teddy4().run();
	}

	private void run() {

		// incase somehow this doesnt happen
		EncoderParams.setStateVectorSize(18);
		EncoderParams.setCodeVectorSize((int) Math.pow(EncoderParams.getM(),2));

		// get the model
		FileSystem.setAssnId("Midpoint");
		FileSystem.setExpId("teddyExp");
		TeddyModel m = loadModel("teddy-1422413537484/model");

		// get the matrix map
		System.out.println("loading ast matrix map...");
		Map<TreeNeuron, SimpleMatrix> matrixMap = loadMatrixMap();

		// get the programs
		FileSystem.setAssnId("Midpoint");
		FileSystem.setExpId("prePostExp3");
		System.out.println("load programs...");
		Map<String, TreeNeuron> programMap = ForceMultUtil.loadProgramsZip();

		
		// get the feedback
		System.out.println("loading feedback...");
		FileSystem.setExpId("feedbackExp");
		Map<String, List<Integer>> feedbackMap = ForceMultUtil.loadFeedbackZip();

		FMMinion minion = new FMEncoderTeddy2(m, matrixMap, programMap);
		ForceMultiplier force = new ForceMultiplier(minion, feedbackMap, programMap.keySet());
		force.run(500);
	}


	private List<Boolean> grade(List<Integer> list) {
		List<Boolean> labels = new ArrayList<Boolean>();
		for(int j = 0; j < 10; j++) {
			boolean applies = list.contains(j);
			labels.add(applies);
		}
		return labels;
	}

	private Map<String, List<Boolean>> getPrediction(
			TeddyModel model,
			Map<TreeNeuron, SimpleMatrix> matrixMap,
			Map<String, TreeNeuron> programMap) {
		Map<String, List<Boolean>> predictions = new HashMap<String,List<Boolean>>();
		int done = 0;
		for(String s : programMap.keySet()) {
			
		}

		return predictions;
	}

	private TeddyModel loadModel(String modelName) {
		File saved = new File(FileSystem.getExpDir(), "savedModels");
		File modelDir = new File(saved, modelName);

		Warnings.check(modelDir.exists(), "no model dir: " + modelDir.getAbsolutePath());

		ModelFormat format = EncoderSaver.loadFormat(modelDir);

		SimpleMatrix modelMatrix = EncoderSaver.loadSimpleMatrix(modelDir, "model.dat");
		double[] modelVec = new double[modelMatrix.getNumElements()];
		for(int i = 0; i < modelVec.length; i++) {
			modelVec[i] = modelMatrix.get(i);
		}

		TeddyDimension dim = (TeddyDimension) format.getDimension();

		return TeddyVec.getTeddy(dim, modelVec);
	}

	private double getParamLogScale(double min, double max) {
		double x = Math.random();
		return min * Math.pow(Math.E, Math.log(max/min) * x);
	}

	private Map<TreeNeuron, SimpleMatrix> loadMatrixMap() {
		FileSystem.setAssnId("Results");
		try {
			File f = new File(FileSystem.getAssnDir(), "teddy2.ser");
			InputStream file = new FileInputStream(f);
			InputStream buffer = new BufferedInputStream(file);
			ObjectInput input = new ObjectInputStream (buffer);

			//deserialize the List
			Map<TreeNeuron, SimpleMatrix> data = (Map<TreeNeuron, SimpleMatrix>)input.readObject();

			//display its data
			System.out.println(data.size());
			return data;

		}
		catch(ClassNotFoundException ex){
			throw new RuntimeException(ex);
		}
		catch(IOException ex){
			throw new RuntimeException(ex);
		}
	}
}

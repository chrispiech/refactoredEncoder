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
import minions.forceMult.FMEncoderRandom;
import minions.forceMult.FMMinion;
import minions.forceMult.ForceMultiplier;
import minions.program.EncodeGraphsLoader;
import minions.program.PrePostExperimentLoader;
import models.code.TestTriplet;
import models.encoder.EncodeGraph;
import models.encoder.EncoderParams;
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

public class Teddy3 {


	private Map<String, EncodeGraph> asts = 
			new HashMap<String, EncodeGraph>();

	public static void main(String[] args) {
		new Teddy3().run();
	}

	private void run() {
		System.out.println("loading ast matrix map...");
		Map<TreeNeuron, SimpleMatrix> matrixMap = loadMatrixMap();

		FileSystem.setAssnId("Midpoint");
		System.out.println("loading feedback...");
		FileSystem.setExpId("feedbackExp");
		Map<String, List<Integer>> feedbackMap = ForceMultUtil.loadFeedbackZip();
		
		EncoderParams.setStateVectorSize(18);
		EncoderParams.setCodeVectorSize((int) Math.pow(EncoderParams.getM(),2));
		EncoderParams.setMiniBatchSize(RandomUtil.nextInt(10, 30));
		EncoderParams.setWeightDecay(getParamLogScale(0.00001, 1.0));
		EncoderParams.setLearningRate(getParamLogScale(0.01, 1.0));
		
		FileSystem.setExpId("prePostExp3");
		Map<String, TreeNeuron> programMap = ForceMultUtil.loadProgramsZip();
		String name = "teddy-" + System.currentTimeMillis();
		
		FileSystem.setExpId("teddyExp");
		FMMinion minion = new FMEncoderTeddy(name, matrixMap, programMap, 0);
		ForceMultiplier force = new ForceMultiplier(minion, feedbackMap, feedbackMap.keySet());
		force.run(500);
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

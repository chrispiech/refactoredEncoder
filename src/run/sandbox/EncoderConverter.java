package run.sandbox;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import minions.encoder.EncoderSaver;
import minions.encoder.ModelTester;
import minions.program.PostExperimentLoader;
import models.code.TestTriplet;
import models.encoder.ClusterableMatrix;
import models.encoder.EncoderParams;
import models.encoder.ModelFormat;
import models.encoder.encoders.Encoder;
import models.encoder.encoders.InternalEncoder;
import models.encoder.encoders.models.BearModel;
import models.encoder.encoders.programEncoder.ProgramEncoderVec;
import models.encoder.encoders.state.StateDecoder;
import models.encoder.decoders.*;

import org.ejml.simple.SimpleMatrix;

import util.FileSystem;
public class EncoderConverter {
	
	/*private void run() {

		List<TestTriplet>testSet = ExperimentLoader.loadTests("test", 1000);
		EncoderParams.setCodeVectorSize(50);
		FileSystem.setAssnId("Hoc18");
		FileSystem.setExpId("postExp");
		BearModel m = load("oldAlyssa");
		File modelDir = EncoderSaver.save(m, "conversion of alyssa model");
		Encoder prime = EncoderSaver.load(modelDir.getName());
		System.out.println(m.equals(prime));
		System.out.println("acc = " + ModelTester.calcAccuracy(m, testSet) + "%");
		System.out.println("acc = " + ModelTester.calcAccuracy(prime, testSet) + "%");
		
	}
	
	public static BearModel load(String name) {
		File expDir = FileSystem.getExpDir();
		File modelsDir = new File(expDir, "savedModels");
		File modelDir = new File(modelsDir, name);
		
		ModelFormat f = new ModelFormat("blocky", "bear");
		
		Map<String, OutputDecoder> outMap = new HashMap<String, OutputDecoder>();
		for(int i = 0; i < f.getNumOutputs(); i++) {
			SimpleMatrix W = loadSimpleMatrix(modelDir, "out" + i + "_W");
			SimpleMatrix b = loadSimpleMatrix(modelDir, "out" + i + "_b");
			String key = getKey(i);
			outMap.put(key, new SoftmaxDecoder(W, b, getNumOutputOptions(i)));
		}
		
		HashMap<String, InternalEncoder> ins = new HashMap<String, InternalEncoder>();
		for(String type : f.getInternalEncoderTypes()) {
			List<SimpleMatrix> wList = new ArrayList<SimpleMatrix>();
			for(int i = 0; i < f.getArity(type); i++) {
				SimpleMatrix W = loadSimpleMatrix(modelDir, type + "_W" + i);
				wList.add(W);
			}
			SimpleMatrix b = loadSimpleMatrix(modelDir, type + "_b");
			InternalEncoder in = new InternalEncoder(type, wList, b);
			ins.put(type, in);
		}
		
		HashMap<String, CodeVector> leaves = new HashMap<String, CodeVector>();
		for(String type : f.getLeafTypes()) {
			SimpleMatrix v = loadSimpleMatrix(modelDir, "leaf_"+ type);
			leaves.put(type, new CodeVector(v));
		}
		
		ProgramEncoder program = new ProgramEncoder(f, ins, leaves);
		BearOutputEncoder outputs = new BearOutputEncoder(f, outMap);
		return new BearModel(f, program, outputs);
	}
	
	private static String getKey(int i) {
		switch(i) {
		case 0: return "row";
		case 1: return "col";
		case 2: return "direction";
		case 3: return "status";
		}
		throw new RuntimeException("no");
	}

	private static int getNumOutputOptions(int index) {
		return getOutputs(index).length;
	}
	
	private static int[] getOutputs(int index) {
		if(index == 0) {
			return getRowOutputs();
		} 
		if(index == 1) {
			return getColOutputs();
		}
		if(index == 2) {
			return getDirOutputs();
		}
		if(index == 3) {
			return getStateOutputs();
		}
		throw new RuntimeException("no...");
	}
	
	private static int[] getStateOutputs() {
		int[] outputs = {0, 1, 2, -1};
		return outputs;
	}
	
	private static int[] getDirOutputs() {
		int[] outputs = {0, 1, 2, 3};
		return outputs;
	}

	private static int[] getColOutputs() {
		int[] outputs = {0, 1, 2, 3, 4, 5, 6};
		return outputs;
	}

	private static int[] getRowOutputs() {
		int[] outputs = {2, 3, 4, 5, 6};
		return outputs;
	}
	
	
	private static SimpleMatrix loadSimpleMatrix(File dir, String fileName) {
		File matrixFile = new File(dir, fileName);
		try {
			return SimpleMatrix.loadBinary(matrixFile.getPath());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void main(String[] args) {
		new EncoderConverter().run();
	}*/
}

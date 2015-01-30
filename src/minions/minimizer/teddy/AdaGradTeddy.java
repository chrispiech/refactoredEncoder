package minions.minimizer.teddy;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import minions.encoder.EncoderSaver;
import minions.encoder.factory.EncoderFactory;
import models.encoder.EncoderParams;
import models.encoder.ModelFormat;
import models.encoder.encoders.Encoder;
import models.encoder.neurons.TreeNeuron;

import org.ejml.simple.SimpleMatrix;

import util.FileSystem;

public class AdaGradTeddy {

	Map<TreeNeuron, SimpleMatrix> encodingMap;
	Map<String, TreeNeuron> programMap;
	Map<String, List<Boolean>> gradedMap;
	ModelFormat format;
	
	String name;
	String log = "";
	
	double sumError;
	int numTested;

	public static void train(
			Map<TreeNeuron, SimpleMatrix> encodingMap,
			Map<String, TreeNeuron> programMap,
			Map<String, List<Boolean>> gradedMap,
			String name) {
		AdaGradTeddy teddy = new AdaGradTeddy();
		teddy.encodingMap = encodingMap;
		teddy.programMap = programMap;
		teddy.gradedMap = gradedMap;
		teddy.format = new ModelFormat("karel", "teddy");
		teddy.name = name;
		teddy.run();
	}

	private void run() {
		System.out.println("Training!");
		System.out.println("---------");
		System.out.println("num params: " + format.getDimension().getDimension());
		setupLog();
		
		List<String> train = new ArrayList<String>(gradedMap.keySet());
		double[] x = EncoderFactory.makeRandomVec(format);
		double[] gradStore = new double[x.length];
		int miniBatchSize = EncoderParams.getMiniBatchSize();
		double eta = EncoderParams.getLearningRate();
		
		System.out.println();
		System.out.println("Traning in progress ...");
		while(true){
			sumError = 0;
			numTested = 0;

			Collections.shuffle(train);			
			int numMinibatches = train.size() / miniBatchSize;

			for(int t = 0; t < numMinibatches; t++) {
				TeddyModel model = getModel(x);
				List<String> miniBatch = getMiniBatch(train, t, miniBatchSize);
				double[] gradVec = getGrad(model, miniBatch);
				for(int i = 0; i < gradVec.length; i++) {
					if(gradVec[i] == 0) continue;
					gradStore[i] += Math.pow(gradVec[i], 2); 
					double weight = eta/Math.sqrt(gradStore[i]);
					x[i] = (x[i] - weight*gradVec[i]);
				}
			}

			epochOutput(x);
		}

	}
	
	private void setupLog() {
		log = "log start " + new SimpleDateFormat("dd-MM-yyyy").format(new Date()) + "\n";
		log += EncoderSaver.makeNotes() + "\n";
		log += "-------------\n";
		System.out.println(log);
	}

	private void epochOutput(double[] x) {
		double error = sumError / numTested;
		System.out.println(error);
		
		log += error + "\n";
		File expDir = FileSystem.getExpDir();
		File savedModelsDir = new File(expDir, "savedModels");
		File modelDir = new File(savedModelsDir, name);
		FileSystem.createFile(modelDir, "log.txt", log);

		String notes = EncoderSaver.makeNotes();
		notes += error;
		EncoderSaver.save(x, format, name, "model", notes);
	}

	private double[] getGrad(TeddyModel model, List<String> miniBatch) {
		TeddyBackprop b = new TeddyBackprop();
		b.encodingMap = encodingMap;
		b.programMap = programMap;
		b.gradedMap = gradedMap;
		b.dimension = (TeddyDimension) new ModelFormat("karel", "teddy").getDimension();
		TeddyModel grad = b.run(model, miniBatch);
		sumError += b.getSumError();
		numTested += miniBatch.size();
		return TeddyVec.teddyToVec(grad, (TeddyDimension) format.getDimension());
	}

	private List<String> getMiniBatch(List<String> train, int t,
			int miniBatchSize) {
		int start = t * miniBatchSize;
		int end = (t + 1) * miniBatchSize;
		return train.subList(start, end);
	}

	private TeddyModel getModel(double[] x) {
		return TeddyVec.getTeddy((TeddyDimension) format.getDimension(), x);
	}

}

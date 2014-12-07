package run.train;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import util.FileSystem;
import minions.encoder.EncoderSaver;
import minions.encoder.ModelValueAt;
import minions.encoder.backprop.BearBackprop;
import minions.encoder.factory.EncoderFactory;
import minions.encoder.modelVector.ModelVector;
import minions.minimizer.AdaGrad;
import minions.program.PostExperimentLoader;
import models.code.TestTriplet;
import models.encoder.EncoderParams;
import models.encoder.ModelFormat;
import models.encoder.encoders.BearModel;
import models.encoder.encoders.Encoder;

public class MidpointTrain {
	
	private static final String LANGUAGE = "karel";
	private static final String MODEL_TYPE = "bear";
	private static final String NAME = "honey0";
	
	List<TestTriplet> trainSet = null;
	List<TestTriplet> testSet = null;
	ModelFormat format = null;

	private void run() {
		format = new ModelFormat(LANGUAGE, MODEL_TYPE);
		EncoderParams.setCodeVectorSize(40);
		FileSystem.setAssnId("Midpoint");
		FileSystem.setExpId("postExp");
		EncoderParams.setWorldDim(6, 6);
		trainSet = PostExperimentLoader.loadFolds("6x6", 9, format.getLanguage());
		trainSet = PostExperimentLoader.removeRecursive(trainSet, format.getLanguage());
		
		System.out.println(trainSet.size());
		//testSet = ExperimentLoader.loadTests("test", 100);
		
		train();
	}
	
	public void train() {
		int epochs = 30;  
		double eta = EncoderParams.getLearningRate();
		int miniBatchSize = 10000;
		double[] loss = new double[epochs];
		final long startTime = System.currentTimeMillis();
		Encoder model = AdaGrad.minimize(format, trainSet, epochs, miniBatchSize, eta, loss, NAME);
		final long endTime = System.currentTimeMillis();
		long elapsedTime = endTime - startTime;
		System.out.println("time to train: " + elapsedTime);
		
		String notes = "";
		notes += "epochs: " + epochs;
		notes += "miniBatchSize: " + miniBatchSize;
		notes += "learningRate: " + EncoderParams.getLearningRate();
		notes += "weightDecay: " + EncoderParams.getWeightDecay();
		EncoderSaver.save(model, NAME, notes);
	}
	
	public static void main(String[] args) {
		new MidpointTrain().run();
	}
	
}

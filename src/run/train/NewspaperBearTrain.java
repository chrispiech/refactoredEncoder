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
import models.encoder.encoders.Encoder;
import models.encoder.encoders.models.BearModel;

public class NewspaperBearTrain {
	
	private static final String LANGUAGE = "karel";
	private static final String MODEL_TYPE = "bear";
	private static final String NAME = "polar0";
	
	List<TestTriplet> trainSet = null;
	List<TestTriplet> testSet = null;
	ModelFormat format = null;

	private void run() {
		format = new ModelFormat(LANGUAGE, MODEL_TYPE);
		EncoderParams.setCodeVectorSize(40);
		FileSystem.setAssnId("Newspaper");
		FileSystem.setExpId("postExp");
		trainSet = PostExperimentLoader.loadFolds("homeWorld", 9, format.getLanguage());
		trainSet = PostExperimentLoader.removeRecursive(trainSet, format.getLanguage());
		
		System.out.println(trainSet.size());
		//testSet = ExperimentLoader.loadTests("test", 100);
		
		train();
	}
	
	public void train() {
		int epochs = 30;  
		double eta = EncoderParams.getLearningRate();
		int miniBatchSize = 100;
		double[] loss = new double[epochs];
		final long startTime = System.currentTimeMillis();
		Encoder model = AdaGrad.minimize(format, trainSet, epochs, miniBatchSize, eta, loss, NAME);
		final long endTime = System.currentTimeMillis();
		long elapsedTime = endTime - startTime;
		System.out.println("time to train: " + elapsedTime);
		EncoderSaver.save(model, NAME, "");
	}
	
	
	
	public static void main(String[] args) {
		new NewspaperBearTrain().run();
	}
	
}

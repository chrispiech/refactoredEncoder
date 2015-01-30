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
import minions.minimizer.lemur.AdaGradThreadedLemur;
import minions.program.PostExperimentLoader;
import models.code.TestTriplet;
import models.encoder.EncoderParams;
import models.encoder.ModelFormat;
import models.encoder.encoders.Encoder;
import models.encoder.encoders.models.BearModel;

public class BlockyBearTrain {
	
	private static final String NAME = "brown2";
	private static final String LANGUAGE = "blocky";
	private static final String MODEL_TYPE = "bear";
	
	List<TestTriplet> trainSet = null;
	List<TestTriplet> testSet = null;
	ModelFormat format = null;

	private void run() {
		System.out.println("hello world");
		FileSystem.setAssnId("Hoc18");
		FileSystem.setExpId("postExp");
		EncoderParams.setCodeVectorSize(100);
		EncoderParams.setWeightDecay(0.000001);
		EncoderParams.setLearningRate(0.1);

		format = new ModelFormat(LANGUAGE, MODEL_TYPE);
		trainSet = PostExperimentLoader.loadTests("train", 70000, format.getLanguage());
		//testSet = ExperimentLoader.loadTests("test", 100);
		train();
	}
	
	public void train() {
		int epochs = 50;  
		int miniBatchSize = 1000;
		
		double eta = EncoderParams.getLearningRate();
		
		double[] loss = new double[epochs];
		final long startTime = System.currentTimeMillis();
		Encoder model = AdaGradThreadedLemur.train(format, trainSet, epochs, NAME);
		//Encoder model = AdaGrad.minimize(format, trainSet, epochs, miniBatchSize, eta, loss, NAME);
		final long endTime = System.currentTimeMillis();
		long elapsedTime = endTime - startTime;
		System.out.println("time to train: " + elapsedTime);
		EncoderSaver.save(model, NAME, EncoderSaver.makeNotes(trainSet, epochs));
	
	}
	
	public static void main(String[] args) {
		new BlockyBearTrain().run();
	}
	
}

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
import minions.program.PrePostExperimentLoader;
import models.code.TestTriplet;
import models.encoder.EncoderParams;
import models.encoder.ModelFormat;
import models.encoder.encoders.Encoder;
import models.encoder.encoders.models.BearModel;

public class BlockyMonkeyTrain {
	
	private static final String LANGUAGE = "blocky";
	private static final String MODEL_TYPE = "monkey";
	private static final String NAME = "gorilla196";
	
	List<TestTriplet> trainSet = null;
	List<TestTriplet> testSet = null;
	ModelFormat format = null;

	private void run() {
		System.out.println("hello world");
		EncoderParams.setCodeVectorSize(9);
		EncoderParams.setStateVectorSize(EncoderParams.getSqrtN());
		// first, try to overfit
		EncoderParams.setWeightDecay(0.0001);
		FileSystem.setAssnId("Hoc18");
		FileSystem.setExpId("prePostExp");
		format = new ModelFormat(LANGUAGE, MODEL_TYPE);
		trainSet = PrePostExperimentLoader.loadTriplets("train", 2000, format.getLanguage());
		System.out.println("train set size: " + trainSet.size());
		train();
	}
	
	public void train() {
		int epochs = 20;  
		double eta = EncoderParams.getLearningRate();
		int miniBatchSize = 1000;
		double[] loss = new double[epochs];
		Encoder model = AdaGradThreadedLemur.train(format, trainSet, epochs, NAME);
		//Encoder model = AdaGrad.minimize(format, trainSet, epochs, miniBatchSize, eta, loss, NAME);
		EncoderSaver.save(model, NAME, EncoderSaver.makeNotes(trainSet, epochs));
	}

	public static void main(String[] args) {
		new BlockyMonkeyTrain().run();
	}
	
}

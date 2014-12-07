package run.train;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import util.FileSystem;
import util.Warnings;
import minions.encoder.EncoderSaver;
import minions.encoder.ModelValueAt;
import minions.encoder.backprop.BearBackprop;
import minions.encoder.factory.EncoderFactory;
import minions.encoder.modelVector.ModelVector;
import minions.minimizer.AdaGrad;
import minions.minimizer.AdaGrad2;
import minions.program.PostExperimentLoader;
import minions.program.PrePostExperimentLoader;
import models.code.TestTriplet;
import models.encoder.EncoderParams;
import models.encoder.ModelFormat;
import models.encoder.encoders.BearModel;
import models.encoder.encoders.Encoder;

public class NewspaperMonkeyTrain {
	
	private static final String LANGUAGE = "karel";
	private static final String MODEL_TYPE = "monkey";
	private static final String NAME = "papermonkey";
	
	List<TestTriplet> trainSet = null;
	List<TestTriplet> testSet = null;
	ModelFormat format = null;

	private void run() {
		format = new ModelFormat(LANGUAGE, MODEL_TYPE);
		EncoderParams.setCodeVectorSize(64);
		EncoderParams.setStateVectorSize(EncoderParams.getSqrtN());
		EncoderParams.setWeightDecay(0.0001);
		FileSystem.setAssnId("Newspaper");
		FileSystem.setExpId("prePostExp");
		EncoderParams.setWorldDim(5, 7);
		trainSet = PrePostExperimentLoader.loadTriplets("train", -1, format.getLanguage());
		
		System.out.println(trainSet.size());
		
		train();
	}
	
	public void train() {
		int epochs = 30;  
		final long startTime = System.currentTimeMillis();
		Encoder model = AdaGrad2.train(format, trainSet, epochs, NAME);
		final long endTime = System.currentTimeMillis();
		long elapsedTime = endTime - startTime;
		System.out.println("time to train: " + elapsedTime);
		EncoderSaver.save(model, NAME, "");
	}
	
	
	
	public static void main(String[] args) {
		new NewspaperMonkeyTrain().run();
	}
	
}

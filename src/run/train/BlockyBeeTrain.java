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
import minions.program.PrePostExperimentLoader;
import models.code.TestTriplet;
import models.encoder.EncoderParams;
import models.encoder.ModelFormat;
import models.encoder.encoders.BearModel;
import models.encoder.encoders.Encoder;

public class BlockyBeeTrain {
	
	private static final String LANGUAGE = "blocky";
	private static final String MODEL_TYPE = "bee";
	private static final String NAME = "bumble0";
	
	List<TestTriplet> trainSet = null;
	List<TestTriplet> testSet = null;
	ModelFormat format = null;

	private void run() {
		System.out.println("hello world");
		EncoderParams.setCodeVectorSize(64);
		// first, try to overfit
		EncoderParams.setWeightDecay(0.0);
		EncoderParams.setStateVectorSize(10);
		FileSystem.setAssnId("Hoc18");
		FileSystem.setExpId("prePostExp");
		format = new ModelFormat(LANGUAGE, MODEL_TYPE);
		trainSet = PrePostExperimentLoader.loadTriplets("train", -1, format.getLanguage());
		System.out.println("train set size: " + trainSet.size());
		train();
	}
	
	public void train() {
		int epochs = 100;  
		double eta = EncoderParams.getLearningRate();
		int miniBatchSize = 1000;
		double[] loss = new double[epochs];
		Encoder model = AdaGrad.minimize(format, trainSet, epochs, miniBatchSize, eta, loss, NAME);
		EncoderSaver.save(model, NAME, EncoderSaver.makeNotes(trainSet, epochs));
	}
	
	


	public static void main(String[] args) {
		new BlockyBeeTrain().run();
	}
	
}

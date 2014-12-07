package run.sandbox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import minions.encoder.EncoderSaver;
import minions.minimizer.AdaGrad;
import minions.program.PostExperimentLoader;
import models.code.TestTriplet;
import models.encoder.EncoderParams;
import models.encoder.ModelFormat;
import models.encoder.encoders.Encoder;
import models.language.BlockyLanguage;
import run.train.NewspaperBearTrain;
import util.FileSystem;

public class BlockyGrad {
	private void run() {
		System.out.println("hello world");
		EncoderParams.setCodeVectorSize(2);
		FileSystem.setAssnId("Hoc18");
		FileSystem.setExpId("postExp");
		EncoderParams.setWorldDim(5, 7);
		List<TestTriplet> trainSet = PostExperimentLoader.load(200, new BlockyLanguage());
		ModelFormat format = new ModelFormat("blocky", "bear");
		
		System.out.println("Original size: " + trainSet.size());
		trainSet = PostExperimentLoader.removeRecursive(trainSet, format.getLanguage());
		System.out.println("Nonrecursive:  " + trainSet.size());
		
		
		gogogo(format, trainSet);
	}
	
	public void gogogo(ModelFormat format, List<TestTriplet> trainSet) {
		int epochs = 100;  
		double eta = EncoderParams.getLearningRate();
		int miniBatchSize = 100;
		double[] loss = new double[epochs];
		final long startTime = System.currentTimeMillis();
		AdaGrad.checkGrad(format, trainSet, epochs, miniBatchSize, eta, loss);
		final long endTime = System.currentTimeMillis();
		long elapsedTime = endTime - startTime;
		System.out.println("time to train: " + elapsedTime);
	}
	
	
	
	public static void main(String[] args) {
		new BlockyGrad().run();
	}
}

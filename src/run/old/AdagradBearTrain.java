package run.old;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Arrays; 

import minions.encoder.EncoderSaver;
import minions.encoder.GradValidator;
import minions.encoder.ModelTester;
import minions.encoder.ModelValueAt;
import minions.encoder.backprop.EncoderBackprop;
import minions.encoder.modelVector.ModelVector;
import minions.parser.EncodeTreeParser;
import minions.program.PostExperimentLoader;
import models.ast.Tree;
import models.code.TestTriplet;
import models.encoder.EncoderParams;
import models.encoder.ModelFormat;
import models.encoder.encoders.BearModel;
import models.encoder.encoders.Encoder;
import models.encoder.neurons.TreeNeuron;

import org.ejml.simple.SimpleMatrix;

import util.FileSystem;
import util.MatrixUtil;

public class AdagradBearTrain {

	private static int NUM_TRAIN = 70000;
	
	private List<TestTriplet> trainSet;
	private List<TestTriplet> testSet1k;
	private List<TestTriplet> testSet3k;
	private List<TestTriplet> testSet10k;
	private List<TestTriplet> testSetFull;


	private ModelFormat format = new ModelFormat("blocky", "bear");

	public double[] getInitial() {
		BearModel init = new BearModel(format);
		return ModelVector.modelToVec(init);
	}

	public BearModel getModel(double[] params) {
		return (BearModel) ModelVector.vecToModel(format, params);
	}

	public double valueAt(double[] params) {
		BearModel model = getModel(params);
		return ModelValueAt.valueAt(model, trainSet);
	}

	private void run() {
		FileSystem.setAssnId("Hoc18");
		FileSystem.setExpId("postExp");
	
		trainSet = PostExperimentLoader.loadTests("train", NUM_TRAIN, format.getLanguage());
		testSet1k = PostExperimentLoader.loadTests("test", 1000, format.getLanguage());
		testSet3k = PostExperimentLoader.loadTests("test", 3000, format.getLanguage());
		testSet10k = PostExperimentLoader.loadTests("test", 10000, format.getLanguage());
		testSetFull = PostExperimentLoader.loadTests("test", -1, format.getLanguage());
		System.out.println();
		System.out.println("-----------------------------------");
		System.out.println("NOW it's time to do some training!!");
		trainAndTest(trainSet);
	}

	private double[] SGD(List<TestTriplet> train, int epochs, int mini_batch_size, double eta, double[] loss) {
		BearModel init = new BearModel(format);
		double[] x = ModelVector.modelToVec(init);
		double[] gradStore = new double[x.length];

		System.out.println();
		System.out.println("Traning in progress ...");

		for(int j = 0; j < epochs; j++) {
			final long startTime = System.currentTimeMillis();

			long seed = System.nanoTime();
			Collections.shuffle(train, new Random(seed));			
			List<List<TestTriplet>> mini_batches = new ArrayList<List<TestTriplet>>();
			for(int k = 0; k < train.size(); k += mini_batch_size) {
				mini_batches.add(train.subList(k, k + mini_batch_size));
			}

			for(int t = 0; t < mini_batches.size(); t++) {

				//checkGrad(mini_batches, t, ModelVector.vecToModel(format, x));
				BearModel model = getModel(x);
				Encoder grad = EncoderBackprop.derivative(model, mini_batches.get(t));
				double[] gradVec =  ModelVector.modelToVec(grad);
				for(int i = 0; i < gradVec.length; i++) {
					gradStore[i] += Math.pow(gradVec[i], 2); 
					double weight = eta/Math.sqrt(gradStore[i]);
					x[i] = (x[i] - weight*gradVec[i]);
				}
			}

			double value = valueAt(x);
			System.out.println("Epoch " + j + " complete. Objective value is " + value);
			final long endTime = System.currentTimeMillis();
			System.out.println("Epoch time: " + (endTime - startTime)/1000 );
			loss[j] = value;  
		}

		return x;
	}

	private void trainAndTest(List<TestTriplet> train) {

		EncoderParams.setLearningRate(0.05);
		int minibatch_size = 1000;
		int epochs = 50;   
		EncoderParams.setCodeVectorSize(10);
		EncoderParams.setWeightDecay(0.001);

		double eta = EncoderParams.getLearningRate();
		System.out.println("\n");

		System.out.println("num train = " + NUM_TRAIN);
		System.out.println();
		System.out.println("Set up");
		System.out.println("----------------");
		System.out.println("Learning rate = " + eta);
		System.out.println("Minibatch size = " + minibatch_size);
		System.out.println("Epochs = " + epochs);
		System.out.println("code vector dim = " + EncoderParams.getCodeVectorSize());
		System.out.println("weight decay = " + EncoderParams.getWeightDecay());

		double[] loss = new double[epochs];
		final long startTime = System.currentTimeMillis();
		double[] x = SGD(train, epochs, minibatch_size, eta, loss);
		final long endTime = System.currentTimeMillis();

		Encoder model = getModel(x);

		double accuracyTrain = calcAccuracy(train, model);
		double accuracy1k = calcAccuracy(testSet1k, model);
		double accuracy3k = calcAccuracy(testSet3k, model);
		double accuracy10k = calcAccuracy(testSet10k, model);
		double accuracyFull = calcAccuracy(testSetFull, model);

		System.out.println();
		System.out.println("Result");
		System.out.println("----------------");
		System.out.println("num train = " + NUM_TRAIN);
		System.out.println("num parameter = " + x.length);
		System.out.println("training time = " + (endTime - startTime)/1000 + " s");
		System.out.println("Set up");
		System.out.println("----------------");
		System.out.println("Learning rate = " + EncoderParams.getLearningRate());
		System.out.println("Minibatch size = " + minibatch_size);
		System.out.println("Epochs = " + epochs);
		System.out.println("code vector dim = " + EncoderParams.getCodeVectorSize());
		System.out.println("weight decay = " + EncoderParams.getWeightDecay());
		System.out.println("Accuracy");
		System.out.println("----------------");
		System.out.println("final loss function = " + loss[epochs-1]);
		System.out.println("train accuracy: " + accuracyTrain + "%");
		System.out.println("test accuracy (1k): " + accuracy1k + "%");
		System.out.println("test accuracy (3k): " + accuracy3k + "%");
		System.out.println("test accuracy (10k): " + accuracy10k + "%");
		System.out.println("test accuracy (full): " + accuracyFull + "%");

		String notes = "Result" + "\n";
		notes += "----------------" + "\n";
		notes += "num train = " + NUM_TRAIN + "\n";
		notes += "num parameter = " + x.length + "\n";
		notes += "training time = " + (endTime - startTime)/1000 + " s" + "\n";
		notes += "Set up" + "\n";
		notes += "----------------" + "\n";
		notes += "Learning rate = " + EncoderParams.getLearningRate() + "\n";
		notes += "Minibatch size = " + minibatch_size + "\n";
		notes += "Epochs = " + epochs + "\n";
		notes += "code vector dim = " + EncoderParams.getCodeVectorSize() + "\n";
		notes += "weight decay = " + EncoderParams.getWeightDecay() + "\n";
		notes += "Accuracy" + "\n";
		notes += "train accuracy: " + accuracyTrain + "%" + "\n";
		notes += "test accuracy (1k): " + accuracy1k + "%" + "\n";
		notes += "test accuracy (3k): " + accuracy3k + "%" + "\n";
		notes += "test accuracy (10k): " + accuracy10k + "%" + "\n";
		notes += "test accuracy (full): " + accuracyFull + "%" + "\n";
		notes +=  Arrays.toString(loss)+ "\n";

		EncoderSaver.save(model, "oldBear", notes);
	}	


	private double calcAccuracy(List<TestTriplet> train, Encoder model) {
		return ModelTester.calcAccuracy(model, train);
	}



	/*
	private String makeReport(double accuracy) {
		String notes = "accuracy = " + accuracy + "\n";
		notes += "num train = " + NUM_TRAIN + "\n";
		notes += "function tolerance = " + TOLERANCE + "\n";
		notes += "code vector dim = " + EncoderParams.CODE_VECTOR_SIZE + "\n";
		notes += "weight decay = " + EncoderParams.WEIGHT_DECAY + "\n";
		return notes;
	}
	 */

	public static void main(String [] args) {
		new AdagradBearTrain().run();
	}

}


package minions.minimizer.lemur;

import java.io.File;
import java.io.ObjectInputStream.GetField;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import org.apache.commons.math3.util.Pair;
import org.ejml.simple.SimpleMatrix;

import util.FileSystem;
import util.MatrixUtil;
import util.Warnings;
import edu.stanford.nlp.optimization.SGDMinimizer;
import minions.encoder.EncoderSaver;
import minions.encoder.GradValidator;
import minions.encoder.ModelValueAt;
import minions.encoder.backprop.EncoderBackprop;
import minions.encoder.factory.EncoderFactory;
import minions.encoder.modelVector.ModelVector;
import minions.minimizer.turtle.TurtleThread;
import models.code.TestTriplet;
import models.encoder.EncoderParams;
import models.encoder.ModelFormat;
import models.encoder.encoders.Encoder;
import models.encoder.encoders.models.BearModel;
import models.encoder.encoders.models.LemurModel;
import models.encoder.encoders.models.TurtleModel;

public class AdaGradThreadedLemur {
	private ModelFormat format;

	// some bookkeeping.
	private String name;
	private double sumError;
	private int numTested;
	private String log;

	public static void train(ModelFormat f, List<TestTriplet> train, int maxHours, String name) {
		AdaGradThreadedLemur min = new AdaGradThreadedLemur();
		min.format = f;
		min.name = name;
		int miniBatchSize = EncoderParams.getMiniBatchSize();
		double eta = EncoderParams.getLearningRate();
		min.SGD(train, maxHours, miniBatchSize, eta);
	}

	private void SGD(List<TestTriplet> train, int epochs, int miniBatchSize, double eta) {
		System.out.println("Training!");
		System.out.println("---------");
		System.out.println("num params: " + format.getDimension().getDimension());

		double[] x = EncoderFactory.makeRandomVec(format);
		double[] gradStore = new double[x.length];

		setup();

		System.out.println();
		System.out.println("Traning in progress ...");
		while(true){
			sumError = 0;
			numTested = 0;

			Collections.shuffle(train);			
			int numMinibatches = train.size() / miniBatchSize;

			System.out.println("mini batches: " + numMinibatches);

			for(int t = 0; t < numMinibatches; t++) {
				Encoder model = getModel(x);
				List<TestTriplet> miniBatch = getMiniBatch(train, t, miniBatchSize);
				double[] gradVec = getGrad(model, miniBatch);
				for(int i = 0; i < gradVec.length; i++) {
					if(gradVec[i] == 0) continue;
					gradStore[i] += Math.pow(gradVec[i], 2); 
					double weight = eta/Math.sqrt(gradStore[i]);
					x[i] = (x[i] - weight*gradVec[i]);
				}
				System.out.print(".");
			}
			System.out.println("\n");

			epochOutput(x);
		}
	}

	private List<TestTriplet> getMiniBatch(List<TestTriplet> train, int t, int miniBatchSize) {
		int start = t * miniBatchSize;
		int end = (t + 1) * miniBatchSize;
		return train.subList(start, end);
	}

	private void setup() {
		log = "log start " + new SimpleDateFormat("dd-MM-yyyy").format(new Date()) + "\n";
		log += EncoderSaver.makeNotes() + "\n";
		log += "-------------\n";
		System.out.println(log);
	}

	private double[] getGrad(Encoder model, List<TestTriplet> list) {
		List<LemurThread> threadList = new ArrayList<LemurThread>();


		LemurThread.miniBatch = list;
		LemurThread.modelGrad = new double[format.getNumParams()];

		int numThreads = EncoderParams.getNumThreads();
		if(EncoderParams.getMultiThreaded() && numThreads > 1) {
			for(int i = 0; i < numThreads; i++) {
				LemurThread thread = new LemurThread(model);
				thread.start();
				threadList.add(thread);
			}
			for(LemurThread thread : threadList) {
				try {
					thread.join();
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}

				sumError += thread.getSumError();
				numTested += thread.getTested();
			}
		} else {
			LemurThread thread = new LemurThread(model);
			thread.run();
			sumError += thread.getSumError();
			numTested += thread.getTested();
		}
		return LemurThread.modelGrad;
	}

	public double[] getInitial() {
		BearModel init = new BearModel(format);
		return ModelVector.modelToVec(init);
	}

	private void epochOutput(double[] x) {
		double error = sumError / numTested;
		System.out.println(error);

		String notes = EncoderSaver.makeNotes();
		notes += error;
		log += error + "\n";

		EncoderSaver.save(x, format, name, "model", notes);

		saveLog();
	}

	private void saveLog() {
		File expDir = FileSystem.getExpDir();
		File savedModelsDir = new File(expDir, "savedModels");
		File modelDir = new File(savedModelsDir, name);
		FileSystem.createFile(modelDir, "log.txt", log);

	}

	public Encoder getModel(double[] params) {
		return ModelVector.vecToModel(format, params);
	}

}

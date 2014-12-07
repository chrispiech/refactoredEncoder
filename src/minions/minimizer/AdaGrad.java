package minions.minimizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.ejml.simple.SimpleMatrix;

import util.MatrixUtil;
import util.Warnings;
import minions.encoder.EncoderSaver;
import minions.encoder.GradValidator;
import minions.encoder.ModelValueAt;
import minions.encoder.backprop.BearBackprop;
import minions.encoder.backprop.EncoderBackprop;
import minions.encoder.factory.EncoderFactory;
import minions.encoder.modelVector.ModelVector;
import models.code.TestTriplet;
import models.encoder.ModelFormat;
import models.encoder.encoders.BearModel;
import models.encoder.encoders.Encoder;
import models.encoder.encoders.StateEncoder;
import models.encoder.encoders.types.StateEncodable;

/**
 * @author Mike Phulsuksombati
 */
public class AdaGrad {

	public static Encoder minimize(ModelFormat format, 
			List<TestTriplet> train, 
			int epochs, 
			int miniBatchSize, 
			double eta, 
			double[] loss, String name) {
		return runAlgorithm(format, train, epochs, miniBatchSize, eta, loss, false, name);
	}


	public static void checkGrad(ModelFormat format, 
			List<TestTriplet> train, 
			int epochs, 
			int miniBatchSize, 
			double eta, 
			double[] loss) {
		runAlgorithm(format, train, epochs, miniBatchSize, eta, loss, true, "");
	}

	private static Encoder runAlgorithm(
			ModelFormat format, 
			List<TestTriplet> train, 
			int epochs, 
			int miniBatchSize, 
			double eta, 
			double[] loss,
			boolean checkGrad, String name) {
		final long algoStartTime = System.currentTimeMillis();
		Encoder init = EncoderFactory.makeInitial(format);
		double[] x = ModelVector.modelToVec(init);
		double[] gradStore = new double[x.length];

		System.out.println();
		System.out.println("Traning in progress ...");
		System.out.println("size: " + train.size());
		
		double numMiniBatches = Math.ceil(1.0 * train.size() / miniBatchSize);
		double numMiniBatchUpdates = epochs * numMiniBatches;
		int updatesDone = 0;

		for(int j = 0; j < epochs; j++) {
			System.out.println("\nEpoch " + j);
			final long epochStartTime = System.currentTimeMillis();

			long seed = System.nanoTime();
			Collections.shuffle(train, new Random(seed));			
			List<List<TestTriplet>> miniBatches = makeMiniBatches(train,
					miniBatchSize);

			for(int t = 0; t < miniBatches.size(); t++) {
				final long miniBatchStart = System.currentTimeMillis();
				Encoder model = getModel(format, x);
				if(checkGrad) {
					checkGrad(miniBatches, t, model);
				}
				Encoder grad = EncoderBackprop.derivative(model, miniBatches.get(t));
				double[] gradVec =  ModelVector.modelToVec(grad);
				for(int i = 0; i < gradVec.length; i++) {
					if(gradVec[i] == 0) continue;
					gradStore[i] += Math.pow(gradVec[i], 2);
					double weight = eta/Math.sqrt(gradStore[i]);
					x[i] = (x[i] - weight*gradVec[i]);
				}
				
				/*final long miniBatchEnd = System.currentTimeMillis();
				updatesDone++;
				double algoDone = (100.0 * updatesDone / numMiniBatchUpdates);
				double todo = numMiniBatchUpdates - updatesDone;
				double algoTime = (miniBatchEnd - algoStartTime)/1000.0;
				double timeToComplete = (algoTime / updatesDone) * todo;
				//StateEncodable justForTests = (StateEncodable) ModelVector.vecToModel(format, x);
				System.out.println("  epoch:             " + j);
				System.out.println("  mini-batch:        " + t);
				System.out.println("  mini-batch time:   " + (miniBatchEnd - miniBatchStart)/1000.0 + "s");
				System.out.println("  mini-batch size:   " + miniBatches.get(t).size());
				System.out.println("  algo elapsed time: " + algoTime +"s");
				System.out.println("  algo % complete:   " + algoDone );
				System.out.println("  time to complete:  " + timeToComplete + "s");
				System.out.println("");*/
			}

			double value = valueAt(format, x, train);
			Warnings.check(!Double.isNaN(value));
			System.out.println("Epoch " + j + " complete. Objective value is " + value);
			final long epochEndTime = System.currentTimeMillis();
			System.out.println("Epoch time: " + (epochEndTime - epochStartTime)/1000.0  +"s");
			loss[j] = value;  
			if(!checkGrad) {
				String notes = EncoderSaver.makeNotes(train, epochs);
				EncoderSaver.save(x, format, name + "-epoch" + j, notes);
			}
		}
		final long algoEndTime = System.currentTimeMillis();
		System.out.println("Algo time: " + (algoEndTime - algoStartTime)/1000.0 + "s");
		System.out.println("");
		return getModel(format, x);
	}


	private static List<List<TestTriplet>> makeMiniBatches(
			List<TestTriplet> train, int miniBatchSize) {
		List<List<TestTriplet>> miniBatches = new ArrayList<List<TestTriplet>>();
		for(int k = 0; k < train.size(); k += miniBatchSize) {
			int end = Math.min(train.size(), k + miniBatchSize);
			miniBatches.add(train.subList(k, end));
		}
		return miniBatches;
	}


	private static void checkGrad(List<List<TestTriplet>> miniBatches, int t,
			Encoder model) {
		if(GradValidator.validate(model, miniBatches.get(t))) {
			System.out.println("passed mini-batch");
		} else {
			System.out.println("failed mini-batch");
		}

		System.out.println("\n\n");
	}

	private static Encoder getModel(ModelFormat format, double[] params) {
		return ModelVector.vecToModel(format, params);
	}

	private static double valueAt(ModelFormat format, double[] params, List<TestTriplet> train) {
		Encoder model = getModel(format, params);
		return ModelValueAt.valueAt(model, train);
	}
}

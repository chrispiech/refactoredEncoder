package minions.minimizer;

import java.io.ObjectInputStream.GetField;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import util.MatrixUtil;
import edu.stanford.nlp.optimization.SGDMinimizer;
import minions.encoder.EncoderSaver;
import minions.encoder.ModelValueAt;
import minions.encoder.backprop.EncoderBackprop;
import minions.encoder.factory.EncoderFactory;
import minions.encoder.modelVector.ModelVector;
import models.code.TestTriplet;
import models.encoder.EncoderParams;
import models.encoder.ModelFormat;
import models.encoder.encoders.BearModel;
import models.encoder.encoders.Encoder;

public class AdaGrad2 {
	private ModelFormat format;
	
	// some bookkeeping.
	private long algoStartTime;
	private double numMiniBatchUpdates;
	private int updatesDone;
	private String name;
	
	
	public static Encoder train(ModelFormat f, List<TestTriplet> train, int epochs, String name) {
		AdaGrad2 min = new AdaGrad2();
		min.format = f;
		min.name = name;
		int miniBatchSize = 1000;
		double eta = EncoderParams.getLearningRate();
		double[] loss = new double[epochs];
		double[] x = min.SGD(train, epochs, miniBatchSize, eta, loss);
		return ModelVector.vecToModel(min.format, x);
	}
	
	private double[] SGD(List<TestTriplet> train, int epochs, int miniBatchSize, double eta, double[] loss) {
		System.out.println("Training!");
		System.out.println("---------");
		System.out.println("num params: " + format.getDimension().getDimension());
		Encoder init = EncoderFactory.makeInitial(format);
		
		double[] x = ModelVector.modelToVec(init);
		double[] gradStore = new double[x.length];
		
		setup(train, miniBatchSize, epochs);

		System.out.println();
		System.out.println("Traning in progress ...");
		algoStartTime = System.currentTimeMillis();
		for(int j = 0; j < epochs; j++) {
			final long startTime = System.currentTimeMillis();

			Collections.shuffle(train);			
			List<List<TestTriplet>> miniBatches = makeMiniBatches(train,
					miniBatchSize);

			for(int t = 0; t < miniBatches.size(); t++) {
				Encoder model = getModel(x);
				Encoder grad = EncoderBackprop.derivative(model, miniBatches.get(t));
				double[] gradVec =  ModelVector.modelToVec(grad);
				for(int i = 0; i < gradVec.length; i++) {
					if(gradVec[i] == 0) continue;
					gradStore[i] += Math.pow(gradVec[i], 2); 
					double weight = eta/Math.sqrt(gradStore[i]);
					x[i] = (x[i] - weight*gradVec[i]);
				}
				miniBatchOutput(j, t);
			}
			Encoder model = getModel(x);
			double value = ModelValueAt.valueAt(model, train);
			System.out.println("Epoch " + j + " complete. Objective value is " + value);
			final long endTime = System.currentTimeMillis();
			System.out.println("Epoch time: " + (endTime - startTime)/1000 );
			loss[j] = value;  
			String notes = EncoderSaver.makeNotes(train, epochs);
			EncoderSaver.save(x, format, name + "-epoch" + j, notes);
			System.out.println("");
		}

		return x;
	}

	private void miniBatchOutput(int j, int t) {
		updatesDone++;
		long miniBatchEnd = System.currentTimeMillis();
		double algoDone = (100.0 * updatesDone / numMiniBatchUpdates);
		double algoTime = (miniBatchEnd - algoStartTime)/1000.0;
		double todo = numMiniBatchUpdates - updatesDone;
		double timeToComplete = (algoTime / updatesDone) * todo;
		System.out.println("  epoch:             " + j);
		System.out.println("  mini-batch:        " + t);
		System.out.println("  algo elapsed time: " + algoTime +"s");
		System.out.println("  algo % complete:   " + algoDone );
		System.out.println("  time to complete:  " + timeToComplete + "s");
		System.out.println("");
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

	public double[] getInitial() {
		BearModel init = new BearModel(format);
		return ModelVector.modelToVec(init);
	}
	
	public void setup(List<TestTriplet> train, int miniBatchSize, int epochs) {
		
		double numMiniBatches = Math.ceil(1.0 * train.size() / miniBatchSize);
		numMiniBatchUpdates = epochs * numMiniBatches;
		updatesDone = 0;
	}

	public Encoder getModel(double[] params) {
		return ModelVector.vecToModel(format, params);
	}

}

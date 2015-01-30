package minions.minimizer.lemur;

import java.util.List;

import minions.encoder.backprop.EncoderBackprop;
import minions.encoder.modelVector.ModelVector;
import models.code.TestTriplet;
import models.encoder.encoders.Encoder;
import util.Warnings;

public class LemurThread extends Thread {

	public static List<TestTriplet> miniBatch;
	public static double[] modelGrad;

	private double sumError;
	private int numTested;
	private Encoder model;

	public LemurThread(Encoder model) {
		this.model = model;
	}

	@Override
	public void run() {
		while(true) {
			// chose a program to update
			TestTriplet test = getNext();
			if(test == null) break;
			Encoder grad = EncoderBackprop.derivative(model, test);
			double[] gradVec = ModelVector.modelToVec(grad);
			updateModelGrad(gradVec);
			sumError += model.logLoss(test);
			numTested++;
		}
	}

	private static synchronized void updateModelGrad(double[] gradVec) {
		Warnings.check(gradVec.length == modelGrad.length);
		for(int i = 0; i < modelGrad.length; i++) {
			modelGrad[i] += gradVec[i];
		}
	}

	private static synchronized TestTriplet getNext() {
		try {
			return miniBatch.remove(0);
		} catch(IndexOutOfBoundsException e) {
			return null;
		}
	}

	public double getSumError() {
		return sumError;
	}

	public int getTested() {
		return numTested;
	}
}

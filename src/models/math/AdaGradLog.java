package models.math;

import java.io.File;
import java.io.ObjectInputStream.GetField;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.ejml.simple.SimpleMatrix;

import util.FileSystem;
import util.MatrixUtil;
import util.Warnings;
import edu.stanford.nlp.optimization.SGDMinimizer;
import minions.encoder.EncoderSaver;
import minions.encoder.ModelTester;
import minions.encoder.ModelValueAt;
import minions.encoder.backprop.EncoderBackprop;
import minions.encoder.factory.EncoderFactory;
import minions.encoder.modelVector.ModelVector;
import minions.program.PrePostExperimentLoader;
import models.code.TestTriplet;
import models.encoder.EncoderParams;
import models.encoder.ModelFormat;
import models.encoder.encoders.Encoder;
import models.encoder.encoders.models.BearModel;

public class AdaGradLog {


	private static final int EPOCHS = 99999;
	private static final double ETA = 0.1;

	public static SimpleMatrix train(SimpleMatrix f, SimpleMatrix labels) {
		return new AdaGradLog().run(f, labels);
	}
	
	private SimpleMatrix theta;
	private SimpleMatrix gradStore;
	private boolean gradZero = false;

	private SimpleMatrix run(SimpleMatrix f, SimpleMatrix labels) {
		theta = MatrixUtil.randomVector(f.numCols(), 1).transpose();
		gradStore = new SimpleMatrix(theta.numRows(), theta.numCols());
		
		for(int i = 0; i < EPOCHS; i++) {
			for(int j = 0; j < f.numRows(); j++) {
				SimpleMatrix x = f.extractMatrix(j, j+1, 0, f.numCols());
				double label = labels.get(j); 
				update(x, label);
				if(gradZero) return theta;
			}
		}

		return theta;
	}

	private void update(SimpleMatrix x, double label) {
		SimpleMatrix dtheta = derivative(x, label);
		for(int i = 0; i < dtheta.getNumElements(); i++) {
			double di = dtheta.get(i);
			gradStore.set(i, gradStore.get(i) + Math.pow(di, 2)); 
			if(di == 0) continue;
			double weight = ETA/Math.sqrt(gradStore.get(i));
			theta.set(i, theta.get(i) - weight * di);
		}
	}

	private SimpleMatrix derivative(SimpleMatrix x, double label) {
		SimpleMatrix gradVec = new SimpleMatrix(x.getNumElements(), 1);
		for(int i = 0; i < x.getNumElements(); i++) {
			double activation = logistic(x);
			double loss = label - activation;
			double grad = loss * x.get(i);
			gradVec.set(i, grad);
		}
		double len = gradVec.dot(gradVec.transpose());
		double lenPer = len / gradVec.getNumElements();
		System.out.println(lenPer);
		if(lenPer <= 0.0000001) {
			gradZero = true;
		}
		return gradVec;
	}
	
	private double logistic(SimpleMatrix x) {
		double z = theta.mult(x.transpose()).get(0, 0);
		return 1.0 / (1.0 +Math.exp(-z));
	}



}

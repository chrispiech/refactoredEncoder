package minions.minimizer.turtle;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import minions.encoder.GradValidator;
import minions.encoder.ModelTester;
import minions.encoder.ModelValueAt;
import minions.encoder.backprop.TurtleBackprop;
import minions.encoder.modelVector.ModelVector;
import models.code.TestTriplet;
import models.encoder.EncoderParams;
import models.encoder.encoders.Encoder;
import models.encoder.encoders.models.TurtleModel;

import org.apache.commons.math3.util.Pair;
import org.ejml.simple.SimpleMatrix;

import util.FileSystem;
import util.MatrixUtil;
import util.Warnings;

public class TurtleThread extends Thread{

	private static final boolean VALIDATE = false;

	public static List<String> miniBatch = null;

	private Map<String, List<TestTriplet>> testMap;
	private TurtleModel model;
	private double[] modelGrad;
	private File matrixDir;

	private int numTested = 0;
	private double sumError = 0;
	private double sumAccuracy = 0;

	public TurtleThread(
			Map<String, List<TestTriplet>> testMap,
			TurtleModel model,
			double[] modelGrad,
			File matrixDir) {
		this.testMap = testMap;
		this.model = model;
		this.modelGrad = modelGrad;
		this.matrixDir = matrixDir;
	}

	@Override
	public void run() {
		int M = EncoderParams.getM();
		while(true) {
			// chose a program to update
			String matrixKey = getNext();
			if(matrixKey == null) break;

			TurtleMatrix matrixValue = loadMatrix(matrixKey);
			List<TestTriplet> matrixTests = testMap.get(matrixKey);
			SimpleMatrix matrix = matrixValue.x;

			// update for each test
			SimpleMatrix matrixGrad = new SimpleMatrix(M,M);
			for(TestTriplet test : matrixTests) {
				Pair<TurtleModel, SimpleMatrix> grad = TurtleBackprop.getGradient(model, matrix, test);
				double[] model_dxdp = ModelVector.modelToVec(grad.getFirst());
				matrixGrad = matrixGrad.plus(grad.getSecond());
				for(int i = 0; i < modelGrad.length; i++) {
					modelGrad[i] += model_dxdp[i];
				}
				
				Pair<TurtleModel, SimpleMatrix> modelMatrix = 
						new Pair<TurtleModel, SimpleMatrix>(model, matrix);
				if(VALIDATE)validateGrad(modelMatrix, grad, test);

				sumError += getError(model, test, matrix);
				sumAccuracy += getAccuracy(model, test, matrix);
				numTested++;
			}

			// AdaGrad update (and save to disk) for the program matrix
			adaGradMatrix(matrixValue, matrixKey, matrixGrad);
		}
	}

	private void validateGrad(
			Pair<TurtleModel, SimpleMatrix> model,
			Pair<TurtleModel, SimpleMatrix> grad, TestTriplet test) {
		boolean passed = true;
		double[] gradVec = ModelVector.modelToVec(grad.getFirst(), grad.getSecond());
		for(int i = 0; i < gradVec.length; i++) {
			double jPlus = getJPlus(model, test, i);
			double jMinus = getJMinus(model, test, i);
			double truth = finiteDifferenceGrad(jPlus, jMinus);
			//System.out.println(truth);
			double calculated = gradVec[i];
			if(!testDiff(truth, calculated, i)) {
				passed = false;
			}
		}
		if(passed) {
			System.out.println("passed");
		} else {
			throw new RuntimeException("failed");
		}
	}

	//---------

	private double getJMinus(Pair<TurtleModel, SimpleMatrix> model, TestTriplet test, int i) {
		return getJMod(model, test, i, -GradValidator.EPSILON);
	}

	private double getJPlus(Pair<TurtleModel, SimpleMatrix> model, TestTriplet test, int i) {
		return getJMod(model, test, i, GradValidator.EPSILON);
	}

	private double getJMod(Pair<TurtleModel, SimpleMatrix> model,
			TestTriplet test, int index, double mod) {
		double[] x = ModelVector.modelToVec(model.getFirst(), model.getSecond());
		x[index] += mod;
		Pair<TurtleModel, SimpleMatrix> prime = ModelVector.vecToModelMatrix(x);
		return prime.getFirst().logLoss(test, prime.getSecond());
	}

	private double finiteDifferenceGrad(double jPlus, double jMinus) {
		return (jPlus - jMinus) / (2 * GradValidator.EPSILON);
	}

	private boolean testDiff(double truth, double calculated, int elem) {
		double diff = Math.abs(truth - calculated);
		if(diff > GradValidator.OK_DIFF) {
			System.out.println("-------");
			System.out.println("DERIVATIVE WARNING!");
			//System.out.println("astId:  " + test.getId());
			System.out.println("matrix: " + ModelVector.getMatrixForIndex(model.getFormat(), elem));
			System.out.println("elem:   " + elem);
			System.out.println("calc:   " + calculated);
			System.out.println("truth:  " + truth);
			System.out.println("diff:   " + diff);
			if(truth != 0) {
				System.out.println("diffM: " + calculated / truth);
			}
			//EncodeGraph g = test.getEncodeGraph();
			//System.out.println(g.getRunEncodeTreeClone());
			System.out.println("-------");
			return false;
			//throw new RuntimeException("is this derivative correct? ");
		} 
		return true;
	}











	private void adaGradMatrix(TurtleMatrix matrix, String matrixKey, SimpleMatrix grad) {
		matrix.gradStore = matrix.gradStore.elementMult(matrix.gradStore);
		double eta = EncoderParams.getLearningRate();
		for(int i = 0; i < grad.getNumElements(); i++) {
			double v = matrix.gradStore.get(i);
			if(v == 0) continue;
			double weight = eta/Math.sqrt(v);
			double newX = matrix.x.get(i) - weight * v;
			matrix.x.set(i, newX);
		}
		saveMatrix(matrixKey, matrix);
	}

	private void saveMatrix(String s, TurtleMatrix matrixValue) {
		FileSystem.createPath(matrixDir);
		String baseName = matrixDir.getPath() + "/" + s;
		try {
			matrixValue.x.saveToFileBinary(baseName + "_x");
			matrixValue.gradStore.saveToFileBinary(baseName + "_gs");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	

	private SimpleMatrix load(String string) {
		try {
			return SimpleMatrix.loadBinary(string);
		} catch (IOException e) {
			int M = EncoderParams.getM();
			return MatrixUtil.randomMatrix(M, M, EncoderParams.getInitStd());
		}
	}

	private double getAccuracy(TurtleModel model, TestTriplet test,
			SimpleMatrix matrix) {
		models.code.State guess = model.getGuess(test, matrix);
		return ModelTester.calcAccuracyPartialCredit(model.getFormat(), test.getPostcondition(), guess);
	}
	
	private static synchronized String getNext() {
		if(miniBatch.isEmpty()) return null;
		return miniBatch.remove(0);
	}

	private TurtleMatrix loadMatrix(String s) {
		String baseName = matrixDir.getPath() + "/" + s;
		TurtleMatrix v = new TurtleMatrix();
		v.x = load(baseName + "_x");
		v.gradStore = load(baseName + "_gs");
		return v;
	}

	private double getError(TurtleModel model, TestTriplet test,
			SimpleMatrix matrix) {
		return model.logLoss(test, matrix);
	}

	public double getSumError() {
		return sumError;
	}

	public double getSumAccuracy() {
		return sumAccuracy;
	}

	public int getTested() {
		return numTested;
	}





}

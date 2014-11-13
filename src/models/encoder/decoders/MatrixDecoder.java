package models.encoder.decoders;

import java.util.ArrayList;
import java.util.List;

import models.code.State;
import models.code.TestTriplet;
import models.encoder.CodeVector;
import models.encoder.EncoderParams;
import models.encoder.neurons.ValueNeuron;
import models.language.Language;

import org.ejml.simple.SimpleMatrix;

import util.MatrixUtil;
import util.Warnings;

public class MatrixDecoder extends ValueDecoder {

	private int rows;
	private int cols;
	
	public MatrixDecoder(String key, int rows, int cols) {
		this.key = key;
		this.rows = rows;
		this.cols = cols;
		int n = EncoderParams.getCodeVectorSize();
		double std = EncoderParams.getInitStd();
		W = MatrixUtil.randomMatrix(getNumOutputs(), n, std);
		b = MatrixUtil.randomVector(getNumOutputs(), std);
	}

	public MatrixDecoder(String key, SimpleMatrix W, SimpleMatrix b, int r, int c) {
		this.W = W;
		this.b = b;
		this.rows = r;
		this.cols = c;
		this.key = key;
	}
	
	public int getNumOutputs() {
		return rows * cols;
	}
	
	public SimpleMatrix getActivation(SimpleMatrix z) {
		return z;
	}

	public ValueNeuron outActivation(SimpleMatrix cv, String key) {
		SimpleMatrix z = getZ(cv);
		SimpleMatrix a = getActivation(z);
		return new ValueNeuron(z, a, key);
	}
	
	@Override
	public double logLoss(State state, SimpleMatrix stateVector) {
		SimpleMatrix z = getZ(stateVector);
		SimpleMatrix prediction = getActivation(z);
		SimpleMatrix truth = state.getMatrixVector(key);
		SimpleMatrix diff = prediction.minus(truth);
		double loss = 0;
		for(int i = 0; i < diff.getNumElements(); i++) {
			loss += Math.pow(diff.get(i), 2);
		}
		return loss;
	}
}

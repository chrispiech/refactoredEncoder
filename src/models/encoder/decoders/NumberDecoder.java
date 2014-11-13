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

public class NumberDecoder extends ValueDecoder{

	public NumberDecoder(String key, SimpleMatrix W, SimpleMatrix b) {
		this.key = key;
		this.W = W;
		this.b = b;
	}
	
	public NumberDecoder(String key) {
		int n = EncoderParams.getCodeVectorSize();
		double std = EncoderParams.getInitStd();
		W = MatrixUtil.randomMatrix(1, n, std);
		b = MatrixUtil.randomVector(1, std);
		this.key = key;
	}
	
	public SimpleMatrix getActivation(SimpleMatrix z) {
		return z;
	}

	public ValueNeuron outActivation(SimpleMatrix cv, String key) {
		SimpleMatrix z = getZ(cv);
		SimpleMatrix a = getActivation(z);
		return new ValueNeuron(z, a, key);
	}
	
	public int decodeNumber(SimpleMatrix cv) {
		SimpleMatrix z = getZ(cv);
		SimpleMatrix a = getActivation(z);
		Warnings.check(a.getNumElements() == 1);
		double v = a.get(0);
		return (int) Math.round(v);
	}
	
	@Override
	public double logLoss(State state, SimpleMatrix stateVector) {
		SimpleMatrix z = getZ(stateVector);
		SimpleMatrix a = getActivation(z);
		Warnings.check(a.getNumElements() == 1);
		double trueNumber = state.getNumber(key);
		double predictedNumber = a.get(0);
		return Math.pow(trueNumber - predictedNumber, 2);
	}
}

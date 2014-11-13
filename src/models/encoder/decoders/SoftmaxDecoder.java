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

public class SoftmaxDecoder extends ValueDecoder{

	private int numOutputOptions;
	private Language language;

	public static int getTruthIndex(Language l, State truth, String key) {
		String rightAnswer = truth.getChoice(key);
		String[] options = l.getChoiceOptions(key);
		for(int i = 0; i < options.length; i++) {
			String o = options[i];
			if(o.equals(rightAnswer)) {
				return i;
			}
		}
		throw new RuntimeException("none found");

	}

	public SoftmaxDecoder(Language l, String key, SimpleMatrix W, SimpleMatrix b) {
		this.W = W;
		this.b = b;
		this.numOutputOptions = l.getNumOutputOptions(key);
		this.language = l;
		this.key = key;

	}
	public SoftmaxDecoder(Language l, String key) {
		this.numOutputOptions = l.getNumOutputOptions(key);
		int n = EncoderParams.getCodeVectorSize();
		double std = EncoderParams.getInitStd();
		W = MatrixUtil.randomMatrix(numOutputOptions, n, std);
		b = MatrixUtil.randomVector(numOutputOptions, std);
		this.language = l;
		this.key = key;
	}

	public SoftmaxDecoder(SoftmaxDecoder toCopy) {
		W = new SimpleMatrix(toCopy.W);
		b = new SimpleMatrix(toCopy.b);
		numOutputOptions = toCopy.numOutputOptions;
		this.language = toCopy.language;
		this.key = toCopy.key;
	}

	public SimpleMatrix getActivation(SimpleMatrix z) {
		SimpleMatrix a = MatrixUtil.softmax(z);
		MatrixUtil.validate(a);
		return a;
	}

	public int decodeChoice(SimpleMatrix cv) {
		SimpleMatrix softMax = getActivation(getZ(cv));
		Integer argMax = null;
		double max = 0;
		for(int i = 0; i < softMax.getNumElements(); i++) {
			double value = softMax.get(i);
			if(argMax == null || value > max) {
				argMax = i;
				max = value;
			}
		}
		return argMax;
	}

	public ValueNeuron outActivation(SimpleMatrix cv, String key) {
		SimpleMatrix z = getZ(cv);
		SimpleMatrix a = getActivation(z);
		return new ValueNeuron(z, a, key);
	}

	@Override
	public double logLoss(State state, SimpleMatrix stateVector) {
		SimpleMatrix z = getZ(stateVector);
		SimpleMatrix a = getActivation(z);
		int truthIndex = SoftmaxDecoder.getTruthIndex(language, state, key);
		double softmaxK = a.get(truthIndex);
		return -Math.log(softmaxK);
	}

}

package models.encoder.encoders;

import models.encoder.neurons.Neuron;

import org.ejml.simple.SimpleMatrix;

import util.MatrixUtil;
import util.NeuralUtils;

public class Mixer {
	
	private SimpleMatrix W1;
	private SimpleMatrix W2;
	private SimpleMatrix b;

	public Mixer(SimpleMatrix w1, SimpleMatrix w2, SimpleMatrix b) {
		this.W1 = w1;
		this.W2 = w2;
		this.b = b;
	}

	public SimpleMatrix getW1() {
		return W1;
	}

	public SimpleMatrix getW2() {
		return W2;
	}

	public SimpleMatrix getB() {
		return b;
	}

	public SimpleMatrix getPostVector(SimpleMatrix program,
			SimpleMatrix state) {
		SimpleMatrix z = calcZ(program, state);
		return calcA(z);
	}

	public SimpleMatrix activate(Neuron mixerNeuron, SimpleMatrix preVector,
			SimpleMatrix codeVector) {
		SimpleMatrix z = calcZ(codeVector, preVector);
		mixerNeuron.setZ(z);
		mixerNeuron.setActivation(calcA(z));
		return mixerNeuron.getActivation();
	}
	
	private SimpleMatrix calcA(SimpleMatrix z) {
		return NeuralUtils.elementwiseApplyTanh(z);
	}

	private SimpleMatrix calcZ(SimpleMatrix program, SimpleMatrix state) {
		SimpleMatrix z = W1.mult(program).plus(W2.mult(state)).plus(b);
		return z;
	}

	public void setParameters(SimpleMatrix W1, SimpleMatrix W2,
			SimpleMatrix b) {
		this.W1 = W1;
		this.W2 = W2;
		this.b = b;
	}

	public void scale(double d) {
		W1 = W1.scale(d);
		W2 = W2.scale(d);
		b = b.scale(d);
	}
	
	@Override
	public boolean equals(Object o) {
		Mixer other = (Mixer) o;
		if(!MatrixUtil.equals(W1, other.W1)) return false;
		if(!MatrixUtil.equals(W2, other.W2)) return false;
		if(!MatrixUtil.equals(b, other.b)) return false;
		return true;
	}

}

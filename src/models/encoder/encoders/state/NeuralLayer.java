package models.encoder.encoders.state;

import models.encoder.EncoderParams;
import models.encoder.neurons.Neuron;

import org.ejml.simple.SimpleMatrix;

import util.MatrixUtil;
import util.NeuralUtils;

public class NeuralLayer {
	public SimpleMatrix W;
	public SimpleMatrix b;

	public NeuralLayer(SimpleMatrix W, SimpleMatrix b) {
		this.W = W;
		this.b = b;
	}

	public boolean equals(Object o) {
		throw new RuntimeException("not done");
	}

	public SimpleMatrix activate(SimpleMatrix input) {
		SimpleMatrix z = W.mult(input).plus(b);
		return NeuralUtils.elementwiseApplyTanh(z);
	}

	public Neuron activateNeuron(SimpleMatrix v) {
		Neuron n = new Neuron();
		n.setZ(W.mult(v).plus(b));
		n.setActivation(NeuralUtils.elementwiseApplyTanh(n.getZ()));
		return n;
	}

	public double getWeightLoss() { 
		return (EncoderParams.getWeightDecay() / 2.0) * MatrixUtil.norm(W);
	}

}

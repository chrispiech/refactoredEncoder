package models.encoder.encoders.state;

import java.util.*;

import org.ejml.simple.SimpleMatrix;

import util.MatrixUtil;
import util.Warnings;
import minions.encoder.factory.EncoderFactory;
import models.code.State;
import models.encoder.ClusterableMatrix;
import models.encoder.EncoderParams;
import models.encoder.ModelFormat;
import models.encoder.decoders.ValueDecoder;
import models.encoder.neurons.Neuron;


public class DeepStateDecoder implements StateDecoderI{
	
	private NeuralLayer hiddenLayer = null;
	
	private StateDecoder decoder = null;
	private ModelFormat format = null;

	public DeepStateDecoder(ModelFormat f, StateDecoder decoder, NeuralLayer hidden) {
		this.decoder = decoder;
		this.hiddenLayer = hidden;
		this.format = f;
	}

	public DeepStateDecoder(ModelFormat format) {
		this.format = format;
		this.decoder = new StateDecoder(format);
	}

	public State getState(SimpleMatrix sv) {
		SimpleMatrix layer = hiddenLayer.activate(sv);
		return decoder.getState(layer);
	}
	
	@Override
	public boolean equals(Object o) {
		throw new RuntimeException("todo");
	}

	public void scale(double d) {
		throw new RuntimeException("todo");
	}

	public double getLogLoss(State state, SimpleMatrix input) {
		SimpleMatrix h = hiddenLayer.activate(input);
		return decoder.getLogLoss(state, h);
	}

	public StateDecoder getDecoder() {
		return decoder;
	}

	public NeuralLayer getHidden() {
		return hiddenLayer;
	}

	public Neuron activateHidden(SimpleMatrix v) {
		return hiddenLayer.activateNeuron(v);
	}
	
	
	
	
}

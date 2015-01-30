package models.encoder.encoders.state;

import java.util.List;
import java.util.Map;
import java.util.Set;

import models.code.State;
import models.encoder.EncoderParams;
import models.encoder.ModelFormat;
import models.encoder.neurons.StateNeuron;

import org.ejml.simple.SimpleMatrix;

import util.MatrixUtil;
import util.NeuralUtils;

public class DeepStateEncoder implements StateEncoderI {
	
	private ModelFormat format;
	private StateEncoder stateEncoder;
	private NeuralLayer hiddenLayer;

	public DeepStateEncoder(ModelFormat format, StateEncoder e, NeuralLayer l) {
		this.format = format;
		this.stateEncoder = e;
		this.hiddenLayer = l;
	}
	
	@Override
	public boolean equals(Object o) {
		throw new RuntimeException("todo");
	}

	public NeuralLayer getHidden() {
		return hiddenLayer;
	}
	
	public StateEncoder getEncoder() {
		return stateEncoder;
	}

	public SimpleMatrix getVector(State state) {
		SimpleMatrix h = stateEncoder.getVector(state);
		return hiddenLayer.activate(h);
	}
}

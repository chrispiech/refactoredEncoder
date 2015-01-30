package models.encoder.encoders.models;

import models.code.State;
import models.code.TestTriplet;
import models.encoder.ClusterableMatrix;
import models.encoder.ModelFormat;
import models.encoder.decoders.ValueDecoder;
import models.encoder.encoders.Encoder;
import models.encoder.encoders.programEncoder.ProgramEncoder;
import models.encoder.encoders.state.StateDecoder;
import models.encoder.encoders.state.StateEncoder;

import org.ejml.simple.SimpleMatrix;

public class BeeModel implements Encoder, StateEncodable{
	
	private StateDecoder stateDecoder = null;
	private StateEncoder stateEncoder = null;
	private ModelFormat format = null;
	
	public BeeModel(ModelFormat format, StateDecoder output, StateEncoder input) {
		this.format = format;
		this.stateDecoder = output;
		this.stateEncoder = input;
	}

	@Override
	public double logLoss(TestTriplet test) {
		double logLoss = 0;
		//logLoss += getLogLoss(test.getPrecondition());
		logLoss += getLogLoss(test.getPostcondition(), test.getCount());
		
		if(Double.isNaN(logLoss)) {
			throw new RuntimeException("nan loss");
		}
		return logLoss;
	}

	private double getLogLoss(State state, int weight) {
		SimpleMatrix stateVector = stateEncoder.getVector(state);
		return stateDecoder.getLogLoss(state, stateVector, weight);
	}
	
	@Override
	public State getAutoEncode(State state) {
		SimpleMatrix stateVector = stateEncoder.getVector(state);
		return stateDecoder.getState(stateVector);
	}

	@Override
	public State getOutput(TestTriplet test) {
		return getAutoEncode(test.getPostcondition());
	}

	@Override
	public ProgramEncoder getProgramEncoder() {
		throw new RuntimeException("illegal");
	}

	@Override
	public StateDecoder getStateDecoder() {
		return stateDecoder;
	}
	
	public StateEncoder getStateEncoder() {
		return stateEncoder;
	}

	public ValueDecoder getOutputDecoder(String key) {
		return stateDecoder.getOutputDecoder(key);
	}

	@Override
	public ModelFormat getFormat() {
		return format;
	}

	public void scale(double d) {
		stateDecoder.scale(d);
		stateEncoder.scale(d);
	}

	@Override
	public SimpleMatrix getCodeEmbedding(TestTriplet test) {
		throw new RuntimeException("not a good ask");
	}

	@Override
	public SimpleMatrix getVector(State state) {
		return stateEncoder.getVector(state);
	}

}

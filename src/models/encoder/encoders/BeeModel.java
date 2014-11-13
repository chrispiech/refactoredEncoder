package models.encoder.encoders;

import java.util.List;

import org.ejml.simple.SimpleMatrix;

import util.Warnings;
import models.code.State;
import models.code.TestTriplet;
import models.encoder.ModelFormat;
import models.encoder.decoders.ValueDecoder;
import models.encoder.encoders.types.StateEncodable;

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
	public double logLoss(List<TestTriplet> tests) {
		double sumLoss = 0;
		for(TestTriplet t : tests) {
			sumLoss += logLoss(t);
		}
		int m = (tests.size() * format.getNumOutputs());
		return sumLoss / m;
	}

	@Override
	public double logLoss(TestTriplet test) {
		double logLoss = 0;
		logLoss += getLogLoss(test.getPrecondition());
		logLoss += getLogLoss(test.getPostcondition());
		
		if(Double.isNaN(logLoss)) {
			throw new RuntimeException("nan loss");
		}
		return logLoss;
	}

	private double getLogLoss(State state) {
		SimpleMatrix stateVector = stateEncoder.getVector(state);
		return stateDecoder.getLogLoss(state, stateVector);
	}
	
	@Override
	public State getAutoEncode(State state) {
		SimpleMatrix stateVector = stateEncoder.getVector(state);
		return stateDecoder.getState(stateVector);
	}

	@Override
	public State getOutput(TestTriplet test) {
		throw new RuntimeException("todo");
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

	@Override
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

}

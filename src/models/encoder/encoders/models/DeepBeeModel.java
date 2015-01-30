package models.encoder.encoders.models;

import models.code.State;
import models.code.TestTriplet;
import models.encoder.ModelFormat;
import models.encoder.decoders.ValueDecoder;
import models.encoder.encoders.Encoder;
import models.encoder.encoders.programEncoder.ProgramEncoder;
import models.encoder.encoders.state.DeepStateDecoder;
import models.encoder.encoders.state.DeepStateEncoder;
import models.encoder.encoders.state.StateDecoder;
import models.encoder.encoders.state.StateEncoder;

import org.ejml.simple.SimpleMatrix;

public class DeepBeeModel implements Encoder, StateEncodable{
	
	private DeepStateDecoder stateDecoder = null;
	private DeepStateEncoder stateEncoder = null;
	private ModelFormat format = null;
	
	public DeepBeeModel(ModelFormat format, DeepStateDecoder output, DeepStateEncoder input) {
		this.format = format;
		this.stateDecoder = output;
		this.stateEncoder = input;
	}

	@Override
	public double logLoss(TestTriplet test) {
		double logLoss = 0;
		//logLoss += getLogLoss(test.getPrecondition());
		logLoss += getLogLoss(test.getPostcondition());
		
		if(Double.isNaN(logLoss)) {
			throw new RuntimeException("nan loss");
		}
		return logLoss;
	}

	private double getLogLoss(State state) {
		SimpleMatrix stateVector = stateEncoder.getVector(state);
		
		double logLoss = 0;
		logLoss += stateDecoder.getLogLoss(state, stateVector);
		logLoss += getWeightLoss();
		return logLoss;
	}
	
	private double getWeightLoss() {
		double weightLoss = 0;
		weightLoss += stateDecoder.getDecoder().getWeightLoss();
		weightLoss += stateEncoder.getEncoder().getWeightLoss();
		weightLoss += stateDecoder.getHidden().getWeightLoss();
		weightLoss += stateEncoder.getHidden().getWeightLoss();
		return weightLoss;
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
	public DeepStateDecoder getStateDecoder() {
		return stateDecoder;
	}

	@Override
	public ModelFormat getFormat() {
		return format;
	}

	public void scale(double d) {
		//stateDecoder.scale(d);
		//stateEncoder.scale(d);
		throw new RuntimeException("not done");
	}

	@Override
	public SimpleMatrix getCodeEmbedding(TestTriplet test) {
		throw new RuntimeException("not a good ask");
	}

	@Override
	public DeepStateEncoder getStateEncoder() {
		return stateEncoder;
	}

	@Override
	public ValueDecoder getOutputDecoder(String key) {
		return stateDecoder.getDecoder().getOutputDecoder(key);
	}
	

}

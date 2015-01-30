package models.encoder.encoders.models;

import models.code.State;
import models.code.TestTriplet;
import models.encoder.EncodeGraph;
import models.encoder.ModelFormat;
import models.encoder.decoders.ValueDecoder;
import models.encoder.encoders.Encoder;
import models.encoder.encoders.programEncoder.MatrixFarm;
import models.encoder.encoders.programEncoder.ProgramEncoder;
import models.encoder.encoders.programEncoder.ProgramEncoderMatrix;
import models.encoder.encoders.state.StateDecoder;
import models.encoder.encoders.state.StateEncoder;
import models.encoder.neurons.TreeNeuron;

import org.ejml.simple.SimpleMatrix;

import util.Warnings;

public class TurtleModel implements Encoder, StateEncodable{

	private StateDecoder stateDecoder = null;
	private StateEncoder stateEncoder = null;
	private MatrixFarm matrixFarm = null;
	private ModelFormat format = null;
	
	public TurtleModel(ModelFormat format, 
			StateDecoder output, StateEncoder input) {
		this.format = format;
		this.stateDecoder = output;
		this.stateEncoder = input;
	}

	@Override
	public StateEncoder getStateEncoder() {
		return stateEncoder;
	}

	@Override
	public State getAutoEncode(State state) {
		throw new RuntimeException("not done");
	}

	@Override
	public double logLoss(TestTriplet t) {
		throw new RuntimeException("bad ask");
	}
	
	public double logLoss(TestTriplet t, SimpleMatrix matrix) {
		State pre = t.getPrecondition();
		State post = t.getPostcondition();
		SimpleMatrix preVector = stateEncoder.getVector(pre);
		SimpleMatrix postVector = getPostVector(preVector, matrix);

		double logLoss = 0;
		
		//logLoss += stateDecoder.getLogLoss(pre, preVector);
		logLoss += stateDecoder.getLogLoss(post, postVector);
		logLoss += getWeightLoss();
		
		if(Double.isNaN(logLoss)) {
			throw new RuntimeException("nan loss");
		}
		return logLoss;
	}

	private double getWeightLoss() {
		double weightLoss = 0;
		Warnings.msg("should I care about weight loss of programs?");
		//weightLoss += programEncoder.getWeightLoss();
		weightLoss += stateDecoder.getWeightLoss();
		weightLoss += stateEncoder.getWeightLoss();
		return weightLoss;
	}

	@Override
	public State getOutput(TestTriplet test) {
		throw new RuntimeException("bad ask");
	}

	@Override
	public ProgramEncoder getProgramEncoder() {
		throw new RuntimeException("not a good ask");
	}

	@Override
	public StateDecoder getStateDecoder() {
		return stateDecoder;
	}

	public ValueDecoder getOutputDecoder(String key) {
		return stateDecoder.getOutputDecoder(key);
	}

	@Override
	public ModelFormat getFormat() {
		return format;
	}

	@Override
	public SimpleMatrix getCodeEmbedding(TestTriplet test) {
		throw new RuntimeException("not done");
	}

	public SimpleMatrix getPostVector(SimpleMatrix preVector,
			SimpleMatrix codeMatrix) {
		return codeMatrix.mult(preVector);
	}

	public void scale(double d) {
		/*programEncoder.scale(d);
		stateDecoder.scale(d);
		stateEncoder.scale(d);*/
		throw new RuntimeException("not sure what this means");
	}


	public State getGuess(TestTriplet test, SimpleMatrix matrix) {
		State pre = test.getPrecondition();
		SimpleMatrix preVector = stateEncoder.getVector(pre);
		SimpleMatrix postVector = getPostVector(preVector, matrix);
		return stateDecoder.getState(postVector);
	}


}

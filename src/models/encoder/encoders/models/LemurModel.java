package models.encoder.encoders.models;

import models.code.State;
import models.code.TestTriplet;
import models.encoder.ClusterableMatrix;
import models.encoder.EncodeGraph;
import models.encoder.ModelFormat;
import models.encoder.decoders.ValueDecoder;
import models.encoder.encoders.Encoder;
import models.encoder.encoders.programEncoder.ProgramEncoder;
import models.encoder.encoders.programEncoder.ProgramEncoderMatrix;
import models.encoder.encoders.state.StateDecoder;
import models.encoder.encoders.state.StateEncoder;
import models.encoder.neurons.TreeNeuron;

import org.ejml.simple.SimpleMatrix;

public class LemurModel implements Encoder, StateEncodable{

	private ProgramEncoderMatrix programEncoder = null;
	private StateDecoder stateDecoder = null;
	private StateEncoder stateEncoder = null;
	private ModelFormat format = null;

	public LemurModel(ModelFormat format, ProgramEncoderMatrix program,
			StateDecoder output, StateEncoder input) {
		this.format = format;
		this.stateDecoder = output;
		this.stateEncoder = input;
		this.programEncoder = program;
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
		State pre = t.getPrecondition();
		State post = t.getPostcondition();
		EncodeGraph encodeGraph = t.getEncodeGraph();
		TreeNeuron runTree = encodeGraph.getEffectiveTree(t.getNodeId());
		//SimpleMatrix preVector = stateEncoder.getVector(pre);
		SimpleMatrix postVector = getPostVector(pre, runTree);

		double logLoss = 0;

		//logLoss += stateDecoder.getLogLoss(pre, preVector);
		logLoss += stateDecoder.getLogLoss(post, postVector, t.getCount());
		logLoss += getWeightLoss();

		if(Double.isNaN(logLoss)) {
			throw new RuntimeException("nan loss");
		}
		return logLoss;
	}

	private double getWeightLoss() {
		double weightLoss = 0;
		weightLoss += programEncoder.getWeightLoss();
		weightLoss += stateDecoder.getWeightLoss();
		weightLoss += stateEncoder.getWeightLoss();
		return weightLoss;
	}

	@Override
	public State getOutput(TestTriplet test) {
		EncodeGraph g = test.getEncodeGraph();
		TreeNeuron r = g.getEffectiveTree(test.getNodeId());
		State pre = test.getPrecondition();
		SimpleMatrix postEncoding = getPostVector(pre, r);
		return stateDecoder.getState(postEncoding);
	}

	@Override
	public ProgramEncoder getProgramEncoder() {
		return programEncoder;
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

	private SimpleMatrix getPostVector(State pre, TreeNeuron runTree) {
		SimpleMatrix preEmbedding = stateEncoder.getVector(pre);
		SimpleMatrix codeEmbedding = programEncoder.activateTree(runTree);
		return getPostVector(preEmbedding, codeEmbedding);
	}

	public SimpleMatrix getPostVector(SimpleMatrix preVector,
			SimpleMatrix codeMatrix) {
		return codeMatrix.mult(preVector);
	}

	public void scale(double d) {
		programEncoder.scale(d);
		stateDecoder.scale(d);
		stateEncoder.scale(d);
	}

	@Override
	public SimpleMatrix getVector(State state) {
		throw new RuntimeException("bad ask");
	}

}

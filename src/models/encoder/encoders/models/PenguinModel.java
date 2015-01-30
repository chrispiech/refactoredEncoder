package models.encoder.encoders.models;

import java.util.List;

import org.ejml.simple.SimpleMatrix;

import models.code.State;
import models.code.TestTriplet;
import models.encoder.ClusterableMatrix;
import models.encoder.EncodeGraph;
import models.encoder.ModelFormat;
import models.encoder.decoders.ValueDecoder;
import models.encoder.encoders.Encoder;
import models.encoder.encoders.Mixer;
import models.encoder.encoders.programEncoder.ProgramEncoderVec;
import models.encoder.encoders.state.StateDecoder;
import models.encoder.encoders.state.StateEncoder;
import models.encoder.neurons.TreeNeuron;

public class PenguinModel implements Encoder, StateEncodable  {

	private ModelFormat format;
	private ProgramEncoderVec programEncoder;
	private StateDecoder stateDecoder;
	private StateEncoder stateEncoder;
	private Mixer combiner;

	public PenguinModel(
			ModelFormat format, ProgramEncoderVec program,
			StateDecoder output, StateEncoder input,
			Mixer combiner) {
		this.programEncoder = program;
		this.format = format;
		this.stateDecoder = output;
		this.stateEncoder = input;
		this.combiner = combiner;
	}

	@Override
	public StateEncoder getStateEncoder() {
		return stateEncoder;
	}

	@Override
	public State getAutoEncode(State state) {
		SimpleMatrix stateVector = stateEncoder.getVector(state);
		return stateDecoder.getState(stateVector);
	}

	@Override
	public double logLoss(TestTriplet t) {
		State pre = t.getPrecondition();
		State post = t.getPostcondition();
		EncodeGraph encodeGraph = t.getEncodeGraph();
		TreeNeuron runTree = encodeGraph.getRunEncodeTreeClone();
		SimpleMatrix preVector = stateEncoder.getVector(pre);

		SimpleMatrix codeVector = programEncoder.activateTree(runTree);

		SimpleMatrix postVector = getPostVector(preVector, codeVector);

		double logLoss = 0;
		logLoss += stateDecoder.getLogLoss(pre, preVector);
		logLoss += stateDecoder.getLogLoss(post, postVector);

		if(Double.isNaN(logLoss)) {
			throw new RuntimeException("nan loss");
		}
		return logLoss;
	}

	@Override
	public State getOutput(TestTriplet test) {
		State pre = test.getPrecondition();
		EncodeGraph graph = test.getEncodeGraph();
		TreeNeuron runTree = graph.getRunEncodeTreeClone();
		return getOutput(pre, runTree);
	}

	private State getOutput(State pre, TreeNeuron et) {
		SimpleMatrix preVector = stateEncoder.getVector(pre);
		SimpleMatrix code = programEncoder.activateTree(et);
		SimpleMatrix postVector = getPostVector(preVector, code);
		return stateDecoder.getState(postVector);
	}

	public SimpleMatrix getPostVector(SimpleMatrix stateVector,
			SimpleMatrix programVector) {
		return combiner.getPostVector(programVector, stateVector);
	}
	
	@Override
	public SimpleMatrix getCodeEmbedding(TestTriplet test) {
		EncodeGraph graph = test.getEncodeGraph();
		TreeNeuron encodeTree = graph.getRunEncodeTreeClone();
		return programEncoder.activateTree(encodeTree);
	}


	@Override
	public ProgramEncoderVec getProgramEncoder() {
		return programEncoder;
	}

	@Override
	public StateDecoder getStateDecoder() {
		return stateDecoder;
	}

	@Override
	public ValueDecoder getOutputDecoder(String key) {
		return stateDecoder.getOutputDecoder(key);
	}

	@Override
	public ModelFormat getFormat() {
		return format;
	}

	public Mixer getCombiner() {
		return combiner;
	}

	public void scale(double d) {
		programEncoder.scale(d);
		stateDecoder.scale(d);
		stateEncoder.scale(d);
		combiner.scale(d);
	}

	@Override
	public boolean equals(Object o) {
		PenguinModel other = (PenguinModel)o;
		if(!programEncoder.equals(other.programEncoder)) return false;
		if(!stateDecoder.equals(other.stateDecoder)) return false;
		if(!stateEncoder.equals(other.stateEncoder)) return false;
		if(!combiner.equals(other.combiner)) return false;
		if(!format.equals(other.format)) return false;
		return true;
	}


}

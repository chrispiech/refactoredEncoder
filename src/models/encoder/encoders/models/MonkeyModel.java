package models.encoder.encoders.models;

import models.code.State;
import models.code.TestTriplet;
import models.encoder.ClusterableMatrix;
import models.encoder.EncodeGraph;
import models.encoder.EncoderParams;
import models.encoder.ModelFormat;
import models.encoder.decoders.ValueDecoder;
import models.encoder.encoders.Encoder;
import models.encoder.encoders.programEncoder.ProgramEncoder;
import models.encoder.encoders.programEncoder.ProgramEncoderVec;
import models.encoder.encoders.state.StateDecoder;
import models.encoder.encoders.state.StateEncoder;
import models.encoder.neurons.TreeNeuron;

import org.ejml.simple.SimpleMatrix;

import util.Warnings;

public class MonkeyModel implements Encoder, StateEncodable {
	
	private ProgramEncoderVec programEncoder = null;
	private StateDecoder stateDecoder = null;
	private StateEncoder stateEncoder = null;
	private ModelFormat format = null;
	
	public MonkeyModel(ModelFormat format, ProgramEncoderVec program,
			StateDecoder output, StateEncoder input) {
		this.programEncoder = program;
		this.format = format;
		this.stateDecoder = output;
		this.stateEncoder = input;
	}
	
	@Override
	public double logLoss(TestTriplet t) {
		State pre = t.getPrecondition();
		State post = t.getPostcondition();
		EncodeGraph encodeGraph = t.getEncodeGraph();
		TreeNeuron runTree = encodeGraph.getRunEncodeTreeClone();
		SimpleMatrix preVector = stateEncoder.getVector(pre);
		SimpleMatrix postVector = getPostVector(pre, runTree);

		double logLoss = 0;
		
		logLoss += stateDecoder.getLogLoss(pre, preVector);
		logLoss += stateDecoder.getLogLoss(post, postVector);
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
		State pre = test.getPrecondition();
		EncodeGraph graph = test.getEncodeGraph();
		TreeNeuron runTree = graph.getRunEncodeTreeClone();
		return getOutput(pre, runTree);
	}
	
	private State getOutput(State pre, TreeNeuron et) {
		SimpleMatrix postVector = getPostVector(pre, et);
		return stateDecoder.getState(postVector);
	}
	
	@Override
	public SimpleMatrix getCodeEmbedding(TestTriplet test) {
		EncodeGraph graph = test.getEncodeGraph();
		TreeNeuron et = graph.getRunEncodeTreeClone();		
		return programEncoder.activateTree(et);
	}
	
	private SimpleMatrix getPostVector(State pre, TreeNeuron et) {
		TreeNeuron clone = new TreeNeuron(et);
		SimpleMatrix preVector = stateEncoder.getVector(pre);
		SimpleMatrix codeVector = programEncoder.activateTree(clone);
		return getPostVector(preVector, codeVector);
	}
	
	public SimpleMatrix getPostVector(SimpleMatrix preVec, SimpleMatrix codeVec) {
		return reshape(codeVec).mult(preVec);
	}

	@Override
	public ProgramEncoder getProgramEncoder() {
		return programEncoder;
	}
	
	@Override
	public ValueDecoder getOutputDecoder(String key) {
		return stateDecoder.getOutputDecoder(key);
	}
	
	@Override
	public ModelFormat getFormat() {
		return format;
	}

	public StateDecoder getStateDecoder() {
		return stateDecoder;
	}
	
	public StateEncoder getStateEncoder() {
		return stateEncoder;
	}
	
	public void scale(double d) {
		programEncoder.scale(d);
		stateDecoder.scale(d);
		stateEncoder.scale(d);
	}
	
	@Override
	public boolean equals(Object o) {
		MonkeyModel other = (MonkeyModel)o;
		if(!programEncoder.equals(other.programEncoder)) return false;
		if(!stateDecoder.equals(other.stateDecoder)) return false;
		if(!stateEncoder.equals(other.stateEncoder)) return false;
		if(!format.equals(other.format)) return false;
		return true;
	}

	public static SimpleMatrix reshape(SimpleMatrix v) {
		int sqrtN = EncoderParams.getSqrtN();
		Warnings.check(v.getNumElements() == sqrtN * sqrtN);
		SimpleMatrix codeMatrix = new SimpleMatrix(v);
		codeMatrix.reshape(sqrtN, sqrtN);
		return codeMatrix;
	}

	public static SimpleMatrix unshape(SimpleMatrix m) {
		int sqrtN = EncoderParams.getSqrtN();
		int n = EncoderParams.getN();
		Warnings.check(m.numCols() == sqrtN);
		Warnings.check(m.numRows() == sqrtN);
		SimpleMatrix v = new SimpleMatrix(m);
		v.reshape(n, 1);
		return v;
	}

	@Override
	public State getAutoEncode(State state) {
		SimpleMatrix stateVector = stateEncoder.getVector(state);
		return stateDecoder.getState(stateVector);
	}
	
}


package models.encoder.encoders.models;

import org.ejml.simple.SimpleMatrix;

import models.code.State;
import models.code.TestTriplet;
import models.encoder.ClusterableMatrix;
import models.encoder.EncodeGraph;
import models.encoder.ModelFormat;
import models.encoder.decoders.ValueDecoder;
import models.encoder.encoders.Encoder;
import models.encoder.encoders.programEncoder.ProgramEncoder;
import models.encoder.encoders.programEncoder.ProgramEncoderVec;
import models.encoder.encoders.state.StateDecoder;
import models.encoder.neurons.TreeNeuron;
import models.language.Language;

public class BearModel implements Encoder{

	private ProgramEncoderVec programEncoder = null;
	private StateDecoder outputDecoder = null;	
	private ModelFormat format = null;


	public BearModel(ModelFormat format) {
		this.format = format;
		this.programEncoder = new ProgramEncoderVec(format);
		this.outputDecoder = new StateDecoder(format);
	}

	public BearModel(ModelFormat format, ProgramEncoderVec program,
			StateDecoder output) {
		this.format = format;
		this.programEncoder = program;
		this.outputDecoder = output;
	}
	
	@Override
	public SimpleMatrix getCodeEmbedding(TestTriplet test) {
		EncodeGraph g = test.getEncodeGraph();
		TreeNeuron runTree = g.getRunEncodeTreeClone();
		return programEncoder.activateTree(runTree);
	}

	public State getOutput(TestTriplet test) {
		SimpleMatrix cv = getCodeEmbedding(test);
		return outputDecoder.getState(cv);
	}

	@Override
	public ProgramEncoder getProgramEncoder() {
		return programEncoder;
	}

	@Override
	public ModelFormat getFormat() {
		return format;
	}

	public ValueDecoder getOutputDecoder(String outKey) {
		return outputDecoder.getOutputDecoder(outKey);
	}

	@Override
	public boolean equals(Object o) {
		BearModel other = (BearModel)o;
		if(!programEncoder.equals(other.programEncoder)) return false;
		if(!outputDecoder.equals(other.outputDecoder)) return false;
		if(!format.equals(other.format)) return false;
		return true;
	}

	public void scale(double d) {
		programEncoder.scale(d);
		outputDecoder.scale(d);
	}
	
	public Language getLanguage() {
		return format.getLanguage();
	}


	//*********************************************************************************
	//*                 LOG LOSS
	//*********************************************************************************

	public double logLoss(TestTriplet t) {
		EncodeGraph encodeGraph = t.getEncodeGraph();
		TreeNeuron runTree = encodeGraph.getRunEncodeTreeClone();
		SimpleMatrix cv = programEncoder.activateTree(runTree);
		State output = t.getPostcondition();
		double logLoss = outputDecoder.getLogLoss(output, cv);
		double weightLoss = getWeightLoss();
		if(Double.isNaN(logLoss)) {
			throw new RuntimeException("nan loss");
		}
		return logLoss + weightLoss;
	}

	private double getWeightLoss() {
		return programEncoder.getWeightLoss() +
				outputDecoder.getWeightLoss();
	}

	public StateDecoder getStateDecoder() {
		return outputDecoder;
	}

	





}

package models.encoder.encoders;

import java.util.List;

import models.code.State;
import models.code.TestTriplet;
import models.encoder.ModelFormat;
import models.encoder.decoders.ValueDecoder;
import models.encoder.encoders.programEncoder.ProgramEncoder;
import models.encoder.encoders.state.StateDecoderI;

import org.ejml.simple.SimpleMatrix;

public interface Encoder {
	
	public double logLoss(TestTriplet test);
	
	public State getOutput(TestTriplet test);

	public ProgramEncoder getProgramEncoder();
	
	public StateDecoderI getStateDecoder();

	public ModelFormat getFormat();

	public SimpleMatrix getCodeEmbedding(TestTriplet test);

	public ValueDecoder getOutputDecoder(String key);


	

}

package models.encoder.encoders;

import java.util.List;

import models.code.State;
import models.code.TestTriplet;
import models.encoder.CodeVector;
import models.encoder.EncodeGraph;
import models.encoder.ModelFormat;
import models.encoder.decoders.ValueDecoder;

public interface Encoder {

	public double logLoss(List<TestTriplet> tests);
	
	public double logLoss(TestTriplet test);
	
	public State getOutput(TestTriplet test);

	public ProgramEncoder getProgramEncoder();
	
	public StateDecoder getStateDecoder();
	
	public ValueDecoder getOutputDecoder(String key);

	public ModelFormat getFormat();

}

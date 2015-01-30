package models.encoder.encoders.models;

import org.ejml.simple.SimpleMatrix;

import models.code.State;
import models.encoder.ModelFormat;
import models.encoder.encoders.state.StateEncoder;
import models.encoder.encoders.state.StateEncoderI;

public interface StateEncodable {

	ModelFormat getFormat();

	StateEncoderI getStateEncoder();
	
	State getAutoEncode(State state);

	SimpleMatrix getVector(State state);

}

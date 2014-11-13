package models.encoder.encoders.types;

import models.code.State;
import models.encoder.ModelFormat;
import models.encoder.encoders.StateEncoder;

public interface StateEncodable {

	ModelFormat getFormat();

	StateEncoder getStateEncoder();
	
	State getAutoEncode(State state);

}

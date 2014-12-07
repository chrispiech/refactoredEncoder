package minions.encoder;

import java.util.Collections;
import java.util.List;

import minions.encoder.modelVector.ModelVector;
import models.code.TestTriplet;
import models.encoder.EncoderParams;
import models.encoder.encoders.Encoder;

public class ModelValueAt {

	public static double valueAt(Encoder m, List<TestTriplet> tests) {
		double loss = 0;
		for(TestTriplet t : tests) {
			loss += m.logLoss(t);
		}
		return loss / tests.size(); //return average loss
	}


}

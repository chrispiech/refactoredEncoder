package minions.encoder;

import java.util.Collections;
import java.util.List;

import minions.encoder.modelVector.ModelVector;
import models.code.TestTriplet;
import models.encoder.EncoderParams;
import models.encoder.encoders.Encoder;

public class ModelValueAt {

	public static double valueAt(Encoder m, List<TestTriplet> tests) {
		return m.logLoss(tests);
	}
	
	public static double valueAt(Encoder m, TestTriplet test) {
		return valueAt(m, Collections.singletonList(test));
	}
	
	public static double valueAtWithDecay(Encoder m, List<TestTriplet> tests) {
		double norm = getNorm(m);
		double value = valueAt(m, tests);
		return value + EncoderParams.getWeightDecay() / 2 * norm;
	}

	private static double getNorm(Encoder m) {
		double[] vec = ModelVector.modelToVec(m);
		double norm = 0;
		for(int i = 0; i < vec.length; i++) {
			norm += Math.pow(vec[i], 2);
		}
		return norm;
	}


}

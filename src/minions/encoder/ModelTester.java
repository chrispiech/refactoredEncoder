package minions.encoder;

import java.util.List;

import models.code.State;
import models.code.TestTriplet;
import models.encoder.encoders.Encoder;
import models.encoder.encoders.types.StateEncodable;

import org.ejml.simple.SimpleMatrix;

import util.MatrixUtil;

public class ModelTester {

	public static double calcAccuracy(Encoder model, List<TestTriplet> tests) {
		int numCorrect = 0;
		int numTested = tests.size();
		for(TestTriplet t : tests) {
			State guess = model.getOutput(t);
			State truth = t.getPostcondition();
			if(guess.equals(truth)) numCorrect++;
		}
		return (100.0 * numCorrect) / numTested;
	}
	
	public static double calcAccuracyPartialCredit(Encoder m, List<TestTriplet> ts) {
		int correct = 0;
		int tested = 0;
		for(TestTriplet t : ts) {
			State guess = m.getOutput(t);
			State truth = t.getPostcondition();
			for(String key : guess.getKeys()) {
				SimpleMatrix a = guess.getActivation(m.getFormat(), key);
				SimpleMatrix b = truth.getActivation(m.getFormat(), key);
				if(MatrixUtil.equals(a, b)) {
					correct++;
				}
				tested++;
			}
		}
		return (100.0 * correct) / tested;
	}
	
	public static double stateAutoEncoderAccuracy(StateEncodable model, List<TestTriplet> tests) {
		int numCorrect = 0;
		int numTested = 0;
		for(TestTriplet t : tests) {
			State pre = t.getPrecondition();
			State preGuess = model.getAutoEncode(pre);
			if(preGuess.equals(pre)) numCorrect++;
			
			State post = t.getPostcondition();
			State postGuess = model.getAutoEncode(post);
			if(postGuess.equals(post)) numCorrect++;
			
			numTested+=2;
		}
		return (100.0 * numCorrect) / numTested;
	}

}

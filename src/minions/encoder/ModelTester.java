package minions.encoder;

import java.util.List;

import models.code.State;
import models.code.TestTriplet;
import models.encoder.ModelFormat;
import models.encoder.encoders.Encoder;
import models.encoder.encoders.models.StateEncodable;

import org.ejml.simple.SimpleMatrix;

import util.MatrixUtil;
import util.Warnings;

public class ModelTester {

	public static double calcAccuracy(Encoder model, List<TestTriplet> tests) {
		int numCorrect = 0;
		int numTested = tests.size();
		Warnings.error("should check rounded");
		for(TestTriplet t : tests) {
			State guess = model.getOutput(t);
			State truth = t.getPostcondition();
			if(guess.equals(truth)) numCorrect++;
		}
		return (100.0 * numCorrect) / numTested;
	}
	
	public static double calcAccuracyPartialCredit(ModelFormat f, State truth, State guess) {
		int correct = 0;
		int tested = 0;
		for(String key : guess.getKeys()) {
			if(f.getOutputType(key).equals("choice")) {
				if(truth.getChoice(key).equals(guess.getChoice(key))) {
					correct++;
				}
				tested++;
			} else {
				SimpleMatrix a = guess.getActivation(f, key);
				SimpleMatrix b = truth.getActivation(f, key);

				for(int r = 0; r < a.numRows(); r++) {
					for(int c = 0; c < a.numCols(); c++) {
						double v1 = Math.round(a.get(r,c));
						double v2 = Math.round(b.get(r,c));
						if(v1 == v2) {
							correct++;
						} 
						tested++;
					}
				}
			}
		}
		return (100.0 * correct) / tested;
	}

	public static double calcAccuracyPartialCredit(Encoder m, List<TestTriplet> ts) {
		int correct = 0;
		int tested = 0;
		for(TestTriplet t : ts) {
			State guess = m.getOutput(t);
			State truth = t.getPostcondition();
			for(String key : guess.getKeys()) {
				if(m.getFormat().getOutputType(key).equals("choice")) {
					SimpleMatrix a = guess.getActivation(m.getFormat(), key);
					SimpleMatrix b = truth.getActivation(m.getFormat(), key);
					if(MatrixUtil.equals(a, b)) {
						correct++;
					}
					tested++;
				} else {
					SimpleMatrix a = guess.getActivation(m.getFormat(), key);
					SimpleMatrix b = truth.getActivation(m.getFormat(), key);

					for(int r = 0; r < a.numRows(); r++) {
						for(int c = 0; c < a.numCols(); c++) {
							double v1 = Math.round(a.get(r,c));
							double v2 = Math.round(b.get(r,c));
							if(v1 == v2) {
								correct++;
							} 
							tested++;
						}
					}
				}
			}
		}
		return (100.0 * correct) / tested;
	}

	private static boolean roundedEquals(SimpleMatrix a, SimpleMatrix b) {
		if(a.numRows() != b.numRows()) return false;
		if(a.numCols() != b.numCols()) return false;
		for(int r = 0; r < a.numRows(); r++) {
			for(int c = 0; c < a.numCols(); c++) {
				double v1 = Math.round(a.get(r,c));
				double v2 = Math.round(b.get(r,c));
				if(v1 != v2) return false;
			}
		}
		return true;
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

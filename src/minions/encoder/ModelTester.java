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
	
	/*public static double calcAccuracyPartialCredit(OldEncoderModel m, List<TestTriplet> ts) {
		int correct = 0;
		int tested = 0;
		for(TestTriplet leaf : ts) {
			int[] guess = predict(m, leaf.getAst());
			int[] truth = leaf.getPostcondition();
			for(int i = 0; i < m.getNumOutputs(); i++) {
				if(guess[i] == truth[i]) {
					correct += 1;
				}
				tested++;
			}
		}
		return (100.0 * correct) / tested;
	}
	
	public static boolean checkCorrect(int[] guess, int[] truth) {
		for(int i = 0; i < guess.length; i++) {
			if(guess[i] != truth[i]) {
				return false;
			}
		}
		return true;
	}

	public static int[] predict(OldEncoderModel model, Tree ast) {
		EncodeTree encodeTree = EncodeTreeParser.parseEncodeTree(ast);

		// calculate the activation of all tree nodes
		model.treeActivation(encodeTree);

		int[] output = new int[model.getFormat().getNumOutputs()];
		for(int i = 0; i < model.getFormat().getNumOutputs(); i++){

			// calculate the activation of an out node
			OutputNode outNode = model.outActivation(encodeTree, i);
			SimpleMatrix activation = outNode.getActivation();

			// make the prediction (argmax of activation)
			int index = MatrixUtil.argmax(activation);
			output[i] = model.getFormat().getOutputOptions(i)[index];
		}
		return output;
	}*/
}

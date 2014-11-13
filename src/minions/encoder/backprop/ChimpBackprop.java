package minions.encoder.backprop;

import java.util.List;

import org.ejml.simple.SimpleMatrix;

import util.MatrixUtil;
import util.NeuralUtils;
import util.Warnings;
import minions.encoder.factory.EncoderFactory;
import models.code.State;
import models.code.TestTriplet;
import models.encoder.CodeVector;
import models.encoder.EncodeGraph;
import models.encoder.ModelFormat;
import models.encoder.decoders.ValueDecoder;
import models.encoder.encoders.BearModel;
import models.encoder.encoders.Encoder;
import models.encoder.encoders.MonkeyModel;
import models.encoder.encoders.ProgramEncoder;
import models.encoder.encoders.StateDecoder;
import models.encoder.encoders.StateEncoder;
import models.encoder.neurons.ValueNeuron;
import models.encoder.neurons.StateNeuron;
import models.encoder.neurons.TreeNeuron;

public class ChimpBackprop extends MonkeyBackprop {

	protected void addWeightDecay(MonkeyModel model2) {
		ProgramBackprop.addWeightDecay(
				model.getProgramEncoder(), 
				modelGrad.getProgramEncoder());
	}

	protected void addGradForTest(TestTriplet test) {
		backpropForPostError(test);
	}

	protected void calculateError(
			TreeNeuron runTree, 
			StateNeuron input,
			ValueNeuron valueNeuron,
			State truth, 
			String key) {

		// calculate mu for the output neuron
		StateDecoderBackprop.outputError(model.getFormat(), valueNeuron, truth, key);

		// program error
		SimpleMatrix outputError = valueNeuron.getError();
		ValueDecoder decoder = model.getOutputDecoder(key);
		SimpleMatrix outputW = decoder.getW();

		if(!runTree.isLeaf()) {
			rootError(runTree, input, outputError, outputW);
			ProgramBackprop.childError(model.getProgramEncoder(), runTree, 0);
		}
	}

	protected void addGrad(
			TreeNeuron tree, 
			StateNeuron input, 
			ValueNeuron outNode, 
			SimpleMatrix postv) {

		// update the output encoder params...
		StateDecoderBackprop.gradientStepValue(modelGrad, postv, outNode);

		// then update all the tree params...
		addGradProgram(tree, input, outNode);
	}

}

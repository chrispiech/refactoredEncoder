package minions.encoder.backprop;

import java.util.List;

import org.ejml.simple.SimpleMatrix;

import util.NeuralUtils;
import util.Warnings;
import minions.encoder.factory.EncoderFactory;
import models.code.State;
import models.code.TestTriplet;
import models.encoder.CodeVector;
import models.encoder.EncodeGraph;
import models.encoder.ModelFormat;
import models.encoder.decoders.ValueDecoder;
import models.encoder.encoders.Encoder;
import models.encoder.encoders.Mixer;
import models.encoder.encoders.MonkeyModel;
import models.encoder.encoders.PenguinModel;
import models.encoder.encoders.ProgramEncoder;
import models.encoder.encoders.StateEncoder;
import models.encoder.neurons.Neuron;
import models.encoder.neurons.StateNeuron;
import models.encoder.neurons.TreeNeuron;
import models.encoder.neurons.ValueNeuron;

public class PenguinBackprop {

	public static Encoder derivativeWithDecay(PenguinModel model,
			List<TestTriplet> list) {
		return new PenguinBackprop().getGradWithDecay(model, list);
	}

	public static Encoder derivative(PenguinModel model, List<TestTriplet> list) {
		return new PenguinBackprop().getGradNoDecay(model, list);
	}

	private PenguinModel model;
	private ModelFormat format;
	private PenguinModel modelGrad;

	private Encoder getGradNoDecay(PenguinModel model, List<TestTriplet> data) {
		this.model = model;
		this.format = model.getFormat();
		this.modelGrad = (PenguinModel)EncoderFactory.makeZero(model.getFormat());
		calculateGradNoDecay(data);
		return modelGrad;
	}

	private Encoder getGradWithDecay(PenguinModel model, List<TestTriplet> data) {
		this.model = model;
		this.modelGrad = (PenguinModel)EncoderFactory.makeZero(model.getFormat());
		this.format = model.getFormat();
		calculateGradNoDecay(data);
		addWeightDecay(model);
		return modelGrad;
	}

	private void calculateGradNoDecay(List<TestTriplet> data) {
		for(TestTriplet test : data) {
			addGradForTest(test);
		}
		int numOutputs = format.getNumOutputs();
		modelGrad.scale(1.0 / (numOutputs * data.size()));
	}

	private void addGradForTest(TestTriplet test) {
		backpropForPostError(test);
		backpropForPreError(test);
	}

	private void backpropForPreError(TestTriplet test) {
		State truth = test.getPrecondition();
		StateNeuron n3 = new StateNeuron(truth);
		StateEncoder stateEncoder = model.getStateEncoder();
		SimpleMatrix preVector = stateEncoder.activateState(n3);

		// backprop for post error
		for(String key : format.getStateKeys()){
			// calculate activation and mu for the value neuron
			ValueDecoder decoder = model.getOutputDecoder(key);
			ValueNeuron n2 = decoder.outActivation(preVector, key);
			StateDecoderBackprop.outputError(model.getFormat(), n2, truth, key);

			// calculate mu for the state neuron
			SimpleMatrix mu2 = n2.getError();
			SimpleMatrix z3 = n3.getZ();
			SimpleMatrix lhs = decoder.getW().transpose().mult(mu2);
			SimpleMatrix rhs = NeuralUtils.elementTanhGrad(z3);
			SimpleMatrix mu3 = lhs.elementMult(rhs);
			n3.setError(mu3);

			// now update the gradients..
			SimpleMatrix a3 = n3.getActivation();
			StateDecoderBackprop.gradientStepValue(modelGrad, a3, n2);
			StateEncoderBackprop.gradientStepIn(modelGrad, n3);
		}
	}

	private void backpropForPostError(TestTriplet test) {
		EncodeGraph graph = test.getEncodeGraph();
		TreeNeuron runTree = graph.getRunEncodeTreeClone();
		State pre = test.getPrecondition();
		State truth = test.getPostcondition();

		// calculate the activation of all tree nodes
		ProgramEncoder programEncoder = model.getProgramEncoder();
		CodeVector cv = programEncoder.activateTree(runTree);

		// calcuate the activation of the pre state embedding
		StateNeuron input = new StateNeuron(pre);
		StateEncoder stateEncoder = model.getStateEncoder();
		SimpleMatrix preVector = stateEncoder.activateState(input);

		// calculate the activation of the post state embedding
		Neuron mixerNeuron = new Neuron();
		Mixer mixer = model.getCombiner();
		SimpleMatrix postVector = mixer.activate(mixerNeuron, preVector, cv.getVector());

		// backprop for post error
		for(String key : format.getStateKeys()){
			// guess values for the post state.
			ValueDecoder outDecoder = model.getOutputDecoder(key);
			ValueNeuron output = outDecoder.outActivation(postVector, key);

			// calculate the error terms of all neurons
			calculateError(runTree, mixerNeuron, input, output, truth, key);

			// add to the grad for all parameters
			addGrad(runTree, mixerNeuron, input, output, postVector);
		}
	}

	private void calculateError(TreeNeuron runTree, Neuron mixer, StateNeuron input,
			ValueNeuron valueNeuron, State truth, String key) {
		// calculate mu for the output neuron
		StateDecoderBackprop.outputError(model.getFormat(), valueNeuron, truth, key);

		// calculate mu for the mixer neuron
		mixerError(valueNeuron, mixer);

		// calculate mu for the root neuron
		if(!runTree.isLeaf()) {
			rootError(runTree, mixer);
			ProgramBackprop.childError(model.getProgramEncoder(), runTree, 0);
		}

		// calculate mu for the input neuron
		inputError(input, mixer);

	}

	private void inputError(StateNeuron input, Neuron mixer) {
		SimpleMatrix W2 = model.getCombiner().getW2();
		SimpleMatrix lhs = W2.transpose().mult(mixer.getError());
		SimpleMatrix rhs = NeuralUtils.elementTanhGrad(input.getZ());
		input.setError(lhs.elementMult(rhs));
	}

	private void rootError(TreeNeuron runTree, Neuron mixer) {
		SimpleMatrix W1 = model.getCombiner().getW1();
		SimpleMatrix lhs = W1.transpose().mult(mixer.getError());
		SimpleMatrix rhs = NeuralUtils.elementTanhGrad(runTree.getZ());
		runTree.setError(lhs.elementMult(rhs));
	}

	private void mixerError(ValueNeuron valueNeuron, Neuron mixer) {
		// extract some useful matrices
		SimpleMatrix outputError = valueNeuron.getError();
		ValueDecoder decoder = model.getOutputDecoder(valueNeuron.getKey());
		SimpleMatrix outputW = decoder.getW();
		SimpleMatrix z1 = mixer.getZ();

		SimpleMatrix lhs = outputW.transpose().mult(outputError);
		SimpleMatrix rhs = NeuralUtils.elementTanhGrad(z1);
		mixer.setError(lhs.elementMult(rhs));
	}



	private void addGrad(TreeNeuron tree, Neuron mixer, StateNeuron input,
			ValueNeuron output, SimpleMatrix postVector) {
		// update the output encoder params...
		StateDecoderBackprop.gradientStepValue(modelGrad, postVector, output);

		// update the mixer params...
		addGradMixer(mixer, tree, input);
		
		// then update all the tree params...
		addGradProgram(tree, mixer);

		// the update all the input params
		StateEncoderBackprop.gradientStepIn(modelGrad, input);
	}

	private void addGradMixer(Neuron mixer, TreeNeuron tree, StateNeuron input) {
		SimpleMatrix mu0 = mixer.getError();
		SimpleMatrix a1 = tree.getActivation();
		SimpleMatrix a2 = input.getActivation();
		
		SimpleMatrix dW1 = mu0.mult(a1.transpose());
		SimpleMatrix dW2 = mu0.mult(a2.transpose());
		SimpleMatrix dB = mu0;
		
		Mixer comb = modelGrad.getCombiner();
		SimpleMatrix newW1 = comb.getW1().plus(dW1);
		SimpleMatrix newW2 = comb.getW2().plus(dW2);
		SimpleMatrix newB = comb.getB().plus(dB);
		
		comb.setParameters(newW1, newW2, newB);
	}

	private void addGradProgram(TreeNeuron tree, Neuron mixer) {
		ProgramEncoder programModel = model.getProgramEncoder();
		ProgramEncoder programGrad = modelGrad.getProgramEncoder();

		if(tree.isLeaf()) {
			Mixer combiner = model.getCombiner();
			SimpleMatrix W1 = combiner.getW1();
			SimpleMatrix mu0 = mixer.getError();
			SimpleMatrix dF = W1.transpose().mult(mu0);
			ProgramBackprop.updateGradLeaf(programGrad, dF, tree.getType());
		} else {
			ProgramBackprop.gradStepInternal(programModel, programGrad, tree);
			ProgramBackprop.gradStepChildren(programModel, programGrad, tree, 0);
		}
	}

	private void addWeightDecay(PenguinModel model2) {
		ProgramBackprop.addWeightDecay(
				model.getProgramEncoder(), 
				modelGrad.getProgramEncoder());

		StateDecoderBackprop.addWeightDecay(
				model.getStateDecoder(),
				modelGrad.getStateDecoder());

		StateEncoderBackprop.addWeightDecay(
				model.getStateEncoder(),
				modelGrad.getStateEncoder());
		
		Warnings.msg("no weight decay added to combiner");
		
	}

}

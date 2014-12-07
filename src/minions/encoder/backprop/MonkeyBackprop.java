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
import models.encoder.encoders.StateEncoder;
import models.encoder.neurons.ValueNeuron;
import models.encoder.neurons.StateNeuron;
import models.encoder.neurons.TreeNeuron;

public class MonkeyBackprop {

	public static Encoder derivative(MonkeyModel model, List<TestTriplet> list) {
		return new MonkeyBackprop().getDerivative(model, list);
	}

	public static Encoder derivativeWithDecay(MonkeyModel model,
			List<TestTriplet> list) {
		return new MonkeyBackprop().getDerivative(model, list);
	}

	protected MonkeyModel model = null;
	protected MonkeyModel modelGrad = null;
	protected ModelFormat format = null;

	protected Encoder getDerivative(MonkeyModel model, List<TestTriplet> data) {
		this.model = model;
		this.modelGrad = (MonkeyModel)EncoderFactory.makeZero(model.getFormat());
		this.format = model.getFormat();
		for(TestTriplet test : data) {
			addGradForTest(test);
		}
		modelGrad.scale(1.0 / data.size());
		
		// this is equivalent to adding weight decay once per test and then 
		// scaling weight decay with the rest of the grad by 1/data.size
		addWeightDecay(); 
		return modelGrad;
	}

	protected void addWeightDecay() {
		ProgramBackprop.addWeightDecay(
				model.getProgramEncoder(), 
				modelGrad.getProgramEncoder());

		StateDecoderBackprop.addWeightDecay(
				model.getStateDecoder(),
				modelGrad.getStateDecoder());

		StateEncoderBackprop.addWeightDecay(
				model.getStateEncoder(),
				modelGrad.getStateEncoder());
	}

	protected void calculateGradNoDecay(List<TestTriplet> data) {
		for(TestTriplet test : data) {
			addGradForTest(test);
		}
		int numOutputs = format.getNumOutputs();
		modelGrad.scale(1.0 / (numOutputs * data.size()));
	}

	protected void addGradForTest(TestTriplet test) {
		backpropForPostError(test);
		backpropForPreError(test);
	}

	protected void backpropForPostError(TestTriplet test) {
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
		SimpleMatrix postVector = model.getPostVector(preVector, cv.getVector());

		// backprop for post error
		for(String key : format.getStateKeys()){
			// guess values for the post state.
			ValueDecoder outDecoder = model.getOutputDecoder(key);
			ValueNeuron output = outDecoder.outActivation(postVector, key);

			// calculate the error terms of all neurons
			calculateError(runTree, input, output, truth, key);

			// add to the grad for all parameters
			addGrad(runTree, input, output, postVector);
		}
	}

	protected void backpropForPreError(TestTriplet test) {
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

	protected void calculateError(
			TreeNeuron runTree, 
			StateNeuron input,
			ValueNeuron valueNeuron,
			State truth, 
			String key) {

		// calculate mu for the output neuron
		StateDecoderBackprop.outputError(model.getFormat(), valueNeuron, truth, key);

		// extract some useful matrices
		SimpleMatrix outputError = valueNeuron.getError();
		ValueDecoder decoder = model.getOutputDecoder(key);
		SimpleMatrix outputW = decoder.getW();

		// calculate mu for the root neuron
		if(!runTree.isLeaf()) {
			rootError(runTree, input, outputError, outputW);
			ProgramBackprop.childError(model.getProgramEncoder(), runTree, 0);
		}

		// calculate mu for the input neuron
		inputError(runTree, input, outputError, outputW);
	}

	protected void inputError(TreeNeuron runTree, StateNeuron input,
			SimpleMatrix outputError, SimpleMatrix outputW) {
		// Derivation from the flight SFO to LHR
		SimpleMatrix a5 = runTree.getActivation();
		SimpleMatrix mu1 = outputW.transpose().mult(outputError);
		SimpleMatrix lhs = MonkeyModel.reshape(a5).transpose().mult(mu1);
		SimpleMatrix tanhPrime = NeuralUtils.elementTanhGrad(input.getZ());
		SimpleMatrix inputError = lhs.elementMult(tanhPrime);

		input.setError(inputError);
	}

	protected void rootError(TreeNeuron runTree, StateNeuron input,
			SimpleMatrix outputError, SimpleMatrix outputW) {
		// Derivation from the flight SFO to LHR. This is the same as the 
		// unshape of the derivation from shareLatex.
		SimpleMatrix a3 = input.getActivation();
		SimpleMatrix mu1 = outputW.transpose().mult(outputError);
		SimpleMatrix lhs = MonkeyModel.unshape(mu1.mult(a3.transpose()));

		SimpleMatrix z5 = runTree.getZ();
		SimpleMatrix rhs = NeuralUtils.elementTanhGrad(z5);

		SimpleMatrix rootError = lhs.elementMult(rhs);
		runTree.setError(rootError);
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

		// the update all the input params
		StateEncoderBackprop.gradientStepIn(modelGrad, input);
	}

	protected void addGradProgram(TreeNeuron tree, StateNeuron input,
			ValueNeuron outNode) {
		ProgramEncoder programModel = model.getProgramEncoder();
		ProgramEncoder programGrad = modelGrad.getProgramEncoder();

		if(tree.isLeaf()) {
			ValueDecoder parent = model.getOutputDecoder(outNode.getKey());
			SimpleMatrix parentError = outNode.getError();
			SimpleMatrix parentW = parent.getW();
			SimpleMatrix mu1 = parentW.transpose().mult(parentError);
			SimpleMatrix a3 = input.getActivation();
			SimpleMatrix matrixGrad = mu1.mult(a3.transpose());
			SimpleMatrix dF = MonkeyModel.unshape(matrixGrad);
			ProgramBackprop.updateGradLeaf(programGrad, dF, tree.getType());
		} else {
			ProgramBackprop.gradStepInternal(programModel, programGrad, tree);
			ProgramBackprop.gradStepChildren(programModel, programGrad, tree, 0);
		}
	}

}

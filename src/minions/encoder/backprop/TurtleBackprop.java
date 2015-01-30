package minions.encoder.backprop;

import minions.encoder.factory.EncoderFactory;
import models.code.State;
import models.code.TestTriplet;
import models.encoder.ModelFormat;
import models.encoder.decoders.ValueDecoder;
import models.encoder.encoders.Encoder;
import models.encoder.encoders.models.LemurModel;
import models.encoder.encoders.models.StateEncodable;
import models.encoder.encoders.models.TurtleModel;
import models.encoder.encoders.programEncoder.ProgramEncoder;
import models.encoder.encoders.state.StateEncoder;
import models.encoder.neurons.StateNeuron;
import models.encoder.neurons.TreeNeuron;
import models.encoder.neurons.ValueNeuron;

import org.apache.commons.math3.util.Pair;
import org.ejml.simple.SimpleMatrix;

import util.NeuralUtils;

public class TurtleBackprop {

	public static Pair<TurtleModel, SimpleMatrix> getGradient(TurtleModel model,
			SimpleMatrix program, TestTriplet test) {
		return new TurtleBackprop().run(model, program, test);
	}

	private ModelFormat format;
	private TurtleModel model;
	private TurtleModel grad;
	private SimpleMatrix program;
	private SimpleMatrix programGrad;

	private Pair<TurtleModel, SimpleMatrix> run(TurtleModel model, SimpleMatrix program, TestTriplet test) {
		format = model.getFormat();
		this.model = model;
		this.program = program;
		this.grad = (TurtleModel)EncoderFactory.makeZero(model.getFormat());
		this.programGrad = new SimpleMatrix(program.numRows(), program.numCols());
		
		State pre = test.getPrecondition();
		State truth = test.getPostcondition();

		// calcuate the activation of the pre state embedding
		StateNeuron input = new StateNeuron(pre);
		StateEncoder stateEncoder = model.getStateEncoder();
		SimpleMatrix preVector = stateEncoder.activateState(input);

		// calculate the activation of the post state embedding
		SimpleMatrix postVector = model.getPostVector(preVector, program);

		// backprop for post error
		for(String key : format.getStateKeys()){
			// guess values for the post state.
			ValueDecoder outDecoder = model.getOutputDecoder(key);
			ValueNeuron output = outDecoder.outActivation(postVector, key);

			// calculate the error terms of all neurons
			calculateError(input, output, truth, key);

			// add to the grad for all parameters
			addGrad(input, output, postVector);
		}
		addWeightDecay(); 
		return new Pair<TurtleModel, SimpleMatrix>(grad, programGrad);
	}

	private void addWeightDecay() {
		StateDecoderBackprop.addWeightDecay(
				model.getStateDecoder(),
				grad.getStateDecoder());

		StateEncoderBackprop.addWeightDecay(
				model.getStateEncoder(),
				grad.getStateEncoder());
	}

	private void addGrad(StateNeuron input, ValueNeuron outNode,
			SimpleMatrix postv) {
		// update the output encoder params...
		StateDecoderBackprop.gradientStepValue(grad, postv, outNode);

		// then update all the tree params...
		addGradProgram(input, outNode);

		// the update all the input params
		StateEncoderBackprop.gradientStepIn(grad, input);
	}

	protected void addGradProgram(StateNeuron input,ValueNeuron outNode) {
		ValueDecoder parent = model.getOutputDecoder(outNode.getKey());
		SimpleMatrix parentError = outNode.getError();
		SimpleMatrix parentW = parent.getW();
		SimpleMatrix mu1 = parentW.transpose().mult(parentError);
		SimpleMatrix a3 = input.getActivation();
		SimpleMatrix matrixGrad = mu1.mult(a3.transpose());
		SimpleMatrix dF = matrixGrad;
		//ProgramBackprop.updateGradLeaf(programGrad, dF, tree.getType());
		
		programGrad.set(programGrad.plus(dF));
	}

	private void calculateError(StateNeuron input, ValueNeuron output,
			State truth, String key) {
		// calculate mu for the output neuron
		StateDecoderBackprop.outputError(model.getFormat(), output, truth, key);

		// extract some useful matrices
		SimpleMatrix outputError = output.getError();
		ValueDecoder decoder = model.getOutputDecoder(key);
		SimpleMatrix outputW = decoder.getW();

		// calculate mu for the input neuron
		inputError(input, outputError, outputW);

	}

	private void inputError(StateNeuron input, SimpleMatrix outputError,
			SimpleMatrix outputW) {
		// Derivation from the flight SFO to LHR
		SimpleMatrix a5 = program;
		SimpleMatrix mu1 = outputW.transpose().mult(outputError);
		SimpleMatrix lhs = a5.transpose().mult(mu1);
		SimpleMatrix tanhPrime = NeuralUtils.elementTanhGrad(input.getZ());
		SimpleMatrix inputError = lhs.elementMult(tanhPrime);
		input.setError(inputError);
	}

}

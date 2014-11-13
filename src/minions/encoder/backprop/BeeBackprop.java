package minions.encoder.backprop;

import java.util.List;

import org.ejml.simple.SimpleMatrix;

import util.NeuralUtils;
import util.Warnings;
import minions.encoder.factory.EncoderFactory;
import models.code.State;
import models.code.TestTriplet;
import models.encoder.ModelFormat;
import models.encoder.decoders.ValueDecoder;
import models.encoder.encoders.BearModel;
import models.encoder.encoders.BeeModel;
import models.encoder.encoders.Encoder;
import models.encoder.encoders.StateEncoder;
import models.encoder.neurons.StateNeuron;
import models.encoder.neurons.ValueNeuron;

public class BeeBackprop {

	private BeeModel model = null;
	private BeeModel modelGrad = null;
	private ModelFormat format;
	
	public static Encoder derivative(BeeModel m, List<TestTriplet> data) {
		return new BeeBackprop().getGrad(m, data);
	}

	public static Encoder derivativeWithDecay(BeeModel model, List<TestTriplet> data) {
		return new BeeBackprop().getGradWithDecay(model, data);
	}

	private Encoder getGrad(BeeModel m, List<TestTriplet> data) {
		this.model = m;
		this.modelGrad = (BeeModel)EncoderFactory.makeZero(model.getFormat());
		this.format = model.getFormat();
		calculateGradNoDecay(data);
		return modelGrad;
	}

	private Encoder getGradWithDecay(BeeModel model, List<TestTriplet> data) {
		this.model = model;
		this.modelGrad = (BeeModel)EncoderFactory.makeZero(model.getFormat());
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
		addGradForState(test.getPrecondition());
		addGradForState(test.getPostcondition());
	}

	private void addGradForState(State truth) {
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

	private void addWeightDecay(BeeModel model) {
		StateDecoderBackprop.addWeightDecay(
				model.getStateDecoder(),
				modelGrad.getStateDecoder());

		StateEncoderBackprop.addWeightDecay(
				model.getStateEncoder(),
				modelGrad.getStateEncoder());
	}

}

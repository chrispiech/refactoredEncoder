package minions.encoder.backprop;

import java.util.List;

import org.ejml.simple.SimpleMatrix;

import util.NeuralUtils;
import minions.encoder.factory.EncoderFactory;
import models.code.State;
import models.code.TestTriplet;
import models.encoder.ModelFormat;
import models.encoder.decoders.ValueDecoder;
import models.encoder.encoders.Encoder;
import models.encoder.encoders.models.DeepBeeModel;
import models.encoder.encoders.models.LemurModel;
import models.encoder.encoders.state.DeepStateEncoder;
import models.encoder.encoders.state.NeuralLayer;
import models.encoder.encoders.state.StateEncoder;
import models.encoder.neurons.Neuron;
import models.encoder.neurons.StateNeuron;
import models.encoder.neurons.ValueNeuron;

public class DeepBeeBackprop {

	public static Encoder derivativeWithDecay(DeepBeeModel model,
			List<TestTriplet> list) {
		return new DeepBeeBackprop().run(model, list);
	}

	private DeepBeeModel model;
	private DeepBeeModel modelGrad;
	private ModelFormat format;

	private Encoder run(DeepBeeModel model, List<TestTriplet> data) {
		this.model = model;
		this.modelGrad = (DeepBeeModel)EncoderFactory.makeZero(model.getFormat());
		this.format = model.getFormat();
		for(TestTriplet test : data) {
			addGradForTest(test);
		}
		
		return modelGrad;
	}

	private void addGradForTest(TestTriplet test) {
		addGradForState(test.getPostcondition());
		addWeightDecay(); 
	}
	
	private void addWeightDecay() {
		StateDecoderBackprop.addWeightDecay(
				model.getStateDecoder().getDecoder(),
				modelGrad.getStateDecoder().getDecoder());

		StateEncoderBackprop.addWeightDecay(
				model.getStateEncoder().getEncoder(),
				modelGrad.getStateEncoder().getEncoder());
		
		NeuralLayerBackprop.addWeightDecay(
				model.getStateEncoder().getHidden(),
				modelGrad.getStateEncoder().getHidden());
		
		NeuralLayerBackprop.addWeightDecay(
				model.getStateDecoder().getHidden(),
				modelGrad.getStateDecoder().getHidden());
	}

	// in this derivation there are five neurons
	// [out, h3, h2, h1, in]
	private void addGradForState(State state) {
		// forward pass
		StateNeuron in = new StateNeuron(state);
		DeepStateEncoder stateEncoder = model.getStateEncoder();
		SimpleMatrix h1_a = stateEncoder.getEncoder().activateState(in);
		
		NeuralLayer hiddenEncoderLayer = model.getStateEncoder().getHidden();
		Neuron h2 = hiddenEncoderLayer.activateNeuron(h1_a);
		SimpleMatrix h2_a = h2.getActivation();
		
		NeuralLayer hiddenDecoderLayer = model.getStateDecoder().getHidden();
		Neuron h3 = hiddenDecoderLayer.activateNeuron(h2_a);
		SimpleMatrix h3_a = h3.getActivation();
		
		// this is per output.. which... is awesome
		for(String key : format.getStateKeys()){
			
			// calculate activation and mu for the value neuron
			ValueDecoder decoder = model.getOutputDecoder(key);
			ValueNeuron out = decoder.outActivation(h3_a, key);
			StateDecoderBackprop.outputError(model.getFormat(), out, state, key);
			
			// calculate mus
			h3.setError(calcMu(h3.getZ(), out.getError(), decoder.getW()));
			h2.setError(calcMu(h2.getZ(), h3.getError(), hiddenDecoderLayer.W));
			in.setError(calcMu(in.getZ(), h2.getError(), hiddenEncoderLayer.W));
			
			// now update the gradients..
			gradStepDecoder(out, h3.getActivation());
			gradStepHiddenDecoder(h3, h2.getActivation());
			gradStepHiddenEncoder(h2, in.getActivation());
			gradStepEncoder(in);
		}
	}

	private void gradStepDecoder(ValueNeuron out, SimpleMatrix childA) {
		SimpleMatrix dW = out.getError().mult(childA.transpose());
		SimpleMatrix dB = new SimpleMatrix(out.getError());
		ValueDecoder outEncoder = modelGrad.getOutputDecoder(out.getKey());
		StateDecoderBackprop.updateGradOut(outEncoder, dW, dB);
	}
	
	private void gradStepHiddenDecoder(Neuron h3, SimpleMatrix childA) {
		SimpleMatrix dW = h3.getError().mult(childA.transpose());
		SimpleMatrix dB = new SimpleMatrix(h3.getError());
		NeuralLayer layer = modelGrad.getStateDecoder().getHidden();
		NeuralLayerBackprop.updateGrad(layer, dW, dB);
	}
	
	private void gradStepHiddenEncoder(Neuron h2, SimpleMatrix childA) {
		SimpleMatrix dW = h2.getError().mult(childA.transpose());
		SimpleMatrix dB = new SimpleMatrix(h2.getError());
		NeuralLayer layer = modelGrad.getStateEncoder().getHidden();
		NeuralLayerBackprop.updateGrad(layer, dW, dB);
	}
	
	private void gradStepEncoder(StateNeuron in) {
		SimpleMatrix mu3 = in.getError();
		ModelFormat format = modelGrad.getFormat();
		StateEncoder stateEncoder = modelGrad.getStateEncoder().getEncoder();
		State inputState = in.getState();
		for(String key : format.getLanguage().getStateKeys()) {
			SimpleMatrix a4 = inputState.getActivation(format, key);
			SimpleMatrix dW = mu3.mult(a4.transpose());
			StateEncoderBackprop.updateStateEncodeWGrad(stateEncoder, key, dW);
		}
		StateEncoderBackprop.updateStateEncodeBGrad(stateEncoder, mu3);
	}

	private SimpleMatrix calcMu(SimpleMatrix z, 
			SimpleMatrix parentMu, SimpleMatrix parentW) {
		return parentW.transpose().mult(parentMu).elementMult(NeuralUtils.elementTanhGrad(z));
	}

}

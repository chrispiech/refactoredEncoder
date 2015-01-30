package minions.encoder.backprop;

import org.ejml.simple.SimpleMatrix;

import models.code.State;
import models.encoder.EncoderParams;
import models.encoder.ModelFormat;
import models.encoder.decoders.ValueDecoder;
import models.encoder.encoders.models.MonkeyModel;
import models.encoder.encoders.models.StateEncodable;
import models.encoder.encoders.state.StateEncoder;
import models.encoder.neurons.StateNeuron;

public class StateEncoderBackprop {

	public static void gradientStepIn(StateEncodable modelGrad, StateNeuron input) {
		SimpleMatrix mu3 = input.getError();
		ModelFormat format = modelGrad.getFormat();
		StateEncoder stateEncoder = (StateEncoder) modelGrad.getStateEncoder();
		State inputState = input.getState();
		for(String key : format.getLanguage().getStateKeys()) {
			SimpleMatrix a4 = inputState.getActivation(format, key);
			SimpleMatrix dW = mu3.mult(a4.transpose());
			updateStateEncodeWGrad(stateEncoder, key, dW);
		}
		SimpleMatrix dB = new SimpleMatrix(mu3);
		updateStateEncodeBGrad(stateEncoder, dB);
	}

	public static void updateStateEncodeBGrad(StateEncoder stateEncoder,
			SimpleMatrix dB) {
		SimpleMatrix oldB = stateEncoder.getB();
		SimpleMatrix newB = oldB.plus(dB);
		stateEncoder.setB(newB);
	}

	public static void updateStateEncodeWGrad(StateEncoder stateEncoder,
			String key, SimpleMatrix dW) {
		SimpleMatrix oldW = stateEncoder.getW(key);
		SimpleMatrix newW = oldW.plus(dW);
		stateEncoder.setW(key, newW);
	}

	public static void addWeightDecay(StateEncoder encoder,
			StateEncoder encoderGrad) {
		double lambda = EncoderParams.getWeightDecay();
		
		for(String key : encoder.getStateKeys()) {
			SimpleMatrix dW = encoderGrad.getW(key);
			SimpleMatrix W = encoder.getW(key);
			dW = dW.plus(W.scale(lambda));
			encoderGrad.setW(key, dW);
		}
	}

}

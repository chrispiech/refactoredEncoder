package minions.encoder.backprop;

import org.ejml.simple.SimpleMatrix;

import models.code.State;
import models.encoder.EncoderParams;
import models.encoder.ModelFormat;
import models.encoder.decoders.ValueDecoder;
import models.encoder.encoders.MonkeyModel;
import models.encoder.encoders.StateEncoder;
import models.encoder.encoders.types.StateEncodable;
import models.encoder.neurons.StateNeuron;

public class StateEncoderBackprop {

	public static void gradientStepIn(StateEncodable modelGrad, StateNeuron input) {
		SimpleMatrix mu3 = input.getError();
		ModelFormat format = modelGrad.getFormat();
		StateEncoder stateEncoder = modelGrad.getStateEncoder();
		State inputState = input.getState();
		for(String key : format.getLanguage().getStateKeys()) {
			SimpleMatrix a4 = inputState.getActivation(format, key);
			SimpleMatrix dW = mu3.mult(a4.transpose());
			updateStateEncodeWGrad(stateEncoder, key, dW);
		}
		updateStateEncodeBGrad(stateEncoder, mu3);
	}

	private static void updateStateEncodeBGrad(StateEncoder stateEncoder,
			SimpleMatrix dB) {
		SimpleMatrix oldB = stateEncoder.getB();
		SimpleMatrix newB = oldB.plus(dB);
		stateEncoder.setB(newB);
	}

	private static void updateStateEncodeWGrad(StateEncoder stateEncoder,
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

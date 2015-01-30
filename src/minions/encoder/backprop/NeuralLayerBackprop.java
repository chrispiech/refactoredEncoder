package minions.encoder.backprop;

import models.encoder.EncoderParams;
import models.encoder.encoders.state.NeuralLayer;

import org.ejml.simple.SimpleMatrix;

public class NeuralLayerBackprop {

	public static void updateGrad(NeuralLayer layer, SimpleMatrix dW,
			SimpleMatrix dB) {
		layer.W = layer.W.plus(dW);
		layer.b = layer.b.plus(dB);
	}

	public static void addWeightDecay(NeuralLayer model, NeuralLayer grad) {
		double lambda = EncoderParams.getWeightDecay();
		grad.W = grad.W.plus(model.W.scale(lambda));
	}

}

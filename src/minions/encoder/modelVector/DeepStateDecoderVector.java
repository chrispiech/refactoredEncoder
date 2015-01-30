package minions.encoder.modelVector;

import java.util.LinkedList;
import java.util.List;

import models.encoder.EncoderParams;
import models.encoder.ModelFormat;
import models.encoder.encoders.state.DeepStateDecoder;
import models.encoder.encoders.state.NeuralLayer;
import models.encoder.encoders.state.StateDecoder;
import util.MatrixUtil;

public class DeepStateDecoderVector {

	public static DeepStateDecoder vecToDecoder(ModelFormat format,
			List<Double> list) {
		int M = EncoderParams.getM();
		int hiddenLayerSize = M * M + M;
		int nonHidden = list.size() - hiddenLayerSize;
		
		StateDecoder d = StateDecoderVector.vecToDecoder(format, list.subList(0, nonHidden));
		NeuralLayer l = NeuralLayerVector.vecToLayer(format, list.subList(nonHidden, list.size()));
		return new DeepStateDecoder(format, d, l);
	}

	public static List<Double> decoderToVec(ModelFormat format,
			DeepStateDecoder stateDecoder) {
		List<Double> list = new LinkedList<Double>();
		list.addAll(StateDecoderVector.decoderToVec(format, stateDecoder.getDecoder()));
		list.addAll(MatrixUtil.matrixToList(stateDecoder.getHidden().W));
		list.addAll(MatrixUtil.matrixToList(stateDecoder.getHidden().b));
		return list;
	}

}

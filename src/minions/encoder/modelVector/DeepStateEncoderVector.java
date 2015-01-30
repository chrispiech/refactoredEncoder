package minions.encoder.modelVector;

import java.util.LinkedList;
import java.util.List;

import util.MatrixUtil;
import models.encoder.EncoderParams;
import models.encoder.ModelFormat;
import models.encoder.encoders.state.DeepStateEncoder;
import models.encoder.encoders.state.NeuralLayer;
import models.encoder.encoders.state.StateEncoder;

public class DeepStateEncoderVector {

	public static DeepStateEncoder vecToEncoder(ModelFormat format,
			List<Double> list) {
		int M = EncoderParams.getM();
		int hiddenLayerSize = M * M + M;
		int nonHidden = list.size() - hiddenLayerSize;
		
		StateEncoder e = StateEncoderVector.vecToEncoder(format, list.subList(0, nonHidden));
		NeuralLayer l = NeuralLayerVector.vecToLayer(format, list.subList(nonHidden, list.size()));
		return new DeepStateEncoder(format, e, l);
	}

	public static List<Double> encoderToVec(ModelFormat format,
			DeepStateEncoder stateEncoder) {
		List<Double> list = new LinkedList<Double>();
		list.addAll(StateEncoderVector.encoderToVec(format, stateEncoder.getEncoder()));
		list.addAll(MatrixUtil.matrixToList(stateEncoder.getHidden().W));
		list.addAll(MatrixUtil.matrixToList(stateEncoder.getHidden().b));
		return list;
	}

}

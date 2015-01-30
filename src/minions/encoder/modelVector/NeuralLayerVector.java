package minions.encoder.modelVector;

import java.util.List;

import models.encoder.EncoderParams;
import models.encoder.ModelFormat;
import models.encoder.encoders.state.NeuralLayer;

import org.ejml.simple.SimpleMatrix;

import util.MatrixUtil;

public class NeuralLayerVector {

	public static NeuralLayer vecToLayer(ModelFormat format, List<Double> list) {
		int M = EncoderParams.getM();
		List<Double> wList = list.subList(0, M * M);
		List<Double> bList = list.subList(M * M, list.size());
		SimpleMatrix W = MatrixUtil.listToMatrix(wList, M, M);
		SimpleMatrix b = MatrixUtil.listToMatrix(bList, M, 1);
		return new NeuralLayer(W, b);
	}

}

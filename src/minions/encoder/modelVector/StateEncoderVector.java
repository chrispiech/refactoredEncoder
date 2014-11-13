package minions.encoder.modelVector;

import java.util.*;

import org.ejml.simple.SimpleMatrix;

import util.MatrixUtil;
import models.encoder.EncoderParams;
import models.encoder.ModelFormat;
import models.encoder.encoders.StateEncoder;

public class StateEncoderVector {

	public static StateEncoder vecToEncoder(ModelFormat format,
			List<Double> inList) {
		Map<String, SimpleMatrix> wMap = new HashMap<String, SimpleMatrix>();
		int stateSize = format.getStateVectorSize();
		for(String key : format.getLanguage().getStateKeys()) {
			int rows = stateSize;
			int cols = format.getTypeVectorSize(key);
			int size = rows * cols;
			List<Double> vec = inList.subList(0, size);
			inList = ModelVector.listPop(inList, size);
			SimpleMatrix W = MatrixUtil.listToMatrix(vec, rows, cols);
			wMap.put(key, W);
		}
		SimpleMatrix b = MatrixUtil.listToMatrix(inList, stateSize, 1);
		return new StateEncoder(wMap, b, format);
	}

	public static List<Double> encoderToVec(ModelFormat format,
			StateEncoder stateEncoder) {
		List<Double> list = new ArrayList<Double>();
		for(String key : format.getLanguage().getStateKeys()) {
			SimpleMatrix W = stateEncoder.getW(key);
			list.addAll(MatrixUtil.matrixToList(W));
		}
		list.addAll(MatrixUtil.matrixToList(stateEncoder.getB()));
		return list;
	}

}

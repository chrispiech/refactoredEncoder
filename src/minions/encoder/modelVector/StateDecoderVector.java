package minions.encoder.modelVector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import minions.encoder.factory.EncoderFactory;
import models.encoder.EncoderParams;
import models.encoder.ModelFormat;
import models.encoder.decoders.ValueDecoder;
import models.encoder.encoders.BearModel;
import models.encoder.encoders.StateDecoder;

import org.ejml.simple.SimpleMatrix;

import util.MatrixUtil;

public class StateDecoderVector {

	public static StateDecoder vecToDecoder(
			ModelFormat format,
			List<Double> decoderList) {
		Map<String, ValueDecoder> map = new HashMap<String, ValueDecoder>();
		//OutputEncoder[] outs = new OutputEncoder[format.getNumOutputs()];
		for(String key : format.getStateKeys()){
			int dim = format.getStateDecoderDimension(key);
			List<Double> encoderList = decoderList.subList(0, dim);
			decoderList = ModelVector.listPop(decoderList, dim);
			int m = format.getTypeVectorSize(key);
			int stateEncodeSize = format.getStateVectorSize();
			int wSize = m * format.getStateVectorSize();
			List<Double> wList = encoderList.subList(0, wSize);
			SimpleMatrix W = MatrixUtil.listToMatrix(wList, m, stateEncodeSize);
			List<Double> bList = encoderList.subList(wSize, encoderList.size());
			SimpleMatrix b = MatrixUtil.listToMatrix(bList, m, 1);
			
			map.put(key, EncoderFactory.makeOutput(format, key, W, b));
		}
		return new StateDecoder(format, map);
	}
	
	public static List<Double> decoderToVec(ModelFormat format, StateDecoder model) {
		List<Double> vecList = new ArrayList<Double>();
		for(String outKey : format.getStateKeys()) {
			ValueDecoder encoder = model.getOutputDecoder(outKey);
			for(SimpleMatrix m : encoder.getParams()) {
				vecList.addAll(MatrixUtil.matrixToList(m));
			}
		}
		return vecList;
	}
	
}

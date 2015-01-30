package minions.minimizer.teddy;

import java.util.ArrayList;
import java.util.List;

import models.encoder.EncoderParams;
import models.encoder.decoders.SoftmaxDecoder;

import org.ejml.simple.SimpleMatrix;

import util.MatrixUtil;
import util.Warnings;

import com.google.common.primitives.Doubles;

import edu.stanford.nlp.util.ArrayUtils;

public class TeddyVec {
	
	public static double[] teddyToVec(TeddyModel grad, TeddyDimension dimension) {
		List<Double> list = new ArrayList<Double>();
		for(SoftmaxDecoder d : grad.getDecoders()) {
			for(SimpleMatrix m : d.getParams()) {
				list.addAll(MatrixUtil.matrixToList(m));
			}
		}
		list.addAll(MatrixUtil.matrixToList(grad.getW()));
		list.addAll(MatrixUtil.matrixToList(grad.getB()));
		return ArrayUtils.asPrimitiveDoubleArray(list);
	}
	
	////////////////////////////////////////////////

	public static TeddyModel getTeddy(TeddyDimension dim, double[] params) {
		List<Double> list = Doubles.asList(params);
		
		int m = EncoderParams.getM();
		Warnings.check(params.length == dim.getNumParameters());
		int outNodeSize = dim.getOutNodeSize();
		int numDecoders = dim.getNumDecoders();
		List<SoftmaxDecoder> decoders = new ArrayList<SoftmaxDecoder>();
		for(int i = 0; i < numDecoders; i++) {
			int start = i * outNodeSize;
			int end = (i + 1) * outNodeSize;
			List<Double> decoderParams = list.subList(start, end);
			SoftmaxDecoder decoder = makeSoftmaxDecoder(i, decoderParams);
			decoders.add(decoder);
		}
		int start = numDecoders * outNodeSize;
		List<Double> wList = list.subList(start, start + m*m);
		List<Double> bList = list.subList(start + m*m, list.size());
		SimpleMatrix W = MatrixUtil.listToMatrix(wList, m, m);
		SimpleMatrix b = MatrixUtil.listToMatrix(bList, m, m);
		return new TeddyModel(decoders, W, b);
		
	}

	private static SoftmaxDecoder makeSoftmaxDecoder(int i, List<Double> list) {
		int m = EncoderParams.getM();
		List<Double> wList = list.subList(0, m*m*2);
		List<Double> bList = list.subList(m*m*2, list.size());
		SimpleMatrix W = MatrixUtil.listToMatrix(wList, 2, m*m);
		SimpleMatrix b = MatrixUtil.listToMatrix(bList, 2, 1);
		return new SoftmaxDecoder(new TeddyLanguage(), i + "", W, b);
	}
}

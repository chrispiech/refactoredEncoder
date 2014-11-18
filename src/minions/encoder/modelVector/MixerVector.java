package minions.encoder.modelVector;

import java.util.*;

import org.ejml.simple.SimpleMatrix;

import util.MatrixUtil;
import models.encoder.EncoderParams;
import models.encoder.ModelFormat;
import models.encoder.encoders.Mixer;

public class MixerVector {

	public static Mixer vecToMixer(ModelFormat format,
			List<Double> mixerList) {
		
		int n = EncoderParams.getN();
		int m = EncoderParams.getM();
		
		int W1Dim = n * m;
		int W2Dim = m * m;
		
		List<Double> W1List = mixerList.subList(0, W1Dim);
		List<Double> W2List = mixerList.subList(W1Dim, W1Dim + W2Dim);
		List<Double> bList = mixerList.subList(W1Dim + W2Dim, mixerList.size());
		
		SimpleMatrix W1 = MatrixUtil.listToMatrix(W1List, m, n);
		SimpleMatrix W2 = MatrixUtil.listToMatrix(W2List, m, m);
		SimpleMatrix b = MatrixUtil.listToMatrix(bList, m, 1);
		
		return new Mixer(W1, W2, b);
	}

	public static List<Double> mixerToVec(Mixer mixer) {
		List<Double> list = new ArrayList<Double>();
		
		list.addAll(MatrixUtil.matrixToList(mixer.getW1()));
		list.addAll(MatrixUtil.matrixToList(mixer.getW2()));
		list.addAll(MatrixUtil.matrixToList(mixer.getB()));
		
		return list;
	}

}

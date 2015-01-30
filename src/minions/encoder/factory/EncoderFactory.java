package minions.encoder.factory;

import org.apache.commons.math3.util.Pair;
import org.ejml.simple.SimpleMatrix;

import util.RandomUtil;
import minions.encoder.modelVector.ModelVector;
import models.encoder.EncoderParams;
import models.encoder.ModelFormat;
import models.encoder.decoders.*;
import models.encoder.encoders.Encoder;
import models.language.Language;

public class EncoderFactory {
	
	public static Encoder makeInitial(ModelFormat format) {
		if(format.isChimp()) {
			return ChimpFactory.makeInitial(format);
		} else {
			return makeRandom(format);
		}
	}

	public static Encoder makeRandom(ModelFormat format) {
		double[] vec = makeRandomVec(format);
		return ModelVector.vecToModel(format, vec);
	}

	public static double[] makeRandomVec(ModelFormat format) {
		int dim = format.getNumParams();
		double[] vec = new double[dim];
		for(int i = 0; i < dim; i++) {
			vec[i] = RandomUtil.gauss(0, EncoderParams.getInitStd());
		}
		return vec;
	}
	
	public static Encoder makeZero(ModelFormat format) {
		int dim = format.getNumParams();
		double[] vec = new double[dim];
		for(int i = 0; i < dim; i++) {
			vec[i] = 0;
		}
		return ModelVector.vecToModel(format, vec);
	}

	public static ValueDecoder makeOutput(ModelFormat format, String key) {
		String type = format.getOutputType(key);
		if(type.equals("choice")) {
			Language l = format.getLanguage();
			return new SoftmaxDecoder(l, key);
		}
		if(type.equals("number")) {
			return new NumberDecoder(key);
		}
		if(type.equals("matrix")) {
			Pair<Integer, Integer> dim = format.getMatrixDim(key);
			return new MatrixDecoder(key, dim.getFirst(), dim.getSecond());
		}
		throw new RuntimeException("no");
	}
	
	public static ValueDecoder makeOutput(ModelFormat format, String key, 
			SimpleMatrix W, SimpleMatrix b) {
		String type = format.getOutputType(key);
		if(type.equals("choice")) {
			Language l = format.getLanguage();
			return new SoftmaxDecoder(l, key, W, b);
		}
		if(type.equals("number")) {
			return new NumberDecoder(key, W, b);
		}
		if(type.equals("matrix")) {
			Pair<Integer, Integer> dim = format.getMatrixDim(key);
			int rows = dim.getFirst();
			int cols = dim.getSecond();
			return new MatrixDecoder(key, W, b, rows, cols);
		}
		throw new RuntimeException("no");
	}
	
}

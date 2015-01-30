package minions.encoder.modelVector;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.util.Pair;
import org.ejml.simple.SimpleMatrix;

import models.encoder.ModelFormat;
import models.encoder.dimension.PenguinDimension;
import models.encoder.encoders.Encoder;
import models.encoder.encoders.models.LemurModel;
import models.encoder.encoders.models.TurtleModel;
import models.encoder.encoders.state.StateDecoder;
import models.encoder.encoders.state.StateEncoder;
import util.MatrixUtil;
import util.Warnings;

public class TurtleVector {
	
	
	public static List<Double> turtleToVec(TurtleModel model) {
		List<Double> vecList = new LinkedList<Double>();
		ModelFormat format = model.getFormat();

		// add out model
		StateDecoder stateDecoder = model.getStateDecoder();
		vecList.addAll(StateDecoderVector.decoderToVec(format, stateDecoder));
		Warnings.check(vecList.size() == format.getStateDecoderDimension());
		
		// add in model
		StateEncoder stateEncoder = model.getStateEncoder();
		List<Double> encoderList = StateEncoderVector.encoderToVec(format, stateEncoder);
		vecList.addAll(encoderList);
		Warnings.check(encoderList.size() == format.getStateEncoderDimension());
		
		return vecList;
	}

	public static TurtleModel vecToTurtle(ModelFormat format, List<Double> list) {
		int numOutParams = format.getStateDecoderDimension();
		
		int dimension = format.getNumParams();
		Warnings.check(dimension == list.size());
		
		List<Double> outList = list.subList(0, numOutParams);
		List<Double> inList = list.subList(numOutParams, list.size());

		// extract outs
		StateDecoder output = StateDecoderVector.vecToDecoder(format, outList);
		
		// extract ins
		StateEncoder input = StateEncoderVector.vecToEncoder(format, inList);

		
		return new TurtleModel(format, output, input);
	}

	public static List<Double> turtleToVec(TurtleModel model,
			SimpleMatrix matrix) {
		List<Double> vecList = new LinkedList<Double>();
		vecList.addAll(turtleToVec(model));
		vecList.addAll(MatrixUtil.matrixToList(matrix));
		return vecList;
	}

	public static Pair<TurtleModel, SimpleMatrix> vecToTurtleMatrix(
			List<Double> list) {
		ModelFormat f = new ModelFormat("karel", "turtle");
		int dim = f.getNumParams();
		TurtleModel model = vecToTurtle(f, list.subList(0, dim));
		int matrixSize = list.size() - dim;
		int n = (int) Math.sqrt(matrixSize);
		Warnings.check(n*n == matrixSize);
		SimpleMatrix matrix = MatrixUtil.listToMatrix(list.subList(dim, list.size()), n, n);
		return new Pair<TurtleModel, SimpleMatrix>(model, matrix);
	}

	public static String getNameForIndex(ModelFormat format, int elem) {
		int dim = format.getNumParams();
		if(elem < dim) {
			return "encoderDecoder";
		} else {
			return "matrix";
		}
	}
}

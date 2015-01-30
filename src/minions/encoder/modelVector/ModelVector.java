package minions.encoder.modelVector;

import java.util.List;

import minions.encoder.factory.EncoderFactory;
import minions.minimizer.teddy.TeddyVec;
import models.encoder.EncoderParams;
import models.encoder.ModelFormat;
import models.encoder.encoders.Encoder;
import models.encoder.encoders.models.BearModel;
import models.encoder.encoders.models.BeeModel;
import models.encoder.encoders.models.DeepBeeModel;
import models.encoder.encoders.models.LemurModel;
import models.encoder.encoders.models.MonkeyModel;
import models.encoder.encoders.models.PenguinModel;
import models.encoder.encoders.models.TurtleModel;

import org.apache.commons.math3.util.Pair;
import org.ejml.simple.SimpleMatrix;

import com.google.common.primitives.Doubles;

import util.Warnings;
import edu.stanford.nlp.util.ArrayUtils;


public class ModelVector {

	/************************************************************************
	 *                        MODEL TO VEC
	 ***********************************************************************/
	
	public static double[] modelToVec(TurtleModel model, SimpleMatrix matrix) {
		List<Double> vecList = TurtleVector.turtleToVec(model, matrix);
		return listToVec(vecList);
	}
	
	public static double[] modelToVec(Encoder model) {
		List<Double> vecList = null;
		ModelFormat format = model.getFormat();
		if(format.isBear()){
			vecList = BearVector.bearToVec((BearModel) model);
		} else if(format.isMonkey() || format.isChimp()) {
			vecList = MonkeyVector.monkeyToVec((MonkeyModel) model);
		} else if(format.isBee()) {
			vecList = BeeVector.beeToVec((BeeModel) model);
		} else if(format.isPenguin()) {
			vecList = PenguinVector.penguinToVec((PenguinModel) model);
		} else if(format.isLemur()) {
			vecList = LemurVector.lemurToVec((LemurModel) model);
		} else if(format.isTurtle()) {
			vecList = TurtleVector.turtleToVec((TurtleModel) model);
		} else if(format.isDeepBee()) {
			vecList = DeepBeeVector.deepbeeToVec((DeepBeeModel)model);
		}

		validateVec(model, vecList);
		return listToVec(vecList);
	}

	private static void validateVec(Encoder model, List<Double> vecList) {
		Warnings.check(vecList != null, "not populated");
		if(vecList.size() != model.getFormat().getNumParams()) {
			System.out.println("vectorDim: " + vecList.size());
			System.out.println("expected:  " + model.getFormat().getNumParams());
			throw new RuntimeException("wrong size!");
		}
	}

	/************************************************************************
	 *                        VEC TO MODEL
	 ***********************************************************************/

	public static String getMatrixForIndex(ModelFormat format, int elem) {
		if(format.isBear()) {
			return BearVector.getNameForIndex(format, elem);
		}
		if(format.isMonkey()) {
			return MonkeyVector.getNameForIndex(format, elem);
		}
		if(format.isPenguin()) {
			return PenguinVector.getNameForIndex(format, elem);
		}
		if(format.isTurtle()) {
			return TurtleVector.getNameForIndex(format, elem);
		}
		if(format.isLemur()) {
			return LemurVector.getNameForIndex(format, elem);
		}
		throw new RuntimeException("test");
	}
	
	public static Pair<TurtleModel, SimpleMatrix> vecToModelMatrix(double[] x) {
		List<Double> list = vecToList(x);
		return TurtleVector.vecToTurtleMatrix(list);
	}

	public static Encoder vecToModel(ModelFormat format, double[] params) {
		List<Double> list = vecToList(params);
		if(format.isBear()) {
			return BearVector.vecToBear(format, list);
		} else if(format.isMonkey() || format.isChimp()) {
			return MonkeyVector.vecToMonkey(format, list);
		} else if(format.isBee()) {
			return BeeVector.vecToBee(format, list);
		} else if(format.isPenguin()) { 
			return PenguinVector.vecToPenguin(format, list);
		} else if(format.isLemur()) {
			return LemurVector.vecToLemur(format, list);
		} else if(format.isTurtle()) {
			return TurtleVector.vecToTurtle(format, list);
		} else if(format.isDeepBee()) {
			return DeepBeeVector.vecToDeepBee(format, list);
		} 
		throw new RuntimeException("no");

	}

	public static List<Double> listPop(List<Double> encoderList, int size) {
		return encoderList.subList(size, encoderList.size());
	}

	private static List<Double> vecToList(double[] params) {
		return Doubles.asList(params);
	}

	private static double[] listToVec(List<Double> vecList) {
		return ArrayUtils.asPrimitiveDoubleArray(vecList);
	}

	public static void main(String[] args) {
		EncoderParams.setCodeVectorSize(16);
		ModelFormat format = new ModelFormat("karel", "monkey");
		MonkeyModel init = (MonkeyModel) EncoderFactory.makeRandom(format);
		double[] vec = modelToVec(init);
		for(int i = 0; i < 10; i++) {
			System.out.println(vec[i]);
		}
		Encoder modelPrime = vecToModel(format, vec);
		Warnings.check(init.equals(modelPrime));
		System.out.println("passed");
	}

}

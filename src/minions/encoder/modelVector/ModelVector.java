package minions.encoder.modelVector;

import java.util.*;

import minions.encoder.factory.EncoderFactory;
import models.encoder.EncoderParams;
import models.encoder.ModelFormat;
import models.encoder.encoders.BearModel;
import models.encoder.encoders.BeeModel;
import models.encoder.encoders.Encoder;
import models.encoder.encoders.MonkeyModel;
import util.Warnings;


public class ModelVector {

	/************************************************************************
	 *                        MODEL TO VEC
	 ***********************************************************************/

	public static double[] modelToVec(Encoder model) {
		List<Double> vecList = null;
		ModelFormat format = model.getFormat();
		if(format.isBear()){
			vecList = BearVector.bearToVec((BearModel) model);
		} else if(format.isMonkey() || format.isChimp()) {
			vecList = MonkeyVector.monkeyToVec((MonkeyModel) model);
		} else if(format.isBee()) {
			vecList = BeeVector.beeToVec((BeeModel) model);
		}

		validateVec(model, vecList);
		return listToVec(vecList);
	}

	private static void validateVec(Encoder model, List<Double> vecList) {
		Warnings.check(vecList != null, "not populated");
		if(vecList.size() != model.getFormat().getDimension()) {
			System.out.println("vectorDim: " + vecList.size());
			System.out.println("expected:  " + model.getFormat().getDimension());
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
		throw new RuntimeException("todo");
	}

	public static Encoder vecToModel(ModelFormat format, double[] params) {
		List<Double> list = vecToList(params);
		if(format.isBear()) {
			return BearVector.vecToBear(format, list);
		} else if(format.isMonkey() || format.isChimp()) {
			return MonkeyVector.vecToMonkey(format, list);
		} else if(format.isBee()) {
			return BeeVector.vecToBee(format, list);
		} else {
			throw new RuntimeException("no");
		}
	}

	public static List<Double> listPop(List<Double> encoderList, int size) {
		return encoderList.subList(size, encoderList.size());
	}

	private static List<Double> vecToList(double[] params) {
		List<Double> list = new LinkedList<Double>();
		for(int i = 0; i < params.length; i++) {
			list.add(params[i]);
		}
		return list;
	}

	private static double[] listToVec(List<Double> vecList) {
		double[] vec = new double[vecList.size()];
		for(int i = 0; i < vecList.size(); i++) {
			vec[i] = vecList.get(i);
		}

		return vec;
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

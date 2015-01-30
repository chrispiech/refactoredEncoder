package minions.encoder.modelVector;

import java.util.LinkedList;
import java.util.List;

import models.encoder.ModelFormat;
import models.encoder.encoders.Encoder;
import models.encoder.encoders.models.BeeModel;
import models.encoder.encoders.models.DeepBeeModel;
import models.encoder.encoders.state.DeepStateDecoder;
import models.encoder.encoders.state.DeepStateEncoder;
import models.encoder.encoders.state.StateDecoder;
import models.encoder.encoders.state.StateEncoder;
import util.Warnings;

public class DeepBeeVector {

	public static Encoder vecToDeepBee(ModelFormat format, List<Double> list) {
		int numOutParams = format.getStateDecoderDimension();

		int dimension = format.getNumParams();
		Warnings.check(dimension == list.size());

		List<Double> outList = list.subList(0, numOutParams);
		List<Double> inList = list.subList(numOutParams, dimension);

		// extract outs
		DeepStateDecoder output = DeepStateDecoderVector.vecToDecoder(format, outList);

		// extract ins
		DeepStateEncoder input = DeepStateEncoderVector.vecToEncoder(format, inList);


		return new DeepBeeModel(format, output, input);
	}

	public static List<Double> deepbeeToVec(DeepBeeModel model) {
		List<Double> vecList = new LinkedList<Double>();
		ModelFormat format = model.getFormat();

		// add out model
		DeepStateDecoder stateDecoder = model.getStateDecoder();
		vecList.addAll(DeepStateDecoderVector.decoderToVec(format, stateDecoder));
		Warnings.check(vecList.size() == format.getStateDecoderDimension());

		// add in model
		DeepStateEncoder stateEncoder = model.getStateEncoder();
		List<Double> encoderList = DeepStateEncoderVector.encoderToVec(format, stateEncoder);
		vecList.addAll(encoderList);
		Warnings.check(encoderList.size() == format.getStateEncoderDimension());

		return vecList;
	}

}

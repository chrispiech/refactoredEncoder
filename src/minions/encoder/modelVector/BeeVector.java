package minions.encoder.modelVector;

import java.util.LinkedList;
import java.util.List;

import util.Warnings;
import models.encoder.ModelFormat;
import models.encoder.encoders.BearModel;
import models.encoder.encoders.BeeModel;
import models.encoder.encoders.Encoder;
import models.encoder.encoders.MonkeyModel;
import models.encoder.encoders.ProgramEncoder;
import models.encoder.encoders.StateDecoder;
import models.encoder.encoders.StateEncoder;

public class BeeVector {

	public static Encoder vecToBee(ModelFormat format, List<Double> list) {
		int numOutParams = format.getStateDecoderDimension();
		
		int dimension = format.getDimension();
		Warnings.check(dimension == list.size());
		
		List<Double> outList = list.subList(0, numOutParams);
		List<Double> inList = list.subList(numOutParams, dimension);

		// extract outs
		StateDecoder output = StateDecoderVector.vecToDecoder(format, outList);
		
		// extract ins
		StateEncoder input = StateEncoderVector.vecToEncoder(format, inList);


		return new BeeModel(format, output, input);
	}

	
	public static List<Double> beeToVec(BeeModel model) {
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
}

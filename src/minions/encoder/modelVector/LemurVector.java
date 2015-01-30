package minions.encoder.modelVector;

import java.util.ArrayList;
import java.util.List;

import models.encoder.ModelFormat;
import models.encoder.encoders.Encoder;
import models.encoder.encoders.models.LemurModel;
import models.encoder.encoders.programEncoder.ProgramEncoderMatrix;
import models.encoder.encoders.state.StateDecoder;
import models.encoder.encoders.state.StateEncoder;
import util.Warnings;

public class LemurVector {

	public static List<Double> lemurToVec(LemurModel model) {
		List<Double> vecList = new ArrayList<Double>();
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
		
		// add program model
		List<Double> programList = ProgramVector.programToVec(model);
		vecList.addAll(programList);
		Warnings.check(programList.size() == format.getProgramEncoderDimension());

		return vecList;
	}

	public static Encoder vecToLemur(ModelFormat format, List<Double> list) {
		int numOutParams = format.getStateDecoderDimension();
		int numInputParams = format.getStateEncoderDimension();
		int inputOutputParams = numOutParams + numInputParams;
		
		int dimension = format.getNumParams();
		Warnings.check(dimension == list.size());
		
		List<Double> outList = list.subList(0, numOutParams);
		List<Double> inList = list.subList(numOutParams, inputOutputParams);
		List<Double> programList = list.subList(inputOutputParams, list.size());

		// extract outs
		StateDecoder output = StateDecoderVector.vecToDecoder(format, outList);
		
		// extract ins
		StateEncoder input = StateEncoderVector.vecToEncoder(format, inList);

		// extract internal
		ProgramEncoderMatrix program = ProgramVector.vecToProgramMatrix(format, programList);

		return new LemurModel(format, program, output, input);
	}

	public static String getNameForIndex(ModelFormat format, int elem) {
		int numOutParams = format.getStateDecoderDimension();
		int numInputParams = format.getStateEncoderDimension();
		int inputOutputParams = numOutParams + numInputParams;
		
		if(elem < numOutParams) {
			return "output";
		} else if(elem < inputOutputParams) {
			return "input";
		} else {
			int offset = elem - inputOutputParams;
			return ProgramVector.getNameForIndex(format, offset);
		}
	}

}

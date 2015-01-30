package minions.encoder.modelVector;

import java.util.*;

import org.ejml.simple.SimpleMatrix;

import util.MatrixUtil;
import util.Warnings;
import minions.encoder.factory.EncoderFactory;
import models.encoder.EncoderParams;
import models.encoder.ModelFormat;
import models.encoder.encoders.*;
import models.encoder.encoders.models.BearModel;
import models.encoder.encoders.programEncoder.ProgramEncoderVec;
import models.encoder.encoders.state.StateDecoder;
import models.encoder.decoders.*;

public class BearVector {

	public static Encoder vecToBear(ModelFormat format, List<Double> list) {
		int numOutParams = format.getStateDecoderDimension();
		int dimension = format.getNumParams();
		Warnings.check(dimension == list.size());
		
		List<Double> outList = list.subList(0, numOutParams);
		List<Double> programList = list.subList(numOutParams, list.size());

		// extract outs
		StateDecoder output = StateDecoderVector.vecToDecoder(format, outList);

		// extract internal
		ProgramEncoderVec program = ProgramVector.vecToProgram(format, programList);

		return new BearModel(format, program, output);
	}

	public static List<Double> bearToVec(BearModel model) {
		List<Double> vecList = new LinkedList<Double>();
		ModelFormat format = model.getFormat();
		
		// add out model
		StateDecoder decoder = model.getStateDecoder();
		vecList.addAll(StateDecoderVector.decoderToVec(format, decoder));

		// add program model
		vecList.addAll(ProgramVector.programToVec(model));

		return vecList;
	}
	
	public static String getNameForIndex(ModelFormat format, int elem) {
		int numOutParams = format.getStateDecoderDimension();
		if(elem < numOutParams) {
			return "output";
		} else {
			return ProgramVector.getNameForIndex(format, elem - numOutParams);
		}
	}

}

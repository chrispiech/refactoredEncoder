package minions.encoder.modelVector;

import java.util.LinkedList;
import java.util.List;

import util.Warnings;
import models.encoder.ModelFormat;
import models.encoder.dimension.PenguinDimension;
import models.encoder.encoders.Encoder;
import models.encoder.encoders.Mixer;
import models.encoder.encoders.MonkeyModel;
import models.encoder.encoders.PenguinModel;
import models.encoder.encoders.ProgramEncoder;
import models.encoder.encoders.StateDecoder;
import models.encoder.encoders.StateEncoder;

public class PenguinVector {

	public static PenguinModel vecToPenguin(ModelFormat format, List<Double> list) {
		int numOutParams = format.getStateDecoderDimension();
		int numInputParams = format.getStateEncoderDimension();
		int inputOutputParams = numOutParams + numInputParams;
		int stateProgramParams = inputOutputParams + format.getProgramEncoderDimension();
		
		int dimension = format.getNumParams();
		Warnings.check(dimension == list.size());
		
		List<Double> outList = list.subList(0, numOutParams);
		List<Double> inList = list.subList(numOutParams, inputOutputParams);
		List<Double> programList = list.subList(inputOutputParams, stateProgramParams);
		List<Double> mixerList = list.subList(stateProgramParams, list.size());

		PenguinDimension dimObj = (PenguinDimension)(format.getDimension());
		Warnings.check(mixerList.size() == dimObj.getCompositionDim());
		
		// extract outs
		StateDecoder output = StateDecoderVector.vecToDecoder(format, outList);
		
		// extract ins
		StateEncoder input = StateEncoderVector.vecToEncoder(format, inList);

		// extract internal
		ProgramEncoder program = ProgramVector.vecToProgram(format, programList);
		
		// extract composer
		Mixer composer = MixerVector.vecToMixer(format, mixerList);

		return new PenguinModel(format, program, output, input, composer);
	}
	
	public static List<Double> penguinToVec(PenguinModel model) {
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
		
		// add program model
		List<Double> programList = ProgramVector.programToVec(model);
		vecList.addAll(programList);
		Warnings.check(programList.size() == format.getProgramEncoderDimension());
		
		// add mixer model
		Mixer mixer = model.getCombiner();
		List<Double> mixerList = MixerVector.mixerToVec(mixer);
		vecList.addAll(mixerList);

		return vecList;
	}

	public static String getNameForIndex(ModelFormat format, int elem) {
		PenguinDimension d = (PenguinDimension) format.getDimension();
		int numOutParams = format.getStateDecoderDimension();
		int numInputParams = format.getStateEncoderDimension();
		int stateParams = numOutParams + numInputParams;
		int stateCodeParams = stateParams + format.getProgramEncoderDimension();
		
		if(elem < numOutParams) {
			return "output";
		} else if(elem < stateParams) {
			return "input";
		} else if(elem < stateCodeParams){
			int offset = elem - stateParams;
			return ProgramVector.getNameForIndex(format, offset);
		} else {
			return "compose";
		}
	}

}

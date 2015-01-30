package minions.encoder.factory;

import util.Warnings;
import minions.encoder.EncoderSaver;
import models.encoder.EncoderParams;
import models.encoder.ModelFormat;
import models.encoder.encoders.Encoder;
import models.encoder.encoders.models.BeeModel;
import models.encoder.encoders.models.MonkeyModel;
import models.encoder.encoders.programEncoder.ProgramEncoderVec;
import models.encoder.encoders.state.StateDecoder;
import models.encoder.encoders.state.StateEncoder;

public class ChimpFactory {
	
	private static final String MODEL_NAME = "bumbleBee";

	public static Encoder makeInitial(ModelFormat format) {
		MonkeyModel randomMonkey = (MonkeyModel)EncoderFactory.makeRandom(format);
		BeeModel bee = (BeeModel) EncoderSaver.load(MODEL_NAME);
		int stateSize1 = bee.getFormat().getStateVectorSize();
		int stateSize2 = EncoderParams.getStateVectorSize();
		Warnings.check(stateSize1 == stateSize2);
		
		ProgramEncoderVec program = randomMonkey.getProgramEncoder();
		StateDecoder output = bee.getStateDecoder();
		StateEncoder input = bee.getStateEncoder();
		
		return new MonkeyModel(format, program, output, input);
	}

}

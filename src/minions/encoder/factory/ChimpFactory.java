package minions.encoder.factory;

import util.Warnings;
import minions.encoder.EncoderSaver;
import models.encoder.EncoderParams;
import models.encoder.ModelFormat;
import models.encoder.encoders.BeeModel;
import models.encoder.encoders.Encoder;
import models.encoder.encoders.MonkeyModel;
import models.encoder.encoders.ProgramEncoder;
import models.encoder.encoders.StateDecoder;
import models.encoder.encoders.StateEncoder;

public class ChimpFactory {
	
	private static final String MODEL_NAME = "bumbleBee";

	public static Encoder makeInitial(ModelFormat format) {
		MonkeyModel randomMonkey = (MonkeyModel)EncoderFactory.makeRandom(format);
		BeeModel bee = (BeeModel) EncoderSaver.load(MODEL_NAME);
		int stateSize1 = bee.getFormat().getStateVectorSize();
		int stateSize2 = EncoderParams.getStateVectorSize();
		Warnings.check(stateSize1 == stateSize2);
		
		ProgramEncoder program = randomMonkey.getProgramEncoder();
		StateDecoder output = bee.getStateDecoder();
		StateEncoder input = bee.getStateEncoder();
		
		return new MonkeyModel(format, program, output, input);
	}

}

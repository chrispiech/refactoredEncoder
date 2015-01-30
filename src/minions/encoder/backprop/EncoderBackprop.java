package minions.encoder.backprop;

import java.util.Collections;
import java.util.List;

import models.code.TestTriplet;
import models.encoder.EncoderParams;
import models.encoder.ModelFormat;
import models.encoder.encoders.Encoder;
import models.encoder.encoders.models.BearModel;
import models.encoder.encoders.models.BeeModel;
import models.encoder.encoders.models.DeepBeeModel;
import models.encoder.encoders.models.LemurModel;
import models.encoder.encoders.models.MonkeyModel;
import models.encoder.encoders.models.PenguinModel;

public class EncoderBackprop {
	
	public static Encoder derivative(Encoder model, TestTriplet t) {
		return derivative(model, Collections.singletonList(t));
	}

	public static Encoder derivative(Encoder model,
			List<TestTriplet> list) {
		
		ModelFormat format = model.getFormat();
		if(format.isBear()) {
			return BearBackprop.derivative((BearModel)model, list);
		}
		if(format.isMonkey()) {
			return MonkeyBackprop.derivativeWithDecay((MonkeyModel)model, list);
		}
		if(format.isBee()) {
			return BeeBackprop.derivativeWithDecay((BeeModel)model, list);
		}
		if(format.isChimp()) {
			return ChimpBackprop.derivativeWithDecay((MonkeyModel)model, list);
		}
		if(format.isPenguin()) {
			return PenguinBackprop.derivativeWithDecay((PenguinModel)model, list);
		}
		if(format.isLemur()) {
			return LemurBackprop.derivativeWithDecay((LemurModel)model, list);
		}
		if(format.isDeepBee()) {
			return DeepBeeBackprop.derivativeWithDecay((DeepBeeModel)model, list);
		}
		throw new RuntimeException("unknown model");
	}

}

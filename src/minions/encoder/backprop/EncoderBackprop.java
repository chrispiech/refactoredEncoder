package minions.encoder.backprop;

import java.util.List;

import models.code.TestTriplet;
import models.encoder.ModelFormat;
import models.encoder.encoders.BearModel;
import models.encoder.encoders.BeeModel;
import models.encoder.encoders.Encoder;
import models.encoder.encoders.MonkeyModel;

public class EncoderBackprop {

	public static Encoder derivativeWithDecay(Encoder model,
			List<TestTriplet> list) {
		ModelFormat format = model.getFormat();
		if(format.isBear()) {
			return BearBackprop.derivativeWithDecay((BearModel)model, list);
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
		throw new RuntimeException("unknown model type");
	}
	
	public static Encoder derivative(Encoder model,
			List<TestTriplet> list) {
		ModelFormat format = model.getFormat();
		if(format.isBear()) {
			return BearBackprop.derivative((BearModel)model, list);
		}
		if(format.isMonkey()) {
			return MonkeyBackprop.derivative((MonkeyModel)model, list);
		}
		if(format.isBee()) {
			return BeeBackprop.derivative((BeeModel) model, list);
		}
		if(format.isChimp()) {
			return ChimpBackprop.derivative((MonkeyModel)model, list);
		}
		throw new RuntimeException("unknown model type");
	}

}

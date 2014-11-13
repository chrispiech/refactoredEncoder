package run.validate;

import minions.encoder.factory.EncoderFactory;
import minions.encoder.modelVector.ModelVector;
import models.encoder.EncoderParams;
import models.encoder.ModelFormat;
import models.encoder.encoders.Encoder;
import models.encoder.encoders.MonkeyModel;
import util.Warnings;

public class BlockyMonkeyVectorTest {

	private void run() {
		EncoderParams.setCodeVectorSize(16);
		ModelFormat format = new ModelFormat("blocky", "monkey");
		MonkeyModel init = (MonkeyModel) EncoderFactory.makeRandom(format);
		double[] vec = ModelVector.modelToVec(init);
		for(int i = 0; i < 10; i++) {
			System.out.println(vec[i]);
		}
		Encoder modelPrime = ModelVector.vecToModel(format, vec);
		Warnings.check(init.equals(modelPrime));
		System.out.println("passed");
	}
	
	public static void main(String[] args) {
		new BlockyMonkeyVectorTest().run();
	}
	
}

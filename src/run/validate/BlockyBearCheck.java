package run.validate;

import java.util.ArrayList;
import java.util.List;

import minions.encoder.EncoderSaver;
import minions.minimizer.AdaGrad;
import minions.program.PostExperimentLoader;
import minions.program.PrePostExperimentLoader;
import models.code.TestTriplet;
import models.encoder.EncoderParams;
import models.encoder.ModelFormat;
import models.encoder.encoders.Encoder;
import run.train.BlockyMonkeyTrain;
import util.FileSystem;

public class BlockyBearCheck {

	private static final String LANGUAGE = "blocky";
	private static final String MODEL_TYPE = "bear";
	
	private List<TestTriplet> trainSet = null;
	private List<TestTriplet> testSet = null;
	private ModelFormat format = null;

	private void run() {
		System.out.println("hello world");
		EncoderParams.setLanguage("blocky");
		EncoderParams.setCodeVectorSize(9);
		FileSystem.setAssnId("Hoc18");
		FileSystem.setExpId("postExp");

		format = new ModelFormat(LANGUAGE, MODEL_TYPE);
		trainSet = PostExperimentLoader.loadTests("train", 200, format.getLanguage());
		
		validate(trainSet);
	}
	
	public void validate(List<TestTriplet> set) {
		int epochs = 100;  
		double eta = EncoderParams.getLearningRate();
		int miniBatchSize = 1000;
		double[] loss = new double[epochs];
		AdaGrad.checkGrad(format, set, epochs, miniBatchSize, eta, loss);
	}
	

	public static void main(String[] args) {
		new BlockyBearCheck().run();
	}
}

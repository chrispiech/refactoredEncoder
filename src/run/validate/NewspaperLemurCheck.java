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

public class NewspaperLemurCheck {

	private static final String LANGUAGE = "karel";
	private static final String MODEL_TYPE = "lemur";
	
	private List<TestTriplet> trainSet = null;
	private List<TestTriplet> testSet = null;
	private ModelFormat format = null;

	private void run() {
		System.out.println("hello world");
		EncoderParams.setStateVectorSize(2);
		EncoderParams.setCodeVectorSize(4);
		EncoderParams.setWeightDecay(0.1);
		EncoderParams.setInitStd(5);
		EncoderParams.stateHasSize = true;
		EncoderParams.worldRows = 5;
		FileSystem.setAssnId("Newspaper");
		FileSystem.setExpId("prePostExp");

		format = new ModelFormat(LANGUAGE, MODEL_TYPE);
		trainSet = PrePostExperimentLoader.loadTriplets("train", 100, format.getLanguage());
		
		validate(trainSet);
	}
	
	public void validate(List<TestTriplet> set) {
		int epochs = 100;  
		double eta = EncoderParams.getLearningRate();
		int miniBatchSize = 10;
		double[] loss = new double[epochs];
		AdaGrad.checkGrad(format, set, epochs, miniBatchSize, eta, loss);
	}
	

	public static void main(String[] args) {
		new NewspaperLemurCheck().run();
	}
}

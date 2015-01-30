package run.toys;

import java.util.List;

import minions.minimizer.AdaGradCluster;
import minions.program.PrePostExperimentLoader;
import models.code.TestTriplet;
import models.encoder.EncoderParams;
import models.encoder.ModelFormat;
import util.FileSystem;

public class SquareBeepers {

	public static void main(String[] args) {
		new SquareBeepers().run();
	}

	private void run() {
		ModelFormat format = new ModelFormat("toy", "lemur");
		FileSystem.setAssnId("SquareBeepers");
		setParameters(64);
		
		FileSystem.setExpId("prePostExp");
		System.out.println("load test...");
		List<TestTriplet> tests = PrePostExperimentLoader.loadTrainSet(format.getLanguage());
		System.out.println("num tests: " +tests.size());
		
		int maxHours = 24 * 2;
		String name = "lemur";
		AdaGradCluster.train(format, tests, tests, maxHours, name);
		
	}
	
	private void setParameters(int n) {
		EncoderParams.setCodeVectorSize(n);
		EncoderParams.setStateVectorSize(EncoderParams.getSqrtN());
		EncoderParams.setWeightDecay(0.00);
		EncoderParams.setLearningRate(0.02);
		EncoderParams.setMiniBatchSize(1);
	}
}

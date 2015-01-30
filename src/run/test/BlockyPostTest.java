package run.test;

import java.util.*;

import minions.encoder.EncoderSaver;
import minions.encoder.ModelTester;
import minions.program.PostExperimentLoader;
import models.code.TestTriplet;
import models.encoder.EncoderParams;
import models.encoder.encoders.Encoder;
import models.encoder.encoders.models.BearModel;
import models.language.BlockyLanguage;

import org.ejml.simple.SimpleMatrix;

import util.FileSystem;
import util.MatrixUtil;

public class BlockyPostTest {

	private void run() {
		EncoderParams.setCodeVectorSize(10);
		FileSystem.setAssnId("Hoc18");
		FileSystem.setExpId("postExp");
		Encoder model = EncoderSaver.load("brown2-epoch10");
		List<TestTriplet>testSet = PostExperimentLoader.loadTests("train", -1, new BlockyLanguage());
		//testModel(model, testSet, 1000);
		//testModel(model, testSet, 5000);
		testModel(model, testSet, testSet.size());
	}

	private void testModel(Encoder model, List<TestTriplet> testSet, int size) {
		double acc = ModelTester.calcAccuracy(model, testSet.subList(0, size));
		System.out.println("acc (" + size + ") = " + acc + "%");
	}
	
	public static void main(String[] args) {
		new BlockyPostTest().run();
	}
}

package run.test;

import java.util.*;

import minions.encoder.EncoderSaver;
import minions.encoder.ModelTester;
import minions.program.PostExperimentLoader;
import models.code.TestTriplet;
import models.encoder.EncoderParams;
import models.encoder.encoders.Encoder;
import models.encoder.encoders.models.BearModel;
import models.language.KarelLanguage;

import org.ejml.simple.SimpleMatrix;

import util.FileSystem;
import util.MatrixUtil;

public class NewspaperTest {

	private void run() {
		EncoderParams.setCodeVectorSize(40);
		FileSystem.setAssnId("Newspaper");
		FileSystem.setExpId("postExp");
		Encoder model = EncoderSaver.load("second");
		List<TestTriplet>testSet = PostExperimentLoader.loadFold("homeWorld", 9, new KarelLanguage());
		testSet = PostExperimentLoader.removeRecursive(testSet, new KarelLanguage());
		//testModel(model, testSet, 1000);
		//testModel(model, testSet, 5000);
		testModel(model, testSet, testSet.size());
	}

	private void testModel(Encoder model, List<TestTriplet> testSet, int size) {
		double acc = ModelTester.calcAccuracy(model, testSet.subList(0, size));
		System.out.println("acc (" + size + ") = " + acc + "%");
	}
	
	public static void main(String[] args) {
		new NewspaperTest().run();
	}
}

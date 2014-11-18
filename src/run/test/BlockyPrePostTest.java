package run.test;

import java.util.*;

import minions.encoder.EncoderSaver;
import minions.encoder.ModelTester;
import minions.program.PostExperimentLoader;
import models.code.TestTriplet;
import models.encoder.EncoderParams;
import models.encoder.encoders.BearModel;
import models.encoder.encoders.Encoder;
import models.language.BlockyLanguage;

import org.ejml.simple.SimpleMatrix;

import util.FileSystem;
import util.MatrixUtil;

public class BlockyPrePostTest {
	/*bonobo-epoch1
	bumbleBee
	gorilla-epoch1
	macaque-epoch11
	pygmy-epoch1*/
	
	private void run() {
		EncoderParams.setLanguage("blocky");
		FileSystem.setAssnId("Hoc18");

		FileSystem.setExpId("postExp"); 
		List<TestTriplet>testSet = PostExperimentLoader.loadTests("train", -1, new BlockyLanguage());

		FileSystem.setExpId("prePostExp");
		Encoder model = EncoderSaver.load("gorilla-epoch10");
		testModel(model, testSet, testSet.size());
		/*for(int i = 0; i < 5; i++) {
			Encoder model = EncoderSaver.load("macaque0-epoch" + i);
			testModel(model, testSet, testSet.size());
		}*/
	}

	private void testModel(Encoder model, List<TestTriplet> testSet, int size) {
		double acc = ModelTester.calcAccuracy(model, testSet.subList(0, size));
		System.out.println("acc (" + size + ") = " + acc + "%");
	}

	public static void main(String[] args) {
		new BlockyPrePostTest().run();
	}
}

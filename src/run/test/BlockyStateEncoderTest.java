package run.test;

import java.util.*;

import minions.encoder.EncoderSaver;
import minions.encoder.ModelTester;
import minions.program.PostExperimentLoader;
import minions.program.PrePostExperimentLoader;
import models.code.TestTriplet;
import models.encoder.EncoderParams;
import models.encoder.encoders.BearModel;
import models.encoder.encoders.Encoder;
import models.encoder.encoders.types.StateEncodable;
import models.language.BlockyLanguage;

import org.ejml.simple.SimpleMatrix;

import util.FileSystem;
import util.MatrixUtil;

public class BlockyStateEncoderTest {

	private void run() {
		EncoderParams.setLanguage("blocky");
		FileSystem.setAssnId("Hoc18");

		FileSystem.setExpId("prePostExp");
		List<TestTriplet>testSet = PrePostExperimentLoader.loadTests("test", -1, new BlockyLanguage());
		
		StateEncodable model = (StateEncodable) EncoderSaver.load("bumbleBee");
		testModel(model, testSet, testSet.size());
		
		/*for(int i = 0; i < 100; i++) {
			StateEncodable model = (StateEncodable) EncoderSaver.load("macaqueDecay-epoch" + i);
			testModel(model, testSet, testSet.size());
		}*/
	}

	private void testModel(StateEncodable model, List<TestTriplet> testSet, int size) {
		double acc = ModelTester.stateAutoEncoderAccuracy(model, testSet.subList(0, size));
		System.out.println("acc (" + size + ") = " + acc + "%");
	}

	public static void main(String[] args) {
		new BlockyStateEncoderTest().run();
	}
}

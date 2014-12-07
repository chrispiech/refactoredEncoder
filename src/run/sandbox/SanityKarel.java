package run.sandbox;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import minions.encoder.EncoderSaver;
import minions.encoder.ModelTester;
import minions.encoder.factory.EncoderFactory;
import minions.program.PostExperimentLoader;
import models.code.TestTriplet;
import models.encoder.CodeVector;
import models.encoder.EncoderParams;
import models.encoder.ModelFormat;
import models.encoder.encoders.BearModel;
import models.encoder.encoders.StateDecoder;
import models.encoder.encoders.Encoder;
import models.encoder.encoders.InternalEncoder;
import models.encoder.encoders.ProgramEncoder;
import models.encoder.decoders.*;
import models.language.KarelLanguage;

import org.ejml.simple.SimpleMatrix;

import util.FileSystem;
public class SanityKarel {

	private void run() {
		EncoderParams.setCodeVectorSize(50);
		FileSystem.setAssnId("Newspaper");
		FileSystem.setExpId("postExp");
		List<TestTriplet>testSet = PostExperimentLoader.loadFolds("homeWorld", 1, new KarelLanguage());
		Encoder m = EncoderFactory.makeRandom(new ModelFormat("karel", "bear"));
		double acc = ModelTester.calcAccuracy(m, testSet);
		System.out.println("acc = " + acc + "%");
	}

	public static void main(String[] args) {
		new SanityKarel().run();
	}
}

package run.clusterExp;

import java.util.List;

import minions.encoder.EncoderSaver;
import minions.minimizer.AdaGradCluster;
import minions.program.PostExperimentLoader;
import minions.program.PrePostExperimentLoader;
import models.code.TestTriplet;
import models.encoder.EncoderParams;
import models.encoder.ModelFormat;
import models.encoder.encoders.Encoder;
import util.FileSystem;
import util.RandomUtil;
import util.Warnings;

public class ClusterDatasetReduce {
	private static final String LANGUAGE = "blocky";
	private static final String MODEL_TYPE = "monkey";
	private static final String NAME_BASE = "datareduce";
	
	List<TestTriplet> trainSet = null;
	List<TestTriplet> testSet = null;
	ModelFormat format = null;

	private void run(int dataSetSize) {
		format = new ModelFormat(LANGUAGE, MODEL_TYPE);
		FileSystem.setAssnId("Hoc18");
		
		setParameters();
		
		FileSystem.setExpId("postExp");
		System.out.println("load test...");
		testSet = PostExperimentLoader.loadTests("test", -1,format.getLanguage());
		
		FileSystem.setExpId("prePostExp");
		System.out.println("load train...");
		trainSet = PrePostExperimentLoader.loadTripletsRandom("train", -1, format.getLanguage());
		System.out.println("train set size: " + trainSet.size());
		System.out.println("train set asts:" + dataSetSize );
		train("-" + dataSetSize);
	}
	
	private void setParameters() {
		EncoderParams.setCodeVectorSize(100);
		EncoderParams.setStateVectorSize(EncoderParams.getSqrtN());
		
		EncoderParams.setWeightDecay(0.0001);
		EncoderParams.setLearningRate(0.01);
		EncoderParams.setMiniBatchSize(500);
	}
	

	private void train(String nameExt) {
		int maxHours = 24 * 2;
		String name = NAME_BASE + "-" + nameExt;
		AdaGradCluster.train(format, trainSet, testSet, maxHours, name);
	}
	
	public static void main(String[] args) {
		Warnings.check(args.length == 2, "wrong num args");
		String argDataPath = args[0];
		String argIndex = args[1];
		
		FileSystem.setDataDir(argDataPath);
		int i = Integer.parseInt(argIndex);
		int size = 5000 * (i + 1);
		Warnings.check(size < 71000, "too large");
		new ClusterDatasetReduce().run(size);
	}
}

package minions.minimizer;

import java.io.File;
import java.io.ObjectInputStream.GetField;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

import util.FileSystem;
import util.MatrixUtil;
import util.Warnings;
import edu.stanford.nlp.optimization.SGDMinimizer;
import minions.encoder.EncoderSaver;
import minions.encoder.ModelTester;
import minions.encoder.ModelValueAt;
import minions.encoder.backprop.EncoderBackprop;
import minions.encoder.factory.EncoderFactory;
import minions.encoder.modelVector.ModelVector;
import minions.program.PrePostExperimentLoader;
import models.code.TestTriplet;
import models.encoder.EncoderParams;
import models.encoder.ModelFormat;
import models.encoder.encoders.Encoder;
import models.encoder.encoders.models.BearModel;

public class AdaGradCluster2 {
	private ModelFormat format;

	// some bookkeeping.
	private long algoStartTimeMs;
	private long miniBatchStartTimeMs;
	private String name;

	private String log = "";
	private int maxHours;
	private int numMinibatches;

	private List<TestTriplet> test;
	private List<TestTriplet> train;


	public static Encoder train(ModelFormat f, List<TestTriplet> train, int maxHours, String name) {
		AdaGradCluster2 min = new AdaGradCluster2();
		min.format = f;
		min.name = name;
		min.maxHours = maxHours;
		min.train = train;
		double[] x = min.SGD(train);
		return ModelVector.vecToModel(min.format, x);
	}

	private double[] SGD(List<TestTriplet> train) {
		System.out.println("Training!");
		System.out.println("---------");
		System.out.println("dim = " + format.getDimension().getDimension());
		System.out.println(EncoderSaver.makeNotes());

		setup(train);

		double eta = EncoderParams.getLearningRate();
		Encoder init = EncoderFactory.makeInitial(format);
		double[] x = ModelVector.modelToVec(init);
		double[] gradStore = new double[x.length];

		algoStartTimeMs = System.currentTimeMillis();
		int epoch = 0;
		while(timeHasNotExpired()){
			miniBatchStartTimeMs = System.currentTimeMillis();

			List<List<TestTriplet>> miniBatches = makeMiniBatches(train);

			for(int t = 0; t < miniBatches.size(); t++) {
				Encoder model = getModel(x);
				Encoder grad = EncoderBackprop.derivative(model, miniBatches.get(t));
				double[] gradVec =  ModelVector.modelToVec(grad);
				for(int i = 0; i < gradVec.length; i++) {
					if(gradVec[i] == 0) continue;
					gradStore[i] += Math.pow(gradVec[i], 2); 
					double weight = eta/Math.sqrt(gradStore[i]);
					x[i] = (x[i] - weight*gradVec[i]);
				}
				//miniBatchOutput(epoch, t);
			}
			epochOutput(train, x, epoch);
			epoch++;
		}

		return x;
	}

	private void epochOutput(List<TestTriplet> train, double[] x, int epoch) {
		Encoder model = getModel(x);
		double value = ModelValueAt.valueAt(model, train);
		double testAccuracy = getTestAccuracy(model);
		double algoTimeS = getAlgorithmTimeS();
		addLogLine(epoch, value, testAccuracy, algoTimeS);

		System.out.println(value);

		String notes = EncoderSaver.makeNotes();
		notes += value;

		EncoderSaver.save(x, format, name, "model", notes);
		
		saveLog();
	}

	private double getTestAccuracy(Encoder model) {
		return ModelTester.calcAccuracyPartialCredit(model, test);
	}

	private void miniBatchOutput(int epoch, int batch) {
		String output = "";
		output += "  epoch " + epoch + ", batch " + batch+ ". ";

		if(batch != 0) {
			double batchTimeS = getBatchTimeS();
			int done = batch;
			int todo = numMinibatches - done;
			int timeToCompleteS = (int) ((batchTimeS / done) * todo);

			output += "timeLeft = " + timeToCompleteS + "s";
		}

		System.out.println(output);
	}

	private List<List<TestTriplet>> makeMiniBatches(
			List<TestTriplet> train) {
		Collections.shuffle(train);	

		List<List<TestTriplet>> miniBatches = new ArrayList<List<TestTriplet>>();

		int miniBatchSize = EncoderParams.getMiniBatchSize();
		// will floor the value
		numMinibatches = train.size() / miniBatchSize;
		
		// all mini-batches are the same size
		int start = 0;
		for(int k = 0; k < numMinibatches; k++) {
			int end = start + EncoderParams.getMiniBatchSize();
			miniBatches.add(train.subList(start, end));
			start = end;
		}

		return miniBatches;
	}


	public double[] getInitial() {
		BearModel init = new BearModel(format);
		return ModelVector.modelToVec(init);
	}

	private boolean timeHasNotExpired() {
		double algorithmTimeS = getAlgorithmTimeS();
		double hours = algorithmTimeS / 3600.0;
		return hours < maxHours;
	}

	private double getAlgorithmTimeS() {
		long time = System.currentTimeMillis();
		return (time - algoStartTimeMs) / 1000.0;
	}

	private double getBatchTimeS() {
		long time = System.currentTimeMillis();
		return (time - miniBatchStartTimeMs) / 1000.0;
	}

	public void setup(List<TestTriplet> train) {
		log = "log start " + new SimpleDateFormat("dd-MM-yyyy").format(new Date()) + "\n";
		log += "num params: " + format.getDimension().getDimension() + "\n";
		log += EncoderSaver.makeNotes() + "\n";
		log += "-------------\n";
		addLogHeader();
	}

	public void addLogHeader() {
		log += "epoch, value, testAccuracy, algoTime(s)\n";
	}

	private void addLogLine(int epoch, double value, double testAccuracy, double algoTimeS) {
		String line = "";
		line += value;
		log += line + "\n";
	}
	
	private void saveLog() {
		File expDir = FileSystem.getExpDir();
		File savedModelsDir = new File(expDir, "savedModels");
		File modelDir = new File(savedModelsDir, name);
		FileSystem.createFile(modelDir, "log.txt", log);
		
	}

	public Encoder getModel(double[] params) {
		return ModelVector.vecToModel(format, params);
	}

	

}

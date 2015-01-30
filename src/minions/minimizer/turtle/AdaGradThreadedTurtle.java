package minions.minimizer.turtle;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Vector;

import minions.encoder.EncoderSaver;
import minions.encoder.ModelTester;
import minions.encoder.ModelValueAt;
import minions.encoder.factory.EncoderFactory;
import minions.encoder.modelVector.ModelVector;
import minions.program.PrePostExperimentLoader;
import models.code.State;
import models.code.TestTriplet;
import models.encoder.EncoderParams;
import models.encoder.ModelFormat;
import models.encoder.encoders.Encoder;
import models.encoder.encoders.models.BearModel;
import models.encoder.encoders.models.TurtleModel;

import org.ejml.simple.SimpleMatrix;

import util.FileSystem;
import util.MatrixUtil;

public class AdaGradThreadedTurtle {
	private ModelFormat format;

	// some bookkeeping.
	private long algoStartTimeMs;
	private long miniBatchStartTimeMs;
	private String name;

	private List<TestTriplet> trainSet;

	private String log = "";
	private int maxHours;
	private int numMinibatches;

	private File matrixDir = null;

	public static Encoder train(ModelFormat f, String expName, int maxHours,
			String name) {
		AdaGradThreadedTurtle min = new AdaGradThreadedTurtle();
		min.format = f;
		min.name = name;
		min.maxHours = maxHours;

		double[] x = min.SGD();
		return ModelVector.vecToModel(min.format, x);
	}

	private double[] SGD() {
		System.out.println("what");
		System.out.println("Training!");
		System.out.println("---------");
		System.out.println("dim = " + format.getDimension().getDimension());
		System.out.println(EncoderSaver.makeNotes());

		File savedDir = new File(FileSystem.getExpDir(), "savedModels");
		File modelDir = new File(savedDir, name);
		matrixDir = new File(modelDir, "programMatricies");
		setup();
		loadTrainSet();
		System.out.println("miniBatches per epoch: " + trainSet.size()/EncoderParams.getMiniBatchSize());

		System.out.println("cores: " + Runtime.getRuntime().availableProcessors());
		Map<String, List<TestTriplet>> testMap = makeTestMap();

		double eta = EncoderParams.getLearningRate();
		int M = EncoderParams.getM();
		Encoder init = EncoderFactory.makeInitial(format);
		double[] model_x = ModelVector.modelToVec(init);
		double[] model_gs = new double[model_x.length];

		algoStartTimeMs = System.currentTimeMillis();

		int epoch = 0;

		// continue a lot...
		while(timeHasNotExpired()){
			miniBatchStartTimeMs = System.currentTimeMillis();

			List<List<String>> batches = shuffleMiniBatches(testMap.keySet());

			double sumError = 0;
			double sumAccuracy = 0;
			int numTested = 0;

			// run through the whole dataset
			for(int t = 0; t < batches.size(); t++) {
				Vector<String> miniBatch = new Vector<String>(batches.get(t));
				TurtleModel model = (TurtleModel) ModelVector.vecToModel(format, model_x);
				double sumMiniError = 0;
				double sumMiniAccuracy = 0;
				int miniTested = 0;
				double[] modelGrad = new double[model_x.length];
				List<TurtleThread> threadList = new ArrayList<TurtleThread>();
				TurtleThread.miniBatch = miniBatch;
				for(int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
					TurtleThread thread = new TurtleThread(testMap, model, modelGrad, matrixDir);
					thread.start();
					threadList.add(thread);
				}
				for(TurtleThread thread : threadList) {
					try {
						thread.join();
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}

					sumMiniError += thread.getSumError();
					sumMiniAccuracy += thread.getSumAccuracy();
					miniTested += thread.getTested();
				}
				System.out.print(".");
				// AdaGrad update for the model vector
				adaGradModel(eta, model_x, model_gs, modelGrad);
				//miniBatchOutput(sumMiniError, sumMiniAccuracy, miniTested);
				sumError += sumMiniError;
				sumAccuracy += sumMiniAccuracy;
				numTested += miniTested;
			}
			epochOutput(model_x, sumError, sumAccuracy, numTested);
			epoch++;
		}

		//return x;
		throw new RuntimeException("not ready");
	}





	

	private void miniBatchOutput(double sumError, double sumAccuracy, int numTested) {
		System.out.println("mini " + formatOutput(sumError, sumAccuracy, numTested));
	}

	private String formatOutput(double sumError, double sumAccuracy, int numTested) {
		NumberFormat formatter = new DecimalFormat("0.00000");     
		double aveError = sumError / numTested;
		double aveAccuracy = sumAccuracy / numTested;
		return "(" + 
		formatter.format(aveError) + ",    \t" + 
		formatter.format(aveAccuracy) + 
		")";
	}

	private Map<String, TurtleMatrix> getMatrixValues(List<String> miniBatch) {
		Map<String, TurtleMatrix> matrixValues = new HashMap<String, TurtleMatrix>();
		for(String s : miniBatch) {
			matrixValues.put(s, loadMatrix(s));
		}
		return matrixValues;
	}

	private TurtleMatrix loadMatrix(String s) {
		String baseName = matrixDir.getPath() + "/" + s;
		TurtleMatrix v = new TurtleMatrix();
		v.x = load(baseName + "_x");
		v.gradStore = load(baseName + "_gs");
		return v;
	}

	private SimpleMatrix load(String string) {
		try {
			return SimpleMatrix.loadBinary(string);
		} catch (IOException e) {
			int M = EncoderParams.getM();
			return MatrixUtil.randomMatrix(M, M, EncoderParams.getInitStd());
		}
	}

	private void saveMatrix(String s, TurtleMatrix matrixValue) {
		FileSystem.createPath(matrixDir);
		String baseName = matrixDir.getPath() + "/" + s;
		try {
			matrixValue.x.saveToFileBinary(baseName + "_x");
			matrixValue.gradStore.saveToFileBinary(baseName + "_gs");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private Map<String, List<TestTriplet>> makeTestMap() {
		Map<String, List<TestTriplet>> testMap = new HashMap<String, List<TestTriplet>>();
		for(TestTriplet t : trainSet) {
			String astId = t.getAstId();
			if(!testMap.containsKey(astId)) {
				testMap.put(astId, new ArrayList<TestTriplet>());
			}
			testMap.get(astId).add(t);
		}
		return testMap;
	}

	private void adaGradMatrix(Map<String, TurtleMatrix> matrixValues, String matrixKey, SimpleMatrix grad) {
		TurtleMatrix matrixValue = matrixValues.get(matrixKey);
		matrixValue.gradStore = matrixValue.gradStore.elementMult(matrixValue.gradStore);
		double eta = EncoderParams.getLearningRate();
		for(int i = 0; i < grad.getNumElements(); i++) {
			double v = matrixValue.gradStore.get(i);
			if(v == 0) continue;
			double weight = eta/Math.sqrt(v);
			double newX = matrixValue.x.get(i) - weight * v;
			matrixValue.x.set(i, newX);
		}
		saveMatrix(matrixKey, matrixValue);
	}

	private void adaGradModel(double eta, double[] model_x, double[] model_gs,
			double[] model_grad) {
		for(int i = 0; i < model_grad.length; i++) {
			model_gs[i] += Math.pow(model_grad[i], 2);
			if(model_gs[i] == 0) continue;
			double weight = eta/Math.sqrt(model_gs[i]);
			model_x[i] = (model_x[i] - weight*model_grad[i]);
		}
		saveModel(model_x);
	}

	private void saveModel(double[] model_x) {
		// TODO Auto-generated method stub

	}

	private void loadTrainSet() {
		File expDir = new File(FileSystem.getAssnDir(), "runExp");
		File trainFile = new File(expDir, "prePost.csv");
		Scanner codeIn = null;
		trainSet = new ArrayList<TestTriplet>();
		System.out.println("loading...");
		int done = 0;
		try {
			codeIn = new Scanner(trainFile);
			while (codeIn.hasNextLine()) {
				String line = codeIn.nextLine();
				trainSet.add(loadTestTriplet(line));
				if(++done % 100 == 0) System.out.println(done);
			}
			codeIn.close();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	private List<List<String>> shuffleMiniBatches(Collection<String> keys) {
		List<String> keyList = new ArrayList<String>(keys);
		Collections.shuffle(keyList);

		List<List<String>> miniBatches = new ArrayList<List<String>>();

		int miniBatchSize = EncoderParams.getMiniBatchSize();
		// will floor the value
		numMinibatches = keys.size() / miniBatchSize;

		// all mini-batches are the same size
		int start = 0;
		for(int k = 0; k < numMinibatches; k++) {
			int end = start + EncoderParams.getMiniBatchSize();
			miniBatches.add(keyList.subList(start, end));
			start = end;
		}

		return miniBatches;
	}

	private TestTriplet loadTestTriplet(String line) {
		String[] cols = line.split(",");
		String astId = cols[0];
		int stateSize = (cols.length - 2) / 2;

		int start1 = 2;
		int start2 = 2 + stateSize;
		List<String> preList = new ArrayList<String>();
		for(int j = start1; j < start2; j++) {
			preList.add(cols[j]);
		}
		List<String> postList = new ArrayList<String>();
		for(int j = start2; j < cols.length; j++) {
			postList.add(cols[j]);
		}

		State pre =  PrePostExperimentLoader.loadState(format.getLanguage(), preList);
		State post = PrePostExperimentLoader.loadState(format.getLanguage(), postList);

		return new TestTriplet(pre, post, null, astId, 1);
	}
	
	private void epochOutput(double[] x, double sumError, double sumAccuracy, int numTested) {
		TurtleModel model = (TurtleModel) ModelVector.vecToModel(format, x);
		
		String info = "";
		//info += "epoch: " + epoch + "\n";
		info += "value: " + sumError/numTested + "\n";
		info += "test acc: " + sumAccuracy/numTested + "\n";
		String notes = EncoderSaver.makeNotes();
		notes += info;
		
		EncoderSaver.save(x, format, name, "stateEncoderDecoder", notes);
		
		System.out.println("epoch " + formatOutput(sumError, sumAccuracy, numTested));
		log += sumError/numTested + "\t" + sumAccuracy/numTested + "\n";
		saveLog();

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

	public void setup() {
		log = "log start " + new SimpleDateFormat("dd-MM-yyyy").format(new Date()) + "\n";
		log += "num params: " + format.getDimension().getDimension() + "\n";
		log += EncoderSaver.makeNotes() + "\n";
		log += "-------------\n";
		addLogHeader();
	}

	public void addLogHeader() {
		log += "epoch, value, testAccuracy, algoTime(s)\n";
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

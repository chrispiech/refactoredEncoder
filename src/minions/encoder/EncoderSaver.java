package minions.encoder;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream.GetField;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import minions.encoder.modelVector.ModelVector;
import models.code.TestTriplet;
import models.encoder.EncoderParams;
import models.encoder.ModelFormat;
import models.encoder.encoders.Encoder;

import org.ejml.simple.SimpleMatrix;

import util.FileSystem;
import util.Warnings;


public class EncoderSaver {
	
	public static File save(double[] modelVec, ModelFormat format, String modelDirName, String modelName, String notes) {

		File expDir = FileSystem.getExpDir();
		File savedModelsDir = new File(expDir, "savedModels");
		File modelDir = new File(savedModelsDir, modelDirName);
		File modelEpochDir = new File(modelDir, modelName);

		FileSystem.createPath(modelEpochDir);

		// save the model
		saveModel(modelVec, modelEpochDir);

		// save the format
		saveFormat(format, modelEpochDir);

		// save any notes
		FileSystem.createFile(modelEpochDir, "notes.txt", notes);
		//System.out.println("model saved: " + modelDir.getPath());
		return modelEpochDir;
	}

	public static File save(double[] modelVec, ModelFormat format, String fileNamePrefix, String notes) {
		DateFormat dateFormat = new SimpleDateFormat("MM-dd-HH-mm-ss");
		Calendar cal = Calendar.getInstance();
		String timeStamp = dateFormat.format(cal.getTime());

		String dirName = fileNamePrefix;// + " " + timeStamp;
		File expDir = FileSystem.getExpDir();
		File modelsDir = new File(expDir, "savedModels");
		File modelDir = new File(modelsDir, dirName);

		FileSystem.createPath(modelDir);

		// save the model
		saveModel(modelVec, modelDir);

		// save the format
		saveFormat(format, modelDir);

		// save any notes
		FileSystem.createFile(modelDir, "notes.txt", notes);
		System.out.println("model saved: " + modelDir.getPath());
		return modelDir;
	}

	public static File save(Encoder model, String fileNamePrefix, String notes) {
		double[] modelVec = ModelVector.modelToVec(model);
		ModelFormat format = model.getFormat();
		return save(modelVec, format, fileNamePrefix, notes);
	}
	
	public static Encoder load(File modelDir) {
		Warnings.check(modelDir.exists(), "no model dir: " + modelDir.getAbsolutePath());

		ModelFormat format = loadFormat(modelDir);

		SimpleMatrix modelMatrix = loadSimpleMatrix(modelDir, "model.dat");
		double[] modelVec = new double[modelMatrix.getNumElements()];
		for(int i = 0; i < modelVec.length; i++) {
			modelVec[i] = modelMatrix.get(i);
		}

		return ModelVector.vecToModel(format, modelVec);
	}

	public static Encoder load(String name) {
		File expDir = FileSystem.getExpDir();
		File modelsDir = new File(expDir, "savedModels");
		File modelDir = new File(modelsDir, name);
		return load(modelDir);
	}

	private static void saveModel(double[] modelVec, File modelDir) {
		SimpleMatrix modelMatrix = new SimpleMatrix(modelVec.length, 1);
		for(int i = 0; i < modelVec.length; i++) {
			modelMatrix.set(i, modelVec[i]);
		}
		saveSimpleMatrix(modelDir, "model.dat", modelMatrix);
	}

	private static void saveFormat(ModelFormat format, File modelDir) {
		String formatStr = "";
		formatStr += "language:" +format.getLanguageName() + "\n";
		formatStr += "modelType:" + format.getModelType() + "\n";
		formatStr += "codeVectorSize:" + EncoderParams.getCodeVectorSize() +"\n";
		if(EncoderParams.hasStateVectorSize()) {
			formatStr += "stateVectorSize:" + EncoderParams.getStateVectorSize();
		}
		FileSystem.createFile(modelDir, "format.txt", formatStr);
	}

	public static ModelFormat loadFormat(File modelDir) {
		File modelFormatFile = new File(modelDir, "format.txt");
		List<String> lines = FileSystem.getFileLines(modelFormatFile);
		Map<String, String> formatMap = getFormatMap(lines);
		String language = formatMap.get("language");
		String modelType = formatMap.get("modelType");
		if(formatMap.containsKey("codeVectorSize")) {
			String v = formatMap.get("codeVectorSize");
			EncoderParams.setCodeVectorSize(Integer.parseInt(v));
		}
		if(formatMap.containsKey("stateVectorSize")) {
			String v = formatMap.get("stateVectorSize");
			EncoderParams.setStateVectorSize(Integer.parseInt(v));
		}
		return new ModelFormat(language, modelType);
	}

	private static Map<String, String> getFormatMap(List<String> lines) {
		Map<String, String> formatMap = new HashMap<String, String>();
		for(String line : lines) {
			String key = line.split(":")[0];
			String value = line.split(":")[1];
			formatMap.put(key, value);
		}
		return formatMap;
	}

	public static SimpleMatrix loadSimpleMatrix(File dir, String fileName) {
		File matrixFile = new File(dir, fileName);
		try {
			return SimpleMatrix.loadBinary(matrixFile.getPath());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static void saveSimpleMatrix(File modelDir, String name,
			SimpleMatrix matrix) {
		File matrixFile = new File(modelDir, name);
		try {
			matrix.saveToFileBinary(matrixFile.getPath());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	public static String makeNotes(List<TestTriplet> trainSet, int epochs) {
		String notes = "";
		notes += "epochs: " + epochs + "\n";
		notes += "learning rate: " + EncoderParams.getLearningRate() + "\n";
		notes += "weight decay: " + EncoderParams.getWeightDecay() + "\n";
		return notes;

	}
	
	public static String makeNotes() {
		String notes = "";
		notes += "learning rate: " + EncoderParams.getLearningRate() + "\n";
		notes += "weight decay: " + EncoderParams.getWeightDecay() + "\n";
		notes += "codeVec size: " + EncoderParams.getCodeVectorSize() + "\n";
		notes += "stateVec size: " + EncoderParams.getStateVectorSize() + "\n";
		notes += "miniBatch size: " + EncoderParams.getMiniBatchSize() + "\n";
		notes += "numThreads: " + EncoderParams.getNumThreads() + "\n";
		return notes;

	}

	

}

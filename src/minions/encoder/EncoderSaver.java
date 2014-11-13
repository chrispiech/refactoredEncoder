package minions.encoder;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import minions.encoder.modelVector.ModelVector;
import models.code.TestTriplet;
import models.encoder.EncoderParams;
import models.encoder.ModelFormat;
import models.encoder.encoders.Encoder;

import org.ejml.simple.SimpleMatrix;

import util.FileSystem;


public class EncoderSaver {

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

	public static Encoder load(String name) {
		File expDir = FileSystem.getExpDir();
		File modelsDir = new File(expDir, "savedModels");
		File modelDir = new File(modelsDir, name);

		ModelFormat format = loadFormat(modelDir);

		SimpleMatrix modelMatrix = loadSimpleMatrix(modelDir, "model.dat");
		double[] modelVec = new double[modelMatrix.getNumElements()];
		for(int i = 0; i < modelVec.length; i++) {
			modelVec[i] = modelMatrix.get(i);
		}

		return ModelVector.vecToModel(format, modelVec);
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
		formatStr += "langugage:" +format.getLanguageName() + "\n";
		formatStr += "modelType:" + format.getModelType() + "\n";
		formatStr += "codeVectorSize:" + EncoderParams.getCodeVectorSize() +"\n";
		formatStr += "stateVectorSize:" + EncoderParams.getStateVectorSize();
		FileSystem.createFile(modelDir, "format.txt", formatStr);
	}

	private static ModelFormat loadFormat(File modelDir) {
		File modelFormatFile = new File(modelDir, "format.txt");
		List<String> lines = FileSystem.getFileLines(modelFormatFile);
		String language = lines.get(0).split(":")[1];
		String modelType = lines.get(1).split(":")[1];
		int codeVectorSize = Integer.parseInt(lines.get(2).split(":")[1]);
		int stateVectorSize = Integer.parseInt(lines.get(3).split(":")[1]);
		EncoderParams.setCodeVectorSize(codeVectorSize);
		EncoderParams.setStateVectorSize(stateVectorSize);
		return new ModelFormat(language, modelType);
	}

	private static SimpleMatrix loadSimpleMatrix(File dir, String fileName) {
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
		notes += "train set size: " + trainSet.size() + "\n";
		notes += "epochs: " + epochs + "\n";
		notes += "learning rate: " + EncoderParams.getLearningRate() + "\n";
		notes += "weight decay: " + EncoderParams.getWeightDecay() + "\n";

		if(EncoderParams.getDepthLimit() > 0) {
			notes += "depth limited: " + EncoderParams.getDepthLimit() + "\n";
		} else {
			notes += "depth limited: no\n";
		}
		return notes;

	}

}

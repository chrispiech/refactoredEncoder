package run.toys;

import java.io.File;

import minions.encoder.EncoderSaver;
import models.encoder.encoders.models.LemurModel;

import org.ejml.simple.SimpleMatrix;

import util.FileSystem;

public class ExtractMatrix {
	public static void main(String[] args) {
		new ExtractMatrix().run();
	}

	private void run() {
		FileSystem.setAssnId("BeeperLine");
		File prePostDir = new File(FileSystem.getAssnDir(), "prePostExp");
		File savedModels = new File(prePostDir, "savedModels");
		File lemurModels = new File(savedModels, "lemur");

		// find its last model
		File bestModel = new File(lemurModels, "lemur-epoch110243");
		LemurModel model = (LemurModel) EncoderSaver.load(bestModel);
		
		//SimpleMatrix m = model.getProgramEncoder().getLeafEmbedding("snap");
		
		SimpleMatrix m = model.getStateDecoder().getOutputDecoder("beepers").getW();
		for(int r = 0; r < m.numRows(); r++) {
			for(int c = 0; c < m.numCols(); c++) {
				System.out.print(m.get(r, c));
				if(c != m.numCols() - 1) {
					System.out.print("\t");
				}
			}
			if(r != m.numRows()) {
				System.out.println("");
			}
		}
	}
}

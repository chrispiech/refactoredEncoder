package run.toys;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import minions.encoder.EncoderSaver;
import minions.program.EncodeGraphsLoader;
import minions.program.PrePostExperimentLoader;
import models.code.State;
import models.code.TestTriplet;
import models.encoder.EncodeGraph;
import models.encoder.encoders.Encoder;
import util.FileSystem;

public class PredictSquares {
	public static void main(String[] args) {
		new PredictSquares().run();
	}

	private void run() {
		FileSystem.setAssnId("SquareBeepers");
		FileSystem.setExpId("prePostExp");
		File savedModels = new File(FileSystem.getExpDir(), "savedModels");
		File modelDir = new File(savedModels, "lemurTmp/model");
		Encoder model = EncoderSaver.load(modelDir);
		System.out.println("test");
		
		File encodings = new File(FileSystem.getExpDir(), "train/encode.zip");
		ZipFile zip = null;
		try {
			zip = new ZipFile(encodings);
		} catch (ZipException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Map<String, EncodeGraph> gs = EncodeGraphsLoader.loadGraphs(model.getFormat().getLanguage(), zip);
		EncodeGraph g = gs.get("0");
		
		for(int i= 1; i < 100; i++) {
			State pre = makePre(i);
			
			TestTriplet t = new TestTriplet(pre, null, g, "0", "0", 1);
			State postHat = model.getOutput(t);
			double v = postHat.getMatrix("beepers").get(0);
			System.out.println(i + "\t" + v);
		}
	}

	private State makePre(int i) {
		String line = "0,0,0,1,1,1," + i;
		List<String> list = new ArrayList<String>();
		for(String s : line.split(",")) {
			list.add(s);
		}
		return PrePostExperimentLoader.loadKarelState(list);
	}
}

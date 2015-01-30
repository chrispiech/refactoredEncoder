package run.forceMultiply.midpoint;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import minions.encoder.EncoderSaver;
import minions.forceMult.FMEncoderRandom;
import minions.forceMult.FMMinion;
import minions.forceMult.ForceMultiplier;
import models.encoder.encoders.Encoder;
import models.encoder.neurons.TreeNeuron;

import org.ejml.simple.SimpleMatrix;
import org.json.JSONArray;
import org.json.JSONObject;

import run.forceMultiply.ForceMultUtil;
import util.FileSystem;

public class ProgramRandom {

	private static final int NUM_PROGRAMS = -1;
	private static final int BUDGET = 500;


	private void run() {

		FileSystem.setAssnId("Midpoint");

		// but the feedback lives in the feedbackExp :)
		System.out.println("loading feedback...");
		FileSystem.setExpId("feedbackExp");
		Map<String, List<Integer>> feedbackMap = ForceMultUtil.loadFeedbackZip();

		// the programs came from the start experiment!
		System.out.println("loading model...");
		FileSystem.setExpId("prePostExp3");
		Encoder model = EncoderSaver.load("ringtailed2-beta/model");

		Map<String, TreeNeuron> programMap = ForceMultUtil.loadProgramsZip();
		TreeMap<String, SimpleMatrix> encodingMap = ForceMultUtil.makeEncodingMap(programMap, model);


		Set<String> toGrade = encodingMap.keySet();
		System.out.println("num programs: " + toGrade.size());

		System.out.println("running active learning!");
		FMMinion minion = new FMEncoderRandom(encodingMap, 0);
		ForceMultiplier force = new ForceMultiplier(minion, feedbackMap, toGrade);
		force.run(BUDGET);
	}

	public static void main(String[] args) {
		new ProgramRandom().run();
	}
}

package run.sandbox;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.json.JSONObject;

import minions.program.EncodeTreeLoader;
import minions.program.PrePostExperimentLoader;
import models.code.TestTriplet;
import models.encoder.EncodeGraph;
import models.encoder.neurons.TreeNeuron;
import models.language.KarelLanguage;
import models.language.Language;
import util.FileSystem;
import util.Warnings;

public class Unzipper {

	public static void main(String[] args) throws IOException {
		FileSystem.setAssnId("Midpoint");
		FileSystem.setExpId("prePostExp");

		Language lang = new KarelLanguage();
		
		//testExp(lang);
		trainExp(lang);
	}

	private static void trainExp(Language lang) {
		PrePostExperimentLoader.loadTrainPrograms(lang);
		load1000(lang);
		load1000(lang);
		load1000(lang);
	}

	private static void load1000(Language lang) {
		long startTrain = System.currentTimeMillis();
		PrePostExperimentLoader.loadTrainSetSample(1000, lang);
		long endTrain = System.currentTimeMillis();
	
	    
		long trainTime = endTrain - startTrain;
		System.out.println("train time (per mini batch): " + trainTime + "ms");
	}

	private static void testExp(Language lang) {
		long startTest = System.currentTimeMillis();
		List<TestTriplet>testSet = PrePostExperimentLoader.loadTestSet(5000, lang);
		long endTest = System.currentTimeMillis();
		System.out.println("test: " + testSet.size());
		long testTime = endTest - startTest;
		System.out.println("test time (one time): " + testTime + "s");
	}
	
	public static Map<String, TreeNeuron> loadGraphs(Language lang,
			ZipFile zip) {
		Enumeration<? extends ZipEntry> entries = zip.entries();
		Map<String, TreeNeuron> m = new HashMap<String, TreeNeuron>();
		int done = 0;
		while(entries.hasMoreElements()){
			//System.out.println(++done);
			ZipEntry entry = entries.nextElement();
			
			EncodeGraph g = loadGraph(lang, zip, entry);
			File temp = new File(entry.getName());
			
			String name = FileSystem.getNameWithoutExtension(temp.getName());
			
			
			
			if(g == null || g.hasCycles()) {
				continue;
			}

			//TreeNeuron runTree = g.getRunEncodeTreeClone();
			m.put(name, g.getRunEncodeTreeClone());
			
			if(++done % 100 == 0) System.out.println(done);
		}
		return m;
	}

	public static EncodeGraph loadGraph(Language lang, ZipFile zip,
			ZipEntry entry) {
		String str = getFileString(zip, entry);

		JSONObject fileJson = new JSONObject(str);

		EncodeGraph g = getEncodeGraph(fileJson, lang);
		return g;
	}
	
	private static String getFileString(ZipFile zip, ZipEntry entry){
		InputStream stream = null;
		try {
			stream = zip.getInputStream(entry);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		Scanner sc = new Scanner(stream).useDelimiter("\\A");
		String str = sc.next();
		return str;
	}
	
	private static EncodeGraph getEncodeGraph(JSONObject fileJson, Language lang) {
		Map<String, TreeNeuron> methodMap = new HashMap<String, TreeNeuron>();
		JSONObject encodeTreeJson = fileJson;
		if(encodeTreeJson.toString().equals("{}")) return null;
		for(Object key : encodeTreeJson.keySet()) {
			String name = (String)key;
			JSONObject methodJson = encodeTreeJson.getJSONObject(name);
			TreeNeuron body = EncodeTreeLoader.parseJson(methodJson);
			methodMap.put(name, body);
		}
		return new EncodeGraph(methodMap);
	}

}

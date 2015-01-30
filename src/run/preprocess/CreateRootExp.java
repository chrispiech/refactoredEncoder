package run.preprocess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.zip.ZipFile;

import util.FileSystem;
import util.Warnings;
import minions.program.EncodeGraphsLoader;
import minions.program.EncodeTreeLoader;
import minions.program.PrePostExperimentLoader;
import models.code.State;
import models.code.TestTriplet;
import models.encoder.EncodeGraph;
import models.encoder.ModelFormat;
import models.encoder.neurons.TreeNeuron;
import models.language.Language;

public class CreateRootExp {

	FileWriter rootExpFile = null;
	
	public static void main(String[] args) {
		new CreateRootExp().run();
	}

	private void run() {
		FileSystem.setAssnId("Midpoint");
		createOutputFile();
		System.out.println("create a rootExp from a prePostExp");

		ModelFormat format = new ModelFormat("karel", "lemur");

		loadPrePost(format.getLanguage());
	}

	private void createOutputFile() {
		File rootDir = new File(FileSystem.getAssnDir(), "rootExp");
		File file = new File(rootDir, "prePost.csv");
		try {
			rootExpFile = new FileWriter(file);
		} catch (IOException e) {
			Warnings.error("problem with rootFile");
		} 
		
	}

	private void loadPrePost(Language l) {
		File assnDir = new File(FileSystem.getAssnDir(), "prePostExp");
		File testDir = new File(assnDir, "test");
		File trainDir = new File(assnDir, "train");

		File testPrePost = new File(testDir, "prePost.csv");
		Map<String, String> testRunIds = loadTestRunIds();
		List<TestTriplet> tests = getRootPrePost(testPrePost, testRunIds);
	}

	public static Map<String, String> loadTestRunIds() {
		File expDir = new File(FileSystem.getAssnDir(), "prePostExp");
		File trainDir = new File(expDir, "test");
		ZipFile zip = null;
		try {
			zip = new ZipFile(new File(trainDir, "encode.zip"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return EncodeTreeLoader.getRunIdMap(zip);
	}

	private List<TestTriplet> getRootPrePost(File file, Map<String, String> runIds) {
		Scanner scanner = null;
		String content = "";
		int done = 0;
		try {
			scanner = new Scanner(file);

			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				if(isRoot(runIds, line)) {
					writeLine(line);
				} 
				if(++done % 1000 == 0) System.out.println(done);
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private void writeLine(String line) {
		try {
			rootExpFile.write(line + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean isRoot(Map<String, String> runIds, String line) {
		String[] cols = line.split(",");
		String astId = cols[0];
		String nodeId = cols[1];
		// TODO Auto-generated method stub
		String runId = runIds.get(astId);
		if(runId == null) {
			return false;
		}
		return nodeId.equals(runId);
	}

	private TestTriplet lineToTest(String line) {
		/*String[] cols = line.split(",");
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

		State pre =  loadState(lang, preList);
		State post = loadState(lang, postList);

		Map<String, TreeNeuron> methodMap = new HashMap<String, TreeNeuron>();
		methodMap.put("run", effectiveTree);
		EncodeGraph graph = new EncodeGraph(methodMap);
		return new TestTriplet(pre, post, graph, astId, 1);*/
		return null;
	}

}

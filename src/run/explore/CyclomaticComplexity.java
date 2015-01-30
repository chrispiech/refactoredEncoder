package run.explore;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipFile;

import minions.program.EncodeGraphsLoader;
import models.encoder.EncodeGraph;
import models.encoder.neurons.TreeNeuron;
import models.language.KarelLanguage;
import models.language.KarelLanguage2;
import models.language.Language;
import util.FileSystem;
import util.Histogram;
import util.MapSorter;

public class CyclomaticComplexity {
	public static void main(String[] args){
		new CyclomaticComplexity().run();
	}
	
	private Map<String, EncodeGraph> asts = new HashMap<String, EncodeGraph>();
	private Language lang = new KarelLanguage2();
	
	private void run() {
		FileSystem.setAssnId("Midpoint");
		collectEncodeTrees();
		int maxCc = 0;
		Histogram h = new Histogram(0, 160, 1);
		Map<String, Integer> ccMap = new HashMap<String, Integer>();
		for(String key : asts.keySet()) {
			EncodeGraph graph = asts.get(key);
			TreeNeuron n = graph.getRunEncodeTreeClone();
			int cc = getCyclomaticComplexity(n);
			if(cc > maxCc) {
				maxCc = cc;
			}
			ccMap.put(key, cc);
			h.addPoint(cc);
		}
		System.out.println(maxCc);
		System.out.println("-----");
		System.out.println(h);
		MapSorter<String> s = new MapSorter<String>();
		List<String> ordered = s.sortInt(ccMap);
		System.out.println("------");
		for(double i = 0.0; i < 1; i += 0.1) {
			int pStart = (int) (i * ordered.size());
			int pEnd = (int) ((i + 0.1) * ordered.size());
			pEnd = Math.min(ordered.size(), pEnd);
			List<String> subset = ordered.subList(pStart, pEnd);
			getAverageCc(ccMap, subset);
		}
	}
	
	private void getAverageCc(Map<String, Integer> ccMap, List<String> subset) {
		double sum = 0;
		for(String s : subset) {
			sum += ccMap.get(s);
		}
		System.out.println(sum / subset.size());
	}

	private int getCyclomaticComplexity(TreeNeuron n) {
		return 1 + getCyclomaticComplexityHelper(n);
	}
	
	private int getCyclomaticComplexityHelper(TreeNeuron n) {
		int cc = isBranch(n) ? 1 : 0;
		for(TreeNeuron child : n.getChildren()) {
			cc += getCyclomaticComplexityHelper(child);
		}
		return cc;
	}

	private boolean isBranch(TreeNeuron n) {
		return lang.isBlockType(n.getType());
	}

	private void collectEncodeTrees() {
		File expDir = new File(FileSystem.getAssnDir(), "prePostExp");
		File trainDir = new File(expDir, "train");
		File testDir = new File(expDir, "test");
		collectEncodeTreesFromDir(trainDir);
		collectEncodeTreesFromDir(testDir);
		System.out.println(asts.size());
	}

	private void collectEncodeTreesFromDir(File dir) {

		ZipFile zip = null;
		try {
			zip = new ZipFile(new File(dir, "encode.zip"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		Map<String, EncodeGraph> map =  EncodeGraphsLoader.loadGraphs(new KarelLanguage(), zip);
		for(String key : map.keySet()) {
			if(map.get(key) != null) {
				asts.put(key, map.get(key));
			}
		}
	}
}

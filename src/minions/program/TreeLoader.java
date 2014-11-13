package minions.program;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import models.ast.Tree;

import org.json.JSONArray;
import org.json.JSONObject;

import util.FileSystem;
import util.IdCounter;

public class TreeLoader {

	private IdCounter idCounter = new IdCounter();
	
	public static TreeMap<Integer, Tree> loadTrees(File dir) {
		return loadTrees(dir, -1);
	}

	public static TreeMap<Integer, Tree> loadTrees(File dir, int max) {
		List<File> treeFiles = FileSystem.listNumericalFiles(dir);

		TreeMap<Integer, Tree> trees = new TreeMap<Integer, Tree>();
		for(File f : treeFiles) {

			String testJsonStr = FileSystem.getFileContents(f);
			JSONObject json = new JSONObject(testJsonStr);
			Tree tree = loadJsonTree(json);
			String id = FileSystem.getNameWithoutExtension(f.getName());
			int intId = Integer.parseInt(id);
			trees.put(intId, tree);

			int numLoaded = trees.size();
			if(numLoaded % 100 == 0) {
				System.out.println("loaded: " + numLoaded);
			}
			if(max > 0 && numLoaded >= max) {
				break;
			}
		}

		return trees;
	}

	public static Tree loadJsonTree(JSONObject json) {
		TreeLoader l = new TreeLoader();
		return l.loadJsonTreeHelper(json);
	}

	private Tree loadJsonTreeHelper(JSONObject json) {
		String type = json.getString("type");
		String name = "";
		if(json.has("name")) {
			name = json.getString("name");
		}

		List<Tree> children = new ArrayList<Tree>();
		if(json.has("children")) {
			JSONArray childrenJson = json.getJSONArray("children");
			for(int i = 0; i < childrenJson.length(); i++) {
				JSONObject childJson = childrenJson.getJSONObject(i);
				Tree child = loadJsonTreeHelper(childJson);
				children.add(child);
			}
		}

		String nodeId = idCounter.getNextIdStr();
		return new Tree(type, name, children, nodeId);
	}
}

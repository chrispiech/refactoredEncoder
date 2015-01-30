package minions.program;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import models.ast.Tree;
import models.encoder.EncodeGraph;
import models.encoder.neurons.TreeNeuron;
import models.language.Language;

import org.json.JSONArray;
import org.json.JSONObject;

import util.FileSystem;
import util.Warnings;

public class EncodeTreeLoader {

	public static Map<String, String> getRunIdMap(ZipFile zip) {
		Enumeration<? extends ZipEntry> entries = zip.entries();
		Map<String, String> m = new HashMap<String, String>();
		int done = 0;
		while(entries.hasMoreElements()){
			//System.out.println(++done);
			ZipEntry entry = entries.nextElement();
			if(!FileSystem.getExtension(entry.getName()).equals("json")) {
				continue;
			}
			
			String runId = getRunId(zip, entry);
			if(runId == null) continue;
			
			File temp = new File(entry.getName());
			
			String name = FileSystem.getNameWithoutExtension(temp.getName());
			

			m.put(name, runId);
			if(++done % 100 == 0) System.out.println(done);
		}
		return m;
	}
	
	public static String getRunId(ZipFile zip,ZipEntry entry) {
		String str = FileSystem.getZipEntryString(zip, entry);


		JSONObject fileJson = new JSONObject(str);
		if(!fileJson.has("run")) {
			return null;
		}
		JSONObject runJson = fileJson.getJSONObject("run");
		return runJson.getString("nodeId");
	}
	
	public static TreeNeuron parseJson(JSONObject json) {
		if(!json.has("type")) {
			return new TreeNeuron("noop", "0");
		}
		
		String type = json.getString("type");
		String nodeId = null;
		if(json.has("id")) {
			nodeId = json.getString("id");
		} else if(json.has("nodeId")) {
			nodeId = json.getString("nodeId");
		}
		Warnings.check(nodeId != null);

		List<TreeNeuron> children = new ArrayList<TreeNeuron>();
		if(json.has("children")) {
			JSONArray childrenJson = json.getJSONArray("children");
			for(int i = 0; i < childrenJson.length(); i++) {
				JSONObject childJson = childrenJson.getJSONObject(i);
				TreeNeuron child = parseJson(childJson);
				children.add(child);
			}
		}

		return new TreeNeuron(type, children, nodeId);
	}

}

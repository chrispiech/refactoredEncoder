package minions.program;

import java.util.ArrayList;
import java.util.List;

import models.ast.Tree;
import models.encoder.neurons.TreeNeuron;

import org.json.JSONArray;
import org.json.JSONObject;

public class EncodeTreeLoader {

	
	public static TreeNeuron parseJson(JSONObject json) {
		if(!json.has("type")) {
			return new TreeNeuron("noop", "0");
		}
		
		String type = json.getString("type");
		String nodeId = json.getString("id");

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

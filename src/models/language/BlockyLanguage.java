package models.language;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BlockyLanguage implements Language{

	private List<String> outputKeys = new ArrayList<String>();
	{
		outputKeys.add("row");
		outputKeys.add("col");
		outputKeys.add("status");
		outputKeys.add("direction");
	}
	
	private Map<String, String[]> outputOptions = 
			new HashMap<String, String[]>();
	{
		outputOptions.put("direction", getDirOutputs());
		outputOptions.put("status", getStateOutputs());
		outputOptions.put("row", getRowOutputs());
		outputOptions.put("col", getColOutputs());
	}
	
	
	@Override
	public List<String> getLeafTypes() {
		List<String> types = new ArrayList<String>();
		types.add("maze_turnLeft");
		types.add("maze_turnRight");
		types.add("maze_moveForward");
		types.add("isPathForward");
		types.add("isPathLeft");
		types.add("isPathRight");
		return types;
	}

	@Override
	public int getArity(String type) {
		if(type.equals("ifElse")) return 3;
		if(type.equals("block")) return 2;
		if(type.equals("while")) return 1;
		throw new RuntimeException("unknown: " + type);
	}

	@Override
	public String getEncoderType(String nodeType) {
		if(nodeType.equals("block")) return "block";
		if(nodeType.equals("maze_ifElse")) return "ifElse";
		if(nodeType.equals("maze_forever")) return "while";
		throw new RuntimeException("eh? " + nodeType);
	}

	@Override
	public List<String> getInternalEncoderTypes() {
		List<String> types = new ArrayList<String>();
		types.add("block");
		types.add("while");
		types.add("ifElse");
		return types;
	}

	@Override
	public int getNumOutputs() {
		return outputKeys.size();
	}

	@Override
	public List<String> getStateKeys() {
		return outputKeys;
	}

	@Override
	public int getNumOutputOptions(String key) {
		return outputOptions.get(key).length;
	}

	@Override
	public String getOutputType(String key) {
		return "choice";
	}
	
	@Override
	public String[] getChoiceOptions(String key) {
		return outputOptions.get(key);
	}
	
	private static String[] getStateOutputs() {
		String[] outputs = {"0", "1", "2", "-1"};
		return outputs;
	}
	
	private static String[] getDirOutputs() {
		String[] outputs = {"0", "1", "2", "3"};
		return outputs;
	}

	private static String[] getColOutputs() {
		String[] outputs = {"0", "1", "2", "3", "4", "5", "6"};
		return outputs;
	}

	private static String[] getRowOutputs() {
		String[] outputs = {"2", "3", "4", "5", "6"};
		return outputs;
	}

	@Override
	public boolean isBlockType(String type) {
		return false;
	}

	@Override
	public String getName() {
		return "blocky";
	}

	@Override
	public boolean isMethodInvocation(String type) {
		return false;
	}

	@Override
	public boolean isKarel() {
		return false;
	}

	@Override
	public boolean isBlocky() {
		return true;
	}

}

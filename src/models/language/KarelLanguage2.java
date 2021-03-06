package models.language;

import java.util.*;

import models.encoder.EncoderParams;
import util.Warnings;

public class KarelLanguage2 implements Language{

	protected static final String[] KAREL_LEAVES = {
		"move",
		"turnLeft",
		"turnRight",
		"putBeeper",
		"pickBeeper",
		"turnAround",
		"frontIsClear",
		"frontIsBlocked",
		"leftIsClear",
		"leftIsBlocked",
		"rightIsClear",
		"rightIsBlocked",
		"beepersPresent",
		"noBeepersPresent",
		"beepersInBag",
		"noBeepersInBag",
		"facingNorth",
		"notFacingNorth",
		"facingEast",
		"notFacingEast",
		"facingSouth",
		"notFacingSouth",
		"facingWest",
		"notFacingWest",
		"true",
		"false",
		"noop",
	};
	
	protected static final String[] KAREL_INTERNAL = {
		"block",
		"!",
		"&&",
		"||",
		"ifElse",
		"while",
		"repeat"
	};
	
	protected static final String[] KAREL_BLOCK_TYPES = {
		"while",
		"repeat",
		"ifElse"
	};
	
	protected Map<String, Integer> arityMap = new HashMap<String, Integer>();
	{
		arityMap.put("block", 2);
		arityMap.put("!", 1);
		arityMap.put("&&", 2);
		arityMap.put("||", 2);
		arityMap.put("while", 2);
		arityMap.put("repeat", 2);
		arityMap.put("ifElse", 3);
		arityMap.put("method", 1);
	}
	
	protected List<String> stateKeys = new ArrayList<String>();
	{
		stateKeys.add("row");
		stateKeys.add("col");
		stateKeys.add("status");
		if(EncoderParams.stateHasSize) {
			stateKeys.add("worldRows");
			stateKeys.add("worldCols");
		}
		stateKeys.add("direction");
		stateKeys.add("beepers");
	}
	
	protected Map<String, String[]> outputOptions = 
			new HashMap<String, String[]>();
	{
		String[] dirOptions = {"0", "1", "2", "3"};
		outputOptions.put("direction", dirOptions);
		String[] stateOptions = {"0", "1", "2", "3", "4"};
		outputOptions.put("status", stateOptions);
		String[] colOptions = {"0", "1", "2", "3", "4", "5", "6"};
		outputOptions.put("col", colOptions);
		String[] rowOptions = {"0", "1", "2", "3", "4", "5", "6"};
		outputOptions.put("row", rowOptions);
	}
	
	protected Map<String, String> outputType = 
			new HashMap<String, String>();
	{
		outputType.put("row", "choice");
		outputType.put("col", "choice");
		if(EncoderParams.stateHasSize) {
			outputType.put("worldRows", "number");
			outputType.put("worldCols", "number");
		}
		outputType.put("status", "choice");
		outputType.put("direction", "choice");
		outputType.put("beepers", "matrix");
	}
	
	@Override
	public List<String> getLeafTypes() {
		return new ArrayList<String>(Arrays.asList(KAREL_LEAVES));
	}

	@Override
	public int getArity(String type) {
		return arityMap.get(type);
	}

	@Override
	public String getEncoderType(String nodeType) {
		return nodeType;
	}

	@Override
	public List<String> getInternalEncoderTypes() {
		return new ArrayList<String>(Arrays.asList(KAREL_INTERNAL));
	}
	
	public int getNumOutputs() {
		return stateKeys.size();
	}

	@Override
	public List<String> getStateKeys() {
		return stateKeys;
	}

	@Override
	public int getNumOutputOptions(String key) {
		Warnings.check(getOutputType(key).equals("choice"));
		return outputOptions.get(key).length;
	}

	@Override
	public String getOutputType(String key) {
		return outputType.get(key);
	}

	@Override
	public String[] getChoiceOptions(String key) {
		Warnings.check(getOutputType(key).equals("choice"));
		return outputOptions.get(key);
	}

	@Override
	public boolean isBlockType(String type) {
		return isBlock(type);
	}

	@Override
	public String getName() {
		return "karel";
	}
	
	public static boolean isBlock(String type) {
		for(String s : KAREL_BLOCK_TYPES) {
			if(type.equals(s)) return true;
		}
		return false;
	}

	@Override
	public boolean isMethodInvocation(String type) {
		return type.startsWith("method");
	}

	@Override
	public boolean isKarel() {
		return true;
	}

	@Override
	public boolean isBlocky() {
		return false;
	}


}

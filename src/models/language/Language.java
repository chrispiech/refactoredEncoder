package models.language;

import java.util.List;
import java.util.Set;

import models.code.State;

import org.apache.commons.math3.util.Pair;

public interface Language {

	List<String> getLeafTypes();

	int getArity(String type);

	String getEncoderType(String nodeType);

	List<String> getInternalEncoderTypes();

	int getNumOutputs();

	List<String> getStateKeys();

	int getNumOutputOptions(String key);
	
	String getOutputType(String key);
	
	String[] getChoiceOptions(String key);

	boolean isBlockType(String type);
	
	boolean isMethodInvocation(String type);

	String getName();

	boolean isKarel();

	boolean isBlocky();

}

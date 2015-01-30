package models.encoder.dimension;

import org.apache.commons.math3.util.Pair;

import util.Warnings;
import models.encoder.EncoderParams;
import models.language.Language;

public class Dimension {

	protected Language language;
	
	public Dimension() {
		
	}

	public Dimension(Language language) {
		this.language = language;
	}

	public int getDimension() { 
		throw new RuntimeException("abstract"); 
	} 

	public int getStateEncoderDimension() {
		throw new RuntimeException("abstract");
	}

	public int getStateDecoderDimension() { 
		int numParams = 0;
		for(String key : language.getStateKeys()) {
			numParams += getStateDecoderDimension(key);
		}
		return numParams;
	}

	public int getLeafDimension() { 
		int numLeaves = language.getLeafTypes().size();
		return numLeaves * EncoderParams.getCodeVectorSize();
	}

	public int getStateEncoderDimension(String key) {
		int rows = getTypeVectorSize(key);
		return rows * EncoderParams.getM();
	}

	public int getStateDecoderDimension(String key) {
		int rows = getTypeVectorSize(key);
		return rows * EncoderParams.getM() + rows;
	}

	public int getTypeVectorSize(String key) {
		String type = language.getOutputType(key);
		if(type.equals("choice")) {
			return language.getNumOutputOptions(key);
		} else if(type.equals("number")) {
			return 1;
		} else if(type.equals("matrix")) {
			Pair<Integer, Integer> dim = getMatrixDim(key);
			return dim.getFirst() * dim.getSecond();
		} 
		throw new RuntimeException("no");
	}

	public Pair<Integer, Integer> getMatrixDim(String key) {
		Warnings.check(key.equals("beepers"));
		return new Pair<Integer, Integer>(EncoderParams.worldRows,EncoderParams.worldCols);
	}

	

	public int getInternalDimension() {
		throw new RuntimeException("abstract");
	}

	public int getInternalEncoderDimension(String type) {
		throw new RuntimeException("abstract");
	}

}

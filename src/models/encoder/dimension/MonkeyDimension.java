package models.encoder.dimension;

import org.apache.commons.math3.util.Pair;

import util.Warnings;
import models.encoder.EncoderParams;
import models.language.Language;

public class MonkeyDimension extends Dimension {

	public MonkeyDimension(Language language) {
		super(language);
	}

	@Override
	public int getDimension() {
		int outDim = getStateDecoderDimension();
		int internalDim = getInternalDimension();
		int leafDim = getLeafDimension();
		int inDim = getStateEncoderDimension();
		return outDim + internalDim + leafDim + inDim;
	}

	@Override
	public int getStateVectorSize() {
		return EncoderParams.getSqrtN();
	}

	@Override
	public int getStateEncoderDimension() {
		int numParams = getStateVectorSize();
		for(String key : language.getStateKeys()) {
			numParams += getStateEncoderDimension(key);
		}
		return numParams;
	}
	
	public int getInternalDimension() { 
		int dim = 0;
		for(String type : language.getInternalEncoderTypes()) {
			dim += getInternalEncoderDimension(type);
		}
		return dim;
	}

	public int getInternalEncoderDimension(String type) {
		int arity = language.getArity(type);
		int n = EncoderParams.getCodeVectorSize();
		return arity * n * n + n;
	}

}

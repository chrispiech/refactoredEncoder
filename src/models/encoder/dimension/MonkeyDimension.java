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

}

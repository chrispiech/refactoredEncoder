package models.encoder.dimension;

import models.encoder.EncoderParams;
import models.language.Language;


public class LemurDimension extends Dimension {

	public LemurDimension(Language language) {
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
		int m = EncoderParams.getStateVectorSize();
		return (arity + 1) * m * m;
	}

}

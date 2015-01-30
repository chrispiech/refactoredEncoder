package models.encoder.dimension;

import models.encoder.EncoderParams;
import models.language.Language;

public class BearDimension extends Dimension {

	public BearDimension(Language language) {
		super(language);
	}

	@Override
	public int getDimension() {
		int outDim = getStateDecoderDimension();
		int internalDim = getInternalDimension();
		int leafDim = getLeafDimension();
		return outDim + internalDim + leafDim;
	}

	@Override
	public int getStateVectorSize() {
		return EncoderParams.getCodeVectorSize();
	}
	
	@Override
	public int getStateEncoderDimension() {
		return 0;
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

package models.encoder.dimension;

import models.encoder.EncoderParams;
import models.language.Language;


public class PenguinDimension extends Dimension {

	public PenguinDimension(Language language) {
		super(language);
	}
	
	public int getDimension() { 
		int inDim = getStateEncoderDimension();
		int outDim = getStateDecoderDimension();
		int internalDim = getInternalDimension();
		int leafDim = getLeafDimension();
		int compositionDim = getCompositionDim();
		return inDim + outDim + internalDim + leafDim + compositionDim;
	} 

	public int getCompositionDim() {
		int m = getStateVectorSize();
		int n = EncoderParams.getCodeVectorSize();
		return m*n + m*m + m;
	}

	public int getStateVectorSize() { 
		return EncoderParams.getStateVectorSize();
	}

	public int getStateEncoderDimension() {
		int numParams = getStateVectorSize();
		for(String key : language.getStateKeys()) {
			numParams += getStateEncoderDimension(key);
		}
		return numParams;
	}

}

package models.encoder.dimension;

import models.encoder.EncoderParams;
import models.language.Language;


public class TurtleDimension extends Dimension {

	public TurtleDimension(Language language) {
		super(language);
	}
	
	@Override
	public int getDimension() {
		int outDim = getStateDecoderDimension();
		int inDim = getStateEncoderDimension();
		return outDim + inDim;
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

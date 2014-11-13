package models.encoder.dimension;

import models.encoder.EncoderParams;
import models.language.Language;

public class BeeDimension extends Dimension {

	public BeeDimension(Language language) {
		super(language);
	}
	
	@Override
	public int getDimension() {
		return getStateDecoderDimension() + getStateEncoderDimension();
	}

	public int getStateVectorSize() { 
		return EncoderParams.getM(); 
	}

	public int getStateEncoderDimension() {
		int numParams = getStateVectorSize();
		for(String key : language.getStateKeys()) {
			numParams += getStateEncoderDimension(key);
		}
		return numParams;
	}

}

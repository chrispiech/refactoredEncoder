package models.encoder.dimension;

import models.encoder.EncoderParams;
import models.language.Language;


public class DeepBeeDimension extends Dimension {
	
	public DeepBeeDimension(Language language) {
		super(language);
	}
	
	@Override
	public int getDimension() {
		return getStateDecoderDimension() + getStateEncoderDimension();
	}
	
	public int getStateDecoderDimension() {
		int M = EncoderParams.getM();
		int rawStateSize = getRawStateSize();
		int numParams = 0;
		numParams += getLayerDimension(M, M);
		numParams += getLayerDimension(M, rawStateSize);
		return numParams;
	}

	public int getStateEncoderDimension() {
		int M = EncoderParams.getM();
		int rawStateSize = getRawStateSize();
		int numParams = 0;
		numParams += getLayerDimension(rawStateSize, M);
		numParams += getLayerDimension(M, M);
		return numParams;
	}

	private int getRawStateSize() {
		int rawStateSize = 0;
		for(String key : language.getStateKeys()) {
			rawStateSize += getTypeVectorSize(key);
		}
		return rawStateSize;
	}

	private int getLayerDimension(int from, int to) {
		return from * to + to;
	}
	
}

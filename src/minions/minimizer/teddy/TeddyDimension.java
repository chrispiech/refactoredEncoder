package minions.minimizer.teddy;

import models.encoder.EncoderParams;
import models.encoder.ModelFormat;
import models.encoder.dimension.Dimension;

public class TeddyDimension extends Dimension{

	int numOutputs;
	
	public TeddyDimension(int num) {
		numOutputs = num;
	}
	
	public int getDimension() {
		return getNumParameters();
	}
	
	public int getNumParameters() {
		int m = EncoderParams.getM();
		int recursiveSize = m*m + m*m;
		
		int outputNodeSize = getOutNodeSize();
		int outputSize = outputNodeSize * numOutputs;
		return recursiveSize + outputSize;
	}

	public int getOutNodeSize() {
		int m = EncoderParams.getM();
		return 2 * m*m + 2;
	}

	public int getNumDecoders() {
		return numOutputs;
	}
	
}

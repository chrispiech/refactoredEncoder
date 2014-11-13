package models.encoder.encoders;

import models.encoder.CodeVector;
import models.encoder.EncoderParams;

import org.ejml.simple.SimpleBase;
import org.ejml.simple.SimpleMatrix;

import util.MatrixUtil;
import util.NeuralUtils;

public class ConstantEncoder {
	
	private SimpleMatrix W = null;
	private SimpleMatrix b = null;

	public ConstantEncoder() {
		W = new SimpleMatrix(EncoderParams.getN(), 1);
		b = new SimpleMatrix(EncoderParams.getN(), 1);
	}
	
	public ConstantEncoder(SimpleMatrix W, SimpleMatrix b) {
		setParameters(W, b);
	}

	public SimpleMatrix getW() {
		return W;
	}

	public SimpleMatrix getB() {
		return b;
	}

	public SimpleMatrix getZ(int value) {
		SimpleMatrix x = new SimpleMatrix(1, 1);
		x.set(0, value);
		SimpleMatrix z =  W.mult(x).plus(b);
		MatrixUtil.validate(z);
		return z;
	}

	public CodeVector getActivation(SimpleMatrix z) {
		SimpleMatrix a = NeuralUtils.elementwiseApplyTanh(z);
		MatrixUtil.validate(a);
		return new CodeVector(a);
	}

	public void setParameters(SimpleMatrix newW, SimpleMatrix newB) {
		this.W = newW;
		this.b = newB;
		MatrixUtil.validate(W);
		MatrixUtil.validate(b);
	}

	

}

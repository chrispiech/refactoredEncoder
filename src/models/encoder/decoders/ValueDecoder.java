package models.encoder.decoders;

import java.util.ArrayList;
import java.util.List;

import models.code.State;
import models.code.TestTriplet;
import models.encoder.CodeVector;
import models.encoder.EncoderParams;
import models.encoder.ModelFormat;
import models.encoder.neurons.ValueNeuron;
import models.language.Language;

import org.ejml.simple.SimpleMatrix;

import util.MatrixUtil;

public class ValueDecoder {

	protected SimpleMatrix W;
	protected SimpleMatrix b;
	protected String key;
	
	public List<SimpleMatrix> getParams() {
		List<SimpleMatrix> params = new ArrayList<SimpleMatrix>();
		params.add(W);
		params.add(b);
		return params;
	}
	
	public void setParameters(SimpleMatrix W, SimpleMatrix b) {
		this.W = W;
		this.b = b;
	}
	
	public void setW(SimpleMatrix W) {
		this.W = W;
	}

	public SimpleMatrix getW() {
		return W;
	}

	public SimpleMatrix getB() {
		return b;
	}

	public int getDimension() {
		return W.getNumElements() + b.getNumElements();
	}
	
	public boolean equals(Object obj) {
		ValueDecoder o = (ValueDecoder)obj;
		if(!MatrixUtil.equals(W, o.W)) return false;
		return MatrixUtil.equals(b, o.b);
	}

	public void scale(double d) {
		W = W.scale(d);
		b = b.scale(d);
	}
	
	public SimpleMatrix getZ(SimpleMatrix v) {
		SimpleMatrix z = W.mult(v).plus(b);
		MatrixUtil.validate(z);
		return z;
	}
	
	public SimpleMatrix decodeMatrix(SimpleMatrix cv) {
		throw new RuntimeException("wrong decoder type");
	}
	
	public int decodeChoice(SimpleMatrix sv) {
		throw new RuntimeException("wrong decoder type");
	}
	
	public int decodeNumber(SimpleMatrix cv) {
		throw new RuntimeException("wrong decoder type");
	}

	public ValueNeuron outActivation(SimpleMatrix postv, String key) {
		throw new RuntimeException("abstract class");
	}

	public int getNumOutputs() {
		return 1;
	}	
	
	public double logLoss(State state, SimpleMatrix stateVector) {
		throw new RuntimeException("abstract class");
	}
	
}

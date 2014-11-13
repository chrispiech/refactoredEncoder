package models.encoder.encoders;

import java.util.ArrayList;
import java.util.List;

import models.encoder.CodeVector;
import models.encoder.EncoderParams;
import models.encoder.NeuronLayer;

import org.ejml.simple.SimpleMatrix;

import util.MatrixUtil;
import util.NeuralUtils;

public class InternalEncoder implements NeuronLayer {
	
	// The parameters for an internal encoder.
	private List<SimpleMatrix> wList = new ArrayList<SimpleMatrix>();
	private SimpleMatrix b;
	private String id;
	
	public InternalEncoder(String id, int arity) {
		int n = EncoderParams.getCodeVectorSize();
		double std = EncoderParams.getInitStd();
		for(int i = 0; i < arity; i++) {
			SimpleMatrix Wi = MatrixUtil.randomMatrix(n, n, std);
			wList.add(Wi);
		}
		b = MatrixUtil.randomVector(n, std);
		this.id = id;
	}
	
	public InternalEncoder(InternalEncoder toCopy) {
		wList = new ArrayList<SimpleMatrix>();
		for(SimpleMatrix wToCopy : toCopy.wList) {
			wList.add(new SimpleMatrix(wToCopy));
		}
		b = new SimpleMatrix(toCopy.b);
		id = toCopy.id;
	}
	
	public InternalEncoder(String id, List<SimpleMatrix> wList, SimpleMatrix b) {
		this.id = id;
		this.wList = wList;
		this.b = b;
	}

	public CodeVector getActivation(SimpleMatrix z) {
		SimpleMatrix a = NeuralUtils.elementwiseApplyTanh(z);
		return new CodeVector(a);
	}


	public SimpleMatrix getW(int i) {
		return wList.get(i);
	}
	
	public SimpleMatrix getB() {
		return b;
	}
	
	public SimpleMatrix getZ(List<CodeVector> childAList) {
		SimpleMatrix z = new SimpleMatrix(b);
		for(int i = 0; i < getArity(); i++) {
			SimpleMatrix childA = childAList.get(i).getVector();
			SimpleMatrix W = wList.get(i);
			z = z.plus(W.mult(childA));
		}
		MatrixUtil.validate(z);
		return z;
	}
	
	public void setParameters(List<SimpleMatrix> wList, SimpleMatrix b) {
		this.wList = wList;
		this.b = b;
	}
	
	public void setW(int i, SimpleMatrix W) {
		this.wList.set(i, W);
	}

	public int getArity() {
		return wList.size();
	}

	public String getType() {
		return id;
	}
	
	public String getNodeType() {
		if(id.equals("block")) {
			return "block";
		}
		if(id.equals("ifElse")) {
			return "maze_ifElse";
		}
		if(id.equals("while")) {
			return "maze_forever";
		}
		throw new RuntimeException("not type: " + id);
	}
	
	public boolean equals(Object obj) {
		InternalEncoder o = (InternalEncoder)obj;
		if(!id.equals(o.id)) return false;
		for(int i = 0; i < getArity(); i++) {
			SimpleMatrix Wa = getW(i);
			SimpleMatrix Wb = o.getW(i);
			if(!MatrixUtil.equals(Wa, Wb)) return false;
		}
		if(!MatrixUtil.equals(b, o.b)) return false;
		return true;
	}

	@Override
	public int getDimension() {
		int n = EncoderParams.getCodeVectorSize();
		return getArity() * n * n + n;
	}

	public void scale(double d) {
		for(int i = 0; i < getArity(); i++) {
			SimpleMatrix W = wList.get(i);
			W = W.scale(d);
			wList.set(i, W);
		}
		b = b.scale(d);
	}

	
}

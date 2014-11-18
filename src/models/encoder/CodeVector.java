package models.encoder;

import org.apache.commons.math3.ml.clustering.Clusterable;
import org.ejml.simple.SimpleMatrix;

import util.MatrixUtil;

public class CodeVector implements Clusterable{

	private SimpleMatrix vector;
	
	
	public CodeVector(CodeVector value) {
		vector = new SimpleMatrix(value.vector);
	}

	public CodeVector() {
		vector = new SimpleMatrix(1, EncoderParams.getCodeVectorSize());
	}
	
	public CodeVector(SimpleMatrix v) {
		vector = v;
	}

	public static CodeVector randomCodeVector() {
		CodeVector cv = new CodeVector();
		int n = EncoderParams.getCodeVectorSize();
		cv.vector = MatrixUtil.randomVector(n, 0.01);
		return cv;
	}
	
	public String toString() {
		return vector.toString();
	}

	public SimpleMatrix getVector() {
		return vector;
	}

	public void set(SimpleMatrix v) {
		this.vector = v;
	}

	public double get(int i) {
		return vector.get(i);
	}
	
	public void set(int i, double value) {
		vector.set(i, value);
	}

	public void scale(double d) {
		vector = vector.scale(d);
	}
	
	public double getDist(CodeVector other) {
		return MatrixUtil.euclidDist(vector, other.vector);
	}

	@Override
	public double[] getPoint() {
		double [] point = new double[vector.getNumElements()];
		for(int i = 0; i < point.length; i++) {
			point[i] = vector.get(i);
		}
		return point;
	}

}

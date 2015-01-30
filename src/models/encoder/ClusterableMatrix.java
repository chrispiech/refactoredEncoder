package models.encoder;

import org.apache.commons.math3.ml.clustering.Clusterable;
import org.ejml.simple.SimpleMatrix;

import util.MatrixUtil;

public class ClusterableMatrix implements Clusterable{

	private SimpleMatrix matrix;
	
	
	public ClusterableMatrix(ClusterableMatrix value) {
		matrix = new SimpleMatrix(value.matrix);
	}

	public ClusterableMatrix() {
		matrix = new SimpleMatrix(1, EncoderParams.getCodeVectorSize());
	}
	
	public ClusterableMatrix(SimpleMatrix v) {
		matrix = v;
	}
	
	public String toString() {
		return matrix.toString();
	}

	public SimpleMatrix getVector() {
		return matrix;
	}

	public void set(SimpleMatrix v) {
		this.matrix = v;
	}

	public double get(int i) {
		return matrix.get(i);
	}
	
	public void set(int i, double value) {
		matrix.set(i, value);
	}

	public void scale(double d) {
		matrix = matrix.scale(d);
	}
	
	public double getDist(ClusterableMatrix other) {
		return MatrixUtil.euclidDist(matrix, other.matrix);
	}

	@Override
	public double[] getPoint() {
		double [] point = new double[matrix.getNumElements()];
		for(int i = 0; i < point.length; i++) {
			point[i] = matrix.get(i);
		}
		return point;
	}

}

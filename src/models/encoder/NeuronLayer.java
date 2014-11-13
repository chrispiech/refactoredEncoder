package models.encoder;

import org.ejml.simple.SimpleMatrix;

public interface NeuronLayer {
	
	public SimpleMatrix getW(int childIndex);
	public SimpleMatrix getB();
	public int getDimension();
	
}

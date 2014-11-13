package models.encoder.neurons;

import java.util.List;

import org.ejml.simple.SimpleMatrix;

import util.MatrixUtil;

public class ValueNeuron extends Neuron{
	private String key;
	private int size;
	
	public ValueNeuron(SimpleMatrix z, SimpleMatrix a, String key) {
		this.z = z;
		this.activation = a;
		this.key = key;
		this.size = z.getNumElements();
	}
	
	public String getKey() {
		return key;
	}

	public int getSize() {
		return this.size;
	}
	
}

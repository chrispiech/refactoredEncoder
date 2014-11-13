package models.encoder.neurons;

import models.encoder.CodeVector;

import org.ejml.simple.SimpleMatrix;

public class Neuron {
	
	protected SimpleMatrix z;
	protected SimpleMatrix activation;
	protected SimpleMatrix error;
	
	public SimpleMatrix getActivation() {
		return activation;
	}

	public void setActivation(SimpleMatrix v) {
		activation = v;
	}

	public SimpleMatrix getZ() {
		return z;
	}

	public void setZ(SimpleMatrix z) {
		this.z = z;
	}
	
	public void setError(SimpleMatrix m) {
		this.error = m;
	}
	
	public SimpleMatrix getError() {
		return this.error;
	}

}

package models.encoder.neurons;

import models.code.State;

public class StateNeuron extends Neuron  {

	private State state;
	
	public StateNeuron(State state) {
		this.state = state;
	}

	public State getState() {
		return state;
	}

}

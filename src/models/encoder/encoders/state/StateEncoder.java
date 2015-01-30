package models.encoder.encoders.state;

import java.util.*;

import minions.encoder.factory.EncoderFactory;
import models.code.State;
import models.encoder.EncoderParams;
import models.encoder.ModelFormat;
import models.encoder.decoders.ValueDecoder;
import models.encoder.decoders.SoftmaxDecoder;
import models.encoder.neurons.StateNeuron;

import org.ejml.simple.SimpleMatrix;

import util.MatrixUtil;
import util.NeuralUtils;
import util.Warnings;

public class StateEncoder implements StateEncoderI{
	
	private Map<String, SimpleMatrix> wMap = new HashMap<String, SimpleMatrix>();
	private SimpleMatrix b;
	private ModelFormat format;

	public StateEncoder(Map<String, SimpleMatrix> wMap, SimpleMatrix b, ModelFormat f) {
		this.wMap = wMap;
		this.b = b;
		this.format = f;
	}

	public SimpleMatrix activateState(StateNeuron input) {
		int m = EncoderParams.getM();
		SimpleMatrix z = new SimpleMatrix(m, 1);
		z = z.plus(b);
		State state = input.getState();
		for(String key : format.getLanguage().getStateKeys()) {
			SimpleMatrix x = state.getActivation(format, key);
			SimpleMatrix W = wMap.get(key);
			z = z.plus(W.mult(x));
		}
		SimpleMatrix a = NeuralUtils.elementwiseApplyTanh(z);
		input.setZ(z);
		input.setActivation(a);
		return a;
	}
	
	public SimpleMatrix getVector(State state) {
		StateNeuron stateNeuron = new StateNeuron(state);
		return activateState(stateNeuron);
	}
	
	public SimpleMatrix getW(String key) {
		return wMap.get(key);
	}
	
	public SimpleMatrix getB() {
		return b;
	}
	
	@Override
	public boolean equals(Object o) {
		StateEncoder other = (StateEncoder)o;
		if(!MatrixUtil.equals(this.b, other.b)) return false;
		if(!wMap.keySet().equals(other.wMap.keySet())) return false;
		for(String key : wMap.keySet()) {
			SimpleMatrix w1 = wMap.get(key);
			SimpleMatrix w2 = other.wMap.get(key);
			if(!MatrixUtil.equals(w1, w2)) return false;
		}
		return true;
	}

	public void scale(double d) {
		b = b.scale(d);
		for(String key : wMap.keySet()) {
			SimpleMatrix newW = wMap.get(key).scale(d);
			wMap.put(key, newW);
		}
	}

	public void setW(String key, SimpleMatrix W) {
		wMap.put(key, W);
	}

	public void setB(SimpleMatrix b) {
		this.b = b;
	}

	public Set<String> getStateKeys() {
		return wMap.keySet();
	}

	public double getWeightLoss() {
		double loss = 0;
		for(String key : wMap.keySet()) {
			SimpleMatrix W = wMap.get(key);
			loss += (EncoderParams.getWeightDecay() / 2.0) * MatrixUtil.norm(W);
		}
		return loss;
	}

}

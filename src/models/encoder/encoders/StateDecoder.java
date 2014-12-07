package models.encoder.encoders;

import java.util.*;

import org.ejml.simple.SimpleMatrix;

import util.MatrixUtil;
import util.Warnings;
import minions.encoder.factory.EncoderFactory;
import models.code.State;
import models.encoder.CodeVector;
import models.encoder.EncoderParams;
import models.encoder.ModelFormat;
import models.encoder.decoders.ValueDecoder;


public class StateDecoder {
	
	private Map<String, ValueDecoder> decoderMap = null;
	private ModelFormat format = null;

	public StateDecoder(ModelFormat f, Map<String, ValueDecoder> map) {
		this.decoderMap = map;
		this.format = f;
	}

	public StateDecoder(ModelFormat format) {
		this.format = format;
		decoderMap = new HashMap<String, ValueDecoder>();
		for(String key : format.getStateKeys()) {
			ValueDecoder encoder = EncoderFactory.makeOutput(format, key);
			decoderMap.put(key, encoder);
		}
	}

	public State getState(SimpleMatrix sv) {
		Map<String, String> choiceMap = new HashMap<String, String>();
		Map<String, Integer> numMap = new HashMap<String, Integer>();
		Map<String, SimpleMatrix> matrixMap = new HashMap<String, SimpleMatrix>();
		for(String key : format.getStateKeys()) {
			ValueDecoder encoder = decoderMap.get(key);
			String type = format.getOutputType(key);
			if(type.equals("choice")) {
				int choiceIndex = encoder.decodeChoice(sv);
				String choice = format.getChoiceOptions(key)[choiceIndex];
				choiceMap.put(key, choice);
			} else if(type.equals("number")){
				int number = encoder.decodeNumber(sv);
				numMap.put(key, number);
			} else {
				SimpleMatrix m = encoder.decodeMatrix(sv);
				matrixMap.put(key, m);
			}
		}
		return new State(choiceMap, numMap, matrixMap);
	}

	public ValueDecoder getOutputDecoder(String outKey) {
		return decoderMap.get(outKey);
	}
	
	@Override
	public boolean equals(Object o) {
		StateDecoder other = (StateDecoder)o;
		for(String key : decoderMap.keySet()) {
			ValueDecoder a = decoderMap.get(key);
			ValueDecoder b = other.decoderMap.get(key);
			if(!a.equals(b)) return false;
		}
		return true;	
	}

	public int getNumOutputs() {
		return decoderMap.size();
	}

	public void scale(double d) {
		for(String key : decoderMap.keySet()) {
			decoderMap.get(key).scale(d);
		}
	}

	public Set<String> getStateKeys() {
		return decoderMap.keySet();
	}

	public double getLogLoss(State state, SimpleMatrix stateVector) {
		double logLoss = 0;
		for(String key : getStateKeys()) {
			ValueDecoder outDecoder = getOutputDecoder(key);
			logLoss += outDecoder.logLoss(state, stateVector);
		}
		return logLoss;
	}

	public double getWeightLoss() {
		double loss = 0;
		for(String key : decoderMap.keySet()) {
			ValueDecoder decoder = decoderMap.get(key);
			SimpleMatrix W = decoder.getW();
			loss += (EncoderParams.getWeightDecay() / 2.0) * MatrixUtil.norm(W);
		}
		return loss;
	}
	
	
	
	
}

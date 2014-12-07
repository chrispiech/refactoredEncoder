package models.code;

import java.util.*;

import models.encoder.ModelFormat;
import models.encoder.decoders.SoftmaxDecoder;
import models.language.Language;

import org.ejml.simple.SimpleMatrix;

import util.MatrixUtil;

public class State {
	
	private Map<String, String> choiceMap;
	private Map<String, Integer> numMap;
	private Map<String, SimpleMatrix> matrixMap;

	public State(
			Map<String, String> choiceMap, 
			Map<String, Integer> numMap,
			Map<String, SimpleMatrix> matrixMap) {
		this.choiceMap = choiceMap;
		this.numMap = numMap;
		this.matrixMap = matrixMap;
	}
	
	public Set<String> getKeys() {
		Set<String> keys = new HashSet<String>();
		keys.addAll(choiceMap.keySet());
		keys.addAll(numMap.keySet());
		keys.addAll(matrixMap.keySet());
		return keys;
	}
	
	public String getChoice(String key) {
		return choiceMap.get(key);
	}

	public int getNumber(String key) {
		return numMap.get(key);
	}
	
	public SimpleMatrix getMatrix(String key) {
		return matrixMap.get(key);
	}
	
	public SimpleMatrix getMatrixVector(String key) {
		SimpleMatrix m = getMatrix(key);
		int rows = m.getNumElements();
		SimpleMatrix mPrime = new SimpleMatrix(m);
		mPrime.reshape(rows, 1);
		
		SimpleMatrix withDim = new SimpleMatrix(rows + 2, 1);
		withDim.set(0, m.numRows());
		withDim.set(1, m.numCols());
		for(int i = 0; i < rows; i++) {
			withDim.set(i + 2, mPrime.get(i));
		}
		
		return mPrime;
	}
	
	@Override
	public boolean equals(Object o) {
		State other = (State)o;
		for(String key : choiceMap.keySet()) {
			if(!choiceMap.get(key).equals(other.choiceMap.get(key))) {
				return false;
			}
		}
		for(String key : numMap.keySet()) {
			if(numMap.get(key) != other.numMap.get(key)) {
				return false;
			}
		}
		for(String key : matrixMap.keySet()) {
			SimpleMatrix a = matrixMap.get(key);
			SimpleMatrix b = other.matrixMap.get(key);
			return MatrixUtil.equals(a, b);
		}
		return true;
	}
	
	public int hashCode() {
		List<Object> objs = new LinkedList<Object>();
		objs.add(choiceMap);
		objs.add(numMap);
		return objs.hashCode();
	}
	
	@Override
	public String toString() {
		String str = "";
		for(String key : choiceMap.keySet()) {
			str += key + ":\t" + choiceMap.get(key) + "\n";
		}
		return str;
	}
	
	

	public SimpleMatrix getActivation(ModelFormat format, String key) {
		if(matrixMap.containsKey(key)) {
			return getMatrixVector(key);
		}
		if(numMap.containsKey(key)) {
			int num = getNumber(key);
			SimpleMatrix numMat = new SimpleMatrix(1, 1);
			numMat.set(0, num);
			return numMat;
		}
		if(choiceMap.containsKey(key)) {
			Language l = format.getLanguage();
			int truthIndex = SoftmaxDecoder.getTruthIndex(l, this, key);
			int numOptions = format.getChoiceOptions(key).length;
			SimpleMatrix options = new SimpleMatrix(numOptions, 1);
			options.set(truthIndex, 1);
			return options;
		}
		throw new RuntimeException("no");
	}

	
}

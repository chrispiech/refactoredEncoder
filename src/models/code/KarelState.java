package models.code;

import java.util.*;

import models.encoder.ModelFormat;
import models.encoder.decoders.SoftmaxDecoder;
import models.language.Language;

import org.ejml.simple.SimpleMatrix;

import util.MatrixUtil;

public class KarelState implements State{

	byte row;
	byte col;
	byte dir;
	byte status;
	byte worldRows;
	byte worldCols;
	SimpleMatrix beepers = null;



	public KarelState(int row, int col, int dir, 
			int status, int worldRows, int worldCols, SimpleMatrix beepers) {
		this.row = (byte) row;
		this.col = (byte) col;
		this.dir = (byte) dir;
		this.status = (byte) status;
		this.worldRows = (byte) worldRows;
		this.worldCols = (byte) worldCols;
		this.beepers = beepers;
	}

	public String getChoice(String key) {
		if(key.equals("row")) return row + "";
		if(key.equals("col")) return col + "";
		if(key.equals("status")) return status + "";
		if(key.equals("direction")) return dir + "";
		throw new RuntimeException("what is this? " + key);
	}

	public int getNumber(String key) {
		if(key.equals("worldRows")) return worldRows;
		if(key.equals("worldCols")) return worldCols;
		throw new RuntimeException("what is this? " + key);
	}

	public SimpleMatrix getMatrix(String key) {
		if(key.equals("beepers")) return beepers;
		throw new RuntimeException("what is this? " + key);
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
		KarelState other = (KarelState)o;
		if(row != other.row) return false;
		if(col != other.col) return false;
		if(dir != other.dir) return false;
		if(status != other.status) return false;
		if(worldRows != other.worldRows) return false;
		if(worldCols != other.worldCols) return false;
		if(!MatrixUtil.equals(beepers, other.beepers)) return false;
		return true;
	}

	public int hashCode() {
		List<Object> objs = new ArrayList<Object>();
		objs.add(new Integer(row));
		objs.add(new Integer(col));
		objs.add(new Integer(dir));
		objs.add(new Integer(status));
		objs.add(new Integer(worldRows));
		objs.add(new Integer(worldCols));
		return objs.hashCode();
	}

	@Override
	public String toString() {
		throw new RuntimeException("not done");
	}

	public SimpleMatrix getActivation(ModelFormat format, String key) {
		if(key.equals("beepers")) {
			return getMatrixVector(key);
		} 
		if(key.equals("worldRows") || key.equals("worldCols")) {
			int num = getNumber(key);
			SimpleMatrix numMat = new SimpleMatrix(1, 1);
			numMat.set(0, num);
			return numMat;
		}

		Language l = format.getLanguage();
		int truthIndex = SoftmaxDecoder.getTruthIndex(l, this, key);
		int numOptions = format.getChoiceOptions(key).length;
		SimpleMatrix options = new SimpleMatrix(numOptions, 1);
		options.set(truthIndex, 1);
		return options;
	}

	public SimpleMatrix getMatrix(ModelFormat format) {
		List<SimpleMatrix> values = new ArrayList<SimpleMatrix>();
		int size = 0;
		for(String key : format.getStateKeys()) {
			SimpleMatrix value = getActivation(format, key);
			values.add(value);
			size += value.getNumElements();
		}

		SimpleMatrix m = new SimpleMatrix(size, 1);
		int i = 0;
		for(SimpleMatrix v : values) {
			for(int j = 0; j < v.getNumElements(); j++) {
				m.set(i, v.get(j));
				i++;
			}
		}
		return m;
	}


}

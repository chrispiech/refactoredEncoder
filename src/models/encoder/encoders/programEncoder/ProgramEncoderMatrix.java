package models.encoder.encoders.programEncoder;

import java.util.*;

import minions.parser.EncodeTreeParser;
import models.ast.Tree;
import models.encoder.ClusterableMatrix;
import models.encoder.EncodeGraph;
import models.encoder.EncoderParams;
import models.encoder.ModelFormat;
import models.encoder.decoders.*;
import models.encoder.encoders.InternalEncoder;
import models.encoder.neurons.TreeNeuron;

import org.apache.commons.math3.linear.MatrixUtils;
import org.ejml.simple.SimpleMatrix;

import util.MatrixUtil;
import util.NeuralUtils;
import util.Warnings;

public class ProgramEncoderMatrix implements ProgramEncoder{

	private ModelFormat format;
	private HashMap<String, InternalEncoder> internalEncoders;
	private HashMap<String, SimpleMatrix> leafEmbeddings;

	/**
	 * Method: Empty Constructor
	 * -------------------------
	 * Create a starting model with default values for all parameters.
	 */
	public ProgramEncoderMatrix(ModelFormat format) {
		this.format = format;

		leafEmbeddings = new HashMap<String, SimpleMatrix>();
		for(String type : format.getLeafTypes()) {
			leafEmbeddings.put(type, randomCodeMatrix());
		}

		internalEncoders = new HashMap<String, InternalEncoder>();
		for(String type : format.getInternalEncoderTypes()) {
			int arity = format.getArity(type);
			InternalEncoder encoder = new InternalEncoder(type, arity);
			internalEncoders.put(type, encoder);
		}
	}

	private SimpleMatrix randomCodeMatrix() {
		int m = EncoderParams.getStateVectorSize();
		double std = EncoderParams.getInitStd();
		return MatrixUtil.randomMatrix(m, m, std);
	}

	/**
	 * Method: Encoder Constructor
	 * -------------------------
	 * Create a model from the format, internalEncoders, outEncoders and leaves
	 */
	public ProgramEncoderMatrix(ModelFormat f, HashMap<String, InternalEncoder> in,
			HashMap<String, SimpleMatrix> leaf) {
		this.format = f;
		this.internalEncoders = in;
		this.leafEmbeddings = leaf;
	}

	/**
	 * Method: Copy Constructor
	 * ---------------
	 * Makes a deep copy of the model.
	 */
	public ProgramEncoderMatrix(ProgramEncoderMatrix toCopy) {
		this.format = toCopy.format;

		leafEmbeddings = new HashMap<String, SimpleMatrix>();
		for(String key : toCopy.leafEmbeddings.keySet()) {
			SimpleMatrix value = toCopy.leafEmbeddings.get(key);
			leafEmbeddings.put(key, new SimpleMatrix(value));
		}

		throw new RuntimeException("should also copy internal encoders...");
	}



	public InternalEncoder getInternalEncoder(String type) {
		if(!internalEncoders.containsKey(type)) {
			throw new RuntimeException("eh? " + type);
		}
		return internalEncoders.get(type);
	}

	public InternalEncoder getInternalEncoder(TreeNeuron node) {
		String type = format.getEncoderType(node.getType());
		return getInternalEncoder(type);
	}


	public Set<String> getLeafTypes() {
		return leafEmbeddings.keySet();
	}

	public Collection<InternalEncoder> getInternalEncoders() {
		return internalEncoders.values();
	}

	private Collection<SimpleMatrix> getLeafVectors() {
		return leafEmbeddings.values();
	}

	public ModelFormat getFormat() {
		return format;
	}

	public int getNorm() {
		throw new RuntimeException("todo");
	}

	@Override 
	public boolean equals(Object o) {
		ProgramEncoderMatrix other = (ProgramEncoderMatrix)o;
		for(String type : format.getLeafTypes()) {
			SimpleMatrix v1 = getLeafEmbedding(type);
			SimpleMatrix v2 = other.getLeafEmbedding(type);
			if(!MatrixUtil.equals(v1, v2)) return false;
		}

		for(String type : format.getInternalEncoderTypes()) {
			InternalEncoder e1 = getInternalEncoder(type);
			InternalEncoder e2 = other.getInternalEncoder(type);
			if(!e1.equals(e2)) return false;
		}
		return true;
	}


	//*********************************************************************************
	//*                 ACTIVATION
	//*********************************************************************************

	/**
	 * Method: Calculate Activation
	 * ----------------------------
	 * Calculates the z (input) and a (activation) values for all internal nodes
	 * in the encode tree.
	 */
	public SimpleMatrix activateTree(TreeNeuron et) {
		for(TreeNeuron child : et.getChildren()) {
			activateTree(child);
		}
		nodeActivation(et);
		SimpleMatrix cv = new SimpleMatrix(et.getActivation());
		Warnings.check(cv != null);
		return cv;
	}

	/**
	 * Method: Calculate Node Activation
	 * ---------------------------------
	 * Calculates the z (input) and a (activation) for a given node.
	 * Assumes that activation has been computed for all children.
	 */
	private void nodeActivation(TreeNeuron node) {
		if(node.isConstant()) {
			int value = Integer.parseInt(node.getType());
			int M = EncoderParams.getStateVectorSize();
			SimpleMatrix m = new SimpleMatrix(M, M);
			m.set(0, value);
			node.setActivation(m);
		} else if(node.isLeaf()) {
			SimpleMatrix a = getLeafEmbedding(node.getType());
			node.setActivation(a);
		} else {
			List<ClusterableMatrix> childActivations = node.getChildActivations();
			InternalEncoder encoder = getInternalEncoder(node);
			SimpleMatrix z = encoder.getZ(childActivations);
			node.setZ(z);
			ClusterableMatrix a = encoder.getActivation(z);
			node.setActivation(a.getVector());
		}
	}

	public void scale(double d) {
		for(InternalEncoder e : getInternalEncoders()) {
			e.scale(d);
		}
		for(String type : leafEmbeddings.keySet()) {
			SimpleMatrix v = leafEmbeddings.get(type);
			v = v.scale(d);
			leafEmbeddings.put(type, v);
		}
	}

	public double getWeightLoss() {
		double loss = 0;
		double lambda = EncoderParams.getWeightDecay();
		for(String key : internalEncoders.keySet()) {
			InternalEncoder encoder = internalEncoders.get(key);
			for(int i = 0; i < encoder.getArity(); i++) {
				SimpleMatrix W = encoder.getW(i);
				loss += (lambda / 2.0) * MatrixUtil.norm(W);
			}
		}
		for(String key : leafEmbeddings.keySet()) {
			SimpleMatrix v = leafEmbeddings.get(key);
			loss += (lambda / 2.0) * MatrixUtil.norm(v);
		}
		return loss;
	}

	@Override
	public SimpleMatrix getLeafEmbedding(String type) {
		return leafEmbeddings.get(type);
	}

	@Override
	public void setLeafEmbedding(String type, SimpleMatrix leaf) {
		leafEmbeddings.put(type, leaf);
	}


}

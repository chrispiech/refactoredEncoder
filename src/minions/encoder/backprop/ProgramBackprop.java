package minions.encoder.backprop;

import java.util.ArrayList;
import java.util.List;

import org.ejml.simple.SimpleMatrix;

import util.NeuralUtils;
import models.code.TestTriplet;
import models.encoder.CodeVector;
import models.encoder.EncoderParams;
import models.encoder.encoders.Encoder;
import models.encoder.encoders.InternalEncoder;
import models.encoder.encoders.ProgramEncoder;
import models.encoder.neurons.TreeNeuron;

public class ProgramBackprop {

	/*****************************************************
	 * The per node mus, which are dL/dZ. Aka Error.
	 *****************************************************/
	public static void treeError(
			ProgramEncoder pe, 
			TreeNeuron tree, 
			SimpleMatrix parentError, 
			SimpleMatrix parentW,
			int depth) {
		int maxDepth = EncoderParams.getDepthLimit();
		if(maxDepth > 0 && depth > maxDepth) return;
		if(!tree.isLeaf()) {
			nodeError(tree, parentError, parentW);
			childError(pe, tree, depth);
		}
	}
	
	private static void nodeError(TreeNeuron tree, SimpleMatrix parentError,
			SimpleMatrix parentW) {
		SimpleMatrix z = tree.getZ();
		SimpleMatrix fPrime = NeuralUtils.elementTanhGrad(z);
		SimpleMatrix Werror = parentW.transpose().mult(parentError);
		SimpleMatrix error = Werror.elementMult(fPrime);
		if(!error.isVector() || error.numRows() != EncoderParams.getN()) {
			throw new RuntimeException("wrong size");
		}
		tree.setError(error);
	}

	public static void childError(ProgramEncoder pe, TreeNeuron tree, int depth) {
		InternalEncoder encoder = pe.getInternalEncoder(tree);
		for(int i = 0; i < tree.numChildren(); i++) {
			TreeNeuron child = tree.getChild(i);
			SimpleMatrix Wi = encoder.getW(i);
			treeError(pe, child, tree.getError(), Wi, depth + 1);
		}
	}
	
	/*********************
	 * Gradient Step
	 *********************/
	
	public static void gradientStepTree(
			ProgramEncoder model,
			ProgramEncoder grad,
			TreeNeuron tree, 
			SimpleMatrix parentError,
			SimpleMatrix parentW,
			int depth) {
		int maxDepth = EncoderParams.getDepthLimit();
		if(maxDepth > 0 && depth > maxDepth) return;
		
		gradientStepNode(model, grad, tree, parentError, parentW);
		gradStepChildren(model, grad, tree, depth);
	}
	
	private static void gradientStepNode(
			ProgramEncoder model,
			ProgramEncoder grad,
			TreeNeuron node, 
			SimpleMatrix parentError, 
			SimpleMatrix parentW) {
		if(node.isLeaf()) {
			gradStepLeaf(model, grad, node, parentError, parentW);
		} else {
			gradStepInternal(model, grad, node);
		} 
	}
	
	private static void gradStepLeaf(
			ProgramEncoder model,
			ProgramEncoder grad,
			TreeNeuron node, SimpleMatrix parentError,
			SimpleMatrix parentW) {
		String type = node.getType();
		SimpleMatrix dF = parentW.transpose().mult(parentError);
		updateGradLeaf(grad, dF, type);
	}

	public static void gradStepChildren(
			ProgramEncoder model,
			ProgramEncoder grad,
			TreeNeuron tree,
			int depth) {
		for(int i = 0; i < tree.numChildren(); i++) {
			TreeNeuron child = tree.getChild(i);
			InternalEncoder encoder = model.getInternalEncoder(tree);
			SimpleMatrix W = encoder.getW(i);
			gradientStepTree(model, grad, child, tree.getError(), W, depth + 1);
		}
	}

	public static void gradStepInternal(
			ProgramEncoder model, 
			ProgramEncoder grad,
			TreeNeuron node) {
		InternalEncoder encoder = model.getInternalEncoder(node);
		SimpleMatrix nodeError = node.getError();

		// calculate derivatives...
		List<SimpleMatrix> dWs = new ArrayList<SimpleMatrix>();
		for(int i = 0; i < encoder.getArity(); i++){
			TreeNeuron child = node.getChild(i);
			SimpleMatrix childA = child.getActivation();
			SimpleMatrix dW = nodeError.mult(childA.transpose());
			dWs.add(dW);
		}
		SimpleMatrix dB = new SimpleMatrix(nodeError);

		InternalEncoder gradEncoder = grad.getInternalEncoder(node);
		updateGradInternal(gradEncoder, dWs, dB);
	}

	public static void addWeightDecay(ProgramEncoder model, ProgramEncoder modelGrad) {
		for(String type : model.getFormat().getInternalEncoderTypes()) {
			InternalEncoder modelEncoder = model.getInternalEncoder(type);
			InternalEncoder gradEncoder = modelGrad.getInternalEncoder(type);
			for(int i = 0; i < gradEncoder.getArity(); i++) {
				SimpleMatrix dW = gradEncoder.getW(i);
				SimpleMatrix W = modelEncoder.getW(i);
				dW = dW.plus(W.scale(EncoderParams.getWeightDecay()));
				gradEncoder.setW(i, dW);
			}
		}
		for(String leafType : model.getFormat().getLeafTypes()) {
			CodeVector leafGrad = modelGrad.getLeafVector(leafType);
			SimpleMatrix v = model.getLeafVector(leafType).getVector();
			SimpleMatrix dV = leafGrad.getVector();
			dV = dV.plus(v.scale(EncoderParams.getWeightDecay())); 
			leafGrad.set(dV);
		}
	}
	
	public static void updateGradLeaf(
			ProgramEncoder grad, 
			SimpleMatrix dF, String type) {
		CodeVector leaf = grad.getLeafVector(type);
		SimpleMatrix newV = leaf.getVector().plus(dF);
		leaf.set(newV);
		grad.setLeafVector(type, leaf);
	}
	

	private static void updateGradInternal(InternalEncoder encoder,
			List<SimpleMatrix> dWs, SimpleMatrix dB) {
		List<SimpleMatrix> newWList = new ArrayList<SimpleMatrix>();
		for(int i = 0; i < encoder.getArity(); i++) {
			SimpleMatrix newW = encoder.getW(i).plus(dWs.get(i));
			newWList.add(newW);
		}
		SimpleMatrix newB = encoder.getB().plus(dB);
		encoder.setParameters(newWList, newB);
	}
}

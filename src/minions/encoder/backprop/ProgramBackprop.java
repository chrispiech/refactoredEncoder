package minions.encoder.backprop;

import java.util.ArrayList;
import java.util.List;

import models.encoder.ClusterableMatrix;
import models.encoder.EncoderParams;
import models.encoder.encoders.InternalEncoder;
import models.encoder.encoders.programEncoder.ProgramEncoder;
import models.encoder.encoders.programEncoder.ProgramEncoder;
import models.encoder.neurons.TreeNeuron;

import org.ejml.simple.SimpleMatrix;

import util.NeuralUtils;
import util.StrUtil;

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
			ProgramEncoder programModel,
			ProgramEncoder programGrad,
			TreeNeuron tree, 
			SimpleMatrix parentError,
			SimpleMatrix parentW,
			int depth) {
		
		gradientStepNode(programModel, programGrad, tree, parentError, parentW);
		gradStepChildren(programModel, programGrad, tree, depth);
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
			ProgramEncoder programModel,
			ProgramEncoder programGrad,
			TreeNeuron tree,
			int depth) {
		for(int i = 0; i < tree.numChildren(); i++) {
			TreeNeuron child = tree.getChild(i);
			InternalEncoder encoder = programModel.getInternalEncoder(tree);
			SimpleMatrix W = encoder.getW(i);
			gradientStepTree(programModel, programGrad, child, tree.getError(), W, depth + 1);
		}
	}

	public static void gradStepInternal(
			ProgramEncoder programModel, 
			ProgramEncoder programGrad,
			TreeNeuron node) {
		InternalEncoder encoder = programModel.getInternalEncoder(node);
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

		InternalEncoder gradEncoder = programGrad.getInternalEncoder(node);
		updateGradInternal(gradEncoder, dWs, dB);
	}

	public static void addWeightDecay(ProgramEncoder model, ProgramEncoder modelGrad) {
		for(String type : model.getFormat().getInternalEncoderTypes()) {
			InternalEncoder modelEncoder = model.getInternalEncoder(type);
			InternalEncoder gradEncoder = modelGrad.getInternalEncoder(type);
			for(int i = 0; i < gradEncoder.getArity(); i++) {
				SimpleMatrix dW = gradEncoder.getW(i);
				SimpleMatrix W = modelEncoder.getW(i);
				double lambda = EncoderParams.getWeightDecay();
				SimpleMatrix dR = W.scale(lambda);
				dW = dW.plus(dR);
				gradEncoder.setW(i, dW);
			}
		}
		for(String leafType : model.getFormat().getLeafTypes()) {
			SimpleMatrix dV = modelGrad.getLeafEmbedding(leafType);
			SimpleMatrix v = model.getLeafEmbedding(leafType);
			dV = dV.plus(v.scale(EncoderParams.getWeightDecay())); 
			modelGrad.setLeafEmbedding(leafType, dV);
		}
	}
	
	public static void updateGradLeaf(
			ProgramEncoder programGrad, 
			SimpleMatrix dF, String type) {
		if(StrUtil.isNumeric(type)) return;
		SimpleMatrix leaf = programGrad.getLeafEmbedding(type);
		SimpleMatrix newV = leaf.plus(dF);
		leaf.set(newV);
		programGrad.setLeafEmbedding(type, leaf);
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

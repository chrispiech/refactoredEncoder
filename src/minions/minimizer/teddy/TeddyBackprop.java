package minions.minimizer.teddy;

import java.util.List;
import java.util.Map;

import minions.encoder.backprop.StateDecoderBackprop;
import minions.encoder.factory.EncoderFactory;
import minions.encoder.modelVector.ModelVector;
import models.encoder.EncoderParams;
import models.encoder.ModelFormat;
import models.encoder.decoders.SoftmaxDecoder;
import models.encoder.decoders.ValueDecoder;
import models.encoder.encoders.InternalEncoder;
import models.encoder.encoders.models.LemurModel;
import models.encoder.neurons.TreeNeuron;
import models.encoder.neurons.ValueNeuron;

import org.ejml.simple.SimpleMatrix;

import util.MatrixUtil;
import util.NeuralUtils;

public class TeddyBackprop {

	protected TeddyModel model = null;
	protected TeddyModel modelGrad = null;
	protected TeddyDimension dimension = null;

	Map<String, TreeNeuron> programMap;
	Map<TreeNeuron, SimpleMatrix> encodingMap;
	Map<String, List<Boolean>> gradedMap;
	
	double sumError = 0;

	public static TeddyModel getGrad(TeddyModel model, 
			List<String> miniBatch, 
			Map<String, TreeNeuron> programMap, 
			Map<TreeNeuron, SimpleMatrix> encodingMap, 
			Map<String, List<Boolean>> gradedMap) {
		TeddyBackprop b = new TeddyBackprop();
		b.encodingMap = encodingMap;
		b.programMap = programMap;
		b.gradedMap = gradedMap;
		b.dimension = (TeddyDimension) new ModelFormat("karel", "teddy").getDimension();
		return b.run(model, miniBatch);
	}

	public TeddyModel run(TeddyModel model2, List<String> data) {
		this.model = model2;
		int dim = dimension.getDimension();
		double[] vec = new double[dim];
		this.modelGrad = TeddyVec.getTeddy(dimension, vec);
		
		for(String key : data) {
			addGradForTest(key);
		}
		if(data.size() != 1) {
			modelGrad.scale(1.0 / data.size());
		}

		// this is equivalent to adding weight decay once per test and then 
		// scaling weight decay with the rest of the grad by 1/data.size
		addWeightDecay(); 

		return modelGrad;
	}

	private void addGradForTest(String key) {
		TreeNeuron runTree = new TreeNeuron(programMap.get(key));
		List<Boolean> feedbacks = gradedMap.get(key);

		SimpleMatrix embedding = activateTree(runTree);
		int m = EncoderParams.getM();
		embedding.reshape(m*m, 1);

		// backprop for post error
		for(int i = 0; i < dimension.getNumDecoders(); i++){
			// guess values for the post state.
			SoftmaxDecoder outDecoder = (SoftmaxDecoder) model.getDecoder(i);
			ValueNeuron output = outDecoder.outActivation(embedding, i + "");

			Boolean truth = feedbacks.get(i);
			int truthIndex = truth ? 1 : 0;
			sumError += outDecoder.logLoss(truthIndex, embedding);
			sumError += weightError();
			
			// calculate the error terms of all neurons
			calculateError(runTree, output, truth, i);

			// add to the grad for all parameters
			addGrad(runTree, output, i);
		}

	}

	///////////// GRAD STEP ///////////

	private double weightError() {
		double loss = 0;
		double lambda = EncoderParams.getWeightDecay();
		loss += (lambda / 2.0) * MatrixUtil.norm(model.getW());
		return loss;
	}

	private void addGrad(TreeNeuron tree, ValueNeuron outNode, int index) {
		// update the output encoder params...
		gradientStepSoftmax(tree.getActivation(), outNode, index);

		// then update all the tree params...
		ValueDecoder parent = model.getDecoder(index);
		SimpleMatrix parentError = outNode.getError();
		SimpleMatrix parentW = parent.getW();
		gradientStepTree(tree, parentError, parentW);
	}

	private void gradientStepSoftmax(
			SimpleMatrix activation, 
			ValueNeuron outNode, int index) {
		SimpleMatrix error = outNode.getError();
		SimpleMatrix dW = error.mult(activation.transpose());
		SimpleMatrix dB = new SimpleMatrix(error);
		ValueDecoder outEncoder = modelGrad.getDecoder(index);
		StateDecoderBackprop.updateGradOut(outEncoder, dW, dB);
	}

	private void gradientStepTree(TreeNeuron tree, 
			SimpleMatrix parentError, 
			SimpleMatrix parentW){
		if(tree.isLeaf()) return;

		gradientStepNode(tree, parentError, parentW);
		for(int i = 0; i < tree.numChildren(); i++) {
			TreeNeuron child = tree.getChild(i);
			SimpleMatrix W = model.getW();
			gradientStepTree(child, tree.getError(), W);
		}
	}

	private void gradientStepNode(
			TreeNeuron node, 
			SimpleMatrix parentError, 
			SimpleMatrix parentW) {
		SimpleMatrix nodeError = node.getError();

		// calculate derivatives...
		int m = EncoderParams.getM();
		SimpleMatrix dW = new SimpleMatrix(m, m);
		for(int i = 0; i < node.numChildren(); i++){
			TreeNeuron child = node.getChild(i);
			SimpleMatrix childA = child.getActivation();
			dW = dW.plus(nodeError.mult(childA.transpose()));
		}
		SimpleMatrix dB = new SimpleMatrix(nodeError);

		// update grad values
		SimpleMatrix newW = modelGrad.getW().plus(dW);
		SimpleMatrix newB = modelGrad.getB().plus(dB);
		
		modelGrad.setW(newW);
		modelGrad.setB(newB);
	}

	///////////// ERROR ///////////


	private void calculateError(
			TreeNeuron tree, 
			ValueNeuron output,
			Boolean truth, 
			int index) {
		// calculate mu for the output neuron
		softmaxError(output, truth, index + "");

		// extract some useful matrices
		SimpleMatrix outputError = output.getError();
		ValueDecoder decoder = model.getDecoder(index);
		SimpleMatrix outputW = decoder.getW();

		// calculate mu for the tree
		treeError(tree, outputError, outputW);
	}


	private void treeError(TreeNeuron tree, SimpleMatrix parentError, 
			SimpleMatrix parentW){
		if(!tree.isLeaf()) {
			nodeError(tree, parentError, parentW);
			childError(tree, parentError);
		}
	}

	private void childError(TreeNeuron tree, SimpleMatrix parentError) {
		for(TreeNeuron child : tree.getChildren()) {
			treeError(child, tree.getError(), model.getW());
		}
	}

	private void nodeError(TreeNeuron tree, SimpleMatrix parentError,
			SimpleMatrix parentW) {
		SimpleMatrix z = tree.getZ();
		SimpleMatrix fPrime = NeuralUtils.elementTanhGrad(z);
		SimpleMatrix Werror = parentW.transpose().mult(parentError);
		if(Werror.numCols() == 1) {
			int m = EncoderParams.getM();
			Werror.reshape(m, m);
		}
		SimpleMatrix error = Werror.elementMult(fPrime);
		tree.setError(error);
	}

	private static void softmaxError(
			ValueNeuron outNode, Boolean truth, String key) {
		SimpleMatrix softMax = MatrixUtil.softmax(outNode.getZ());
		int truthIndex = truth ? 1 : 0;
		SimpleMatrix kronecker = MatrixUtil.basis(outNode.getSize(), truthIndex);
		SimpleMatrix error = softMax.minus(kronecker);
		outNode.setError(error);
	}

	private SimpleMatrix activateTree(TreeNeuron tree) {
		SimpleMatrix P = getMatrix(tree);
		
		SimpleMatrix z = P.plus(model.getB());
		for(TreeNeuron child : tree.getChildren()) {
			SimpleMatrix R_c = activateTree(child);
			z = z.plus(model.getW().mult(R_c));
		}
		tree.setZ(z);
		SimpleMatrix a = NeuralUtils.elementwiseApplyTanh(z);
		tree.setActivation(a);
		return a;
	}

	private SimpleMatrix getMatrix(TreeNeuron tree) {
		if(encodingMap.containsKey(tree)) {
			SimpleMatrix P = encodingMap.get(tree);
			int m = EncoderParams.getM();
			P.reshape(m, m);
			return P;
		} else {
			int m = EncoderParams.getM();
			return new SimpleMatrix(m, m);
		}
	}

	private void addWeightDecay() {
		//addDecayForDecoders();
		addDecayForW();
	}

	private void addDecayForW() {
		SimpleMatrix W = model.getW();
		SimpleMatrix dW = modelGrad.getW();
		dW = dW.plus(W.scale(EncoderParams.getWeightDecay()));
		modelGrad.setW(dW);

	}

	private void addDecayForDecoders() {
		for(int i = 0; i < dimension.getNumDecoders(); i++) {
			ValueDecoder decoder = model.getDecoder(i);
			ValueDecoder gradDecoder = modelGrad.getDecoder(i);
			SimpleMatrix dW = gradDecoder.getW();
			SimpleMatrix W = decoder.getW();
			dW = dW.plus(W.scale(EncoderParams.getWeightDecay()));
			gradDecoder.setW(dW);
		}
	}

	public double getSumError() {
		return sumError;
	}

}

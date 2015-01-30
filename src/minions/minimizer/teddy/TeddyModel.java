package minions.minimizer.teddy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.encoder.EncoderParams;
import models.encoder.decoders.SoftmaxDecoder;
import models.encoder.decoders.ValueDecoder;
import models.encoder.neurons.TreeNeuron;

import org.ejml.simple.SimpleMatrix;

import util.NeuralUtils;
import util.Warnings;

public class TeddyModel {

	List<SoftmaxDecoder> decoders;
	SimpleMatrix W;
	SimpleMatrix b;

	public TeddyModel(List<SoftmaxDecoder> decoders2, SimpleMatrix w2,
			SimpleMatrix b) {
		this.decoders = decoders2;
		this.W = w2;
		this.b = b;
	}

	public List<SoftmaxDecoder> getDecoders() {
		return decoders;
	}

	public SimpleMatrix getW() {
		return W;
	}

	public SimpleMatrix getB() {
		return b;
	}

	public void scale(double d) {
		// TODO Auto-generated method stub

	}

	public ValueDecoder getDecoder(int i) {
		return decoders.get(i);
	}

	public void setW(SimpleMatrix W) {
		this.W = W;
	}

	public void setB(SimpleMatrix newB) {
		this.b = newB;
	}

	private SimpleMatrix activateTree(TreeNeuron tree, 
			Map<TreeNeuron, SimpleMatrix> matrixMap) {
		SimpleMatrix P = getMatrix(tree, matrixMap);

		SimpleMatrix z = P.plus(b);
		for(TreeNeuron child : tree.getChildren()) {
			SimpleMatrix R_c = activateTree(child, matrixMap);
			z = z.plus(W.mult(R_c));
		}
		tree.setZ(z);
		SimpleMatrix a = NeuralUtils.elementwiseApplyTanh(z);
		tree.setActivation(a);
		return a;
	}

	private SimpleMatrix getMatrix(TreeNeuron tree, 
			Map<TreeNeuron, SimpleMatrix> matrixMap) {
		if(matrixMap.containsKey(tree)) {
			SimpleMatrix P = matrixMap.get(tree);
			int m = EncoderParams.getM();
			P.reshape(m, m);
			return P;
		} else {
			int m = EncoderParams.getM();
			return new SimpleMatrix(m, m);
		}
	}
	
	private static Map<String, SimpleMatrix> cache = 
			new HashMap<String, SimpleMatrix>();

	public List<Boolean> predict(
			String id,
			TreeNeuron tree,
			Map<TreeNeuron, SimpleMatrix> matrixMap, 
			double threshold) {
		if(!cache.containsKey(id)) {
			TreeNeuron copy = new TreeNeuron(tree);
			SimpleMatrix root = activateTree(copy, matrixMap);
			cache.put(id, root);
		}
		
		SimpleMatrix root = cache.get(id);
		int m = EncoderParams.getM();
		Warnings.check(root.getNumElements() == m*m);
		root.reshape(m * m, 1);

		List<Boolean> hat = new ArrayList<Boolean>();
		for(int i = 0; i < decoders.size(); i++) {
			int choiceIndex = decoders.get(i).decodeChoice(root, threshold);
			Warnings.check(choiceIndex == 0 || choiceIndex == 1);
			hat.add(choiceIndex == 1);
		}
		return hat;
	}


}

package run.forceMultiply.midpoint;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.ejml.simple.SimpleMatrix;

import minions.forceMult.FMMinion;
import minions.forceMult.FMRandomChoser;
import minions.minimizer.teddy.TeddyModel;
import models.encoder.neurons.TreeNeuron;

public class FMEncoderTeddy2 implements FMMinion {

	TeddyModel model;
	Map<TreeNeuron, SimpleMatrix> matrixMap;
	Map<String, TreeNeuron> programMap;
	FMRandomChoser choser;
	
	public FMEncoderTeddy2(
			TeddyModel m,
			Map<TreeNeuron, SimpleMatrix> matrixMap,
			Map<String, TreeNeuron> programMap) {
		this.model = m;
		this.matrixMap = matrixMap;
		this.programMap = programMap;
		this.choser = new FMRandomChoser(programMap.keySet(), 0);
	}

	@Override
	public void updateActiveLearning(String id, List<Boolean> feedback) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<Boolean> predict(String id) {
		throw new RuntimeException("test");
	}

	@Override
	public List<Boolean> predict(String id, double threshold) {
		TreeNeuron tree = programMap.get(id);
		return model.predict(id, tree, matrixMap, threshold);
	}

	@Override
	public void train(Map<String, List<Boolean>> gradedMap) {
		// TODO Auto-generated method stub

	}

	@Override
	public String choseNext(Collection<String> options) {
		return choser.choseNext();
	}

	@Override
	public void setBudget(int budget) {
		choser.setBudget(budget);
	}

}

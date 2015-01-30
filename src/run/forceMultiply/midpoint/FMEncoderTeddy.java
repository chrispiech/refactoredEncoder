package run.forceMultiply.midpoint;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.ejml.simple.SimpleMatrix;

import minions.forceMult.FMChoser;
import minions.forceMult.FMEncoder;
import minions.forceMult.FMMinion;
import minions.forceMult.FMRandomChoser;
import minions.minimizer.lemur.AdaGradThreadedLemur;
import minions.minimizer.teddy.AdaGradTeddy;
import models.encoder.neurons.TreeNeuron;

public class FMEncoderTeddy extends FMEncoder implements FMMinion {
	
	Map<TreeNeuron, SimpleMatrix> encodingMap;
	Map<String, TreeNeuron> programMap;
	FMChoser choser;
	String name;

	public FMEncoderTeddy(String name, Map<TreeNeuron, SimpleMatrix> encodingMap, 
			Map<String, TreeNeuron> programMap, int seed) {
		super();
		this.encodingMap = encodingMap;
		this.programMap = programMap;
		this.name = name;
		choser = new FMRandomChoser(programMap.keySet(), seed);
	}


	@Override
	public void updateActiveLearning(String id, List<Boolean> feedback) {
	}

	@Override
	public List<Boolean> predict(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Boolean> predict(String id, double threshold) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void train(Map<String, List<Boolean>> gradedMap) {
		System.out.println("this is where we fit...");
		// we are going to initialize a model
		// then we are going to fit it using adaGrad
		AdaGradTeddy.train(encodingMap, programMap, gradedMap, name);
	}

	@Override
	public String choseNext(Collection<String> options) {
		return choser.choseNext(this);
	}

	@Override
	public void setBudget(int budget) {
		choser.setBudget(budget);
	}

}

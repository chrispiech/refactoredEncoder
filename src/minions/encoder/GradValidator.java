package minions.encoder;

import java.util.*;

import minions.encoder.backprop.EncoderBackprop;
import minions.encoder.modelVector.ModelVector;
import models.code.TestTriplet;
import models.encoder.EncodeGraph;
import models.encoder.encoders.Encoder;

public class GradValidator {

	public static final double EPSILON = 1E-6;
	public static final double OK_DIFF = 1E-4;

	public static boolean validate(Encoder model, List<TestTriplet> list) {
		/*boolean valid = true;
		for(TestTriplet t : list) {
			if(!validate(model, t)) {
				valid = false;
				System.out.println("failed test");
				System.out.println(t.getEncodeGraph().getRunEncodeTreeClone());
			} else {
				System.out.println("passed test");
			}
		}
		return valid;*/
		return new GradValidator().run(model, list);
	}

	public static boolean validate(Encoder model, TestTriplet t) {
		throw new RuntimeException("depricated");
		//return new GradValidator().run(model, t);
	}

	private Encoder model;
	private List<TestTriplet> tests;

	private boolean run(Encoder model, List<TestTriplet> tests) {
		this.tests = tests;
		this.model = model;
		Encoder grad = EncoderBackprop.derivative(model, tests);
		double[] gradVec = ModelVector.modelToVec(grad);

		boolean passed = true;
		for(int i = 0; i < gradVec.length; i++) {
			double jPlus = getJPlus(i);
			double jMinus = getJMinus(i);
			double truth = finiteDifferenceGrad(jPlus, jMinus);
			//System.out.println(truth);
			double calculated = gradVec[i];
			if(!testDiff(truth, calculated, i)) {
				passed = false;
			}
		}

		return passed;
	}

	private double getJMinus(int i) {
		return getJMod(i, -EPSILON);
	}

	private double getJPlus(int i) {
		return getJMod(i, EPSILON);
	}

	private double getJMod(int index, double mod) {
		double[] x = ModelVector.modelToVec(model);
		x[index] += mod;
		Encoder prime = ModelVector.vecToModel(model.getFormat(), x);
		return ModelValueAt.valueAt(prime, tests);
	}

	private double finiteDifferenceGrad(double jPlus, double jMinus) {
		return (jPlus - jMinus) / (2 * EPSILON);
	}

	private boolean testDiff(double truth, double calculated, int elem) {
		double diff = Math.abs(truth - calculated);
		if(diff > OK_DIFF) {
			System.out.println("-------");
			System.out.println("DERIVATIVE WARNING!");
			//System.out.println("astId:  " + test.getId());
			System.out.println("matrix: " + ModelVector.getMatrixForIndex(model.getFormat(), elem));
			System.out.println("elem:   " + elem);
			System.out.println("calc:   " + calculated);
			System.out.println("truth:  " + truth);
			System.out.println("diff:   " + diff);
			if(truth != 0) {
				System.out.println("diffM: " + calculated / truth);
			}
			//EncodeGraph g = test.getEncodeGraph();
			//System.out.println(g.getRunEncodeTreeClone());
			System.out.println("-------");
			return false;
			//throw new RuntimeException("is this derivative correct? ");
		} 
		return true;
	}

}

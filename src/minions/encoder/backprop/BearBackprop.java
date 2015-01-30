package minions.encoder.backprop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import minions.encoder.factory.EncoderFactory;
import minions.parser.EncodeGraphParser;
import models.ast.Tree;
import models.code.State;
import models.code.TestTriplet;
import models.encoder.*;
import models.encoder.encoders.*;
import models.encoder.encoders.models.BearModel;
import models.encoder.encoders.programEncoder.ProgramEncoderVec;
import models.encoder.neurons.ValueNeuron;
import models.encoder.neurons.TreeNeuron;
import models.encoder.decoders.*;
import models.language.Language;

import org.ejml.simple.SimpleMatrix;

import util.MatrixUtil;
import util.NeuralUtils;
import util.Warnings;

public class BearBackprop {

	private BearModel model = null;
	private BearModel modelGrad = null;
	private ModelFormat format;

	public static Encoder derivative(BearModel model, List<TestTriplet> data) {
		return new BearBackprop().getDerivative(model, data);
	}

	private Encoder getDerivative(BearModel model, List<TestTriplet> data) {
		this.model = model;
		this.modelGrad = (BearModel)EncoderFactory.makeZero(model.getFormat());
		this.format = model.getFormat();
		for(TestTriplet test : data) {
			addGradForTest(test);
		}
		modelGrad.scale(1.0 / data.size());
		
		// this is equivalent to adding weight decay once per test and then 
		// scaling weight decay with the rest of the grad by 1/data.size
		addWeightDecay(); 
		return modelGrad;
	}

	private void addGradForTest(TestTriplet test) {
		EncodeGraph graph = test.getEncodeGraph();
		//Warnings.check(!graph.hasCycles());
		TreeNeuron runTree = graph.getRunEncodeTreeClone();

		// calculate the activation of all tree nodes
		ProgramEncoderVec programEncoder = model.getProgramEncoder();
		SimpleMatrix cv = programEncoder.activateTree(runTree).getVector();
		State truth = test.getPostcondition();

		for(String key : format.getStateKeys()){
			ValueDecoder outDecoder = model.getOutputDecoder(key);
			ValueNeuron outNode = outDecoder.outActivation(cv, key);

			// calculate the error terms of all neurons
			backpropError(runTree, outNode, truth, key);
			
			// add to the grad for all parameters
			addGrad(runTree, outNode);
		}
	}

	//*********************************************************************************
	//*                 ERROR TERM
	//*********************************************************************************

	private void backpropError(TreeNeuron runTree, ValueNeuron outNode, State truth, String key) {
		StateDecoderBackprop.outputError(model.getFormat(), outNode, truth, key);

		SimpleMatrix parentError = outNode.getError();
		ValueDecoder decoder = model.getOutputDecoder(key);
		SimpleMatrix parentW = decoder.getW();

		ProgramBackprop.treeError(model.getProgramEncoder(), runTree, parentError, parentW, 0);
	}

	//*********************************************************************************
	//*                 GRADIENT STEP
	//*********************************************************************************

	private void addGrad(TreeNeuron tree, ValueNeuron outNode) {

		// update the output encoder params...
		StateDecoderBackprop.gradientStepValue(modelGrad, tree.getActivation(), outNode);

		// then update all the tree params...
		ValueDecoder parent = model.getOutputDecoder(outNode.getKey());
		SimpleMatrix parentError = outNode.getError();
		SimpleMatrix parentW = parent.getW();
		ProgramEncoderVec programModel = model.getProgramEncoder();
		ProgramEncoderVec programGrad = modelGrad.getProgramEncoder();
		ProgramBackprop.gradientStepTree(programModel, programGrad, tree,
				parentError, parentW, 0);
	}

	//*********************************************************************************
	//*                 WEIGHT DECAY
	//*********************************************************************************
	private void addWeightDecay() {
		ProgramBackprop.addWeightDecay(
				model.getProgramEncoder(), 
				modelGrad.getProgramEncoder());
		
		StateDecoderBackprop.addWeightDecay(
				model.getStateDecoder(),
				modelGrad.getStateDecoder());
	}

}

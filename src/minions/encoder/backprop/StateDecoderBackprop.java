package minions.encoder.backprop;

import org.ejml.simple.SimpleMatrix;

import util.MatrixUtil;
import util.Warnings;
import models.code.State;
import models.encoder.EncoderParams;
import models.encoder.ModelFormat;
import models.encoder.decoders.ValueDecoder;
import models.encoder.decoders.SoftmaxDecoder;
import models.encoder.encoders.Encoder;
import models.encoder.encoders.StateDecoder;
import models.encoder.neurons.ValueNeuron;
import models.encoder.neurons.TreeNeuron;

public class StateDecoderBackprop {

	public static void outputError(
			ModelFormat format, ValueNeuron outNode, State truth, String key) {
		String outType = format.getOutputType(key);
		if(outType.equals("choice")) {
			softmaxError(format, outNode, truth, key);
		} else if(outType.equals("number")) {
			numberError(format, outNode, truth, key);
		} else if(outType.equals("matrix")) {
			matrixError(format, outNode, truth, key);
		} else {
			Warnings.error("no beans");
		}
	}
	
	public static void gradientStepValue(
			Encoder modelGrad, 
			SimpleMatrix childActivation, 
			ValueNeuron outNode) {
	
		SimpleMatrix error = outNode.getError();

		SimpleMatrix dW = error.mult(childActivation.transpose());
		SimpleMatrix dB = new SimpleMatrix(error);
		ValueDecoder outEncoder = modelGrad.getOutputDecoder(outNode.getKey());
		updateGradOut(outEncoder, dW, dB);
	}
	
	private static void softmaxError(
			ModelFormat format, ValueNeuron outNode, State truth, String key) {
		SimpleMatrix softMax = MatrixUtil.softmax(outNode.getZ());
		int truthIndex = SoftmaxDecoder.getTruthIndex(format.getLanguage(), truth, key);
		SimpleMatrix kronecker = MatrixUtil.basis(outNode.getSize(), truthIndex);
		SimpleMatrix error = softMax.minus(kronecker);
		outNode.setError(error);
	}
	
	private static void numberError(
			ModelFormat format, ValueNeuron outNode, State truth, String key) {
		SimpleMatrix z = outNode.getZ();
		Warnings.check(z.getNumElements() == 1);
		SimpleMatrix t = new SimpleMatrix(1, 1);
		t.set(0, truth.getNumber(key));
		SimpleMatrix mu = getRSquared(t, z);
		outNode.setError(mu);
	}
	
	private static void matrixError(
			ModelFormat format, ValueNeuron outNode, State truth, String key) {
		SimpleMatrix z = outNode.getZ();
		SimpleMatrix t = truth.getMatrixVector(key);
		SimpleMatrix mu = getRSquared(t, z);
		outNode.setError(mu);
	}
	
	private static void updateGradOut(
			ValueDecoder outEncoder, SimpleMatrix dW, SimpleMatrix dB) {
		SimpleMatrix newW = outEncoder.getW().plus(dW);
		SimpleMatrix newB = outEncoder.getB().plus(dB);
		outEncoder.setParameters(newW, newB);
	}
	
	private static SimpleMatrix getRSquared(SimpleMatrix t, SimpleMatrix z) {
		return  (z.minus(t)).scale(2);
	}

	public static void addWeightDecay(StateDecoder decoder,
			StateDecoder decoderGrad) {
		for(String key : decoder.getStateKeys()) {
			ValueDecoder modelEncoder = decoder.getOutputDecoder(key);
			ValueDecoder gradEncoder = decoderGrad.getOutputDecoder(key);
			SimpleMatrix dW = gradEncoder.getW();
			SimpleMatrix W = modelEncoder.getW();
			dW = dW.plus(W.scale(EncoderParams.getWeightDecay()));
			gradEncoder.setW(dW);
		}
		
	}

}

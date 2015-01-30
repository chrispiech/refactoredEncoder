package models.encoder.encoders.programEncoder;

import models.encoder.ModelFormat;
import models.encoder.encoders.InternalEncoder;
import models.encoder.neurons.TreeNeuron;

import org.ejml.simple.SimpleMatrix;

public interface ProgramEncoder {

	// get internal encoders
	InternalEncoder getInternalEncoder(TreeNeuron tree);
	
	InternalEncoder getInternalEncoder(String type);

	// calculate embeddings
	SimpleMatrix getLeafEmbedding(String type);

	SimpleMatrix activateTree(TreeNeuron runTree);

	void setLeafEmbedding(String type, SimpleMatrix leaf);

	ModelFormat getFormat();

	

}

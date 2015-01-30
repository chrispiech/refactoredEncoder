package models.code;

import java.util.*;

import models.encoder.ModelFormat;
import models.encoder.decoders.SoftmaxDecoder;
import models.language.Language;

import org.ejml.simple.SimpleMatrix;

import util.MatrixUtil;

public interface State {

	public SimpleMatrix getActivation(ModelFormat format, String key);

	public String getChoice(String key);
	
	public int getNumber(String key);
	
	public SimpleMatrix getMatrix(String key);
	
	public SimpleMatrix getMatrixVector(String key);
	
	public SimpleMatrix getMatrix(ModelFormat format);
	
}

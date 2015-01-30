package models.encoder;

import java.util.List;

import minions.minimizer.teddy.TeddyDimension;
import models.encoder.dimension.BearDimension;
import models.encoder.dimension.BeeDimension;
import models.encoder.dimension.DeepBeeDimension;
import models.encoder.dimension.Dimension;
import models.encoder.dimension.LemurDimension;
import models.encoder.dimension.MonkeyDimension;
import models.encoder.dimension.PenguinDimension;
import models.encoder.dimension.TurtleDimension;
import models.language.BlockyLanguage;
import models.language.KarelLanguage2;
import models.language.Language;
import models.language.ToyLanguage;

import org.apache.commons.math3.util.Pair;

public class ModelFormat {

	private Language languageFormat = null;
	public Dimension dimension = null;
	private String modelType;
	private String languageType;

	public ModelFormat(String language, String modelType) {
		this.modelType = modelType;
		this.languageType = language;

		if(language.equals("blocky")) {
			languageFormat = new BlockyLanguage();
		} else if(language.equals("karel")) {
			languageFormat = new KarelLanguage2();
		} else if(language.equals("toy")){
			languageFormat = new ToyLanguage();
		} else {
			throw new RuntimeException("wot");
		}

		if(modelType.equals("monkey")) {
			dimension = new MonkeyDimension(languageFormat); 
		} else if(modelType.equals("bear")) {
			dimension = new BearDimension(languageFormat);
		} else if(modelType.equals("bee")) {
			dimension = new BeeDimension(languageFormat);
		} else if(modelType.equals("chimp")) {
			dimension = new MonkeyDimension(languageFormat);
		} else if(modelType.equals("penguin")) {
			dimension = new PenguinDimension(languageFormat);
		} else if(modelType.equals("lemur")) {
			dimension = new LemurDimension(languageFormat);
		} else if(modelType.equals("turtle")) {
			dimension = new TurtleDimension(languageFormat);
		} else if(modelType.equals("deepbee")) {
			dimension = new DeepBeeDimension(languageFormat);
		} else if(modelType.equals("teddy")) {
			dimension = new TeddyDimension(10);
		} else {
			throw new RuntimeException("wot");
		}
	}

	public int getNumParams() {
		return dimension.getDimension();
	}

	public Dimension getDimension() {
		return dimension;
	}

	public int getStateEncoderDimension() {
		return dimension.getStateEncoderDimension();
	}

	public int getStateDecoderDimension() {
		return dimension.getStateDecoderDimension();
	}

	public int getStateDecoderDimension(String key) {
		return dimension.getStateDecoderDimension(key);
	}

	public int getLeafDimension() {
		return dimension.getLeafDimension();
	}



	public int getProgramEncoderDimension() {
		return getInternalDimension() + getLeafDimension();
	}

	public int getInternalDimension() {
		return dimension.getInternalDimension();
	}

	public int getInternalEncoderDimension(String type) {
		return dimension.getInternalEncoderDimension(type);
	}





	// outputs
	public int getNumOutputs() {
		return languageFormat.getNumOutputs();
	}

	// encoders
	public List<String> getInternalEncoderTypes() {
		return languageFormat.getInternalEncoderTypes();
	}

	public int getArity(String type) {
		return languageFormat.getArity(type);
	}

	public List<String> getLeafTypes() {
		return languageFormat.getLeafTypes();
	}

	public String getEncoderType(String nodeType) {
		return languageFormat.getEncoderType(nodeType);
	}

	public List<String> getStateKeys() {
		return languageFormat.getStateKeys();
	}

	public boolean isBear() {
		return modelType.equals("bear");
	}

	public boolean isMonkey() {
		return modelType.equals("monkey");
	}

	public boolean isChimp() {
		return modelType.equals("chimp");
	}

	public boolean isTurtle() {
		return modelType.equals("turtle");
	}

	public boolean isBee() {
		return modelType.equals("bee");
	}

	public boolean isPenguin() {
		return modelType.equals("penguin");
	}

	public boolean isLemur() {
		return modelType.equals("lemur");
	}

	public boolean isDeepBee() {
		return modelType.equals("deepbee");
	}

	public String getModelType() {
		return modelType;
	}

	public String getLanguageName() {
		return languageType;
	}

	public Language getLanguage() {
		return languageFormat;
	}

	public int getNumOutputOptions(String key) {
		return languageFormat.getNumOutputOptions(key);
	}

	public String getOutputType(String key) {
		return languageFormat.getOutputType(key);
	}

	@Override
	public boolean equals(Object o) {
		ModelFormat other = (ModelFormat)o;
		if(!languageType.equals(other.languageType)) return false;
		return modelType.equals(other.modelType);
	}

	public Pair<Integer, Integer> getMatrixDim(String key) {
		return dimension.getMatrixDim(key);
	}

	public int getTypeVectorSize(String key) {
		return dimension.getTypeVectorSize(key);
	}

	public String[] getChoiceOptions(String key) {
		return languageFormat.getChoiceOptions(key);
	}

}

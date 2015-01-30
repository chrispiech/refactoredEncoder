package minions.minimizer.teddy;

import java.util.List;

import models.language.Language;

public class TeddyLanguage implements Language {

	@Override
	public List<String> getLeafTypes() {
		throw new RuntimeException("no");
	}

	@Override
	public int getArity(String type) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getEncoderType(String nodeType) {
		throw new RuntimeException("no");
	}

	@Override
	public List<String> getInternalEncoderTypes() {
		throw new RuntimeException("no");
	}

	@Override
	public int getNumOutputs() {
		throw new RuntimeException("no");
	}

	@Override
	public List<String> getStateKeys() {
		throw new RuntimeException("no");
	}

	@Override
	public int getNumOutputOptions(String key) {
		return 2;
	}

	@Override
	public String getOutputType(String key) {
		throw new RuntimeException("no");
	}

	@Override
	public String[] getChoiceOptions(String key) {
		throw new RuntimeException("no");
	}

	@Override
	public boolean isBlockType(String type) {
		throw new RuntimeException("no");
	}

	@Override
	public boolean isMethodInvocation(String type) {
		throw new RuntimeException("no");
	}

	@Override
	public String getName() {
		throw new RuntimeException("no");
	}

	@Override
	public boolean isKarel() {
		throw new RuntimeException("no");
	}

	@Override
	public boolean isBlocky() {
		throw new RuntimeException("no");
	}

}

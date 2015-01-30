package models.language;

import java.util.*;

import models.encoder.EncoderParams;
import util.Warnings;

public class ToyLanguage extends KarelLanguage2 implements Language{

	protected static final String[] KAREL_LEAVES = {
		"snap",
	};
	
	@Override
	public List<String> getLeafTypes() {
		return new ArrayList<String>(Arrays.asList(KAREL_LEAVES));
	}
	
	@Override
	public List<String> getInternalEncoderTypes() {
		return new ArrayList<String>();
	}

	


}

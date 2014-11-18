package minions.forceMult;

import java.util.*;

import models.encoder.CodeVector;
import models.math.LogRegression;

import org.ejml.simple.SimpleMatrix;

import util.MatrixUtil;

public class FMEncoderRandom extends FMEncoder{

	public FMEncoderRandom(TreeMap<String, CodeVector> encodingMap) {
		super(encodingMap);
		this.choser = new FMRandomChoser(encodingMap.keySet());
	}



}

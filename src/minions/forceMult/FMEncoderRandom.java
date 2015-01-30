package minions.forceMult;

import java.util.*;

import models.encoder.ClusterableMatrix;
import models.math.LogRegression;

import org.ejml.simple.SimpleMatrix;

import util.MatrixUtil;

public class FMEncoderRandom extends FMEncoder{

	public FMEncoderRandom(TreeMap<String, SimpleMatrix> encodingMap, int seed) {
		super(encodingMap);
		this.choser = new FMRandomChoser(encodingMap.keySet(), seed);
	}



}

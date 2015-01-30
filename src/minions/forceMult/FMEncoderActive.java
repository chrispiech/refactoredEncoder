package minions.forceMult;

import java.util.*;

import models.encoder.ClusterableMatrix;
import models.encoder.EncoderParams;
import models.math.LogRegression;

import org.ejml.simple.SimpleMatrix;

import util.MatrixUtil;

public class FMEncoderActive extends FMEncoder {

	public FMEncoderActive(TreeMap<String, SimpleMatrix> encodingMap) {
		super(encodingMap);
		this.choser = new FMCluster(encodingMap);
	}
}

package minions.forceMult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import models.encoder.CodeVector;
import models.encoder.EncoderParams;
import models.math.LogRegression;

import org.ejml.simple.SimpleMatrix;

import util.MatrixUtil;

public class FMEncoder implements FMMinion, CodeVectorLogistic{
	protected FMChoser choser = null;
	private List<LogRegression> classifiers = null;
	private Map<String, CodeVector> encodingMap = null;
	private Map<String, List<Boolean>> gradedMap = null;

	private int numFeedbacks = 0;

	public FMEncoder(TreeMap<String, CodeVector> encodingMap) {
		this.encodingMap = encodingMap;
	}

	@Override
	public String choseNext(Collection<String> options) {
		return choser.choseNext(this);
	}

	@Override
	public void updateActiveLearning(String id, List<Boolean> feedback) {
		numFeedbacks = feedback.size();
		choser.update(id, feedback);
	}

	@Override
	public List<Boolean> predict(String id) {
		return predict(id, 0.5);
	}

	@Override
	public List<Boolean> predict(String id, double theta) {
		SimpleMatrix features = getFeatures(id);
		List<Boolean> feedback = new ArrayList<Boolean>();
		for(int i = 0; i < numFeedbacks; i++) {
			LogRegression classifier = classifiers.get(i);
			feedback.add(classifier.predict(features, theta) == 1);
		}
		return feedback;
	}

	@Override
	public void train(Map<String, List<Boolean>> gradedMap) {
		this.gradedMap = gradedMap;
		classifiers = new ArrayList<LogRegression>();
		for(int i = 0; i < numFeedbacks; i++) {
			SimpleMatrix features = getFeatureMatrix();
			SimpleMatrix labels = getLabelMatrix(i);
			LogRegression c = new LogRegression(i + "");
			c.train(features, labels);
			classifiers.add(c);
		}
	}
	
	@Override
	public void setBudget(int budget) {
		choser.setBudget(budget);
	}
	
	@Override
	public int getNumPredictions() {
		return numFeedbacks;
	}
	
	public int getNumFeedbacks() {
		return numFeedbacks;
	}

	public LogRegression getClassifier(int i) {
		return classifiers.get(i);
	}
	
	public SimpleMatrix getFeatures(String id) {
		CodeVector cv = encodingMap.get(id);
		return cv.getVector().transpose();
	}

	private SimpleMatrix getLabelMatrix(int index) {
		SimpleMatrix labelMatrix = new SimpleMatrix(gradedMap.size(), 1);
		int i = 0;
		for(String id : gradedMap.keySet()) {
			List<Boolean> labels = gradedMap.get(id);
			int label = labels.get(index) ? 1 : 0;
			labelMatrix.set(i, label);
			i++;
		}
		return labelMatrix;
	}

	private SimpleMatrix getFeatureMatrix() {
		int rows = gradedMap.size();
		int cols = EncoderParams.getCodeVectorSize();
		SimpleMatrix featureMatrix = new SimpleMatrix(rows, cols);
		int r = 0;
		for(String id : gradedMap.keySet()) {
			SimpleMatrix features = getFeatures(id);
			MatrixUtil.setRow(featureMatrix, r, features);
			r++;
		}
		return featureMatrix;
	}

	@Override
	public void test() {
		throw new RuntimeException("test");
	}
}

package minions.forceMult;

import java.util.List;
import java.util.Map;

import models.math.LogRegression;

public interface CodeVectorLogistic {

	public int getNumPredictions();

	public LogRegression getClassifier(int i);

	public void train(Map<String, List<Boolean>> gradedMap);

	public void test();

}

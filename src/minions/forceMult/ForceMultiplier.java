package minions.forceMult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.ejml.simple.SimpleMatrix;

import edu.stanford.nlp.util.Pair;

public class ForceMultiplier {
	
	private static final double ACC_THRESHOLD = 0.9;

	private FMMinion minion;
	private Map<String, List<Integer>> feedbackMap;
	private Map<String, List<Boolean>> gradedMap;
	private Collection<String> toGrade;
	private int numFeedbacks;

	public ForceMultiplier(
			FMMinion minion, 
			Map<String, List<Integer>> feedbackMap,
			Collection<String> toGrade) {
		this.minion = minion;
		this.feedbackMap = feedbackMap;
		this.numFeedbacks = 0;
		this.toGrade = toGrade;
		this.gradedMap = new TreeMap<String, List<Boolean>>();
		for(String id : feedbackMap.keySet()) {
			for(int feedback : feedbackMap.get(id)) {
				if(feedback > numFeedbacks) {
					numFeedbacks = feedback;
				}
			}
		}
	}

	/**
	 * Method: Run
	 * -----------
	 * Force multiply teacher grading, given a budget. Report how well
	 * you do.
	 */
	public void run(int budget) {
		minion.setBudget(budget);
		
		// populate datasets...
		getFeedback(budget);

		// fit logistic regression model for each output...
		fitClassifiers();

		// lets try to make a PRCurve...
		List<Pair<Double, Double>> incorrectCorrect = makeICCurve();

		// lets get a best score
		//double bestScore = getScore(incorrectCorrect);
		double numProp = argMaxScore(incorrectCorrect);
		//System.out.println("best score: " + bestScore);
		int thresold = (int) (ACC_THRESHOLD * 100);
		System.out.println("num propagated at " + thresold + "% " + numProp);
	}

	private double argMaxScore(List<Pair<Double, Double>> icList) {
		double bestScore = 0;
		for(Pair<Double, Double> incorrectCorrect : icList) {
			double incorrect = incorrectCorrect.first();
			double correct = incorrectCorrect.second();
			double accuracy = correct / (correct + incorrect);
			double score = correct + incorrect;
			if(accuracy > ACC_THRESHOLD && score > bestScore) {
				bestScore = score;
			}
		}
		return bestScore;
	}

	/**
	 * Method: Get Feedback
	 * --------------------
	 * Simulate the process of grading from a large set of submissions.
	 * You have budget number of times that a grader can provide feedback,
	 * and you can actively chose which item to grade.
	 */
	private void getFeedback(int budget) {
		for(int i = 0; i < budget; i++) {
			String chosenId = minion.choseNext(toGrade);
			System.out.println("chosen: " + chosenId);
			List<Boolean> feedback = grade(chosenId);
			gradedMap.put(chosenId, feedback);
			minion.updateActiveLearning(chosenId, feedback);
		}
	}

	/**
	 * Method: Grade
	 * -------------
	 * Simulate grading a given astId
	 */
	private List<Boolean> grade(String chosenId) {
		List<Integer> feedback = feedbackMap.get(chosenId);
		List<Boolean> labels = new ArrayList<Boolean>();
		for(int j = 0; j < numFeedbacks; j++) {
			boolean applies = feedback.contains(j);
			labels.add(applies);
		}
		return labels;
	}

	/**
	 * Method: Fit Classifiers
	 * -----------------------
	 * After simulating grading, fit the optimal classifier so that you can
	 * predict feedback for ungraded work.
	 */
	private void fitClassifiers() {
		minion.train(gradedMap);
	}

	/**
	 * Method: Make Correct Incorrect Curve
	 * ------------------------------------
	 * Test how well the active learner is by creating a correct, incorrect
	 * curve. For a threshold parameter (theta) in the range of 0 to 1, 
	 * change your classifier to make more predictions.
	 */
	private List<Pair<Double, Double>> makeICCurve() {
		List<Pair<Double, Double>> prCurve = new ArrayList<Pair<Double, Double>>();
		System.out.println("precision recall");
		System.out.println("incorrect, correct");
		System.out.println("-----");
		for(double threshold = 0.80; threshold <= 1.0; threshold += 0.001) {
			Pair<Double, Double> ic = getIncorrectCorrect(threshold);
			prCurve.add(ic);
			double inc = ic.first();
			double cor = ic.second();
			double acc = cor / (inc + cor);
			System.out.println(inc + "\t" + cor + "\t" + acc);
		}
		int bCorrect = getBaselineCorrect();
		Pair<Double, Double> baseline = new Pair<Double, Double>(0., (double)bCorrect);
		System.out.println(baseline.first() + "\t" + baseline.second());
		prCurve.add(baseline);
		return prCurve;
	}

	/**
	 * Method: Get Baseline Correct
	 * ----------------------------
	 * Get the number of correct feedbacks given after grading, assuming no
	 * force multiplied feedback.
	 */
	private int getBaselineCorrect() {
		int correct = 0;
		for(String id : toGrade) {
			if(gradedMap.containsKey(id)) {
				List<Boolean> feedback = gradedMap.get(id);
				for(boolean f : feedback) {
					if(f) correct++;
				}
			}
		}
		return correct;
	}

	/**
	 * Method: Get Incorrect Correct
	 * -----------------------------
	 * For a given threshold in [0, 1] count the number of times feedback
	 * was given correctly (to a program that fit the feedback) and 
	 * incorrectly (to a program that didn't fit the feedback).
	 */
	private Pair<Double, Double> getIncorrectCorrect(double theta) {
		int correctFeedback = 0;
		int incorrectFeedback = 0;

		for(String id : toGrade) {
			List<Boolean> labels = grade(id);
			List<Boolean> prediction = predict(id, theta);
			for(int i = 0; i < labels.size(); i++) {
				boolean trueLabel = labels.get(i);
				boolean predicted = prediction.get(i);
				if(predicted && trueLabel) correctFeedback++;
				if(predicted && !trueLabel) incorrectFeedback++;
			}
		}

		Double incorrect = ((double)incorrectFeedback);
		Double correct = ((double)correctFeedback);

		return new Pair<Double, Double>(incorrect, correct);
	}

	/**
	 * Method: Predict
	 * ---------------
	 * For an astId, an index in the feedback list and a threshold,
	 * decide if you should give the feedback. Note that if the astId
	 * was in the set chosen for training, the feedback is known.
	 */
	private List<Boolean> predict(String id,  double threshold) {
		if(gradedMap.containsKey(id)) {
			return gradedMap.get(id);
		} else {
			return minion.predict(id, threshold);
		}
	}

	/**
	 * Method: Get Score
	 * ----------------
	 * Chose the correct/incorrect combination that maximizes the "score"
	 * function. The score function is meant to trade off the fact that it
	 * is much worse to give bad feedback, than to give no feedback.
	 */
	private double getScore(List<Pair<Double, Double>> icList) {
		double bestScore = 0;
		for(Pair<Double, Double> incorrectCorrect : icList) {
			double incorrect = incorrectCorrect.first();
			double correct = incorrectCorrect.second();
			double score = correct - 5 * incorrect;
			if(score > bestScore) bestScore = score;
		}
		return bestScore;
	}

}

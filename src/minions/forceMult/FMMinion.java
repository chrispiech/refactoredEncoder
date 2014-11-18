package minions.forceMult;

import java.util.*;

public interface FMMinion {
	
	public void updateActiveLearning(String id, List<Boolean> feedback);
	
	public List<Boolean> predict(String id);
	
	public List<Boolean> predict(String id, double threshold);
	
	public void train(Map<String, List<Boolean>> gradedMap);

	String choseNext(Collection<String> options);

	public void setBudget(int budget);

}

package minions.forceMult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public interface FMChoser {

	public void update(String id, List<Boolean> feedback);

	public String choseNext(FMEncoder fmEncoder);
	
	public void setBudget(int budget);
}

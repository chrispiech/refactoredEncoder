package minions.forceMult;

import java.util.*;

public class FMRandomChoser implements FMChoser {
	
	private List<String> chosen;
	private Random choiceRandom;
	private List<String> options;
	private int budget;

	public FMRandomChoser(Set<String> options, int seed) {
		choiceRandom = new Random(seed);
		this.options = new ArrayList<String>();
		this.options.addAll(options);
	}

	@Override
	public String choseNext(FMEncoder encoder) {
		return choseNext();
	}
	
	public String choseNext() {
		if(chosen == null) {
			chose();
		}
		return chosen.remove(0);
	}


	private void chose() {
		chosen = new ArrayList<String>();
		for(int i = 0; i < budget; i++) {
			int index = choiceRandom.nextInt(options.size());
			String next = options.remove(index);
			chosen.add(next);
		}
	}

	@Override
	public void setBudget(int budget) {
		this.budget = budget;
	}


	@Override
	public void update(String id, List<Boolean> feedback) {
		// TODO Auto-generated method stub
		
	}
	
}

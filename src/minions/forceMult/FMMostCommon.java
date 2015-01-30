package minions.forceMult;

import java.util.*;

import models.encoder.ClusterableMatrix;

public class FMMostCommon {
	
	private LinkedList<String> gradeList = new LinkedList<String>();

	public FMMostCommon(TreeMap<String, ClusterableMatrix> encodingMap) {
		ArrayList<Integer> intIdList = new ArrayList<Integer>();
		for(String id : encodingMap.keySet()) {
			intIdList.add(Integer.parseInt(id));
		}
		Collections.sort(intIdList);
		
		for(int id : intIdList) {
			gradeList.add("" + id);
		}
	}

	public String choseNext(FMEncoderActive fmEncoderActive) {
		String chosen = gradeList.removeFirst();
		System.out.println("chosen: " + chosen);
		return chosen;
	}

	public void update(String id, List<Boolean> feedback) {
		// do nothing...
	}

}

package util;

import java.util.*;

import org.ejml.simple.SimpleMatrix;

public class Silhouette {
	
	private int n;
	private SimpleMatrix dist;
	private Map<Integer, Integer> clusterMap;
	private Map<Integer, List<Integer>> reverseClusterMap;

	/**
	 * Dist is assumed to be a bottom-left triangle matrix.
	 */
	public static double getScore(SimpleMatrix dist, Map<Integer, Integer> clusters) {
		return new Silhouette().calc(dist, clusters);
	}

	private double calc(SimpleMatrix dist, Map<Integer, Integer> clusters) {
		this.n = clusters.size();
		this.dist = dist;
		this.clusterMap = clusters;
		this.reverseClusterMap = getReverseClusterMap();
		validateClusters();
		double sum = 0;
		for(int i = 0; i < n; i++) {
			double a = getA(i);
			double b = getB(i);
			double s = (b - a) / Math.max(a, b);
			sum += s;
		}
		double silhouetteScore = sum / n;
		return silhouetteScore;
	}

	private void validateClusters() {
		for(int cluster : reverseClusterMap.keySet()) {
			int size = getClusterSize(cluster);
			if(size <= 1) {
				throw new RuntimeException("no clusters of size 1!");
			}
		}
	}

	private double getA(int index) {
		int cluster = clusterMap.get(index);
		return getClusterDist(cluster, index);
	}
	
	private double getB(int i) {
		Double minDist = null;
		int trueCluster = clusterMap.get(i);
		for(int cluster : reverseClusterMap.keySet()){
			if(cluster == trueCluster) continue;
			double clusterDist = getClusterDist(cluster, i);
			if(minDist == null || clusterDist < minDist) {
				minDist = clusterDist;
			}
		}
		return minDist;
	}

	private double getClusterDist(int cluster, int i) {
		int numOthers = getClusterSize(cluster);
		double sum = 0;
		for(int otherIndex : reverseClusterMap.get(cluster)) {
			if(otherIndex == i) continue;
			double d = getDist(otherIndex, i);
			sum += d;
		}
		return sum / numOthers;
	}

	private int getClusterSize(int cluster) {
		return reverseClusterMap.get(cluster).size();
	}

	private double getDist(int otherIndex, int index) {
		int row = Math.max(otherIndex, index);
		int col = Math.min(otherIndex, index);
		return dist.get(row, col);
	}

	private Map<Integer, List<Integer>> getReverseClusterMap() {
		Map<Integer, List<Integer>> map = new HashMap<Integer, List<Integer>>();
		for(int i = 0; i < n; i++) {
			int cluster = clusterMap.get(i);
			if(!map.containsKey(cluster)) {
				map.put(cluster, new ArrayList<Integer>());
			}
			map.get(cluster).add(i);
		}
		return map;
	}
	
	public static void main(String[] args) {
		SimpleMatrix dist = MatrixUtil.randomMatrix(4, 4, 1);
		dist = dist.elementMult(dist);
		//dist.set(1, 0, 0.0001);
		//dist.set(3, 2, 0.0001);
		System.out.println(dist);
		Map<Integer, Integer> clusters = new HashMap<Integer, Integer>();
		clusters.put(0, 0);
		clusters.put(1, 0);
		clusters.put(2, 1);
		clusters.put(3, 1);
		double silhouette = getScore(dist, clusters);

		System.out.println("-----");
		System.out.println(silhouette);
	}
}

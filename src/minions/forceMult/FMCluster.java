package minions.forceMult;

import java.util.*;

import models.encoder.CodeVector;

import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.ejml.simple.SimpleMatrix;

import util.PQueue;

public class FMCluster implements FMChoser{

	private int K = -1;
	private int MAX_ITERATIONS = 10;

	private Queue<String> toGrade = new LinkedList<String>();

	private Map<CodeVector, String> reverseEncoding = null;

	public FMCluster(Map<String, CodeVector> encodingMap) {
		reverseEncoding = new HashMap<CodeVector, String>();
		for(String id : encodingMap.keySet()) {
			reverseEncoding.put(encodingMap.get(id), id);
		}
	}

	public void update(String id, List<Boolean> feedback) {
		// ignore
	}

	public String choseNext(FMEncoder encoder) {
		if(toGrade.isEmpty()) {
			makeBatch();
		}
		return toGrade.remove();
	}

	public void setBudget(int budget) {
		K = budget;
	}

	private void makeBatch() {
		System.out.println("clustering...");
		List<CodeVector> points = new ArrayList<CodeVector>();
		points.addAll(reverseEncoding.keySet());
		List<CentroidCluster<CodeVector>> centroids = cluster(points);
		for(CentroidCluster<CodeVector> centroid : centroids) {
			List<CodeVector> cluster = centroid.getPoints();
			Clusterable center = centroid.getCenter();
			String id = getMedioidId(center, cluster);
			toGrade.add(id);
		}
		System.out.println("done clustering.");
	}

	private List<CentroidCluster<CodeVector>> cluster(List<CodeVector> points) {
		int randomRestarts = 10;
		List<CentroidCluster<CodeVector>> best = null;
		double minLoss = 0;
		for(int i = 0; i < randomRestarts; i++) {
			System.out.println("k-means restart: " + i);
			KMeansPlusPlusClusterer<CodeVector> kMeans = 
					new KMeansPlusPlusClusterer<CodeVector>(K, 100);
			List<CentroidCluster<CodeVector>> centroids = kMeans.cluster(points);
			double loss = clusterLoss(points, centroids);
			if(best == null || loss < minLoss) {
				best = centroids;
				minLoss = loss;
			}
		}
		System.out.println("kmeans loss: " + minLoss);
		return best;
	}

	private double clusterLoss(
			List<CodeVector> points,
			List<CentroidCluster<CodeVector>> centroids) {
		double loss = 0;
		for(CodeVector v : points) {
			double dist = getL2Dist(v, centroids);
			loss += dist;
		}
		return loss;
	}

	private double getL2Dist(CodeVector v,
			List<CentroidCluster<CodeVector>> centroids) {
		Double min = null;
		for(CentroidCluster<CodeVector> c : centroids) {
			double d = distSquared(c.getCenter(), v);
			if(min == null || d < min) {
				min = d;
			}
		}
		return min;
	}

	private String getMedioidId(Clusterable center, List<CodeVector> cluster) {
		CodeVector argMin = null;
		double min = 0;
		for(CodeVector v : cluster) {
			double d = distSquared(center, v);
			if(argMin == null || d < min) {
				argMin = v;
				min = d;
			}
		}
		return reverseEncoding.get(argMin);
	}

	private double distSquared(Clusterable a, Clusterable b) {
		double[] pA = a.getPoint();
		double[] pB = b.getPoint();
		double d = 0;
		for(int i = 0; i < pA.length; i++) {
			double diff = pA[i] - pB[i];
			d += diff * diff;
		}
		return d;
	}

}

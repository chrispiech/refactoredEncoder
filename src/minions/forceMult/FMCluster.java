package minions.forceMult;

import java.util.*;

import models.encoder.ClusterableMatrix;
import models.math.PCA;

import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.ejml.simple.SimpleMatrix;

import util.MatrixUtil;
import util.PQueue;
import util.Warnings;

public class FMCluster implements FMChoser{

	private int K = -1;
	private int RANDOM_RESTARTS = 3;
	private int NUM_BASIS = 30;
	private int SUBSAMPLE = -1;

	private static final boolean USE_PCA = false;

	// actual instance variables
	private Queue<String> toGrade = new LinkedList<String>();
	private Map<ClusterableMatrix, String> reverseEncoding = null;

	public FMCluster(TreeMap<String, SimpleMatrix> encodingMap) {
		System.out.println("running pca...");

		PCA pca = null;
		if(USE_PCA) {
			pca = new PCA();
			long startTime = System.currentTimeMillis();
			pca.setup(encodingMap.size(), getSampleSize(encodingMap));
			for(String id : encodingMap.keySet()) {
				SimpleMatrix encoding = encodingMap.get(id);
				pca.addSample(MatrixUtil.asVector(encoding));
			}
			pca.computeBasis(NUM_BASIS);
			long endTime = System.currentTimeMillis();
			System.out.println("pca time: " + (endTime -startTime) + "ms");
		}

		reverseEncoding = new HashMap<ClusterableMatrix, String>();
		for(String id : encodingMap.keySet()) {
			SimpleMatrix encoding = encodingMap.get(id);
			if(USE_PCA) {
				double[] p = pca.sampleToEigenSpace(MatrixUtil.asVector(encoding));
				encoding = MatrixUtil.asSimpleMatrix(p);
			}
			reverseEncoding.put(new ClusterableMatrix(encoding), id);
		}

	}

	private int getSampleSize(TreeMap<String, SimpleMatrix> encodingMap) {
		String first = encodingMap.keySet().iterator().next();
		return encodingMap.get(first).getNumElements();
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
		List<ClusterableMatrix> points = new ArrayList<ClusterableMatrix>();
		points.addAll(reverseEncoding.keySet());
		List<CentroidCluster<ClusterableMatrix>> centroids = cluster(points);
		for(CentroidCluster<ClusterableMatrix> centroid : centroids) {
			List<ClusterableMatrix> cluster = centroid.getPoints();
			Clusterable center = centroid.getCenter();
			String id = getMedioidId(center, cluster);
			Warnings.check(id != null);
			toGrade.add(id);
		}

		System.out.println("done clustering.");
	}

	private List<CentroidCluster<ClusterableMatrix>> cluster(List<ClusterableMatrix> data) {

		List<ClusterableMatrix> points = new LinkedList<ClusterableMatrix>();

		if(SUBSAMPLE < 0) {
			points.addAll(data);
		} else {
			Collections.shuffle(data);
			for(int i = 0; i < Math.min(SUBSAMPLE, data.size()); i++) {
				points.add(data.get(i));
			}
		}

		List<CentroidCluster<ClusterableMatrix>> best = null;
		double minLoss = 0;
		for(int i = 0; i < RANDOM_RESTARTS; i++) {
			System.out.println("kmeans restart: " + i);
			long startTime = System.currentTimeMillis();
			KMeansPlusPlusClusterer<ClusterableMatrix> kMeans = 
					new KMeansPlusPlusClusterer<ClusterableMatrix>(K, 100);
			List<CentroidCluster<ClusterableMatrix>> centroids = kMeans.cluster(points);
			double loss = clusterLoss(points, centroids);
			if(best == null || loss < minLoss) {
				best = centroids;
				minLoss = loss;
			}
			long endTime = System.currentTimeMillis();
			System.out.println("kmeans time: " + (endTime -startTime) + "ms");

		}
		System.out.println("kmeans loss: " + minLoss);
		return best;
	}

	private double clusterLoss(
			List<ClusterableMatrix> points,
			List<CentroidCluster<ClusterableMatrix>> centroids) {
		double loss = 0;
		for(ClusterableMatrix v : points) {
			double dist = getL2Dist(v, centroids);
			loss += dist;
		}
		return loss;
	}

	private double getL2Dist(ClusterableMatrix v,
			List<CentroidCluster<ClusterableMatrix>> centroids) {
		Double min = null;
		for(CentroidCluster<ClusterableMatrix> c : centroids) {
			double d = distSquared(c.getCenter(), v);
			if(min == null || d < min) {
				min = d;
			}
		}
		return min;
	}

	private String getMedioidId(Clusterable center, List<ClusterableMatrix> cluster) {
		ClusterableMatrix argMin = null;
		double min = 0;
		for(ClusterableMatrix v : cluster) {
			double d = distSquared(center, v);
			if(argMin == null || d < min) {
				argMin = v;
				min = d;
			}
		}
		Warnings.check(argMin != null);
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

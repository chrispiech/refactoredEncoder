package util;

import java.util.Random;

public class RandomUtil {

	private static Random rg = new Random();
	
	public static double gauss(double mean, double std) {
		return rg.nextGaussian() * std + mean;
	}
	
	public static void setSeed(int seed) {
		rg.setSeed(seed);
	}
	
}

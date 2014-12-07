package util;

import java.util.Random;

public class RandomUtil {

	private static Random rg = new Random();

	public static double gauss(double mean, double std) {
		return rg.nextGaussian() * std + mean;
	}

	public static long nextLong(long n) {
		// error checking and 2^x checking removed for simplicity.
		long bits, val;
		do {
			bits = (rg.nextLong() << 1) >>> 1;
			val = bits % n;
		} while (bits-val+(n-1) < 0L);
		return val;
	}

}

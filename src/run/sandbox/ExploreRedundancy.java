package run.sandbox;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import minions.program.PrePostExperimentLoader;
import models.code.TestTriplet;
import models.encoder.EncoderParams;
import models.encoder.ModelFormat;
import models.language.KarelLanguage;
import models.language.Language;
import util.FileSystem;

public class ExploreRedundancy {

	public static void main(String[] args) {
		new ExploreRedundancy().run();
	}

	private void run() {
		FileSystem.setAssnId("Midpoint");
		Language l = new KarelLanguage();

		FileSystem.setExpId("prePostExp");
		System.out.println("load test...");
		List<TestTriplet>testSet = 
				PrePostExperimentLoader.loadSubset("test", 50000,l);

		System.out.println(testSet.size());
		
		Set<TestTriplet> uniqueSet = new HashSet<TestTriplet>();
		uniqueSet.addAll(testSet);
		System.out.println(uniqueSet.size());
		
	}

}

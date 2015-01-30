package run.explore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.ejml.simple.SimpleMatrix;

import minions.encoder.EncoderSaver;
import models.encoder.ClusterableMatrix;
import models.encoder.EncoderParams;
import models.encoder.encoders.Encoder;
import models.encoder.neurons.TreeNeuron;
import util.FileSystem;

public class VisualizeCodeMatrix {

	private void run() {
		FileSystem.setAssnId("Newspaper");
		FileSystem.setExpId("prePostExp");
		EncoderParams.worldRows = 5;
		EncoderParams.stateHasSize = false;
		Encoder model = EncoderSaver.load("gorilla-epoch10");
		
		Map<String, TreeNeuron> nMap = new TreeMap<String, TreeNeuron>();
		//nMap.put("ident_noop", getIdentityTree1());
		nMap.put("ident_leftRight", getIdentityTree2());
		nMap.put("ident_leftX4", getIdentityTree3());
		nMap.put("ident_rightLeft", getIdentityTree4());
		nMap.put("left", getTurnLeft1());
		nMap.put("right", getTurnRight1());
		nMap.put("right_leftX3", getTurnRight2());
		
		
		for(String key : nMap.keySet()) {
			TreeNeuron n = nMap.get(key);
			//System.out.println(n);
			SimpleMatrix cm = model.getProgramEncoder().activateTree(n);
			SimpleMatrix m = new SimpleMatrix(cm);
			m.reshape(10, 10);
			System.out.println(key);
			outputMatrix(m);
			System.out.println("\n\n");
		}
		
		
	}
	
	private void outputMatrix(SimpleMatrix m) {
		for(int r = 0; r < m.numRows(); r++) {
			String line = "";
			for(int c = 0; c < m.numCols(); c++) {
				line += String.format( "%.2f", m.get(r, c) );
				if(c != m.numCols() - 1) line += "\t";
			}
			System.out.println(line);
		}
	}

	private String getTurnLeftStr() {
		return "maze_turnLeft";
	}
	
	private String getTurnRightStr() {
		return "maze_turnRight";
	}
	
	private TreeNeuron getIdentityTree1() {
		return new TreeNeuron("noop", "0");
	}
	
	private TreeNeuron getIdentityTree2() {
		List<TreeNeuron> children = new ArrayList<TreeNeuron>();
		TreeNeuron a = new TreeNeuron(getTurnLeftStr(), "1");
		TreeNeuron b = new TreeNeuron(getTurnRightStr(), "2");
		children.add(a);
		children.add(b);
		return new TreeNeuron("block", children, "0");
	}
	
	private TreeNeuron getIdentityTree3() {
		TreeNeuron a = new TreeNeuron(getTurnLeftStr(), "1");
		TreeNeuron b = new TreeNeuron(getTurnLeftStr(), "2");
		TreeNeuron c = new TreeNeuron(getTurnLeftStr(), "3");
		TreeNeuron d = new TreeNeuron(getTurnLeftStr(), "4");
		
		List<TreeNeuron> children1 = new ArrayList<TreeNeuron>();
		children1.add(a);
		children1.add(b);
		TreeNeuron x = new TreeNeuron("block", children1, "5");
		
		List<TreeNeuron> children2 = new ArrayList<TreeNeuron>();
		children2.add(x);
		children2.add(c);
		TreeNeuron y = new TreeNeuron("block", children2, "6");
		
		List<TreeNeuron> children3 = new ArrayList<TreeNeuron>();
		children3.add(y);
		children3.add(d);
		
		return new TreeNeuron("block", children3, "0");
	}
	
	private TreeNeuron getIdentityTree4() {
		List<TreeNeuron> children = new ArrayList<TreeNeuron>();
		TreeNeuron a = new TreeNeuron(getTurnRightStr(), "2");
		TreeNeuron b = new TreeNeuron(getTurnLeftStr(), "1");
		children.add(a);
		children.add(b);
		return new TreeNeuron("block", children, "0");
	}
	
	private TreeNeuron getTurnLeft1() {
		return new TreeNeuron(getTurnLeftStr(), "0");
	}
	
	private TreeNeuron getTurnRight1() {
		return new TreeNeuron(getTurnRightStr(), "0");
	}
	
	private TreeNeuron getTurnRight2() {
		
		TreeNeuron a = new TreeNeuron(getTurnLeftStr(), "1");
		TreeNeuron b = new TreeNeuron(getTurnLeftStr(), "2");
		TreeNeuron c = new TreeNeuron(getTurnLeftStr(), "3");
		
		List<TreeNeuron> children1 = new ArrayList<TreeNeuron>();
		children1.add(a);
		children1.add(b);
		TreeNeuron x = new TreeNeuron("block", children1, "4");
		
		List<TreeNeuron> children2 = new ArrayList<TreeNeuron>();
		children2.add(x);
		children2.add(c);
		
		return new TreeNeuron("block", children2, "0");
	}

	public static void main(String[] args) {
		new VisualizeCodeMatrix().run();
	}

}

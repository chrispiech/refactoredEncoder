package models.code;


import java.io.File;
import java.util.*;

import minions.parser.EncodeGraphParser;
import minions.program.TreeJasonizer;
import minions.program.TreeLoader;
import models.ast.Tree;
import models.encoder.EncodeGraph;
import models.encoder.EncoderParams;

import org.ejml.simple.SimpleMatrix;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import util.FileSystem;
import util.MatrixUtil;

/**
 * Class: Test Triplet
 * --------------------
 * This class stores the triplet of
 *  1. precondition (blocky dynamic state)
 *  2. code-snippet (any blocky ast)
 *  3. postcondition (blocky dynamic state)
 * For the first experiment, startExp, the precondition
 * is always going to be the same (the dynamic variables of
 * the start state). Blocky state has four dynamic variables...
 * row, col, dir, and programState in that order.
 */
public class TestTriplet {

	private State precondition;
	private State postcondition;
	private EncodeGraph encodeGraph;
	private int count;
	private String astId;

	public TestTriplet(State pre, State post, EncodeGraph graph, String astId, int count) {
		precondition = pre;
		postcondition = post;
		encodeGraph = graph;
		this.astId = astId;
		this.count = count;
	}
	
	public void saveToFile(String expName, String dirName) {
		/*File expDir = new File(FileSystem.getDataDir(), expName);
		File tripletDir = new File(expDir, dirName);
		String fileName = astId + ".json";
		JSONObject tripletJson = new JSONObject();
		JSONObject programJson = TreeJasonizer.jsonify(ast);
		tripletJson.put("ast", programJson);
		//tripletJson.put("pre", getJsonArray(precondition));
		//tripletJson.put("post", getJsonArray(postcondition));
		tripletJson.put("astId", astId);
		tripletJson.put("count", count);
		String fileTxt = tripletJson.toString(3);
		FileSystem.createFile(tripletDir, fileName, fileTxt);*/
		throw new RuntimeException("todo");
	}

	public String toString() {
		String str = "";
		str += "precondition: " +precondition.toString() + "\n";
		str += encodeGraph.toString();
		str += "postcondition: " + postcondition.toString() + "\n";
		return str;
	}

	public State getPostcondition() {
		return postcondition;
	}

	public State getPrecondition() {
		return precondition;
	}

	public int getCount() {
		return count;
	}

	public String getId() {
		return astId;
	}
	
	public boolean equals(Object o) {
		TestTriplet other = (TestTriplet)o;
		if(!encodeGraph.equals(other.encodeGraph)) return false;
		if(!precondition.equals(other.precondition)) return false;
		if(!postcondition.equals(other.postcondition)) return false;
		return true;
	}
	
	@Override
	public int hashCode() {
		List<Object> objList = new LinkedList<Object>();
		objList.add(precondition);
		objList.add(postcondition);
		objList.add(encodeGraph);
		return objList.hashCode();
	}

	public EncodeGraph getEncodeGraph() {
		return encodeGraph;
	}

}

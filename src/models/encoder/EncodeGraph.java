package models.encoder;

import java.util.*;

import models.blocky.BlockyHelper;
import models.encoder.neurons.TreeNeuron;

import org.ejml.simple.SimpleMatrix;

import util.DirectedGraph;
import util.IdCounter;


public class EncodeGraph {
	
	private DirectedGraph<String> callGraph;
	private Map<String, TreeNeuron> methods;
	
	public EncodeGraph(Map<String, TreeNeuron> methodMap) {
		this.methods = methodMap;
		makeCallGraph();
	}

	private void makeCallGraph() {
		callGraph = new DirectedGraph<String>();
		for(String methodName : methods.keySet()) {
			TreeNeuron body = methods.get(methodName);
			Set<String> calledMethods = getCalledMethods(body);
			for(String m : calledMethods) {
				callGraph.addEdge(methodName, m);
			}
		}
	}

	private Set<String> getCalledMethods(TreeNeuron tree) {
		Set<String> called = new HashSet<String>();
		String node = tree.getType();
		if(methods.containsKey(node)) {
			called.add(node);
		}
		for(TreeNeuron child : tree.getChildren()) {
			called.addAll(getCalledMethods(child));
		}
		return called;
	}

	public boolean hasCycles() {
		return callGraph.hasCycles();
	}
	
	public CodeVector getRunActivation() {
		throw new RuntimeException("todo");
	}
	
	@Override
	public String toString() {
		String str = "";
		for(String key : methods.keySet()) {
			TreeNeuron tree = methods.get(key);
			str += key + ":\n";
			str += tree;
			str += "\n\n";
		}
		str += "call graph:\n";
		str += callGraph.toString();
		str += "\n";
		return str;
	}
	
	public TreeNeuron getRunEncodeTreeClone() {
		return new TreeNeuron(getRunEncodeTree());
	}

	private TreeNeuron getRunEncodeTree() {
		if(!methods.containsKey("run")) {
			return new TreeNeuron("noop", "0");
		}
		TreeNeuron run = methods.get("run");
		IdCounter counter = new IdCounter();
		return flatTree(run, counter);
	}
	
	private TreeNeuron flatTree(TreeNeuron tree, IdCounter counter) {
		String id = counter.getNextIdStr();
		String type = tree.getType();
		if(methods.containsKey(type)) {
			return flatTree(methods.get(type), counter);
		}
		List<TreeNeuron> children = new ArrayList<TreeNeuron>();
		for(TreeNeuron child : tree.getChildren()) {
			children.add(flatTree(child, counter));
		}
		return new TreeNeuron(tree.getType(), children, id);
	}
	
	
}

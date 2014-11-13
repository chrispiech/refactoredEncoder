package models.encoder.neurons;

import java.util.ArrayList;
import java.util.List;

import models.blocky.BlockyHelper;
import models.encoder.CodeVector;

import org.ejml.simple.SimpleMatrix;

import util.Warnings;

public class TreeNeuron extends Neuron {
	
	private String type;
	private List<TreeNeuron> children;
	private String nodeId;
	
	public TreeNeuron(String type, String nodeId) {
		this(type, new ArrayList<TreeNeuron>(), nodeId);
	}

	public TreeNeuron(String type, List<TreeNeuron> children, String nodeId) {
		this.type = type;
		this.children = children;
		this.nodeId = nodeId;
		z = null;
		activation = null;
	}

	public TreeNeuron(TreeNeuron toCopy) {
		this.type = toCopy.type;
		this.z = null;
		this.activation = null;
		this.nodeId = toCopy.nodeId;
		children = new ArrayList<TreeNeuron>();
		for(TreeNeuron child : toCopy.children) {
			TreeNeuron newChild = new TreeNeuron(child);
			children.add(newChild);
		}
	}
	
	public List<TreeNeuron> getChildren() {
		return children;
	}

	public String getType() {
		return type;
	}

	public int numChildren() {
		return children.size();
	}

	public TreeNeuron getChild(int i) {
		return children.get(i);
	}

	
	
	public boolean isLeaf() {
		return children.size() == 0;
	}
	
	public boolean isConstant() {
		try { 
	        Integer.parseInt(type); 
	    } catch(NumberFormatException e) { 
	        return false; 
	    }
	    // only got here if we didn't return false
		Warnings.check(children.size() == 0);
	    return true;
	}
	
	public String toString() {
		return toString(0);
	}

	protected String toString(int indentSize) {
		String treeString = "";
		String indent = "";
		for(int i = 0; i < indentSize; i++){
			indent += "  ";
		}
		treeString += indent + type + "\n";
		if(!children.isEmpty()) {
			treeString += indent + "[\n";
			for(TreeNeuron child : children) {
				treeString += child.toString(indentSize + 1);
			}
			treeString += indent + "]\n";
		}
		return treeString;
	}

	public boolean containsNode(String type) {
		for(TreeNeuron child : getChildren()) {
			if(child.containsNode(type)) {
				return true;
			}
		}
		return this.type.equals(type);
	}

	public static String blockType() {
		return "block";
	}

	public List<CodeVector> getChildActivations() {
		List<CodeVector> acts = new ArrayList<CodeVector>();
		for(TreeNeuron child : getChildren()) {
			acts.add(new CodeVector(child.getActivation()));
		}
		return acts;
	}
	
	public TreeNeuron getDescendant(String nodeId) {
		if(this.nodeId.equals(nodeId)) {
			return this;
		}
		for(TreeNeuron child : children) {
			TreeNeuron descendant = child.getDescendant(nodeId);
			if(descendant != null) {
				return descendant;
			}
		}
		return null;
	}

}

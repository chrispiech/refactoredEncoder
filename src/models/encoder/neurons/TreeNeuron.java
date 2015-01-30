package models.encoder.neurons;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import models.encoder.ClusterableMatrix;
import util.Warnings;

public class TreeNeuron extends Neuron implements Serializable{
	private static final long serialVersionUID = -7014726958033335159L;
	private String type;
	private TreeNeuron[] children;
	private transient short nodeId;

	public TreeNeuron(String type, String nodeId) {
		this(type, new ArrayList<TreeNeuron>(), nodeId);
	}

	public TreeNeuron(String type, List<TreeNeuron> children, String nodeId) {
		this.type = type;
		this.children = new TreeNeuron[children.size()];
		children.toArray(this.children);
		this.nodeId = Short.parseShort(nodeId);
		z = null;
		activation = null;
	}

	public TreeNeuron(TreeNeuron toCopy) {
		this.type = toCopy.type;
		this.z = null;
		this.activation = null;
		this.nodeId = toCopy.nodeId;
		int numChildren = toCopy.children.length;
		children = new TreeNeuron[numChildren];
		for(int i = 0; i < numChildren; i++) {
			TreeNeuron child = toCopy.children[i];
			TreeNeuron newChild = new TreeNeuron(child);
			children[i] = newChild;
		}
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setChildren(TreeNeuron[] children) {
		this.children = children;
	}

	public void setNodeId(short nodeId) {
		this.nodeId = nodeId;
	}

	public String getNodeId() {
		return nodeId + "";
	}

	public List<TreeNeuron> getChildren() {
		return Arrays.asList(children);
	}

	public String getType() {
		return type;
	}

	public int numChildren() {
		return children.length;
	}

	public TreeNeuron getChild(int i) {
		return children[i];
	}



	public boolean isLeaf() {
		return numChildren() == 0;
	}

	public boolean isConstant() {
		try { 
			Integer.parseInt(type); 
		} catch(NumberFormatException e) { 
			return false; 
		}
		// only got here if we didn't return false
		Warnings.check(isLeaf());
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
		if(!isLeaf()) {
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

	public List<ClusterableMatrix> getChildActivations() {
		List<ClusterableMatrix> acts = new ArrayList<ClusterableMatrix>();
		for(TreeNeuron child : getChildren()) {
			acts.add(new ClusterableMatrix(child.getActivation()));
		}
		return acts;
	}

	public boolean equals(Object o) {
		TreeNeuron other = (TreeNeuron)o;
		if(!type.equals(other.type)) return false;
		return Arrays.equals(children, other.children);
	}

	public int hashCode() {
		List<Object> objs = new LinkedList<Object>();
		objs.add(type);
		objs.addAll(getChildren());
		return objs.hashCode();
	}

	public TreeNeuron getDescendant(String nodeId) {
		if(this.nodeId == Short.parseShort(nodeId)) {
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

	private void writeObject(
			ObjectOutputStream aOutputStream
			) throws IOException {
		//perform the default serialization for all non-transient, non-static fields
		aOutputStream.defaultWriteObject();
	}

}

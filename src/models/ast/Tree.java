package models.ast;

import java.util.*;

public class Tree {

	// What a user thinks we store
	private String type;
	private List<Tree> children;
	private String id;

	// What we also store
	private int size;
	private String tag; // used for edit distance...

	public static final Tree NULL = new Tree("null", "", Collections.<Tree> emptyList(), null);

	public Tree(String type, List<Tree> children, String nodeId) {
		this.type = type;
		this.children = children;
		this.id = nodeId;
		size = 1;
		for(Tree child : children) {
			size += child.size;
		}
	}
	
	public Tree(String type, String name, List<Tree> children, String nodeId) {
		this.type = type;
		this.children = children;
		this.id = nodeId;
		size = 1;
		for(Tree child : children) {
			size += child.size;
		}
	}

	public Tree(String type, String nodeId) {
		this(type, new ArrayList<Tree>(), nodeId);
	}

	public List<Tree> getChildren() {
		return children;
	}

	public int size() {
		return size;
	}

	public boolean isLeaf() {
		return children.isEmpty();
	}

	public List<Tree> getPostorder() {
		List<Tree> postorder = new ArrayList<Tree>();
		for(Tree child : children) {
			postorder.addAll(child.getPostorder());
		}
		postorder.add(this);
		return postorder;
	}
	
	public List<Tree> getSubtreeOfSize(String method, int n) {
		List<Tree> result = new ArrayList<Tree>();
		
		if(method.equals("=") && this.size == n) result.add(this);
		if(method.equals("<") && this.size < n) result.add(this);
		if(method.equals("<=") && this.size <= n) result.add(this);	
		if(this.size <= 0) return result;
		
		for(Tree child : children) {
			result.addAll(child.getSubtreeOfSize(method , n));
		}
		return result;
	}

	public String getType() {
		return type;
	}

	public String getLabel() {
		return type;
	}
	
	public String getId() {
		return id;
	}
	
	public Tree getPreviousTree(Tree subtree) {
		Tree parent = getParent(subtree);
		if(parent == null) return null;
		List<Tree> siblings = parent.children;
		int index = 0;
		// Can't use indexOf because need == not .equals
		for(int i = 0; i < siblings.size(); i++) {
			Tree child = siblings.get(i);
			if(child == subtree) {
				index = i;
			}
		}
		if(index == 0) return null;
		return siblings.get(index - 1);
	}
	
	public boolean isRightmostSubtree(Tree subtree) {
		Tree curr = this;
		while(curr != null) {
			if(curr == subtree) {
				return true;
			}
			curr = curr.getRightmostChild();
		}
		return false;
	}
	
	public boolean isLeftmostSubtree(Tree subtree) {
		Tree curr = this;
		while(curr != null) {
			if(curr == subtree) {
				return true;
			}
			curr = curr.getLeftmostChild();
		}
		return false;
	}
	
	public Tree getRightmostChild() {
		if(children.isEmpty()) return null;
		return children.get(children.size() - 1);
	}
	
	public Tree getLeftmostChild() {
		if(children.isEmpty()) return null;
		return children.get(0);
	}

	public boolean shiftEquals(Tree other, int shift, int otherShift) {
		this.size -= shift;
		other.size -= otherShift;
		boolean result = this.equals(other);
		this.size += shift;
		other.size += otherShift;
		return result;
	}
	
	public int numChildren() {
		return children.size();
	}
	
	private Tree getParent(Tree subtree) {
		for(Tree child : children) {
			if(child == subtree) {
				return this;
			}
			Tree childParent = child.getParent(subtree);
			if(childParent != null) {
				return childParent;
			}
		}
		return null;
	}
	
	public void setTag(String tag) {
		this.tag = tag;
	}
	
	public String getTag() {
		return tag;
	}

	@Override
	public boolean equals(Object o) {
		if(o == null) {
			return false;
		}
		Tree other = (Tree) o;
		if(!type.equals(other.type)) {
			return false;
		}
		if(size != other.size) {
			return false;
		}
		return children.equals(other.children);
	}
	
	public int getRootHash() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public String toString() {
		return toString(0);
	}

	protected String toString(int indentSize) {
		String treeString = "";
		String indent = "";
		for(int i = 0; i < indentSize; i++){
			indent += "  ";
		}
		treeString += indent + getLabel() + "\n";
		if(!children.isEmpty()) {
			treeString += indent + "[\n";
			for(Tree child : children) {
				treeString += child.toString(indentSize + 1);
			}
			treeString += indent + "]\n";
		}
		return treeString;
	}

	public boolean isType(String name) {
		return type.equals(name);
	}

	public Tree getChild(int i) {
		return children.get(i);
	}

	public boolean hasChildren() {
		return getNumChildren() > 0;
	}

	public int getNumChildren() {
		return children.size();
	}


}

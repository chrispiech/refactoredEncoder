package models.blocky;

import java.util.*;

import models.ast.Tree;
import util.FileSystem;
import util.Graph;
import util.IdCounter;
import util.PQueue;
import util.Warnings;

public class BlockyHelper {

	public static boolean isContiguous(Tree ast) {
		int numChildren = ast.getChildren().size();
		return numChildren <= 1;
	}

	public static boolean isBlock(Tree t) {
		String type = t.getType();
		if(checkType(t, "maze_turnLeft")) return true;
		if(checkType(t, "maze_turnRight")) return true;
		if(checkType(t, "maze_moveForward")) return true;
		if(checkType(t, "maze_ifElse")) return true;
		if(checkType(t, "maze_forever")) return true;

		return false;
	}
	
	public static boolean isCommand(String type) {
		if(type.equals("maze_turnLeft")) return true;
		if(type.equals("maze_turnRight")) return true;
		if(type.equals("maze_moveForward")) return true;
		return false;
	}


	public static String getBlocklyName(Tree tree) {
		if(tree == null) {
			return "null";
		}
		String type = tree.getType();
		if(isTurn(tree)) {
			Tree param = tree.getChildren().get(0);
			return param.getLabel();
		}
		if(isMove(tree)) {
			return "moveForward";
		}
		return type;
	}

	public static int getNumBlocks(Tree t) {
		int numBlocks = 0;
		if(isBlock(t)) {
			numBlocks++;
		}
		for(Tree child : t.getChildren()) {
			numBlocks += getNumBlocks(child);
		}
		return numBlocks;
	}
	
	public static int getNumBlocks(Tree t, String type) {
		int numBlocks = checkType(t, type) ? 1 : 0;
		for(Tree child : t.getChildren()) {
			numBlocks += getNumBlocks(child, type);
		}
		return numBlocks;
	}

	public static Tree simplify(Tree node) {
		Warnings.error("simplifying...");
		List<Tree> children = new ArrayList<Tree>();

		if(node.getType().equals("maze_turn")) {
			String dirType = node.getLeftmostChild().getType();
			if(dirType.equals("turnLeft")) {
				return new Tree("maze_turnLeft", children, node.getId());
			}
			if(dirType.equals("turnRight")) {
				return new Tree("maze_turnRight", children, node.getId());
			}
		}

		if(node.getType().equals("maze_forever")) {
			Tree doBlock = node.getLeftmostChild();
			if(doBlock != null && BlockyHelper.checkType(doBlock, "DO")) {
				Tree simpleDo = simplify(doBlock);
				return new Tree("maze_forever", simpleDo.getChildren(), node.getId());
			} 
		}


		for(Tree child : node.getChildren()) {
			if(child.getType().equals("statementList")) {
				for(Tree slChild : child.getChildren()) {
					children.add(simplify(slChild));
				}
			} else {
				Tree simpleChild = simplify(child);
				children.add(simpleChild);
			}
		}

		return new Tree(node.getType(), children, node.getId());
	}

	public static boolean isTurn(Tree node) {
		if(node == null) return false;
		if(node.getType().equals("maze_turnLeft")) return true;
		if(node.getType().equals("maze_turnRight")) return true;
		return node.getType().equals("maze_turn");
	}

	public static boolean isForever(Tree node) {
		if(node == null) return false;
		return node.getType().equals("maze_forever");
	}

	public static boolean isMove(Tree node) {
		if(node == null) return false;
		return node.getType().equals("maze_moveForward");
	}

	public static boolean isTurnParam(Tree node) {
		if(node == null) return false;
		String type = node.getType();
		if(type.equals("turnLeft")) return true;
		if(type.equals("turnRight")) return true;
		return false;
	}

	public static boolean isCodeBlockParent(Tree node) {
		if(checkType(node, "program")) return true;
		if(checkType(node, "DO")) return true;
		if(checkType(node, "ELSE")) return true;
		if(checkType(node, "maze_forever")) return true;
		return false;
	}

	public static boolean isEditable(Tree node) {
		if(checkType(node, "program")) return false;
		if(checkType(node, "DO")) return false;
		if(checkType(node, "ELSE")) return false;
		return true;
	}

	public static boolean isIfElse(String type) {
		return type.equals("maze_ifElse");
	}
	
	public static boolean isRepeatForever(String type){
		return type.equals("maze_forever");
	}

	public static boolean isCondition(String type) {
		if(type.equals("isPathForward")) return true;
		if(type.equals("isPathLeft")) return true;
		if(type.equals("isPathRight")) return true;
		return false;
	}

	public static boolean isElse(Tree node) {
		if(node == null) return false;
		return node.getType().equals("ELSE");
	}

	private static boolean isTurnLeft(Tree node) {
		return checkType(node, "maze_turnLeft");
	}

	public static boolean checkType(Tree node, String string) {
		return node.getType().equals(string);
	}

	public static boolean isProgram(String string) {
		return string.equals("program");
	}



}

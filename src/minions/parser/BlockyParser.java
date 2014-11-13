package minions.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import minions.program.PostExperimentLoader;
import models.ast.Tree;
import models.blocky.BlockyHelper;
import models.code.TestTriplet;
import models.encoder.neurons.TreeNeuron;
import models.language.Language;
import util.IdCounter;

public class BlockyParser extends EncodeTreeParser {
	
	public static TreeNeuron parse(Tree ast) {
		return new BlockyParser().parseTree(ast);
	}

	/******************************************************
	 *         PRIVATE PARTS
	 ******************************************************/

	protected TreeNeuron parseTree(Tree ast) {
		List<TreeNeuron> parseChildren = new ArrayList<TreeNeuron>();
		String type = ast.getType();
		if(type.equals("DO") || type.equals("ELSE") || type.equals("program")) {
			return parseList(ast.getChildren());
		} else if(shouldSplitBlock(ast)) {
			TreeNeuron body = parseList(ast.getChildren());
			parseChildren = Collections.singletonList(body);
		} else {
			for(Tree child : ast.getChildren()) {
				parseChildren.add(parseTree(child));
			}
		}
		
		return new TreeNeuron(type, parseChildren, nodeIds.getNextIdStr());
	}
	
	private boolean shouldSplitBlock(Tree ast) {
		boolean isBlock = isBlock(ast);
		int numChildren = ast.numChildren();
		return isBlock && numChildren >= 2;
	}

	private boolean isBlock(Tree ast) {
		if(ast.getType().equals("block")) {
			return true;
		}
		if(BlockyHelper.isCodeBlockParent(ast)) {
			return true;
		}
		return false;
	}
	
	private static void validateHead(TreeNeuron encodeTree) {
		int expected = getNumExpectedChildren(encodeTree);
		int numChildren = encodeTree.numChildren();
		if(expected != numChildren) {
			throw new RuntimeException("malformed tree: " + encodeTree);
		}
	}
	
	

	private static int getNumExpectedChildren(TreeNeuron tree) {
		String type = tree.getType();
		if(type.equals("block")) {
			return 2;
		} 
		if(type.equals("maze_ifElse")) {
			return 3;
		}
		if(type.equals("maze_forever")) {
			return 1;
		}
		if(BlockyHelper.isCommand(type)) {
			return 0;
		}
		if(BlockyHelper.isCondition(type)) {
			return 0;
		}
		throw new RuntimeException("unexpected type: " + tree);
	}
	
	public static void main(String [] args) {
		/*System.out.println("Testing the Encode Tree Parser...");
		List<TestTriplet> train = ExperimentLoader.loadTests("train", 10);
		
		for(TestTriplet test : train) {
			Tree ast = test.getAst();
			System.out.println(ast);
			EncodeTree encodeTree = EncodeTreeParser.parseEncodeTree(ast);
			System.out.println(encodeTree);
			EncodeTreeParser.validateTree(encodeTree);
		}*/
	}
}

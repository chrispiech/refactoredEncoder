package minions.parser;

import java.util.*;

import minions.program.PostExperimentLoader;
import models.ast.Tree;
import models.blocky.BlockyHelper;
import models.code.TestTriplet;
import models.encoder.neurons.TreeNeuron;
import models.language.Language;
import util.IdCounter;
import util.Warnings;

/*
 * Class: EncodeTreeParser
 * ----------------------
 * This takes a normal ast from typical parsing, and turns it into
 * a tree that can be used as input to a recursive encoder. Most
 * of the work is to split up codeblocks with more than two children.
 * I call the output an encodeTree (which is different than an ast)
 */
public class EncodeTreeParser {

	/**
	 * Method: Parse Encode Tree
	 * -------------------------
	 * Takes an AST and turns it into an ET (Encode Tree)... which
	 * mostly just means that blocks of code are split up into
	 * binary trees.
	 */
	public static TreeNeuron parse(Tree ast, Language language) {
		if(language.getName().equals("blocky")) {
			return BlockyParser.parse(ast);
		} else {
			return KarelParser.parse(ast);
		}
	}

	/**
	 * Method: Validate Tree
	 * -------------------------
	 * Takes an ET (Encode Tree) and makes sure that its valid.
	 * Doesn't test everything, just basic sanity checks..
	 */
	public static void validateTree(TreeNeuron encodeTree) {
		/*validateHead(encodeTree);
		for(EncodeTree child : encodeTree.getChildren()) {
			validateTree(child);
		}*/
		Warnings.error("update");
	}

	protected IdCounter nodeIds = new IdCounter();

	protected TreeNeuron parseList(List<Tree> treeList) {
		if(treeList.isEmpty()) {
			return new TreeNeuron("noop", nodeIds.getNextIdStr());
		}

		List<TreeNeuron> parseChildren = new ArrayList<TreeNeuron>();

		// this program is empty!
		if(treeList.size() == 0) return null;

		// If there is only one node in the list, call normal parse
		if(treeList.size() == 1) {
			return parseTree(treeList.get(0));
		}

		// If there are more than two nodes in the left tree, its broken down.
		List<Tree> leftChildren = treeList.subList(0, treeList.size() -1);
		if(leftChildren.size() == 1) {
			parseChildren.add(parseTree(treeList.get(0)));
		} else {
			parseChildren.add(parseList(leftChildren));
		}

		// The right tree is always parsed as a tree
		Tree rightTree = treeList.get(treeList.size() - 1);
		parseChildren.add(parseTree(rightTree));

		return new TreeNeuron("block", parseChildren, nodeIds.getNextIdStr());
	}

	protected TreeNeuron parseTree(Tree tree) {
		throw new RuntimeException("abstract class");
	}


}

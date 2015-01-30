package minions.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import util.FileSystem;
import util.IdCounter;
import util.Warnings;
import minions.program.PostExperimentLoader;
import models.ast.Tree;
import models.code.TestTriplet;
import models.encoder.EncodeGraph;
import models.encoder.EncoderParams;
import models.encoder.neurons.TreeNeuron;
import models.language.KarelLanguage;
import models.language.Language;

public class KarelParser extends EncodeTreeParser {

	public static TreeNeuron parse(Tree ast) {
		return new KarelParser().parseTree(ast);
	}

	protected TreeNeuron parseTree(Tree ast) {
		List<TreeNeuron> parseChildren = new ArrayList<TreeNeuron>();
		String type = ast.getType();
		if(type.equals("body") || type.equals("then") || type.equals("else")) {
			return parseList(ast.getChildren());
		} else if(type.equals("while")) {
			Tree cond = ast.getChild(0).getChild(0);
			parseChildren.add(parseTree(cond));
			parseChildren.add(parseChild(ast, 1));
		} else if(type.equals("ifElse")) {
			Tree cond = ast.getChild(0).getChild(0);
			parseChildren.add(parseTree(cond));
			parseChildren.add(parseChild(ast, 1));
			parseChildren.add(parseChild(ast, 2));
		} else if(type.equals("repeat")) {
			Tree count = ast.getChild(0).getChild(0);
			parseChildren.add(parseTree(count));
			parseChildren.add(parseChild(ast, 1));
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

	private TreeNeuron parseChild(Tree ast, int index) {
		Tree child = ast.getChild(index);
		return parseTree(child);
	}

	private boolean shouldSplitBlock(Tree ast) {
		boolean isBlock = KarelLanguage.isBlock(ast.getType());
		int numChildren = ast.numChildren();
		return isBlock && numChildren >= 2;
	}

	public static void main(String [] args) {
		FileSystem.setAssnId("Midpoint");
		FileSystem.setExpId("postExp");
		System.out.println("Testing the Encode Tree Parser...");
		Language karel = new KarelLanguage();
		List<TestTriplet> train = PostExperimentLoader.loadFolds("6x6", 1, karel);

		int recursionCount = 0;
		for(TestTriplet test : train) {
			int id = Integer.parseInt(test.getAstId());
			//System.out.println(id);
			//if(id != 390) continue;
			//System.out.println(ast);
			//System.out.println("-----");
			EncodeGraph callGraph = test.getEncodeGraph();
			//System.out.println(callGraph);
			if(callGraph.hasCycles()) {
				recursionCount++;
			}
			
			//break;
		}
		
		System.out.println(100.0 * recursionCount / train.size());
	}

}

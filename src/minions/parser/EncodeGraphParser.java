package minions.parser;

import java.util.*;

import util.Warnings;
import models.ast.Tree;
import models.encoder.EncodeGraph;
import models.encoder.neurons.TreeNeuron;
import models.language.Language;

public class EncodeGraphParser {

	public static EncodeGraph parse(Tree ast, Language language) {
		if(language.getName().equals("blocky")) {
			return parseBlocky(ast, language);
		} else {
			return parseClass(ast, language);
		}
	}

	private static EncodeGraph parseClass(Tree ast, Language language) {
		List<Tree> methods = ast.getChildren();
		for(Tree t : methods) {
			Warnings.check(t.isType("method"));
		}
		
		Map<String, TreeNeuron> methodMap = new HashMap<String, TreeNeuron>();
		for(Tree t : methods) {
			String name = t.getChild(0).getChild(0).getType();
			Tree body = t.getChild(1);
			TreeNeuron bodyEncodeTree = EncodeTreeParser.parse(body, language);
			methodMap.put(name, bodyEncodeTree);
		}
		return new EncodeGraph(methodMap);
	}

	private static EncodeGraph parseBlocky(Tree ast, Language language) {
		TreeNeuron encodeTree = EncodeTreeParser.parse(ast, language);
		Map<String, TreeNeuron> methodMap = new HashMap<String, TreeNeuron>();
		methodMap.put("run", encodeTree);
		return new EncodeGraph(methodMap);
	} 
	
}

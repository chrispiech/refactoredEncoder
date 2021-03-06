package minions.program;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import models.encoder.EncodeGraph;
import models.encoder.neurons.TreeNeuron;
import models.language.Language;

import org.json.JSONObject;

import util.FileSystem;
import util.Warnings;

public class EncodeGraphsLoader {
	
	public static Map<String, EncodeGraph> loadGraphs(Language lang,
			ZipFile zip) {
		Enumeration<? extends ZipEntry> entries = zip.entries();
		Map<String, EncodeGraph> m = new HashMap<String, EncodeGraph>();
		int done = 0;
		while(entries.hasMoreElements()){
			//System.out.println(++done);
			ZipEntry entry = entries.nextElement();
			if(!FileSystem.getExtension(entry.getName()).equals("json")) {
				continue;
			}
			
			EncodeGraph g = loadGraph(lang, zip, entry);
			File temp = new File(entry.getName());
			
			String name = FileSystem.getNameWithoutExtension(temp.getName());
			
			//System.out.println(name);
			
			if(g == null || g.hasCycles()) {
				continue;
			}

			m.put(name, g);
			if(++done % 100 == 0) System.out.println(done);
			//Warnings.msg("don't forget you are only loading 500 programs");
			//if(done > 500) break;
		}
		return m;
	}
	
	public static Map<String, TreeNeuron> loadRunTreeClones(Language lang,
			ZipFile zip) {
		Enumeration<? extends ZipEntry> entries = zip.entries();
		Map<String, TreeNeuron> m = new HashMap<String, TreeNeuron>();
		int done = 0;
		while(entries.hasMoreElements()){
			//System.out.println(++done);
			ZipEntry entry = entries.nextElement();
			if(!FileSystem.getExtension(entry.getName()).equals("json")) {
				continue;
			}
			
			EncodeGraph g = loadGraph(lang, zip, entry);
			File temp = new File(entry.getName());
			
			String name = FileSystem.getNameWithoutExtension(temp.getName());
			
			//System.out.println(name);
			
			if(g == null || g.hasCycles()) {
				continue;
			}

			m.put(name, g.getRunEncodeTreeClone());
			if(++done % 100 == 0) System.out.println(done);
		}
		return m;
	}

	public static EncodeGraph loadGraph(Language lang, ZipFile zip,
			ZipEntry entry) {
		String str = FileSystem.getZipEntryString(zip, entry);


		JSONObject fileJson = new JSONObject(str);

		EncodeGraph g = getEncodeGraph(fileJson, lang);
		return g;
	}

	
	
	private static EncodeGraph getEncodeGraph(JSONObject fileJson, Language lang) {
		Map<String, TreeNeuron> methodMap = new HashMap<String, TreeNeuron>();
		JSONObject encodeTreeJson = fileJson;
		if(encodeTreeJson.toString().equals("{}")) return null;
		for(Object key : encodeTreeJson.keySet()) {
			String name = (String)key;
			JSONObject methodJson = encodeTreeJson.getJSONObject(name);
			TreeNeuron body = EncodeTreeLoader.parseJson(methodJson);
			methodMap.put(name, body);
		}
		return new EncodeGraph(methodMap);
	}
}

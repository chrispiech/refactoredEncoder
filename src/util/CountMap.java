package util;

import java.util.*;

public class CountMap<T> {
	
	Map<T, Integer> map = new HashMap<T, Integer>();
	
	public void add(T item) {
		if(!map.containsKey(item)) {
			map.put(item,  0);
		}
		int newCount = map.get(item) + 1;
		map.put(item, newCount);
	}
	
	public void addMany(T item, int num) {
		if(!map.containsKey(item)) {
			map.put(item,  0);
		}
		int newCount = map.get(item) + num;
		map.put(item, newCount);
	}
	
	public int get(T item) {
		if(!map.containsKey(item)) {
			return 0;
		}
		return map.get(item);
	}
	
	public Set<T> keySet() {
		return map.keySet();
	}
	
	public int size() {
		return map.size();
	}
	
	public static<T> CountMap<T> merge(CountMap<T> a, CountMap<T> b) {
		CountMap<T> merged = new CountMap<T>();
		for(T key : a.keySet()) {
			merged.addMany(key, a.get(key));
		}
		for(T key : b.keySet()) {
			merged.addMany(key, b.get(key));
		}
		return merged;
	}

	
	
	

}

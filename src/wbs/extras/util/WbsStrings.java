package wbs.extras.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import jdk.internal.joptsimple.internal.Strings;

public abstract class WbsStrings {
	
	public static String capitalizeAll(String str) {
		String[] words = str.toLowerCase().split(" ");
		String display = ""; 
		for (String word : words) {
			display += " " + capitalize(word);
		}
		return display.substring(1);
	}
	
	public static String capitalize(String str) {
		String display = str.substring(1).toLowerCase();
		display = str.substring(0, 1).toUpperCase() + display;
		return display;
	}
	
	public static Set<String> getNextNodes(String currentPath, List<String> strings) {
		Set<String> nodes = new HashSet<>();

		String[] currentNodes = currentPath.split("\\.");
		int currentLength = currentNodes.length;
		
		if (currentPath.endsWith(".")) {
			String[] temp = new String[currentLength+1];
			for (int i = 0; i < currentLength; i++) {
				temp[i] = currentNodes[i];
			}
			temp[currentLength] = "";
			currentLength++;
			
			currentNodes = temp;
		}
		
		String latestArg = currentNodes[currentLength-1];
		
		main: for (String path: strings) {
			String[] nodesInPath = path.split("\\.");
			
			if (nodesInPath.length < currentLength) {
				continue;
			} else {
				for (int i = 0; i < currentLength-1; i++) {
					if (!currentNodes[i].equals(nodesInPath[i])) {
						continue main;
					}
				}
				
				// We now know that currentNodes is the starting args of nodesInPath
				// Set will handle duplicates
				String toAdd = nodesInPath[currentLength-1];
				
				if (toAdd.startsWith(latestArg)) {
					toAdd = toAdd.substring(latestArg.length());
					
					if (toAdd.length() == 0) {
						if (nodesInPath.length > currentLength) {
							return getNextNodes(currentPath + ".", strings);
						}
					} else {
						nodes.add(currentPath + toAdd);
					}
				}
			}
		}
		
		return nodes;
	}
	
	public static String getLineWith(String find, Collection<String> in) {
		for (String node : in) {
			if (node.contains(find)) {
				return node;
			}
		}
		return null;
	}
	
	public static String getInvisibleString(String original) {
		char[] charList = new char[original.length()*2];
		int i = 0;
		for (char c : original.toCharArray()) {
			charList[i] = '§';
			charList[i+1] = c;
			i+=2;
		}
		return charList.toString();
	}
	
	public static String revealString(String invisibleString) {
		return invisibleString.replaceAll("§", "");
	}

	/***
	 * 
	 * @param strings The array of Strings to combine
	 * @param index The index to start combining at
	 * @return A single String containing all entries in {strings} split with " ", excluding the first {index} entries
	 */
	
	public static String combineLast(String[] strings, int index) {
		String[] newStringList = new String[strings.length-index];
		for (int i = index; i < strings.length; i++) {
			newStringList[i-index] = strings[i];
		}
		return String.join(" ", newStringList);
	}

	public static String combineFirst(String[] strings, int index, String with) {
		String[] newStringList = new String[strings.length-index];
		for (int i = 0; i <= index; i++) {
			newStringList[i] = strings[i];
		}
		return String.join(with, newStringList);
	}
}

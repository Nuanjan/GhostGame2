package studentCode;


import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;

public class MyTrie implements Trie {
	private Node root;
	static final int SIZE = 26;
	public static class Node{
		HashMap<Character, Node> charMap;
		Character itshelf;
		boolean isWord;
		Node parent;
		
		public Node(Character it, Boolean endWord, Node parent) {
			itshelf = it;
			isWord = endWord;
			charMap = new HashMap<>();
			this.parent = parent;
		}
		public String getWord() {
			StringBuilder builder = new StringBuilder();
			getInternalWord(builder, this);
			return builder.reverse().toString();
		}
		
		private void getInternalWord(StringBuilder builder, Node current) {
			Character ch;
			ch = current.itshelf;
			if(ch!=null) {
				builder.append(ch);
				getInternalWord(builder,current.parent);
				
			}
		}
		
	}
	public MyTrie() {
		root = new Node(null,false,null);
	}
	@Override
	public void insert(String word) {
		// TODO Auto-g
		HashMap<Character, Node> k = root.charMap;
		Node v = root;
		for (int i = 0; i < word.length(); i++) {
			char letter = word.charAt(i);
			
			if (k.containsKey(letter)) {
				v = k.get(letter);
			} else {
				
				v = new Node(letter, false, v);
				k.put(letter, v);
			}
			k = v.charMap;
			if (i == word.length() - 1)
				v.isWord = true;
		}
	}

	@Override
	public boolean contains(String word) {
		// TODO Auto-generated method stub
		HashMap<Character, Node> k = root.charMap;
		Node t = null;
		for (int i = 0; i < word.length(); i++) {
			char c = word.charAt(i);
			if (k.containsKey(c)) {
				t = k.get(c);
				k = t.charMap;
			} else {
				t = null;
			}

		}
		if (t != null && t.isWord)
			return true;
		else
			return false;
	}

	@Override
	public boolean containsPrefix(String prefix) {
		// TODO Auto-generated method stub
		HashMap<Character, Node> k = root.charMap;
		Node t = null;
		for (int i = 0; i < prefix.length(); i++) {
			char c = prefix.charAt(i);
			if (k.containsKey(c)) {
				t = k.get(c);
				k = t.charMap;
			} else {
				t = null;
			}
		}
		if (t == null)
			return false;
		else
			return true;
	}
	private void wordGetter(Node currentNode, Set<String> n) {
		// String word; 
			//System.out.println(" \t " + currentNode.endofword);
		 if(currentNode.isWord)
		  n.add(currentNode.getWord());
		 //System.out.println(" \t_"	 		+ " " + currentNode.getWord());}
		 for(Node child: currentNode.charMap.values())
		 {
			 wordGetter(child, n);
			 
		 }
	}
	@Override
	public Set<String> getAllWords(String prefix) {
		// TODO Auto-generated method stub
		Set<String> n = new HashSet<>();
		Node currentNode = root;
		//System.out.println("Prefix " + prefix);
		for (int i = 0; i < prefix.length(); i++) {
			//currentNode.charmap.get(prefix.charAt(i));
			if(currentNode == null)
				return n;
			
			else
			{
			currentNode = currentNode.charMap.get(prefix.charAt(i));
			//System.out.println(" \t " + currentNode.itshelf);
			}}
		wordGetter(currentNode, n);        
		
		return n;
	}

	@Override
	public Set<String> getAllOddWords(String prefix) {
		// TODO Auto-generated method stub
		Set<String> words = getAllWords(prefix);
		Set<String> odds = new HashSet<>();
		for(String str: words)
		{
			if(str.length() %2 == 1)
				odds.add(str);
		}
		return odds;
	}

	@Override
	public Set<String> getAllEvenWords(String prefix) {
		// TODO Auto-generated method stub
		Set<String> words = getAllWords(prefix);
		Set<String> evenWords = new HashSet<>();
		for(String str: words)
		{
			if(str.length() %2 == 0)
				evenWords.add(str);
		}
		return evenWords;
	}

}

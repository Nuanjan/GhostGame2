package studentCode;


import java.util.Set;

public interface Trie {
	
	/**
	 * Inserts the specified string into the Trie. The Last node associated to the
	 * of the last char of the specified string will be marked as an end-of-word node. 
	 * @param word
	 */
	void insert(String word);
	
	/**
	 * Returns true if this Trie contains the specified string. In other words 
	 * the last char of the specified string is a node that is marked as 
	 * a end-of-word node.
	 * @param word
	 * @return
	 */
	boolean contains(String word);
	
	/**
	 * Returns true if this Trie contains the specified string as a word or a 
	 * word in the Trie begins with the specified string.
	 * @param prefix
	 * @return
	 */
	boolean containsPrefix(String prefix);
	
	/**
	 * Returns all words that begin with the specified prefix. 
	 * @param prefix
	 * @return
	 */
	Set<String> getAllWords(String prefix);
	
	/**
	 * Returns all words of odd length (odd number of characters) that begin
	 * with the specified prefix.
	 * @param prefix
	 * @return
	 */
	Set<String> getAllOddWords(String prefix);
	
	/**
	 * Returns all words of even length (even number of characters) that begin
	 * with the specified prefix.
	 * @param prefix
	 * @return
	 */
	Set<String> getAllEvenWords(String prefix);
}

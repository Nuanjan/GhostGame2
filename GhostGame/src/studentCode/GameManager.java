package studentCode;


import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import studentCode.GhostPlayerMain.TurnData;

/**
 * This class is your application's brain. It will be used by the
 * GhostPlayerMain class to get letter and location of the letter for each game
 * turn.
 * 
 * 
 * @author Student
 *
 */
public class GameManager {
	private Set<String> even,odd;
	boolean isFront = false;
	
	

	/**
	 * This method will get called once when your application is started up. You
	 * should use this to initialize your application by setting up your necessary
	 * data structures other classes. You will be given a number denoting the
	 * minimum winning word length as well as a set containing all the English words
	 * in the dictionary (>700K words)
	 * 
	 * @param minWordLength A value denoting the minimum length the fragment must to
	 *                      be consider as a word and for losing condition.
	 * @param allWords      All the words on the English language this is >700K
	 *                      elements.
	 */
	
	public GameManager(int minWordLength, Set<String> allWords) {
		//create HashSet to collect the word that has the odd or even length
		even = Collections.synchronizedSet(new HashSet<>());
		odd = Collections.synchronizedSet(new HashSet<>());
		
		allWords.parallelStream().forEach(word->{
			boolean isEven = (word.length()%2) ==0;
			Set<String> wordSet = isEven? even:odd;
			wordSet.add(word);
		});
		
		
	}

	/**
	 * This method is called every round you will be given the current word fragment
	 * of the game. In this method you will figure out the next letter and the
	 * location of the letter.
	 * 
	 * @param fragment The current ordered collection of letters that have been
	 *                 played in the game.
	 * @return An object that contains information about letter and location on the
	 *         next turn.
	 */
	public TurnData onTurn(String fragment) {
		Set<String> wordSet = getSet(fragment);
		AtomicReference<String> currentWord = new AtomicReference<String>("");
		System.out.println("word= "+wordSet);
		wordSet.parallelStream().forEach(word->{
			System.out.println("word= "+word+" fragment= "+fragment);
				if(word.contains(fragment)) {
					synchronized(currentWord) {
						if(currentWord.get().isEmpty()||currentWord.get().length()<= word.length()) {
							
							currentWord.set(word);
							System.out.print("word ="+word);
						}
						
					}
				
				}
		});
		
		
		// find the shorted word for more chance to win
		String shortedWord = currentWord.get();
		//set the first index to decide what position of character we want to add
		int startIndex = shortedWord.indexOf(fragment);
		int firstChar = shortedWord.indexOf(0);
		int leftIndex = startIndex-1;
		int rightIndex = startIndex+ fragment.length();
		

		char c ;
		if(leftIndex>0 && leftIndex!=0) {
			 c = shortedWord.charAt(leftIndex);
			 isFront = true;
		//	 System.out.println(" letter of front="+ c);
			
		}else {
			System.out.println("rightIndex="+rightIndex+" "+shortedWord);
			c = shortedWord.charAt(rightIndex);
			isFront = false;
			
		}
		System.out.println(" letter of back="+ fragment);
		return TurnData.create(c, isFront);
			}
	private Set<String> getSet(String fragment){
		//check if fragment is even
		boolean isEven = (fragment.length()%2) ==0;
		
		return isEven? even:odd;
	}
	
	private String getShorted(String shortedWord,String fragment){
		String bestWord = fragment ;
	
		return even;
		
	}
	
	
	
}

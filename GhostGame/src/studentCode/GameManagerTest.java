package studentCode;

import static org.junit.Assert.*;

import java.util.HashSet;

import org.junit.Test;

import studentCode.GhostPlayerMain.TurnData;

public class GameManagerTest {

	@Test
	public void winTest() {
		
		HashSet<String> allWords = new HashSet<>();
		
		allWords.add("test");
		allWords.add("jump");
		allWords.add("zappos");
		allWords.add("hello");
		
		GameManager manager = new GameManager(8, allWords);
						
		TurnData turnData = null;
				/*manager.onTurn("");
		
		assertNotNull(turnData);
		
		assertEquals('t', turnData.getLetter()); */
		
		turnData = manager.onTurn("te");
		
		assertNotNull(turnData);
		
		assertEquals('s', turnData.getLetter());
		
		assertFalse(turnData.isAddFront());
	}
	
	@Test
	public void loseTest() {
		
		HashSet<String> allWords = new HashSet<>();
		
		allWords.add("taco");
		allWords.add("jump");
		allWords.add("zappos");
		allWords.add("hello");
		
		GameManager manager = new GameManager(2, allWords);
						
		TurnData turnData = null;
			/*	manager.onTurn("t");
		
		assertNotNull(turnData);
		
		assertEquals('a', turnData.getLetter());
		
		assertFalse(turnData.isAddFront()); */
		
		turnData = manager.onTurn("tac");
		
		assertNotNull(turnData);
		
		assertEquals('o', turnData.getLetter());
		
		assertFalse(turnData.isAddFront());
	}

}

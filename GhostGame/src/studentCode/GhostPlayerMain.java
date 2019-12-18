package studentCode;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import sharedCode.AddLocation;
import sharedCode.GameAction;
import sharedCode.SharedGameData;

public class GhostPlayerMain {

	static String sharedFilePath;
	static String fragment;
	static GhostPlayerMain playerMain;
	static Random randomizer;
	static String myTeamName = "UNNAMED";
	static int minWordLength;
	static Dictionary dictionary;
	public static final String INTERNAL_DICTIONARY_FILE_RELATIVE_PATH = "ARBITOR_DICTIONARY.txt";
	public static final String INTERNAL_DICTIONARY_FILE_PATH = File.separator+INTERNAL_DICTIONARY_FILE_RELATIVE_PATH;

	private String path;
	private MessageDigest messageDigest;
	private int currentCheckSum;
	private boolean hasChanged = false;
	private long nextUpateTime = 0;
	private GameManager gameManager;

	public static void main(String[] args) throws Exception {

		if(args.length < 2) {
			System.err.println("This application must be started using the SGhostApp.jar, please see the Moodle assignment page for startup instructions.");
			System.exit(0);
		}

		File exeDir = new java.io.File(GhostPlayerMain.class.getProtectionDomain()
				.getCodeSource()
				.getLocation()
				.getPath());

		String dictFilePath = exeDir.getParent()+File.separator+INTERNAL_DICTIONARY_FILE_RELATIVE_PATH;

		//Use this code to get the name of the containing jar file
		myTeamName = exeDir.getName().replaceAll(".jar", "");

		//The path to the shared game file, passed to my program by the arbiter program
		sharedFilePath = args[0];

		//The minimum winning word length
		minWordLength = Integer.parseInt(args[1]);

		dictionary = new Dictionary(dictFilePath);

		playerMain = new GhostPlayerMain(sharedFilePath);

		SharedGameData currentGameData;

		System.out.println(myTeamName+" STARTED");
		
		playerMain.initGameManager(minWordLength, dictionary.getDictionary());

		while(true) {
			playerMain.update();

			if (playerMain.hasChanged()) {

				//We want to get the most recent version of the file
				currentGameData = playerMain.read();

				if(Objects.isNull(currentGameData)) return;

				if(playerMain.isGameOver(currentGameData)) {
					System.out.println(myTeamName+" TERMINATED");
					System.exit(0);
				}
				else if(playerMain.isMyTurn(myTeamName, currentGameData)) {
					//Update the current word fragment this is useful for when deciding the next letter to play
					updateWordFragment(currentGameData);

					GameAction gameAction = playerMain.onTurn(fragment);

					//Add the new game action to the current game shared data
					currentGameData.addAction(gameAction);

					//Save game shared data
					playerMain.write(currentGameData);
				}
			}
		}
	}

	public static void updateWordFragment(SharedGameData shareGameData) {

		fragment = "";

		shareGameData.gameActions().forEach(action -> {
			boolean isFront = action.getAddLocation().equals(AddLocation.Front);

			String letter = String.valueOf(action.getLetter());  

			fragment = isFront ? letter+fragment : fragment+letter;
		});
	}

	public GhostPlayerMain(String path) throws Exception {
		messageDigest = MessageDigest.getInstance("SHA-256");
		setFilePath(path);
		this.setPath(path);
	}

	public GameManager getGameManager() {
		return gameManager;
	}

	/**
	 * Updates the variables that correspond to the file being monitored
	 * <br>
	 * <br>
	 * This function has been throttled such that it will only update values every 250 ms. In other words successive calls to this 
	 * function in time intervals that are less than 250 ms will yield no change.  
	 * @throws IOException Thrown if any type of I/O exception occurs while writing to the file
	 * @throws IllegalStateException If the {@link #setFile(String)} method in not invoked with a proper file prior to invocation of this
	 * method. In other words if no file is currently set to be monitored. 
	 */
	public void update() throws IOException, IllegalStateException{

		if(nextUpateTime > System.currentTimeMillis()) {
			hasChanged = false;

			return;
		}

		nextUpateTime = System.currentTimeMillis() + 250;

		File file = new File(getFilePath());

		if(!file.exists()) return;

		try (DigestInputStream dis = new DigestInputStream(new FileInputStream(getFilePath()), messageDigest)) {
			while (dis.read() != -1) ;
			messageDigest = dis.getMessageDigest();
		}

		StringBuilder result = new StringBuilder();
		for (byte b : messageDigest.digest()) {
			result.append(String.format("%02x", b));
		}

		hasChanged =  currentCheckSum != result.toString().hashCode();

		currentCheckSum = result.toString().hashCode();
	}

	/**
	 * Tests if the file being monitored has changed since the last time {@link #update()} was invoked.
	 * @return true if and only if this monitor deems this file has changed. This will return false if the {@link #update()} method is not
	 * invoked prior to invocation of this method
	 */
	public boolean hasChanged(){
		return hasChanged;
	}

	public void setPath(String path) throws FileNotFoundException, IllegalArgumentException {
		File file = new File(path);

		if(file.isDirectory()) {
			throw new IllegalArgumentException();
		}

		this.setFilePath(path);
	}

	public String getPath() {
		try {
			return getFilePath();
		} catch (IllegalStateException e) {
			return null;
		}
	}

	public SharedGameData read() throws IOException, ClassNotFoundException, IllegalStateException {
		SharedGameData returnObject;

		// Reading the object from a file 
		FileInputStream file = null;
		ObjectInputStream in = null;

		try {
			file = new FileInputStream(getFilePath()); 
			in = new ObjectInputStream(file); 

			// Method for deserialization of object of type T
			returnObject = (SharedGameData)in.readObject();

			return returnObject;

		}catch (java.io.InvalidClassException e) {
			System.out.println("["+myTeamName+"] YOU ARE ATTEMPTING TO OPEN A FILE VERSION THAT IS NEWER THAN THIS PROGRAM CAN HANDLE. PLEASE UPDATE THIS PROGRAM WITH THE LATEST VERSION ON MOODLE!!!");
			throw e;
		}catch (EOFException | StreamCorruptedException e) {
			return null;			
		} finally{
			if(Objects.nonNull(in)) {
				in.close();
			}

			if(Objects.nonNull(file)) {
				file.close();
			}
		}

	}


	public void write(SharedGameData object) throws IOException, IllegalStateException {

		//Saving of object in a file 
		FileOutputStream file = null;
		ObjectOutputStream out = null;

		try {

			file = new FileOutputStream(getFilePath()); 
			out = new ObjectOutputStream(file); 

			// Method for serialization of object 
			out.writeObject(object);

		} finally {

			if(Objects.nonNull(out)) {
				out.close();
			}

			if(Objects.nonNull(file)) {
				file.close();
			}
		}  

	}

	public void setFilePath(String path) {
		this.path = path;
	}


	public String getFilePath() throws IllegalStateException {
		if(Objects.isNull(this.path)) {
			throw new IllegalStateException();
		}

		return this.path;
	}

	public boolean isGameOver(SharedGameData shareGameData) {
		return isGameStarted(shareGameData) && shareGameData.getGameState().isOver();
	}

	public boolean isGameStarted(SharedGameData shareGameData) {
		return Objects.nonNull(shareGameData);
	}

	private boolean isTurn(SharedGameData shareGameData) {
		return isGameStarted(shareGameData) && shareGameData.getGameState().isTurn();
	}


	public boolean isMyTurn(String myTeamName, SharedGameData shareGameData) {

		return 
				//Its someone's turn 
				isTurn(shareGameData) && 
				//The reciever of the state == my team name 
				shareGameData.getGameState().getReceiver().equalsIgnoreCase(myTeamName) &&
				//Check to make sure the current game action is empty
				(shareGameData.gameActions().isEmpty() || 
						//Check to make sure the last game action is not me (Make sure I haven't already played)
						!shareGameData.gameActions().get(shareGameData.gameActions().size()-1).getOwner().equalsIgnoreCase(myTeamName));

	}

	public boolean isOtherPlayersTurn(String myTeamName, SharedGameData shareGameData) {
		return isTurn(shareGameData) && !shareGameData.getGameState().getReceiver().equalsIgnoreCase(myTeamName);
	}

	public boolean isOtherPlayersTurnFinished(String myTeamName, SharedGameData shareGameData) {

		return 
				//Its someone's turn 
				isTurn(shareGameData) && 
				//The receiver of the state != my team name 
				shareGameData.getGameState().getReceiver().equalsIgnoreCase(myTeamName) &&
				//Check to make sure the current game action is not empty
				!shareGameData.gameActions().isEmpty() && 
				//Check to make sure the last game action is not me (Make sure they played)
				!shareGameData.gameActions().get(shareGameData.gameActions().size()-1).getOwner().equalsIgnoreCase(myTeamName);
	}

	public GameAction onTurn(String fragment) {

		TurnData turnData;

		try {
			turnData = gameManager.onTurn(fragment);

			Objects.requireNonNull(turnData, "onTurn() is returning a null value");

		} catch (Throwable e) {
			System.err.println("_______["+myTeamName+"] Error onTurn()_______");
			e.printStackTrace();
			throw e;
		}

		AddLocation location = turnData.isAddFront() ? AddLocation.Front : AddLocation.Back;

		GameAction action = new GameAction(location, turnData.getLetter(), myTeamName);

		return action;
	}
	
	public void initGameManager(int minWordLength, Set<String> allWords) {
		try {
			gameManager = new GameManager(minWordLength, allWords);
		} catch (Throwable e) {
			System.err.println("_______["+myTeamName+"]  GameManager Initialization Error_______");
			e.printStackTrace();
			throw e;
		}

	}

	public static class TurnData {

		private final char letter;
		private final boolean addFront;

		/**
		 * Creates a valid Turn Data object. If a null or non-alphanumeric (A-z or 0-9) value is used for a letter a random 
		 * char will be chosen. If a null value is used for addFront then a random value is chosen.
		 * @param letter a character that you would like to add to the word fragment.
		 * @param addFront The location the letter will be added to the word fragment, True = the letter will be added to the front, 
		 * False = the letter will be added to the back
		 * @return
		 */
		public static TurnData create(Character letter, Boolean addFront) {

			if(letter == null) {
				System.err.println("["+myTeamName+"] A null 'letter' value used at construction of the TurnData. A random character is use.");
				letter = getRandomLetter().charAt(0);
			}
			
			if(!Character.isAlphabetic(letter) && !Character.isDigit(letter)) {
				System.err.println("["+myTeamName+"] A 'letter' value that was not alphanumeric (A-z or 0-9) was used. The letter chosen was \""+letter+"\", it is not alphanumeric. A random character is use.");
				letter = getRandomLetter().charAt(0);
			}
			
			letter = Character.toLowerCase(letter);

			if(addFront == null) {
				System.err.println("["+myTeamName+"] A null 'addFront' value used at construction of the TurnData so the letter will be randomly added to front or back of the fragment.");
				addFront = getRandomLocation();
			}


			return new TurnData(letter, addFront);
		}

		private TurnData (char letter, boolean addFront) {

			this.letter = letter;

			this.addFront = addFront;
		}

		/**
		 * The letter to be played
		 * @return
		 */
		public char getLetter() {
			return letter;
		}

		/**
		 * The location the letter will be played:
		 * <br>
		 * true - add letter to front
		 * <br>
		 * false - add letter to the back
		 * @return
		 */
		public boolean isAddFront() {
			return addFront;
		}

		public static String getRandomLetter() {
			char c = (char)(ThreadLocalRandom.current().nextInt(26) + 'a');

			return String.valueOf(c);  
		}

		public static boolean getRandomLocation() {		
			return ThreadLocalRandom.current().nextBoolean();  
		}
	}

	public static class Dictionary {

		private Set<String> dictionary = new HashSet<>();

		public Dictionary(String fileDirectroy) throws IOException {
			File file = new File(fileDirectroy);

			FileReader fileReader = null;

			InputStreamReader inputStreamReader = null;

			InputStream inputStream = null;

			BufferedReader bufferedReader;

			try {
				bufferedReader = new BufferedReader(fileReader = new FileReader(file));
			}catch (FileNotFoundException e) {
				System.out.println("["+myTeamName+"]"+file+" Not found!");

				System.out.println("["+myTeamName+"] Searching for Dictionary internally...");

				inputStream = getClass().getResourceAsStream(INTERNAL_DICTIONARY_FILE_PATH);

				if(Objects.isNull(inputStream)) {
					inputStream = getClass().getResourceAsStream(INTERNAL_DICTIONARY_FILE_RELATIVE_PATH);
				}

				if(Objects.isNull(inputStream)) {
					System.err.println("["+myTeamName+"] Cannot find Dictionary externally or internally! Make sure the the file "+INTERNAL_DICTIONARY_FILE_PATH+" is in the same directory as your jar file.");
					System.exit(0);
				}else {
					System.out.println("["+myTeamName+"] Found dictionary internally");
				}

				inputStreamReader = new InputStreamReader(inputStream);

				bufferedReader = new BufferedReader(inputStreamReader);
			}

			String word;

			while ((word = bufferedReader.readLine()) != null) {
				dictionary.add(cleanWord(word));
			}

			if(Objects.nonNull(fileReader)) {
				fileReader.close();
			}

			if(Objects.nonNull(inputStreamReader)) {
				inputStreamReader.close();
			}

			if(Objects.nonNull(inputStream)) {
				inputStream.close();
			}

			bufferedReader.close();
		}

		public Set<String> getDictionary() {
			return new HashSet<>(dictionary);
		}

		private String cleanWord(String word) {
			return word.trim().toLowerCase();
		}

	}

}

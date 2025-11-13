import java.util.Scanner;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Collections;

public class Game {
    private Player player;
    private Scanner scanner;
    // Commands now accept the full list of processed words for flexible parsing
    private Map<String, Consumer<List<String>>> commands; 
    private Map<String, Room> worldMap;
    private Map<String, String> exitsMap;
    private static final List<String> NOISE_WORDS = Arrays.asList("a", "an", "the", "and", "then", "my");
    private static final List<String> PREPOSITIONS = Arrays.asList("on", "with", "in", "to");


    public static void main(String[] args) {
        Game game = new Game();
        game.play();
    }
    
    // ... (Game constructor and initialize methods are largely the same as before) ...
    public Game() {
        scanner = new Scanner(System.in);
        commands = new HashMap<>();
        worldMap = new HashMap<>();
        exitsMap = new HashMap<>();

        initializeCommands();
        initializeRoomsAndItems();
        
        player = new Player(worldMap.get("outside")); 
    }

    private void initializeCommands() {
        commands.put("go", this::handleGo);
        commands.put("take", this::handleTakeMulti);
        commands.put("get", this::handleTakeMulti);
        commands.put("drop", this::handleDropMulti);
        commands.put("look", this::handleLook);
        commands.put("inventory", this::handleInventory);
        commands.put("i", this::handleInventory);
        commands.put("use", this::handleUse); // Add the new 'use' command handler
    }

    private void initializeRoomsAndItems() {
        // ... (Room and Item initialization same as previous versions) ...
        Room outside = new Room("You are standing outside a dark cave entrance.");
        Room caveEntrance = new Room("You are in a dimly lit entrance hall. The air is cold.");
        Room treasureRoom = new Room("You have found the legendary treasure room! It's full of gold.");

        worldMap.put("outside", outside);
        worldMap.put("cave_entrance", caveEntrance);
        worldMap.put("treasure_room", treasureRoom);

        exitsMap.put("outside:north", "cave_entrance");
        exitsMap.put("cave_entrance:south", "outside");
        exitsMap.put("cave_entrance:north", "treasure_room");
        exitsMap.put("treasure_room:south", "cave_entrance");

        Item key = new Item("key", "A small, rusty iron key.");
        Item sword = new Item("sword", "A sharp, silver sword.");
        caveEntrance.addItem(key);
        treasureRoom.addItem(sword);
    }

    public void play() {
        System.out.println("Welcome to the Adventure Game!");
        printLocationInfo();

        while (true) {
            System.out.print("> ");
            String inputLine = scanner.nextLine();
            if (inputLine.equalsIgnoreCase("quit") || inputLine.equalsIgnoreCase("exit")) {
                System.out.println("Goodbye!");
                break;
            }
            // Use the single-command processor, as the multi-noun logic is within parseCommand
            parseCommand(inputLine);
        }
        scanner.close();
    }

    /**
     * Parses the user input into a single verb and a list of nouns, then executes the action.
     */
    public void parseCommand(String input) {
        String cleanInput = input.trim().toLowerCase();

        List<String> words = Arrays.stream(cleanInput.split(" "))
                .filter(word -> !word.isEmpty())
                .collect(Collectors.toList());

        if (words.isEmpty()) {
            System.out.println("Please enter a command.");
            return;
        }

        String commandWord = words.get(0);
        List<String> remainingWords = words.stream().skip(1).collect(Collectors.toList());

        Consumer<List<String>> action = commands.get(commandWord);

        if (action != null) {
            action.accept(remainingWords);
        } else {
            System.out.println("I don't know how to " + commandWord + ".");
        }
    }

    // --- Command Handler Methods ---

    // New handler for "use [item] on/with [object]"
    private void handleUse(List<String> words) {
        // Expected format: [item] [preposition] [object]
        if (words.size() < 3) {
            System.out.println("Use what on what? Try 'use [item] on [object]'.");
            return;
        }

        int prepIndex = -1;
        String preposition = null;

        // Find the index of the preposition (on, with, in)
        for (int i = 0; i < words.size(); i++) {
            if (PREPOSITIONS.contains(words.get(i))) {
                prepIndex = i;
                preposition = words.get(i);
                break;
            }
        }

        if (prepIndex == -1 || prepIndex == 0 || prepIndex == words.size() - 1) {
            System.out.println("Please specify a proper preposition and items/objects.");
            return;
        }

        // Extract the item name (before the preposition) and the target name (after the preposition)
        String itemName = String.join(" ", words.subList(0, prepIndex));
        String targetName = String.join(" ", words.subList(prepIndex + 1, words.size()));

        // Remove noise words from the parsed names
        itemName = Arrays.stream(itemName.split(" "))
                .filter(word -> !NOISE_WORDS.contains(word))
                .collect(Collectors.joining(" "));
        targetName = Arrays.stream(targetName.split(" "))
                .filter(word -> !NOISE_WORDS.contains(word))
                .collect(Collectors.joining(" "));


        // 1. Check if the player has the item
        Item itemInInventory = player.getItemFromInventory(itemName);
        if (itemInInventory == null) {
            System.out.println("You don't have the " + itemName + ".");
            return;
        }

        // 2. Check if the target object is in the room
        // For this simple example, we assume the target must be an item in the room. 
        // A real game would check doors, containers, NPCs, etc.
        Item targetInRoom = player.getCurrentRoom().getItem(targetName);
        if (targetInRoom == null) {
            System.out.println("You don't see a " + targetName + " here.");
            return;
        }

        // 3. Implement specific interaction logic (The core mechanic)
        if (itemName.equals("key") && targetName.equals("sword") && preposition.equals("on")) {
             System.out.println("You use the key on the sword. Nothing happens.");
        } else {
             System.out.println("You use the " + itemName + " " + preposition + " the " + targetName + ". It doesn't work.");
        }
    }
    
    // ... (handleGo, handleTakeMulti, handleDropMulti, handleLook, handleInventory, 
    //      getRoomIdByObject, printLocationInfo methods remain the same as the previous version) ...

    private void handleGo(List<String> directions) {
        // ... (Same as previous version) ...
    }
    private void handleTakeMulti(List<String> items) {
       // ... (Same as previous version) ...
    }
    private void handleDropMulti(List<String> items) {
       // ... (Same as previous version) ...
    }
    private void handleLook(List<String> objects) {
        // ... (Same as previous version) ...
    }
    private void handleInventory(List<String> objects) {
        System.out.println(player.getInventoryDescription());
    }
    private String getRoomIdByObject(Room targetRoom) {
        // ... (Same as previous version) ...
        for (Map.Entry<String, Room> entry : worldMap.entrySet()) {
            if (entry.getValue().equals(targetRoom)) {
                return entry.getKey();
            }
        }
        return null;
    }
    private void printLocationInfo() {
         // ... (Same as previous version) ...
        Room current = player.getCurrentRoom();
        String currentRoomId = getRoomIdByObject(current);
        System.out.println("\n" + current.getDescription());
        System.out.print("Exits: ");
        boolean foundExit = false;
        for (String key : exitsMap.keySet()) {
            if (key.startsWith(currentRoomId + ":")) {
                String direction = key.split(":");
                System.out.print(direction + " ");
                foundExit = true;
            }
        }
        if (!foundExit) System.out.print("none");
        System.out.println();
        System.out.print("Items in the room: ");
        if (current.getItems().isEmpty()) {
            System.out.println("none");
        } else {
            for (Item item : current.getItems()) {
                 System.out.print(item.getName() + " ");
            }
            System.out.println();
        }
    }
}

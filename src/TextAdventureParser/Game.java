package TextAdventureParser;

import java.util.Scanner;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Collections;

public class Game {
    private Player player;
    private Scanner scanner;
    private Map<String, Consumer<List<String>>> commands; // Change signature to accept List<String>
    private Map<String, Room> worldMap;
    private Map<String, String> exitsMap;
    private static final List<String> NOISE_WORDS = Arrays.asList("a", "an", "the", "and", "then", "my");
    private static final List<String> PREPOSITIONS = Arrays.asList("on", "with", "in", "to");


    public static void main(String[] args) {
        Game game = new Game();
        game.play();
    }

    public Game() {
        scanner = new Scanner(System.in);
        commands = new HashMap<>();
        worldMap = new HashMap<>();
        exitsMap = new HashMap<>();

        // Use the new Initialize class to set everything up
        Initialize.initializeCommands(commands, this); // Pass 'this' (the Game instance)
        String startRoomId = Initialize.initializeRoomsAndItems(worldMap, exitsMap);
        
        player = new Player(worldMap.get(startRoomId)); // Use the returned start room ID
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
            // Use the single-command processor, as the multi-noun logic is within parseCommand now
            parseCommand(inputLine);
        }
        scanner.close();
    }

    /**
     * Parses the user input into a single verb and a list of nouns, then executes the action.
     */
    public void parseCommand(String input) {
        String cleanInput = input.trim().toLowerCase();

        // Remove noise words before splitting into parts
        List<String> words = Arrays.stream(cleanInput.split(" "))
                .filter(word -> !NOISE_WORDS.contains(word) && !word.isEmpty())
                .collect(Collectors.toList());

        if (words.isEmpty()) {
            System.out.println("Please enter a command.");
            return;
        }

        String commandWord = words.get(0);
        List<String> nouns = words.stream().skip(1).collect(Collectors.toList());

        // Look up the command in our map
        Consumer<List<String>> action = commands.get(commandWord);

        if (action != null) {
            // Execute the associated function, passing the list of nouns
            action.accept(nouns);
        } else {
            System.out.println("I don't know how to " + commandWord + ".");
        }
    }

    // --- Helper and Command Handler Methods ---

    // Helper method to find a Room ID by its object reference
    private String getRoomIdByObject(Room targetRoom) {
        for (Map.Entry<String, Room> entry : worldMap.entrySet()) {
            if (entry.getValue().equals(targetRoom)) {
                return entry.getKey();
            }
        }
        return null;
    }
    
    // Helper method to print room info and exits from the exits map
    private void printLocationInfo() {
        // ... (this method remains identical to the previous version) ...
        Room current = player.getCurrentRoom();
        String currentRoomId = getRoomIdByObject(current);
        System.out.println("\n" + current.getDescription());

        System.out.print("Exits: ");
        boolean foundExit = false;
        for (String key : exitsMap.keySet()) {
            if (key.startsWith(currentRoomId + ":")) {
                String direction[] = key.split(":");
                System.out.print(direction[1] + " ");
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

    // Command Handlers (private, using Consumer<List<String>> signature)

    // New handler for "use [item] on/with [object]"
    public void handleUse(List<String> words) {
        if (words.size() < 3) {
            System.out.println("Use what on what? Try 'use [item] on [object]'.");
            return;
        }

        int prepIndex = -1;
        String preposition = null;

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

        String itemName = String.join(" ", words.subList(0, prepIndex));
        String targetName = String.join(" ", words.subList(prepIndex + 1, words.size()));

        itemName = Arrays.stream(itemName.split(" "))
                .filter(word -> !NOISE_WORDS.contains(word))
                .collect(Collectors.joining(" "));
        targetName = Arrays.stream(targetName.split(" "))
                .filter(word -> !NOISE_WORDS.contains(word))
                .collect(Collectors.joining(" "));

        Item itemInInventory = player.getItemFromInventory(itemName);
        if (itemInInventory == null) {
            System.out.println("You don't have the " + itemName + ".");
            return;
        }

        Item targetInRoom = player.getCurrentRoom().getItem(targetName);
        if (targetInRoom == null) {
            System.out.println("You don't see a " + targetName + " here.");
            return;
        }

        // Specific interaction logic for key and chest
        if (itemName.equals("key") && targetName.equals("chest") && preposition.equals("on")) {
            if (targetInRoom.isLocked()) {
                targetInRoom.setLocked(false);
                // Move the hidden item (lantern) from the chest's inventory to the room's inventory
                List<Item> chestContents = new ArrayList<>(targetInRoom.getInventory());
                for(Item content : chestContents) {
                    player.getCurrentRoom().addItem(content);
                    targetInRoom.removeItem(content);
                }
                System.out.println("You use the key on the chest. It clicks open! Inside you find a lantern.");
            } else {
                System.out.println("The chest is already unlocked.");
            }
        } else {
            System.out.println("You use the " + itemName + " " + preposition + " the " + targetName + ". It doesn't work.");
        }
    }
    
    // handleGo now expects a list of nouns, handles the first one
    public void handleGo(List<String> directions) {
        if (directions == null || directions.isEmpty()) {
            System.out.println("Go where? (north, south, etc.)");
            return;
        }
        String direction = directions.get(0); // Only use the first direction

        String currentRoomId = getRoomIdByObject(player.getCurrentRoom());
        String exitKey = currentRoomId + ":" + direction;

        if (exitsMap.containsKey(exitKey)) {
            String destinationRoomId = exitsMap.get(exitKey);
            Room destinationRoom = worldMap.get(destinationRoomId);
            
            if (destinationRoom != null) {
                player.setCurrentRoom(destinationRoom); 
                printLocationInfo();
            } else {
                System.out.println("Error: destination room not found in map data.");
            }
        } else {
            System.out.println("You can't go that way!");
        }
    }

    // New handler to process multiple items for the "take" verb
    public void handleTakeMulti(List<String> items) {
        if (items.isEmpty()) {
            System.out.println("Take what?");
            return;
        }
        for (String itemName : items) {
            Item itemToTake = player.getCurrentRoom().getItem(itemName);
            if (itemToTake != null) {
                player.getCurrentRoom().removeItem(itemToTake);
                player.addItem(itemToTake);
                System.out.println("You take the " + itemName + ".");
            } else {
                System.out.println("There is no " + itemName + " here.");
            }
        }
    }
    
    // New handler to process multiple items for the "drop" verb
    public void handleDropMulti(List<String> items) {
         if (items.isEmpty()) {
            System.out.println("Drop what?");
            return;
        }
        for (String itemName : items) {
            Item itemToDrop = player.getItemFromInventory(itemName);
            if (itemToDrop != null) {
                player.removeItem(itemToDrop);
                player.getCurrentRoom().addItem(itemToDrop);
                System.out.println("You drop the " + itemName + ".");
            } else {
                System.out.println("You don't have a " + itemName + " in your inventory.");
            }
        }
    }

    // handleLook now expects a list of nouns
    public void handleLook(List<String> objects) {
        if (objects == null || objects.isEmpty()) {
             printLocationInfo();
        } else {
            // A more advanced game would implement looking at specific objects here
            System.out.println("You look closely at the " + String.join(" ", objects) + ".");
        }
    }

    // handleInventory is still simple
    public void handleInventory(List<String> dummyHolder) {
        System.out.println(player.getInventoryDescription());
    }
}

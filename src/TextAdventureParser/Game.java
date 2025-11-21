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
import java.util.Optional;

public class Game {
    private Player player;
    private Scanner scanner;
//    private Map<String, Consumer<List<String>>> commands; // Change signature to accept List<String>
    private Map<String, Consumer<List<String>>> commands; 
    private Map<String, String> primaryCommands; // New field for primary commands/descriptions
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
        primaryCommands = new HashMap<>(); // Initialize the new map
        commands = new HashMap<>();
        worldMap = new HashMap<>();
        exitsMap = new HashMap<>();

        // Use the new Initialize class to set everything up
        Initialize.initializeCommands(commands, primaryCommands, this); // Pass 'this' (the Game instance)
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

    // Utility method to find an item by name or any of its aliases
    // Returns the item wrapped in Optional, or Optional.empty() if no unique match found
    private Optional<Item> findItemByNameOrAlias(String nameOrAlias) {
        // Clean the input name/alias by removing noise words
        List<String> cleanInputWords = Arrays.stream(nameOrAlias.toLowerCase().split(" "))
                .filter(word -> !NOISE_WORDS.contains(word) && !word.isEmpty())
                .collect(Collectors.toList());

        if (cleanInputWords.isEmpty()) {
            return Optional.empty(); // Cannot search for an empty string
        }

        List<Item> potentialMatches = new ArrayList<>();

        // Helper to check for matches in a given list of items
        Consumer<List<Item>> checkMatches = (itemList) -> {
            for (Item item : itemList) {
                // Check if ALL input words appear in the item's aliases
                boolean allWordsMatch = true;
                for (String word : cleanInputWords) {
                    // Check if any alias for the item contains the current input word
                    boolean wordFoundInAlias = item.getAliases().stream().anyMatch(alias -> alias.contains(word));
                    if (!wordFoundInAlias) {
                        allWordsMatch = false;
                        break;
                    }
                }
                if (allWordsMatch) {
                    potentialMatches.add(item);
                }
            }
        };

        // Check both inventory and room items
        checkMatches.accept(player.getItemInventoryList());
        checkMatches.accept(player.getCurrentRoom().getItems());

        // Handle ambiguity:
        if (potentialMatches.size() == 1) {
            return Optional.of(potentialMatches.get(0));
        } else if (potentialMatches.size() > 1) {
            // Ambiguous input, the calling method must handle this by printing clarification
            return Optional.empty(); 
        } else {
            // No match found
            return Optional.empty();
        }
    }

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

    /**
     * Handles the "use [item] on/with [target]" command structure using robust alias searching.
     */
    public void handleUse(List<String> words) {
        // Expected format: [item part 1] [preposition] [target part 2]
        if (words.size() < 3) {
            System.out.println("Use what on what? Try 'use [item] on [target]'.");
            return;
        }

        int prepIndex = -1;
        String preposition = null;

        // Find the index of the preposition (on, with, in, to)
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

        // Extract the raw item name part (before the preposition) 
        // and the raw target name part (after the preposition)
        String itemAlias = String.join(" ", words.subList(0, prepIndex));
        String targetAlias = String.join(" ", words.subList(prepIndex + 1, words.size()));

        // Use the robust helper method to find the actual item objects using the alias strings
        Optional<Item> itemInInventoryOpt = findItemByNameOrAlias(itemAlias);
        Optional<Item> targetInRoomOpt = findItemByNameOrAlias(targetAlias);

        // 1. Validate the source item is in inventory
        if (itemInInventoryOpt.isEmpty() || !player.getItemInventoryList().contains(itemInInventoryOpt.get())) {
            handleAmbiguityOrNoMatch(itemAlias);
            return;
        }
        Item itemInInventory = itemInInventoryOpt.get();


        // 2. Validate the target is in the room
        if (targetInRoomOpt.isEmpty() || !player.getCurrentRoom().getItems().contains(targetInRoomOpt.get())) {
            handleAmbiguityOrNoMatch(targetAlias);
            return;
        }
        Item targetInRoom = targetInRoomOpt.get();


        // 3. Implement specific interaction logic (The core mechanic)
        // Check if the actual item names match "key" and "chest", regardless of the aliases used
        if (itemInInventory.getName().equals("rusty key") && targetInRoom.getName().equals("chest")) {
            if (targetInRoom.isLocked()) {
                targetInRoom.setLocked(false);
                // Move the hidden item (lantern) from the chest's inventory to the room's inventory
                List<Item> chestContents = new ArrayList<>(targetInRoom.getInventory());
                for(Item content : chestContents) {
                    player.getCurrentRoom().addItem(content);
                    targetInRoom.removeItem(content);
                }
                System.out.println("You use the " + itemInInventory.getName() + " on the " + targetInRoom.getName() + ". It clicks open! Inside you find a lantern.");
            } else {
                System.out.println("The chest is already unlocked.");
            }
        } else {
             System.out.println("You use the " + itemInInventory.getName() + " " + preposition + " the " + targetInRoom.getName() + ". It doesn't work.");
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

    // Updated handler to process multiple items for the "take" verb, 
    // including context-aware single item pickup.
    public void handleTakeMulti(List<String> items) {
        // Create a copy of the list of user-provided item names/aliases
        List<String> itemsToProcess = new ArrayList<>(items);
        List<Item> roomItems = player.getCurrentRoom().getItems();
        String autoItemName = null;

        // If the player didn't specify an item name(s) (e.g., just typed "take")
        if (itemsToProcess.isEmpty()) {
            if (roomItems.size() == 1) {
                Item itemToTake = roomItems.get(0);
                itemsToProcess = Arrays.asList(itemToTake.getName());
                autoItemName = itemToTake.getName();
            } else if (roomItems.size() > 1) {
                System.out.println("Take what? There are multiple items here.");
                return;
            } else {
                System.out.println("There is nothing here to take.");
                return;
            }
        }
        
        // Process the list of items
        for (String itemNameOrAlias : itemsToProcess) {
            Item foundItem = null;
            // Search specifically within the current room's items using aliases
            // Use a copy of getItems() to avoid ConcurrentModificationException if we were using a fail-fast iterator
            List<Item> currentRoomItemsCopy = new ArrayList<>(player.getCurrentRoom().getItems());
            
            for (Item item : currentRoomItemsCopy) {
                // Check if the alias provided by the user matches any of the item's defined aliases
                if (item.getAliases().stream().anyMatch(alias -> alias.equalsIgnoreCase(itemNameOrAlias))) {
                    foundItem = item;
                    break;
                }
            }

            if (foundItem != null) {
                // If found in the room, move it to the player's inventory
                player.getCurrentRoom().removeItem(foundItem);
                player.addItem(foundItem);
                // Use the item's *primary name* for the message
                System.out.println("You take the " + foundItem.getName() + ".");
            } else {
                // If the item is no longer found in the room's current list, it might have been taken in 
                // a previous iteration. Only display an error if it's truly not in the entire game context (ambiguous or missing).
                if (findItemByNameOrAlias(itemNameOrAlias).isEmpty()) {
                    handleAmbiguityOrNoMatch(itemNameOrAlias);
                }
            }
        }
    }
    
    // New handler to process multiple items for the "drop" verb
	public void handleDropMulti(List<String> items) {
	    if (items.isEmpty()) { System.out.println("Drop what?"); return; }
	   for (String itemNameOrAlias : items) {
	        Optional<Item> itemOpt = findItemByNameOrAlias(itemNameOrAlias);
	        if (itemOpt.isPresent() && player.getItemFromInventory(itemOpt.get().getName()) != null) {
	            Item itemToDrop = itemOpt.get();
	            player.removeItem(itemToDrop);
	            player.getCurrentRoom().addItem(itemToDrop);
	            System.out.println("You drop the " + itemToDrop.getName() + ".");
	        } else {
	            handleAmbiguityOrNoMatch(itemNameOrAlias); // Use the helper
	        }
	   }
	}

    /**
     * Handles the 'examine' or 'x' command.
     */
    public void handleExamine(List<String> objects) {
        if (objects.isEmpty()) {
        	printLocationInfo(); // Look around the room
            return;
        }

        String targetName = String.join(" ", objects);
        Optional<Item> itemOpt = findItemByNameOrAlias(targetName);

        if (itemOpt.isPresent()) {
            Item item = itemOpt.get();
            System.out.println(item.getDescription());
            if (!item.getInventory().isEmpty() && !item.isLocked()) {
                 System.out.print("Inside you see: ");
                 item.getInventory().forEach(i -> System.out.print(i.getName() + " "));
                 System.out.println();
            }
        } else {
            // Handle ambiguity or no match
            handleAmbiguityOrNoMatch(targetName);
        }
    }
    
    // handleLook now expects a list of nouns
    public void handleLook(List<String> objects) {
        handleExamine(objects); // 'look' and 'examine' can share logic now
    }

    // handleInventory is still simple
    public void handleInventory(List<String> dummyHolder) {
        System.out.println(player.getInventoryDescription());
    }
    
    // Helper method to provide better feedback on ambiguous or unknown items
    private void handleAmbiguityOrNoMatch(String input) {
        // Re-run the search to determine if it was a total miss or an ambiguous match
        List<String> cleanInputWords = Arrays.stream(input.toLowerCase().split(" "))
                .filter(word -> !NOISE_WORDS.contains(word) && !word.isEmpty())
                .collect(Collectors.toList());
        
        List<Item> allAvailableItems = new ArrayList<>();
        allAvailableItems.addAll(player.getItemInventoryList());
        allAvailableItems.addAll(player.getCurrentRoom().getItems());
        
        List<Item> matches = allAvailableItems.stream().filter(item -> {
             return cleanInputWords.stream().allMatch(word -> item.getAliases().stream().anyMatch(alias -> alias.contains(word)));
        }).collect(Collectors.toList());

        if (matches.size() > 1) {
            System.out.print("Which one did you mean? ");
            matches.forEach(item -> System.out.print(item.getName() + " or "));
            System.out.println("?");
        } else {
            System.out.println("You don't see any \"" + input + "\" here or in your inventory.");
        }
    }

    /**
     * Handles the 'help' command, listing available actions from the table.
     */
    public void handleHelp(List<String> objects) {
        System.out.println("\nYou are playing a text adventure game.");
        System.out.println("Available commands:");
        // Iterate over the primary commands map to list available options
        primaryCommands.forEach((command, description) -> {
            System.out.printf("- %s: %s%n", command, description);
        });
    }

}

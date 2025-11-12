package TextAdventureParser;

import java.util.Scanner;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Game {
    private Player player;
    private Scanner scanner;
    private Map<String, Consumer<String>> commands;
    // Map to store all rooms, keyed by a unique String ID (e.g., "outside", "cave_entrance")
    private Map<String, Room> worldMap;
    // Map to store all exits, using a custom key like "roomId_direction" -> "destinationRoomId"
    private Map<String, String> exitsMap;


    public static void main(String[] args) {
        Game game = new Game();
        game.play();
    }

    public Game() {
        scanner = new Scanner(System.in);
        commands = new HashMap<>();
        worldMap = new HashMap<>();
        exitsMap = new HashMap<>();

        initializeCommands();
        initializeRoomsAndItems();
        
        // Start the player in the initial room ID
        player = new Player(worldMap.get("outside")); 
    }

    private void initializeCommands() {
        commands.put("go", this::handleGo);
        commands.put("take", this::handleTake);
        commands.put("get", this::handleTake);
        commands.put("drop", this::handleDrop);
        commands.put("look", this::handleLook);
        commands.put("inventory", this::handleInventory);
        commands.put("i", this::handleInventory);
    }

    private void initializeRoomsAndItems() {
        // Define all rooms and add them to the world map
        Room outside = new Room("You are standing outside a dark cave entrance.");
        Room caveEntrance = new Room("You are in a dimly lit entrance hall. The air is cold.");
        Room treasureRoom = new Room("You have found the legendary treasure room! It's full of gold.");

        worldMap.put("outside", outside);
        worldMap.put("cave_entrance", caveEntrance);
        worldMap.put("treasure_room", treasureRoom);

        // Define exits using the table-based approach:
        // Key format: "current_room_id:direction" -> Value: "destination_room_id"
        exitsMap.put("outside:north", "cave_entrance");
        exitsMap.put("cave_entrance:south", "outside");
        exitsMap.put("cave_entrance:north", "treasure_room");
        exitsMap.put("treasure_room:south", "cave_entrance");

        // Add items to rooms
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
            parseCommand(inputLine);
        }
        scanner.close();
    }

    // ... (parseCommand method is the same as the previous version) ...
    public void parseCommand(String input) {
        String cleanInput = input.trim().toLowerCase();
        String[] parts = cleanInput.split(" ");
        
        if (parts.length == 0 || parts[0].isEmpty()) {
            System.out.println("Please enter a command.");
            return;
        }

        String commandWord = parts[0];
        String object = (parts.length > 1) ? parts[1] : null;

        Consumer<String> action = commands.get(commandWord);

        if (action != null) {
            action.accept(object);
        } else {
            System.out.println("I don't know how to " + commandWord + ".");
        }
    }


    // --- Command Handler Methods ---

    private void handleGo(String direction) {
        if (direction == null) {
            System.out.println("Go where? (north, south, etc.)");
            return;
        }

        // We need the player's current room ID to check the exits map
        // This requires a minor update to the Player class (adding a getRoomId() method, see below)
        String currentRoomId = getRoomIdByObject(player.getCurrentRoom());
        String exitKey = currentRoomId + ":" + direction;

        if (exitsMap.containsKey(exitKey)) {
            String destinationRoomId = exitsMap.get(exitKey);
            Room destinationRoom = worldMap.get(destinationRoomId);
            
            if (destinationRoom != null) {
                player.setCurrentRoom(destinationRoom); // Player needs a setCurrentRoom method too
                printLocationInfo();
            } else {
                System.out.println("Error: destination room not found in map data.");
            }
        } else {
            System.out.println("You can't go that way!");
        }
    }

    // Helper method to find a Room ID by its object reference
    private String getRoomIdByObject(Room targetRoom) {
        for (Map.Entry<String, Room> entry : worldMap.entrySet()) {
            if (entry.getValue().equals(targetRoom)) {
                return entry.getKey();
            }
        }
        return null; // Should not happen in a working game
    }
    
    // Helper method to print room info and exits from the exits map
    private void printLocationInfo() {
        Room current = player.getCurrentRoom();
        String currentRoomId = getRoomIdByObject(current);
        System.out.println("\n" + current.getDescription());

        // Manually find exits from the exits map
        System.out.print("Exits: ");
        boolean foundExit = false;
        for (String key : exitsMap.keySet()) {
            if (key.startsWith(currentRoomId + ":")) {
                String direction = key.split(":")[1];
                System.out.print(direction + " ");
                foundExit = true;
            }
        }
        if (!foundExit) System.out.print("none");
        System.out.println();
        
        // Print items in the room
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


    private void handleTake(String itemName) {
        if (itemName == null) {
            System.out.println("Take what?");
            return;
        }
        Item itemToTake = player.getCurrentRoom().getItem(itemName);
        if (itemToTake != null) {
            player.getCurrentRoom().removeItem(itemToTake);
            player.addItem(itemToTake);
            System.out.println("You take the " + itemName + ".");
        } else {
            System.out.println("There is no " + itemName + " here.");
        }
    }

    private void handleDrop(String itemName) {
        if (itemName == null) {
            System.out.println("Drop what?");
            return;
        }

        Item itemToDrop = player.getItemFromInventory(itemName);
        if (itemToDrop != null) {
            player.removeItem(itemToDrop);
            player.getCurrentRoom().addItem(itemToDrop);
            System.out.println("You drop the " + itemName + ".");
        } else {
            System.out.println("You don't have a " + itemName + " in your inventory.");
        }
    }

    private void handleLook(String object) {
        printLocationInfo();
    }

    private void handleInventory(String object) {
        System.out.println(player.getInventoryDescription());
    }
}

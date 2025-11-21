package TextAdventureParser;


import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class Initialize {

    /**
     * Populates the command map with valid commands and their actions.
     * @param commands The map to populate.
     * @param game The Game instance to link command actions to.
     */

        // We link the commands back to the methods in the Game class instance
    public static void initializeCommands(Map<String, Consumer<List<String>>> commands, 
                   Map<String, String> primaryCommands, // New map parameter
                   Game game) {
/*
        commands.put("go", game::handleGo);
        commands.put("take", game::handleTakeMulti); // New multi-noun handler
        commands.put("drop", game::handleDropMulti); // New multi-noun handler
        commands.put("look", game::handleLook);
        commands.put("inventory", game::handleInventory);
        commands.put("use", game::handleUse);
        commands.put("examine", game::handleExamine); // Add examine command
        
        //examples of aliases
        commands.put("get", game::handleTakeMulti);  
        commands.put("pickup", game::handleTakeMulti); 
        commands.put("i", game::handleInventory); //Abbreviation for inventory
        commands.put("x", game::handleExamine); // Alias for examine

        // Add aliases for directions
        commands.put("north", nouns -> game.handleGo(Arrays.asList("north")));
        commands.put("n", nouns -> game.handleGo(Arrays.asList("north")));
        commands.put("south", nouns -> game.handleGo(Arrays.asList("south")));
        commands.put("s", nouns -> game.handleGo(Arrays.asList("south")));
        commands.put("east", nouns -> game.handleGo(Arrays.asList("east")));
        commands.put("e", nouns -> game.handleGo(Arrays.asList("east")));
        commands.put("west", nouns -> game.handleGo(Arrays.asList("west")));
        commands.put("w", nouns -> game.handleGo(Arrays.asList("west")));
        // You can add more like "up", "down", etc.
 */
        // Define primary commands and their descriptions
        primaryCommands.put("go", "Move in a direction (e.g., 'go north', 'n')");
        primaryCommands.put("take", "Pick up an item (e.g., 'take key', 'get all')");
        primaryCommands.put("drop", "Put down an item from inventory");
        primaryCommands.put("examine", "Look closely at something (e.g., 'examine chest', 'x key')");
        primaryCommands.put("look", "Look around the room");
        primaryCommands.put("inventory", "Check your inventory (or 'i')");
        primaryCommands.put("use", "Combine items (e.g., 'use key on chest')");
        primaryCommands.put("help", "Display this help message (or '?')");
        primaryCommands.put("quit", "Exit the game (or 'exit')");


        // Link all aliases back to the primary command handlers in the Game instance
        commands.put("go", game::handleGo);
        commands.put("take", game::handleTakeMulti);
        commands.put("get", game::handleTakeMulti);
        commands.put("drop", game::handleDropMulti);
        commands.put("look", game::handleLook);
        commands.put("examine", game::handleExamine);
        commands.put("x", game::handleExamine);
        commands.put("inventory", game::handleInventory);
        commands.put("i", game::handleInventory);
        commands.put("use", game::handleUse);
        commands.put("help", game::handleHelp);
        commands.put("?", game::handleHelp);
        commands.put("quit", null); // Handled explicitly in the game loop check
        commands.put("exit", null); // Handled explicitly in the game loop check

        // Direction aliases (These are technically single-word commands that use the 'go' handler)
        commands.put("north", nouns -> game.handleGo(Arrays.asList("north")));
        commands.put("n", nouns -> game.handleGo(Arrays.asList("north")));
        commands.put("south", nouns -> game.handleGo(Arrays.asList("south")));
        commands.put("s", nouns -> game.handleGo(Arrays.asList("south")));
        commands.put("east", nouns -> game.handleGo(Arrays.asList("east")));
        commands.put("e", nouns -> game.handleGo(Arrays.asList("east")));
        commands.put("west", nouns -> game.handleGo(Arrays.asList("west")));
        commands.put("w", nouns -> game.handleGo(Arrays.asList("west")));
    }


    /**
     * Creates rooms, items, links exits, and returns the starting room ID.
     * @param worldMap The map to store all Room objects.
     * @param exitsMap The map to store all exit connections.
     * @return The ID of the starting room.
     */
    public static String initializeRoomsAndItems(Map<String, Room> worldMap, Map<String, String> exitsMap) {
        // Define all rooms and add them to the world map
        Room outside = new Room("You are standing outside a dark cave entrance.");
        Room caveEntrance = new Room("You are in a dimly lit entrance hall. The air is cold.");
        Room treasureRoom = new Room("You have found the legendary treasure room! It's full of gold.");

        worldMap.put("outside", outside);
        worldMap.put("cave_entrance", caveEntrance);
        worldMap.put("treasure_room", treasureRoom);

        // Define exits using the table-based approach
        exitsMap.put("outside:north", "cave_entrance");
        exitsMap.put("cave_entrance:south", "outside");
        exitsMap.put("cave_entrance:north", "treasure_room");
        exitsMap.put("treasure_room:south", "cave_entrance");

        // Add items to rooms
        // Item(String name, String description, String... aliases)
        Item rustyKey = new Item("rusty key", "A small, rusty iron key.", "key", "iron key", "small key", "rusty");
        Item goldenKey = new Item("golden key", "A large, shiny golden key.", "key", "gold key", "shiny key", "golden");
        Item sword = new Item("sword", "A sharp, silver sword.", "silver sword", "sharp sword");
        Item chest = new Item("chest", "A heavy iron chest. It appears to be locked.", "iron chest", "heavy chest");
        Item lantern = new Item("lantern", "A dusty, old lantern.", "dusty lantern");
        Item shield = new Item("shield", "A sturdy, silver shield", "silver shield", "studry shield");
        
        // Add items to rooms
        caveEntrance.addItem(rustyKey);
        treasureRoom.addItem(sword);
        treasureRoom.addItem(chest);
        treasureRoom.addItem(goldenKey);
        treasureRoom.addItem(shield);
        
        // **New Logic:** Link the lantern to the chest for later
        chest.addItem(lantern);
        chest.setLocked(true);

        return "outside"; // Return the starting room ID
    }
}

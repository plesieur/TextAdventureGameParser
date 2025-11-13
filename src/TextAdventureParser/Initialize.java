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
    public static void initializeCommands(Map<String, Consumer<List<String>>> commands, Game game) {
        // We link the commands back to the methods in the Game class instance
        commands.put("go", game::handleGo);
        commands.put("take", game::handleTakeMulti); // New multi-noun handler
        commands.put("drop", game::handleDropMulti); // New multi-noun handler
        commands.put("look", game::handleLook);
        commands.put("inventory", game::handleInventory);
        commands.put("use", game::handleUse);
        
        //examples of aliases
        commands.put("get", game::handleTakeMulti);  
        commands.put("pickup", game::handleTakeMulti); 
        commands.put("i", game::handleInventory); //Abbreviation
        commands.put("north", game::handleGo);   //Abbreviation for 'go north'
        commands.put("n", game::handleGo); // Abbreviation for 'go north'     
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
        Item key = new Item("key", "A small, rusty iron key.");
        Item sword = new Item("sword", "A sharp, silver sword.");
        caveEntrance.addItem(key);
        treasureRoom.addItem(sword);
        
        return "outside"; // Return the starting room ID
    }
}

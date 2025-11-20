package TextAdventureParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class Player {
    private Room currentRoom;
    private List<Item> inventory;

    public Player(Room startRoom) {
        this.currentRoom = startRoom;
        this.inventory = new ArrayList<>();
    }

    public Room getCurrentRoom() {
        return currentRoom;
    }
    
    // New method to set the player's current room
    public void setCurrentRoom(Room newRoom) {
        this.currentRoom = newRoom;
    }

    public List<Item> getItemInventoryList() {
        return Collections.unmodifiableList(inventory);
    }

    // The move method is now handled entirely within the Game class's handleGo method
    // You can remove the old 'move' method from the Player class if you like, 
    // but the rest of the inventory logic remains the same.
    /*
    public boolean move(String direction) { 
        // This logic is now in Game.handleGo()
        return false;
    }
    */

    public void addItem(Item item) {
        inventory.add(item);
    }
    public void removeItem(Item item) {
        inventory.remove(item);
    }
    public Item getItemFromInventory(String itemName) {
        for (Item item : inventory) {
            if (item.getName().equalsIgnoreCase(itemName)) {
                return item;
            }
        }
        return null;
    }
    public String getInventoryDescription() {
        String invString = "Inventory: ";
        if (inventory.isEmpty()) {
            invString += "empty";
        } else {
            for (Item item : inventory) {
                invString += item.getName() + " ";
            }
        }
        return invString;
    }
}

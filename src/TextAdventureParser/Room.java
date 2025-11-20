package TextAdventureParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections; // Import Collections

public class Room {
    private String description;
    private List<Item> items;

    public Room(String description) {
        this.description = description;
        this.items = new ArrayList<>();
    }

    // ... (existing item methods: addItem, removeItem, getItem, etc.) ...
    public void addItem(Item item) {
        items.add(item);
    }
    public void removeItem(Item item) {
        items.remove(item);
    }
    public Item getItem(String itemName) {
        for (Item item : items) {
            if (item.getName().equalsIgnoreCase(itemName)) {
                return item;
            }
        }
        return null;
    }
    public List<Item> getItems() { // New getter for items
        return Collections.unmodifiableList(items); // Return an unmodifiable list
    }

    public String getDescription() { // New getter for description
        return description;
    }
    
    /**
     * Get the full description of the room, including available exits and items.
     * @return The room description, exits string, and item list.
     */
    public String getFullDescription() { 
        String exitString = "Exits: ";
        // Logic to build the exit string based on the exits map data (managed in Game.java now)
        // ... 

        String itemString = "Items in the room: ";
        // Logic to build the item string based on the items list
        // ...

        return description + "\n" + exitString + "\n" + itemString;
    }

}

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
}

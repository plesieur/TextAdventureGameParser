package TextAdventureParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;

public class Item {
    private String name;
    private String description;
    private List<Item> inventory; // A list to hold items inside this item
    private boolean locked; // A flag to indicate if this item is a locked container
    private List<String> aliases; // New list for synonyms/adjectives
    
    public Item(String name, String description, String... aliases) {
        this.name = name;
        this.description = description;
        this.inventory = new ArrayList<>();
        this.locked = false; // By default, items are not locked
        this.aliases = new ArrayList<>(Arrays.asList(aliases));
//        this.aliases = new ArrayList<>();        
        this.aliases.add(name.toLowerCase()); // Add the primary name as an alias
  }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    // New methods for locking/unlocking
    public void setLocked(boolean locked) {
        this.locked = locked;
    }
    public boolean isLocked() {
        return locked;
    }
    
    public List<String> getAliases() { return aliases; } // New getter

    // New methods for container functionality
    public void addItem(Item item) {
        inventory.add(item);
    }
    public void removeItem(Item item) {
        inventory.remove(item);
    }
    public List<Item> getInventory() {
        return Collections.unmodifiableList(inventory);
    }

    
    @Override
    public String toString() {
        // This is useful for printing the item directly
        return name + ": " + description;
    }
}

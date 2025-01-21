package majster2nn.dev.betonQuestQT.InventoryHandlers;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;
import java.util.function.Function;

public class InventoryButton {
    private Function<Player, ItemStack> iconCreator = player -> new ItemStack(Material.AIR); // Default creator
    private Consumer<InventoryClickEvent> eventConsumer = event -> {}; // Default consumer does nothing

    public InventoryButton creator(Function<Player, ItemStack> iconCreator) {
        this.iconCreator = iconCreator;
        return this;
    }

    public InventoryButton consumer(Consumer<InventoryClickEvent> eventConsumer) {
        this.eventConsumer = eventConsumer;
        return this;
    }

    public Consumer<InventoryClickEvent> getEventConsumer() {
        return this.eventConsumer;
    }

    public Function<Player, ItemStack> getIconCreator() {
        return this.iconCreator;
    }
}

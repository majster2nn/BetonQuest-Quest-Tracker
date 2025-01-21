package majster2nn.dev.betonQuestQT.InventoryHandlers;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.UUID;

public class HeadsHandlers {

    public static ItemStack getHead(String value) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);

        if (value == null || value.isEmpty()) {
            return head;  // Return the default head if no value is provided
        }

        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta == null) {
            return head;  // In case the meta is null for some reason, return the default head
        }

        // Use a placeholder name if the actual name is not necessary
        GameProfile profile = new GameProfile(UUID.randomUUID(), "Head");

        profile.getProperties().put("textures", new Property("textures", value));
        try {
            Field field = meta.getClass().getDeclaredField("profile");
            field.setAccessible(true);
            field.set(meta, profile);
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException ex) {
            ex.printStackTrace();
        }

        head.setItemMeta(meta);
        return head;
    }
}

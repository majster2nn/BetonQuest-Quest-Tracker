
package majster2nn.dev.betonQuestQT.menu_handlers;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;

import java.util.UUID;

public class HeadsHandlers {

    public static ItemStack getHead(String skinName) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();

        if (meta != null) {
            // Remplacer par un nom valide si null ou trop long
            if (skinName == null || skinName.isEmpty()) {
                skinName = "MHF_Question";
            }

            if (skinName.length() > 16) {
                skinName = skinName.substring(0, 16);
            }

            PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID(), skinName);
            meta.setOwnerProfile(profile);
            head.setItemMeta(meta);
        }

        return head;
    }
}

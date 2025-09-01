package majster2nn.dev.betonQuestQT.tracker.Menus.CupCodeMenus;

import fr.perrier.cupcodeapi.textdisplay.TextDisplayInstance;
import fr.perrier.cupcodeapi.textdisplay.builders.TextDisplayBuilder;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;

public class MainQuestHoverMenu {
    public static TextDisplayInstance questDisplay(Player player, Location loc){
        loc = loc.clone().add(player.getLocation().getDirection().setY(0).normalize().multiply(3));
        TextDisplayInstance questDisplay = new TextDisplayBuilder(loc, player)
                .setText(
                        "TESTING TESTING TESTING",
                        "TESTING TESTING TESTING",
                        "TESTING TESTING TESTING",
                        "TESTING TESTING TESTING",
                        "",
                        "",
                        "TESTING TESTING TESTING"
                )
                .setBillboard(Display.Billboard.FIXED)
                .setAlignment(TextDisplay.TextAlignment.CENTER)
                .setRotation(player.getYaw() - 180,0)
                .setBackgroundColor(Color.fromRGB(130, 50, 255))
                .addHoverButton("test", "Autism", "Autism",0.5f, 0.2f, 0.4f, 0.35f)
                .setExpirationTime(10)
                .build();

        questDisplay.onClick("test", event -> {
            event.getPlayer().sendMessage("Test successful");
        });

        return questDisplay;
    }
}

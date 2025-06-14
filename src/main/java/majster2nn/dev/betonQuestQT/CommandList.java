package majster2nn.dev.betonQuestQT;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.Commands;
import majster2nn.dev.betonQuestQT.Tracker.Menus.MainQuestMenu;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class CommandList {
    BetonQuestQT plugin;
    public List<LiteralCommandNode> commandBuilders(){
        List<LiteralCommandNode> commands = new ArrayList<>();

        commands.add(Commands.literal("questmenu")
                .executes(ctx -> {
                    plugin = BetonQuestQT.getInstance();
                    if (!(ctx.getSource().getSender() instanceof Player player)) {
                        ctx.getSource().getSender().sendMessage("You can only use this command in game as a player!");
                        return 0;
                    }

                    plugin.guiManager.openGui(new MainQuestMenu(plugin.getTranslation("main-menu", player)), player);
                    return 1;
                })
                .build()
        );

        commands.add(Commands.literal("bqqt")
                .requires(sender -> sender.getSender().hasPermission("bqqt.admin"))
                .then(Commands.literal("reload")
                        .executes(x -> {
                            plugin = BetonQuestQT.getInstance();
                            try{
                                plugin.reload();
                                return 1;
                            }catch (Exception e){
                                plugin.getLogger().severe(e.getMessage());
                                return 0;
                            }
                        }))
                .build()
        );

        return commands;
    }
}

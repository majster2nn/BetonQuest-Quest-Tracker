package majster2nn.dev.betonQuestQT;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.Commands;
import majster2nn.dev.betonQuestQT.tracker.menus.display.MainMenu;
import net.kyori.adventure.text.Component;
import org.betonquest.betonquest.BetonQuest;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class CommandList {
    public List<LiteralCommandNode> commandBuilders(){
        List<LiteralCommandNode> commands = new ArrayList<>();

        commands.add(Commands.literal("questmenu")
                .executes(ctx -> {
                    if (!(ctx.getSource().getSender() instanceof Player player)) {
                        ctx.getSource().getSender().sendMessage("You can only use this command in game as a player!");
                        return 0;
                    }
                    BetonQuestQT plugin = BetonQuestQT.getInstance();
                    plugin.guiManager.openGui(new MainMenu(plugin.getMenuTranslation("main-menu", player)), player);
                    return 1;
                })
                .build()
        );

        commands.add(Commands.literal("bqqt")
                .requires(sender -> sender.getSender().hasPermission("bqqt.admin"))
                .then(Commands.literal("reload")
                        .executes(x -> {
                            BetonQuest.getInstance().reload();
                            BetonQuestQT plugin = BetonQuestQT.getInstance();
                            try{
                                plugin.reload();
                                x.getSource().getSender().sendMessage("Plugin reloaded successfully!");
                                return 1;
                            }catch (Exception e){
                                plugin.getLogger().severe(e.getMessage());
                                x.getSource().getSender().sendMessage("Plugin reload failed! Check the console for errors and contact administrator.");
                                return 0;
                            }
                        }))
//                .then(Commands.literal("testMenu")
//                        .executes(x -> {
//                            if(!(x.getSource().getSender() instanceof Player player)) return 0;
//                            MainQuestHoverMenu.questDisplay(player, player.getLocation().clone().add(0, 0.7, 0));
//                            return 1;
//                        }))
                .then(Commands.literal("debug")
                        .executes(x -> {
                            BetonQuestQT.debug = !BetonQuestQT.debug;
                            x.getSource().getSender().sendMessage(Component.text("Debug mode set to: " + BetonQuestQT.debug));
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(Commands.literal("questStatus")
                                .then(Commands.argument("player", StringArgumentType.word())
                                        .suggests((x, builder) -> {
                                            List<String> players = new ArrayList<>();
                                            Bukkit.getOnlinePlayers().forEach(p -> players.add(p.getName()));
                                            players.stream()
                                                    .filter(entry -> entry.toLowerCase().startsWith(builder.getRemainingLowerCase()))
                                                    .forEach(builder::suggest);
                                            return builder.buildFuture();
                                        })
                                        .executes(ctx -> {
//                                            Player player = Bukkit.getPlayer(ctx.getArgument("player", String.class));
//                                            if(player != null){
//
//                                                StringBuilder messageRaw = new StringBuilder();
//                                                statuses.entrySet().forEach(k -> {
//                                                    messageRaw.append(k.getKey()).append(" ").append(k.getValue()).append("\n");
//                                                });
//                                                ctx.getSource().getSender().sendMessage(MiniMessage.miniMessage().deserialize(messageRaw.toString()));
//                                            }else{
//                                                ctx.getSource().getSender().sendMessage("Provided player is either offline or doesn't exist!!!");
//                                            }
                                            ctx.getSource().getSender().sendMessage("Currently this command is disabled. This will be fixed soon");
                                            //TODO readd the statuses command
                                            return Command.SINGLE_SUCCESS;
                                        }))))
                .then(Commands.literal("purge")
                        .then(Commands.argument("player", StringArgumentType.word())
                                .suggests((x, builder) -> {
                                    List<String> players = new ArrayList<>();
                                    Bukkit.getOnlinePlayers().forEach(p -> players.add(p.getName()));
                                    players.stream()
                                            .filter(entry -> entry.toLowerCase().startsWith(builder.getRemainingLowerCase()))
                                            .forEach(builder::suggest);
                                    return builder.buildFuture();
                                })
                                .executes(ctx -> {
                                    Player player = Bukkit.getPlayer(ctx.getArgument("player", String.class));
                                    if(player != null){
                                        ctx.getSource().getSender().sendMessage(Component.text("Successfully purged player " + player.getName() + "! (this command currently has co effect and will most likely be removed)"));
                                    }else{
                                        ctx.getSource().getSender().sendMessage("Provided player is either offline or doesn't exist!!!");
                                    }
                                    return Command.SINGLE_SUCCESS;
                                })))
                .build()
        );

        return commands;
    }
}

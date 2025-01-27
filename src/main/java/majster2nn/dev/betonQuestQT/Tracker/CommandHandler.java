package majster2nn.dev.betonQuestQT.Tracker;

import majster2nn.dev.betonQuestQT.BetonQuestQT;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandHandler implements CommandExecutor {
    BetonQuestQT plugin;

    public CommandHandler(BetonQuestQT bq){
        this.plugin = bq;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equalsIgnoreCase("questmenu")){
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players can use this command!");
                return true;
            }

            Player player = (Player) sender;

            // Open the GUI
            plugin.guiManager.openGui(new QuestMenu(plugin.getTranslation("header", player)), player);
            return true;
        }
        if(command.getName().equalsIgnoreCase("reload")){
            if(sender.hasPermission("bqqt.restart")) {
                BetonQuestQT.getInstance().reload();
                return true;
            }else{
                sender.sendMessage(NamedTextColor.RED + "You dont have permission to use this command!!!");
            }
        }
        return true;
    }
}

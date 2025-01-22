package majster2nn.dev.betonQuestQT;


import majster2nn.dev.betonQuestQT.InventoryHandlers.GUIListener;
import majster2nn.dev.betonQuestQT.InventoryHandlers.GUIManager;
import majster2nn.dev.betonQuestQT.Tracker.CommandHandler;
import majster2nn.dev.betonQuestQT.Tracker.QuestPlaceholder;
import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.utils.PlayerConverter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public final class BetonQuestQT extends JavaPlugin {
    public File config;
    public FileConfiguration configData;
    public double version = 0.2;
    public GUIManager guiManager;

    @Override
    public void onEnable() {
        if (Bukkit.getPluginManager().getPlugin("BetonQuest") == null){
            getLogger().warning("BetonQuest plugin not found. This plugin requires BetonQuest");
            getServer().getPluginManager().disablePlugin(this);
        }
        this.guiManager = new GUIManager();

        Bukkit.getPluginManager().registerEvents(new GUIListener(guiManager), this);

        CommandHandler commandHandler = new CommandHandler(this);

        getCommand("QuestMenu").setExecutor(commandHandler);

        setup();
        updateConfig();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static BetonQuestQT getInstance(){
        return getPlugin(BetonQuestQT.class);
    }

    private void setup() {
        config = new File(this.getDataFolder(), "config.yml");

        if (!config.exists()) {
            config.getParentFile().mkdirs();
            this.saveDefaultConfig();
        }

        configData = YamlConfiguration.loadConfiguration(config);

        try {
            double configVersion = getConfig().getDouble("version");

            if (configVersion != version) {
                getConfig().set("version", version);
                this.saveDefaultConfig();
                configData = YamlConfiguration.loadConfiguration(config);
            }
        } catch (Exception e) {
            try{
                getConfig().set("version", version);
                this.saveDefaultConfig();
                configData = YamlConfiguration.loadConfiguration(config);
            }catch (Exception err){
                err.printStackTrace();
            }
        }
    }
    public void updateConfig(){
        config = new File(this.getDataFolder(), "config.yml");
        configData = YamlConfiguration.loadConfiguration(config);
    }

    public @NotNull FileConfiguration getConfig() {
        return this.configData;
    }

    public String getTranslation(String part, Player player){
        return configData.getString(
                "menuTranslations." + part +
                        "." + BetonQuest.getInstance().getPlayerData(PlayerConverter.getID(player)).getLanguage());
    }
}

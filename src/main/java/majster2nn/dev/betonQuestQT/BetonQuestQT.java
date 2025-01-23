package majster2nn.dev.betonQuestQT;


import majster2nn.dev.betonQuestQT.Database.DataBaseManager;
import majster2nn.dev.betonQuestQT.Events.Events;
import majster2nn.dev.betonQuestQT.InventoryHandlers.GUIListener;
import majster2nn.dev.betonQuestQT.InventoryHandlers.GUIManager;
import majster2nn.dev.betonQuestQT.Tracker.BQEvents.ActiveQuestFactory;
import majster2nn.dev.betonQuestQT.Tracker.BQEvents.FinishQuestFactory;
import majster2nn.dev.betonQuestQT.Tracker.BQEvents.LockQuestFactory;
import majster2nn.dev.betonQuestQT.Tracker.CommandHandler;
import majster2nn.dev.betonQuestQT.Tracker.Placeholders.QuestStatus;
import majster2nn.dev.betonQuestQT.Tracker.QuestPlaceholder;
import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.api.logger.BetonQuestLoggerFactory;
import org.betonquest.betonquest.config.Config;
import org.betonquest.betonquest.utils.PlayerConverter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public final class BetonQuestQT extends JavaPlugin {
    public File config;
    public FileConfiguration configData;
    public double version = 0.2;
    public GUIManager guiManager;
    private BetonQuestLoggerFactory loggerFactory;

    @Override
    public void onLoad(){
        DataBaseManager.connectToDb();
    }

    @Override
    public void onEnable() {
        if (Bukkit.getPluginManager().getPlugin("BetonQuest") == null){
            getLogger().warning("BetonQuest plugin not found. This plugin requires BetonQuest");
            getServer().getPluginManager().disablePlugin(this);
        }
        this.guiManager = new GUIManager();

        BetonQuest betonQuest = BetonQuest.getInstance();
        this.loggerFactory = betonQuest.getLoggerFactory();

        registerEvents(betonQuest);

        new BukkitRunnable() {
            @Override
            public void run() {
                Config.getPackages().forEach((id, questPackage) -> {
                    QuestPlaceholder.packageByNameMap.clear();

                    if(!questPackage.getTemplates().contains("trackedQuest")){return;}
                    QuestPlaceholder.packageByNameMap.put(id, questPackage);
                });
            }
        }.runTaskTimer(this, 20*60*1, 0);


        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) { //
            new QuestStatus().register(); //
        }

        Bukkit.getPluginManager().registerEvents(new GUIListener(guiManager), this);
        Bukkit.getPluginManager().registerEvents(new Events(), this);

        CommandHandler commandHandler = new CommandHandler(this);

        getCommand("QuestMenu").setExecutor(commandHandler);

        setup();
        updateConfig();
    }

    @Override
    public void onDisable() {
        for(Player player : Bukkit.getOnlinePlayers()){
            Config.getPackages().forEach((id, questPackage) -> {
                DataBaseManager.setQuestPackage(id, player.getUniqueId().toString(), QuestPlaceholder.packageStatusesMap.get(player).get(questPackage));
            });
        }
        DataBaseManager.disconnectFromDB();
    }

    public void registerEvents(BetonQuest betonQuest){
        betonQuest.getQuestRegistries().getEventTypes().register("lockQuest", new LockQuestFactory(loggerFactory));
        betonQuest.getQuestRegistries().getEventTypes().register("activeQuest", new ActiveQuestFactory(loggerFactory));
        betonQuest.getQuestRegistries().getEventTypes().register("finishQuest", new FinishQuestFactory(loggerFactory));
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

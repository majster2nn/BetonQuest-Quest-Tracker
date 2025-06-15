package majster2nn.dev.betonQuestQT;

import majster2nn.dev.betonQuestQT.Database.DataBaseManager;
import majster2nn.dev.betonQuestQT.Events.Events;
import majster2nn.dev.betonQuestQT.InventoryHandlers.GUIListener;
import majster2nn.dev.betonQuestQT.InventoryHandlers.GUIManager;
import majster2nn.dev.betonQuestQT.Tracker.BQEvents.ActiveQuestFactory;
import majster2nn.dev.betonQuestQT.Tracker.BQEvents.FinishQuestFactory;
import majster2nn.dev.betonQuestQT.Tracker.BQEvents.HideQuestFactory;
import majster2nn.dev.betonQuestQT.Tracker.BQEvents.LockQuestFactory;
import majster2nn.dev.betonQuestQT.Tracker.Menus.ButtonVisualsStorage;
import majster2nn.dev.betonQuestQT.Tracker.Placeholders.QuestStatus;
import majster2nn.dev.betonQuestQT.Tracker.QuestPlaceholder;
import majster2nn.dev.betonQuestQT.Tracker.Statuses;
import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.api.logger.BetonQuestLoggerFactory;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class BetonQuestQT extends JavaPlugin {
    public File config;
    public FileConfiguration configData;
    public double version = 0.3;
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

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) { //
            new QuestStatus().register(); //
        }

        reload();

        Bukkit.getPluginManager().registerEvents(new GUIListener(guiManager), this);
        Bukkit.getPluginManager().registerEvents(new Events(), this);

        setup();
    }

    @Override
    public void onDisable() {
        for(Player player : Bukkit.getOnlinePlayers()){
            Map<String, List<String>> statusesMap = new HashMap<>();

            BetonQuest.getInstance().getPackages().forEach((id, questPackage) -> {
                if(!questPackage.getTemplates().contains("trackedQuest")){return;}

                Statuses status = QuestPlaceholder.packageStatusesMap.get(player).getOrDefault(questPackage, Statuses.HIDDEN);
                switch (status) {
                    case ACTIVE -> statusesMap.computeIfAbsent("activeQuests", x -> new ArrayList<>()).add(id);
                    case LOCKED -> statusesMap.computeIfAbsent("lockedQuests", x -> new ArrayList<>()).add(id);
                    case FINISHED -> statusesMap.computeIfAbsent("finishedQuests", x -> new ArrayList<>()).add(id);
                }
            });

            for(String key : statusesMap.keySet()){
                DataBaseManager.addColumnValueToUserTable(key, String.join(",", statusesMap.getOrDefault(key, new ArrayList<>())), player);
            }
        }
        DataBaseManager.disconnectFromDB();
    }

    public void reload(){
        updateConfig();
        resetQuestPackages();
        ButtonVisualsStorage.setButtonsMaterials();
    }

    public void resetQuestPackages(){
        QuestPlaceholder.packageByName.clear();
        QuestPlaceholder.packagesByCategory.clear();
        QuestPlaceholder.tags.clear();

        QuestPlaceholder.tags.addAll(getConfig().getStringList("filters"));

        BetonQuest.getInstance().getPackages().forEach((id, questPackage) -> {
            if(!questPackage.getTemplates().contains("trackedQuest")){return;}
            QuestPlaceholder.packageByName.put(id, questPackage);

            String questCategory = Utils.getSafeString(questPackage.getConfig(), "questParameters", "category");
            if (questCategory == null) {
                questCategory = "other";
            }

            switch(questCategory){
                case "main" -> QuestPlaceholder.packagesByCategory.put(questPackage, "main");
                case "side" -> QuestPlaceholder.packagesByCategory.put(questPackage, "side");
                case "other" -> QuestPlaceholder.packagesByCategory.put(questPackage, "other");
            }

            String questTags = Utils.getSafeString(questPackage.getConfig(), "questParameters", "tags");
            List<String> tags = questTags != null ? List.of(questTags.split(",")): new ArrayList<>();
            QuestPlaceholder.packagesTags.put(questPackage, tags);
        });

    }

    public void registerEvents(BetonQuest betonQuest){
        betonQuest.getQuestRegistries().event().register("lockQuest", new LockQuestFactory(loggerFactory));
        betonQuest.getQuestRegistries().event().register("activeQuest", new ActiveQuestFactory(loggerFactory));
        betonQuest.getQuestRegistries().event().register("finishQuest", new FinishQuestFactory(loggerFactory));
        betonQuest.getQuestRegistries().event().register("hideQuest", new HideQuestFactory(loggerFactory));
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

    public String getTranslation(String part, Player player) {
        String lang = "en-US";

        try {
            lang = BetonQuest.getInstance().getConfig().getString("language", "en-US");
        } catch (Exception ignored) {}

        String path = "menuTranslations." + part + "." + lang;

        String result = configData.getString(path);

        return (result != null && !result.isEmpty()) ? result : "???";
    }
}

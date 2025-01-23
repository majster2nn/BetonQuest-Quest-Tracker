package majster2nn.dev.betonQuestQT.Tracker.Placeholders;

import majster2nn.dev.betonQuestQT.Tracker.QuestPlaceholder;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.betonquest.betonquest.api.config.quest.QuestPackage;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class QuestStatus extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return "questStatus";
    }

    @Override
    public @NotNull String getAuthor() {
        return "majster2nn";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player.isOnline() && player instanceof Player) {
            return String.valueOf(QuestPlaceholder.packageStatusesMap.getOrDefault(player, new HashMap<>(){{
                put(QuestPlaceholder.packageByNameMap.get(params), QuestPlaceholder.Statuses.LOCKED);
            }}).get(QuestPlaceholder.packageByNameMap.get(params)));
        }
        return "0";
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        return String.valueOf(QuestPlaceholder.packageStatusesMap.get(player).get(QuestPlaceholder.packageByNameMap.get(params)));
    }
}

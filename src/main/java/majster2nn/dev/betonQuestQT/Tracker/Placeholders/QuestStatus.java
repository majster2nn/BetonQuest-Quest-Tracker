package majster2nn.dev.betonQuestQT.Tracker.Placeholders;

import majster2nn.dev.betonQuestQT.Tracker.QuestPlaceholder;
import majster2nn.dev.betonQuestQT.Tracker.Statuses;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
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
            return QuestPlaceholder.packageStatusesMap.getOrDefault(player.getPlayer(), new HashMap<>() {{
                put(QuestPlaceholder.packageByName.get(params), Statuses.LOCKED);
            }}).getOrDefault(QuestPlaceholder.packageByName.get(params), Statuses.LOCKED).toString();
        }
        return "0";
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        return QuestPlaceholder.packageStatusesMap.getOrDefault(player, new HashMap<>() {{
            put(QuestPlaceholder.packageByName.get(params), Statuses.LOCKED);
        }}).getOrDefault(QuestPlaceholder.packageByName.get(params), Statuses.LOCKED).toString();
    }
}

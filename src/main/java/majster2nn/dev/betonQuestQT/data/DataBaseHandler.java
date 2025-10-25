package majster2nn.dev.betonQuestQT.data;

import majster2nn.dev.betonQuestQT.data.asyncSaver.Record;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface DataBaseHandler {
    void init();
    void close();
    void saveToDb(@NotNull String tableId, List<Record> records);
    void removeFromDb(@NotNull String tableId, @NotNull String columnId, Object object);
    Object getFromDb(@NotNull String tableId, @NotNull String columnId, @NotNull String conditionColumnId, Object object);
}

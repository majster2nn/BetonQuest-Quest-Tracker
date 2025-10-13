package majster2nn.dev.betonQuestQT.data;

import majster2nn.dev.betonQuestQT.BetonQuestQT;
import majster2nn.dev.betonQuestQT.data.asyncSaver.Record;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SqliteManager implements DataBaseHandler{
    private static final BetonQuestQT plugin = BetonQuestQT.getInstance();
    private static final String DB_URL = "jdbc:sqlite:" + plugin.getDataFolder() + "/playerData.db";

    public Connection databaseConnection;
    private Statement stmt;

    public void createTables(){
        String sql ="CREATE TABLE IF NOT EXISTS userData (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "uuid STRING UNIQUE," +
                "username STRING," +
                "activeQuests STRING," +
                "lockedQuests STRING," +
                "finishedQuests STRING," +
                "currentlyActiveQuest STRING)";

        try{
            stmt.execute(sql);
        } catch (SQLException e) {
            plugin.getLogger().warning("Database table creation attempt FAILED!!! " + e.getMessage());
        }
    }
    public void updateAllTables() {
        updateTableColumns(databaseConnection, "userData", Map.ofEntries(
                Map.entry("uuid", "STRING UNIQUE"),
                Map.entry("username", "STRING"),
                Map.entry("activeQuests", "STRING"),
                Map.entry("lockedQuests", "STRING"),
                Map.entry("finishedQuests", "STRING"),
                Map.entry("currentlyActiveQuest", "STRING")
        ));
    }
    private void updateTableColumns(Connection conn, String tableName, Map<String, String> requiredColumns) {
        Set<String> existingColumns = new HashSet<>();

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("PRAGMA table_info(" + tableName + ")")) {
            while (rs.next()) {
                existingColumns.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to read table info for " + tableName + ": " + e.getMessage());
            return;
        }

        for (Map.Entry<String, String> column : requiredColumns.entrySet()) {
            if (!existingColumns.contains(column.getKey())) {
                String alterSQL = "ALTER TABLE " + tableName + " ADD COLUMN " + column.getKey() + " " + column.getValue() + ";";
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(alterSQL);
                    plugin.getLogger().info("[" + tableName + "] Added missing column: " + column.getKey());
                } catch (SQLException e) {
                    plugin.getLogger().warning("[" + tableName + "] Failed to add column " + column.getKey() + ": " + e.getMessage());
                }
            }
        }
    }

    @Override
    public void init() {
        try{
            databaseConnection = DriverManager.getConnection(DB_URL);
            stmt = databaseConnection.createStatement();

            createTables();
            updateAllTables();
            plugin.getComponentLogger().info(Component.text("Connected to SQLite database.", NamedTextColor.GREEN));
        }catch(SQLException e){
            plugin.getComponentLogger().error(Component.text("Database connection error!!! " + e.getMessage(), NamedTextColor.RED));
        }
    }

    @Override
    public void close() {
        try {
            if (stmt != null) stmt.close();
            if (databaseConnection != null) databaseConnection.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Database closing error!!! " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void saveToDb(@NotNull String tableId, List<Record> records) {
        System.out.println("SAVING INTO SQLITE");
        for (Record record : records) {
            System.out.println(record);
            // Build SQL per record, since column names are dynamic
            String sql = """
                        INSERT INTO %s (uuid, %s)
                        VALUES (?, ?)
                        ON CONFLICT(uuid) DO UPDATE SET %s = excluded.%s
                    """.formatted(tableId, record.getColumn(), record.getColumn(), record.getColumn());

            try (PreparedStatement ps = databaseConnection.prepareStatement(sql)) {
                ps.setString(1, record.getKey());
                ps.setString(2, record.getValue());
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void removeFromDb(@NotNull String tableId, @NotNull String columnId, Object object) {

    }

    @Override
    public Object getFromDb(@NotNull String tableId, @NotNull String columnId, @NotNull String conditionColumnId, Object object) {
        String sql = "SELECT %s FROM %s WHERE %s = ?".formatted(columnId, tableId, conditionColumnId);
        try(PreparedStatement ps = databaseConnection.prepareStatement(sql)) {

            ps.setObject(1, object);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getObject(columnId);
                }
            }
        }catch(SQLException e){
            throw new RuntimeException(e);
        }

        return "";
    }
}
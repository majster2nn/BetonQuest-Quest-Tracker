package majster2nn.dev.betonQuestQT.data;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import majster2nn.dev.betonQuestQT.BetonQuestQT;
import majster2nn.dev.betonQuestQT.data.asyncSaver.Record;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MySqlManager implements DataBaseHandler{
    private static HikariDataSource ds;

    @Override
    public void init(){
        HikariConfig config = new HikariConfig(BetonQuestQT.getInstance().getDataFolder() + "/dbConfig.properties");
        ds = new HikariDataSource(config);

        try(Connection con = ds.getConnection()) {
            createUserDataTable(con);

            Map<String, String> requiredColumns = Map.of(
                    "uuid", "CHAR(36) UNIQUE",
                    "username", "VARCHAR(16)",
                    "activeQuests", "TEXT",
                    "lockedQuests", "TEXT",
                    "finishedQuests", "TEXT",
                    "currentlyActiveQuest", "TEXT"
            );

            updateUserDataTableColumns(con, requiredColumns);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void createUserDataTable(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            String sql = """
                    CREATE TABLE IF NOT EXISTS userData (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        uuid CHAR(36) UNIQUE,
                        username VARCHAR(16),
                        activeQuests TEXT,
                        lockedQuests TEXT,
                        finishedQuests TEXT,
                        currentlyActiveQuest TEXT
                    );
                    """;


            stmt.execute(sql);
            BetonQuestQT.getInstance().getLogger().info("userData table ensured.");
        } catch (SQLException e) {
            BetonQuestQT.getInstance().getLogger().warning("Failed to create userData table: " + e.getMessage());
        }
    }

    private static void updateUserDataTableColumns(Connection conn, Map<String, String> requiredColumns) {
        Set<String> existingColumns = new HashSet<>();

        // Get existing columns
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SHOW COLUMNS FROM userData")) {
            while (rs.next()) {
                existingColumns.add(rs.getString("Field"));
            }
        } catch (SQLException e) {
            BetonQuestQT.getInstance().getLogger().warning("Failed to read table info for userData: " + e.getMessage());
            return;
        }

        // Add missing columns
        for (Map.Entry<String, String> column : requiredColumns.entrySet()) {
            if (!existingColumns.contains(column.getKey())) {
                String alterSQL = "ALTER TABLE userData ADD COLUMN " + column.getKey() + " " + column.getValue();
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(alterSQL);
                    BetonQuestQT.getInstance().getLogger().info("[userData] Added missing column: " + column.getKey());
                } catch (SQLException e) {
                    BetonQuestQT.getInstance().getLogger().warning("[userData] Failed to add column " + column.getKey() + ": " + e.getMessage());
                }
            }
        }
    }

    @Override
    public void close() {
        ds.close();
    }

    @Override
    public void saveToDb(@NotNull String tableId, @NotNull List<Record> records) {
        try (Connection con = ds.getConnection()) {
            for (Record record : records) {
                String sql = """
                            INSERT INTO %s (uuid, %s)
                            VALUES (?, ?)
                            ON DUPLICATE KEY UPDATE
                            %s = VALUES(%s)
                        """.formatted(tableId, record.getColumn(), record.getColumn(), record.getColumn());

                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, record.getKey());
                    ps.setString(2, record.getValue());
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void removeFromDb(@NotNull String tableId, @NotNull String columnId, Object object){
        String sql = """
                        DELETE FROM %s WHERE %s = ?
                    """.formatted(tableId, columnId);

        try (Connection con = ds.getConnection(); PreparedStatement ps = con.prepareStatement(sql)){
            ps.setObject(1, object);
            ps.executeUpdate();
        }catch (SQLException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object getFromDb(@NotNull String tableId, @NotNull String columnId, @NotNull String conditionColumnId, Object object) {
        String sql = "SELECT %s FROM %s WHERE %s = ?".formatted(columnId, tableId, conditionColumnId);

        try (Connection con = ds.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setObject(1, object);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getObject(columnId);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return "";
    }
}

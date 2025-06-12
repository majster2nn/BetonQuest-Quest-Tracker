package majster2nn.dev.betonQuestQT.Database;

import majster2nn.dev.betonQuestQT.BetonQuestQT;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.*;

public class DataBaseManager {
    private static final BetonQuestQT plugin = BetonQuestQT.getInstance();
    private static final String DB_URL = "jdbc:sqlite:" + plugin.getDataFolder() + "/playerData.db";

    public static Connection databaseConnection;
    private static Statement stmt;

    public static void connectToDb(){
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
    public static void createTables(){
        String sql ="CREATE TABLE IF NOT EXISTS userData (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "uuid STRING, UNIQUE" +
                "username STRING," +
                "activeQuests STRING," +
                "lockedQuests STRING," +
                "finishedQuests STRING)";

        String sql1 ="CREATE TABLE IF NOT EXISTS questData (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "questName STRING UNIQUE)";

        try{
            stmt.execute(sql);
            stmt.execute(sql1);
        } catch (SQLException e) {
            plugin.getLogger().warning("Database table creation attempt FAILED!!! " + e.getMessage());
        }
    }
    public static void updateAllTables() {
        updateTableColumns(databaseConnection, "userData", Map.ofEntries(
                Map.entry("uuid", "STRING UNIQUE"),
                Map.entry("username", "STRING"),
                Map.entry("activeQuests", "STRING"),
                Map.entry("lockedQuests", "STRING"),
                Map.entry("finishedQuests", "STRING")
        ));

        updateTableColumns(databaseConnection, "questData", Map.ofEntries(
                Map.entry("questName", "STRING UNIQUE"),
                Map.entry("questId", "STRING UINIQUE")
        ));

    }
    private static void updateTableColumns(Connection conn, String tableName, Map<String, String> requiredColumns) {
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

    public static void addColumnValueToUserTable(String column, String value, Player player) {
        try {
            String uuid = player.getUniqueId().toString();
            String sql = "INSERT INTO userData (uuid, " + column + ") " +
                    "VALUES (?, ?) " +
                    "ON CONFLICT(uuid) DO UPDATE SET " + column + " = excluded." + column;

            PreparedStatement ps = databaseConnection.prepareStatement(sql);
            ps.setString(1, uuid);
            ps.setString(2, value);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public static String getValueOfCellInUserTable(String data, Player player) {
        String uuid = player.getUniqueId().toString();
        try {
            ResultSet rs = stmt.executeQuery("SELECT " + data + " FROM userData WHERE UUID = '" + uuid + "'");
            if (rs.next()) {
                String value = rs.getString(1);
                return value != null ? value : "";
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Error retrieving column from database: " + e.getMessage());
        }
        return "";
    }

    public static void addColumnValueToQuestTable(String column, String value, String questName) {
        try {
            String sql = "INSERT INTO questData (questName, " + column + ") " +
                    "VALUES (?, ?) " +
                    "ON CONFLICT(questName) DO UPDATE SET " + column + " = excluded." + column;

            PreparedStatement ps = databaseConnection.prepareStatement(sql);
            ps.setString(1, questName);
            ps.setString(2, value);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public static String getValueOfCellInQuestTable(String data, String questName){
        try {
            ResultSet rs = stmt.executeQuery("SELECT " + data + " FROM questData WHERE questName = '" + questName + "'");

            if(rs.next()){
                String value = rs.getString(1);
                return value != null ? value : "";
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Error retrieving column from database" + e.getMessage());
        }
        return "";
    }

    public static void disconnectFromDB(){
        try {
            if (stmt != null) stmt.close();
            if (databaseConnection != null) databaseConnection.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Database closing error!!! " + e.getMessage());
            e.printStackTrace();
        }
    }
}
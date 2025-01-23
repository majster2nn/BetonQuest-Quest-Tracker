package majster2nn.dev.betonQuestQT.Database;

import majster2nn.dev.betonQuestQT.BetonQuestQT;
import majster2nn.dev.betonQuestQT.Tracker.QuestPlaceholder.*;
import net.md_5.bungee.api.ChatColor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataBaseManager {
    private static final String DB_URL = "jdbc:sqlite:" + BetonQuestQT.getInstance().getDataFolder() + "/playerData.db";

    public static Connection databaseConnection;
    private static Statement stmt;
    private static final String userDataTable = "userData";
    private static final List<String> registeredColumns = new ArrayList<>();


    public static void connectToDb(){
        try{
            databaseConnection = DriverManager.getConnection(DB_URL);
            stmt = databaseConnection.createStatement();


            ResultSet rs = stmt.executeQuery("PRAGMA table_info(" + userDataTable + ")");
            while (rs.next()) {
                registeredColumns.add(rs.getString("name"));
            }
            System.out.println(registeredColumns);
            createTables();
            BetonQuestQT.getInstance().getLogger().info(ChatColor.GREEN + "Connected to SQLite database.");
        }catch(SQLException e){
            BetonQuestQT.getInstance().getLogger().severe("Database connection error!!! " + e.getMessage());
        }
    }
    public static void createTables(){
        String sql = "CREATE TABLE IF NOT EXISTS " + userDataTable + " (id INTEGER PRIMARY KEY AUTOINCREMENT)";
        try{
            stmt.execute(sql);
            createColumns();
        } catch (SQLException e) {
            BetonQuestQT.getInstance().getLogger().warning("Database table creation attempt FAILED!!! " + e.getMessage());
        }
    }

    public static void createColumns(){
        for(DataBaseColumn column : DataBaseColumn.values()){
            createColumn(column.name(), column.getColumnType());
        }
    }

    public static void createColumn(String columnName, String type){
        if(!registeredColumns.contains(columnName)) {
            try {
                stmt.execute("ALTER TABLE " + userDataTable + " ADD COLUMN " + columnName + " " + type);
            } catch (SQLException e) {
                BetonQuestQT.getInstance().getLogger().warning("Something went wrong while adding column to database!!! " + e.getMessage());
            }
        }
    }

    public static void setQuestPackage(String questName, String uuid, Statuses status){
        try {
            ResultSet rs = stmt.executeQuery("SELECT * FROM " + userDataTable + " WHERE UUID = '" + uuid + "' AND QUEST = '" + questName + "'");
            if (!rs.next()) {
                stmt.execute("INSERT INTO " + userDataTable + " (UUID, QUEST, STATUS) VALUES ('" + uuid + "', '" + questName + "', '" + status + "')");
            }

            stmt.execute("UPDATE " + userDataTable + " SET STATUS = '" + status.toString() + "' WHERE UUID = '" + uuid + "' AND QUEST = '" + questName + "'");
            System.out.println(questName + "  jajo");
        } catch (SQLException e) {
            BetonQuestQT.getInstance().getLogger().severe(e.getMessage());
        }
    }

    public static String getQuestStatus(String uuid, String packageId){
        String status = "LOCKED";
        try {
            ResultSet rs = stmt.executeQuery("SELECT STATUS FROM " + userDataTable + " WHERE UUID = '" + uuid + "'" + " AND QUEST = '" + packageId + "'");
            status = rs.getString("STATUS");
        } catch (SQLException ignored){

        }
        return status;
    }

    public static void disconnectFromDB(){
        try {
            if (stmt != null) stmt.close();
            if (databaseConnection != null) databaseConnection.close();
        } catch (SQLException e) {
            BetonQuestQT.getInstance().getLogger().severe("Database closing error!!! " + e.getMessage());
            e.printStackTrace();  // Optional: prints the stack trace for debugging
        }
    }
}
package de.Main.database;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

public class DBM  {
    private  final Gson gson = new Gson();
    private final SQLConnection connection;
    private  SQLTable table;


    public DBM(SQLConnection connection, String tablename , HashMap<String, SQLDataType> userdatacolumns) {
        table = new SQLTable(connection, tablename, userdatacolumns);
        this.connection = connection;
    }

    public void onPlayerJoin(PlayerJoinEvent event, String tableName, HashMap<String, Object> defaultValues) {
        Player player = event.getPlayer();
        String playerUUID = player.getUniqueId().toString();

        SQLTable.Condition userdatacondition = new SQLTable.Condition("owner_uuid", playerUUID);

        if (!this.table.exits(tableName, userdatacondition)) {
            table.insert(tableName, defaultValues);
        }
    }




    public List<UUID> getAllUUIDs(String tableName, String uuidColumn) {
        List<UUID> uuids = new ArrayList<>();
        List<Object> uuidObjects = table.getAllValues(tableName, uuidColumn);
        for (Object obj : uuidObjects) {
            if (obj != null) {
                try {
                    uuids.add(UUID.fromString(obj.toString()));
                } catch (IllegalArgumentException ignored) {}
            }
        }
        return uuids;
    }


    public void insertDefaultValues(String tableName, Object playerUUID , HashMap<String, Object> defaultValues){
        SQLTable.Condition userdatacondition = new SQLTable.Condition("owner_uuid", String.valueOf(playerUUID));

        if (!table.exits(tableName, userdatacondition)) {
            table.insert(tableName, defaultValues);
        } else {
            for (Map.Entry<String, Object> entry : defaultValues.entrySet()) {
                String column = entry.getKey();
                Object value = entry.getValue();
                table.set(tableName, column, value, userdatacondition);
            }
        }
    }

    public  void setInt(String table, Object uuid, String columnName, int value) {
        SQLTable.Condition cond = new SQLTable.Condition("owner_uuid", uuid.toString());
        this.table.set(table, columnName, value, cond);
    }

    public  void setDouble(String table, Object uuid, String columnName, double value) {
        SQLTable.Condition cond = new SQLTable.Condition("owner_uuid", uuid.toString());
        this.table.set(table, columnName, value, cond);
    }

    public void remove(String table, Object uuid) {
        SQLTable.Condition condition = new SQLTable.Condition("owner_uuid", uuid.toString());
        this.table.remove(table, condition);
    }




    public void setString(String table, Object uuid, String columnName, String value) {
        SQLTable.Condition cond = new SQLTable.Condition("owner_uuid", uuid.toString());
        this.table.set(table, columnName, value, cond);
    }

    public  void setBoolean(String table, Object uuid, String columnName, boolean value) {
        SQLTable.Condition cond = new SQLTable.Condition("owner_uuid", uuid.toString());
        this.table.set(table, columnName, value, cond);
    }

    public  void setList(String table, Object uuid, String columnName, List<String> list) {
        SQLTable.Condition cond = new SQLTable.Condition("owner_uuid", uuid.toString());
        String csv = String.join(",", list);
        this.table.set(table, columnName, csv, cond);
    }

    public  String getString(String table, Object uuid, String columnName, String defaultValue) {
        SQLTable.Condition condition = new SQLTable.Condition("owner_uuid", uuid.toString());
        if (this.table.exits(table, condition)) {
            return this.table.getString(table, columnName, condition);
        }
        return defaultValue;
    }

    public  int getInt(String table, Object uuid, String columnName, int defaultValue) {
        SQLTable.Condition condition = new SQLTable.Condition("owner_uuid", uuid.toString());
        if (this.table.exits(table, condition)) {
            return this.table.getInt(table, columnName, condition);
        }
        return defaultValue;
    }

    public double getDouble(String table, Object uuid, String columnName, double defaultValue) {
        SQLTable.Condition condition = new SQLTable.Condition("owner_uuid", uuid.toString());
        if (this.table.exits(table, condition)) {
            return this.table.getDouble(table, columnName, condition);
        }
        return defaultValue;
    }




    public  boolean getBoolean(String table, Object uuid, String columnName, boolean defaultValue) {
        SQLTable.Condition condition = new SQLTable.Condition("owner_uuid", uuid.toString());
        if (this.table.exits(table, condition)) {
            return this.table.getBoolean(table, columnName, condition);
        }
        return defaultValue;
    }

    public  List<String> getList(UUID ownerUUID, String key, List<String> defaultList) {
        String json = getJsonFromDB(ownerUUID, key);
        if (json == null || json.isEmpty()) {
            return defaultList;
        }

        try {
            JsonElement element = JsonParser.parseString(json);
            if (!element.isJsonArray()) {
                return defaultList;
            }
            return gson.fromJson(json, new TypeToken<List<String>>() {
            }.getType());
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            return defaultList;
        }
    }

    private  String getJsonFromDB(UUID ownerUUID, String key) {

        return null;
    }

    public  UUID getUUID(String table, UUID userUUID, String key, UUID defaultValue) {
        try {
            String uuidString = getString(table, userUUID, key, null);
            if (uuidString != null && !uuidString.isEmpty()) {
                return UUID.fromString(uuidString);
            }
        } catch (IllegalArgumentException e) {
            Bukkit.getLogger().warning("Ungültige UUID für " + key + " von " + userUUID + ": " + e.getMessage());
        }
        return defaultValue;
    }

    public  List<String> getStringList(String table, UUID uuid, String column) {
        String listAsString = getString(table, uuid, column, ""); // Liste als CSV-String abrufen  (in komma umgewandlete Strings )
        if (listAsString.isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.asList(listAsString.split(",")); // CSV-String in Liste umwandeln  (in komma umgewandlete Strings )
    }

    public  void setStringList(String table, UUID uuid, String column, List<String> values) {
        String listAsString = String.join(",", values); // Liste in CSV-String umwandeln (in komma umgewandlete Strings )
        setString(table, uuid, column, listAsString); //In die Datenbank
    }

    public  int getTotalBlocks() {
        int totalBlocks = -1;
        String sql = "SELECT TotalBlocks FROM userdata LIMIT 1";

        try (PreparedStatement ps = this.connection.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                totalBlocks = rs.getInt("TotalBlocks");
            }

        } catch (SQLException e) {
           Bukkit.getLogger().log(Level.SEVERE, "Fehler beim Abrufen von TotalBlocks", e);
        }

        return totalBlocks;
    }


}
package de.Main.database;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

public class DBM {
    private final Gson gson = new Gson();
    private final SQLConnection connection;
    private SQLTable table;


    public DBM(SQLConnection connection, String tablename, HashMap<String, SQLDataType> userdatacolumns) {
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
        List<Object> uuidObjects = Collections.singletonList(table.getAllValues(tableName, uuidColumn));
        for (Object obj : uuidObjects) {
            if (obj != null) {
                try {
                    uuids.add(UUID.fromString(obj.toString()));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        return uuids;
    }

    public List<String> getAllValues(String tableName, String columnName) {
        List<String> values = new ArrayList<>();
        List<String> columnObjects = table.getAllValues(tableName, columnName);

        for (Object obj : columnObjects) {
            if (obj != null) {
                values.add(obj.toString());
            }
        }

        return values;
    }


    public List<String> getValuesLike(String table, String columnName, String search) {
        List<String> results = new ArrayList<>();

        if (!table.matches("[a-zA-Z0-9_]+") || !columnName.matches("[a-zA-Z0-9_]+")) {
            throw new IllegalArgumentException("Ungültiger Tabellen- oder Spaltenname!");
        }

        String sql = "SELECT " + columnName + " FROM " + table + " WHERE " + columnName + " LIKE ?";

        try (PreparedStatement ps = this.connection.getConnection().prepareStatement(sql)) {
            ps.setString(1, "%" + search + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String value = rs.getString(columnName);
                    if (value != null) {
                        results.add(value);
                    }
                }
            }

        } catch (SQLException e) {
            Bukkit.getLogger().severe("Fehler bei getValuesLike(" + table + "): " + e.getMessage());
            e.printStackTrace();
        }

        return results;
    }


    public UUID getUUIDByName(String table, String playerName) {
        String sql = "SELECT owner_uuid FROM " + table + " WHERE owner = ? LIMIT 1";

        try {
            UUID uuidResult;
            try (PreparedStatement ps = this.connection.getConnection().prepareStatement(sql)) {
                ps.setString(1, playerName);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return null;
                    }

                    String uuidString = rs.getString("owner_uuid");
                    uuidResult = UUID.fromString(uuidString);
                }
            }

            return uuidResult;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }


    public void insertDefaultValues(String tableName, Object playerUUID, HashMap<String, Object> defaultValues) {
        SQLTable.Condition userdatacondition = new SQLTable.Condition("owner_uuid", String.valueOf(playerUUID));

        if (!table.exits(tableName, userdatacondition)) {
            table.insert(tableName, defaultValues);
        }
    }

    public void insertDefaultValues(String tableName, String conditionKey,Object playerUUID, HashMap<String, Object> defaultValues) {
        SQLTable.Condition userdatacondition = new SQLTable.Condition(conditionKey, String.valueOf(playerUUID));

        if (!table.exits(tableName, userdatacondition)) {
            table.insert(tableName, defaultValues);
        }
    }

    public void setInt(String table, Object uuid, String columnName, int value) {
        SQLTable.Condition cond = new SQLTable.Condition("owner_uuid", uuid.toString());
        this.table.set(table, columnName, value, cond);
    }

    public void setDouble(String table, Object uuid, String columnName, double value) {
        SQLTable.Condition cond = new SQLTable.Condition("owner_uuid", uuid.toString());
        this.table.set(table, columnName, value, cond);
    }


    public void setDouble(String table, Object conditionKey, Object conditionValue ,String columnName, double value) {
        SQLTable.Condition cond = new SQLTable.Condition(conditionKey.toString(), conditionValue.toString());
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


    public void setLocation(String table, Object uuid, String columnName, Location location) {
        SQLTable.Condition cond = new SQLTable.Condition("owner_uuid", uuid.toString());
        this.table.set(table, columnName, location, cond);
    }

    public Location getLocation(String table, Object uuid, String columnName, Location defaultValue) {
        SQLTable.Condition condition = new SQLTable.Condition("owner_uuid", uuid.toString());
        if (this.table.exits(table, condition)) {
            return this.table.getLocation(table, columnName, condition);
        }
        return defaultValue;
    }

    public void setBoolean(String table, Object uuid, String columnName, boolean value) {
        SQLTable.Condition cond = new SQLTable.Condition("owner_uuid", uuid.toString());
        this.table.set(table, columnName, value, cond);
    }

    public void setList(String table, Object uuid, String columnName, List<String> list) {
        SQLTable.Condition cond = new SQLTable.Condition("owner_uuid", uuid.toString());
        String csv = String.join(",", list);
        this.table.set(table, columnName, csv, cond);
    }

    public String getString(String table, Object uuid, String columnName, String defaultValue) {
        SQLTable.Condition condition = new SQLTable.Condition("owner_uuid", uuid.toString());
        if (this.table.exits(table, condition)) {
            return this.table.getString(table, columnName, condition);
        }
        return defaultValue;
    }


    public String getString(String table, String conditionKey, Object conditionValue, String columnName, String defaultValue) {
        SQLTable.Condition condition = new SQLTable.Condition(conditionKey, Objects.requireNonNull(conditionValue).toString());
        if (this.table.exits(table, condition)) {
            return this.table.getString(table, columnName, condition);
        }
        return defaultValue;
    }

    public int getInt(String table, Object key, String columnName, int defaultValue) {
        SQLTable.Condition condition = new SQLTable.Condition("owner_uuid", key.toString());
        if (this.table.exits(table, condition)) {
            return this.table.getInt(table, columnName, condition);
        }
        return defaultValue;
    }

    public int getInt(String table, String conditionKey, Object conditionValue, String columnName, int defaultValue) {
        SQLTable.Condition condition = new SQLTable.Condition(conditionKey, conditionValue.toString());
        if (this.table.exits(table, condition)) {
            return this.table.getInt(table, columnName, condition);
        }
        return defaultValue;
    }


    public long getLong(String table, Object key, String columName, long defaultValue) {
        SQLTable.Condition condition = new SQLTable.Condition("owner_uuid", key.toString());
        return this.table.exits(table, condition) ? this.table.getLong(table, columName, condition) : defaultValue;
    }



    public double getDouble(String table, Object uuid, String columnName, double defaultValue) {
        SQLTable.Condition condition = new SQLTable.Condition("owner_uuid", uuid.toString());
        if (this.table.exits(table, condition)) {
            return this.table.getDouble(table, columnName, condition);
        }
        return defaultValue;
    }
    public double getDouble(String table, String conditionKey, Object conditionValue, String columnName, double defaultValue) {
        SQLTable.Condition condition = new SQLTable.Condition(conditionKey, conditionValue.toString());
        if (this.table.exits(table, condition)) {
            return this.table.getDouble(table, columnName, condition);
        }
        return defaultValue;
    }


    public boolean getBoolean(String table, Object uuid, String columnName, boolean defaultValue) {
        SQLTable.Condition condition = new SQLTable.Condition("owner_uuid", uuid.toString());
        if (this.table.exits(table, condition)) {
            return this.table.getBoolean(table, columnName, condition);
        }
        return defaultValue;
    }

    public boolean getBoolean(String table, String conditionKey, Object conditionValue, String columnName, boolean defaultValue) {
        SQLTable.Condition condition = new SQLTable.Condition(conditionKey, conditionValue.toString());
        if (this.table.exits(table, condition)) {
            return this.table.getBoolean(table, columnName, condition);
        }
        return defaultValue;
    }


    public List<Integer> getIntegerList(String tableName, UUID ownerUUID, String key) {
        List<Integer> defaultList = new ArrayList<>();
        String json = getJsonFromDB(tableName, ownerUUID, key);
        if (json == null || json.isEmpty()) return defaultList;

        try {
            JsonElement element = JsonParser.parseString(json);
            if (!element.isJsonArray()) return defaultList;
            return gson.fromJson(json, new TypeToken<List<Integer>>() {
            }.getType());
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            return defaultList;
        }
    }

    public void setIntegerList(String tableName, UUID ownerUUID, String key, List<Integer> list) {
        String json = gson.toJson(list);
        try (PreparedStatement stmt = this.connection.getConnection().prepareStatement(
                "INSERT INTO `" + tableName + "` (uuid, `" + key + "`) VALUES (?, ?) " +
                        "ON DUPLICATE KEY UPDATE `" + key + "` = ?")) {
            stmt.setString(1, ownerUUID.toString());
            stmt.setString(2, json);
            stmt.setString(3, json);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String getJsonFromDB(String tableName, UUID ownerUUID, String key) {
        try (PreparedStatement stmt = this.connection.getConnection().prepareStatement(
                "SELECT `" + key + "` FROM `" + tableName + "` WHERE owner_uuid = ?")) {
            stmt.setString(1, ownerUUID.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString(key);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public UUID getUUID(String table, UUID userUUID, String key, UUID defaultValue) {
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

    public List<String> getStringList(String table, UUID uuid, String column) {
        String listAsString = getString(table, uuid, column, "");
        if (listAsString.isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.asList(listAsString.split(","));
    }

    public void setStringList(String table, UUID uuid, String column, List<String> values) {
        String listAsString = String.join(",", values);
        setString(table, uuid, column, listAsString);
    }

    public int getTotalBlocks() {
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
package de.Main;

import de.Main.database.DBM;
import de.Main.database.SQLConnection;
import de.Main.database.SQLDataType;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;

public final class Main extends JavaPlugin {
    private static Main instance;
    private static String prefix;
    public static final String WORLD_NAME = "OneBlock";
    public static World oneBlockWorld;
    public static FileConfiguration config;
    SQLConnection connection;
    DBM moneyManager;
    private File marketfile;
    private FileConfiguration marketconfig;
    public static File marketDataFolder;


    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;

        // SQL-Verbindung
        connection = new SQLConnection("localhost", 3306, "admin", "admin", "1234");
        HashMap<String, SQLDataType> userdatacolumns = new HashMap<>();
        moneyManager = new DBM(this, connection, "OneBlock", userdatacolumns);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }


    public SQLConnection getConnection() {
        return connection;
    }

    public static Main getInstance() {
        return instance;
    }
}

package de.Main;

import de.Main.database.SQLConnection;
import org.bukkit.plugin.java.JavaPlugin;
public final class Main extends JavaPlugin {
    private static Main instance;
    SQLConnection connection;


    @Override
    public void onEnable() {
        instance = this;

        // SQL-Verbindung
        connection = new SQLConnection("localhost", 3306, "admin", "admin", "1234");
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

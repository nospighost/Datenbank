package de.Main.config;

import com.sun.tools.javac.Main;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ConfigManager {

    private static ConfigManager instance;
    private final JavaPlugin plugin;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public static ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager(JavaPlugin.getProvidingPlugin(ConfigManager.class));
        }
        return instance;
    }



    public void createUserData(Player player) {
        FileConfiguration config = createUserDataConfig(player.getUniqueId().toString());
        if (config == null) return;


        saveUserData(player, config);
    }

    public void deleteUserData(Player player) {
        File file = getUserDataFile(player.getUniqueId().toString());
        if (file.exists()) file.delete();
    }

    public void saveUserData(OfflinePlayer offlinePlayer, FileConfiguration config) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(offlinePlayer.getUniqueId()).getPlayer();
        File file = getUserDataFile(player.getUniqueId().toString());
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public FileConfiguration getUserDataConfig(OfflinePlayer offlinePlayer) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(offlinePlayer.getUniqueId());
        File file = getUserDataFile(Objects.requireNonNull(player.getUniqueId()).toString());
        if (!file.exists()) return null;
        return YamlConfiguration.loadConfiguration(file);
    }

    private File getUserDataFile(String uuid) {
        return new File(getUserDataFolder(), uuid + ".yml");
    }

    private FileConfiguration createUserDataConfig(String uuid) {
        File file = getUserDataFile(uuid);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Konnte Userdata-Datei nicht erstellen: " + uuid);
                e.printStackTrace();
                return null;
            }
        }
        return YamlConfiguration.loadConfiguration(file);
    }



    private File getCasesFolder() {
        File folder = new File(plugin.getDataFolder(), "cases");
        if (!folder.exists() && !folder.mkdirs()) {
            plugin.getLogger().severe("Konnte 'cases'-Ordner nicht erstellen!");
        }
        return folder;
    }

    private File getUserDataFolder() {
        File folder = new File(plugin.getDataFolder(), "userdata");
        if (!folder.exists() && !folder.mkdirs()) {
            plugin.getLogger().severe("Konnte 'userdata'-Ordner nicht erstellen!");
        }
        return folder;
    }

    private File getConfigFile(String name) {
        return new File(getCasesFolder(), name + ".yml");
    }

    public FileConfiguration createConfig(String name) {
        File file = getConfigFile(name);

        if (file.exists()) {
            return YamlConfiguration.loadConfiguration(file);
        }

        try {
            file.createNewFile();
        } catch (IOException e) {
            plugin.getLogger().severe("Konnte Config-Datei nicht erstellen: " + name);
            e.printStackTrace();
            return null;
        }

        return YamlConfiguration.loadConfiguration(file);
    }

    public FileConfiguration getConfig(String name) {

        File file = getConfigFile(name);

        if (!file.exists()) {
            plugin.getLogger().warning("Config '" + name + "' existiert nicht!");
            return null;
        }

        return YamlConfiguration.loadConfiguration(file);
    }

    public boolean saveConfig(String name, FileConfiguration config) {
        File file = getConfigFile(name);

        try {
            config.save(file);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public FileConfiguration reloadConfig(String name) {
        File file = getConfigFile(name);
        if (!file.exists()) {
            return null;
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    public boolean deleteConfig(String name) {
        File file = getConfigFile(name);
        if (!file.exists()) {
            return false;
        }

        if (file.delete()) {
            return true;
        } else {
            plugin.getLogger().severe("Konnte Config '" + name + "' nicht löschen!");
            return false;
        }
    }

    public boolean configExists(String name) {
        return getConfigFile(name).exists();
    }

    public List<String> getAllConfigs() {
        List<String> list = new ArrayList<>();
        File folder = getCasesFolder();
        File[] files = folder.listFiles((dir, n) -> n.toLowerCase().endsWith(".yml"));

        if (files != null) {
            for (File file : files) {
                String fileName = file.getName().replace(".yml", "");
                list.add(fileName);
            }
        }
        return list;
    }


    public List<UUID> getAllUserUUIDs() {
        List<UUID> list = new ArrayList<>();
        File folder = getUserDataFolder();
        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".yml"));

        if (files != null) {
            for (File file : files) {
                String fileName = file.getName().replace(".yml", "");
                try {
                    UUID uuid = UUID.fromString(fileName);
                    list.add(uuid);
                } catch (IllegalArgumentException e) {
                    // Falls eine Datei keinen gültigen UUID-Namen hat
                    Bukkit.getLogger().warning("⚠️ Ungültiger UUID-Dateiname: " + fileName);
                }
            }
        }

        return list;
    }

    public boolean savePartial(String name, String path, Object value) {
        File file = getConfigFile(name);
        if (!file.exists()) {
            plugin.getLogger().warning("Config '" + name + "' existiert nicht!");
            return false;
        }

        FileConfiguration tempConfig = YamlConfiguration.loadConfiguration(file);

        tempConfig.set(path, value);

        try {
            tempConfig.save(file);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


}

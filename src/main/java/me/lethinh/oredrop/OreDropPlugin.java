package me.lethinh.oredrop;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Files;

@Getter
public final class OreDropPlugin extends JavaPlugin {
    public static final String NAME = "OreDrop";
    public static OreDropPlugin plugin;
    private OreManager oreManager;

    @Override
    public void onEnable() {
        plugin = this;
        if (Files.notExists(getDataFolder().toPath().resolve("ores/example.yml")))
            saveResource("ores/example.yml", false);

        oreManager = new OreManager();

        getServer().getPluginCommand("oredrop").setExecutor(new CommandOreDrop());
        getServer().getPluginManager().registerEvents(new EventListener(), this);
    }

    @Override
    public void onDisable() {
        oreManager.saveData();
    }
}

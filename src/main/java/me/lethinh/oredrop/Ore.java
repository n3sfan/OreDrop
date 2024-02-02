package me.lethinh.oredrop;

import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class Ore {
    private final String name;
    private final Material type;
    private final int timeRegen;
    private final Material typeBlockBreak;
    private final List<DropItem> dropItems;
    private final List<Map.Entry<Integer, String>> dropCommands;

    public Ore(Path oreConfig) {
        this.name = oreConfig.getFileName().toString().substring(0, oreConfig.getFileName().toString().indexOf('.'));
        ConfigurationSection config = YamlConfiguration.loadConfiguration(oreConfig.toFile());
        this.type = Material.valueOf(config.getString("Id"));
        this.timeRegen = config.getInt("TimeRegen");
        this.typeBlockBreak = Material.valueOf(config.getString("IdBlockBreak"));
        this.dropItems = new ArrayList<>();
        this.dropCommands = new ArrayList<>();

        for (String key : config.getConfigurationSection("Drops").getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection("Drops." + key);
            String type = section.getString("Type");
            if ("item".equalsIgnoreCase(type)) {
                this.dropItems.add(new DropItem(section));
            } else if ("cmd".equalsIgnoreCase(type)) {
                this.dropCommands.addAll(section.getStringList("Cmds").stream().map(s -> new AbstractMap.SimpleEntry<>(Integer.parseInt(s.substring(0, s.indexOf(':'))), s.substring(s.indexOf(':') + 1))).collect(Collectors.toList()));
            }
        }
    }
}
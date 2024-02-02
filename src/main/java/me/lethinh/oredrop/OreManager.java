package me.lethinh.oredrop;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.stream.Stream;

public class OreManager {
    private final List<Ore> ores;
    private final Map<Location, OreState> locationOreState;
    private final Random random;

    public OreManager() {
        ores = new ArrayList<>();
        locationOreState = new HashMap<>();
        random = new Random();

        OreDropPlugin plugin = OreDropPlugin.plugin;
        try (Stream<Path> stream = Files.list(plugin.getDataFolder().toPath().resolve("ores"))) {
            stream.forEach(file -> {
                ores.add(new Ore(file));
            });
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Exception", e);
        }

        loadData();
    }

    public Ore getOre(String name) {
        return ores.stream().filter(o -> o.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public Ore getOre(Block b) {
        Location loc = b.getLocation();

        if (locationOreState.get((loc)) == null) {
            return null;
        }

        return getOre(locationOreState.get((loc)).getOreName());
    }

    public OreState getOreState(Block b) {
        return locationOreState.get(b.getLocation());
    }

    public ItemStack createOreBlockStack(Ore ore) {
        ItemStack stack = new ItemStack(ore.getType());
        ItemMeta meta = stack.getItemMeta();
        meta.setLore(Collections.singletonList(OreDropPlugin.NAME + " " + ore.getName()));
        stack.setItemMeta(meta);
        return stack;
    }

    public void addOrePlaced(Block b, String oreName) {
        locationOreState.put(b.getLocation(), new OreState(oreName));
    }

    public void removeOreBlock(Block b) {
        locationOreState.remove(b.getLocation());
    }

    public boolean isBlockOre(Block block) {
        return locationOreState.containsKey((block.getLocation()));
    }

    public boolean hasOreRegen(Block block) {
        OreState oreState = getOreState(block);
        Ore ore = getOre(oreState.getOreName());
        return TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - oreState.getLastBroken()) >= ore.getTimeRegen();
    }

    public void regenOre(Block block, Ore ore, long delay) {
        getOreState(block).setLastBroken(System.currentTimeMillis());

        Bukkit.getScheduler().runTaskLater(OreDropPlugin.plugin, () -> {
            block.setType(ore.getType());
        }, delay * 20L);
    }

    public void givePlayerDrops(Player player, Ore ore) {
        for (DropItem dropItem : ore.getDropItems()) {
            int chance = random.nextInt(100);
            if (chance < dropItem.getChance()) {
                ItemStack item = dropItem.getItem().clone();
                item.setAmount(Utils.randomInt(random, dropItem.getAmountMin(), dropItem.getAmountMax()));
                player.getWorld().dropItem(player.getLocation(), item);
            }
        }

        for (Map.Entry<Integer, String> entry : ore.getDropCommands()) {
            int chance = random.nextInt(100);
            if (chance < entry.getKey()) {
                String cmd = entry.getValue();
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("<player>", player.getName()));
            }
        }
    }

    public void loadData() {
        Path oreLocationsFile = OreDropPlugin.plugin.getDataFolder().toPath().resolve("data/locations.yml");
        if (Files.notExists(oreLocationsFile))
            return;

        YamlConfiguration locations = YamlConfiguration.loadConfiguration(oreLocationsFile.toFile());

        locationOreState.clear();
        for (String key : locations.getKeys(false)) {
            ConfigurationSection section = locations.getConfigurationSection(key);
            Location loc = section.getSerializable("loc", Location.class);

            OreState oreState = new OreState(section.getString("name"));
            oreState.setLastBroken(section.getLong("lastBroken"));
            locationOreState.put(loc, oreState);
        }

        // Schedule regen block tasks
        for (Map.Entry<Location, OreState> entry : locationOreState.entrySet()) {
            Ore ore = getOre(entry.getValue().getOreName());
            long delay = ore.getTimeRegen() - TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - entry.getValue().getLastBroken());
            if (delay < 0L) {
                delay = 0L;
            }

            regenOre(entry.getKey().getBlock(), ore, delay);
        }
    }

    public void saveData() {
        Path oreLocationsFile = OreDropPlugin.plugin.getDataFolder().toPath().resolve("data/locations.yml");
        if (Files.notExists(oreLocationsFile)) {
            try {
                if (Files.notExists(oreLocationsFile.getParent())) {
                    Files.createDirectories(oreLocationsFile.getParent());
                }

                Files.createFile(oreLocationsFile);
            } catch (IOException e) {
                OreDropPlugin.plugin.getLogger().log(Level.SEVERE, "Exception", e);
                return;
            }
        }

        YamlConfiguration locations = new YamlConfiguration();

        int i = 0;
        for (Map.Entry<Location, OreState> entry : locationOreState.entrySet()) {
            ConfigurationSection section = locations.createSection(String.valueOf(i++));
            section.set("loc", entry.getKey());
            section.set("name", entry.getValue().getOreName());
            section.set("lastBroken", entry.getValue().getLastBroken());
        }
        locationOreState.clear();

        try {
            locations.save(oreLocationsFile.toFile());
        } catch (IOException e) {
            OreDropPlugin.plugin.getLogger().log(Level.SEVERE, "Exception", e);
        }
    }
}

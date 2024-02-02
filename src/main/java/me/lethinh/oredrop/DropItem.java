package me.lethinh.oredrop;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class DropItem {
    private final ItemStack item;
    private final int amountMin, amountMax;
    private final double chance;

    public DropItem(ConfigurationSection section) {
        String id = section.getString("Id");
        String name = section.getString("Name");
        List<String> lore = section.getStringList("Lore");

        this.item = new ItemStack(Material.valueOf(id));
        ItemMeta meta = this.item.getItemMeta();
        meta.setDisplayName(Utils.parseColor(name));
        meta.setLore(lore.stream().map(Utils::parseColor).collect(Collectors.toList()));
        this.item.setItemMeta(meta);

        this.chance = section.getDouble("Chance");
        String amounts = section.getString("Amount");

        if (amounts.contains("-")) {
            this.amountMin = Integer.parseInt(amounts.split("-")[0]);
            this.amountMax = Integer.parseInt(amounts.split("-")[1]);
        } else {
            this.amountMin = this.amountMax = Integer.parseInt(amounts);
        }
    }
}

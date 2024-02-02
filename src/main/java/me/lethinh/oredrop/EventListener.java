package me.lethinh.oredrop;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class EventListener implements Listener {
    private final OreDropPlugin plugin;
    private final OreManager oreManager;

    public EventListener() {
        plugin = OreDropPlugin.plugin;
        oreManager = plugin.getOreManager();
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();

        if (!item.hasItemMeta() || !item.getItemMeta().hasLore() || !item.getItemMeta().getLore().get(0).startsWith(OreDropPlugin.NAME)) {
            return;
        }

        String oreName = item.getItemMeta().getLore().get(0).substring(OreDropPlugin.NAME.length() + 1);
        Ore ore = plugin.getOreManager().getOre(oreName);

        event.getBlockPlaced().setType(ore.getType());
        oreManager.addOrePlaced(event.getBlockPlaced(), oreName);
    }

    /**
     * Normal break
     */
    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Block b = event.getBlock();

        if (!oreManager.isBlockOre(b) || !oreManager.hasOreRegen(b) || (event.getPlayer().hasPermission("oredrop.admin") && event.getPlayer().isSneaking())) {
            return;
        }

        event.setCancelled(true);
        Ore ore = oreManager.getOre(b);
        Player player = event.getPlayer();

        event.getBlock().setType(ore.getTypeBlockBreak());
        oreManager.regenOre(b, ore, ore.getTimeRegen());
        oreManager.givePlayerDrops(event.getPlayer(), ore);
    }

    /**
     * Admin breaks
     */
    @EventHandler
    public void onShiftBreak(PlayerInteractEvent event) {
        if (!event.getPlayer().isSneaking() || event.getAction() != Action.LEFT_CLICK_BLOCK || !event.getPlayer().hasPermission("oredrop.admin")
                || !oreManager.isBlockOre(event.getClickedBlock())) {
            return;
        }

        event.setCancelled(true);
        Player player = event.getPlayer();
        Ore ore = oreManager.getOre(event.getClickedBlock());
        ItemStack oreBlockStack = oreManager.createOreBlockStack(ore);

        oreManager.removeOreBlock(event.getClickedBlock());
        event.getClickedBlock().setType(Material.AIR);
        if (player.getInventory().firstEmpty() == -1) {
            player.getWorld().dropItemNaturally(event.getClickedBlock().getLocation(), oreBlockStack);
        } else {
            player.getInventory().addItem(oreBlockStack);
        }
    }
}

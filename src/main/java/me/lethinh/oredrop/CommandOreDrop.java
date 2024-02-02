package me.lethinh.oredrop;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;

public class CommandOreDrop implements CommandExecutor {
    private final OreDropPlugin plugin;

    public CommandOreDrop() {
        plugin = OreDropPlugin.plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            return false;
        }
        if (!sender.hasPermission("oredrop.admin")) {
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT).charAt(0)) {
            case 'g':
                if (!(sender instanceof Player)) {
                    return true;
                }

                if (args.length < 3) {
                    return false;
                }

                String name = args[1];
                int amount = Integer.parseInt(args[2]);
                Ore ore = plugin.getOreManager().getOre(name);

                if (ore == null) {
                    sender.sendMessage(ChatColor.RED + "Ore not found!");
                    return true;
                }

                if (amount <= 0) {
                    return true;
                }

                Player p = (Player) sender;

                ItemStack stack = plugin.getOreManager().createOreBlockStack(ore);
                stack.setAmount(amount);
                p.getInventory().addItem(stack);
                p.sendMessage(ChatColor.GREEN + "Ok!");
                return true;
            case 'r':
                Bukkit.getPluginManager().disablePlugin(plugin);
                Bukkit.getPluginManager().enablePlugin(plugin);
                sender.sendMessage(ChatColor.GREEN + "Reloaded!");
                return true;
            default:
                return false;
        }
    }
}

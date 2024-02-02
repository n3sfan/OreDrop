package me.lethinh.oredrop;

import org.bukkit.ChatColor;
import org.bukkit.Location;

import java.util.Random;

public class Utils {
    public static String parseColor(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    /**
     * Min, max are inclusive
     */
    public static int randomInt(Random random, int min, int max) {
        return random.nextInt(max - min + 1) + min;
    }
}

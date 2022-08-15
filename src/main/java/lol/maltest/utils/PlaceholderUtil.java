package lol.maltest.utils;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PlaceholderUtil {
    public static List<Component> applyPlaceholders(List<String> strings, Player player) {
        ArrayList<Component> newStrings = new ArrayList<>();
        strings.forEach(string -> {
            newStrings.add(ChatUtil.color(string
                    .replaceAll("%player%", player.getUsername())
                    .replaceAll("%player_health%", String.valueOf(player.getHealth()))
                    .replaceAll("%online%", MinecraftServer.getConnectionManager().getOnlinePlayers().size() + "")
                    .replaceAll("%date%", new Date().toString())
            ));
        });
        return newStrings;
    }
}

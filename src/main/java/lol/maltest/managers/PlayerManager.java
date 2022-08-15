package lol.maltest.managers;

import lol.maltest.MalHubServer;
import lol.maltest.impl.SideBar;
import lol.maltest.utils.ChatUtil;
import lol.maltest.utils.PlaceholderUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;

public class PlayerManager {

    private MalHubServer server;
    public List<String> defaultLines = new ArrayList<>();
    public Component defaultTitle;
    public HashMap<UUID, SideBar> scoreboards = new HashMap<>();

    public PlayerManager(MalHubServer server) {
        this.server = server;
        getScoreboardDefaults();
    }

    public void createScoreboard(Player player) {
        SideBar sideBar = new SideBar(defaultTitle, PlaceholderUtil.applyPlaceholders(defaultLines, player));
        sideBar.addPlayer(player);
        scoreboards.put(player.getUuid(), sideBar);
    }

    public void destoryScoreboard(Player player) {
        scoreboards.forEach((uuid, sideBar) -> {
            if(uuid == player.getUuid()) {
                sideBar.removePlayer(player);
                scoreboards.remove(uuid);
            }
        });
    }

    public void updateScoreboards() {
        getScoreboardDefaults();
        scoreboards.forEach((uuid, sideBar) -> {
            sideBar.lines = PlaceholderUtil.applyPlaceholders(defaultLines, MinecraftServer.getConnectionManager().getPlayer(uuid));
            sideBar.title = defaultTitle;
        });
    }

    public void getScoreboardDefaults() {
        defaultLines.clear();
        defaultTitle = null;
        try {
            server.serverConfig.reload();
        } catch (IOException e) {
            e.printStackTrace();
        }
        defaultLines.addAll(server.serverConfig.getStringList("scoreboard.lines"));
        defaultTitle = ChatUtil.color(server.serverConfig.getString("scoreboard.title"));
        scoreboards.forEach((uuid, sideBar) -> {
            sideBar.update(MinecraftServer.getConnectionManager().getPlayer(uuid));
        });
    }

    // todo
}

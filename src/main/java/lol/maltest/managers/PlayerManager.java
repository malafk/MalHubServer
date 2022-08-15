package lol.maltest.managers;

import lol.maltest.MalHubServer;
import lol.maltest.impl.SideBar;
import lol.maltest.utils.ChatUtil;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public class PlayerManager {

    private MalHubServer server;
    public HashMap<UUID, SideBar> scoreboards = new HashMap<>();

    public PlayerManager(MalHubServer server) {
        this.server = server;
    }

    public void createScoreboard(Player player) {
        SideBar sideBar = new SideBar(ChatUtil.color("&c&lTest Scoreboard"), Arrays.asList(ChatUtil.color("&e"), ChatUtil.color("&aTest!!")));
        sideBar.addPlayer(player);
        scoreboards.put(player.getUuid(), sideBar);
    }

    // todo
}

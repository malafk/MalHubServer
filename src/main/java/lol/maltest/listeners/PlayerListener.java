package lol.maltest.listeners;

import io.github.bloepiloepi.pvp.events.PlayerFishEvent;
import io.github.bloepiloepi.pvp.projectile.FishingBobber;
import lol.maltest.MalHubServer;
import lol.maltest.entity.NPCEntity;
import lol.maltest.impl.events.NPCClickEvent;
import lol.maltest.utils.ChatUtil;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.entity.fakeplayer.FakePlayer;
import net.minestom.server.entity.hologram.Hologram;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.entity.EntityShootEvent;
import net.minestom.server.event.player.*;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.PlayerListHeaderAndFooterPacket;
import net.minestom.server.permission.Permission;
import net.minestom.server.timer.TaskSchedule;

import javax.xml.stream.Location;
import java.util.HashMap;
import java.util.Vector;
import java.util.function.Function;

public class PlayerListener {

    GlobalEventHandler handler;

    private MalHubServer server;
    private boolean firstLogin = true;

    public PlayerListener(MalHubServer server) {
        this.server = server;
        start();
    }


    public void start() {
        handler = MinecraftServer.getGlobalEventHandler();

        handler.addListener(PlayerChatEvent.class, event -> {
            if(event.getPlayer().getUsername().contains("Bot")) event.setCancelled(true);
            event.setChatFormat((playerChatEvent) -> ChatUtil.color("&f" + playerChatEvent.getPlayer().getUsername() + ": " + event.getMessage()));
        });

        handler.addListener(PlayerFishEvent.class, event -> {
            Pos bobber = event.getBobber().getPosition();
            Pos playerLoc = event.getPlayer().getPosition();
            Vec vec = new Vec(bobber.x() - playerLoc.x(), 0.7, bobber.z() - playerLoc.z()).mul(15);
                        event.getPlayer().setVelocity(vec);
        });

        handler.addListener(NPCClickEvent.class, event -> {
            String command = server.npcConfig.getString("npcs." + event.npc.id() + ".command");
            event.player.chat("/" + command);
        });

        handler.addListener(PlayerEntityInteractEvent.class, event -> {
            if(event.getTarget() instanceof NPCEntity) {
                EventDispatcher.call(new NPCClickEvent(event.getPlayer(), server.npcManager.getNpc(event.getTarget().getUuid()), event.getHand()));
            }
        });

        handler.addListener(PlayerLoginEvent.class, event -> {
            Player player = event.getPlayer();
            player.getPlayerConnection().sendPacket(new PlayerListHeaderAndFooterPacket(ChatUtil.color("FirstLine\nSecondLine\nThirdLine\n4\n5\n6\n7\n8\n9\n10\n11\n12\n13\n14\n15"), ChatUtil.color("1\n2\n3")));
            event.setSpawningInstance(server.instanceContainer);
            if (player instanceof FakePlayer) return;
            player.setRespawnPoint(new Pos(0, 64, 0));
            player.getAllPermissions().add(new Permission("spark"));
        });

        handler.addListener(PlayerSpawnEvent.class, event -> {
            Player player = event.getPlayer();
            if(firstLogin) {
                server.npcManager.loadFromConfig();
                server.hologramManager.loadFromConfig();
                firstLogin = false;
            }
            player.setGameMode(GameMode.CREATIVE);
            server.playerManager.createScoreboard(player);
        });

        handler.addListener(PlayerSkinInitEvent.class, event -> {
            if(event.getPlayer() instanceof FakePlayer) return;
            if(!server.serverConfig.getBoolean("online-mode")) return;
            System.out.println(event.getPlayer().getUuid());
            PlayerSkin skinFromUUID = PlayerSkin.fromUuid(event.getPlayer().getUuid().toString());
            PlayerSkin skin = new PlayerSkin(skinFromUUID.textures(), skinFromUUID.signature());
            event.setSkin(skin);
        });

        handler.addListener(PlayerDisconnectEvent.class, event -> {
            server.playerManager.destoryScoreboard(event.getPlayer());
        });

    }

    public void giveHubItems(Player player) {
        player.getInventory().setItemStack(8, ItemStack.builder(Material.FISHING_ROD).displayName(server.rodName).build());
    }

}

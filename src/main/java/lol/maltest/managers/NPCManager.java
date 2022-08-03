package lol.maltest.managers;

import lol.maltest.MalHubServer;
import lol.maltest.entity.NPCEntity;
import lol.maltest.utils.ChatUtil;
import lol.maltest.utils.LoggerUtil;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.network.packet.server.play.TeamsPacket;
import net.minestom.server.scoreboard.Team;
import net.minestom.server.timer.TaskSchedule;

import java.io.IOException;
import java.util.*;

public class NPCManager {

    private MalHubServer server;
    private ArrayList<NPCEntity> npcs = new ArrayList<>();
    private Team npcTeam;

    public NPCManager(MalHubServer server) {
        this.server = server;

        LoggerUtil.info("Starting NPC Manager");

        npcTeam = MinecraftServer.getTeamManager().createTeam("npcs");
        npcTeam.setNameTagVisibility(TeamsPacket.NameTagVisibility.NEVER);
    }

    public void loadFromConfig() {
        if (server.npcConfig.getSection("npcs") == null) return;
        server.npcConfig.getSection("npcs").getKeys().forEach(key -> {

            UUID uuid = UUID.fromString(server.npcConfig.getString("npcs." + key + ".uuid"));
            String name = server.npcConfig.getString("npcs." + key + ".name");
            PlayerSkin skin = getSkin(key.toString());
            Pos pos = getPos(key.toString());

            NPCEntity npc = new NPCEntity(uuid, key.toString(), pos, ChatUtil.color(name), skin);

            npc.setInstance(server.instanceContainer);
            npc.teleport(pos);
            npcs.add(npc);
        });
    }

    public NPCEntity getNpc(UUID uuid) {
        for (NPCEntity npc : npcs) {
            if (npc.getUuid().equals(uuid)) {
                return npc;
            }
        }
        return null;
    }

    public void deleteNPC(UUID uuid, String id, boolean config) {
        NPCEntity.getEntity(uuid).remove();
        for(NPCEntity npc : npcs) {
            if(npc.getUuid().equals(uuid)) {
                System.out.println("Removing NPC " + npc.id());
                npcs.remove(npc);
                break;
            }
        }
        if (config) {
            server.npcConfig.remove("npcs." + id);
            try {
                server.npcConfig.save();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    public void createNpc(CommandSender sender, String name) {
        UUID uuid = UUID.randomUUID();
        String id = "npc-" + new Random().nextInt(1, 10000);
        PlayerSkin skin = PlayerSkin.fromUsername("maltost");

        sender.sendMessage(ChatUtil.color("&b&lNPCS &8- &7Creating NPC with ID: &f" + id));

        NPCEntity npc = new NPCEntity(uuid, id, sender.asPlayer().getPosition(), ChatUtil.color(name), skin);

        server.npcConfig.set("npcs." + id + ".uuid", uuid.toString());
        server.npcConfig.set("npcs." + id + ".creation", new Date().getTime());
        server.npcConfig.set("npcs." + id + ".position.x", sender.asPlayer().getPosition().x());
        server.npcConfig.set("npcs." + id + ".position.y", sender.asPlayer().getPosition().y());
        server.npcConfig.set("npcs." + id + ".position.z", sender.asPlayer().getPosition().z());
        server.npcConfig.set("npcs." + id + ".position.yaw", sender.asPlayer().getPosition().yaw());
        server.npcConfig.set("npcs." + id + ".position.pitch", sender.asPlayer().getPosition().pitch());
        server.npcConfig.set("npcs." + id + ".name", name);
        server.npcConfig.set("npcs." + id + ".command", "");
        server.npcConfig.set("npcs." + id + ".skin.textures", skin.textures());
        server.npcConfig.set("npcs." + id + ".skin.signature", skin.signature());

        npc.setInstance(server.instanceContainer);
        npc.teleport(sender.asPlayer().getPosition());

        try {
            server.npcConfig.save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        npcs.add(npc);
    }

    public NPCEntity getNPC(String id) {
        for (NPCEntity npc : npcs) {
            if (npc.id().equals(id)) {
                return npc;
            }
        }
        return null;
    }

    public PlayerSkin getSkin(String id) {
        if (server.npcConfig.getSection("npcs." + id) == null) return null;
        return new PlayerSkin(server.npcConfig.getString("npcs." + id + ".skin.textures"), server.npcConfig.getString("npcs." + id + ".skin.signature"));
    }

    public Pos getPos(String id) {
        if (server.npcConfig.getSection("npcs." + id) == null) return null;
        return new Pos(server.npcConfig.getDouble("npcs." + id + ".position.x"), server.npcConfig.getDouble("npcs." + id + ".position.y"), server.npcConfig.getDouble("npcs." + id + ".position.z"), server.npcConfig.getFloat("npcs." + id + ".position.yaw"), server.npcConfig.getFloat("npcs." + id + ".position.pitch"));
    }

    public void updateNPC(String id) {
        if(server.npcConfig.getSection("npcs." + id) == null) return;
        deleteNPC(UUID.fromString(server.npcConfig.getString("npcs." + id + ".uuid")), id, false);

        String name = server.npcConfig.getString("npcs." + id + ".name");
        UUID uuid = UUID.fromString(server.npcConfig.getString("npcs." + id + ".uuid"));
        PlayerSkin skin = getSkin(id);
        Pos pos = getPos(id);

        NPCEntity npc = new NPCEntity(uuid, id, pos, ChatUtil.color(name), skin);

        npc.setInstance(server.instanceContainer);
        npc.teleport(pos);

        npcs.add(npc);
    }

    public void setCommand(String id, String command) {
        if(server.npcConfig.getSection("npcs." + id) == null) return;
        server.npcConfig.set("npcs." + id + ".command", command);

        try {
            server.npcConfig.save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setNPCSkin(String textures, String signature, String id) {
        if(server.npcConfig.getSection("npcs." + id) == null) return;
        server.npcConfig.set("npcs." + id + ".skin.textures", textures);
        server.npcConfig.set("npcs." + id + ".skin.signature", signature);
        try {
            server.npcConfig.save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        updateNPC(id);
    }
}

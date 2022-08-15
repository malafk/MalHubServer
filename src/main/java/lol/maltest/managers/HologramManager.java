package lol.maltest.managers;

import lol.maltest.MalHubServer;
import lol.maltest.entity.NPCEntity;
import lol.maltest.impl.Hologram;
import lol.maltest.utils.ChatUtil;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.PlayerSkin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

public class HologramManager {

    private MalHubServer server;
    public ArrayList<Hologram> holograms = new ArrayList<>();

    public HologramManager(MalHubServer server) {
        this.server = server;
    }

    public void loadFromConfig() {
        if (server.hologramConfig.getSection("holograms") == null) return;
        server.hologramConfig.getSection("holograms").getKeys().forEach(key -> {
            String id = key.toString();
            Pos pos = new Pos(server.hologramConfig.getDouble("holograms." + id + ".position.x"), server.hologramConfig.getDouble("holograms." + id + ".position.y"), server.hologramConfig.getDouble("holograms." + id + ".position.z"));
            boolean npc = server.hologramConfig.getBoolean("holograms." + id + ".npc");
            Hologram hologram = new Hologram(id, new ArrayList<>(server.hologramConfig.getStringList("holograms." + id + ".text")), pos, server);
            hologram.create(npc);
            holograms.add(hologram);
        });
    }

    public Hologram getHologram(String id) {
        for(Hologram hologram : holograms) {
            if(hologram.id.equals(id)) {
                return hologram;
            }
        }
        return null;
    }

    public Hologram createHologram(String id, ArrayList<String> text, Pos position, boolean isNpc) throws IOException {
//        String id = "holo-" + new Random().nextInt(1, 10000);
        Hologram hologram = new Hologram(id, text, position, server);

        server.hologramConfig.set("holograms." + id + ".npc", isNpc);
        server.hologramConfig.set("holograms." + id + ".text", new ArrayList<>(text));
        server.hologramConfig.set("holograms." + id + ".position.x", position.x());
        server.hologramConfig.set("holograms." + id + ".position.y", position.y());
        server.hologramConfig.set("holograms." + id + ".position.z", position.z());

        server.hologramConfig.save();

        holograms.add(hologram);
        return hologram;
    }

    public void addLine(String id, String line, boolean npc) {
        if(getHologram(id) == null) return;
        getHologram(id).text.add(line);
        getHologram(id).remove();
        getHologram(id).create(npc);

        saveHologram(id);
    }

    public void setLine(String id, int line, String text, boolean npc) {
        if(getHologram(id) == null) return;
        getHologram(id).text.set(line, text);
        getHologram(id).remove();
        getHologram(id).create(npc);

        saveHologram(id);
    }

    public void deleteHologram(String id) throws IOException {
        if(getHologram(id) == null) return;
        getHologram(id).remove();
        server.hologramConfig.remove("holograms." + id);
        holograms.remove(getHologram(id));

        try {
            server.hologramConfig.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveHologram(String id) {
        if(getHologram(id) == null) return;

        Hologram hologram = getHologram(id);

        server.hologramConfig.set("holograms." + id + ".text", hologram.text);
        server.hologramConfig.set("holograms." + id + ".position.x", hologram.position.x());
        server.hologramConfig.set("holograms." + id + ".position.y", hologram.position.y());
        server.hologramConfig.set("holograms." + id + ".position.z", hologram.position.z());

        try {
            server.hologramConfig.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

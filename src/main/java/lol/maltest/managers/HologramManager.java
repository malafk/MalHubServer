package lol.maltest.managers;

import lol.maltest.MalHubServer;
import lol.maltest.impl.events.Hologram;
import lol.maltest.utils.ChatUtil;
import net.minestom.server.coordinate.Pos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.Collectors;

public class HologramManager {

    private MalHubServer server;
    public ArrayList<Hologram> holograms = new ArrayList<>();

    public HologramManager(MalHubServer server) {
        this.server = server;
    }

    public Hologram getHologram(String id) {
        for(Hologram hologram : holograms) {
            if(hologram.id.equals(id)) {
                return hologram;
            }
        }
        return null;
    }

    public Hologram createHologram(String id, ArrayList<String> text, Pos position) throws IOException {
//        String id = "holo-" + new Random().nextInt(1, 10000);
        Hologram hologram = new Hologram(id, text, position, server);

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
    }

    public void deleteHologram(String id) throws IOException {
        if(getHologram(id) == null) return;
        getHologram(id).remove();
        server.hologramConfig.remove("holograms." + id);
        server.hologramConfig.save();
        holograms.remove(getHologram(id));
    }
}

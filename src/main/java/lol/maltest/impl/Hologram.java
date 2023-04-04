package lol.maltest.impl;

import lol.maltest.MalHubServer;
import lol.maltest.utils.ChatUtil;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.other.ArmorStandMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Hologram {

    public String id;
    private List<Entity> entityList = new ArrayList<>();
    public ArrayList<String> text = new ArrayList<>();
    public Pos position;
    private MalHubServer server;

    public Hologram(String id, ArrayList<String> text, Pos position, MalHubServer server) {
        this.id = id;
        this.text = text;
        this.position = position;
        this.server = server;
    }

    public void create(boolean npc) {
        for(int i = 0; i < text.size(); i++) {
            Entity entity = new Entity(EntityType.ARMOR_STAND);
            ArmorStandMeta meta = (ArmorStandMeta) entity.getEntityMeta();
            meta.setSmall(true);
            meta.setHasNoBasePlate(true);
            meta.setMarker(true);
            meta.setInvisible(true);
            meta.setCustomNameVisible(true);
            meta.setHasNoGravity(true);
            meta.setNotifyAboutChanges(true);
            meta.setCustomName(ChatUtil.color(text.get(i)));

            entity.setInstance(server.instanceContainer);
            if(npc) {
                entity.teleport( position.add(0, 1.5 + (0.30 * (text.size() - i)), 0));
            } else {
                entity.teleport( position.add(0, 0.5 + (0.30 * (text.size() - i)), 0));
            }
            entityList.add(entity);
        }
    }

    public void setLine(int index, String text) {
        entityList.get(index).setCustomName(ChatUtil.color(text));
    }

    public void remove() {
        entityList.forEach(Entity::remove);
        entityList.clear();
    }

}

package lol.maltest.impl.events;

import lol.maltest.entity.NPCEntity;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;

public class NPCClickEvent implements Event {

    public final Player player;
    public final NPCEntity npc;
    public final Player.Hand hand;

    public NPCClickEvent(Player player, NPCEntity npc, Player.Hand hand) {
        this.player = player;
        this.npc = npc;
        this.hand = hand;
    }
}

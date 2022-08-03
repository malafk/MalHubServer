package lol.maltest.commands.npcs;

import lol.maltest.MalHubServer;
import net.minestom.server.command.builder.Command;

import java.util.ArrayList;

public class NPCListCommand extends Command {

    public NPCListCommand(MalHubServer plugin) {
        super("npclist");

        setDefaultExecutor((sender, args) -> {
            ArrayList<String> npcNames = new ArrayList<>();
            plugin.npcEntities.forEach(npc -> {
                npcNames.add(npc.id());
            });
//            sender.sendMessage("NPCs: " + npcNames);
            sender.sendMessage("NPCs Amount: " + plugin.npcEntities.size());
        });
    }

}

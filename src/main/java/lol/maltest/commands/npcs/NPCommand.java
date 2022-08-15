package lol.maltest.commands.npcs;

import lol.maltest.MalHubServer;
import lol.maltest.entity.NPCEntity;
import lol.maltest.impl.Hologram;
import lol.maltest.utils.ChatUtil;
import net.kyori.adventure.text.TextComponent;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.awt.*;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class NPCommand extends Command {

    private MalHubServer server;

    public NPCommand(MalHubServer server) {
        super("npc");
        this.server = server;

        TextComponent[] helpMessage = {
                ChatUtil.color("&b&lMalHubServer &7- NPC Management"),
                ChatUtil.color("&7"),
                ChatUtil.color("&f/npc create <name> &7- Creates an NPC"),
                ChatUtil.color("&f/npc remove <name> &7- Removes an NPC"),
                ChatUtil.color("&f/npc setskin <id> <url> &7- Set's NPC skin"),
        };

        setDefaultExecutor((sender, args) -> {
            List<TextComponent> helpList = Arrays.asList(helpMessage);
            helpList.forEach(sender::sendMessage);
        });

        addSubcommand(new CreateNpcCommand());
        addSubcommand(new RemoveNpcCommand());
        addSubcommand(new SetNPCCommand());
        addSubcommand(new SetNPCSkinCommand());
        addSubcommand(new SetLineCommand());
        addSubcommand(new AddLineCommand());
    }

    class RemoveNpcCommand extends Command {

        RemoveNpcCommand() {
            super("remove");

            var name = ArgumentType.String("id");

            addSyntax(((sender, context) -> {
                String id1 = context.get("id");
                if(server.npcConfig.getString("npcs." + id1 + ".name") != null) {
                    server.npcManager.deleteNPC(UUID.fromString(server.npcConfig.getString("npcs." + id1 + ".uuid")), id1, true);
                    sender.sendMessage(ChatUtil.color("&b&lNPCS &8- &7Removed NPC with ID: &f" + id1));
                } else {
                    sender.sendMessage(ChatUtil.color("&b&lNPCS &8- &7Couldn't find a NPC with ID: &f" + id1));
                }
            }), name);
        }
    }

    class CreateNpcCommand extends Command {

        CreateNpcCommand() {
            super("create");

            var name = ArgumentType.String("name");

            addSyntax(((sender, context) -> {
                Player player = ((Player) sender);
                String name1 = context.get("name");
                NPCEntity npc = server.npcManager.createNpc(sender, name1);
                Hologram hologram = null;
                try {
                    hologram = server.hologramManager.createHologram(npc.id(), new ArrayList<>(Arrays.asList("&7Modify me with /npc!")), player.getPosition(), true);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                hologram.create(true);
                sender.sendMessage(ChatUtil.color("&b&lNPCS &8- &7The NPC has been created!"));
            }), name);
        }
    }

    class SetNPCSkinCommand extends Command {

        SetNPCSkinCommand() {
            super("setskin");

            var npcid = ArgumentType.String("npcid");
            var url = ArgumentType.String("url");

            addSyntax(((sender, context) -> {
                Player player = ((Player) sender);
                String url1 = context.get("url");
                String id = context.get("npcid");
                DataOutputStream out = null;
                BufferedReader reader = null;
                try {
                    URL target = new URL("https://api.mineskin.org/generate/url");
                    HttpURLConnection con = (HttpURLConnection) target.openConnection();
                    con.setRequestMethod("POST");
                    con.setDoOutput(true);
                    con.setConnectTimeout(1000);
                    con.setReadTimeout(30000);
                    out = new DataOutputStream(con.getOutputStream());
                    out.writeBytes("url=" + URLEncoder.encode(url1, StandardCharsets.UTF_8));
                    out.close();
                    reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    JSONObject output = (JSONObject) new JSONParser().parse(reader);
                    JSONObject data = (JSONObject) output.get("data");
                    String uuid = (String) data.get("uuid");
                    JSONObject texture = (JSONObject) data.get("texture");
                    String textureEncoded = (String) texture.get("value");
                    String signature = (String) texture.get("signature");
                    con.disconnect();

                    server.npcManager.setNPCSkin(textureEncoded, signature, id);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
//                server.npcManager.setNpc(sender, name1);
                sender.sendMessage(ChatUtil.color("&b&lNPCS &8- &7The NPC's skin has been updated!"));
            }), npcid, url);
        }
    }

    class SetNPCCommand extends Command {

        SetNPCCommand() {
            super("setcommand");

            var id1 = ArgumentType.String("id");
            var command = ArgumentType.StringArray("command");

            addSyntax(((sender, context) -> {
                Player player = ((Player) sender);
                String id = context.get(id1);
                String[] command1 = context.get("command");
                String str = String.join(" ", command1);
                System.out.println(str);
                server.npcManager.setCommand(id, str);
                sender.sendMessage(ChatUtil.color("&b&lNPCS &8- &7The NPC's command has been set!"));
            }), id1, command);
        }
    }

    class SetLineCommand extends Command {

        SetLineCommand() {
                super("setline");

                var id = ArgumentType.String("npcid");
                var line = ArgumentType.Integer("line");
                var text = ArgumentType.StringArray("text");

                addSyntax(((sender, context) -> {
                    String id1 = context.get(id);
                    int line1 = context.get(line);

                    String[] text1 = context.get(text);
                    String lineText = String.join(" ", text1);

                    server.hologramManager.setLine(id1, line1, lineText, true);
                    sender.sendMessage(ChatUtil.color("&b&lNPCS &8- &7The NPC's hologram has been updated!"));
                }), id, line, text);
            }
    }

    class AddLineCommand extends Command {

        AddLineCommand() {
            super("addline");

            var id = ArgumentType.String("npcid");
            var text = ArgumentType.StringArray("text");

            addSyntax(((sender, context) -> {
                String id1 = context.get(id);

                String[] text1 = context.get(text);
                String lineText = String.join(" ", text1);

                server.hologramManager.addLine(id1, lineText, true);
                sender.sendMessage(ChatUtil.color("&b&lNPCS &8- &7The NPC's hologram has been updated!"));
            }), id, text);
        }
    }

}

package lol.maltest.commands.npcs;

import lol.maltest.MalHubServer;
import lol.maltest.entity.NPCEntity;
import lol.maltest.impl.events.Hologram;
import lol.maltest.utils.ChatUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.utils.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
                server.npcManager.createNpc(sender, name1);
                String[] test = {
                        "&#084cfbd&#0a4efbf&#0c50fbj&#0e52fbu &#1054fbo&#1256fbi&#1458fbs&#165afbd&#185cfbf&#1a5efbj&#1c60fbs&#1e62fbd&#2064fbi&#2266fbj&#2468fbs&#266afbd&#286cfbi &#2a6efbj&#2c70fbd&#2e72fbs&#3074fbo&#3276fci&#3478fcs&#367afcd&#387cfcj&#3a7efcf&#3c80fcs&#3e82fcd&#4084fcf&#4286fch&#4488fcs&#468afcd&#488cfco&#4a8efci&#4c90fch&#4e92fcn&#5094fcs&#5296fco&#5498fcd&#569afch&#589cfcn&#5a9efcd&#5ba1fcs&#5da3fcf&#5fa5fch&#61a7fcd&#63a9fcs&#65abfcd&#67adfcs&#69affch&#6bb1fcs&#6db3fcd&#6fb5fcb&#71b7fch&#73b9fcj&#75bbfcd&#77bdfcs&#79bffcf&#7bc1fcb&#7dc3fch&#7fc5fcj&#81c7fcs&#83c9fcd&#85cbfdb&#87cdfdd&#89cffds&#8bd1fdb&#8dd3fdf&#8fd5fdd&#91d7fds&#93d9fdh&#95dbfdb&#97ddfdd&#99dffds&#9be1fdh&#9de3fdd&#9fe5fds&#a1e7fdb&#a3e9fdf&#a5ebfdj&#a7edfdh&#a9effds&#abf1fdd&#adf3fdb",
                        "&6Middle",
                        "7afdf8ds0f8dsf",
                        "&8Bottom \uD83D\uDE80"
                };
                try {
                    Hologram hologram = server.hologramManager.createHologram("test" + new Random().nextInt(444), new ArrayList<>(Arrays.asList(test)), player.getPosition());
                    hologram.create(true);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
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

}

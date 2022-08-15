package lol.maltest;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import io.github.bloepiloepi.pvp.PvpExtension;
import lol.maltest.commands.ListCommand;
import lol.maltest.commands.npcs.NPCListCommand;
import lol.maltest.commands.npcs.NPCommand;
import lol.maltest.commands.npcs.SpawnNPCCommand;
import lol.maltest.commands.staff.GamemodeCommand;
import lol.maltest.entity.NPCEntity;
import lol.maltest.listeners.PlayerListener;
import lol.maltest.managers.HologramManager;
import lol.maltest.managers.NPCManager;
import lol.maltest.managers.PlayerManager;
import lol.maltest.utils.ChatUtil;
import lol.maltest.utils.LoggerUtil;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.block.Block;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

public class MalHubServer {

    private MalHubServer plugin;
    public MinecraftServer server;

    public YamlDocument serverConfig;
    public YamlDocument hologramConfig;
    public YamlDocument npcConfig;

    public PlayerManager playerManager;
    public NPCManager npcManager;
    public HologramManager hologramManager;

    private PlayerListener playerListener;
    public InstanceContainer instanceContainer;

    public Component rodName;

    public ArrayList<NPCEntity> npcEntities = new ArrayList<>();

    public MalHubServer() {
        plugin = this;
        server = MinecraftServer.init();

        new File("data/").mkdirs();

        try {
            serverConfig = YamlDocument.create(new File("data", "config.yml"), getClass().getClassLoader().getResource("config.yml").openStream(),
                    GeneralSettings.DEFAULT, LoaderSettings.builder().setAutoUpdate(true).build(), DumperSettings.DEFAULT, UpdaterSettings.builder().setVersioning(new BasicVersioning("file-version")).build());

            npcConfig = YamlDocument.create(new File("data", "npcs.yml"), getClass().getClassLoader().getResource("npcs.yml").openStream(),
                    GeneralSettings.DEFAULT, LoaderSettings.builder().setAutoUpdate(true).build(), DumperSettings.DEFAULT, UpdaterSettings.builder().setVersioning(new BasicVersioning("file-version")).build());

            hologramConfig = YamlDocument.create(new File("data", "holograms.yml"), getClass().getClassLoader().getResource("holograms.yml").openStream(),
                    GeneralSettings.DEFAULT, LoaderSettings.builder().setAutoUpdate(true).build(), DumperSettings.DEFAULT, UpdaterSettings.builder().setVersioning(new BasicVersioning("file-version")).build());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        rodName = ChatUtil.color(serverConfig.getString("items.grappler"));

        InstanceManager instanceManager = MinecraftServer.getInstanceManager();

        instanceContainer = instanceManager.createInstanceContainer();
        instanceContainer.setGenerator((unit -> unit.modifier().fillHeight(0, 40, Block.STONE)));


        if(serverConfig.getBoolean("online-mode")) {
            MojangAuth.init();
        }

        int port = serverConfig.getInt("port");
        LoggerUtil.info("Server running on port: " + port);
        server.start("0.0.0.0", port);

        this.playerManager = new PlayerManager(this);
        this.npcManager = new NPCManager(this);
        this.hologramManager = new HologramManager(this);
        playerListener = new PlayerListener(plugin);

        MinecraftServer.getCommandManager().register(new SpawnNPCCommand(this));
        MinecraftServer.getCommandManager().register(new NPCListCommand(this));
        MinecraftServer.getCommandManager().register(new NPCommand(this));
        MinecraftServer.getCommandManager().register(new GamemodeCommand());
        MinecraftServer.getCommandManager().register(new ListCommand());

        PvpExtension.init();
        MinecraftServer.getGlobalEventHandler().addChild(PvpExtension.events());
    }

}

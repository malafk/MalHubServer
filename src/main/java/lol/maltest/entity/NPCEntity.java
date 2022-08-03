package lol.maltest.entity;

import com.extollit.gaming.ai.path.HydrazinePathFinder;
import lol.maltest.MalHubServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.*;
import net.minestom.server.entity.metadata.PlayerMeta;
import net.minestom.server.entity.pathfinding.NavigableEntity;
import net.minestom.server.entity.pathfinding.Navigator;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.client.play.ClientPlayerRotationPacket;
import net.minestom.server.network.packet.server.play.*;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class NPCEntity extends LivingEntity implements NavigableEntity {

    private final @NotNull Navigator navigator = new Navigator(this);
    private final @NotNull String id;
    private final @NotNull Pos homePosition;
    private final @NotNull String name;
    private final PlayerSkin playerSkin;
    private final @NotNull PlayerInfoPacket PLAYER_ADD_INFO;
    private final @NotNull PlayerInfoPacket PLAYER_HIDE_INFO;

    public NPCEntity(
            @NotNull UUID uuid,
            @NotNull String id,
            @NotNull Pos homePosition,
            @NotNull TextComponent displayName,
            @NotNull PlayerSkin playerSkin
            ) {
        super(EntityType.PLAYER, uuid);
        this.id = id;
        this.homePosition = homePosition;
        final String name = displayName.content();
        this.name = name.substring(0, Math.min(name.length(), 16));
        this.playerSkin = playerSkin;
        this.PLAYER_ADD_INFO = generatePlayerAddInfo();
        this.PLAYER_HIDE_INFO = generatePlayerHideInfo();
        this.setCustomName(displayName);
        this.invulnerable = true;
        PlayerMeta meta = (PlayerMeta) this.entityMeta;
        meta.setNotifyAboutChanges(false);
        meta.setCapeEnabled(true);
        meta.setHatEnabled(true);
        meta.setJacketEnabled(true);
        meta.setLeftLegEnabled(true);
        meta.setRightLegEnabled(true);
        meta.setLeftSleeveEnabled(true);
        meta.setRightSleeveEnabled(true);
        meta.setNotifyAboutChanges(true);
    }

    /**
     * Gets the internal id of this npc
     */
    public @NotNull String id() {
        return id;
    };

    /**
     * Gets the home position of this npc
     */
    public @NotNull Pos homePosition() {
        return homePosition;
    };

    /**
     * This is run when a player interacts with this npc
     */
    public void onInteract(@NotNull MalHubServer server, PlayerEntityInteractEvent event) {
    }

    @NotNull
    @Override
    public Navigator getNavigator() {
        return navigator;
    }

    @Override
    public void update(long time) {
        super.update(time);

        // Path finding
        this.navigator.tick();
    }

    @Override
    public CompletableFuture<Void> setInstance(@NotNull Instance instance, @NotNull Pos spawnPosition) {
        this.navigator.setPathFinder(new HydrazinePathFinder(navigator.getPathingEntity(), instance.getInstanceSpace()));
        return super.setInstance(instance, spawnPosition);
    }

    private @NotNull PlayerInfoPacket generatePlayerAddInfo() {
        Component customName = getCustomName();
        Component displayName = customName == null ? Component.text(name) : customName;

        return new PlayerInfoPacket(
                PlayerInfoPacket.Action.ADD_PLAYER,
                List.of(new PlayerInfoPacket.AddPlayer(uuid, name, List.of(new PlayerInfoPacket.AddPlayer.Property("textures",playerSkin.textures(),playerSkin.signature())), GameMode.CREATIVE, 0, displayName))
        );
    }

    private @NotNull PlayerInfoPacket generatePlayerHideInfo() {
                return new PlayerInfoPacket(
                        PlayerInfoPacket.Action.REMOVE_PLAYER,
                        new PlayerInfoPacket.RemovePlayer(uuid));
    }

    @Override
    public void updateNewViewer(@NotNull Player player) {
        final PlayerConnection connection = player.getPlayerConnection();
        connection.sendPacket(PLAYER_ADD_INFO);
        // hide name
        connection.sendPacket(new TeamsPacket("npcs", new TeamsPacket.AddEntitiesToTeamAction(List.of(name))));
        // Hide npc from tablist
        // This needs to be delayed, otherwise the player does not render.
        this.scheduler().buildTask(new Runnable() {
            @Override
            public void run() {
                connection.sendPacket(PLAYER_HIDE_INFO);
            }
        }).delay(Duration.ofSeconds(5)).schedule();
        super.updateNewViewer(player);
    }

    public static interface NPCSupplier {
        public @NotNull NPCEntity create(
                @NotNull UUID uuid,
                @NotNull String id,
                @NotNull Pos homePosition,
                @NotNull TextComponent displayName
        );
    }
}

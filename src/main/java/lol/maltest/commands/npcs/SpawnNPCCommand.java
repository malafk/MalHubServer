package lol.maltest.commands.npcs;

import lol.maltest.MalHubServer;
import lol.maltest.entity.NPCEntity;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.ArgumentCallback;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.entity.fakeplayer.FakePlayer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.EntityAnimationPacket;
import net.minestom.server.network.packet.server.play.NamedSoundEffectPacket;
import net.minestom.server.network.packet.server.play.PlayerInfoPacket;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.mca.BlockState;

import java.util.List;
import java.util.Random;
import java.util.UUID;

public class SpawnNPCCommand extends Command {

    public SpawnNPCCommand(MalHubServer server) {
        super("spawnnpc");

        setDefaultExecutor((sender, args) -> {
            sender.sendMessage("Usage: /spawnnpc <amount>");
        });

        var amount = ArgumentType.Integer("amount");
        addSyntax((sender, args) -> {
            int amountOfNpcs = args.get("amount");
            sender.sendMessage("Spawning " + amountOfNpcs + " npcs");
            Player player = (Player) sender;
            Random random = new Random();
            int low = -50;
            int high = 50;
            for(int i = 0; i < amountOfNpcs; i++) {
                Pos randomLoc = new Pos(player.getPosition().x() + random.nextInt(high - low) + low, player.getPosition().y(), player.getPosition().z() + random.nextInt(high - low) + low);
                NPCEntity npc = new NPCEntity(UUID.randomUUID(), "bot-" + i, randomLoc,  Component.text("test").color(TextColor.color(153, 255, 153)), PlayerSkin.fromUsername("moocowgalaxy"));
                npc.setInstance(server.instanceContainer);
                npc.teleport(randomLoc);
                npc.setChestplate(ItemStack.builder(Material.ELYTRA).build());
                npc.setItemInMainHand(ItemStack.builder(Material.OBSIDIAN).build());
                npc.addEffect(new Potion(PotionEffect.SPEED, (byte) 10, 10000000));
                server.npcEntities.add(npc);
                Scheduler scheduler = MinecraftServer.getSchedulerManager();
                scheduler.scheduleNextTick(() -> npc.getNavigator().jump(3));
                scheduler.submitTask(() -> {
                    if(npc.isOnGround()) {
//                        if(player.getPosition().y() > npc.getPosition().y()) {
//                            npc.getNavigator().jump(5);
//                            npc.teleport(new Pos(npc.getPosition().x(), npc.getPosition().y(), npc.getPosition().z(), npc.getPosition().yaw(), 90));
//                            player.sendPacket(new EntityAnimationPacket(npc.getEntityId(), EntityAnimationPacket.Animation.SWING_MAIN_ARM));
////                            player.sendPacket(new NamedSoundEffectPacket(Material.OBSIDIAN.g, Sound.Source.BLOCK, (int) npc.getPosition().x(), (int) npc.getPosition().y(), (int) npc.getPosition().z(), 1, 1));
//
//                            server.instanceContainer.setBlock(npc.getPosition(), Material.OBSIDIAN.block());
//                        }
                        npc.getNavigator().setPathTo(player.getPosition());
                    }
                    return TaskSchedule.millis(750);
                });

            }
        }, amount);
    }

//    public Sound breakblockSound(Block block) {
//        block.getProperty("")
//    }
}

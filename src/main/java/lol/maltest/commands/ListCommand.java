package lol.maltest.commands;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ListCommand extends Command {
    public ListCommand() {
        super("list");

        setDefaultExecutor((sender, args) -> {
            sender.sendMessage("Online players: " + MinecraftServer.getConnectionManager().getOnlinePlayers().size());
        });
    }
}

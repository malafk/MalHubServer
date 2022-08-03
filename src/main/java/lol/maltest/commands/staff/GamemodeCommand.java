package lol.maltest.commands.staff;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GamemodeCommand extends Command {
    public GamemodeCommand() {
        super("gamemode", "gm");

        setDefaultExecutor((sender, args) -> {
            sender.sendMessage("/gamemode <mode>");
        });

        var mode = ArgumentType.String("mode");

        addSyntax((sender, args) -> {
            if(!(sender instanceof Player)) {
                sender.sendMessage("Only players can use this command.");
                return;
            }
            Player player = ((Player) sender);
            String mode1 = args.get("mode");
            switch (mode1.toLowerCase()) {
                case "c":
                case "creative":
                    player.setGameMode(GameMode.CREATIVE);
                    break;
                case "s":
                case "survival":
                    player.setGameMode(GameMode.SURVIVAL);
                    break;
                case "sp":
                case "spectator":
                    player.setGameMode(GameMode.SPECTATOR);
                    break;
                case "a":
                case "adventure":
                    player.setGameMode(GameMode.ADVENTURE);
                    break;
                default:
                    player.sendMessage("Unknown gamemode: " + mode1);
                    break;
            }
        }, mode);
    }
}

package cc.aesth.semihardcore;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReviveCommand implements CommandExecutor {

    private final SemiHardcore plugin;

    public ReviveCommand(SemiHardcore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("semihardcore.revive")) {
            sender.sendMessage(Component.text("You don't have permission to use this command.")
                    .color(NamedTextColor.RED));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(Component.text("Usage: /revive <player>")
                    .color(NamedTextColor.RED));
            return true;
        }

        String targetName = args[0];
        Player reviverPlayer = (sender instanceof Player p) ? p : null;

        // Admin command does NOT consume an item
        boolean success = ReviveLogic.reviveByName(reviverPlayer, targetName, false);
        if (!success) {
            sender.sendMessage(Component.text("Could not find a dead player named: " + targetName)
                    .color(NamedTextColor.RED));
        }

        return true;
    }
}

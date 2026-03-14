package cc.aesth.semihardcore;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class ReviveLogic {

    private ReviveLogic() {
    }

    /**
     * Revives a dead (spectator) player by UUID.
     *
     * @param reviver     the player performing the revival (null = console)
     * @param targetUUID  UUID of the dead player to revive
     * @param consumeItem whether to consume one Soul Amber from the reviver's hand
     * @return true if revival succeeded, false otherwise
     */
    public static boolean revive(Player reviver, UUID targetUUID, boolean consumeItem) {
        SemiHardcore plugin = SemiHardcore.getInstance();
        Map<UUID, OfflinePlayer> dead = plugin.getDeadPlayers();

        OfflinePlayer target = dead.get(targetUUID);
        if (target == null) {
            if (reviver != null) {
                reviver.sendMessage(Component.text("That player is not in the dead players list.")
                        .color(NamedTextColor.RED));
            }
            return false;
        }

        // Remove from dead list
        plugin.removeDeadPlayer(targetUUID);

        // Consume Soul Amber if requested
        if (consumeItem && reviver != null) {
            ItemStack hand = reviver.getInventory().getItemInMainHand();
            if (hand.getAmount() > 1) {
                hand.setAmount(hand.getAmount() - 1);
            } else {
                reviver.getInventory().setItemInMainHand(null);
            }
        }

        String targetName = target.getName() != null ? target.getName() : targetUUID.toString();

        // If the player is online, set them back to survival
        Player online = Bukkit.getPlayer(targetUUID);
        if (online != null && online.isOnline()) {
            online.setGameMode(GameMode.SURVIVAL);

            // Teleport to spawn so they aren't stuck at their death location
            online.teleport(online.getWorld().getSpawnLocation());

            online.setHealth(20.0);
            online.setFoodLevel(20);
            online.sendMessage(Component.text("☀ You have been revived at spawn! Welcome back.")
                    .color(NamedTextColor.GOLD));
        }

        // Broadcast revival message
        String reviverName = reviver != null ? reviver.getName() : "an Admin";
        Component broadcast = Component.text()
                .append(Component.text("☀ ", NamedTextColor.YELLOW))
                .append(Component.text(targetName, NamedTextColor.GOLD)
                        .decoration(TextDecoration.BOLD, true))
                .append(Component.text(" has been revived by ", NamedTextColor.YELLOW))
                .append(Component.text(reviverName, NamedTextColor.AQUA))
                .append(Component.text("!", NamedTextColor.YELLOW))
                .build();
        Bukkit.broadcast(broadcast);

        return true;
    }

    /**
     * Convenience overload: revive by display name (searches the dead players map).
     */
    public static boolean reviveByName(Player reviver, String targetName, boolean consumeItem) {
        SemiHardcore plugin = SemiHardcore.getInstance();
        for (Map.Entry<UUID, OfflinePlayer> entry : plugin.getDeadPlayers().entrySet()) {
            OfflinePlayer op = entry.getValue();
            if (op.getName() != null && op.getName().equalsIgnoreCase(targetName)) {
                return revive(reviver, entry.getKey(), consumeItem);
            }
        }
        if (reviver != null) {
            reviver.sendMessage(Component.text("No dead player found with name: " + targetName)
                    .color(NamedTextColor.RED));
        }
        return false;
    }

    /**
     * Formats a date for lore display.
     */
    public static String formatDate(Date date) {
        if (date == null)
            return "Unknown";
        return new SimpleDateFormat("MMM d, yyyy").format(date);
    }
}

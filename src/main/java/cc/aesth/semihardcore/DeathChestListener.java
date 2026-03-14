package cc.aesth.semihardcore;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class DeathChestListener implements Listener {

    private final SemiHardcore plugin;

    public DeathChestListener(SemiHardcore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        World world = player.getWorld();

        // Only act in hardcore worlds
        if (!world.isHardcore())
            return;

        // Register player as dead
        plugin.addDeadPlayer(player);

        // Collect items before the event clears them
        ItemStack[] contents = event.getDrops().toArray(new ItemStack[0]);

        // Suppress natural drops — we place a chest instead
        event.getDrops().clear();

        Location deathLoc = player.getLocation().clone();
        String playerName = player.getName();

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            // Set spectator mode so the player can observe the world
            player.setGameMode(GameMode.SPECTATOR);

            // Place death chest only if they had items
            boolean hasItems = false;
            for (ItemStack item : contents) {
                if (item != null && item.getType() != Material.AIR) {
                    hasItems = true;
                    break;
                }
            }

            if (!hasItems)
                return;

            Block chestBlock = findSafeBlock(deathLoc);
            if (chestBlock == null) {
                plugin.getLogger().warning("Could not find safe block for death chest at " + deathLoc);
                return;
            }

            chestBlock.setType(Material.CHEST);

            if (!(chestBlock.getState() instanceof Chest chest))
                return;

            Inventory chestInv = chest.getBlockInventory();
            for (ItemStack item : contents) {
                if (item != null && item.getType() != Material.AIR) {
                    chestInv.addItem(item);
                }
            }

            int x = chestBlock.getX(), y = chestBlock.getY(), z = chestBlock.getZ();
            Component msg = Component.text()
                    .append(Component.text("☠ ", NamedTextColor.DARK_RED))
                    .append(Component.text(playerName, NamedTextColor.RED))
                    .append(Component.text("'s items have been stored in a death chest at ", NamedTextColor.GRAY))
                    .append(Component.text(
                            String.format("[%d, %d, %d]", x, y, z),
                            NamedTextColor.YELLOW))
                    .append(Component.text(" in ", NamedTextColor.GRAY))
                    .append(Component.text(world.getName(), NamedTextColor.WHITE))
                    .build();

            plugin.getServer().broadcast(msg);
        });
    }

    private Block findSafeBlock(Location loc) {
        World world = loc.getWorld();
        if (world == null)
            return null;

        int x = loc.getBlockX();
        int z = loc.getBlockZ();
        int startY = loc.getBlockY();

        for (int y = startY; y < world.getMaxHeight() - 1; y++) {
            Block below = world.getBlockAt(x, y - 1, z);
            Block here = world.getBlockAt(x, y, z);
            Block above = world.getBlockAt(x, y + 1, z);

            if (below.getType().isSolid()
                    && (here.getType() == Material.AIR || !here.getType().isSolid())
                    && (above.getType() == Material.AIR || !above.getType().isSolid())) {
                return here;
            }
        }

        return world.getBlockAt(x, startY, z);
    }
}

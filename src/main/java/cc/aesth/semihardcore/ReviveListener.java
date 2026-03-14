package cc.aesth.semihardcore;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ReviveListener implements Listener {

    private static final String GUI_TITLE_PLAIN = "✦ Revival Chamber";
    private final List<Inventory> openGuis = new ArrayList<>();
    private final SemiHardcore plugin;

    public ReviveListener(SemiHardcore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        ItemStack item = event.getItem();
        if (!SoulAmber.isSoulAmber(item)) return;

        event.setCancelled(true);
        openRevivalGUI(event.getPlayer());
    }

    private void openRevivalGUI(Player player) {
        Map<UUID, OfflinePlayer> deadMap = plugin.getDeadPlayers();

        if (deadMap.isEmpty()) {
            player.sendMessage(Component.text("There are no souls to revive.")
                    .color(NamedTextColor.YELLOW));
            return;
        }

        List<Map.Entry<UUID, OfflinePlayer>> entries = new ArrayList<>(deadMap.entrySet());

        // Calculate rows needed + 1 border row, capped at 6 rows (54 slots)
        int contentSlots = entries.size();
        int rows = Math.max(1, (int) Math.ceil(contentSlots / 9.0)) + 1;
        rows = Math.min(rows, 6);
        int size = rows * 9;

        Inventory gui = plugin.getServer().createInventory(null, size,
                Component.text(GUI_TITLE_PLAIN)
                        .color(NamedTextColor.DARK_PURPLE)
                        .decoration(TextDecoration.BOLD, true));

        // Fill bottom row with purple glass pane border
        ItemStack pane = new ItemStack(Material.PURPLE_STAINED_GLASS_PANE);
        org.bukkit.inventory.meta.ItemMeta paneMeta = pane.getItemMeta();
        if (paneMeta != null) {
            paneMeta.displayName(Component.text(" ").decoration(TextDecoration.ITALIC, false));
            pane.setItemMeta(paneMeta);
        }
        for (int i = size - 9; i < size; i++) {
            gui.setItem(i, pane);
        }

        // Add player skull for each dead player
        int slot = 0;
        for (Map.Entry<UUID, OfflinePlayer> entry : entries) {
            if (slot >= size - 9) break;
            OfflinePlayer dead = entry.getValue();
            UUID uuid = entry.getKey();

            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
            if (skullMeta != null) {
                skullMeta.setOwningPlayer(dead);

                String name = dead.getName() != null ? dead.getName() : uuid.toString();
                skullMeta.displayName(Component.text(name)
                        .color(NamedTextColor.GOLD)
                        .decoration(TextDecoration.ITALIC, false)
                        .decoration(TextDecoration.BOLD, true));

                skullMeta.lore(List.of(
                        Component.text("Dead in hardcore.")
                                .color(NamedTextColor.GRAY)
                                .decoration(TextDecoration.ITALIC, false),
                        Component.empty(),
                        Component.text("Click to revive this soul.")
                                .color(NamedTextColor.GREEN)
                                .decoration(TextDecoration.ITALIC, false)
                ));

                // Store the UUID in the lore-accessible PDC of the skull so we can look it up on click
                skullMeta.getPersistentDataContainer().set(
                        new org.bukkit.NamespacedKey(plugin, "dead_uuid"),
                        org.bukkit.persistence.PersistentDataType.STRING,
                        uuid.toString()
                );
                skull.setItemMeta(skullMeta);
            }
            gui.setItem(slot++, skull);
        }

        openGuis.add(gui);
        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inv = event.getInventory();
        if (!openGuis.contains(inv)) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() != Material.PLAYER_HEAD) return;

        org.bukkit.inventory.meta.ItemMeta meta = clicked.getItemMeta();
        if (meta == null) return;

        // Retrieve UUID stored in the skull PDC
        String uuidStr = meta.getPersistentDataContainer().get(
                new org.bukkit.NamespacedKey(plugin, "dead_uuid"),
                org.bukkit.persistence.PersistentDataType.STRING
        );
        if (uuidStr == null) return;

        UUID targetUUID;
        try {
            targetUUID = UUID.fromString(uuidStr);
        } catch (IllegalArgumentException e) {
            return;
        }

        if (!(event.getWhoClicked() instanceof Player reviver)) return;

        reviver.closeInventory();
        openGuis.remove(inv);

        UUID finalTargetUUID = targetUUID;
        plugin.getServer().getScheduler().runTask(plugin, () ->
                ReviveLogic.revive(reviver, finalTargetUUID, true)
        );
    }
}

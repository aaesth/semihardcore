package cc.aesth.semihardcore;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class SoulAmber {

    public static final NamespacedKey KEY = new NamespacedKey(SemiHardcore.getInstance(), "soul_amber");

    private SoulAmber() {}

    /**
     * Creates a new Soul Amber ItemStack with PDC tag, name, and lore.
     */
    public static ItemStack create() {
        ItemStack item = new ItemStack(Material.GHAST_TEAR);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.displayName(Component.text("Soul Amber")
                .color(NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false));

        meta.lore(List.of(
                Component.text("Contains a lingering soul, not yet lost.")
                        .color(NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.empty(),
                Component.text("Right-click to open the Revival Chamber.")
                        .color(NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, false)
        ));

        meta.getPersistentDataContainer().set(KEY, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Returns true if the given ItemStack is a Soul Amber.
     */
    public static boolean isSoulAmber(ItemStack item) {
        if (item == null || item.getType() != Material.GHAST_TEAR) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(KEY, PersistentDataType.BYTE);
    }
}

package cc.aesth.semihardcore;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SemiHardcore extends JavaPlugin {

    private static SemiHardcore instance;

    /**
     * Tracks players who died in a hardcore world.
     * Key = UUID, Value = OfflinePlayer snapshot for skull rendering.
     */
    private final Map<UUID, OfflinePlayer> deadPlayers = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;

        // Register listeners
        getServer().getPluginManager().registerEvents(new ReviveListener(this), this);
        getServer().getPluginManager().registerEvents(new DeathChestListener(this), this);

        // Register /revive command
        getCommand("revive").setExecutor(new ReviveCommand(this));

        // Register Soul Amber crafting recipe
        registerRecipe();

        getLogger().info("SemiHardcore enabled. Soul Amber ready.");
    }

    @Override
    public void onDisable() {
        getLogger().info("SemiHardcore disabled.");
    }

    private void registerRecipe() {
        ItemStack result = SoulAmber.create();
        NamespacedKey key = new NamespacedKey(this, "soul_amber_recipe");

        ShapedRecipe recipe = new ShapedRecipe(key, result);
        recipe.shape(
            " D ",
            "DGD",
            " D "
        );
        recipe.setIngredient('D', Material.DIAMOND);
        recipe.setIngredient('G', Material.GHAST_TEAR);

        Bukkit.addRecipe(recipe);
    }

    public void addDeadPlayer(OfflinePlayer player) {
        deadPlayers.put(player.getUniqueId(), player);
    }

    public void removeDeadPlayer(UUID uuid) {
        deadPlayers.remove(uuid);
    }

    public boolean isDeadPlayer(UUID uuid) {
        return deadPlayers.containsKey(uuid);
    }

    public Map<UUID, OfflinePlayer> getDeadPlayers() {
        return Collections.unmodifiableMap(deadPlayers);
    }

    public static SemiHardcore getInstance() {
        return instance;
    }
}

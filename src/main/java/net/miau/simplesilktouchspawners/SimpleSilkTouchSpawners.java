package net.miau.simplesilktouchspawners;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

public final class SimpleSilkTouchSpawners extends JavaPlugin implements Listener {

    private boolean needsPermissionToBreak = false;
    private boolean needsPermissionToPlace = false;

    @Override
    public void onEnable() {
        getConfig().options().header("Permissions: 'simpleSTS.break' & 'simpleSTS.place'");
        getConfig().options().copyDefaults(true);
        saveConfig();
        if (!getConfig().contains("needsPermissionToBreak")) {
            getConfig().set("needsPermissionToBreak", this.needsPermissionToBreak);
            saveConfig();
        }
        this.needsPermissionToBreak = getConfig().getBoolean("needsPermissionToBreak");
        if (!getConfig().contains("needsPermissionToPlace")) {
            getConfig().set("needsPermissionToPlace", this.needsPermissionToPlace);
            saveConfig();
        }
        this.needsPermissionToPlace = getConfig().getBoolean("needsPermissionToPlace");

        Bukkit.getPluginManager().registerEvents(this, this);

        new Metrics(this, 17392);

        try {
            URL url = new URL("https://api.github.com/repos/innocentmiau/SimpleSilkTouchSpawners/releases/latest");
            String s = stream(url);
            String version = s.substring(s.indexOf("\"tag_name\":\"") + 13, s.indexOf("\"target_commitish\"") - 2);
            if (!version.equals(this.getDescription().getVersion())) {
                getLogger().info("---[SimpleSilkTouchSpawners]---");
                getLogger().info("[>] There is a new update available.");
                getLogger().info("[>] current version: " + this.getDescription().getVersion());
                getLogger().info("[>] latest version: " + version);
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }

    }

    @Override
    public void onDisable() {
    }

    public String stream(URL url) throws IOException {
        try (InputStream input = url.openStream()) {
            InputStreamReader isr = new InputStreamReader(input);
            BufferedReader reader = new BufferedReader(isr);
            StringBuilder json = new StringBuilder();
            int c;
            while ((c = reader.read()) != -1) {
                json.append((char) c);
            }
            return json.toString();
        }
    }

    @EventHandler
    public void onBBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack i = player.getInventory().getItemInMainHand();
        if (event.getBlock().getType().toString().contains("SPAWNER") && (
                i.getType() == Material.DIAMOND_PICKAXE || i.getType().toString().contains("NETHERITE_PICKAXE")) &&
                i.getEnchantments().containsKey(Enchantment.SILK_TOUCH)) {
            if (this.needsPermissionToBreak && !player.hasPermission("simpleSTS.break")) return;
            event.setExpToDrop(0);
            CreatureSpawner cs = (CreatureSpawner) event.getBlock().getState();
            String type = cs.getCreatureTypeName().toString();
            Material material = Material.getMaterial("SPAWNER");
            if (material == null) {
                material = Material.getMaterial("MOB_SPAWNER");
                if (material == null) {
                    player.sendMessage("§cAn error occurred, please contact de dev.");
                    return;
                }
            }
            ItemStack is = new ItemStack(material);
            if (type.length() > 0) {
                ItemMeta ism = is.getItemMeta();
                ArrayList<String> lore = new ArrayList<>();
                lore.add(type);
                ism.setLore(lore);
                is.setItemMeta(ism);
            }
            event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), is);
        }
    }

    @EventHandler
    public void onBPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        if (block.getType().toString().equalsIgnoreCase("SPAWNER")
                || block.getType().toString().equalsIgnoreCase("MOB_SPAWNER")) {
            Player player = event.getPlayer();
            if (this.needsPermissionToPlace && !player.hasPermission("simpleSTS.place")) return;
            ItemStack i = player.getItemInHand();
            if (i.getItemMeta() != null && i.getItemMeta().getLore() != null && i.getItemMeta().getLore().size() > 0) {
                CreatureSpawner cs = (CreatureSpawner)event.getBlock().getState();
                String type = i.getItemMeta().getLore().get(0);
                cs.setCreatureTypeByName(type);
                cs.update();
            }
        }
    }
}

package io.sn.storexp;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

import static io.sn.storexp.Genesis.*;

public class Utils {

    public static int getCfgLevelPerCost() {
        return Genesis.CONFIG.getInt("level-per-store", 5);
    }

    public static List<Component> getCfgItemLore(int xpPointValue) {
        return Genesis.CONFIG.getStringList("item-lore").stream().map(s -> {
            var replaced = s.replaceAll("%value%", String.valueOf(xpPointValue));
            return MINIMSG.deserialize(replaced);
        }).collect(Collectors.toList());
    }

    public enum PlaysoundType {
        USE("sound.on-use"), STORE("sound.on-store");

        private final String path;

        PlaysoundType(String path) {
            this.path = path;
        }
    }

    public static void cfgPlaySoundFor(Player plr, PlaysoundType type) {
        var orig = CONFIG.getString(type.path);
        if (orig == null || orig.isEmpty()) return;

        var c = orig.split(";");
        if (c.length != 3) return;

        plr.playSound(plr, Sound.valueOf(c[0]), Float.parseFloat(c[1]), Float.parseFloat(c[2]));
    }

    public static void cfgSendMessage(Player plr, String key) {
        var msg = CONFIG.getString("message." + key);
        if (msg == null) throw new IllegalStateException("Invalid message in config.yml: message." + key);
        plr.sendMessage(MINIMSG.deserialize(msg));
    }

    public static @NotNull ItemStack generateBottleWithXpPointValue(int xpPointValue) {
        var itm = new ItemStack(Material.EXPERIENCE_BOTTLE);
        itm.editMeta(m -> {
            m.lore(getCfgItemLore(xpPointValue));
            m.getPersistentDataContainer().set(PDC_KEY, PersistentDataType.INTEGER, xpPointValue);
        });
        return itm;
    }

    public static int parseBottleToXpPointValue(@NotNull ItemStack itm) {
        if (!itm.hasItemMeta()) return -1;
        PersistentDataContainer pdc;
        if (!(pdc = itm.getItemMeta().getPersistentDataContainer()).has(PDC_KEY, PersistentDataType.INTEGER)) return -1;
        //noinspection DataFlowIssue
        return pdc.get(PDC_KEY, PersistentDataType.INTEGER);
    }

    public static boolean isStoredBottle(@NotNull ItemStack itm) {
        return itm.getType() == Material.EXPERIENCE_BOTTLE && itm.getItemMeta().getPersistentDataContainer().has(PDC_KEY, PersistentDataType.INTEGER);
    }

    public static void giveBottleWithValue(@NotNull Player target, int xpPointValue) {
        final var dropOnGround = target.getInventory().firstEmpty() == -1;
        final var itm = generateBottleWithXpPointValue(xpPointValue);

        if (dropOnGround) {
            target.getWorld().dropItem(target.getLocation(), itm);
        } else {
            target.getInventory().addItem(itm);
        }
    }

}

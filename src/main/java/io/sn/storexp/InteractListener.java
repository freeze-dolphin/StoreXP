package io.sn.storexp;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownExpBottle;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class InteractListener implements org.bukkit.event.Listener {

    private static final ItemStack EMPTY_BOTTLE = new ItemStack(Material.GLASS_BOTTLE);

    @EventHandler
    public void onRightClick(PlayerInteractEvent evt) {
        if (!evt.getPlayer().hasPermission("storexp.use")) return;
        if (!evt.hasItem()) return;
        if (evt.getItem() == null || evt.getHand() == null) return;

        if (evt.getPlayer().isSneaking() && evt.hasBlock() && evt.getClickedBlock() != null && evt.getAction() == Action.RIGHT_CLICK_BLOCK && evt.getClickedBlock().getType() == Material.ENCHANTING_TABLE) {
            // store
            if (Utils.isStoredBottle(evt.getItem())) {
                evt.setUseItemInHand(Event.Result.DENY);
                evt.setCancelled(true);
                use(evt);
                return;
            }

            if (!evt.getItem().isSimilar(EMPTY_BOTTLE)) return;
            if (evt.getPlayer().getLevel() < Utils.getCfgLevelPerCost()) {
                Utils.cfgSendMessage(evt.getPlayer(), "no-sufficient");
                return;
            }

            evt.setUseItemInHand(Event.Result.DENY);
            evt.setCancelled(true);

            store(evt);

        } else if (evt.getPlayer().isSneaking() && (evt.getAction() == Action.RIGHT_CLICK_BLOCK || evt.getAction() == Action.RIGHT_CLICK_AIR)) {
            // unbox
            if (!Utils.isStoredBottle(evt.getItem())) return;

            evt.setUseItemInHand(Event.Result.DENY);
            evt.setCancelled(true);

            use(evt);

        }
    }

    @EventHandler
    public void onThrow(ProjectileLaunchEvent evt) {
        if (evt.getEntityType() == EntityType.EXPERIENCE_BOTTLE) {
            if (evt.getEntity().getShooter() instanceof Player plr) {
                if (Utils.isStoredBottle(plr.getInventory().getItemInMainHand())) {
                    evt.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onDispenser(BlockDispenseEvent evt) {
        if (Utils.isStoredBottle(evt.getItem())) {
            evt.setCancelled(true);
        }
    }

    private static void store(PlayerInteractEvent evt) {
        var before = evt.getPlayer().calculateTotalExperiencePoints();
        evt.getPlayer().setLevel(evt.getPlayer().getLevel() - Utils.getCfgLevelPerCost());
        var after = evt.getPlayer().calculateTotalExperiencePoints();

        var held = evt.getPlayer().getInventory().getItem(evt.getHand());
        evt.getPlayer().getInventory().setItem(evt.getHand(), held.subtract());
        Utils.giveBottleWithValue(evt.getPlayer(), before - after);

        Utils.cfgPlaySoundFor(evt.getPlayer(), Utils.PlaysoundType.STORE);
    }

    private static void use(PlayerInteractEvent evt) {
        var value = Utils.parseBottleToXpPointValue(evt.getItem());
        var held = evt.getPlayer().getInventory().getItem(evt.getHand());
        evt.getPlayer().getInventory().setItem(evt.getHand(), held.subtract());
        evt.getPlayer().setExperienceLevelAndProgress(evt.getPlayer().calculateTotalExperiencePoints() + value);

        Utils.cfgPlaySoundFor(evt.getPlayer(), Utils.PlaysoundType.USE);
    }

}
